package com.bridgephase.store;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.bridgephase.store.CashRegister.Transaction.LineItem;

class CashRegisterTransactionLineItemTest {
  @Test
  void testLineItemExtendedPrice() {
    LineItem lineItem = new LineItem("a", new BigDecimal("1.33"), 4);
    assertEquals(new BigDecimal("5.32"), lineItem.extendedPrice());
  }

  @Test
  void testLineItemNullName() {
    assertThrows(NullPointerException.class, () -> new LineItem(null, new BigDecimal("1.33"), 4));
  }

  @Test
  void testLineItemNullPrice() {
    assertThrows(NullPointerException.class, () -> new LineItem("a", null, 4));
  }

  @Test
  void testLineItemNullQuantity() {
    assertThrows(NullPointerException.class, () -> new LineItem("a", new BigDecimal("1.33"), null));
  }

}
