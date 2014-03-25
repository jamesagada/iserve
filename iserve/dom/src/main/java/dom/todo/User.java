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
import java.util.Collections;
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
			name="AllUsers", language="JDOQL", 
			value="SELECT FROM dom.todo.User"),
	@javax.jdo.annotations.Query(
			name="UserAutoComplete", language="JDOQL",  
			value="SELECT FROM dom.todo.User"),
	@javax.jdo.annotations.Query(
			name="UserForName", language="JDOQL",  
			value="SELECT FROM dom.todo.User WHERE name == :name")
	
})
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
@javax.jdo.annotations.Unique(name="User_must_be_unique", members={"name"})
@ObjectType("User")
@Audited
//@PublishedObject(VoucherDenominationChangedPayloadFactory.class)
@AutoComplete(repository=UserManager.class, action="autoUser")
//@MemberGroups({"General", "Detail"})
@Bookmarkable
public class User  /*, Locatable*/ { // GMAP3: uncomment to use https://github.com/danhaywood/isis-wicket-gmap3

	//	private static final long ONE_WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L;

	//    public static enum Category {
	//        Professional, Domestic, Other;
	//    }

	// {{ Identification on the UI
	public String title() {
		final TitleBuffer buf = new TitleBuffer();
		buf.append(getName());
		return buf.toString();
	}
	// }}

	// {{ Name
	private String name;

	// words, spaces and selected punctuation
	@MemberOrder(sequence = "1")
	@javax.jdo.annotations.Column(allowsNull="False")
	public String getName() {
		return name;
	}

	public void setName(final String n) {
		this.name = n;
	}
	// }}
	// {{ Description
	private String description;

	@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
	// words, spaces and selected punctuation
	@MemberOrder(sequence = "2")
	@javax.jdo.annotations.Column(allowsNull="False")
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	// }}

	// {{ Password
	private String password;

	// how do we format it as password? - or maybe we should just hide it
	@Hidden
	@MemberOrder(sequence = "3")
	@javax.jdo.annotations.Column(allowsNull="False")
	public String getPassword() {
		return password;
	}

	public void setPassword(final String p) {
		this.password = p;
	}
	// }}

	// {{ Party
	private Party party;

	// words, spaces and selected punctuation
	@MemberOrder(sequence = "4")
	@javax.jdo.annotations.Column(allowsNull="False")
	public Party getParty() {
		return party;
	}

	public void setParty(final Party p) {
		this.party = p;
	}
	// }}

    // //////////////////////////////////////
    // Roles (collection), 
    // Add (action), Remove (action)
    // //////////////////////////////////////

    

    

    @javax.jdo.annotations.Persistent(table="UserRoles")
    @javax.jdo.annotations.Join(column="userId")
    @javax.jdo.annotations.Element(column="roleId")
    private List<Role> roles = new ArrayList<Role>();

    @Disabled
    @Render(Type.EAGERLY)
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(final List<Role> r) {
        this.roles = r;
    }

    @PublishedAction
    public User addRole(final Role role) {
        getRoles().add(role);
        return this;
    }
    @PublishedAction
    public User removeRole(final Role role) {
        getRoles().remove(role);
        return this;
    }
    // validate the provided argument prior to invoking action
    public String validateAddRole(final Role role) {
        if(getRoles().contains(role)) {
            return "Already a role for this user";
        }
        return null;
    }

    public String validateRemoveRole(final Role role) {
        if(!getRoles().contains(role)) {
            return "Not a role for this user";
        }
        return null;
    }
    // provide a drop-down
    public List<Role> choices0RemoveRole() {
        return getRoles();
    }
    

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


	// {{ delete (action) will not be needed.
	@Bulk
	@MemberOrder(sequence = "4")
	public List<User> delete() {	
		container.removeIfNotAlready(this);
		container.informUser("Deleted " + container.titleOf(this));
		// invalid to return 'this' (cannot render a deleted object)
		return userManager.allUsers();//we need to ensure that allTerminal obeys business rules 
	}
	// }}

	public static Filter<Role> thoseOwnedBy(final String currentUser) {
		return new Filter<Role>() {
			@Override
			public boolean accept(final Role role) {
				return Objects.equal(role.getOwnedBy(), currentUser);
			}

		};
	}

	// }}
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


	// {{ injected: DomainObjectContainer
	private DomainObjectContainer container;

	public void injectDomainObjectContainer(final DomainObjectContainer container) {
		this.container = container;
	}
	// }}

	// {{ injected: userManager
	private UserManager userManager;

	public void injectUserManager(final UserManager userManager) {
		this.userManager = userManager;
	}
	// }}


}
