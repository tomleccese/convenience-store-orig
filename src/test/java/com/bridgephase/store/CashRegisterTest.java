package com.bridgephase.store;

import static com.bridgephase.store.TestUtils.bigdec;
import static java.lang.String.format;
import static org.easymock.EasyMock.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bridgephase.store.interfaces.IInventory;
import com.google.common.collect.ImmutableList;

class CashRegisterTest {
  private CashRegister register;

  private IInventory inventory;
  private Object[] mocks;

  private List<Product> products;

  @BeforeEach
  private void setup() {
    inventory = createMock(IInventory.class);
    products = ImmutableList.of(new Product("A123", "Apple", 0.50, 1.00, 100),
      new Product("B234", "Peach", 0.35, 0.75, 200), new Product("C123", "Milk", 2.15, 4.50, 40));
    mocks = new Object[] { inventory };
    expect(inventory.list()).andReturn(products);
    replay(mocks);
    register = new CashRegister(inventory);
    verify(mocks);
  }

  @Test
  void testBeginTransaction() {
    // expect no problems starting transaction in register without a current
    // transaction
    register.beginTransaction();
  }

  @Test
  void testBeginTransactionAlreadyStarted() {
    register.beginTransaction();
    // expect an IllegalStateException when attempting to start a transaction when
    // register has a current transaction
    assertThrows(IllegalStateException.class, () -> register.beginTransaction());
  }

  @Test
  void testGetTotalTransactionNotStarted() {
    // transaction not started -> IllegalStateException
    assertThrows(IllegalStateException.class, () -> register.getTotal());
  }

  @Test
  void testGetTotalTransactionEmptyTransaction() {
    register.beginTransaction();
    assertEquals(bigdec(0.00), register.getTotal());
  }

  @Test
  void testScanTransactionNotStarted() {
    // transaction not started -> IllegalStateException
    assertThrows(IllegalStateException.class, () -> register.scan("P9889"));
  }

  @Test
  void testScanProductNull() {
    register.beginTransaction();
    assertThrows(IllegalArgumentException.class, () -> register.scan(null));
  }

  @Test
  void testScanProductNotInInventory() {
    register.beginTransaction();
    assertEquals(false, register.scan("P9889"),
      "Expected register.scan to return false when scanning a upc that is not in inventory");
    assertEquals(bigdec(0.00), register.getTotal());
  }

  @Test
  void testScanPaidAndReceiptPrinted() {
    register.beginTransaction();

    // verify that transaction total is 0.00
    assertEquals(bigdec(0.00), register.getTotal());

    // add one A123 product
    assertEquals(true, register.scan("A123"),
      "Expected register.scan to return true when scanning a upc that is in inventory");
    assertEquals(bigdec(1.00), register.getTotal());

    // add another A123 product
    assertEquals(true, register.scan("A123"),
      "Expected register.scan to return true when scanning a upc that is in inventory");
    assertEquals(bigdec(2.00), register.getTotal());

    // add B234 product
    assertEquals(true, register.scan("B234"),
      "Expected register.scan to return true when scanning a upc that is in inventory");
    assertEquals(bigdec(2.75), register.getTotal());

    // add P9889 product
    assertEquals(false, register.scan("P9889"),
      "Expected register.scan to return false when scanning a upc that is not in inventory");
    // and verify that the total has not changed
    assertEquals(bigdec(2.75), register.getTotal());

    assertEquals(bigdec(0.25), register.pay(bigdec(3.00)));
    assertEquals(bigdec(2.75), register.getTotal());

    final String expectedReceipt;
    {
      final NumberFormat currency = NumberFormat.getCurrencyInstance();
      // @formatter:off
      StringBuilder b = new StringBuilder();
      b.append(format("BridgePhase Convenience Store%n"));
      b.append(format("-----------------------------%n"));
      b.append(format("Total Products Bought: 3%n"));
      b.append(format("%n"));
      b.append(format("2 Apple @ %s: %s%n", currency.format(1), currency.format(2)));
      b.append(format("1 Peach @ %s: %s%n", currency.format(.75), currency.format(.75)));
      b.append(format("-----------------------------%n"));
      b.append(format("Total: %s%n", currency.format(2.75)));
      b.append(format("Paid: %s%n", currency.format(3)));
      b.append(format("Change: %s%n", currency.format(.25)));
      b.append(format("-----------------------------%n"));
      // @formatter:on
      expectedReceipt = b.toString();
    }
    final String actualReceipt;
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      register.printReceipt(out);
      actualReceipt = out.toString();
    }
    assertEquals(expectedReceipt, actualReceipt);

  }

  @Test
  void testPaidWithInsufficientFunds() {
    register.beginTransaction();

    // verify that transaction total is 0.00
    assertEquals(bigdec(0.00), register.getTotal());

    // add one A123 product
    assertEquals(true, register.scan("A123"),
      "Expected register.scan to return true when scanning a upc that is in inventory");
    assertEquals(bigdec(1.00), register.getTotal());

    // add another A123 product
    assertEquals(true, register.scan("A123"),
      "Expected register.scan to return true when scanning a upc that is in inventory");
    assertEquals(bigdec(2.00), register.getTotal());

    // add B234 product
    assertEquals(true, register.scan("B234"),
      "Expected register.scan to return true when scanning a upc that is in inventory");
    assertEquals(bigdec(2.75), register.getTotal());

    // add P9889 product
    assertEquals(false, register.scan("P9889"),
      "Expected register.scan to return false when scanning a upc that is not in inventory");
    // and verify that the total has not changed
    assertEquals(bigdec(2.75), register.getTotal());

    assertThrows(InsufficientFundsException.class, () -> register.pay(bigdec(2.00)));
  }

  @Test
  void testTwoTransactions() {
    register.beginTransaction();
    register.pay(bigdec(0));
    register.beginTransaction();
    register.pay(bigdec(0));
  }
}