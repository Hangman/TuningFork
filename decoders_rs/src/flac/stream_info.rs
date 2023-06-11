//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

use std::fmt;
use std::fmt::{Display, Formatter};

pub struct StreamInfo {
    pub min_block_size: u16,
    pub max_block_size: u16,
    pub min_frame_size: u32,
    pub max_frame_size: u32,
    pub sample_rate: u32,
    pub num_channels: u8,
    pub bits_per_sample: u8,
    pub total_samples: u64,
}

impl StreamInfo {
    pub fn new(data: [u8; 34]) -> Self {
        let min_block_size = u16::from_be_bytes([data[0], data[1]]);
        let max_block_size = u16::from_be_bytes([data[2], data[3]]);

        let min_frame_size = u32::from_be_bytes([0, data[4], data[5], data[6]]);
        let max_frame_size = u32::from_be_bytes([0, data[7], data[8], data[9]]);

        let sample_rate_msb = u16::from_be_bytes([data[10], data[11]]);
        let sample_rate_lsb = (data[12] >> 4) & 0xF;
        let sample_rate = ((sample_rate_msb as u32) << 4) | (sample_rate_lsb as u32);

        let num_channels = ((data[12] >> 1) & 0b111) + 1;

        let bits_per_sample_msb = data[12] & 0b1;
        let bits_per_sample_lsb = (data[13] >> 4) & 0xF;
        let bits_per_sample = ((bits_per_sample_msb << 4) | bits_per_sample_lsb) + 1;

        let total_samples_msb = (data[13] & 0xF) as u64;
        let total_samples_lsb = u32::from_be_bytes([data[14], data[15], data[16], data[17]]) as u64;
        let total_samples = (total_samples_msb << 32) | total_samples_lsb;

        Self {
            min_block_size,
            max_block_size,
            min_frame_size,
            max_frame_size,
            sample_rate,
            num_channels,
            bits_per_sample,
            total_samples,
        }
    }
}

impl Display for StreamInfo {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "StreamInfo:\n\
            - Minimum Block Size: {}\n\
            - Maximum Block Size: {}\n\
            - Minimum Frame Size: {}\n\
            - Maximum Frame Size: {}\n\
            - Sample Rate: {}\n\
            - Number of Channels: {}\n\
            - Bits per Sample: {}\n\
            - Total Samples: {}",
            self.min_block_size,
            self.max_block_size,
            self.min_frame_size,
            self.max_frame_size,
            self.sample_rate,
            self.num_channels,
            self.bits_per_sample,
            self.total_samples
        )
    }
}
