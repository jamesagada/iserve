/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package dom.todo;

import org.apache.isis.applib.annotation.PublishedObject.ChangeKind;
import org.apache.isis.applib.annotation.PublishedObject.PayloadFactory;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventPayloadForObjectChanged;

public class StockLedgerChangedPayLoadFactory implements PayloadFactory{

    public static class StockLedgerPayload extends EventPayloadForObjectChanged<StockLedger> {

        public StockLedgerPayload(StockLedger changed) {
            super(changed);
        }
        
        public String getDescription() {
            return getChanged().title();
        }
    }
    @Override
    public EventPayload payloadFor(Object changedObject, ChangeKind changeKind) {
        return new StockLedgerPayload((StockLedger) changedObject);
    }

}
