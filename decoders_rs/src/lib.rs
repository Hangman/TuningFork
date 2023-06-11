//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

use std::slice;

use jni::objects::{AutoArray, JClass, JString, JValue, ReleaseMode};
use jni::sys::{jboolean, jbyte, jobject};
use jni::{
    objects::JObject,
    sys::{jbyteArray, jint},
    JNIEnv,
};

pub use crate::ima_adpcm::decoder::{decode, decode_wav_file};

pub mod common;
pub mod flac;
pub mod ima_adpcm;

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
        .expect("Failed to convert jbyteArray to AutoArray<jbyte>");
    let input_size = env
        .get_array_length(input)
        .expect("Failed to retrieve the size of input");
    let input_data =
        unsafe { slice::from_raw_parts(input_elements.as_ptr() as *const u8, input_size as usize) };

    let pcm_data = decode(input_data, block_size as usize, stereo != 0);

    let output = env
        .byte_array_from_slice(pcm_data.as_slice())
        .expect("Failed to convert the pcm data to jbyteArray");
    output
}

#[no_mangle]
pub extern "C" fn Java_de_pottgames_tuningfork_bindings_FlacRs_decodeFlac(
    env: JNIEnv,
    _object: JObject,
    input: jbyteArray,
) -> jobject {
    let input_elements: AutoArray<jbyte> = env
        .get_byte_array_elements(input, ReleaseMode::NoCopyBack)
        .expect("Failed to convert jbyteArray to AutoArray<jbyte>");
    let input_size = env
        .get_array_length(input)
        .expect("Failed to retrieve the size of input");
    let input_data =
        unsafe { slice::from_raw_parts(input_elements.as_ptr() as *const u8, input_size as usize) };

    let decoded_data = flac::decoder::decode(input_data).expect("Failed to decode flac data");

    let ima_adpcm_data_class = env
        .find_class("de/pottgames/tuningfork/bindings/ImaAdpcmData")
        .expect("Failed to find class de.pottgames.tuningfork.bindings.ImaAdpcmData");
    let pcm_data_array = env
        .byte_array_from_slice(decoded_data.pcm_data.as_slice())
        .expect("Failed to create byte array") as jobject;

    let ima_adpcm_data_obj = env
        .new_object(
            ima_adpcm_data_class,
            "([BIII)V",
            &[
                JValue::from(unsafe { JObject::from_raw(pcm_data_array) }),
                JValue::from(decoded_data.sample_rate as i32),
                JValue::from(decoded_data.bits_per_sample as i32),
                JValue::from(decoded_data.num_channels as i32),
            ],
        )
        .expect("Failed to create ImaAdpcmData object");

    ima_adpcm_data_obj.into_raw()
}

#[no_mangle]
pub extern "C" fn Java_de_pottgames_tuningfork_bindings_ImaAdpcmRs_decodeFile(
    env: JNIEnv,
    _class: JClass,
    path: JString,
) -> jobject {
    let path: String = env
        .get_string(path)
        .expect("Failed to get Java string")
        .into();
    let decoded_data = decode_wav_file(&path).expect("Failed to decode WAV file");
    let ima_adpcm_data_class = env
        .find_class("de/pottgames/tuningfork/bindings/ImaAdpcmData")
        .expect("Failed to find class de.pottgames.tuningfork.bindings.ImaAdpcmData");
    let pcm_data_array = env
        .byte_array_from_slice(decoded_data.pcm_data.as_slice())
        .expect("Failed to create byte array") as jobject;

    let ima_adpcm_data_obj = env
        .new_object(
            ima_adpcm_data_class,
            "([BIII)V",
            &[
                JValue::from(unsafe { JObject::from_raw(pcm_data_array) }),
                JValue::from(decoded_data.sample_rate as i32),
                JValue::from(decoded_data.bits_per_sample as i32),
                JValue::from(decoded_data.num_channels as i32),
            ],
        )
        .expect("Failed to create ImaAdpcmData object");

    ima_adpcm_data_obj.into_raw()
}

pub struct DecodedData {
    pub pcm_data: Vec<u8>,
    pub sample_rate: u32,
    pub bits_per_sample: u16,
    pub num_channels: u16,
    pub block_size: usize,
}
