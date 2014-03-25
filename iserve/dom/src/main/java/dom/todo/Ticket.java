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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;

import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;

@Named("Tickets")
public class Ticket extends AbstractFactoryAndRepository {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "Tickets";
    }

    public String iconName() {
        return "Tickets";
    }
    // }}

    
    // {{ Sell a Ticket  (action)
    @MemberOrder(sequence = "1")
	@Named("Denomination")

    // }}
    //{{ RedeemATicket
    //}}
    //{{Transfer Ticket Stock
    //}}
    //{{IssueTicketToCard - issue a ticket to a card
    //}}
    //{{CheckTicket -- check a given ticket to find its validity
    //}}
    //{{Issue card is carried out when creating a new party. If you have multiple cards, you will have multiple records.


    // {{ helpers
    protected boolean ownedByCurrentUser(final Partytype t) {
        return Objects.equal(t.getOwnedBy(), currentUserName());
    }
    protected String currentUserName() {
        return getContainer().getUser().getName();
    }
    // }}


}
