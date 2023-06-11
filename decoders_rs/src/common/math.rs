//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

pub fn bits_as_signed_i8(value: u8, bits: u8) -> i16 {
    debug_assert!(bits <= 8);
    let shift = 8 - bits;
    ((value << shift) as i16) >> shift
}

pub fn bits_as_signed_i16(value: u16, bits: u8) -> i16 {
    debug_assert!(bits <= 16);
    let shift = 16 - bits;
    ((value << shift) as i16) >> shift
}

pub fn bits_as_signed_i32(value: u32, bits: u8) -> i32 {
    debug_assert!(bits <= 32);
    let shift = 32 - bits;
    ((value << shift) as i32) >> shift
}
