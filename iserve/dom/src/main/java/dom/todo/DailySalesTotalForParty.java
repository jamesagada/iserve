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
    table = "DailySalesTotalForParty",
    extensions = {
        @Extension(vendorName = "datanucleus", key = "view-definition",
            value = "CREATE VIEW {DailySalesTotalForParty} " +
                    "( " +
                    "  {this.transactionDate}, " +
                    "  {this.totalAmount} ," +
                    "  {this.fromPartyReference}, " +
                    "  {this.transactiontype} " +
                    ") AS " +
                    "SELECT " +
                    "   {Transaction.transactionDate} , " +                   
                    "   SUM({Transaction.faceValue}) AS totalAmount ," +
                   // "   \"Transaction\".\"fromParty_Party_ID_OID\"  AS \"fromParty\", " + 
                    "   {Transaction.fromPartyReference}  , " +                     
					"   {Transaction.transactiontype} " +
                    "  FROM " + 
					"   {Transaction} " +
                    "GROUP BY " +
                    " {Transaction.transactionDate},  " +
					" {Transaction.fromPartyReference}, " +
					" {Transaction.transactiontype} " 
					+
                    "ORDER BY " +          
                    " {Transaction.transactionDate}, " +
					" {Transaction.fromPartyReference}, " +
					" {Transaction.transactiontype}"
					
					)
					
    })
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.Queries( {
	@javax.jdo.annotations.Query(
				name="DailySalesForParty", language="JDOQL",  
				value="SELECT FROM dom.todo.DailySalesTotalForParty ")
})
@Bookmarkable
@Immutable
public class DailySalesTotalForParty  {

        // //////////////////////////////////////

 
    private String transactiontype;
	  @javax.jdo.annotations.Column(allowsNull = "false")
	
    //@Optional
   // @Title(sequence = "1")
    public String getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(final String type) {
        this.transactiontype = type;
    }

    // //////////////////////////////////////
	
	private String fromPartyReference;
	  @javax.jdo.annotations.Column(allowsNull = "false")	
	//@Optional
	public String getFromPartyReference(){
		return this.fromPartyReference;
	}
	public void setFromPartyReference(String id ){
		this.fromPartyReference =id;
	}

	//////////////////////////////////////
    private LocalDate transactionDate;
    @javax.jdo.annotations.Column(allowsNull = "false")
    //@Title(sequence = "2", prepend = " - ")
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(final LocalDate dueDate) {
        this.transactionDate = dueDate;
    }

    // //////////////////////////////////////

    private BigDecimal totalAmount;
    @javax.jdo.annotations.Column(allowsNull = "false")
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(final BigDecimal total) {
        this.totalAmount = total;
    }

   
    //////////////////////////////////////////
  /*  public Party getParty(){
    	return partytypes.getPartyFromReference(fromPartyReference);
    }
    // //////////////////////////////////////
*/
   /* @Render(Type.EAGERLY)
    public List<Transaction> getTransactions() {
    	return transactions.findTransactionForPartyOnDate(getParty(), getTransactionDate());
    }
*/
    // //////////////////////////////////////
/*
    private Partytypes partytypes;

    final public void injectPartytypes(final Partytypes partytypes) {
        this.partytypes = partytypes;
    }

    private Transactions transactions;

    final public void injectTransactions(final Transactions transactions) {
        this.transactions = transactions;
    }
*/
}