//! Copyright 2023 Matthias Finke
//!
//! Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
//! License at
//!
//! http://www.apache.org/licenses/LICENSE-2.0
//!
//! Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
//! CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

use std::error::Error;
use std::fmt;
use std::io::ErrorKind;

#[derive(Debug)]
pub struct BasicError {
    details: String,
}

impl BasicError {
    pub fn new(msg: &str) -> Self {
        Self {
            details: msg.to_string(),
        }
    }
}

impl fmt::Display for BasicError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.details)
    }
}

impl Error for BasicError {
    fn description(&self) -> &str {
        &self.details
    }
}

impl From<std::io::Error> for BasicError {
    fn from(err: std::io::Error) -> Self {
        BasicError::new(err.to_string().as_str())
    }
}

impl From<BasicError> for std::io::Error {
    fn from(err: BasicError) -> Self {
        std::io::Error::new(ErrorKind::Other, err.to_string())
    }
}
