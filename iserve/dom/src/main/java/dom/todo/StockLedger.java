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
import java.util.List;
import java.util.ArrayList;
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
			name="StockLedger_all", language="JDOQL",  
			value="SELECT FROM dom.todo.StockLedger"),
	@javax.jdo.annotations.Query(
			name="StockLedger_autoComplete", language="JDOQL",  
			value="SELECT FROM dom.todo.StockLedger WHERE description.indexOf(:description) >= 0"),
	@javax.jdo.annotations.Query(
			name="StockLedgerForParty", language="JDOQL",
			value = "SELECT FROM dom.todo.StockLedger WHERE stockingParty == :party"),
	@javax.jdo.annotations.Query(
			name="StockLedgerForVoucherClass",language="JDOQL",
			value = "SELECT FROM dom.todo.StockLedger WHERE voucherDenomination.voucherClass ==:voucher"),
	@javax.jdo.annotations.Query(
			name="StockLedgerForVoucherDenomination",language="JDOQL",
			value = "SELECT FROM dom.todo.StockLedger WHERE voucherDenomination == :denomination"),

})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="Stock_record_must_be_unique", members={"ownedBy","stockingParty","voucherDenomination","sku","serialNumber"})
@ObjectType("StockLedger")
@Audited
@PublishedObject(StockLedgerChangedPayLoadFactory.class)
@AutoComplete(repository=Inventory.class, action="autoCompleteStockLedger")
//@MemberGroups({"General", "Detail"})
@Bookmarkable
public class StockLedger  /*, Locatable*/ { // GMAP3: uncomment to use https://github.com/danhaywood/isis-wicket-gmap3

	//	private static final long ONE_WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L;

	//    public static enum Category {
	//        Professional, Domestic, Other;
	//    }

	// {{ Identification on the UI
		private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StockLedger.class);
	    public String disabled(final Identifier.Type type) {
	    	// editing not allowed if you are not the owner of the record
	    	if (this.getOwnedBy() != container.getUser().getName()) {
	    		return "Blocked";
	    	} else { 
	    		return null;
	    	}
	     }
	public String title() {
		final TitleBuffer buf = new TitleBuffer();
		buf.append(getSku().title());
		buf.append(" - of ");
		buf.append(getVoucherDenomination().title());
		buf.append(" with ");
		buf.append(this.getStockingParty().title());
		return buf.toString();
	}
	// }}

	// {{ VoucherDenomination
	private VoucherDenomination voucherDenomination;
	@MemberOrder(sequence = "1")
	@javax.jdo.annotations.Column(allowsNull="false")
	public VoucherDenomination getVoucherDenomination() {
		return voucherDenomination;
	}

	public void setVoucherDenomination(final VoucherDenomination voucherDenomination) {
		this.voucherDenomination = voucherDenomination;
	}
	// }}

	//{{ stockingParty (property) who is holding the stock physically or who is it assigned to
	private Party stockingParty;
	@MemberOrder(sequence = "2")
	@Named("Party holding the stock")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Party getStockingParty(){
		return stockingParty;
	}
	public void setStockingParty( final Party stockingParty){
		this.stockingParty=stockingParty;
	}
	//}}
	//{{ sku (property) stock keeping units in which the stock is kept
	private Sku sku;
	@MemberOrder( sequence = "3")
	@Named("StockedIn")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Sku getSku(){
		return sku;
	}
	public void setSku(final Sku sku){
		this.sku=sku;
	}
	//}}
	//{{ serialNumber - if sku qty is one, then this is the serialNumber of the ticket otherwise is the start of the batch
	private String serialNumber;
	@Hidden(where=Where.ALL_TABLES)
	@MemberOrder( sequence = "4")
	@Named("Serial Number")
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getSerialNumber(){
		return serialNumber;
	}
	public void setSerialNumber(final String number){
		this.serialNumber=number;
	}
	//}}
	//{{ voucherCode - if sku is single, this is the voucherCode of the voucher
	private String voucherCode;
	@Hidden(where=Where.ALL_TABLES)
	@MemberOrder( sequence = "5" )
	@Named("Voucher Code")
	@javax.jdo.annotations.Column(allowsNull="true")
	public String getVoucherCode(){
		return voucherCode;
	}
	public void setVoucherCode(final String voucherCode){
		this.voucherCode=voucherCode;
	}
	//}}
	//{{ quantityInStock - quantity in stock. We are keeping details of the stock in the journal
	// this means that if we open a pack, we have to readjust the stock to 
	//reflect that the pack has become units or that carton has become rolls
	//This might also be outstanding value
	private BigDecimal quantityInStock;
	@MemberOrder(sequence ="6")
	@Named("Quantity In Stock")
	@javax.jdo.annotations.Column(allowsNull="true", scale=12)
	public BigDecimal getQuantityInStock(){
		return quantityInStock;
	}
	public void setQuantityInStock(final BigDecimal qty){
		this.quantityInStock=qty;
	}
//}}
	// {{ journal (property). collection of transactions that happened on this stockledger
	private List<Transaction> journal = new ArrayList<Transaction>();

	@Named("Stock Journal")
	@MemberOrder(sequence="7")
	
	public List<Transaction> getJournal() {
		return journal;
	}

	public void setJournal(List<Transaction> j) {
		this.journal = j;
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



	public static Filter<StockLedger> thoseOwnedBy(final String currentUser) {
		return new Filter<StockLedger>() {
			@Override
			public boolean accept(final StockLedger ledger) {
				return Objects.equal(ledger.getOwnedBy(), currentUser);
			}

		};
	}

	// }}
	//{{UpdateFromTransaction(Transaction t)
	@Hidden
	@Programmatic
	public void updateFromTransaction(Transaction t) {
		//when we have a transaction and we want to update
		// we first determine if we are holding party or issuing party
		//Is our stocking party the fromParty or the toParty 
		LOG.info("updating stockledger with transaction -->" + t.title());
		if ( this.getStockingParty().samePartyAs(t.getFromParty()) ) {
			LOG.info("Updating quantity in stock for fromParty");
			//now check the kind of transaction
			// it will have been better if we put the transactiontypes in a table and define their properties
			//TODO - check if the batch can be broken into units. if so you have to change the sku to units
			if (( t.getTransactiontype() == Transaction.TransactionType.SALE ) ||
					(t.getTransactiontype() == Transaction.TransactionType.SALEREPORT) ||  
					(t.getTransactiontype() == Transaction.TransactionType.TRANSFER)  || (t.getTransactiontype() == Transaction.TransactionType.RETURN)) {
				//
				if (this.getSku().equal(t.getSku())) {
					LOG.info("Updating quantity in stock for same sku. Quantity before " + t);	
					this.setQuantityInStock ( this.getQuantityInStock().subtract(t.getQuantity()));
					this.journal.add(t);
				} else { //we have a different sku. we need to convert the bigger sku to units of the smaller sku
					//and then create new stock record for the smaller sku and then update the smaller ones
					//we will still update this sku
					//determine relationship between skus
					LOG.info("Updating quantity in stock for different sku");					
					BigDecimal sgd = inventory.convertSkuToSku(this.getSku(), t.getSku(),t.getVoucherDenomination());
					if (sgd.compareTo(BigDecimal.ZERO) > 0 ) { 
						//multiply transaction sku by conversion factor and the update this inventory correctly
						LOG.info("Convert from "+ this.getSku().getDescription() + " to " + t.getSku().getDescription() + " by multiplying with " + sgd);
						this.setQuantityInStock(this.getQuantityInStock().subtract(t.getQuantity().divide(sgd)));
						this.journal.add(t);
						}
				}
			}
		}
		if ( this.getStockingParty().samePartyAs(t.getToParty()) ) {
			LOG.info("Updating quantity in stock for toParty");
			//now check the kind of transaction
			// it will have been better if we put the transactiontypes in a table and define their properties
			if (( t.getTransactiontype() == Transaction.TransactionType.SALE ) ||
					(t.getTransactiontype() == Transaction.TransactionType.SALEREPORT) ||  
					(t.getTransactiontype() == Transaction.TransactionType.TRANSFER) ||
					(t.getTransactiontype() == Transaction.TransactionType.RETURN)) {
				//
				if (this.getSku().equal(t.getSku())) {
					LOG.info("Updating quantity in stock for same sku");
					this.setQuantityInStock ( this.getQuantityInStock().add(t.getQuantity()));
					this.journal.add(t);
				} else { //
					//determine relationship between skus
					BigDecimal sgd = inventory.convertSkuToSku(this.getSku(), t.getSku(),t.getVoucherDenomination());
					if (sgd.compareTo(BigDecimal.ZERO) > 0 )  { 
						//multiply transaction sku by conversion factor and the update this inventory correctly
						LOG.info("Convert from "+ this.getSku().getDescription() + " to " + t.getSku().getDescription() + " by multiplying with " + sgd);
						this.setQuantityInStock(this.getQuantityInStock().add(t.getQuantity().divide(sgd)));
						this.journal.add(t);
					}
				}
			}
		}
	}
	//}}
	//{{ StockLedgerForTransaction - determine if this stockledger is for this transaction
	@Hidden
	@Programmatic
	public boolean stockLedgerForTransaction(Transaction t){
		boolean retval=false;
		if ( this.getStockingParty().samePartyAs(t.getToParty()) || this.getStockingParty().samePartyAs(t.getFromParty())) {
			//check for voucherDenomination and then look at serialNumber ( if this is a batch?)
			// should we check for the SKU - if we check for the sku then we could have a problem. We should have only one sku in store
			if (this.getVoucherDenomination().sameDenominationAs(t.getVoucherDenomination())) {
				retval=true;
			}
		}
		return retval;
	}
	//}}

	// {{ injected: DomainObjectContainer
	private DomainObjectContainer container;

	public void injectDomainObjectContainer(final DomainObjectContainer container) {
		this.container = container;
	}
	// }}

	// {{ injected: inventory
	private Inventory inventory;

	public void injectInventory(final Inventory inventory) {
		this.inventory = inventory;
	}
	// }}


}
