package com.bridgephase.store;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bridgephase.store.interfaces.IInventory;
import com.google.common.base.Preconditions;

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
  /**
   * A transaction for a cash register. Once a transaction is created by the
   * register, a cash register can {@link #add(Product, int) add} products to the
   * transaction, report the {@link #getTotal() total amount} of the transaction,
   * accept {@link #pay(BigDecimal) payment} of the transaction (i.e. ending the
   * transaction) and {@link #printReceipt(OutputStream) print a receipt} for the
   * paid transaction.
   */
  static class Transaction {
    static enum State {
      STARTED, PAID
    }

    static class LineItem {
      private final String name;
      private final BigDecimal price;
      private final Integer quantity;

      LineItem(String name, BigDecimal price, Integer quantity) {
        super();
        this.name = Objects.requireNonNull(name, "name");
        this.price = Objects.requireNonNull(price, "price");
        this.quantity = Objects.requireNonNull(quantity, "quantity");
      }

      BigDecimal extendedPrice() {
        return price.multiply(BigDecimal.valueOf(this.quantity));
      }
    }

    /**
     * The state of the transaction.
     * 
     * @see State
     */
    private State state = State.STARTED;

    /**
     * all the line items added to this transaction, keyed by upc
     */
    private final Map<String, LineItem> lineItems = new LinkedHashMap<>();

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
      Preconditions.checkState(state != State.PAID, "Cannot add product to a paid transaction");
      Preconditions.checkArgument(product != null, "The 'Product product' argument is required; it must not be null");
      final LineItem item = lineItems.get(product.getUpc());
      if (item == null) {
        lineItems.put(product.getUpc(), new LineItem(product.getName(), product.getRetailPrice(), quantity));
      } else {
        // update the the quantity of the product in this transaction
        lineItems.put(product.getUpc(),
          new LineItem(product.getName(), product.getRetailPrice(), item.quantity + quantity));
      }
      return true;
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
      Preconditions.checkState(state != State.PAID, "Cannot pay for a transaction that has already been paid");
      Preconditions.checkArgument(amountPaid != null,
        "The 'BigDecimal amountPaid' argument is required; it must not be null");
      final BigDecimal total = getTotal();
      if (amountPaid.compareTo(total) < 0) {
        throw new InsufficientFundsException(String
          .format("The amount of %s is insufficient to cover the total transaction cost of %s", amountPaid, total));
      }
      this.total = total;
      this.count = getCount();
      this.paid = amountPaid;
      this.change = amountPaid.subtract(total);
      this.state = State.PAID;
      return this.change;
    }

    /**
     * Prints the receipt to the given output stream
     * 
     * @param out the output stream
     * @throws UncheckedIOException     if an IOException occurs while printing the
     *                                  receipt
     * @throws IllegalStateException    if the transaction has not been been paid
     * @throws IllegalArgumentException if the output stream is null
     */
    void printReceipt(OutputStream out) {
      Preconditions.checkState(state == State.PAID, "Cannot print a receipt for an unpaid transaction");
      Preconditions.checkArgument(out != null, "The 'OutputStream out' argument is required; it must not be null");
      final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
      final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
      try {
        writer.append("BridgePhase Convenience Store");
        writer.newLine();
        writer.append("-----------------------------");
        writer.newLine();
        writer.append(String.format("Total Products Bought: %d", count));
        writer.newLine();
        writer.newLine();
        for (Entry<String, LineItem> entry : this.lineItems.entrySet()) {
          @SuppressWarnings("unused")
          String upc = entry.getKey();
          LineItem lineItem = entry.getValue();
          writer.append(String.format("%d %s @ %s: %s", lineItem.quantity, lineItem.name, currencyFormat.format(lineItem.price),
            currencyFormat.format(lineItem.extendedPrice())));
          writer.newLine();
        }
        writer.append("-----------------------------");
        writer.newLine();
        writer.append(String.format("Total: %s", currencyFormat.format(this.total)));
        writer.newLine();
        writer.append(String.format("Paid: %s", currencyFormat.format(this.paid)));
        writer.newLine();
        writer.append(String.format("Change: %s", currencyFormat.format(this.change)));
        writer.newLine();
        writer.append("-----------------------------");
        writer.newLine();
        writer.flush();
      } catch (IOException e) {
        throw new UncheckedIOException("Error printing receipt", e);
      }
    }

    public boolean isPaid() {
      return state == State.PAID;
    }

    /**
     * @return the count for all products in this transaction
     */
    Integer getCount() {
      return state == State.PAID ? this.count
        : lineItems.values().stream().map(item -> item.quantity).reduce((q1, q2) -> q1 + q2).orElse(Integer.valueOf(0));
    }

    /**
     * @return the total amount for all products in this transaction
     */
    BigDecimal getTotal() {
      return state == State.PAID ? this.total
        : lineItems.values().stream().map(item -> item.extendedPrice()).reduce((ep1, ep2) -> ep1.add(ep2))
          .orElse(BigDecimal.valueOf(0, 2));
    }

    BigDecimal getPaid() {
      return state == State.PAID ? paid : BigDecimal.valueOf(0, 2);
    }

    BigDecimal getChange() {
      return state == State.PAID ? change : BigDecimal.valueOf(0, 2);
    }

  }

  private final Map<String, Product> products;

  private Optional<Transaction> transaction = Optional.empty();

  public CashRegister(IInventory inventory) {
    super();
    this.products = inventory.list().stream().collect(Collectors.toMap(Product::getUpc, p -> p));
  }

  public void beginTransaction() {
    Preconditions.checkState(transaction.isEmpty() || transaction.get().isPaid(), "Transaction has already been started");
    transaction = Optional.of(new Transaction());
  }

  public boolean scan(final String upc) {
    Preconditions.checkState(!transaction.isEmpty(),
      "Transaction has not been started; start transaction before scanning products");
    Preconditions.checkArgument(upc != null, "The 'String upc' argument is required; it must not be null");
    final Product product = products.get(upc);
    if (product != null) {
      // add 1 of this product to the transaction
      return transaction.get().add(product, 1);
    } else {
      // product record with given upc is not in inventory
      return false;
    }
  }

  public BigDecimal getTotal() {
    Preconditions.checkState(!transaction.isEmpty(), "Transaction has not been started");
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
    Preconditions.checkState(!transaction.isEmpty(),
      "Transaction has not been started; cannot pay for a transaction that has not been started");
    return transaction.get().pay(amountPaid);
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
    transaction.get().printReceipt(out);
  }
}
