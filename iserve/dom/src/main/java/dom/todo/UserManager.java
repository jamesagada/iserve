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

import com.google.common.base.Predicates;
import com.google.common.base.Objects;

import org.joda.time.LocalDate;
import org.apache.isis.applib.AbstractFactoryAndRepository;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.filter.Filter;
import services.ClockService;

@Named("UserManagement")
public class UserManager extends AbstractFactoryAndRepository {

    // //////////////////////////////////////
    // Identification in the UI
    // //////////////////////////////////////

    public String getId() {
        return "userManager";
    }

    public String iconName() {
        return "userManager";
    }

     // //////////////////////////////////////
    // NewPermission (action)
    // //////////////////////////////////////
    @Named("New Permission")
    @MemberOrder(sequence= "1" )
    public Permission newPermission(
    	@Named("Name")
    	String name,
    	@Named("Description")
    	String description,
    	@Named("Permission Details")
    	String permission){
    	final String ownedBy=getContainer().getUser().getName();
    	return newPermission(name,description,permission,ownedBy);
    	}
    // //////////////////////////////////////
    // NewPermission (helper)
    // //////////////////////////////////////  	
    @Programmatic
    @Hidden
    public Permission newPermission(final String name, final String description,
    		final String permission, final String ownedBy){
    	final Permission p = newTransientInstance(Permission.class);
    	p.setName(name);
    	p.setDescription(description);
    	p.setPermission(permission);
    	p.setOwnedBy(ownedBy);
    	persist(p);
    	return p;
    }	
    // //////////////////////////////////////
    // NewRole (action)
    // //////////////////////////////////////
    @Named("New Role")
    @MemberOrder(sequence="2")
    public Role newRole(
    	@Named("Name")
    	String name,
    	@Named("Description")
    	String description){
    	final String ownedBy=getContainer().getUser().getName();
    	return newRole(name,description,ownedBy);
    }	
    // //////////////////////////////////////
    // NewRole (helper)
    // //////////////////////////////////////  
    @Programmatic
    @Hidden
    public Role newRole(
    	final String name,
    	final String description,
    	final String ownedBy){
    	final Role r = newTransientInstance(Role.class);
    	r.setName(name);
    	r.setDescription(description);
    	r.setOwnedBy(ownedBy);
    	persist(r);
    	return r;
    }	  
    // //////////////////////////////////////
    // NewUser (action)
    // //////////////////////////////////////
    @Named("New User")
    @MemberOrder(sequence="3")
    public User newUser(
    	@Named("Name")
    	String name,
    	@Named("Description")
    	String description,
    	@Named("Password")
    	String password,
    	@Named("Affiliated To")
    	Party party){
    	final String ownedBy=getContainer().getUser().getName();
    	return newUser(name,description,password,party,ownedBy);
    }	
    // //////////////////////////////////////
    // NewUser (helper)
    // //////////////////////////////////////
    @Programmatic
    @Hidden
    public User newUser(
    	final String name,
    	final String description,
    	final String password,
    	final Party party,
    	final String ownedBy){
    	final User u = newTransientInstance(User.class);
    	u.setName(name);
    	u.setDescription(description);
    	u.setPassword(getPasswordHash(password));
    	u.setParty(party);
    	u.setOwnedBy(ownedBy);
    	persist(u);
    	return u;
	}
    // //////////////////////////////////////
    // AllPermissions (action)
    // //////////////////////////////////////
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "4")
	@Named("All Permissions")
	public List<Permission> allPermissions() {
		return allPermissions(NotifyUserIfNone.YES);
	}
	//
	@Programmatic
	public List<Permission> allPermissions(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<Permission> items = allMatches(
				new QueryDefault<Permission>(Permission.class, 
						"AllPermissions", 
						"ownedBy", currentUserName()));

		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No Permissions found.");
		}
		return items;
	}
	//    
	/////////////////////////////////////////
		public enum NotifyUserIfNone { YES, NO };
    // //////////////////////////////////////
    // AllRoles (action)
    // //////////////////////////////////////   
    @ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "4")
	@Named("All Roles")
	public List<Role> allRoles() {
		return allRoles(NotifyUserIfNone.YES);
	}
	//
	@Programmatic
	public List<Role> allRoles(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<Role> items = allMatches(
				new QueryDefault<Role>(Role.class, 
						"AllRoles", 
						"ownedBy", currentUserName()));

		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No Roles found.");
		}
		return items;
	}
     // //////////////////////////////////////
    // AllUsers (action)
    // //////////////////////////////////////   
    @ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "5")
	@Named("All Users")
	public List<User> allUsers() {
		return allUsers(NotifyUserIfNone.YES);
	}


	@Programmatic
	public List<User> allUsers(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<User> items = allMatches(
				new QueryDefault<User>(User.class, 
						"AllUsers", 
						"ownedBy", currentUserName()));

		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No Users found.");
		}
		return items;
	}
    ////////////////////////////////////////
    // autoPermission - autocomplete for permissions
    ///////////////////////////////////////
    @Hidden
    public List<Permission> autoPermission(final String name) {
		return allMatches(Permission.class, new Filter<Permission>() {
			@Override
			public boolean accept(final Permission p) {
				return p.getName().contains(name);
			}

		});
	} 
    //////////////////////////////////////
    ////////////////////////////////////////
    // autoRole - autocomplete for roles
    ///////////////////////////////////////
    @Hidden
    public List<Role> autoRole(final String name) {
		return allMatches(Role.class, new Filter<Role>() {
			@Override
			public boolean accept(final Role p) {
				return p.getName().contains(name);
			}

		});
	} 
    //////////////////////////////////////
    ////////////////////////////////////////
    // autoUser - autocomplete for Users
    ///////////////////////////////////////
    @Hidden
    public List<User> autoUser(final String name) {
		return allMatches(User.class, new Filter<User>() {
			@Override
			public boolean accept(final User p) {
				return p.getName().contains(name);
			}

		});
	} 
    /////////////////////////////////////////////////////
    // getPasswordHash - hash of the password to be saved.
    // //////////////////////////////////////////////////
    private String getPasswordHash(String password){
    //use the securityutils of shiro
    return password;
    }
    /////////////////////////////////////
    private String currentUserName() {
        return container.getUser().getName();
    }

    
    // //////////////////////////////////////
    // Injected Services
    // //////////////////////////////////////

    
    private DomainObjectContainer container;

    public void injectDomainObjectContainer(final DomainObjectContainer container) {
        this.container = container;
    }

    private ClockService clockService;
    public void injectClockService(ClockService clockService) {
        this.clockService = clockService;
    }


}
