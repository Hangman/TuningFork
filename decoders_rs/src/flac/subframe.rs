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
use crate::common::math;
use crate::flac::reader::FlacReader;
use crate::flac::rice;
use crate::flac::subframe::ResidualCodingMethod::{PartitionedRice, PartitionedRice2};
use crate::flac::subframe::RiceType::{Rice1, Rice2};

impl<T: Read> FlacReader<T> {
    pub(crate) fn decode_subframe(
        &mut self,
        block_size: u32,
        bits_per_sample: u8,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        let header = self.read_subframe_header()?;
        let wasted_bits = header.wasted_bits_per_sample;
        let actual_bps = bits_per_sample - wasted_bits;

        match header.subframe_type {
            SubframeType::Constant => {
                self.decode_constant(actual_bps, output)?;
            }
            SubframeType::Verbatim => {
                self.decode_verbatim(actual_bps, output)?;
            }
            SubframeType::Fixed(order) => {
                self.decode_fixed(block_size, order, actual_bps, output)?;
            }
            SubframeType::Lpc(order) => {
                self.decode_lpc(block_size, order, actual_bps, output)?;
            }
        }

        // shift samples by wasted bits per sample
        if wasted_bits > 0 {
            for sample in output.iter_mut() {
                *sample <<= wasted_bits;
            }
        }

        Ok(())
    }

    fn read_subframe_header(&mut self) -> Result<SubframeHeader, BasicError> {
        self.stream.read_bits(1)?; // padding

        // subframe type
        let type_code = self.stream.read_bits(6)?;
        let subframe_type = match type_code {
            0b000000 => Ok(SubframeType::Constant),
            0b000001 => Ok(SubframeType::Verbatim),
            0b001000..=0b001100 => Ok(SubframeType::Fixed((type_code & 0b111) as u8)),
            0b100000..=0b111111 => Ok(SubframeType::Lpc(((type_code & 0b11111) + 1) as u8)),
            _ => Err(BasicError::new("Invalid subframe type code")),
        }?;

        // read wasted bits per sample from unary coded bits
        let wasted_bits_flag = self.stream.read_bits(1)?;
        let wasted_bits = if wasted_bits_flag == 0 {
            0
        } else {
            (self.stream.read_unary()? + 1) as u8
        };

        Ok(SubframeHeader {
            subframe_type,
            wasted_bits_per_sample: wasted_bits,
        })
    }

    fn decode_constant(
        &mut self,
        bits_per_sample: u8,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        let raw_sample = self.stream.read_bits(bits_per_sample)?;
        let sample = math::bits_as_signed_i32(raw_sample, bits_per_sample);

        for value in output {
            *value = sample;
        }
        Ok(())
    }

    fn decode_verbatim(
        &mut self,
        bits_per_sample: u8,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        let shift = 32 - bits_per_sample;

        for value in output {
            let raw_sample = self.stream.read_bits(bits_per_sample)?;
            let sample = ((raw_sample << shift) as i32) >> shift;
            *value = sample;
        }

        Ok(())
    }

    fn decode_fixed(
        &mut self,
        block_size: u32,
        order: u8,
        bits_per_sample: u8,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        const COEFFICIENTS_0: [i16; 0] = [];
        const COEFFICIENTS_1: [i16; 1] = [1];
        const COEFFICIENTS_2: [i16; 2] = [2, -1];
        const COEFFICIENTS_3: [i16; 3] = [3, -3, 1];
        const COEFFICIENTS_4: [i16; 4] = [4, -6, 4, -1];

        // unencoded warm-up samples
        self.decode_verbatim(bits_per_sample, &mut output[0..order as usize])?;

        self.decode_residual(block_size, order, &mut output[(order as usize)..])?;

        let coefficients: &[i16] = match order {
            0 => &COEFFICIENTS_0,
            1 => &COEFFICIENTS_1,
            2 => &COEFFICIENTS_2,
            3 => &COEFFICIENTS_3,
            4 => &COEFFICIENTS_4,
            _ => return Err(BasicError::new("Invalid order")),
        };
        self.predict_lpc(coefficients, 0, output)?;

        Ok(())
    }

    fn decode_lpc(
        &mut self,
        block_size: u32,
        order: u8,
        bits_per_sample: u8,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        self.decode_verbatim(bits_per_sample, &mut output[0..(order as usize)])?;

        let coefficient_precision = (self.stream.read_bits(4)? + 1) as u8;
        let coefficient_shift = {
            let value = self.stream.read_bits(5)?;
            math::bits_as_signed_i16(value as u16, 5)
        };

        let mut coefficient_candidates = [0i16; 32];
        for coefficient in coefficient_candidates[0..(order as usize)].iter_mut() {
            let value = self.stream.read_bits(coefficient_precision)? as u16;
            *coefficient = math::bits_as_signed_i16(value, coefficient_precision);
        }
        let coefficients = &coefficient_candidates[0..(order as usize)];

        self.decode_residual(block_size, order, &mut output[(order as usize)..])?;
        self.predict_lpc(coefficients, coefficient_shift, output)?;

        Ok(())
    }

    fn predict_lpc(
        &mut self,
        coefficients: &[i16],
        shift: i16,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        for i in coefficients.len()..output.len() {
            let mut unshifted_value: i64 = 0;
            for j in 0..coefficients.len() {
                unshifted_value += (output[i - 1 - j] as i64) * (coefficients[j] as i64);
            }
            output[i] += (unshifted_value >> shift) as i32;
        }

        Ok(())
    }

    fn decode_residual(
        &mut self,
        block_size: u32,
        predictor_order: u8,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        let coding_method_code = self.stream.read_bits(2)?;
        let coding_method_type = match coding_method_code {
            0b00 => Ok(PartitionedRice),
            0b01 => Ok(PartitionedRice2),
            _ => Err(BasicError::new("Invalid coding method type")),
        }?;
        let rice_type = match coding_method_type {
            PartitionedRice => Rice1,
            PartitionedRice2 => Rice2,
        };
        let partition_order = self.stream.read_bits(4)?;
        let partitions = 1 << partition_order;
        let samples_per_partition = (block_size / partitions) as usize;
        let partition_size_for_order0 = (block_size as usize) - (predictor_order as usize);
        let partition_size_common = samples_per_partition - (predictor_order as usize);

        let mut slice_index: usize = 0;
        for i in 0..partitions {
            let num_samples = if partition_order == 0 {
                partition_size_for_order0
            } else if i != 0 {
                samples_per_partition
            } else {
                partition_size_common
            };
            let slice = &mut output[slice_index..(slice_index + num_samples)];
            self.decode_rice_partition(&rice_type, slice)?;
            slice_index += num_samples;
        }

        Ok(())
    }

    fn decode_rice_partition(
        &mut self,
        rice_type: &RiceType,
        output: &mut [i32],
    ) -> Result<(), BasicError> {
        let rice_parameter = match rice_type {
            Rice1 => self.stream.read_bits(4)?,
            Rice2 => self.stream.read_bits(5)?,
        };
        let escape = match rice_type {
            Rice1 => rice_parameter == 0b1111,
            Rice2 => rice_parameter == 0b11111,
        };

        if escape {
            let escape_bps = self.stream.read_bits(5)? as u8;
            let shift = 32 - escape_bps;
            for sample in output {
                let value = self.stream.read_bits(escape_bps)?;
                *sample = ((value << shift) as i32) >> shift;
            }
        } else {
            for sample in output {
                let quotient = self.stream.read_unary()?;
                let remainder = self.stream.read_bits(rice_parameter as u8)?;
                let rice_value = (quotient << rice_parameter) | remainder;
                *sample = rice::decode_rice_int(rice_value);
            }
        }

        Ok(())
    }
}

#[derive(Debug)]
pub(crate) enum RiceType {
    Rice1,
    Rice2,
}

#[derive(Debug)]
pub(crate) enum SubframeType {
    Constant,
    Verbatim,
    Fixed(u8),
    Lpc(u8),
}

#[derive(Debug)]
pub(crate) enum ResidualCodingMethod {
    PartitionedRice,
    PartitionedRice2,
}

pub(crate) struct SubframeHeader {
    subframe_type: SubframeType,
    wasted_bits_per_sample: u8,
}
