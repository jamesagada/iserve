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
import org.apache.isis.applib.query.QueryDefault;
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

@Named("PartiesAndContracts")
public class Partytypes extends AbstractFactoryAndRepository {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Partytypes.class);
	// {{ Id, iconName
	@Override
	public String getId() {
		return "PartiesAndContracts";
	}

	public String iconName() {
		return "PartiesAndContracts";
	}
	// }}

	// {{ newContract  (action)
	@MemberOrder(sequence = "7")
	@Named("New Contract ")
	public Contract newContract(
			@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") // words, spaces and selected punctuation
			@Named("Description") String description,
			//@Named("Principal Party") Party principal, 
			@Named("Subordinate Party") Party subordinate,
			@Named("Starting ") LocalDate start,
			@Named("Reference") String reference,
			@Named("Duration ") Integer duration,
			@Optional
			@Named("Active? ") Boolean active,
			@Optional
			@Named("Default Discount ") java.math.BigDecimal discount
			) {
		final String ownedBy = currentUserName();
		final Party principal = getPartyFromCurrentUser(currentUserName());
		return newContract(description, principal, ownedBy,subordinate, start,reference, duration,active, discount);
	}

	// }}

	// {{ newParty  (action)
	@MemberOrder(sequence = "5")
	@Named("New Party ")
	public Party newParty(
			@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") // words, spaces and selected punctuation
			@Named("Description") String description,
			@Named("Type of party") Partytype partytype,
			@Named("Active ?" ) Boolean active,
			@Named("Phone Number") String phoneNumber,
			@Named("Manager") User manager,
			@Named("Transport Card") String transportCard) {
		final String ownedBy = currentUserName(); 
		return newParty(description, partytype, ownedBy,manager,active,phoneNumber,transportCard);
	}

	// }} 
	// {{ newPartytype  (action)
	@MemberOrder(sequence = "1")
	@Named("New Party Type")
	public Partytype newPartytype(
			@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") // words, spaces and selected punctuation
			@Named("Description") String description,
			@Optional
			@Named("Phone Number ") String phoneNumber,
			@Optional
			@Named("Transport Card ") String transportCard,
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
			@Named("Redeems voucher? ") Boolean redeemsticket ) {
		final String ownedBy = currentUserName();
		return newPartytype(description, operator, ownedBy,canorder, candeliver,
				canprincipal, selfvends, usevirtualticket, redeemsticket);
	}
	public Boolean default1NewPartytype(){
		return false;
	}

	// }}


	// {{ allPartyTypes (action)
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "2")
	@Named("Party Types")
	public List<Partytype> allPartyTypes() {
		return allPartyTypes(NotifyUserIfNone.YES);
	}

	public enum NotifyUserIfNone { YES, NO }

	@Programmatic
	public List<Partytype> allPartyTypes(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<Partytype> items = allMatches(
				new QueryDefault(Partytype.class,
						"partytype_all"));
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

	// {{ newParty  (hidden)
	@Hidden // for use by fixtures
	public Party newParty(
			final String description, 
			final Partytype partytype, 
			final String userName,
			final User manager,
			final Boolean active,
			final String phoneNumber,
			final String transportCard
			) {
		final Party party = newTransientInstance(Party.class);
		party.setDescription(description);
		party.setOwnedBy(userName);
		party.setManager(manager);
		party.setPartytype(partytype);
		party.setTransportCard(transportCard);
		party.setPhoneNumber(phoneNumber);
		persist(party);
		return party;
	}
	// {{ newContract  (hidden)
	//@newContract(description, principal, ownedBy,subordinate, start, reference, duration,active, discount);
	@Hidden // for use by fixtures
	@Programmatic
	public Contract newContract(
			final String description, 
			final Party principal,
			final String userName,
			final Party subordinate,
			final LocalDate start,
			final String reference,
			final Integer duration,
			final Boolean active,
			final java.math.BigDecimal discount ) {
		final Contract contract = newTransientInstance(Contract.class);
		contract.setDescription(description);
		contract.setOwnedBy(userName);
		contract.setPrincipal(principal);
		contract.setSubordinate(subordinate);
		contract.setStart(start);
		contract.setReference(reference);
		contract.setDuration(duration);
		contract.setActive(active);
		contract.setDiscount(discount);
		persist(contract);
		return contract;
	}

	// {{ autoCompleteParty (hidden)
	@SuppressWarnings("deprecation")
	@Hidden
	public List<Party> autoListParty(final String description) {
		return allMatches(Party.class, new Filter<Party>() {
			@Override
			public boolean accept(final Party t) {
				return t.getDescription().contains(description);
			}

		});
	}
	// }}


	// {{ autoComplete (hidden)
	@SuppressWarnings("deprecation")
	@Hidden
	public List<Partytype> autoComplete(final String description) {
		return allMatches(Partytype.class, new Filter<Partytype>() {
			@Override
			public boolean accept(final Partytype t) {
				return t.getDescription().contains(description);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Party getPartyFromCurrentUser(String currentUser){
		// find the party that is attached to this current user. In reality we look through all the parties and find which one has the same user name
		List<User> users = allMatches(
				new QueryDefault(User.class,
						"UserForName",
						"name",currentUser));
		if (users != null ){
			//return the first one. so user name must be unique
			return users.get(0).getParty();
		} else {
		return null;
		}
	}
	// {{ allParties (action)
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "4")
	@Named("All Parties")
	public List<Party> allParties() {
		return allParties(NotifyUserIfNone.YES);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Programmatic
	public List<Party> allParties(NotifyUserIfNone notifyUser) {
		//final String currentUser = currentUserName();
		List<Party> items = allMatches(
				new QueryDefault(Party.class,
						"party_all"));
		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No Party found.");
		}
		return items;
	}
	// }}

	// {{ allContracts (action)
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "9")
	@Named("All Contracts")
	public List<Contract> allContracts() {
		return allContracts(NotifyUserIfNone.YES);
	}


	@SuppressWarnings("deprecation")
	@Programmatic
	public List<Contract> allContracts(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<Contract> items = allMatches(Contract.class,Contract.thoseOwnedBy(currentUser));
		//Collections.sort(items);
		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No Contracts found.");
		}
		return items;
	}
	// }}
	@Programmatic
	public boolean hasAContract(Party principal, Party subordinate){
		//return true if the principal and the subordinate has a valid contract
		List<Contract> lc = findValidContracts(principal,subordinate);
		LOG.info("Contracts found " + lc.toString());
		if (lc.isEmpty()) {
			return false;
			
		}else {
			return true;
		}
		
	}
	@Programmatic
	public List<Contract> findValidContracts(Party principal, Party subordinate){
		return allMatches(
				new QueryDefault<Contract>(Contract.class, 
						"ValidContractByParty", 
						"principal", principal,
						"status",true,
						"subordinate", subordinate));
	}
	@Programmatic
	public List<Contract> getContractForPartyAsPrincipal(Party p){
		return allMatches(
				new QueryDefault<Contract>(Contract.class,
						"ContractForPartyAsPrincipal",
						"principal",p));
	}

	@Programmatic
	public List<Party> getPartyFromPhoneNumber(String p){
		return allMatches(
				new QueryDefault<Party>(Party.class,
						"PartyFromPhoneNumber",
						"phoneNumber",p));
	}

	@Programmatic
	public List<PartyDatabaseRecord> getPartyDatabaseRecordFromId(Long p){
		return allMatches(
				new QueryDefault<PartyDatabaseRecord>(PartyDatabaseRecord.class,
						"PartyRecordFromId",
						"Id",p));
	}	
	@Programmatic
	public Party getPartyFromReference(String ref){
		List<Party>partiesForRef=allMatches(
				new QueryDefault<Party>(Party.class,
						"PartyFromReference",
						"reference",ref));
		if (partiesForRef.size() == 0 ) {
			return null;
		}else {
			return partiesForRef.get(0);
		}
	}
	@Programmatic
	public List<DailySalesTotalForParty> getDailySalesTotalForParty(Party p){
		//get party id
		//get party
		//String party = p.getUniqueRef();
		//party="2";
		return allMatches(
				new QueryDefault<DailySalesTotalForParty>( DailySalesTotalForParty.class,
						"DailySalesForParty"));
	}
}
