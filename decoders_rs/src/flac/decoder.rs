//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

use std::io::Cursor;

use itertools::izip;

use crate::common::error::BasicError;
use crate::flac::reader::FlacReader;
use crate::DecodedData;

pub fn decode(input_data: &[u8]) -> Result<DecodedData, BasicError> {
    let stream = Cursor::new(input_data);
    let mut flac_reader = FlacReader::new(stream)?;
    let mut output: Vec<u8> = vec![];
    let bits_per_sample = flac_reader.streaminfo.bits_per_sample;
    let num_channels = flac_reader.streaminfo.num_channels;
    let sample_rate = flac_reader.streaminfo.sample_rate;

    let mut buffer = vec![0; 65536 * 8];

    while let Some(result) = flac_reader.decode_frame(sample_rate, &mut buffer)? {
        match num_channels {
            1 => {
                for value in &buffer[0..result] {
                    let bytes = match bits_per_sample {
                        8 => (*value).to_le_bytes()[0..1].to_vec(),
                        16 => (*value).to_le_bytes()[0..2].to_vec(),
                        24 => (*value).to_le_bytes()[0..3].to_vec(),
                        32 => (*value).to_le_bytes()[0..4].to_vec(),
                        _ => unreachable!("Unsupported bits_per_sample"),
                    };
                    output.extend_from_slice(&bytes);
                }
            }
            2 => {
                let (left, right) = buffer.split_at(result / 2);
                interleave_2_channels(bits_per_sample, left, right, &mut output);
            }
            3 => {
                let channel_size = result / 3;
                let left = &buffer[0..channel_size];
                let right = &buffer[channel_size..(channel_size * 2)];
                let center = &buffer[(channel_size * 2)..];
                interleave_3_channels(bits_per_sample, left, right, center, &mut output);
            }
            4 => {
                let channel_size = result / 4;
                let front_left = &buffer[0..channel_size];
                let front_right = &buffer[channel_size..(channel_size * 2)];
                let back_left = &buffer[(channel_size * 2)..(channel_size * 3)];
                let back_right = &buffer[(channel_size * 3)..];
                interleave_4_channels(
                    bits_per_sample,
                    front_left,
                    front_right,
                    back_left,
                    back_right,
                    &mut output,
                );
            }
            5 => {
                let channel_size = result / 5;
                let front_left = &buffer[0..channel_size];
                let front_right = &buffer[channel_size..(channel_size * 2)];
                let center = &buffer[(channel_size * 2)..(channel_size * 3)];
                let back_left = &buffer[(channel_size * 3)..(channel_size * 4)];
                let back_right = &buffer[(channel_size * 4)..];
                interleave_5_channels(
                    bits_per_sample,
                    front_left,
                    front_right,
                    center,
                    back_left,
                    back_right,
                    &mut output,
                );
            }
            6 => {
                let channel_size = result / 6;
                let front_left = &buffer[0..channel_size];
                let front_right = &buffer[channel_size..(channel_size * 2)];
                let center = &buffer[(channel_size * 2)..(channel_size * 3)];
                let lfe = &buffer[(channel_size * 3)..(channel_size * 4)];
                let back_left = &buffer[(channel_size * 4)..(channel_size * 5)];
                let back_right = &buffer[(channel_size * 5)..];
                interleave_6_channels(
                    bits_per_sample,
                    front_left,
                    front_right,
                    center,
                    lfe,
                    back_left,
                    back_right,
                    &mut output,
                );
            }
            7 => {
                let channel_size = result / 7;
                let front_left = &buffer[0..channel_size];
                let front_right = &buffer[channel_size..(channel_size * 2)];
                let front_center = &buffer[(channel_size * 2)..(channel_size * 3)];
                let lfe = &buffer[(channel_size * 3)..(channel_size * 4)];
                let back_center = &buffer[(channel_size * 4)..(channel_size * 5)];
                let back_left = &buffer[(channel_size * 5)..(channel_size * 6)];
                let back_right = &buffer[(channel_size * 6)..];
                interleave_7_channels(
                    bits_per_sample,
                    front_left,
                    front_right,
                    front_center,
                    lfe,
                    back_center,
                    back_left,
                    back_right,
                    &mut output,
                );
            }
            8 => {
                let channel_size = result / 8;
                let front_left = &buffer[0..channel_size];
                let front_right = &buffer[channel_size..(channel_size * 2)];
                let center = &buffer[(channel_size * 2)..(channel_size * 3)];
                let lfe = &buffer[(channel_size * 3)..(channel_size * 4)];
                let back_left = &buffer[(channel_size * 4)..(channel_size * 5)];
                let back_right = &buffer[(channel_size * 5)..(channel_size * 6)];
                let side_left = &buffer[(channel_size * 6)..(channel_size * 7)];
                let side_right = &buffer[(channel_size * 7)..];
                interleave_8_channels(
                    bits_per_sample,
                    front_left,
                    front_right,
                    center,
                    lfe,
                    back_left,
                    back_right,
                    side_left,
                    side_right,
                    &mut output,
                );
            }
            _ => panic!("Unsupported number of channels: {num_channels}"),
        }
    }

    Ok(DecodedData {
        pcm_data: output,
        sample_rate: flac_reader.streaminfo.sample_rate,
        bits_per_sample: flac_reader.streaminfo.bits_per_sample as u16,
        num_channels: flac_reader.streaminfo.num_channels as u16,
        block_size: flac_reader.streaminfo.max_block_size as usize,
    })
}

fn interleave_2_channels(bits_per_sample: u8, left: &[i32], right: &[i32], output: &mut Vec<u8>) {
    for (left_sample, right_sample) in left.iter().zip(right) {
        extend_by_sample(*left_sample, bits_per_sample, output);
        extend_by_sample(*right_sample, bits_per_sample, output);
    }
}

fn interleave_3_channels(
    bits_per_sample: u8,
    left: &[i32],
    right: &[i32],
    center: &[i32],
    output: &mut Vec<u8>,
) {
    for (left_sample, right_sample, center_sample) in izip!(left, right, center) {
        extend_by_sample(*left_sample, bits_per_sample, output);
        extend_by_sample(*right_sample, bits_per_sample, output);
        extend_by_sample(*center_sample, bits_per_sample, output);
    }
}

fn interleave_4_channels(
    bits_per_sample: u8,
    front_left: &[i32],
    front_right: &[i32],
    back_left: &[i32],
    back_right: &[i32],
    output: &mut Vec<u8>,
) {
    for (front_left_sample, front_right_sample, back_left_sample, back_right_sample) in
        izip!(front_left, front_right, back_left, back_right)
    {
        extend_by_sample(*front_left_sample, bits_per_sample, output);
        extend_by_sample(*front_right_sample, bits_per_sample, output);
        extend_by_sample(*back_left_sample, bits_per_sample, output);
        extend_by_sample(*back_right_sample, bits_per_sample, output);
    }
}

fn interleave_5_channels(
    bits_per_sample: u8,
    front_left: &[i32],
    front_right: &[i32],
    center: &[i32],
    back_left: &[i32],
    back_right: &[i32],
    output: &mut Vec<u8>,
) {
    for (
        front_left_sample,
        front_right_sample,
        center_sample,
        back_left_sample,
        back_right_sample,
    ) in izip!(front_left, front_right, center, back_left, back_right)
    {
        extend_by_sample(*front_left_sample, bits_per_sample, output);
        extend_by_sample(*front_right_sample, bits_per_sample, output);
        extend_by_sample(*center_sample, bits_per_sample, output);
        extend_by_sample(*back_left_sample, bits_per_sample, output);
        extend_by_sample(*back_right_sample, bits_per_sample, output);
    }
}

fn interleave_6_channels(
    bits_per_sample: u8,
    front_left: &[i32],
    front_right: &[i32],
    center: &[i32],
    lfe: &[i32],
    back_left: &[i32],
    back_right: &[i32],
    output: &mut Vec<u8>,
) {
    for (
        front_left_sample,
        front_right_sample,
        center_sample,
        lfe_sample,
        back_left_sample,
        back_right_sample,
    ) in izip!(front_left, front_right, center, lfe, back_left, back_right)
    {
        extend_by_sample(*front_left_sample, bits_per_sample, output);
        extend_by_sample(*front_right_sample, bits_per_sample, output);
        extend_by_sample(*center_sample, bits_per_sample, output);
        extend_by_sample(*lfe_sample, bits_per_sample, output);
        extend_by_sample(*back_left_sample, bits_per_sample, output);
        extend_by_sample(*back_right_sample, bits_per_sample, output);
    }
}

fn interleave_7_channels(
    bits_per_sample: u8,
    front_left: &[i32],
    front_right: &[i32],
    front_center: &[i32],
    lfe: &[i32],
    back_center: &[i32],
    side_left: &[i32],
    side_right: &[i32],
    output: &mut Vec<u8>,
) {
    for (
        front_left_sample,
        front_right_sample,
        front_center_sample,
        lfe_sample,
        back_center_sample,
        side_left_sample,
        side_right_sample,
    ) in izip!(
        front_left,
        front_right,
        front_center,
        lfe,
        back_center,
        side_left,
        side_right
    ) {
        extend_by_sample(*front_left_sample, bits_per_sample, output);
        extend_by_sample(*front_right_sample, bits_per_sample, output);
        extend_by_sample(*front_center_sample, bits_per_sample, output);
        extend_by_sample(*lfe_sample, bits_per_sample, output);
        extend_by_sample(*back_center_sample, bits_per_sample, output);
        extend_by_sample(*side_left_sample, bits_per_sample, output);
        extend_by_sample(*side_right_sample, bits_per_sample, output);
    }
}

fn interleave_8_channels(
    bits_per_sample: u8,
    front_left: &[i32],
    front_right: &[i32],
    center: &[i32],
    lfe: &[i32],
    back_left: &[i32],
    back_right: &[i32],
    side_left: &[i32],
    side_right: &[i32],
    output: &mut Vec<u8>,
) {
    for (
        front_left_sample,
        front_right_sample,
        center_sample,
        lfe_sample,
        back_left_sample,
        back_right_sample,
        side_left_sample,
        side_right_sample,
    ) in izip!(
        front_left,
        front_right,
        center,
        lfe,
        back_left,
        back_right,
        side_left,
        side_right
    ) {
        extend_by_sample(*front_left_sample, bits_per_sample, output);
        extend_by_sample(*front_right_sample, bits_per_sample, output);
        extend_by_sample(*center_sample, bits_per_sample, output);
        extend_by_sample(*lfe_sample, bits_per_sample, output);
        extend_by_sample(*back_left_sample, bits_per_sample, output);
        extend_by_sample(*back_right_sample, bits_per_sample, output);
        extend_by_sample(*side_left_sample, bits_per_sample, output);
        extend_by_sample(*side_right_sample, bits_per_sample, output);
    }
}

#[inline(always)]
fn extend_by_sample(value: i32, bits_per_sample: u8, output: &mut Vec<u8>) {
    let bytes = match bits_per_sample {
        8 => value.to_le_bytes()[0..1].to_vec(),
        16 => value.to_le_bytes()[0..2].to_vec(),
        24 => value.to_le_bytes()[0..3].to_vec(),
        32 => value.to_le_bytes()[0..4].to_vec(),
        _ => unreachable!("Unsupported bits_per_sample"),
    };
    output.extend_from_slice(&bytes);
}
