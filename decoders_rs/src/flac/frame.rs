//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

use std::io::Read;

use crate::common::error::BasicError;
use crate::flac::frame::ChannelAssignment::{
    Independent, LeftSideStereo, MidSideStereo, RightSideStereo,
};
use crate::flac::frame::FrameSamplePosition::{FrameIndex, SampleIndex};
use crate::flac::reader::FlacReader;

impl<T: Read> FlacReader<T> {
    pub fn decode_frame(
        &mut self,
        expected_sample_rate: u32,
        output: &mut [i32],
    ) -> Result<Option<usize>, BasicError> {
        let frame_header = match self.read_frame_header()? {
            Some(value) => value,
            None => {
                return Ok(None);
            }
        };

        if frame_header.sample_rate != expected_sample_rate {
            return Err(BasicError::new(
                "Changes of the sample rate mid-stream are not supported",
            ));
        }

        let num_channels = match frame_header.channel_assignment {
            Independent(number) => number,
            LeftSideStereo => 2,
            MidSideStereo => 2,
            RightSideStereo => 2,
        };
        let block_size = frame_header.block_size;
        let bits_per_sample = frame_header.bits_per_sample;

        // slice output buffer
        let total_samples = (frame_header.block_size * (num_channels as u32)) as usize;
        let output = &mut output[0..total_samples];

        // decode subframes
        match frame_header.channel_assignment {
            Independent(num_channels) => {
                for i in 0..(num_channels as u32) {
                    let start_index = (i * block_size) as usize;
                    let end_index = start_index + (block_size as usize);
                    let slice = &mut output[start_index..end_index];
                    self.decode_subframe(block_size, bits_per_sample, slice)?;
                }
            }
            LeftSideStereo => {
                let left = &mut output[0..(block_size as usize)];
                self.decode_subframe(block_size, bits_per_sample, left)?;
                let side = &mut output[(block_size as usize)..((block_size as usize) * 2)];
                self.decode_subframe(block_size, bits_per_sample + 1, side)?;
                decode_left_side(&mut output[0..((block_size as usize) * 2)]);
            }
            MidSideStereo => {
                let mid = &mut output[0..(block_size as usize)];
                self.decode_subframe(block_size, bits_per_sample, mid)?;
                let side = &mut output[(block_size as usize)..((block_size as usize) * 2)];
                self.decode_subframe(block_size, bits_per_sample + 1, side)?;
                decode_mid_side(&mut output[0..((block_size as usize) * 2)]);
            }
            RightSideStereo => {
                let right = &mut output[0..(block_size as usize)];
                self.decode_subframe(block_size, bits_per_sample + 1, right)?;
                let side = &mut output[(block_size as usize)..((block_size as usize) * 2)];
                self.decode_subframe(block_size, bits_per_sample, side)?;
                decode_right_side(&mut output[0..((block_size as usize) * 2)]);
            }
        }

        self.stream.read_to_byte_alignment()?;
        self.read_frame_footer()?;

        Ok(Some(total_samples))
    }

    pub(crate) fn read_frame_header(&mut self) -> Result<Option<FrameHeader>, BasicError> {
        let mut fixed_size_data = [0u8; 4];

        // read first byte individually to check for EOF
        let mut first_byte = [0u8; 1];
        let read_bytes = self.stream.read(&mut first_byte)?;
        if read_bytes == 0 {
            return Ok(None);
        }
        fixed_size_data[0] = first_byte[0];

        self.stream.read_exact(&mut fixed_size_data[1..4])?;
        if u16::from_be_bytes([fixed_size_data[0], fixed_size_data[1]]) >> 2 != 0b11111111111110 {
            return Err(BasicError::new("Invalid frame header sync code"));
        }
        let blocking_strategy = fixed_size_data[1] & 0b1;
        let block_size_code = fixed_size_data[2] >> 4;
        let sample_rate_code = fixed_size_data[2] & 0b1111;
        let channel_assignment_code = fixed_size_data[3] >> 4;
        let sample_size_code = (fixed_size_data[3] >> 1) & 0b111;
        let frame_or_sample_number = self.stream.read_utf8_int()?;

        let position = match blocking_strategy {
            0 => FrameIndex(frame_or_sample_number),
            1 => SampleIndex(frame_or_sample_number),
            _ => return Err(BasicError::new("Invalid blocking_strategy")),
        };

        let channel_assignment = match channel_assignment_code {
            0b0000..=0b0111 => Independent(channel_assignment_code + 1),
            0b1000 => LeftSideStereo,
            0b1001 => RightSideStereo,
            0b1010 => MidSideStereo,
            _ => return Err(BasicError::new("Invalid channel assignment")),
        };

        let bits_per_sample = match sample_size_code {
            0b000 => self.streaminfo.bits_per_sample,
            0b001 => 8,
            0b010 => 12,
            0b100 => 16,
            0b101 => 20,
            0b110 => 24,
            0b111 => 32,
            _ => return Err(BasicError::new("Invalid sample_size_code")),
        };

        let block_size = match block_size_code {
            0b0001 => 192,
            0b0010 => 576,
            0b0011 => 1152,
            0b0100 => 2304,
            0b0101 => 4608,
            0b0110 => (self.stream.read_u8()? as u32) + 1,
            0b0111 => {
                (u16::from_be_bytes([(self.stream.read_u8()?), (self.stream.read_u8()?)]) as u32)
                    + 1
            }
            0b1000 => 256,
            0b1001 => 512,
            0b1010 => 1024,
            0b1011 => 2048,
            0b1100 => 4096,
            0b1101 => 8192,
            0b1110 => 16384,
            0b1111 => 32768,
            _ => return Err(BasicError::new("Invalid block_size_code")),
        };

        let sample_rate = match sample_rate_code {
            0b0000 => self.streaminfo.sample_rate,
            0b0001 => 88200,
            0b0010 => 176400,
            0b0011 => 192000,
            0b0100 => 8000,
            0b0101 => 16000,
            0b0110 => 22050,
            0b0111 => 24000,
            0b1000 => 32000,
            0b1001 => 44100,
            0b1010 => 48000,
            0b1011 => 96000,
            0b1100 => ((self.stream.read_u8()?) as u32) * 1000,
            0b1101 => {
                let byte1 = self.stream.read_u8()?;
                let byte2 = self.stream.read_u8()?;
                u16::from_be_bytes([byte1, byte2]) as u32
            }
            0b1110 => {
                let byte1 = self.stream.read_u8()?;
                let byte2 = self.stream.read_u8()?;
                (u16::from_be_bytes([byte1, byte2]) as u32) * 10
            }
            _ => return Err(BasicError::new("Invalid sample_rate_code")),
        };

        // skip crc-8
        self.stream.read_u8()?;

        Ok(Some(FrameHeader {
            block_size,
            sample_rate,
            channel_assignment,
            bits_per_sample,
            _position: position,
        }))
    }

    pub(crate) fn read_frame_footer(&mut self) -> Result<(), BasicError> {
        // ignore crc-16
        let mut buffer = [0u8; 2];
        Ok(self.stream.read_exact(&mut buffer)?)
    }
}

fn decode_mid_side(output: &mut [i32]) {
    let (mid_slice, side_slice) = output.split_at_mut(output.len() / 2);
    let iterator = mid_slice.iter_mut().zip(side_slice);

    for (mid_value, side_value) in iterator {
        let side = *side_value;
        let right_sample = (*mid_value) - (side >> 1);
        let left_sample = right_sample + side;
        *mid_value = left_sample;
        *side_value = right_sample;
    }
}

fn decode_left_side(output: &mut [i32]) {
    let (left_slice, side_slice) = output.split_at_mut(output.len() / 2);
    let iterator = left_slice.iter_mut().zip(side_slice);

    for (left_sample, side_value) in iterator {
        let right_sample = (*left_sample) - (*side_value);
        *side_value = right_sample;
    }
}

fn decode_right_side(output: &mut [i32]) {
    let (side_slice, right_slice) = output.split_at_mut(output.len() / 2);
    let iterator = side_slice.iter_mut().zip(right_slice);

    for (side_value, right_sample) in iterator {
        let left_sample = (*side_value) + (*right_sample);
        *side_value = left_sample;
    }
}

pub(crate) struct FrameHeader {
    pub block_size: u32,
    pub sample_rate: u32,
    pub channel_assignment: ChannelAssignment,
    pub bits_per_sample: u8,
    pub _position: FrameSamplePosition,
}

pub(crate) enum FrameSamplePosition {
    FrameIndex(u64),
    SampleIndex(u64),
}

pub(crate) enum ChannelAssignment {
    Independent(u8),
    LeftSideStereo,
    RightSideStereo,
    MidSideStereo,
}
