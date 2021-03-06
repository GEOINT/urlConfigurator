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
 * May be thrown by a {@link URLConnectionInitalizer} to indicate initialization
 * failed.
 *
 * @see IncompleteConnectionInitializationException
 * @see FatalConnectionInitializationException
 * @author steve_siebert
 */
public abstract class URLConnectionInitializationException extends RuntimeException {

    public URLConnectionInitializationException() {
    }

    public URLConnectionInitializationException(String message) {
        super(message);
    }

    public URLConnectionInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public URLConnectionInitializationException(Throwable cause) {
        super(cause);
    }

}
