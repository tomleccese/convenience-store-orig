package com.bridgephase.store;

import static com.bridgephase.store.TestUtils.bigdec;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
