//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

use std::cmp::min;
use std::fs::File;
use std::io::Read;

use crate::{DecodedData, INDEX_TABLE, STEP_TABLE};

/// Decodes a wav file. As this is an IMA ADPCM decoder only, feeding a file that is not IMA ADPCM encoded leads to a crash.
/// The output will be 16-Bit linear PCM. If there are 2 channels,
/// the samples will be interleaved - left channel sample, right channel sample, left... and so on
pub fn decode_wav_file(path: &str) -> Result<DecodedData, String> {
    // read header
    let mut file = File::open(path).expect("Failed to open file");

    // riff chunk
    let (riff_id, riff_data) = read_wav_chunk(&mut file);
    if riff_id != *b"RIFF" {
        return Err("RIFF chunk not found".to_string());
    }
    if riff_data[..4] != *b"WAVE" {
        return Err("WAVE literal not found in RIFF chunk".to_string());
    }

    // fmt chunk
    let (fmt_id, fmt_data) = read_wav_chunk(&mut file);
    if fmt_id != *b"fmt " {
        return Err("fmt chunk not found".to_string());
    }
    let w_format_tag = u16::from_le_bytes([fmt_data[0], fmt_data[1]]);
    let num_channels = u16::from_le_bytes([fmt_data[2], fmt_data[3]]);
    let sample_rate = u32::from_le_bytes([fmt_data[4], fmt_data[5], fmt_data[6], fmt_data[7]]);
    let block_align = u16::from_le_bytes([fmt_data[12], fmt_data[13]]) as usize;
    let sub_format: Option<u16> = match fmt_data.len() > 24 {
        true => Some(u16::from_le_bytes([fmt_data[24], fmt_data[25]])),
        false => None,
    };

    // validate
    if !(1..=2).contains(&num_channels) {
        return Err(format!("Invalid number of channels: {num_channels}"));
    }
    if w_format_tag != 0xFFFE && w_format_tag != 0x0011 {
        return Err(format!("Unsupported audio format: {w_format_tag}"));
    }
    if w_format_tag == 0xFFFE {
        match sub_format {
            Some(value) => {
                if value != 0x0011 {
                    return Err(format!("Unsupported sub-format: {value}"));
                }
            }
            None => {
                return Err(
                    "WAVE FORMAT EXTENSIBLE detected but no sub-format specified".to_string(),
                );
            }
        }
    }

    // find data block and decode it
    let audio_data = loop {
        let (chunk_id, chunk_data) = read_wav_chunk(&mut file);
        if chunk_id == *b"data" {
            break chunk_data;
        }
    };
    let pcm_data = decode(audio_data.as_slice(), block_align, num_channels == 2);

    Ok(DecodedData {
        pcm_data,
        sample_rate,
        bits_per_sample: 16,
        num_channels,
        block_size: block_align,
    })
}

fn read_wav_chunk(file: &mut File) -> ([u8; 4], Vec<u8>) {
    let mut chunk_info: [u8; 8] = [0; 8];
    file.read_exact(&mut chunk_info)
        .expect("Error reading wav chunk");
    let chunk_id: [u8; 4] = chunk_info[..4].try_into().unwrap();
    let chunk_size = if chunk_id == *b"RIFF" {
        4
    } else {
        u32::from_le_bytes(chunk_info[4..8].try_into().unwrap()) as usize
    };
    let mut chunk_data = vec![0; chunk_size];
    file.read_exact(chunk_data.as_mut_slice())
        .expect("Error reading wav chunk data");
    (chunk_id, chunk_data)
}

/// Decodes a slice of IMA ADPCM audio data.
/// The output will be 16-Bit linear PCM. If there are 2 channels,
/// the samples will be interleaved - left channel sample, right channel sample, left... and so on
pub fn decode(input_data: &[u8], block_size: usize, stereo: bool) -> Vec<u8> {
    let mut pcm_data: Vec<u8> = vec![];
    let mut pcm_data_left: Vec<u8> = vec![];
    let mut pcm_data_right: Vec<u8> = vec![];
    let mut data_pos = 0;
    let mut pcm_bytes_total = 0;
    while data_pos < input_data.len() {
        let this_block_size = min(block_size, input_data.len() - data_pos);
        let block_data = &input_data[data_pos..data_pos + this_block_size];
        data_pos += block_data.len();
        if stereo {
            let pcm_bytes =
                decode_block(block_data, stereo, &mut pcm_data_left, &mut pcm_data_right);
            let interleave_data = InterleaveData {
                input_left: &pcm_data_left,
                input_right: &pcm_data_right,
                offset: pcm_bytes_total / 2,
                length: pcm_bytes / 2,
                output: &mut pcm_data,
            };
            interleave(interleave_data);
            pcm_bytes_total += pcm_bytes;
        } else {
            decode_block(block_data, stereo, &mut pcm_data, &mut pcm_data_right);
        }
    }

    pcm_data
}

#[inline]
fn interleave(data: InterleaveData) {
    for i in (data.offset..data.offset + data.length).step_by(2) {
        data.output.push(data.input_left[i]);
        data.output.push(data.input_left[i + 1]);
        data.output.push(data.input_right[i]);
        data.output.push(data.input_right[i + 1]);
    }
}

#[inline]
fn decode_block(
    data: &[u8],
    stereo: bool,
    output_left: &mut Vec<u8>,
    output_right: &mut Vec<u8>,
) -> usize {
    let mut left_prediction = Prediction::new();
    let mut right_prediction = Prediction::new();
    let preamble_bytes = match stereo {
        true => 8,
        false => 4,
    };

    // read left channel preamble
    left_prediction.predictor = data[0] as u16 | ((data[1] as u16) << 8);
    left_prediction.step_index = num::clamp(data[2], 0, 88) as isize;
    left_prediction.step = STEP_TABLE[left_prediction.step_index as usize];

    // read right channel preamble
    if stereo {
        right_prediction.predictor = data[4] as u16 | ((data[5] as u16) << 8);
        right_prediction.step_index = num::clamp(data[6], 0, 88) as isize;
        right_prediction.step = STEP_TABLE[right_prediction.step_index as usize];
    }

    // decode block
    let mut byte_channel_counter = 0;
    let mut left = true;
    for &input_byte in &data[preamble_bytes..] {
        // decode
        let nibble1 = input_byte & 0b1111;
        let nibble2 = input_byte >> 4;
        let sample1 = match left {
            true => decode_nibble(nibble1, &mut left_prediction),
            false => decode_nibble(nibble1, &mut right_prediction),
        };
        let sample2 = match left {
            true => decode_nibble(nibble2, &mut left_prediction),
            false => decode_nibble(nibble2, &mut right_prediction),
        };

        // split samples into bytes
        let sample1_bytes = sample1.to_le_bytes();
        let sample2_bytes = sample2.to_le_bytes();

        // write output
        if left {
            output_left.push(sample1_bytes[0]);
            output_left.push(sample1_bytes[1]);
            output_left.push(sample2_bytes[0]);
            output_left.push(sample2_bytes[1]);
        } else {
            output_right.push(sample1_bytes[0]);
            output_right.push(sample1_bytes[1]);
            output_right.push(sample2_bytes[0]);
            output_right.push(sample2_bytes[1]);
        }

        // setup for next byte
        if stereo {
            byte_channel_counter += 1;
            if byte_channel_counter >= 4 {
                byte_channel_counter = 0;
                left = !left;
            }
        }
    }

    (data.len() - preamble_bytes) * 4
}

#[inline]
fn decode_nibble(nibble: u8, prediction: &mut Prediction) -> u16 {
    let sign = nibble & 0b1000;
    let delta = nibble & 0b111;

    prediction.step_index = prediction
        .step_index
        .wrapping_add(INDEX_TABLE[nibble as usize] as isize);
    prediction.step_index = num::clamp(prediction.step_index, 0, 88);

    let mut diff = prediction.step >> 3;
    if (delta & 4) != 0 {
        diff = diff.wrapping_add(prediction.step);
    }
    if (delta & 2) != 0 {
        diff = diff.wrapping_add(prediction.step >> 1);
    }
    if (delta & 1) != 0 {
        diff = diff.wrapping_add(prediction.step >> 2);
    }
    if sign != 0 {
        prediction.predictor = prediction.predictor.wrapping_sub(diff);
    } else {
        prediction.predictor = prediction.predictor.wrapping_add(diff);
    }

    prediction.step = STEP_TABLE[prediction.step_index as usize];
    prediction.predictor
}

struct Prediction {
    predictor: u16,
    step_index: isize,
    step: u16,
}

impl Prediction {
    fn new() -> Self {
        Self {
            predictor: 0,
            step_index: 0,
            step: 0,
        }
    }
}

struct InterleaveData<'a> {
    input_left: &'a Vec<u8>,
    input_right: &'a Vec<u8>,
    offset: usize,
    length: usize,
    output: &'a mut Vec<u8>,
}
