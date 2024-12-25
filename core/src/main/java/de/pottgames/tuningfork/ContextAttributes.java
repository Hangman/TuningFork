package de.pottgames.tuningfork;

import java.nio.IntBuffer;

import com.badlogic.gdx.utils.BufferUtils;

public class ContextAttributes {
    protected final IntBuffer buffer;


    public ContextAttributes(int[] attributes) {
        buffer = BufferUtils.newIntBuffer(attributes.length + 1);
        for (final int value : attributes) {
            buffer.put(value);
        }
        buffer.put(0);
        buffer.flip();
    }


    public IntBuffer getBuffer() {
        buffer.rewind();
        return buffer;
    }

}
