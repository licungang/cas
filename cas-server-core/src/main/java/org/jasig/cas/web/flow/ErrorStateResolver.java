/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.execution.Event;

/**
 * Maps exceptions onto Webflow events that can be used for state transitions.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public interface ErrorStateResolver {

    /**
     * Resolves a Webflow event from an exception.
     *
     *
     * @param e Error.
     *
     * @return Event resolved from exception. SHOULD NOT be null.
     */
    Event resolve(Throwable e);
}
