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
 * Thrown if a URLConnectionInitializer could not complete initialization of a
 * URLConnection, but that it may still be usable, and therefore should not
 * prevent the URLConnection from being returned.
 *
 * @see FatalConnectionInitializationException
 * @author steve_siebert
 */
public class IncompleteConnectionInitializationException extends RuntimeException {

    public IncompleteConnectionInitializationException() {
    }

    public IncompleteConnectionInitializationException(String message) {
        super(message);
    }

    public IncompleteConnectionInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompleteConnectionInitializationException(Throwable cause) {
        super(cause);
    }

}
