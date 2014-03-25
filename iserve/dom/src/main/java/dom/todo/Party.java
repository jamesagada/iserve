

package dom.todo;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.ArrayList;

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
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
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
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.QueryDefault;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@javax.jdo.annotations.Queries( {
	@javax.jdo.annotations.Query(
			name="party_all", language="JDOQL",  
			value="SELECT FROM dom.todo.Party"),
	@javax.jdo.annotations.Query(
			name="PartyFromPhoneNumber", language="JDOQL",  
			value="SELECT FROM dom.todo.Party where phoneNumber == :phoneNumber")			
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="Party_must_be_unique", members={"description"})
@ObjectType("PARTY")
@Audited
@AutoComplete(repository=Partytypes.class, action="autoListParty")
@Bookmarkable

public class Party  {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Party.class);
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
		buf.append(getDescription());
		return buf.toString();
	}
	// }}



	// {{ Description
	private String description;

	@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
	// words, spaces and selected punctuation
	@MemberOrder(sequence = "1")
	@Named("Name")
	@javax.jdo.annotations.Column(allowsNull="false")
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	// }}
	
	private String uniqueRef;
	@MemberOrder(sequence="1")
	@Named("UniqueReference")
	@javax.jdo.annotations.Column(allowsNull="false")
	public String getUniqueRef(){
		return uniqueRef;
	}
	public void setUniqueRef(final String ref){
		this.uniqueRef = ref;
	}
	
	//{{ phoneNumber (property)
	private String phoneNumber;
	@MemberOrder( sequence = "2")
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getPhoneNumber(){
		return phoneNumber;
	}
	public void setPhoneNumber(String number){
		this.phoneNumber=number;
	}
	//}}
	//{{ transportCard (property) the secureid of the transport card
	private String transportCard;
	@MemberOrder( sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getTransportCard(){
		return transportCard;
	}
	public void setTransportCard(String number){
		this.transportCard=number;
	}
	//}}

	//{{ The manager (property) which is set to current user but should allow us to pick.

	private User manager;
	@Hidden
	@javax.jdo.annotations.Column(allowsNull="true")
	public User getManager() {
		return manager;
	}
	public void setManager(User aValue) {
		manager = aValue;
	}

	//}}
	

	//  {{ logo (property)
	private Blob attachement;

	@javax.jdo.annotations.Persistent(defaultFetchGroup="true")
	@javax.jdo.annotations.Column(allowsNull="true")
	@MemberOrder( sequence = "7")
	@Hidden(where=Where.STANDALONE_TABLES)
	public Blob getAttachement() {
		return attachement;
	}

	public void setAttachement(final Blob attachement) {
		this.attachement=attachement;
	}
	// }}

	// {{ active - property
	private Boolean active;
	@MemberOrder( sequence = "5")
	@Named("Active Party?")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Boolean getActive(){
		return active;
	}
	public void setActive(final Boolean aValue){
		this.active=aValue;
	}
	//}}
	// {{ PartyType - property
	private Partytype partytype;
	@Named("Type")
	@MemberOrder( sequence = "6" )
	@javax.jdo.annotations.Column(allowsNull="false")
	public Partytype getPartytype(){
		return partytype;
	}
	public void setPartytype(final Partytype aValue){
		this.partytype=aValue;
	}
	//}} 
	// {{ StockLedger (derived collection)
	@Named("My StockLedgers")
	@MemberOrder(sequence = "8")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<StockLedger> getStockLedgers() {
		return inventory.getStockLedgerForParty(this);
	}
	// }}
	// {{ SubOrdinates Stock (derived collection)
	@Named("SubOrdinates StockLedgers")
	@MemberOrder(sequence = "9")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<StockLedger> getSubordinateStockLedgers() {
		//get list of subordinates and their subordinates etc
		List<Contract> subs = getSubordinates();
		List<StockLedger> sl = new ArrayList<StockLedger>();
		for (Contract c:subs){
			sl.addAll(inventory.getStockLedgerForParty(c.getSubordinate()));
		}
		return sl;
	}

	// }}
	// {{ SubOrdinates (derived collection)
	@Named("SubOrdinates")
	@MemberOrder(sequence = "7")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<Contract> getSubordinates() {
		
		ArrayList<Contract> contracts = new ArrayList<Contract>();
		contracts.addAll(partyTypes.getContractForPartyAsPrincipal(this));
		ArrayList<Contract> subs = new ArrayList<Contract>();
		for (Contract c:contracts){
			//retrieve the subordinates and get the subordinates contracts where the sub is a principal
			subs.addAll(c.getSubordinate().getSubordinates());
		}
		subs.addAll(contracts);
		return subs;
	}

	// }}
	// {{ Inventory Transactions (derived collection)
	@Named("Inventory Transactions")
	@MemberOrder(sequence = "10")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<Transaction> getTransactions() {
		return inventory.getTransactionsForParty(this);
	}

	// }}
	// {{ Payments (derived collection)
	@Named("My Payments")
	@MemberOrder(sequence = "11")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<Payment> getPayments() {
		return inventory.getPaymentsForParty(this);
	}

	// }}
	//{{ retrieve payments for this party or a given party
	// {{ SubOrdinates Payment (derived collection)
	@Named("SubOrdinates Payments")
	@MemberOrder(sequence = "12")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<Payment> getSubordinatePayments() {
		//get list of subordinates and their subordinates etc
		List<Contract> subs = getSubordinates();
		List<Payment> sl = new ArrayList<Payment>();
		for (Contract c:subs){
			sl.addAll(inventory.getPaymentsForParty(c.getSubordinate()));
			//sl.addAll(inventory.getPaymentsForParty(c));			
		}
		return sl;
	}
	//}}
	//}}
	//{{ DailySalesTotal (derived collection)
	@Named("Daily Sales Totals")
	@MemberOrder(sequence = "13")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<DailySalesTotalForParty> getDailySales() {
	
		return partyTypes.getDailySalesTotalForParty(this);
	}
	 
	//}}
	
	private DomainObjectContainer container;

	public void injectDomainObjectContainer(final DomainObjectContainer container) {
		this.container = container;
	}
	// }}

	// {{ injected: Partytypes
	private Partytypes partyTypes;

	public void injectPartytypes(final Partytypes partyTypes) {
		this.partyTypes = partyTypes;
	}
	// }}
	// {{ injected: Inventory
	private Inventory inventory;

	public void injectInventory(final Inventory inv) {
		this.inventory = inv;
	}
	// }}
	//{{ injected: Transactions
	private Transactions transactions;
	public void injectTransactions(final Transactions transactions){
		this.transactions = transactions;
	}
	// {{ Version (derived property)
	@Hidden(where=Where.ALL_TABLES)
	@Disabled
	@MemberOrder( sequence = "99")
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
	// {{ OwnedBy (property)
	private String ownedBy;

	@Named("Owner")
	// not shown in the UI
	@javax.jdo.annotations.Column(allowsNull="false")	
	public String getOwnedBy() {
		return ownedBy;
	}

	public void setOwnedBy(final String ownedBy) {
		this.ownedBy = ownedBy;
	}

	// }}

	//{{ SamePartyAs = compares two parties to see if it is the same party
	@Hidden
	@Programmatic
	public boolean samePartyAs(Party p) {
		if (this.getDescription().equals(p.getDescription())) {
			return true;
		}	else {

			return false;
		}
	}
	//}}
	//{{canAcceptToTransanction(dom.todo.Transaction) can this party accept this transaction to it. This will depend on
	// transaction type - you cannot sell to an operator, you can only redeem if you are allowed to
	// there must be a contract between to/fr party except if the to party is an end customer
	@Hidden
	@Programmatic
	public boolean canAcceptToTransanction(Transaction t) {
		boolean canAccept = false;
		if ((t.getTransactiontype() == Transaction.TransactionType.SALE)||
				(t.getTransactiontype() == Transaction.TransactionType.TRANSFER)){
			canAccept =  this.getPartytype().getCanReceive();
		}
		//do i have a contract with this party
		LOG.info("canAccepTo after checking if the to party can receive is " +canAccept);
		if (canAccept){
			canAccept = partyTypes.hasAContract(t.getFromParty(),this);
		}
		LOG.info("canAccepTo after checking if it theer is a contract is " +canAccept);
		//if this transaction is a redeem then i should be able to redeem
		if (t.getTransactiontype() == Transaction.TransactionType.REDEEM) {
			canAccept = getCanRedeem();
			LOG.info("Redeeming  is "+canAccept);			
		}
		//if this is a delivery, do i have an outstanding order?
		//if this is a sale or a transfer, i need to have it in stock
		//if not then i have to be able to sell without stock
		LOG.info("canAcceptTo  is "+canAccept);
		return canAccept;
	}
	//}}
	//{{canAcceptFromTransanction(dom.todo.Transaction) can this party accept this transaction from it. This will depend on
	// transaction type - you cannot sell to an operator, you can only redeem if you are allowed to
	// there must be a contract between to/fr party except if the to party is an end customer
	@Hidden
	@Programmatic
	public boolean canAcceptFromTransaction(Transaction t) {
		
		boolean canAccept = false;
		LOG.info("canAcceptFrom after checking if the from party can deliver voucher is "+canAccept);
		if ((t.getTransactiontype() == Transaction.TransactionType.SALE)||
				(t.getTransactiontype() == Transaction.TransactionType.TRANSFER)){
			canAccept = this.getPartytype().getCandeliver();
		}
		//do i have a contract with this party
		LOG.info("canAcceptFrom after checking if the from party can deliver voucher is "+canAccept);
		if (canAccept){
			canAccept = partyTypes.hasAContract(this,t.getToParty());
		}
		LOG.info("canAcceptFrom after checking if there is a contract is "+canAccept);
		//stock will be checked in stockledger so it is not important here
		if (t.getTransactiontype() == Transaction.TransactionType.SALE ||
				t.getTransactiontype() == Transaction.TransactionType.TRANSFER	) {
			canAccept =  this.inventory.partyHasEnoughInventoryForTransaction(this,t);
		}	
		//what if it is a redeem transaction
		if (t.getTransactiontype() == Transaction.TransactionType.REDEEM) {
			canAccept = getCanRedeem();
			LOG.info("Redeeming  is "+canAccept);			
		}		
		LOG.info("canAcceptFrom  is "+canAccept);		
		return canAccept;
	}
	//{{ getCanRedeem - determine from the partytype that it can do redemption
	@Hidden
	@Programmatic
	public boolean getCanRedeem(){
		boolean canRedeem = false;
		if (this.getPartytype().getRedeemsticket()) canRedeem = true;
		return canRedeem;
	}
	//{{ getNotifyOnTransaction
	@Hidden
	@Programmatic
	public boolean getNotifyOnTransaction(){
		boolean canRedeem = false;
		if (this.getPartytype().getNotifyOnTransaction()) canRedeem = true;
		return canRedeem;
	}
}
