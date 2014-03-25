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
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;

import org.joda.time.LocalDate;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.QueryDefault;

@Named("Inventory")
public class Inventory extends AbstractFactoryAndRepository {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Inventory.class);
	// {{ Id, iconName
	@Override
	public String getId() {
		return "Inventory";
	}

	public String iconName() {
		return "Inventory";
	}
	// }}


	// {{ newVoucherClass  (action)
	@MemberOrder(sequence = "1")
	@Named("New Voucher Class")
	public VoucherClass newVoucherClass(
			@Named("Description") 
			String description,
			@Named("Issuing Party")
			Party issuingParty, 
			@Named("Valid Starting From") 
			@Optional
			LocalDate validFrom,
			@Named("Valid Till") 
			@Optional
			LocalDate validTo,								  
			@Named(" Maximum Redeems Allowed") 
			@Optional
			int maxRedeems,
			@Named("Require Redeem")
			@Optional
			Boolean requireRedeem,
			@Named("Default Validity of Issued Voucher")
			int validity	
			) {
		final String ownedBy = currentUserName();
		return newVoucherClass(description, issuingParty, validFrom, validTo,maxRedeems, requireRedeem, validity,ownedBy);
	}

	// }}


	// {{ allVoucherClass (action)
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "2")
	@Named("All Voucher Classes")
	public List<VoucherClass> allVoucherClass() {
		return allVoucherClass(NotifyUserIfNone.YES);
	}

	public enum NotifyUserIfNone { YES, NO }

	@Programmatic
	public List<VoucherClass> allVoucherClass(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		@SuppressWarnings("deprecation")
		final List<VoucherClass> items = allMatches(VoucherClass.class, VoucherClass.thoseOwnedBy(currentUser));
		//Collections.sort(items);
		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No VoucherClass found.");
		}
		return items;
	}
	// }}

	// {{ newVoucherClass  (hidden)
	//(description, issuingParty, validFrom, validTo,maxRedeems, requireRedeem, validity,ownedBy);
	@Hidden // for use by fixtures
	public VoucherClass newVoucherClass(
			final String description, 
			final Party issuingParty, 
			final LocalDate validFrom,
			final LocalDate  validTo,
			final int maxRedeems,
			final Boolean requireRedeem,
			final int validity,
			final String userName)
	{
		final VoucherClass voucherClass = newTransientInstance(VoucherClass.class);
		voucherClass.setDescription(description);
		voucherClass.setOwnedBy(userName);
		voucherClass.setIssuingParty(issuingParty);
		voucherClass.setValidFrom(validFrom);
		voucherClass.setValidTo(validTo);
		voucherClass.setMaxRedeems(maxRedeems);
		voucherClass.setRequireRedeem(requireRedeem);
		voucherClass.setValidity(validity);
		persist(voucherClass);
		return voucherClass;
	}

	private static double random(double from, double to) {
		return Math.random() * (to-from) + from;
	}
	// }}




	// {{ autoComplete (hidden)
	@SuppressWarnings("deprecation")
	@Hidden
	public List<VoucherClass> autoListVoucherClass(final String description) {
		return allMatches(VoucherClass.class, new Filter<VoucherClass>() {
			@Override
			public boolean accept(final VoucherClass t) {
				return t.getDescription().contains(description);
			}

		});
	}
	// }}

	protected String currentUserName() {
		return getContainer().getUser().getName();
	}
	// }}

	// {{ newVoucherDenomination  (action)
	@MemberOrder(sequence = "3")
	@Named("New Voucher Denomination")
	public VoucherDenomination newVoucherDenomination(
			@Named("Description") 
			String description,
			@Named("Class of Voucher")
			VoucherClass voucherClass, 
			@Named("Valid Starting From") 
			@Optional
			LocalDate validFrom,
			@Named("Valid Till") 
			@Optional
			LocalDate validTo,								  
			@Named(" Face Value") 
			@Optional
			java.math.BigDecimal faceValue   ) {
		final String ownedBy = currentUserName();
		return newVoucherDenomination(description, voucherClass,validFrom,validTo, faceValue,ownedBy);
	}

	// }}


	// {{ allVoucherDenominations (action)
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "4")
	@Named("All Denominations")
	public List<VoucherDenomination> allVoucherDenominations() {
		return allVoucherDenominations(NotifyUserIfNone.YES);
	}


	@Programmatic
	public List<VoucherDenomination> allVoucherDenominations(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<VoucherDenomination> items = allMatches(VoucherDenomination.class, VoucherDenomination.thoseOwnedBy(currentUser));
		//Collections.sort(items);
		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No VoucherDenominations found.");
		}
		return items;
	}
	// }}

	// {{ newVoucherDenomination  (hidden)
	@Hidden // for use by fixtures
	public VoucherDenomination newVoucherDenomination(
			final String description, 
			final VoucherClass voucherClass,
			final LocalDate validFrom,
			final LocalDate validTo,
			final java.math.BigDecimal faceValue,
			final String userName) {
		final VoucherDenomination voucherDenomination = newTransientInstance(VoucherDenomination.class);
		voucherDenomination.setDescription(description);
		voucherDenomination.setOwnedBy(userName);
		voucherDenomination.setVoucherClass(voucherClass);
		voucherDenomination.setValidFrom(validFrom);
		voucherDenomination.setValidTo(validTo);
		voucherDenomination.setFaceValue(faceValue);
		persist(voucherDenomination);
		return voucherDenomination;
	}

	// }}




	// {{ autoCompleteVoucherDenomination (hidden)
	@SuppressWarnings("deprecation")
	@Hidden
	public List<VoucherDenomination> autoListVoucherDenomination(final String description) {
		return allMatches(VoucherDenomination.class, new Filter<VoucherDenomination>() {
			@Override
			public boolean accept(final VoucherDenomination t) {
				return denominationOwnedByCurrentUser(t) && t.getDescription().contains(description);
			}

		});
	}
	// }}

	// {{ helpers
	protected boolean denominationOwnedByCurrentUser(final VoucherDenomination t) {
		return Objects.equal(t.getOwnedBy(), currentUserName());
	}

	// }}


	@Hidden
	public List<VoucherDenomination> denominationsForVoucherClass(final VoucherClass voucherClass){
		return allMatches(VoucherDenomination.class , new Filter<VoucherDenomination>(){
			@Override
			public boolean accept(final VoucherDenomination t) {
				if (t.getVoucherClass()==voucherClass) {
					return true;

				} else 
				{
					return false;
				}
			}});
	}




	// {{ newTerminal  (action)
	@MemberOrder(sequence = "10")
	@Named("New Terminal")
	public Terminal newTerminal(
			@Named("Description") 
			String description,
			@Named("terminal Party")
			Party terminalParty, 
			@Named("Phone Number") 
			String phoneNumber,
			@Named("IP Address") 
			String ipAddress,					 
			@Named(" Terminal ID") 
			String terminalId	
			) {
		final String ownedBy = currentUserName();
		return newTerminal(description,terminalParty, phoneNumber, ipAddress,terminalId, ownedBy);
	}

	// }}


	// {{ allTerminals (action) .. this cannot be implemented this way cos there will be many terminals
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "2")
	@Named("All Terminals")
	public List<Terminal> allTerminals() {
		return allTerminals(NotifyUserIfNone.YES);
	}


	@Programmatic
	public List<Terminal> allTerminals(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<Terminal> items = allMatches(Terminal.class, Terminal.thoseOwnedBy(currentUser));
		//Collections.sort(items);
		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No Terminals found.");
		}
		return items;
	}
	// }}

	// {{ newTerminal  (hidden)
	//newTerminal(description,terminalParty, phoneNumber, ipAddress,terminalId, ownedBy);
	@Hidden // for use by fixtures
	public Terminal newTerminal(
			final String description, 
			final Party terminalParty, 
			final String  phoneNumber,
			final String ipAddress,
			final String terminalId,
			final String userName)
	{
		final Terminal terminal = newTransientInstance(Terminal.class);
		terminal.setDescription(description);
		terminal.setOwnedBy(userName);
		terminal.setTerminalParty(terminalParty);
		terminal.setPhoneNumber(phoneNumber);
		terminal.setIpAddress(ipAddress);
		terminal.setTerminalId(terminalId);
		persist(terminal);
		return terminal;
	}

	// }}




	// {{ autoCompleteTerminal (hidden)
	@Hidden
	public List<Terminal> autoTerminal(final String description) {
		return allMatches(Terminal.class, new Filter<Terminal>() {
			@Override
			public boolean accept(final Terminal t) {
				return t.getDescription().contains(description);
			}

		});
	}
	// }}

	// {{ newSku (action) Menu to create new sku
	@MemberOrder(sequence = "20")
	@Named("New Stock Keeping Unit (SKU)")
	public Sku newSku(
			@Named("Description") 
			String description,
			@Named("Parent Sku")
			@Optional
			Sku parentSku,
			@Named("Units Are In")
			@Optional
			Sku unitSku, 
			@Named("Quantity Of Units in Sku") 
			BigDecimal quantityInSku,
			@Named("Break Into Units When Qty less than Sku?") 
			Boolean breakIntoUnits					 	
			) {
		final String ownedBy = currentUserName();
		return newSku(description,parentSku,unitSku, quantityInSku, breakIntoUnits, ownedBy);
	}

	// }}


	// {{ allSkus (action) .. this cannot be implemented this way cos there will be many terminals
	@ActionSemantics(Of.SAFE)
	@MemberOrder(sequence = "2")
	@Named("All Skus")
	public List<Sku> allSkus() {
		return allSkus(NotifyUserIfNone.YES);
	}


	@Programmatic
	public List<Sku> allSkus(NotifyUserIfNone notifyUser) {
		final String currentUser = currentUserName();
		final List<Sku> items = allMatches(Sku.class, Sku.thoseOwnedBy(currentUser));
		//Collections.sort(items);
		if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
			getContainer().warnUser("No Skus found.");
		}
		return items;
	}
	// }}

	// {{ newSku  (hidden)
	// newSku(description,unitSku, quantityInSku, breakIntoUnits, ownedBy);
	@Hidden // for use by fixtures
	public Sku newSku(
			final String description, 
			final Sku parentSku, 
			final Sku unitSku,
			final BigDecimal  quantityInSku,
			final Boolean breakIntoUnits,
			final String userName)
	{
		final Sku sku = newTransientInstance(Sku.class);
		sku.setDescription(description);
		sku.setOwnedBy(userName);
		sku.setParentSku(parentSku);
		sku.setUnitSku(unitSku);
		sku.setQuantityInSku(quantityInSku);
		sku.setBreakIntoUnits(breakIntoUnits);
		persist(sku);
		return sku;
	}

	// }} 

	// {{ autoCompleteSku (hidden)
	@Hidden
	public List<Sku> autoSku(final String description) {
		return allMatches(Sku.class, new Filter<Sku>() {
			@Override
			public boolean accept(final Sku t) {
				return t.getDescription().contains(description);
			}

		});
	}
	// }}
	//{{UpdateInventoryFromTransaction(Transaction trans) - update inventory
	@Hidden
	@Programmatic
	public void UpdateInventoryFromTransaction(Transaction t){
		//given a transaction t, update the inventory records
		//The inventory records are the StockLedger records
		//we have to first find stockledger records for this record
		boolean updatedToPartyStock = false;
		boolean updatedFromPartyStock = false;
		LOG.info("Finding Stockledger to update.");
		List<StockLedger> stockLedgers = findStockLedgersForTransaction(t);
		//once we have the list then we cycle thru and update each one appropriately
		//this should be a transaction but how do we implement transactions
		//transaction begin
		LOG.info("Number of stockledgers already existing is "+stockLedgers.size());
		for (StockLedger s : stockLedgers) {
			//update this stockledger with the transaction
			LOG.info("Updating stockledger "+s.title());
			s.updateFromTransaction(t);
			//let us check whether this stockledger is for the toParty
			if (s.getStockingParty().samePartyAs(t.getToParty())) updatedToPartyStock=true;
			if (s.getStockingParty().samePartyAs(t.getFromParty())) updatedFromPartyStock=true;
		}
		//If there was no existing one then we need to create new ones
		//or if the existing ones do not represent all the required stockledgers
		// there is a stockledger for each voucherDenomination + stockingParty + voucherCode + voucherSerial
		// if there is no stock with the stocking party, transaction may fail if updating stock
		// so in such case we create a stockledger for only the receiving party
		LOG.info("Create new ledgers if none exist.");
		LOG.info("Number of stockledgers already existing is "+stockLedgers.size());
		if (stockLedgers.size() == 0 || updatedToPartyStock == false) {
			StockLedger s=newStockLedger(t.getToParty(),t.getSku(),
					t.getQuantity(),t.getSerialNumber(), t.getVoucherCode(),t.getVoucherDenomination()); 
					updatedToPartyStock=true;
					s.getJournal().add(t); //add the transaction to the journal
			LOG.info("Created new ledgers for receiving party.");		
		}
		//we should check again if we have at least one stockLedger for fromParty and one for toParty
	}
	//{{FindStockLedgerForTransaction - find the stockledgers to be updated for this transaction
	@Hidden
	@Programmatic
	public List<StockLedger> findStockLedgersForTransaction(final Transaction t) {
		return allMatches(StockLedger.class, new Filter<StockLedger>() {
			@Override
			public boolean accept(final StockLedger s) {
				return s.stockLedgerForTransaction(t);
			}
		});

	}
	//}}
	// {{ newStockLedger  (hidden)
	// newStockLedger(stockingParty, voucherDenomination, quantityInStock, serialNumber, voucherCode,sku);
	@Hidden // for use by fixtures
	public StockLedger newStockLedger(
			final Party stockingParty, 
			final Sku sku, 
			final BigDecimal  quantityInStock,
			final String serialNumber,
			final String voucherCode,
			final VoucherDenomination voucherDenomination
			)
	{
		final StockLedger stockLedger = newTransientInstance(StockLedger.class);
		stockLedger.setStockingParty(stockingParty);
		stockLedger.setVoucherDenomination(voucherDenomination);
		stockLedger.setSku(sku);
		stockLedger.setSerialNumber(serialNumber);
		stockLedger.setVoucherCode(voucherCode);
		stockLedger.setQuantityInStock(quantityInStock);
		persist(stockLedger);
		return stockLedger;
	}
	//convertSkuToSku
	@Hidden
	@Programmatic
	public BigDecimal convertSkuToSku(Sku toSku, Sku fromSku,VoucherDenomination voucherDenomination){
		//we need to build a graph of skus
		//start from the fromSku
		//traverse the 
		boolean multiplierFound = false;
		Sku frSku = fromSku;
		Sku tSku = toSku;
		LOG.info("Convert from " + frSku.getDescription() + " to " + tSku.getDescription());
		if (frSku.getParentSku() !=null){
			LOG.info( frSku.getDescription() + " parent Sku is " + frSku.getParentSku().getDescription());	
		}else {
			LOG.info( frSku.getDescription() + " has no parent sku");			
		}
		BigDecimal multiplier = new BigDecimal("1");
		//from child to parent
		if (frSku.getParentSku() != null) {
		while (( !frSku.getParentSku().equal(tSku))  && (frSku.getParentSku() != null) ){
			LOG.info( frSku.getDescription() + " parent Sku is " + frSku.getParentSku().getDescription());
			multiplier = frSku.getQuantityInSku().multiply(multiplier);
			frSku=frSku.getParentSku();	
		}
		multiplier = multiplier.multiply(frSku.getQuantityInSku());
		LOG.info("Multiplier found " + multiplier);
		multiplierFound=true;
		}
		//multiplier = multiplier*frSku.getQuantityInSku();
		if (frSku.getParentSku() == null && !multiplierFound) {
			// we need to substitute from with to and go again
			frSku = toSku;
			tSku = fromSku;
			multiplier = new BigDecimal("1");
			LOG.info("Convert from " + frSku.getDescription() + " to " + tSku.getDescription());
			if (frSku.getParentSku() !=null){
			while (( !frSku.getParentSku().equal(tSku)) && (frSku.getParentSku() != null))
			{
				multiplier = multiplier.divide(frSku.getQuantityInSku(),6,RoundingMode.CEILING);
				frSku=frSku.getParentSku();	
			}
			multiplier = multiplier.divide(frSku.getQuantityInSku(),6,RoundingMode.CEILING);
			//    multiplier=multiplier/frSku.getQuantityInSku();
			}
		}
		if ( frSku.getParentSku() == null) {
			multiplier = new BigDecimal("0");
		}
		LOG.info("Multiplier found " + multiplier);
		return multiplier;
	}
	//GenerateTicketForTransaction - generate a ticket for this transaction
	//the ticket is filled into the serialnumber and vouchercode fields. The numbers are totally random
	// we will of course have a find ticket function that will return transactions by serial number.
	@Hidden
	@Programmatic
	@Disabled
	public void generateTicketForTransaction(Transaction t) {
		//we generate two random numbers and fill it into the transaction. By using two random numbers, the repetition cycle will be longer 
		t.setVoucherCode(getRandom());
		t.setSerialNumber(getRandom());
		t.setValidTo(t.getTransactionDate().plusDays(
				t.getVoucherDenomination().getVoucherClass().getValidity()));
	}
	//GetRandom -- get a random number. No seed is provided or expected. We can use the XSRandom
	private String getRandom(){
		//generate the randomn number here
		java.util.Random rand = new XSRandom();
		int x=rand.nextInt(999999999);
		return String.format("%09d", x);
		//return String.valueOf(x);
	}
	//RedeemTicket - try to redeem a ticket
	//to redeem a ticket, we first need to make sure it has not been redeemed before
	//and then we redeem it

	@Hidden
	@Programmatic
	public void redeemTicket(Transaction t) {
		//we will need to set a status- ticketStatus to redeem if this is possible
		// first we need to find all the transaction records for this ticket
		// if any of them is redeem then we mark it as double redeem
		// otherwise we find the sales record and if 
		List<Transaction> tt = getTransactionsForTicket(t.getVoucherCode(),t.getSerialNumber());
		t.setTicketStatus("OPERATION_FAILED");
		if (tt.size() > 0 ) {
			//We found some records
			//usually this should not be more than two or three
			for (Transaction ticket:tt){
				if (ticket.getTicketStatus() != null ) {
					if (ticket.getTicketStatus().contains("REDEEM") ) //check for null
					{
					t.setTicketStatus("MULTI_REDEEM_ATTEMPT"); //actually we should be able to support multiple redeem
					}
				}
			}//
			//repeat again if the above did not set the ticketstatus, this time let us look for a sale
			if (t.getTicketStatus() != null ) {
			if (!t.getTicketStatus().contains("REDEEM")) {
				for (Transaction sale : tt ) {
					if (sale.getTransactiontype()==Transaction.TransactionType.SALE) {
						if (t.getValidTo().toDateMidnight().compareTo(t.getTransactionDate().toDateMidnight()) > 0){
							LOG.info("Ticket is still valid by date");
							t.setTicketStatus("REDEEM");
						}
						//there has been a sale before
						//now we need to be sure this ticket was sold to this particular customer otherwise there is some
						// issue of someone else using the ticket
						//in which case it cannot be general party
						if (sale.getFromParty().samePartyAs(t.getToParty())) {
							//ok valid ticket, valid person it was sold to
							//we need to also check the validity of the date
							if (!t.getTicketStatus().contains("REDEEM")) t.setTicketStatus("REDEEMED");
						}
					}
				} 
			}
		}
		}
	}
	@Programmatic
	@Hidden
	public List<Payment> getPaymentsForParty(Party p){
		return allMatches(
				new QueryDefault<Payment>(Payment.class, 
						"PaymentsForParty",  
						"party", p));
	
	}
	@Programmatic
	@Hidden
	public List<Transaction> findTransactionForPartyOnDate(Party p, java.util.Date d) {
		return allMatches(
				new QueryDefault<Transaction>(Transaction.class,
				"TransactionsFromPartyOnDate",
				"party",p ,
				"transactionDate",d));
	}
	@Hidden
	@Programmatic
	public List<Sku> getChildrenSku(Sku sku){

		return allMatches(
				new QueryDefault<Sku>(Sku.class, 
						"ChildrenSku", 
						"sku", sku));
	}
	//{{ getTransactionsForTicket - given a ticket, find the trancsations related to it
	@Hidden
	@Programmatic
	public List<Transaction> getTransactionsForTicket(String voucherCode, String serialNo){

		return allMatches(
				new QueryDefault<Transaction>(Transaction.class, 
						"TransactionsForTicket", 
						"voucherCode", voucherCode, 
						"serialNumber", serialNo));
	}
	//}}
	//{{ getStockLedgerForParty - given a party, return the StockLedgers for this party
	@Hidden
	@Programmatic
	public List<StockLedger> getStockLedgerForParty(Party p){

		return allMatches(
				new QueryDefault<StockLedger>(StockLedger.class, 
						"StockLedgerForParty",  
						"party", p));
	}
	//}}
	//{{ getTransactionsForParty - given a party, return the Transactions for this party
	@Hidden
	@Programmatic
	public List<Transaction> getTransactionsForParty(Party p){

		return allMatches(
				new QueryDefault<Transaction>(Transaction.class, 
						"TransactionsForParty",  
						"party", p));
	}
	//}}
	//{{ getStockLedgerForDenomination - given a denomination, return the StockLedgers for this denomination
	@Hidden
	@Programmatic
	public List<StockLedger> getStockLedgerForVoucherDenomination(VoucherDenomination d){

		return allMatches(
				new QueryDefault<StockLedger>(StockLedger.class, 
						"StockLedgerForVoucherDenomination",  
						"denomination", d));
	}
	//}}
	//{{ getStockLedgerForVoucherClass - given a voucherClass, return the StockLedgers for this voucherClass
	@Hidden
	@Programmatic
	public List<StockLedger> getStockLedgerForVoucherClass(VoucherClass v){

		return allMatches(
				new QueryDefault<StockLedger>(StockLedger.class, 
						"StockLedgerForVoucherClass",  
						"voucher", v));
	}
	//}}
	//{{ partyHasEnoughInventoryForTransaction
	@Hidden
	@Programmatic
	public boolean partyHasEnoughInventoryForTransaction(Party p,Transaction t){
		//return true if at this point there is enough inventory to satisfy the the transaction
		boolean hasInventory = false;
		LOG.info("Finding Stockledger to Check.");
		List<StockLedger> stockLedgers = findStockLedgersForTransaction(t);
		//once we have the list then we cycle thru and check if they are for the party
		for (StockLedger s : stockLedgers) {
			//let us check whether this stockledger is for the toParty
			LOG.info("Checking Stockledger" + s.title());
			LOG.info("Stocking Party Is " + s.getStockingParty().title() +
					"Issuing Party Is " + p.title());
			if (s.getStockingParty().samePartyAs(p)) {
				//check if we are doing the same Sku
				LOG.info("Stocking Party Is " + s.getStockingParty().title() +
						"Issuing Party Is " + p.title());
				if (s.getSku().equal(t.getSku())) {
					//res = bg1.compareTo(bg2)
					LOG.info("Checking for inventory.Sku is the same.");
					if (s.getQuantityInStock().compareTo(t.getQuantity()) > -1 ) hasInventory = true;
				} else {
					//convert sku
					//apply the condition again
					//TODO
					BigDecimal transactionStock = t.getQuantity().divide(convertSkuToSku(s.getSku(),t.getSku(),t.getVoucherDenomination()));
					LOG.info("Converted transaction stock is " + transactionStock);
					if ( s.getQuantityInStock().compareTo(transactionStock) > -1) hasInventory = true;
				}
			}
		}
		LOG.info("There is inventory for transaction is " + hasInventory);		
	return hasInventory;
	}
}
