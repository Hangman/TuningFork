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
use crate::flac::bit_stream::BitStream;
use crate::flac::stream_info::StreamInfo;

pub struct FlacReader<T: Read> {
    pub(crate) stream: BitStream<T>,
    pub streaminfo: StreamInfo,
}

impl<T: Read> FlacReader<T> {
    pub fn new(input_stream: T) -> Result<Self, BasicError> {
        let mut stream = BitStream::new(input_stream);

        // check fLaC stream marker
        let mut header = [0u8; 4];
        stream.read_exact(&mut header)?;
        if &header != b"fLaC" {
            return Err(BasicError::new(
                "Invalid FLAC stream: missing flaC stream marker",
            ));
        }

        // read streaminfo and skip all other metadata block
        let mut streaminfo: Result<StreamInfo, BasicError> =
            Err(BasicError::new("Missing streaminfo"));
        let mut last_block = false;
        while !last_block {
            // read block header
            let mut block_header = [0u8; 4];
            stream.read_exact(&mut block_header)?;
            last_block = (block_header[0] & 0b10000000) != 0;
            let block_type = block_header[0] & 0b1111111;
            let block_length =
                u32::from_be_bytes([0, block_header[1], block_header[2], block_header[3]]);
            let mut buffer = vec![0u8; block_length as usize];

            // read block data
            stream.read_exact(&mut buffer)?;

            // create streaminfo
            if block_type == 0 {
                if buffer.len() == 34 {
                    let mut stream_info_buffer = [0u8; 34];
                    stream_info_buffer.copy_from_slice(&buffer[0..34]);
                    streaminfo = Ok(StreamInfo::new(stream_info_buffer));
                } else {
                    return Err(BasicError::new(
                        "Invalid FLAC stream: missing streaminfo block",
                    ));
                }
            }
        }

        Ok(Self {
            stream,
            streaminfo: streaminfo?,
        })
    }
}
