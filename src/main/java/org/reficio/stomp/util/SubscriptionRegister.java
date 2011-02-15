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

package org.reficio.stomp.util;

import org.apache.commons.lang.StringUtils;
import org.reficio.stomp.StompProtocolException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class SubscriptionRegister {

    private Set<String> subscriptions;

    public SubscriptionRegister() {
        subscriptions = new HashSet<String>();
    }

    public String subscribe(String id) {
        String subscriptionId = id;
        if(StringUtils.isBlank(subscriptionId)) {
            subscriptionId = UUID.randomUUID().toString();
        } else {
            if(subscriptions.contains(id)) {
                throw new StompProtocolException("Subscription id is already used");
            }
        }
        subscriptions.add(subscriptionId);
        return subscriptionId;
    }

    public void unsubscribe(String id) {
        this.subscriptions.remove(id);
    }

    public boolean isSubscriptionActive(String id) {
        return subscriptions.contains(id);
    }

}
