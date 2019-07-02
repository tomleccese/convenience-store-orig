package com.bridgephase.store;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import com.bridgephase.store.InsufficientFundsException;
import com.bridgephase.store.Product;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.*;

import static com.bridgephase.store.TransactionState.*;
/**
 * A transaction for a cash register. Once a transaction is created by the
 * register, a cash register can {@link #add(Product, int) add} products to the
 * transaction, report the {@link #getTotal() total amount} of the transaction,
 * accept {@link #pay(BigDecimal) payment} of the transaction (i.e. ending the
 * transaction) and {@link #printReceipt(OutputStream) print a receipt} for the
 * paid transaction.
 */
public class Transaction {
  /**
   * The state of the transaction.
   * 
   * @see State
   */
  private TransactionState state = STARTED;

  /**
   * all the line items added to this transaction, keyed by upc
   */
  private final Map<String, TransactionLineItem> lineItems = new LinkedHashMap<>();

  /**
   * The count of products in transaction.
   */
  private int count;

  /**
   * The total amount of the transaction.
   */
  private BigDecimal total;

  /**
   * The amount paid.
   */
  private BigDecimal paid;

  /**
   * The change returned.
   */
  private BigDecimal change;

  /**
   * Add the given product and quantity to the transaction
   * 
   * @param product  the product
   * @param quantity the quantity
   * @return true if the product was added to the transaction
   * @throws IllegalStateException    if transaction has already been paid
   * @throws IllegalArgumentException if the product is null
   */
  boolean add(Product product, int quantity) {
    checkState(state != PAID, "Cannot add product to a paid transaction");
    checkArgument(product != null, "The 'Product product' argument is required; it must not be null");
    final TransactionLineItem lineItem = lineItems.merge(product.getUpc(),
      new TransactionLineItem(product.getName(), product.getRetailPrice(), quantity), TransactionLineItem::merge);
    // the spec says to return false if quantity scanned exceeds the quantity of
    // product in stock,
    // however I have decided to add it to the transaction regardless because this
    // is a convenience store
    // and people bring the products to the register to be scanned so regardless of
    // what the inventory is saying
    // if someone has an item in-hand then they will want to buy it and the seller
    // will want to sell it too.
    return (lineItem.getQuantity() > product.getQuantity()) ? false : true;
  }

  /**
   * Pays the transaction and ends transaction.
   * 
   * @param amountPaid the amount tendered by customer
   * @return the change to be returned to customer
   * @throws IllegalStateException      if the transaction has already been paid
   * @throws IllegalArgumentException   if the amountPaid is null or less than the
   *                                    total transaction amount
   * @throws InsufficientFundsException if the amountPaid is less than the total
   *                                    transaction amount
   */
  BigDecimal pay(BigDecimal amountPaid) {
    checkState(state != PAID, "Cannot pay for a transaction that has already been paid");
    checkArgument(amountPaid != null, "The 'BigDecimal amountPaid' argument is required; it must not be null");
    final BigDecimal total = getTotal();
    if (amountPaid.compareTo(total) < 0) {
      throw new InsufficientFundsException(String
        .format("The amount of %s is insufficient to cover the total transaction cost of %s", amountPaid, total));
    }
    this.total = total;
    this.count = getCount();
    this.paid = amountPaid;
    this.change = amountPaid.subtract(total);
    this.state = PAID;
    return this.change;
  }

  public boolean isPaid() {
    return state == PAID;
  }

  public Map<String, TransactionLineItem> getLineItems() {
		return ImmutableMap.copyOf(lineItems);
	}

	/**
   * @return the count for all products in this transaction
   */
  Integer getCount() {
    return state == PAID ? this.count
      : lineItems.values().stream().map(item -> item.getQuantity()).reduce((q1, q2) -> q1 + q2).orElse(Integer.valueOf(0));
  }

  /**
   * @return the total amount for all products in this transaction
   */
  BigDecimal getTotal() {
    return state == PAID ? this.total
      : lineItems.values().stream().map(item -> item.extendedPrice()).reduce((ep1, ep2) -> ep1.add(ep2))
        .orElse(BigDecimal.valueOf(0, 2));
  }

  /**
   * @return the amount paid
   */
  BigDecimal getPaid() {
    return state == PAID ? paid : BigDecimal.valueOf(0, 2);
  }

  /**
   * @return the change returned from payment (i.e. paid - total)
   */
  BigDecimal getChange() {
    return state == PAID ? change : BigDecimal.valueOf(0, 2);
  }
}