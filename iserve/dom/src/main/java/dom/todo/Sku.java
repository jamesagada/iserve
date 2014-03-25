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
			name="Sku_all", language="JDOQL",  
			value="SELECT FROM dom.todo.Sku"),
	@javax.jdo.annotations.Query(
			name = "ChildrenSku", language="JDOQL",
			value="SELECT FROM dom.todo.Sku WHERE parentSku == :sku"),		
			@javax.jdo.annotations.Query(
					name="Sku_autoComplete", language="JDOQL",  
					value="SELECT FROM dom.todo.Sku WHERE description.indexOf(:description) >= 0")
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="Sku_description_must_be_unique", members={"ownedBy","description"})
@ObjectType("SKU")
@Audited
//@PublishedObject(VoucherDenominationChangedPayloadFactory.class)
@AutoComplete(repository=Inventory.class, action="autoSku")
//@MemberGroups({"General", "Detail"})
@Bookmarkable
public class Sku  /*, Locatable*/ { // GMAP3: uncomment to use https://github.com/danhaywood/isis-wicket-gmap3
	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Sku.class);
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
	@javax.jdo.annotations.Column(allowsNull="false")
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	// }}


	// {{ qtyInSku (property)
	private BigDecimal quantityInSku;
	@Named("Number of Sku In Parent")
	@MemberOrder(sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="true")
	public BigDecimal getQuantityInSku() {
		return quantityInSku;
	}

	public void setQuantityInSku(final BigDecimal quantity) {
		this.quantityInSku = quantity;
	}

	// }}


	// {{unitSku (property)
	private Sku unitSku;
	@Named("Units are in ")
	@MemberOrder(sequence = "4")
	@javax.jdo.annotations.Column(allowsNull="true")
	public Sku getUnitSku() {
		return unitSku;
	}

	public void setUnitSku(final Sku unitSku) {
		this.unitSku = unitSku;
	}

	// }}

	// {{parentSku (property)
	private Sku parentSku;
	@Named("Parent Sku Is ")
	@MemberOrder(sequence = "2")
	@javax.jdo.annotations.Column(allowsNull="true")
	public Sku getParentSku() {
		return parentSku;
	}

	public void setParentSku(final Sku pSku) {
		this.parentSku = pSku;
	}

	// }}

	// {{ breakIntoUnits (property) can we break it into units in inventory
	private Boolean breakIntoUnits;
	@Named("Break Into Units When Less Than Sku")
	@MemberOrder(sequence = "4")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Boolean getBreakIntoUnits() {
		return breakIntoUnits;
	}

	public void setBreakIntoUnits(final Boolean breakIntoUnits) {
		this.breakIntoUnits = breakIntoUnits;
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


	// {{ delete (action)
	@Bulk
	@MemberOrder(sequence = "4")
	public List<Sku> delete() {
		//deleting a voucher class requires that we check for inventory  items
		//and delete them too except of container will address that.	
		container.removeIfNotAlready(this);
		container.informUser("Deleted " + container.titleOf(this));
		// invalid to return 'this' (cannot render a deleted object)
		return inventory.allSkus();//we need to ensure that allVoucherDenomination obeys business rules 
	}
	// }}

	public static Filter<Sku> thoseOwnedBy(final String currentUser) {
		return new Filter<Sku>() {
			@Override
			public boolean accept(final Sku sku) {
				return Objects.equal(sku.getOwnedBy(), currentUser);
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

	@Hidden
	@Programmatic
	public boolean equal(Sku s){
		return this.title().equals(s.title());
	}
	@Hidden
	@Programmatic
	public BigDecimal getUnitSkuQuantity(){
		//determine how many single units are in this sku
		//we have to find the unit sku - we should find who our immediate children are
		//then we can recursively call this
		BigDecimal multiplier = new BigDecimal(1);
		List<Sku> childSku = inventory.getChildrenSku(this);
		if (childSku.size() > 0 ) {
			Sku s = childSku.get(0);// take the first one
			multiplier = multiplier.multiply(s.getQuantityInSku().multiply(s.getUnitSkuQuantity()));
			
		}
		LOG.info(this.getDescription() + " multiply " + multiplier );
	return multiplier;	
	}
}

