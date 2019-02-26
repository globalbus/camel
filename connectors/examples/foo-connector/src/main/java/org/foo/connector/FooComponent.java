/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.foo.connector;

import org.apache.camel.component.connector.DefaultConnectorComponent;

public class FooComponent extends DefaultConnectorComponent {

    public FooComponent() {
        super("foo", "org.foo.connector.FooComponent");

        // show how you can add a fixed header
        setBeforeConsumer(e -> e.getIn().setHeader("whoami", "I am foo"));
    }

    public FooComponent(String componentScheme) {
        super("foo", componentScheme, "org.foo.connector.FooComponent");

        // show how you can add a fixed header
        setBeforeConsumer(e -> e.getIn().setHeader("whoami", "I am foo"));
    }

}