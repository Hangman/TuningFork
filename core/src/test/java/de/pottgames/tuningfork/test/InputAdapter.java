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

package de.pottgames.tuningfork.test;

import com.badlogic.gdx.InputProcessor;

public interface InputAdapter extends InputProcessor {

    @Override
    default boolean keyDown(int keycode) {
        return false;
    }


    @Override
    default boolean keyUp(int keycode) {
        return false;
    }


    @Override
    default boolean keyTyped(char character) {
        return false;
    }


    @Override
    default boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }


    @Override
    default boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }


    @Override
    default boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }


    @Override
    default boolean mouseMoved(int screenX, int screenY) {
        return false;
    }


    @Override
    default boolean scrolled(float amountX, float amountY) {
        return false;
    }


    @Override
    default boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

}
