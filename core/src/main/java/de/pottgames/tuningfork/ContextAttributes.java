package de.pottgames.tuningfork;

import com.badlogic.gdx.utils.BufferUtils;
import java.nio.IntBuffer;

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
