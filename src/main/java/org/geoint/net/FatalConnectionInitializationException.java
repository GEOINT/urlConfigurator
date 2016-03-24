/*
 * Copyright 2016 geoint.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geoint.net;

/**
 * Thrown if there is a problem initializing a URLConnection.
 * <p>
 * Throwing this exception will prevent the connection from being returned by
 * being rethrown as an IOException.
 *
 * @author steve_siebert
 */
public class FatalConnectionInitializationException extends RuntimeException {

    public FatalConnectionInitializationException() {
    }

    public FatalConnectionInitializationException(String message) {
        super(message);
    }

    public FatalConnectionInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalConnectionInitializationException(Throwable cause) {
        super(cause);
    }

}
