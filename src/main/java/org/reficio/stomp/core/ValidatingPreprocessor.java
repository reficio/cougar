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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reficio.stomp.domain.Frame;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ValidatingPreprocessor implements FramePreprocessor {

    private static final Log logger = LogFactory.getLog(ValidatingPreprocessor.class);

    @Override
    public void decorate(Frame frame, FrameDecorator decorator) {
        frame.freeze();
        decorator.decorateFrame(frame);

        // Frame frameProxy = getHeaderValidationFrameClassProxy(frame);
        // decorator.decorateFrame(frameProxy);
    }

//    private Frame getHeaderValidationFrameClassProxy(Frame frame) {
//        Object proxy = Enhancer.create(Frame.class, new HeaderValidationFrameBuilderClassProxy(frame, frame.getSetHeaders()));
//        return (Frame)proxy;
//    }
//
//    private static class HeaderValidationFrameBuilderClassProxy implements MethodInterceptor {
//
//        private static final Log logger = LogFactory.getLog(HeaderValidationFrameBuilderClassProxy.class);
//
//        private static final String GET_TARGET_FRAME_METHOD_NAME = "getTargetFrame";
//        private static final String EQUALS_METHOD_NAME = "equals";
//        private static final String HASH_CODE_METHOD_NAME = "hashCode";
//
//        private final Frame target;
//        private final Set<String> headersSetBeforeDecoration;
//
//        public HeaderValidationFrameBuilderClassProxy(Frame target, Set<String> headersSetBeforeDecoration) {
//            this.target = target;
//            this.headersSetBeforeDecoration = headersSetBeforeDecoration;
//        }
//
//        @Override
//        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
//            logger.info("Invoking method " + method.getName() + " " + method.getReturnType());
//            if (method.getName().equals(EQUALS_METHOD_NAME)) {
//                // Only consider equal when proxies are identical.
//                return (proxy == args[0]);
//            } else if (method.getName().equals(HASH_CODE_METHOD_NAME)) {
//                // Use hashCode of Connection proxy.
//                return System.identityHashCode(proxy);
//            } else if (method.getName().equals(GET_TARGET_FRAME_METHOD_NAME)) {
//                // Handle getTargetSession method: return underlying Session.
//                return this.target;
//            } else if(method.getReturnType().equals(FrameBuilder.class)) {
//                String name = (String)args[0];
//                if(headersSetBeforeDecoration.contains(name)) {
//                    throw new InvalidHeaderException(String.format("Header [%s] can't be used in the decorator, it is set by the API", name));
//                }
//            }
//
//            // Invoke method on target Session.
//            try {
//                return methodProxy.invoke(this.target, args);
//            } catch (InvocationTargetException ex) {
//                throw ex.getTargetException();
//            }
//        }
//    }


//    private Frame getHeaderValidationFrameProxy(Frame frame) {
//          HeaderValidationFrameBuilderProxy frameProxy = new HeaderValidationFrameBuilderProxy(frame, frame.getSetHeaders());
//          return (Frame) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
//                  new Class[]{Frame.class}, frameProxy);
//      }
//
//    private static class HeaderValidationFrameBuilderProxy implements InvocationHandler {
//
//        private static final String GET_TARGET_FRAME_METHOD_NAME = "getTargetFrame";
//        private static final String EQUALS_METHOD_NAME = "equals";
//        private static final String HASH_CODE_METHOD_NAME = "hashCode";
//        private static final String ADD_HEADER_METHOD_NAME = "addHeaderByName";
//
//        private final Frame target;
//        private final Set<String> headersSetBeforeDecoration;
//
//        public HeaderValidationFrameBuilderProxy(Frame target, Set<String> headersSetBeforeDecoration) {
//            this.target = target;
//            this.headersSetBeforeDecoration = headersSetBeforeDecoration;
//        }
//
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            // Invocation on SessionProxy interface coming in...
//
//            if (method.getName().equals(EQUALS_METHOD_NAME)) {
//                // Only consider equal when proxies are identical.
//                return (proxy == args[0]);
//            } else if (method.getName().equals(HASH_CODE_METHOD_NAME)) {
//                // Use hashCode of Connection proxy.
//                return System.identityHashCode(proxy);
//            } else if (method.getName().equals(ADD_HEADER_METHOD_NAME)) {
//                String name = (String)args[0];
//                if(headersSetBeforeDecoration.contains(name)) {
//                    throw new InvalidHeaderException(String.format("Header [%s] can't be used in the decorator, it is set by the API", name));
//                }
//            } else if (method.getName().equals(GET_TARGET_FRAME_METHOD_NAME)) {
//                // Handle getTargetSession method: return underlying Session.
//                return this.target;
//            }
//
//            // Invoke method on target Session.
//            try {
//                return method.invoke(this.target, args);
//            } catch (InvocationTargetException ex) {
//                throw ex.getTargetException();
//            }
//        }
//    }
}
