//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

//use std::fs::File;
//use std::io::BufReader;
//use std::time::Instant;
//
//use decoders_rs64::flac::reader::FlacReader;

//#[test]
//fn test_flac_decoder() {
//    //    let file_path = "../core/src/test/resources/numbers_8bit_mono.flac";
//    let file_path = "../core/src/jmh/resources/bench_8bit.flac";
//    let file = File::open(file_path).expect("Failed to open FLAC file");
//    let reader = BufReader::new(file);
//
//    let start = Instant::now();
//    let mut flac_reader = FlacReader::new(reader).expect("Failed to create FlacReader");
//
//    let mut buffer = vec![0; 65536 * 8];
//    while let Some(_result) = flac_reader
//        .decode_frame(flac_reader.streaminfo.sample_rate, &mut buffer)
//        .unwrap()
//    {
//        //        for sample in result.iter() {
//        //            println!("sample: {sample}");
//        //        }
//    }
//    println!("time elapsed: {:?}", start.elapsed());
//}
