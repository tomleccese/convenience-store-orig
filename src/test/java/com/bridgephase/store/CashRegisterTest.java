package com.bridgephase.store;

import static com.bridgephase.store.TestUtils.bigdec;
import static java.lang.String.format;
import static org.easymock.EasyMock.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bridgephase.store.interfaces.IInventory;
import com.google.common.collect.ImmutableMap;

class CashRegisterTest {
  private CashRegister register;

  private IInventory inventory;
  private Object[] mocks;

  private Map<String, Product> products;

  @BeforeEach
  private void setup() {
    inventory = createMock(IInventory.class);
    products = ImmutableMap.of("A123", new Product("A123", "Apple", 0.50, 1.00, 100), "B234",
      new Product("B234", "Peach", 0.35, 0.75, 200), "C123", new Product("C123", "Milk", 2.15, 4.50, 40));
    mocks = new Object[] { inventory };
    register = new CashRegister(inventory);
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
    expect(inventory.find("P9889")).andReturn(Optional.ofNullable(products.get("P9889")));
    replay(mocks);
    assertEquals(false, register.scan("P9889"),
      "Expected register.scan to return false when scanning a upc that is not in inventory");
    assertEquals(bigdec(0.00), register.getTotal());
    verify(mocks);
  }

  @Test
  void testScanPaidAndReceiptPrinted() {
    register.beginTransaction();

    // verify that transaction total is 0.00
    assertEquals(bigdec(0.00), register.getTotal());

    expect(inventory.find("A123")).andReturn(Optional.ofNullable(products.get("A123")));
    expect(inventory.find("A123")).andReturn(Optional.ofNullable(products.get("A123")));
    expect(inventory.find("B234")).andReturn(Optional.ofNullable(products.get("B234")));
    expect(inventory.find("P9889")).andReturn(Optional.ofNullable(products.get("P9889")));
    expect(inventory.adjustQuantity("A123", -2)).andReturn(Optional.empty());
    expect(inventory.adjustQuantity("B234", -1)).andReturn(Optional.empty());
    replay(mocks);

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

    register.pay(bigdec(3));
    
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
    verify(mocks);
  }

  @Test
  void testPaidWithInsufficientFunds() {
    register.beginTransaction();

    // verify that transaction total is 0.00
    assertEquals(bigdec(0.00), register.getTotal());

    expect(inventory.find("A123")).andReturn(Optional.ofNullable(products.get("A123")));
    expect(inventory.find("A123")).andReturn(Optional.ofNullable(products.get("A123")));
    expect(inventory.find("B234")).andReturn(Optional.ofNullable(products.get("B234")));
    expect(inventory.find("P9889")).andReturn(Optional.ofNullable(products.get("P9889")));
    replay(mocks);
    
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
    verify(mocks);
  }

  @Test
  void testTwoTransactions() {
    register.beginTransaction();
    register.pay(bigdec(0));
    register.beginTransaction();
    register.pay(bigdec(0));
  }
}