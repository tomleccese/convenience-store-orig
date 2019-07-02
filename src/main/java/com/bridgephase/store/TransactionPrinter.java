package com.bridgephase.store;

import java.io.OutputStream;

@FunctionalInterface
interface TransactionPrinter {
	void print(Transaction transaction, OutputStream out);
}
