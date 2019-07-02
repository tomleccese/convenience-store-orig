package com.bridgephase.store;

import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Map.Entry;

import com.bridgephase.store.interfaces.IInventory;

import static com.google.common.base.Preconditions.*;

/**
 * Models a Cash Register. One cash register instance can only have one
 * transaction underway at a time. There is no facility to save/suspend/restore
 * transactions.
 * 
 * TODO: Thread safety: This system only supports one cash register per
 * inventory. If an inventory will be shared between multiple cash registers
 * then updates to products in inventory will need to be synchronized.
 */
public class CashRegister {
	private Optional<Transaction> transaction = Optional.empty();
	private final IInventory inventory;
	private final TransactionPrinter receiptPrinter = new TransactionReceiptPrinter();

	public CashRegister(IInventory inventory) {
		super();
		this.inventory = inventory;
	}

	public void beginTransaction() {
		checkState(!transaction.isPresent() || transaction.get().isPaid(), "Transaction has already been started");
		transaction.ifPresent((transaction) -> {
			// TODO: we really should persist this paid transaction somewhere
		});
		transaction = Optional.of(new Transaction());
	}

	public boolean scan(final String upc) {
		checkState(transaction.isPresent(), "Transaction has not been started; start transaction before scanning products");
		checkArgument(upc != null, "The 'String upc' argument is required; it must not be null");
		final Product product = inventory.find(upc).orElse(null);
		if (product != null) {
			// add 1 of this product to the transaction
			return transaction.get().add(product, 1);
		} else {
			// product record with given upc is not in inventory
			return false;
		}
	}

	public BigDecimal getTotal() {
		checkState(transaction.isPresent(), "Transaction has not been started");
		return transaction.get().getTotal();
	}

	/**
	 * Pays the transaction and ends transaction.
	 * 
	 * @param amountPaid the amount tendered by customer
	 * @return the change to be returned to customer
	 * @throws IllegalStateException    if the transaction has not been started or
	 *                                  has already been paid
	 * @throws IllegalArgumentException if the amountPaid is null or less than the
	 *                                  total transaction amount
	 */
	public BigDecimal pay(BigDecimal amountPaid) {
		checkState(transaction.isPresent(),
				"Transaction has not been started; cannot pay for a transaction that has not been started");
		final Transaction transaction = this.transaction.get();
		final BigDecimal change = transaction.pay(amountPaid);
		// adjust inventory for each quantity of item sold
		for (Entry<String, TransactionLineItem> entry : transaction.getLineItems().entrySet()) {
      inventory.adjustQuantity(entry.getKey(), 0 - entry.getValue().getQuantity());
    }
		return change;
	}

	/**
	 * Prints the receipt to the given output stream
	 * 
	 * @param out the output stream
	 * @throws UncheckedIOException     if an IOException occurs while printing the
	 *                                  receipt
	 * @throws IllegalStateException    if the transaction has not been paid
	 * @throws IllegalArgumentException if the output stream is null
	 */
	public void printReceipt(OutputStream out) {
		receiptPrinter.print(this.transaction.get(), out);
	}
}
