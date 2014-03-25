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

@Named("Reports")
public class Reports extends AbstractFactoryAndRepository {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "Reports";
    }

    public String iconName() {
        return "Reports";
    }
    // }}

    
    // {{ newPartytype  (action)
    @MemberOrder(sequence = "1")
	@Named("Daily Sales")
    public Partytype newPartytype(
            @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") // words, spaces and selected punctuation
            @Named("Description") String description,
            @Optional
	    @Named("Is an Operator?") Boolean operator, 
	    @Optional
	    @Named("Can Order ?") Boolean canorder,
	    @Optional
	    @Named("Can Deliver? ") Boolean candeliver,
	    @Optional
	    @Named("Can be Principal in a contract?") Boolean canprincipal,
	    @Optional
	    @Named("Vends To end customer? ") Boolean selfvends,
	    @Optional
	    @Named("Uses Virtual Ticket? ") Boolean usevirtualticket,
	    @Optional
	    @Named("Redeems voucher? ") Boolean redeemsticket
								  ) {
        final String ownedBy = currentUserName();
        return newPartytype(description, operator, ownedBy,canorder, candeliver,
 canprincipal, selfvends, usevirtualticket, redeemsticket);
    }

    // }}


    // {{ allPartyTypes (action)
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
	@Named("Weekly Sales By Vendor")
    public List<Partytype> allPartyTypes() {
        return allPartyTypes(NotifyUserIfNone.YES);
    }

    public enum NotifyUserIfNone { YES, NO }
    
    @Programmatic
    public List<Partytype> allPartyTypes(NotifyUserIfNone notifyUser) {
        final String currentUser = currentUserName();
        final List<Partytype> items = allMatches(Partytype.class, Partytype.thoseOwnedBy(currentUser));
        //Collections.sort(items);
        if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
            getContainer().warnUser("No PartyTypes found.");
        }
        return items;
    }
    // }}
    
    // {{ newPartyType  (hidden)
    @Hidden // for use by fixtures
    public Partytype newPartytype(
            final String description, 
            final Boolean operator, 
            final String userName,
	    final Boolean canorder,
	    final Boolean candeliver,
	    final Boolean canprincipal,
	    final Boolean selfvends,
	    final Boolean usevirtualticket,
	    final Boolean redeemsticket) {
        final Partytype partytype = newTransientInstance(Partytype.class);
	partytype.setDescription(description);
        partytype.setOwnedBy(userName);
	partytype.setOperator(operator);
	partytype.setCanorder(canorder);
	partytype.setCandeliver(candeliver);
	partytype.setCanprincipal(canprincipal);
	partytype.setSelfvends(selfvends);
	partytype.setUsevirtualstock(usevirtualticket);
	partytype.setRedeemsticket(redeemsticket);
        persist(partytype);
        return partytype;
    }
    
    private static double random(double from, double to) {
        return Math.random() * (to-from) + from;
    }
    // }}


    
      
    // {{ autoComplete (hidden)
    @Hidden
    public List<Partytype> autoComplete(final String description) {
        return allMatches(Partytype.class, new Filter<Partytype>() {
            @Override
            public boolean accept(final Partytype t) {
                return ownedByCurrentUser(t) && t.getDescription().contains(description);
            }

        });
    }
    // }}

    // {{ helpers
    protected boolean ownedByCurrentUser(final Partytype t) {
        return Objects.equal(t.getOwnedBy(), currentUserName());
    }
    protected String currentUserName() {
        return getContainer().getUser().getName();
    }
    // }}


}
