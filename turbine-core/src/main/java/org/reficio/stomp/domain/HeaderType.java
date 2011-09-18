/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.reficio.stomp.domain;

import static org.reficio.stomp.domain.Command.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public enum HeaderType {

    ENCODING("encoding") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == CONNECT || command == CONNECTED;
        }
    },
    LOGIN("login") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == CONNECT;
        }
    },
    PASS_CODE("passcode") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == CONNECT;
        }
    },
    SESSION("session") {
        public boolean isAllowed(Frame frame) {
            // documentation says that it is not used yet...
            return true;
        }
    },
    DESTINATION("destination") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == SEND || command == SUBSCRIBE ||
                    command == Command.MESSAGE ||
                    (command == UNSUBSCRIBE && frame.subscriptionId() == null);
        }
    },
    ACK("ack") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == SUBSCRIBE;
        }
    },
    TRANSACTION("transaction") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == SEND || command == BEGIN ||
                    command == COMMIT || command == ABORT || command == Command.ACK;
        }
    },
    RECEIPT("receipt") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == SEND || command == SUBSCRIBE ||
                    command == UNSUBSCRIBE || command == Command.MESSAGE || command == BEGIN ||
                    command == COMMIT || command == ABORT || command == Command.ACK;
        }
    },
    RECEIPT_ID("receipt-id") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == Command.RECEIPT;
        }
    },
    ERROR_MESSAGE_CONTENT("message") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == ERROR;
        }
    },
    MESSAGE_ID("message-id") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == Command.MESSAGE || command == Command.ACK;
        }
    },
    CONTENT_LENGTH("content-length") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == Command.MESSAGE || command == SEND || command == ERROR;
        }
    },
    SUBSCRIPTION_ID("id") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == SUBSCRIBE ||
                    (command == UNSUBSCRIBE && frame.destination() == null);
        }
    },
    SELECTOR("selector") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == SUBSCRIBE;
        }
    },
    SUBSCRIPTION("subscription") {
        public boolean isAllowed(Frame frame) {
            Command command = frame.getCommand();
            return command == Command.MESSAGE;
        }
    };

    public abstract boolean isAllowed(Frame frame);

    private final String name;

    private HeaderType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static HeaderType getInstance(String headerName) {
        for (HeaderType type : HeaderType.values()) {
            if (type.getName().equals(headerName)) {
                return type;
            }
        }
        return null;
    }

}
