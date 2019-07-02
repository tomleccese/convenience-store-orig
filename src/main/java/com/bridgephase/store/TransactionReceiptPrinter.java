package com.bridgephase.store;

import static com.google.common.base.Preconditions.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.text.NumberFormat;
import java.util.Map.Entry;

class TransactionReceiptPrinter implements TransactionPrinter {

	@Override
	public void print(Transaction transaction, OutputStream out) {
		checkNotNull(transaction, "The 'Transaction transaction' argument is required; it must not be null");
		checkNotNull(transaction, "The 'OutputStream out' argument is required; it must not be null");
		checkState(transaction.isPaid(), "Cannot print a receipt for an unpaid transaction");
		final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		try {
			writer.append("BridgePhase Convenience Store");
			writer.newLine();
			writer.append("-----------------------------");
			writer.newLine();
			writer.append(String.format("Total Products Bought: %d", transaction.getCount()));
			writer.newLine();
			writer.newLine();
			for (Entry<String, TransactionLineItem> entry : transaction.getLineItems().entrySet()) {
				@SuppressWarnings("unused")
				String upc = entry.getKey();
				TransactionLineItem lineItem = entry.getValue();
				writer.append(String.format("%d %s @ %s: %s", lineItem.getQuantity(), lineItem.getName(),
						currencyFormat.format(lineItem.getPrice()), currencyFormat.format(lineItem.extendedPrice())));
				writer.newLine();
			}
			writer.append("-----------------------------");
			writer.newLine();
			writer.append(String.format("Total: %s", currencyFormat.format(transaction.getTotal())));
			writer.newLine();
			writer.append(String.format("Paid: %s", currencyFormat.format(transaction.getPaid())));
			writer.newLine();
			writer.append(String.format("Change: %s", currencyFormat.format(transaction.getChange())));
			writer.newLine();
			writer.append("-----------------------------");
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException("Error printing receipt", e);
		}
	}

}
