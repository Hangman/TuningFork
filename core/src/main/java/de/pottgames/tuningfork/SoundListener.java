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

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class SoundListener {
    private final float[] orientation = new float[6];
    private final Vector3 tempVector  = new Vector3();


    SoundListener() {
        // hide public constructor
    }


    void setMasterVolume(float volume) {
        AL10.alListenerf(AL10.AL_GAIN, volume);
    }


    float getMasterVolume() {
        return AL10.alGetListenerf(AL10.AL_GAIN);
    }


    public SoundListener setSpeed(Vector3 speed) {
        return this.setSpeed(speed.x, speed.y, speed.z);
    }


    public SoundListener setSpeed(float x, float y, float z) {
        AL10.alListener3f(AL10.AL_VELOCITY, x, y, z);
        return this;
    }


    public SoundListener setPosition(Camera camera) {
        return this.setPosition(camera.position);
    }


    public SoundListener setPosition(Vector3 position) {
        return this.setPosition(position.x, position.y, position.z);
    }


    public SoundListener setPosition(float x, float y, float z) {
        AL10.alListener3f(AL10.AL_POSITION, x, y, z);
        return this;
    }


    public SoundListener setOrientation(Camera camera) {
        this.tempVector.set(camera.direction);
        this.tempVector.crs(camera.up);
        this.tempVector.crs(camera.direction);
        this.setOrientation(camera.direction, this.tempVector);
        return this;
    }


    public SoundListener setOrientation(Vector3 at, Vector3 up) {
        this.orientation[0] = at.x;
        this.orientation[1] = at.y;
        this.orientation[2] = at.z;
        this.orientation[3] = up.x;
        this.orientation[4] = up.y;
        this.orientation[5] = up.z;
        AL10.alListenerfv(AL10.AL_ORIENTATION, this.orientation);
        return this;
    }

}
