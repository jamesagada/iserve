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

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Title;



@javax.jdo.annotations.PersistenceCapable(
    identityType = IdentityType.NONDURABLE,
    table = "PartyDatabaseRecord",
    extensions = {
        @Extension(vendorName = "datanucleus", key = "view-definition-mysql",
            value = "CREATE VIEW {PartyDatabaseRecord} " +
                    "( " +
                    //"  {this.party_id}, " +
                    "  {this.phoneNumber} ," +
                    "  {this.description} , " +
                    "  {this.transportCard} " +
                    ") AS " +
                    "SELECT " +
                   // "   \"Party\".\"PARTY_ID\" AS party_id  , " +                   
                    "   {Party.phoneNumber} ," + 
                    "   {Party.description} ," +                     
					"   {Party.transportCard} " +
                    "  FROM " + 
					"   {Party} " )
    })
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.Queries( {
	@javax.jdo.annotations.Query(
				name="PartyRecordFromId", language="JDOQL",  
				value="SELECT FROM dom.todo.PartyDatabaseRecord WHERE party_id == :Id "),
	@javax.jdo.annotations.Query(
			name="PartyRecordFromDescription", language="JDOQL",  
			value="SELECT FROM dom.todo.PartyDatabaseRecord WHERE description == :desc ")
			})
@Bookmarkable
@Immutable
public class PartyDatabaseRecord  {


    // //////////////////////////////////////
/*	
	private Long party_id;
	
	@Optional
	public Long getParty_id(){
		return this.party_id;
	}
	public void setParty_id(final Long id ){
		this.party_id =id;
	}
*/
	//////////////////////////////////////
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String desc) {
        this.description = desc;
    }

    // //////////////////////////////////////

    private String transportCard;

    public String getTransportCard() {
        return transportCard;
    }

    public void setTransportCard(final String tc) {
        this.transportCard=tc;
    }
    // //////////////////////////////////////

    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String tc) {
        this.phoneNumber=tc;
    }

  
}