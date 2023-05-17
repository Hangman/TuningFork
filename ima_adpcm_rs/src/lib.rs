/**
 * Copyright 2023 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
use std::cmp::min;
use std::slice;

use jni::objects::{AutoArray, ReleaseMode};
use jni::sys::{jboolean, jbyte};
use jni::{
    objects::JObject,
    sys::{jbyteArray, jint},
    JNIEnv,
};

const STEP_TABLE: [u16; 89] = [
    7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66,
    73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449,
    494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272,
    2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493,
    10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767,
];

const INDEX_TABLE: [i8; 16] = [-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8];

#[no_mangle]
pub extern "C" fn Java_de_pottgames_tuningfork_bindings_ImaAdpcmRs_decode(
    env: JNIEnv,
    _object: JObject,
    input: jbyteArray,
    block_size: jint,
    stereo: jboolean,
) -> jbyteArray {
    let input_elements: AutoArray<jbyte> = env
        .get_byte_array_elements(input, ReleaseMode::NoCopyBack)
        .unwrap();
    let input_size = env.get_array_length(input).unwrap();
    let input_data =
        unsafe { slice::from_raw_parts(input_elements.as_ptr() as *const u8, input_size as usize) };

    // decode
    let mut pcm_data: Vec<u8> = vec![];
    let mut pcm_data_left: Vec<u8> = vec![];
    let mut pcm_data_right: Vec<u8> = vec![];
    let mut data_pos = 0;
    let mut pcm_bytes_total = 0;
    while data_pos < input_data.len() {
        let this_block_size = min(block_size as usize, input_data.len() - data_pos);
        let block_data = &input_data[data_pos..data_pos + this_block_size];
        data_pos += block_data.len();
        if stereo != 0 {
            let pcm_bytes = decode_block(
                block_data,
                stereo != 0,
                &mut pcm_data_left,
                &mut pcm_data_right,
            );
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
            decode_block(block_data, stereo != 0, &mut pcm_data, &mut pcm_data_right);
        }
    }

    // convert to jni output
    let output = env.byte_array_from_slice(pcm_data.as_slice()).unwrap();
    output
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
        let byte1 = (sample1 & 0xFF) as u8;
        let byte2 = ((sample1 >> 8) & 0xFF) as u8;
        let byte3 = (sample2 & 0xFF) as u8;
        let byte4 = ((sample2 >> 8) & 0xFF) as u8;

        // write output
        if left {
            output_left.push(byte1);
            output_left.push(byte2);
            output_left.push(byte3);
            output_left.push(byte4);
        } else {
            output_right.push(byte1);
            output_right.push(byte2);
            output_right.push(byte3);
            output_right.push(byte4);
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
