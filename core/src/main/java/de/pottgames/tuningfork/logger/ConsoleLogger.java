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

package de.pottgames.tuningfork.logger;

public class ConsoleLogger implements TuningForkLogger {
    private LogLevel logLevel = LogLevel.WARN_ERROR;


    public ConsoleLogger() {
        // provide default constructor
    }


    public ConsoleLogger(LogLevel logLevel) {
        this.setLogLevel(logLevel);
    }


    public enum LogLevel {
        TRACE_DEBUG_INFO_WARN_ERROR(5), DEBUG_INFO_WARN_ERROR(4), INFO_WARN_ERROR(3), WARN_ERROR(2), ERROR(1), OFF(0);


        private final int value;


        LogLevel(int value) {
            this.value = value;
        }


        public int getValue() {
            return this.value;
        }


        boolean allowedToLog(LogLevel logLevel) {
            return this.value >= logLevel.value;
        }

    }


    public void setLogLevel(LogLevel logLevel) {
        if (logLevel != null) {
            this.logLevel = logLevel;
        } else {
            this.logLevel = LogLevel.OFF;
        }
    }


    @Override
    public void error(Class<?> clazz, String message) {
        if (this.logLevel.allowedToLog(LogLevel.ERROR)) {
            System.out.println(clazz.getName() + ": " + message);
        }
    }


    @Override
    public void warn(Class<?> clazz, String message) {
        if (this.logLevel.allowedToLog(LogLevel.WARN_ERROR)) {
            System.out.println(clazz.getName() + ": " + message);
        }
    }


    @Override
    public void info(Class<?> clazz, String message) {
        if (this.logLevel.allowedToLog(LogLevel.INFO_WARN_ERROR)) {
            System.out.println(clazz.getName() + ": " + message);
        }
    }


    @Override
    public void debug(Class<?> clazz, String message) {
        if (this.logLevel.allowedToLog(LogLevel.DEBUG_INFO_WARN_ERROR)) {
            System.out.println(clazz.getName() + ": " + message);
        }
    }


    @Override
    public void trace(Class<?> clazz, String message) {
        if (this.logLevel.allowedToLog(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR)) {
            System.out.println(clazz.getName() + ": " + message);
        }
    }

}
