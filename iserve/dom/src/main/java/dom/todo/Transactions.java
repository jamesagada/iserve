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

import com.google.common.base.Objects;

import org.joda.time.*;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.QueryDefault;

@Named("Transactions")
public class Transactions extends AbstractFactoryAndRepository {

	// {{ Id, iconName
	@Override
	public String getId() {
		return "Transactions";
	}

	public String iconName() {
		return "Transactions";
	}
	// }}
	// {{ newPayment  (action)
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "2")
	@Named("New Payment")
	public Payment newPayment(
			// @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") // words, spaces and selected punctuation
			@Named("Transaction Date") LocalDate transactionDate,
			@Named("Reference Number") String referenceNumber,
			@Named("Description") String description, 
			@Named("Paying Party")Party fromParty,
			@Named("Receiving Party ") Party toParty,
			@Named("Amount") BigDecimal amount ){
		final String ownedBy = currentUserName();
		return newPayment( transactionDate, referenceNumber,description, fromParty, toParty,
				amount, ownedBy);
	}

	// }}
	//
	//
	// {{ newPayment  (hidden)
	@Hidden // for use by fixtures
	public Payment newPayment(
			final LocalDate transactionDate,
			final String referenceNumber,
			final String description,
			final Party fromParty,
			final Party toParty,
			final BigDecimal amount,
			final String ownedBy) {
		final Payment t = newTransientInstance(Payment.class);
		t.setDescription(description);
		t.setFromParty(fromParty);
		t.setToParty(toParty);
		t.setAmount(amount);
		t.setReferenceNumber(referenceNumber);
		t.setTransactionDate(transactionDate);
		t.setOwnedBy(ownedBy);
		persist(t);
		return t;
	}

	// {{ newTransaction  (action)
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "1")
	@Named("New Transaction")
	public Transaction newTransaction(
			// @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") // words, spaces and selected punctuation
			@Named("Description") String description,
			@Named("Type") Transaction.TransactionType transactiontype, 
	//		@Named("Issuing Party")Party fromParty,
			@Named("Receiving Party ") Party toParty,
			@Named("Denomination") VoucherDenomination voucherDenomination,
			@Named("Quantity ") BigDecimal quantity,
			@Named("In Units Of ") Sku sku,
			@Optional
			@Named("VoucherCode ") String voucherCode,
			@Optional
			@Named("Serial Number ") String serialNo,
			@Named("Transaction Date") LocalDate transactionDate,
			@Optional
			@Named("Ticket valid to") LocalDate validTo,
			@Optional
			@Named("Face Value") BigDecimal faceValue ){
		final String ownedBy = currentUserName();
		final Party fromParty = getPartyFromCurrentUser(ownedBy);
		return newTransaction(description, transactiontype, fromParty, toParty, voucherDenomination, 
				quantity, sku, voucherCode,serialNo, transactionDate,validTo, faceValue, ownedBy);
	}

	// }}
	// defaults
	//
	public String default0NewTransaction(){
		return "New Sale";
	}
	public Transaction.TransactionType default1NewTransaction(){
		return Transaction.TransactionType.SALE;
	}
	public Party default2NewTransaction(){
		return getPartyFromCurrentUser(currentUserName());
	}
	public Party default3NewTransaction(){
		return getDefaultToPartyForCurrentUser(currentUserName());
	}	
	
	public VoucherDenomination default4NewTransaction(){
		return getDefaultDenominationForCurrentUser(currentUserName());
	}
	
	public BigDecimal default5NewTransaction(){
		return new BigDecimal(1);
	}
	public Sku default6NewTransaction(){
		return getDefaultSkuForDenomination(currentUserName());
	}
	public String default7NewTransaction(){
		return 	getDefaultVoucherCode(currentUserName());
	}
	public String default8NewTransaction(){
		return 	getDefaultSerialNo(currentUserName());
	}

	public LocalDate default9NewTransaction(){
		return 	new LocalDate();
	}
	
	public BigDecimal default11NewTransaction(){
		return 	new BigDecimal(1);
	}
	
		
	//helpers
	private Party getPartyFromCurrentUser(String currentUser){
		// find the party that is attached to this current user. In reality we look through all the parties and find which one has the same user name
		List<User> users = allMatches(
				new QueryDefault(User.class,
						"UserForName",
						"name",currentUser));
		if (users != null ){
			//return the first one. so user name must be unique
			return users.get(0).getParty();
		} else {
		return null;
		}
	}
	private Party getDefaultToPartyForCurrentUser(String currentUser) {
		// find the person he normally transacts with
		return null;
	}
	private VoucherDenomination getDefaultDenominationForCurrentUser(String currentUser){
		//find the most common denominator he deals with
		return null;
	}
	private Sku getDefaultSkuForDenomination(String currentUser){
		//find the default sku
		return null;
	}
	private String getDefaultVoucherCode(String currentUser) {
		//find default vouchercode 
		return null;
	}
	private String getDefaultSerialNo(String currentUser){
		//default serial number
		return null;
	}
	//
	//
	// {{ newTransaction  (hidden)
	@Hidden // for use by fixtures
	public Transaction newTransaction(
			final String description,
			final Transaction.TransactionType transactiontype,
			final Party fromParty,
			final Party toParty,
			final VoucherDenomination voucherDenomination,
			final BigDecimal quantity,
			final Sku sku,
			final String voucherCode,
			final String serialNo,
			final LocalDate transactionDate,
			final LocalDate validTo,
			final BigDecimal faceValue,
			final String ownedBy) {
		final Transaction t = newTransientInstance(Transaction.class);
		t.setDescription(description);
		t.setTransactiontype(transactiontype);
		t.setFromParty(fromParty);
		t.setToParty(toParty);
		t.setVoucherDenomination(voucherDenomination);
		t.setQuantity(quantity);
		t.setSku(sku);
		t.setVoucherCode(voucherCode);
		t.setSerialNumber(serialNo);
		t.setTransactionDate(transactionDate);
		t.setValidTo(transactionDate.plusDays(voucherDenomination.getVoucherClass().getValidity()));
		t.setFaceValue(voucherDenomination.getFaceValue().multiply(
				sku.getUnitSkuQuantity().multiply(quantity)));
		t.setOwnedBy(ownedBy);
		persist(t);
		return t;
	}

	protected String currentUserName() {
		return getContainer().getUser().getName();
	}
	/**@Programmatic
	@Hidden
	public List<Transaction> findTransactionForPartyOnDate(Party p, LocalDate d) {
		return allMatches(
				new QueryDefault<Transaction>(Transaction.class,
				"TransactionsFromPartyOnDate",
				"party",p ,
				"transactionDate",d));
	}**/
	/**
	@Programmatic
	@Hidden
	public List<DailySalesTotalForParty> findDailySalesTotalForParty(Party p) {
		return allMatches(
				new QueryDefault<DailySalesTotalForParty>(DailySalesTotalForParty.class,
				"DailySalesTotalForParty",
				"party",p ));
	}
	 **/
}
