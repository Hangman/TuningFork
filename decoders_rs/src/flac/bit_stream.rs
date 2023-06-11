//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

use std::io::{Error, ErrorKind, Read};

use crate::common::error::BasicError;

pub struct BitStream<T: Read> {
    stream: T,
    bit_buffer_length: u8,
    bit_buffer: u64,
}

impl<T: Read> BitStream<T> {
    pub fn new(stream: T) -> Self {
        Self {
            stream,
            bit_buffer_length: 0,
            bit_buffer: 0,
        }
    }

    pub fn read(&mut self, buffer: &mut [u8]) -> Result<usize, BasicError> {
        if buffer.len() > 1 {
            return Err(BasicError::new(
                "read must not be called with a buffer length != 1",
            ));
        }

        if (self.bit_buffer_length as usize) >= 8 {
            let value = self.read_bits(8)?;
            buffer[0] = value as u8;
            return Ok(8);
        } else if self.bit_buffer_length > 0 {
            return Err(BasicError::new(
                "Called read but bit buffer couldn't provide enough bits + is not empty either",
            ));
        }

        Ok(self.stream.read(buffer)?)
    }

    /// Reads a byte from the stream.
    /// Must not be used if the stream is not byte aligned due to calls to [`BitStream::read_bits`].
    pub fn read_u8(&mut self) -> Result<u8, BasicError> {
        if self.bit_buffer_length > 0 {
            return Err(BasicError::new("Stream is not byte aligned"));
        }

        let result = if self.bit_buffer_length >= 8 {
            self.read_bits(8)? as u8
        } else {
            let mut buffer = [0u8; 1];
            self.stream.read_exact(&mut buffer)?;
            buffer[0]
        };
        Ok(result)
    }

    pub fn read_utf8_int(&mut self) -> Result<u64, BasicError> {
        let first_byte = self.read_u8()?;
        let mut utf8_byte_count = 0u8;
        let mut mask = 0b0111_1111u8;
        let mut mask_mark = 0b1000_0000u8;

        while first_byte & mask_mark != 0 {
            utf8_byte_count += 1;
            mask >>= 1;
            mask_mark >>= 1;
        }

        if utf8_byte_count > 0 {
            if utf8_byte_count == 1 {
                return Err(BasicError::new("Invalid utf8 integer"));
            } else {
                utf8_byte_count -= 1;
            }
        }

        let mut result = ((first_byte & mask) as u64) << (6 * utf8_byte_count);
        for i in (0..utf8_byte_count as i16).rev() {
            let next_byte = self.read_u8()?;
            result |= ((next_byte & 0b0011_1111) as u64) << (6 * i as usize);
        }

        Ok(result)
    }

    /// Reads single bits from the stream.
    pub fn read_bits(&mut self, count: u8) -> Result<u32, BasicError> {
        while self.bit_buffer_length < count {
            let mut byte = [0u8; 1];
            self.stream.read_exact(&mut byte)?;
            self.bit_buffer = (self.bit_buffer << 8) | (byte[0] as u64);
            self.bit_buffer_length += 8;
        }
        let bit_mask = (1 << count as u64) - 1;
        let result = (self.bit_buffer >> (self.bit_buffer_length - count)) & bit_mask;
        self.bit_buffer_length -= count;
        Ok(result as u32)
    }

    pub fn read_unary(&mut self) -> Result<u32, BasicError> {
        let mut bit_counter = 0;
        while self.read_bits(1)? == 0 {
            bit_counter += 1;
        }
        Ok(bit_counter as u32)
    }

    /// Reads buffer.len of bytes from the stream and puts it into the buffer.
    /// Must not be used if the stream is not byte aligned due to calls to [`BitStream::read_bits`].
    pub fn read_exact(&mut self, buffer: &mut [u8]) -> Result<(), Error> {
        if self.bit_buffer_length > 0 {
            return Err(Error::new(ErrorKind::Other, "Stream is not byte aligned"));
        }
        self.stream.read_exact(buffer)
    }

    pub fn read_to_byte_alignment(&mut self) -> Result<(), BasicError> {
        let bits_to_byte_alignment = self.bit_buffer_length % 8;
        if bits_to_byte_alignment > 0 {
            self.read_bits(bits_to_byte_alignment)?;
        }
        Ok(())
    }
}
