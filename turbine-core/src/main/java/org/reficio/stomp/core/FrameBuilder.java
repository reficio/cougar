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

package org.reficio.stomp.core;

import org.apache.commons.lang.StringUtils;
import org.reficio.stomp.InvalidHeaderException;
import org.reficio.stomp.domain.*;

import java.util.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class FrameBuilder {

	protected String command;
	protected Map<String, Header> headers;
    protected String payload;
	private boolean validate;
    private Set<String> frozenHeaders;
    protected Boolean subscriptionValid;

    protected FrameBuilder(CommandType command, Map<String, Header> headers, String payload, Boolean subscriptionValid) {
        this.command = command.getName();
		this.headers = headers;
        this.payload = payload;
		this.validate = true;
        this.subscriptionValid = subscriptionValid;
    }

	public FrameBuilder(CommandType command) {
		this(command.getName());
	}

    public FrameBuilder(String commandName) {
		this.command = commandName;
		this.headers = new TreeMap<String, Header>();
		this.validate = true;
	}

    public CommandType getCommand() {
		return CommandType.getCommand(command);
	}

    public String getCommandName() {
		return command;
	}

    // ----------------------------------------------------------------------------------
	// Header mutators
	// ----------------------------------------------------------------------------------
    private void validate(HeaderType type) {
        if(isValidationEnabled()) {
            if(type.isAllowed((Frame)this) == false) {
                throw new InvalidHeaderException(String.format("Header [%s] is not allowed in frame [%s]", type.name(), command));
            }
        }
    }

	protected void addHeaderByType(HeaderType type, String value) {
        validate(type);
		addHeaderByName(type.getName(), value);
	}

	protected void addHeaderByName(String name, String value) {
        if(value == null) {
		    this.headers.remove(name);
        } else {
            if(isFrozen()) {
                if(frozenHeaders.contains(name)) {
                    throw new InvalidHeaderException(String.format("Header [%s] can't be used in the decorator, it is set by the API", name));
                }
            }
            this.headers.put(name, Header.createHeader(name, value));
        }
	}

    // ----------------------------------------------------------------------------------
	// Validation handlers
	// ----------------------------------------------------------------------------------
	public void disableValidation() {
		this.validate = false;
	}

    private boolean isValidationEnabled() {
        return this.validate;
    }

    // ----------------------------------------------------------------------------------
	// Freeze handlers
	// ----------------------------------------------------------------------------------
    private Set<String> getSetHeaders() {
        return new HashSet(this.headers.keySet());
    }

    protected void freeze() {
        if(isFrozen() == false) {
            this.frozenHeaders = this.getSetHeaders();
        }
    }

    private boolean isFrozen() {
        return this.frozenHeaders != null;
    }

    // ----------------------------------------------------------------------------------
	// Generic header accessors
	// ----------------------------------------------------------------------------------
    public Header getHeader(HeaderType type) {
		return getHeader(type.getName());
	}

    public Header getHeader(String name) {
		return this.headers.get(name);
	}

    public String getHeaderValue(HeaderType type) {
		Header header = getHeader(type.getName());
        return (header != null) ? header.getValue() : null;
	}

    public String getHeaderValue(String name) {
		Header header = getHeader(name);
        return (header != null) ? header.getValue() : null;
	}

    public List<Header> getHeaders() {
		return new ArrayList<Header>(headers.values());
	}

    // ----------------------------------------------------------------------------------
	// Tweaked builder setters and getters
	// ----------------------------------------------------------------------------------
    public FrameBuilder payload(String payload) {
		this.payload=payload;
        CommandType comm = getCommand();
        if(comm.equals(CommandType.SEND) || comm.equals(CommandType.MESSAGE) || comm.equals(CommandType.ERROR)) {
            if(payload != null) {
                contentLength(Integer.valueOf(payload.length()).toString());
            } else {
                contentLength(null);
            }
        }
		return this;
	}

    public String payload() {
		return this.payload;
	}
	
	public FrameBuilder login(String value) {
		addHeaderByType(HeaderType.LOGIN, value);
		return this;
	}

    public String login() {
        return getHeaderValue(HeaderType.LOGIN);
    }

    public FrameBuilder encoding(String value) {
        addHeaderByType(HeaderType.ENCODING, value);
        return this;
    }

    public String encoding() {
        return getHeaderValue(HeaderType.ENCODING);
    }

    public FrameBuilder subscription(String value) {
        addHeaderByType(HeaderType.SUBSCRIPTION, value);
        return this;
    }

    public String subscription() {
        return getHeaderValue(HeaderType.SUBSCRIPTION);
    }



    public FrameBuilder passcode(String value) {
		addHeaderByType(HeaderType.PASS_CODE, value);
		return this;
	}

    public String passcode() {
        return getHeaderValue(HeaderType.PASS_CODE);
    }

	public FrameBuilder session(String value) {
		addHeaderByType(HeaderType.SESSION, value);
		return this;
	}

    public String session() {
        return getHeaderValue(HeaderType.SESSION);
    }

	public FrameBuilder destination(String value) {
		addHeaderByType(HeaderType.DESTINATION, value);
		return this;
	}

    public String destination() {
        return getHeaderValue(HeaderType.DESTINATION);
    }

	public FrameBuilder ack(AckType ack) {
		addHeaderByType(HeaderType.ACK, ack.name().toLowerCase());
		return this;
	}

    public AckType ack() {
        String value = getHeaderValue(HeaderType.ACK);
        if(StringUtils.isNotBlank(value)) {
            validateEnumValue(AckType.class, value.toUpperCase());
            return Enum.valueOf(AckType.class, value.toUpperCase());
        } else {
            return null;
        }
    }

	public FrameBuilder transaction(String value) {
		addHeaderByType(HeaderType.TRANSACTION, value);
		return this;
	}

    public String transaction() {
        return getHeaderValue(HeaderType.TRANSACTION);
    }

	public FrameBuilder receipt(String value) {
		addHeaderByType(HeaderType.RECEIPT, value);
		return this;
	}

    public String receipt() {
        return getHeaderValue(HeaderType.RECEIPT);
    }

	public FrameBuilder errorMessageContent(String value) {
		addHeaderByType(HeaderType.ERROR_MESSAGE_CONTENT, value);
		return this;
	}

    public String errorMessageContent() {
        return getHeaderValue(HeaderType.ERROR_MESSAGE_CONTENT);
    }

	private FrameBuilder contentLength(String value) {
		addHeaderByType(HeaderType.CONTENT_LENGTH, value);
		return this;
	}

    public String contentLength() {
        return getHeaderValue(HeaderType.CONTENT_LENGTH);
    }

	public FrameBuilder subscriptionId(String value) {
		addHeaderByType(HeaderType.SUBSCRIPTION_ID, value);
		return this;
	}

    public String receiptId() {
        return getHeaderValue(HeaderType.RECEIPT_ID);
    }

    public FrameBuilder receiptId(String value) {
		addHeaderByType(HeaderType.RECEIPT_ID, value);
		return this;
	}

    public String messageId() {
        return getHeaderValue(HeaderType.MESSAGE_ID);
    }

    public FrameBuilder messageId(String value) {
		addHeaderByType(HeaderType.MESSAGE_ID, value);
		return this;
	}

    public String subscriptionId() {
        return getHeaderValue(HeaderType.SUBSCRIPTION_ID);
    }

	public FrameBuilder selector(String value) {
		addHeaderByType(HeaderType.SELECTOR, value);
		return this;
	}

    public String selector() {
        return getHeaderValue(HeaderType.SELECTOR);
    }

    public FrameBuilder custom(String name, String value) {
        HeaderType type = HeaderType.getInstance(name);
        if(type != null) {
            // validate enum value
            if(type.equals(HeaderType.ACK)) {
                validateEnumValue(AckType.class, value);
            }
            validate(type);
            addHeaderByType(type, value);
        } else {
            addHeaderByName(name, value);
        }
		return this;
	}

    public String custom(String name) {
        return getHeaderValue(name);
    }

    private void validateEnumValue(Class enumClass, String value) throws InvalidHeaderException {
        try {
            Enum.valueOf(enumClass, value);
        } catch(IllegalArgumentException ex) {
            throw new InvalidHeaderException(String.format("Value [%s] invalid for enum type [%s]", value, enumClass));
        }
    }
}
