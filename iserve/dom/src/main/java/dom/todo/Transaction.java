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
			name="TransactionsForTicket", language="JDOQL",  
			value="SELECT FROM dom.todo.Transaction WHERE voucherCode == :voucherCode && serialNumber == :serialNumber"),
			@javax.jdo.annotations.Query(
					name="TransactionsForParty", language="JDOQL",  
					value="SELECT FROM dom.todo.Transaction WHERE fromParty == :party || toParty == :party"),
			@javax.jdo.annotations.Query(
					name="TransactionsFromPartyForDate", language="JDOQL",
					value="SELECT FROM dom.todo.Transaction WHERE fromParty == :party && transactionDate == :transactiondate"),		
			@javax.jdo.annotations.Query(
					name="TransactionsFromPartyForInterval", language="JDOQL",
					value="SELECT FROM dom.todo.Transaction WHERE fromParty == :party && transactionDate >= :beginPeriod && transactionDate <= :endPeriod")		
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="Transaction_must_be_unique", members={"ownedBy","fromParty","toParty","transactionDate", "sku","serialNumber","voucherCode"})
@ObjectType("Transaction")
@Audited
//@PublishedObject(VoucherDenominationChangedPayloadFactory.class)
@AutoComplete(repository=Ticket.class, action="autoCompleteTransaction")
// @MemberGroups({"General","Transaction","Ticket"})
@MemberGroupLayout( columnSpans = {3,6,3,0}, left = "General", middle="Transaction",right="Ticket" )
@Bookmarkable
//@Immutable(When.ONCE_PERSISTED)
public class Transaction  /*, Locatable*/ { // GMAP3: uncomment to use https://github.com/danhaywood/isis-wicket-gmap3

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Transaction.class);
    //check for disabled - when already processed and also when the user is not the one that created it.
    //or we can just make a call to a service that will interrogate the user management system
    public String disabled(final Identifier.Type type) {
       // if(this.getStatus() =="PROCESSED") {
        //    return "Cannot modify after processing";
       // }
       // else return null;
    	return this.getStatus();
    }

	// {{ Identification on the UI
	public String title() {
		final TitleBuffer buf = new TitleBuffer();
		buf.append(getDescription());
		buf.append(" -From  ");
		buf.append(this.getFromParty().title());
		buf.append(" -To ");
		buf.append(this.getToParty().title());
		//buf.append(this.getTerminal().title());
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


	// {{ fromParty (property) - who did the stock leave his hand
	private Party fromParty;
	@MemberOrder(name="General" ,sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Party getFromParty() {
		return fromParty;
	}

	public void setFromParty(final Party fromParty) {
		this.fromParty=fromParty;
	}
	// }}

	// {{ toParty (property) - who did the stock enter his hand
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


	// }}
	//{{ sku (property) sku in which transaction is made
	private Sku sku;
	@MemberOrder( name="Transaction", sequence = "7")
	@Named("Stock Units")
	@javax.jdo.annotations.Column(allowsNull="true")
	public Sku getSku(){
		return sku;
	}
	public void setSku(final Sku sku){
		this.sku=sku;
	}
	//}}
	//{{Quantity (property)
	private BigDecimal quantity;
	@MemberOrder( name="Transaction" ,sequence ="8" )
	@Named("Quantity or Value")
	@javax.jdo.annotations.Column(allowsNull="true")
	public BigDecimal getQuantity(){
		return quantity;
	}
	public void setQuantity(final BigDecimal quantity){
		this.quantity=quantity;
	}
	//}}
	//{{SerialNumber (property)
	private String serialNumber;
	@MemberOrder( name="Ticket",sequence = "9")
	@Named("Starting Serial Number")
	@Hidden(where=Where.ALL_TABLES)
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getSerialNumber(){
		return serialNumber;
	}
	public void setSerialNumber(final String serialNumber){
		this.serialNumber=serialNumber;
	}
	//}}
	//{{VoucherCode (property) this is unique code for a specific ticket
	private String voucherCode;
	@MemberOrder( name="Ticket",sequence = "10" )
	@Named("Voucher Code")
	@Hidden(where=Where.ALL_TABLES)
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getVoucherCode(){
		return voucherCode;
	}
	public void setVoucherCode( final String voucherCode ){
		this.voucherCode = voucherCode;
	}
	//}}
	//{{Terminal (property) from which the sale was made or ticket issued
	private Terminal terminal;
	@Hidden(where=Where.ALL_TABLES)
	@MemberOrder( name="General",sequence = "11")
	@Named("Terminal")
	@javax.jdo.annotations.Column(allowsNull="true")
	public Terminal getTerminal(){
		return terminal;
	}
	public void setTerminal(final Terminal terminal){
		this.terminal = terminal;
	}
	//}}
	//{{VoucherDenomination (property) 
	private VoucherDenomination voucherDenomination;
	@MemberOrder( name="General",sequence = "6")
	@Named("Voucher Denomination")
	@javax.jdo.annotations.Column(allowsNull="false")
	public VoucherDenomination getVoucherDenomination(){
		return voucherDenomination;
	}
	public void setVoucherDenomination(final VoucherDenomination voucherDenomination){
		this.voucherDenomination = voucherDenomination;
	}
	//}}
	public static enum TransactionType {
	    TRANSFER, RETURN, REDEEM, SALE, ADJUST, SALEREPORT;
	}
	//{{BarCode - of the ticket. This is not stored but is generated when needed
	//}}
	//{{ TransactionType (property)
	private TransactionType transactiontype;
	@MemberOrder( name="General",sequence = "1")
	@Named("Tranaction Type")
	@javax.jdo.annotations.Column(allowsNull="false")
	public TransactionType getTransactiontype(){
		return transactiontype;
	}
	public void setTransactiontype(final TransactionType transactionType){
		this.transactiontype =  transactionType;
	
	}

	//}}

	// {{ validTo (property) - default from voucherDenomination but only when a sale is being made
	@javax.jdo.annotations.Persistent(defaultFetchGroup="true")
	private LocalDate validTo;
	@Named("Valid Till")
	@MemberOrder(name="Ticket",sequence = "12")
	@Hidden(where=Where.ALL_TABLES)
	@javax.jdo.annotations.Column(allowsNull="true")
	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(final LocalDate validTo) {
		this.validTo = validTo;
	}
	public void clearValidTo() {
		setValidTo(null);
	}

	// }}

	// {{ faceValue (property) //should have default fetched from voucher denomination
	private java.math.BigDecimal faceValue;
	@Named("Face Value")
	@MemberOrder(name="Transaction",sequence = "13")
	@Disabled
	@Hidden(where=Where.ALL_TABLES)
	@javax.jdo.annotations.Column(allowsNull="true")
	public java.math.BigDecimal getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(final java.math.BigDecimal faceValue) {
		this.faceValue = faceValue;
	}

	// }}
	// {{ ticketStatus - used for single transaction ticket
	private String ticketStatus;
	@Hidden(where=Where.ALL_TABLES)
	@Named("Ticket Status")
	@MemberOrder(name="Ticket",sequence = "14")
	@javax.jdo.annotations.Column(allowsNull="true")
	@Disabled
	public String getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(final String t) {
		this.ticketStatus = t;
	}

	// }}				   

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
	@MemberOrder(name="Detail", sequence = "99")
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


	public static Filter<Transaction> thoseOwnedBy(final String currentUser) {
		return new Filter<Transaction>() {
			@Override
			public boolean accept(final Transaction transaction) {
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
	//{{from -Party Reference For Reporting
	private String fromPartyReference;
	@Disabled
	@Hidden
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getFromPartyReference(){
		if (this.getFromParty() != null) this.fromPartyReference = this.getFromParty().getUniqueRef();
		return this.fromPartyReference;
	}
	public void setFromPartyReference(String s){
		if (this.getFromParty() != null) this.fromPartyReference = this.getFromParty().getUniqueRef();
	}
	//}}
	//{{to -Party Reference For Reporting
	private String toPartyReference;
	@Disabled
	@Hidden
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getToPartyReference(){
		if (this.getToParty() != null) this.toPartyReference = this.getToParty().getUniqueRef();
		return this.toPartyReference;
	}
	public void setToPartyReference(String s){
		if (this.getToParty() != null) this.toPartyReference = this.getToParty().getUniqueRef();
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
	//{{ validate
	@Programmatic
	public String validate() {
		//validate the transaction object so we can ensure the transaction can happen
		LOG.info("Validating Transaction");
		String vS="";
		if (!this.toParty.canAcceptToTransanction(this)) vS=vS.concat(this.toParty.title().concat(" cannot do this transaction \n"));
		if (!this.fromParty.canAcceptFromTransaction(this)) vS=vS.concat(this.fromParty.title().concat(" cannot do this transaction \n"));
		if (!this.voucherDenomination.getVoucherClass().canAcceptTransanction(this)) vS=vS.concat(this.voucherDenomination.getVoucherClass().title().concat(" cannot do this transaction \n"));
		LOG.info("Transaction validation result "+vS);
		if (vS.isEmpty()) {
			return null;	
		}else{
			return vS;
		}

	}
	//}}
	//{{ processTransaction(), 
	//@Programmatic
	@Named("Process")
	public Transaction processTransaction() {
		if (this.status =="PROCESSED") {
			return this;
		}
		this.setStatus("PROCESSING");
        LOG.info("Current Status "+ this.status);
		// check that the fromParty and toParty are ok for the transaction
		if (this.getToParty().canAcceptToTransanction(this) && 
				this.getFromParty().canAcceptFromTransaction(this) && 
				this.getVoucherDenomination().getVoucherClass().canAcceptTransanction(this)) {
			//do we need to generate a ticket?, then we generate it and put the details in the 
			//transaction itsel
			String vS=validate();
			if (vS != null) {
				container.informUser(vS);
			}
	        LOG.info("Transaction can be processed. Check for ticket generation");
			if ( needGeneratedTicket() ) generateTicket();
			//If we are redeeming we just find the sales record for the 
			//ticket and mark it as redeemed. If we can't find  it then 
			//the processing actually has an error
			if ( needToRedeem() ) redeemTicket();
			//now we need to update the inventory
			if (needInventoryUpdate() ) updateInventory();
			//if we need to send notification, we can do it here or 
			//depend on the publishing service
			if (needNotification()) notifyParties();
			//if (needWriteToCard()) writeToCard();
		}
		this.setStatus("PROCESSED");
		LOG.info("Current Status "+this.getStatus());
		LOG.info("Transaction is" + this.toString());
		container.flush();
		return this;
	}

	//{{needToRedeem - is this a ticket redeem transaction
	private boolean needToRedeem(){
		//at this point we just use the transaction type
		// can the from/toparty redeem?
		// can the voucherClass be redeemed
		if ((transactiontype == TransactionType.REDEEM) &&
				fromParty.getCanRedeem() & voucherDenomination.getVoucherClass().getRequireRedeem()) {
			return true;
		} else {
			return false;
		}
	}
	//}}
	//{{ needGeneratedTicket - does this require us to generate a ticket
	private boolean needGeneratedTicket() {
		//generate ticket depends on whether tickets are virtual
		//and not stored in the inventory as items
		// it is also only when we are doing a sale from terminal
		// to an end customer. So we need to check these conditions
		VoucherClass vc=this.voucherDenomination.getVoucherClass();
		LOG.info("This voucher is virtual "+vc.getIsVirtual());
		if (vc.getIsVirtual() && 
				toParty.getPartytype().getIsEndCustomer() &&
				this.getTransactiontype() == TransactionType.SALE)  {
			return true;
		} else {	    
			return false; 
		}
	}
	//needInventoryUpdate - does this transaction require us to update inventory
	private boolean needInventoryUpdate(){
		return true;
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
	//generateTicket - when the ticket is not being stored in the inventory individually, we will need to generate a ticket
	@Programmatic
	@Hidden
	public void generateTicket() {
		//if the vouchercode and serial number are already filled then dont generate
		if ((this.voucherCode == null) || ( this.serialNumber == null ) ) {
			//there has to be an algorithm for generating these numbers. The algorithm is specific to
			//the voucherClass or denomination
			// we will ask the denomination to generate it
			// we will pass it the whole record of the transaction
			// these are really just random numbers
			// the numbers have to be attached to the party that is buyinh.
			// it is therefore necessary to ensure that the toParty has a valid card

			inventory.generateTicketForTransaction(this);
		}
	}	//updateInventory - update the inventory records
	// this is going to be tricky because we need to know
	// which inventory record to update
	private void updateInventory() {
		//find inventory records
		//if they exist then update them according to the inventory update rules for the voucherClass, 
		//we just call the inventory service to do it
        LOG.info("Update Inventory");
		inventory.UpdateInventoryFromTransaction(this); // what if it fails? 
	}
	//redeem ticket -- ticket detail is in the voucherCode and serial number
	private void redeemTicket(){
		inventory.redeemTicket(this);
	}
	// {{ injected: DomainObjectContainer
	private DomainObjectContainer container;

	public void injectDomainObjectContainer(final DomainObjectContainer container) {
		this.container = container;
	}
	// }}

	// {{ injected: Ticket

	private Ticket ticket;
	@Hidden
	public void injectTicket(final Ticket ticket) {
		this.ticket = ticket;
	}
	// }}
	// {{ injected:Inventory
	private Inventory inventory;
	@Hidden
	public void injectInventory(final Inventory inventory) {
		this.inventory = inventory;
	}
	// }}


}
