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

import java.math.RoundingMode;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.spi.PersistenceCapable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.joda.time.LocalDate;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Audited;
import org.apache.isis.applib.annotation.AutoComplete;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberGroups;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.value.Blob;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@javax.jdo.annotations.Queries( {
		@javax.jdo.annotations.Query(
					name="PaymentsForParty", language="JDOQL",  
					value="SELECT FROM dom.todo.Payment WHERE fromParty == :party || toParty == :party")
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="Payment_must_be_unique", members={"fromParty","toParty","transactionDate", "referenceNumber"})
@ObjectType("Payment")
@Audited
//@PublishedObject(VoucherDenominationChangedPayloadFactory.class)
//@AutoComplete(repository=Ticket.class, action="autoCompleteTransaction")
@MemberGroups({"General","Transaction"})
@MemberGroupLayout( columnSpans = {3,6,3,0}, left = "General", middle="Transaction" )
@Bookmarkable
//@Immutable(When.ONCE_PERSISTED)
public class Payment  /*, Locatable*/ { // GMAP3: uncomment to use https://github.com/danhaywood/isis-wicket-gmap3

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Payment.class);
    //check for disabled - when already processed and also when the user is not the one that created it.
    //or we can just make a call to a service that will interrogate the user management system
    public String disabled(final Identifier.Type type) {
    	// editing not allowed if you are not the owner of the record
    	if (this.getOwnedBy() != container.getUser().getName()) {
    		return "Blocked";
    	} else { 
    		return null;
    	}
     }

	// {{ Identification on the UI
	public String title() {
		final TitleBuffer buf = new TitleBuffer();
		buf.append(this.getAmount());
		buf.append(" Payment By  ");
		buf.append(this.getFromParty().title());
		buf.append(" To ");
		buf.append(this.getToParty().title());
		buf.append( " On");
		buf.append(this.getTransactionDate());
		return buf.toString();
	}
	// }}


	// {{ Description
	private String description;

	@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
	// words, spaces and selected punctuation
	@MemberOrder(name="General", sequence = "2")
	@javax.jdo.annotations.Column(allowsNull="false")
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	// }}


	// {{ fromParty (property) - who made the payment. is the party associated with this user
	private Party fromParty;
	@Disabled
	@MemberOrder(name="General" ,sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Party getFromParty() {
		return fromParty;
	}

	public void setFromParty(final Party fromParty) {
		this.fromParty=fromParty;
	}
	// }}

	// {{ toParty (property) - who received the payment
	private Party toParty;
	@MemberOrder(name = "General", sequence = "4")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Party getToParty() {
		return toParty;
	}

	public void setToParty(final Party toParty) {
		this.toParty=toParty;
	}
	// }}				   

	// {{ transactionDate (property)
	@javax.jdo.annotations.Persistent(defaultFetchGroup="true")
	private LocalDate transactionDate;
	@Named("Transaction Date")
	@MemberOrder(name="General", sequence = "5")
	@javax.jdo.annotations.Column(allowsNull="false")
	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(final LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}


	//}}
	//{{Amount (property)
	private BigDecimal amount;
	@MemberOrder( name="Transaction" ,sequence ="6" )
	@Named("Amount")
	@javax.jdo.annotations.Column(allowsNull="true")
	public BigDecimal getAmount(){
		return amount;
	}
	public void setAmount(final BigDecimal quantity){
		this.amount=quantity;
	}
	//}}
	//{{referenceNumber (property)
	private String referenceNumber;
	@MemberOrder( name="General",sequence = "1")
	@Named("Reference Number")
	@Hidden(where=Where.ALL_TABLES)
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getReferenceNumber(){
		return referenceNumber;
	}
	public void setReferenceNumber(final String serialNumber){
		this.referenceNumber=serialNumber;
	}
	//}}
	// {{ OwnedBy (property)
	private String ownedBy;

	@Hidden
	// not shown in the UI
	@javax.jdo.annotations.Column(allowsNull="false")
	public String getOwnedBy() {
		return ownedBy;
	}

	public void setOwnedBy(final String ownedBy) {
		this.ownedBy = ownedBy;
	}

	// }}


	// {{ Version (derived property)
	@Hidden(where=Where.ALL_TABLES)
	@Disabled
	@MemberOrder(name="General", sequence = "99")
	@Named("Version")
	public Long getVersionSequence() {
		if(!(this instanceof PersistenceCapable)) {
			return null;
		} 
		PersistenceCapable persistenceCapable = (PersistenceCapable) this;
		final Long version = (Long) JDOHelper.getVersion(persistenceCapable);
		return version;
	}
	// hide property (imperatively, based on state of object)
	public boolean hideVersionSequence() {
		return !(this instanceof PersistenceCapable);
	}
	// }}


	public static Filter<Payment> thoseOwnedBy(final String currentUser) {
		return new Filter<Payment>() {
			@Override
			public boolean accept(final Payment transaction) {
				return Objects.equal(transaction.getOwnedBy(), currentUser);
			}

		};
	}

	// }}
	//{{Status of the transaction itself
	private String status;
	@Disabled
	@Hidden(where=Where.ALL_TABLES)
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getStatus(){
		return this.status;
	}
	public void setStatus(String s){
		this.status=s;
	}
	//}}
	//{{ persisted(), this method is called after the transaction has been saved
	// we will process the transaction. Is this respected by the RO viewer
	@Programmatic

	public void persisted(){
		//try and process it
        LOG.info("Persisted: %s", this.container.titleOf(this));
		//if (this.status != "PROCESSED") processTransaction();
	}

	//}}
	//{{ processTransaction(), 
	//@Programmatic
	@Named("Process Payment")
	public Payment processPayment() {
		if (this.status =="PROCESSED") {
			return this;
		}
		this.setStatus("PROCESSING");
        LOG.info("Current Status "+ this.status);
        if (needNotification()) notifyParties();
			//if (needWriteToCard()) writeToCard();
		this.setStatus("PROCESSED");
		LOG.info("Current Status "+this.getStatus());
		LOG.info("Payment is" + this.toString());
		container.flush();
		return this;
	}
	//needNotification - do we need to notify parties that transaction has happened
	private boolean needNotification(){
		if (fromParty.getNotifyOnTransaction() || toParty.getNotifyOnTransaction()){
			return true;
		} else return false;
	}
	// notifyParties - if they need notification, then notify the parties
	private void notifyParties(){
		//send out message to the parties involved
		//notification should use either the emailservice or the publish subscribe service
	}
	// {{ injected: DomainObjectContainer
	private DomainObjectContainer container;

	public void injectDomainObjectContainer(final DomainObjectContainer container) {
		this.container = container;
	}
	// }}




}
