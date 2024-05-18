/**
 * Copyright 2022 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork;

public class UnsupportedAudioDeviceException extends TuningForkException {
    private static final long serialVersionUID = 5966425090417508598L;


    public UnsupportedAudioDeviceException(String message) {
        super(message);
    }


    public UnsupportedAudioDeviceException(Throwable t) {
        super(t);
    }


    public UnsupportedAudioDeviceException(String message, Throwable t) {
        super(message, t);
    }

}
