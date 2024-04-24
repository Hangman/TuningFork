/**
 * Copyright 2022 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.logger;

import com.badlogic.gdx.Gdx;

public class GdxLogger implements TuningForkLogger {
    private static final String TAG = "TuningFork";


    @Override
    public void error(Class<?> clazz, String message) {
        Gdx.app.error(GdxLogger.TAG, message);
    }


    @Override
    public void warn(Class<?> clazz, String message) {
        Gdx.app.error(GdxLogger.TAG, message);
    }


    @Override
    public void info(Class<?> clazz, String message) {
        Gdx.app.log(GdxLogger.TAG, message);
    }


    @Override
    public void debug(Class<?> clazz, String message) {
        Gdx.app.debug(GdxLogger.TAG, message);
    }


    @Override
    public void trace(Class<?> clazz, String message) {
        Gdx.app.debug(GdxLogger.TAG, message);
    }

}
