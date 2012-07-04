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

package org.reficio.cougar.impl;

import org.junit.Test;
import org.reficio.cougar.StompInvalidHeaderException;
import org.reficio.cougar.core.FrameDecorator;
import org.reficio.cougar.core.FramePreprocessor;
import org.reficio.cougar.domain.Command;
import org.reficio.cougar.domain.Frame;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-29
 * Time: 12:11 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */
public class FrameValidatorTest {

    @Test(expected = StompInvalidHeaderException.class)
    public void validationFails() {
        Frame frame = new Frame(Command.BEGIN);
        frame.transaction("tx-1");

        FrameDecorator decorator = new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.transaction("tx-2");
            }
        };

        FramePreprocessor processor = new FrameValidator();
        processor.decorate(frame, decorator);
    }

    @Test
    public void validationSucceeds() {
        Frame frame = new Frame(Command.BEGIN);

        FrameDecorator decorator = new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.transaction("tx-2");
            }
        };

        FramePreprocessor processor = new FrameValidator();
        processor.decorate(frame, decorator);
    }

}
