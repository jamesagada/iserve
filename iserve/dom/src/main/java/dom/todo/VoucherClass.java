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
			name="voucherClass_all", language="JDOQL",  
			value="SELECT FROM dom.todo.VoucherClass"),
			@javax.jdo.annotations.Query(
					name="voucherClass_autoComplete", language="JDOQL",  
					value="SELECT FROM dom.todo.VoucherClass WHERE description.indexOf(:description) >= 0")
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="VoucherClass_description_must_be_unique", members={"description"})
@ObjectType("VOUCHERCLASS")
@Audited
//@PublishedObject(VoucherClassChangedPayloadFactory.class)
@AutoComplete(repository=Inventory.class, action="autoListVoucherClass")
//@MemberGroups({"General", "Detail"})
@Bookmarkable
public class VoucherClass /*, Locatable*/ { // GMAP3: uncomment to use https://github.com/danhaywood/isis-wicket-gmap3
	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VoucherClass.class);
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
		buf.append(" - from ");
		buf.append(this.getIssuingParty().title());
		return buf.toString();
	}
	// }}


	// {{ Description
	private String description;

	@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
	// words, spaces and selected punctuation
	@MemberOrder(sequence = "1")
	@javax.jdo.annotations.Column(allowsNull="false")
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	// }}


	// {{ validFrom (property)
	@javax.jdo.annotations.Persistent(defaultFetchGroup="true")
	private LocalDate validFrom;
	@Named("Valid Starting From")
	@MemberOrder(sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="false")
	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(final LocalDate validFrom) {
		this.validFrom = validFrom;
	}
	public void clearValidFrom() {
		setValidFrom(null);
	}

	// }}


	// {{ validTo (property)
	@javax.jdo.annotations.Persistent(defaultFetchGroup="true")
	private LocalDate validTo;
	@Named("Valid Till")
	@MemberOrder(sequence = "4")
	@javax.jdo.annotations.Column(allowsNull="false")
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


	// {{ IssuingParty (property)
	private Party issuingParty;
	@Named("Issuing Party")
	@MemberOrder(sequence = "5")
	@javax.jdo.annotations.Column(allowsNull="false")	
	public Party getIssuingParty() {
		return issuingParty;
	}

	public void setIssuingParty(final Party issuingParty) {
		this.issuingParty = issuingParty;
	}

	// }}


	// {{ requireRedeem (property)

	private Boolean requireRedeem;
	@Named("Has to be Redeemed")
	@MemberOrder(sequence = "6")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Boolean getRequireRedeem() {
		return requireRedeem;
	}

	public void setRequireRedeem(final Boolean requireRedeem) {
		this.requireRedeem = requireRedeem;
	}


	// }}

	// {{ maxRedeems (property)
	private int maxRedeems;
	@Named("Maximum Redeems Allowed")
	@MemberOrder(sequence = "7")
	@javax.jdo.annotations.Column(allowsNull="true")
	public int getMaxRedeems() {
		return maxRedeems;
	}

	public void setMaxRedeems(final int maxRedeems) {
		this.maxRedeems = maxRedeems;
	}

	// }}

	//{{ validity (property)
	private int validity;
	@Named("Default Validity")
	@javax.jdo.annotations.Column(allowsNull="true")
	public int getValidity(){
		return validity;
	}
	public void setValidity(final int validity){
		this.validity=validity;
	}
	//}}
	// {{ isVirtual - we do not keep physical tickets
	private boolean isVirtual;

	@Named("Is Virtual?")
	@javax.jdo.annotations.Column(allowsNull="false")
	public boolean getIsVirtual() {
		return isVirtual;
	}

	public void setIsVirtual(final boolean aValue) {
		this.isVirtual = aValue;
	}

	// }}


	// {{ OwnedBy (property)
	private String ownedBy;

	@Disabled
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


	// {{ clone (action)
	@Named("Clone")
	// the name of the action in the UI
	@MemberOrder(sequence = "3")
	// nb: method is not called "clone()" is inherited by java.lang.Object and
	// (a) has different semantics and (b) is in any case automatically ignored
	// by the framework
	public VoucherClass duplicate(
			@Named("Description") 
			String description,
			@Named("Issuing Party")
			Party issuingParty, 
			@Named("Valid Starting From") 
			
			LocalDate validFrom,
			@Named("Valid Till") 
			
			LocalDate validTo,								  
			@Named(" Maximum Redeems Allowed") 
			
			int maxRedeems,
			@Named("Require Redeem")
			
			Boolean requireRedeem,
			@Named("Default Validity of Issued Voucher")
			int validity){
		return inventory.newVoucherClass(description, issuingParty, validFrom, validTo,maxRedeems, requireRedeem, validity);
	}
	public String default0Duplicate() {
		return getDescription() + " - Copy";
	}
	public Party default1Duplicate() {
		return getIssuingParty();
	}
	public LocalDate default2Duplicate() {
		return getValidFrom();
	}
	public LocalDate default3Duplicate() {
		return getValidTo();

	}   
	public int default4Duplicate() {
		return getMaxRedeems();
	}
	public Boolean default5Duplicate(){
		return getRequireRedeem();
	}

	public int default6Duplicate(){
		return getValidity();
	}
	// }}


	// {{ delete (action)
	@Bulk
	@MemberOrder(sequence = "4")
	public List<VoucherClass> delete() {
		//deleting a voucher class requires that we check for inventory  items
		//and delete them too except of container will address that.	
		container.removeIfNotAlready(this);
		container.informUser("Deleted " + container.titleOf(this));
		// invalid to return 'this' (cannot render a deleted object)
		return inventory.allVoucherClass();//we need to ensure that allVoucherClass obeys business rules 
	}
	// }}



	// {{ Denominations (derived collection)
	@MemberOrder(sequence = "5")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<VoucherDenomination> getDenominations() {
		return inventory.denominationsForVoucherClass(this);
	}

	// }}
	// {{ StockLedger (derived collection)
	@MemberOrder(sequence = "6")
	@NotPersisted
	@Render(Type.LAZILY)
	public List<StockLedger> getStockLedger() {
		return inventory.getStockLedgerForVoucherClass(this);
	}

	// }}


	public static Filter<VoucherClass> thoseOwnedBy(final String currentUser) {
		return new Filter<VoucherClass>() {
			@Override
			public boolean accept(final VoucherClass voucherClass) {
				return Objects.equal(voucherClass.getOwnedBy(), currentUser);
			}

		};
	}

	// }}



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

	//{{canAcceptTransanction(dom.todo.Transaction) can this voucherClass accept this transaction  
	// transaction type -  
	@Hidden
	@Programmatic
	public boolean canAcceptTransanction(Transaction t) {
		boolean canAccept = true;
		if (t.getTransactiontype() == Transaction.TransactionType.SALE) {
			//check whether it is still available for sale within this period
		}
		if (t.getTransactiontype() == Transaction.TransactionType.REDEEM) {
			//check whether it can be redeemed within the period ie validity has not expired or maxredeem exceeded.
			//this requires that the original sales transaction be found. if not found then it cannot be redeemed
		}
		return canAccept;
	}
}
