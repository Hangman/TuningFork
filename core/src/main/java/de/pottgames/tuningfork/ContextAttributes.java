package de.pottgames.tuningfork;

import com.badlogic.gdx.utils.BufferUtils;

import java.nio.IntBuffer;

public class ContextAttributes {
    protected final IntBuffer buffer;


    public ContextAttributes(int[] attributes) {
        this.buffer = BufferUtils.newIntBuffer(attributes.length + 1);
        for (final int value : attributes) {
            this.buffer.put(value);
        }
        this.buffer.put(0);
        this.buffer.flip();
    }


    public IntBuffer getBuffer() {
        this.buffer.rewind();
        return this.buffer;
    }

}
