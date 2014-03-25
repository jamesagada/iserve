

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
			name="partytype_all", language="JDOQL",  
			value="SELECT FROM dom.todo.Partytype"),
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="PartyType_description_must_be_unique", members={"description"})
@ObjectType("PARTYTYPE")
@Audited
//@PublishedObject(PartyTypeChangedPayloadFactory.class)
@AutoComplete(repository=Partytypes.class, action="autoComplete")
@Bookmarkable
//@Bounded
public class Partytype  {
	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Partytype.class);
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
		//buf.append("Type of Party - ");
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

	// {{ operator (property)
	private Boolean operator;
	@MemberOrder(sequence = "2")
	@javax.jdo.annotations.Column(allowsNull="true")
	public Boolean getOperator() {
		return operator;//deliberate erros
	}

	public void setOperator(final Boolean operator) {
		this.operator = operator;
	}
	// }}

	// {{selfVends (property)
	private Boolean selfvends;
	@MemberOrder(sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="false")
	public Boolean getSelfvends() {
		return selfvends;
	}
	public void setSelfvends( final Boolean aValue) {
		selfvends = aValue;
	}
	// }}
	// {isEndCustomer Is this an endcustomer
	private Boolean isEndCustomer;

	@MemberOrder(sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getIsEndCustomer() {
		return isEndCustomer;
	}
	public void setIsEndCustomer( final Boolean aValue) {
		isEndCustomer = aValue;
	}
	// }}

	// {{ usevirtualstock (property)

	private Boolean usevirtualstock;
	@MemberOrder(sequence = "4")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getUsevirtualstock() {
		return usevirtualstock;
	}
	public void setUsevirtualstock(Boolean aValue) {
		usevirtualstock = aValue;
	}

	// }}

	// {{ redeemsticket (property)
	private Boolean redeemsticket;
	@MemberOrder(sequence = "5")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getRedeemsticket() {
		return redeemsticket;
	}
	public void setRedeemsticket(Boolean aValue) {
		redeemsticket = aValue;
	}


	// }}


	// {{ canprincipal (property)
	private Boolean canprincipal;
	@MemberOrder(sequence = "6")
	@Named("Can be Principal in contract?")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getCanprincipal() {
		return canprincipal;
	}
	public void setCanprincipal(Boolean aValue) {
		canprincipal = aValue;
	}

	// }}
	// {{ conorder (property)
	private Boolean canorder;
	@MemberOrder(sequence = "7")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getCanorder() {
		return canorder;
	}
	public void setCanorder(Boolean aValue) {
		canorder = aValue;
	}

	//}}
	//{{ canreceive (property)
	private Boolean canReceive;
	@MemberOrder(sequence = "8")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getCanReceive() {
		return canReceive;
	}
	public void setCanReceive(Boolean aValue) {
		canReceive = aValue;
	}

	//}}
	//{{ candeliver (property)
	private Boolean candeliver;
	@MemberOrder(sequence = "8")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getCandeliver() {
		return candeliver;
	}
	public void setCandeliver(Boolean aValue) {
		candeliver = aValue;
	}

	//}}
	//{{ notifyOnTransaction (property)
	private Boolean notifyOnTransaction;
	@MemberOrder(sequence = "9")
	@javax.jdo.annotations.Column(allowsNull="false")

	public Boolean getNotifyOnTransaction() {
		if (notifyOnTransaction == null) return false;
		return notifyOnTransaction;
	}
	public void setNotifyOnTransaction(Boolean aValue) {
		this.notifyOnTransaction = aValue;
	}

	//}}

	// {{ OwnedBy (property)
	private String ownedBy;
	@Named("Owner")
	@Disabled
	@MemberOrder(sequence="999")
	@javax.jdo.annotations.Column(allowsNull="false")	
	public String getOwnedBy() {
		return ownedBy;
	}

	public void setOwnedBy(final String ownedBy) {
		this.ownedBy = ownedBy;
	}

	// }}

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

	public static Filter<Partytype> thoseOwnedBy(final String currentUser) {
		return new Filter<Partytype>() {
			@Override
			public boolean accept(final Partytype partyType) {
				return Objects.equal(partyType.getOwnedBy(), currentUser);
			}

		};
	}
}
