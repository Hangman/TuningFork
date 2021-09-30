package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

class SoundListener {
    private final Vector3 currentSpeed = new Vector3();
    private final Vector3 lastPosition = new Vector3();
    private final float[] orientation  = new float[6];


    SoundListener() {
        // hide public constructor
    }


    SoundListener setSpeed(Camera camera) {
        this.currentSpeed.set(camera.position).sub(this.lastPosition);
        this.lastPosition.set(camera.position);
        return this.setSpeed(this.currentSpeed);
    }


    SoundListener setSpeed(Vector3 speed) {
        AL10.alListener3f(AL10.AL_VELOCITY, speed.x, speed.y, speed.z);
        return this;
    }


    SoundListener setPosition(Camera camera) {
        return this.setPosition(camera.position);
    }


    SoundListener setPosition(Vector3 position) {
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
        return this;
    }


    SoundListener setOrientation(Camera camera) {
        return this.setOrientation(camera.direction, camera.up);
    }


    SoundListener setOrientation(Vector3 at, Vector3 up) {
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
