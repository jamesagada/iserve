

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
			name="contract_all", language="JDOQL",  
			value="SELECT FROM dom.todo.Contract WHERE ownedBy == :ownedBy"),
    @javax.jdo.annotations.Query(
					name="ValidContractByParty", language="JDOQL",  
					value="SELECT FROM dom.todo.Contract WHERE active == :status && principal == :principal && subordinate == :subordinate"),
	@javax.jdo.annotations.Query(
				name="ContractForPartyAsPrincipal", language="JDOQL",  
				value="SELECT FROM dom.todo.Contract WHERE principal == :principal")
			
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@ObjectType("CONTRACT")
@Audited
//@AutoComplete(repository=Partytypes.class, action="autoComplete")
@Bookmarkable

public class Contract  {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Contract.class);
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


	//{{ principal (property)
	private Party principal;
	@MemberOrder(sequence ="1")
	@Named("Principal Party")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Party getPrincipal(){
		return principal;
	}
	public void setPrincipal(final Party aValue){
		this.principal=aValue;
	}
	//}}
	//{{ subordinate (property)
	private Party subordinate;
	@MemberOrder(sequence = "2")
	@Named("Subordinate Party")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Party getSubordinate(){
		return subordinate;
	}
	public void setSubordinate(final Party aValue){
		this.subordinate = aValue;
	}
	//}}

	//{{ @Column(name = "active", length = 0, nullable = true)
	private Boolean active;
	@MemberOrder(sequence="4")
	@Named("Contract is Active")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Boolean getActive(){
		return active;
	}
	public void setActive(final Boolean aValue){
		this.active=aValue;
	}

	//}}
	//{{ @Column(name = "reference", length = 45, nullable = true)
	private String reference;
	@Hidden(where=Where.ALL_TABLES)
	@MemberOrder( sequence = "5")
	@Named("Contract Reference")
	@javax.jdo.annotations.Column(allowsNull="false")
	public String getReference(){
		return reference;
	}
	public void setReference(final String aValue){
		this.reference = aValue;
	}
	//}}
	//{{ @Column(name = "discount", length = 10, precision = 0, nullable = true)
	private java.math.BigDecimal discount;
	@Hidden(where=Where.ALL_TABLES)
	@MemberOrder( sequence ="6")
	@Named("Default Sales Discount%")
	@javax.jdo.annotations.Column(allowsNull="false")
	public java.math.BigDecimal getDiscount(){
		return discount;
	}
	public void setDiscount(final java.math.BigDecimal aValue){
		this.discount=aValue;
	}

	//}}

	//{{@Column(name = "start", nullable = true)
	@javax.jdo.annotations.Persistent(defaultFetchGroup="true")

	private LocalDate start;
	@Hidden(where=Where.ALL_TABLES)
	@MemberOrder(sequence = "7")
	@Named("Starting On")
	@javax.jdo.annotations.Column(allowsNull="false")
	public LocalDate getStart() {
		return start;
	}

	public void setStart(final LocalDate start) {
		this.start = start;
	}


	// }}


	//{{ @Column(name = "duration", nullable = true)

	private Integer duration;
	@Hidden(where=Where.ALL_TABLES)
	@MemberOrder(sequence = "8")
	@Named("Duration in days")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Integer getDuration(){
		return duration;
	}
	public void setDuration(final Integer aValue){
		this.duration=aValue;
	}
	//}}

	public static Filter<Contract> thoseOwnedBy(final String currentUser) {
		return new Filter<Contract>() {
			@Override
			public boolean accept(final Contract contract) {
				return Objects.equal(contract.getOwnedBy(), currentUser);
			}

		};
	}



	// {{ injected: DomainObjectContainer
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


}
