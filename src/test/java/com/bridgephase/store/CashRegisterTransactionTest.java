package com.bridgephase.store;

import static java.lang.String.format;

import static com.bridgephase.store.TestUtils.bigdec;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bridgephase.store.CashRegister.Transaction;

class CashRegisterTransactionTest {
  private Transaction transaction;

  @BeforeEach
  private void setup() {
    transaction = new Transaction();
  }

  @Test
  void testTransactionStartedState() {
    assertEquals(false, transaction.isPaid());
  }

  @Test
  void testAddProductNull() {
    assertEquals(false, transaction.isPaid());
    assertThrows(IllegalArgumentException.class, () -> transaction.add(null, 1));
  }

  @Test
  void testGetBeforePaid() {
    assertEquals(false, transaction.isPaid());
    assertEquals(bigdec(0.00), transaction.getTotal());
    assertEquals(bigdec(0.00), transaction.getPaid());
    assertEquals(bigdec(0.00), transaction.getChange());
    assertEquals(0, transaction.getCount());
    transaction.add(new Product("A123", "Apple", 0.50, 1.00, 100), 1);
    assertEquals(bigdec(1.00), transaction.getTotal());
    assertEquals(bigdec(0.00), transaction.getPaid());
    assertEquals(bigdec(0.00), transaction.getChange());
    assertEquals(1, transaction.getCount());
  }

  @Test
  void testAddProductAfterPaid() {
    assertEquals(false, transaction.isPaid());
    transaction.pay(bigdec(0.00));
    assertThrows(IllegalStateException.class, () -> transaction.add(new Product("A123", "Apple", 0.50, 1.00, 100), 1));
  }

  @Test
  void testTransactionPaidZero() {
    assertEquals(false, transaction.isPaid());
    transaction.pay(bigdec(0.00));
    assertEquals(bigdec(0.00), transaction.getTotal());
    assertEquals(bigdec(0.00), transaction.getPaid());
    assertEquals(bigdec(0.00), transaction.getChange());
    assertEquals(true, transaction.isPaid());
    assertEquals(0, transaction.getCount());
  }

  @Test
  void testTransactionPaidWithPrintedReceipt() {
    assertEquals(false, transaction.isPaid());
    // verify that transaction total is 0.00
    assertEquals(bigdec(0.00), transaction.getTotal());

    // add one A123 product
    assertEquals(true, transaction.add(new Product("A123", "Apple", 0.50, 1.00, 100), 1));
    assertEquals(bigdec(1.00), transaction.getTotal());

    // add 2 more A123 product
    assertEquals(true, transaction.add(new Product("A123", "Apple", 0.50, 1.00, 100), 2));
    assertEquals(bigdec(3.00), transaction.getTotal());

    // add B234 product
    assertEquals(true, transaction.add(new Product("B234", "Peach", 0.35, 0.75, 200), 4));
    assertEquals(bigdec(6.00), transaction.getTotal());

    // add P9889 product
    assertEquals(true, transaction.add(new Product("P9889", "Avocado", 0.75, 1.35, 60), 2));
    assertEquals(bigdec(8.70), transaction.getTotal());

    assertEquals(bigdec(1.30), transaction.pay(bigdec(10.00)));
    assertEquals(true, transaction.isPaid());
    assertEquals(bigdec(8.70), transaction.getTotal());
    assertEquals(bigdec(10.00), transaction.getPaid());
    assertEquals(bigdec(1.30), transaction.getChange());
    assertEquals(9, transaction.getCount());

    final String expectedReceipt;
    {
      final NumberFormat currency = NumberFormat.getCurrencyInstance();
      // @formatter:off
      StringBuilder b = new StringBuilder();
      b.append(format("BridgePhase Convenience Store%n"));
      b.append(format("-----------------------------%n"));
      b.append(format("Total Products Bought: 9%n"));
      b.append(format("%n"));
      b.append(format("3 Apple @ %s: %s%n", currency.format(1), currency.format(3)));
      b.append(format("4 Peach @ %s: %s%n", currency.format(.75), currency.format(3)));
      b.append(format("2 Avocado @ %s: %s%n", currency.format(1.35), currency.format(2.7)));
      b.append(format("-----------------------------%n"));
      b.append(format("Total: %s%n", currency.format(8.7)));
      b.append(format("Paid: %s%n", currency.format(10)));
      b.append(format("Change: %s%n", currency.format(1.3)));
      b.append(format("-----------------------------%n"));
      // @formatter:on
      expectedReceipt = b.toString();
    }
    final String actualReceipt;
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      transaction.printReceipt(out);
      actualReceipt = out.toString();
    }
    assertEquals(expectedReceipt, actualReceipt);
  }

}
