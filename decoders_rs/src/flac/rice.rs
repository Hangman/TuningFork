//! This code is inspired by the awesome Claxon FLAC decoder (https://github.com/ruuda/claxon),
//! hence I include their license header here.
//! TuningFork and Claxon are both Apache 2.0 licensed projects.

//! Claxon -- A FLAC decoding library in Rust
//! Copyright 2014 Ruud van Asseldonk
//!
//! Licensed under the Apache License, Version 2.0 (the "License");
//! you may not use this file except in compliance with the License.
//! A copy of the License has been included in the root of the repository.

pub(crate) fn decode_rice_int(value: u32) -> i32 {
    // The following bit-level hackery compiles to only four instructions on
    // x64. It is equivalent to the following code:
    //
    //   if val & 1 == 1 {
    //       -1 - (val / 2) as i32
    //   } else {
    //       (val / 2) as i32
    //   }
    //
    let half = (value >> 1) as i32;
    let extended_bit_0 = ((value << 31) as i32) >> 31;
    half ^ extended_bit_0
}
