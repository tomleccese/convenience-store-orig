package com.bridgephase.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class ProductTest {

  @Test
  public void testConstructor() {
    final String upc = "1263419857";
    final String name = "banana";
    final BigDecimal wholesalePrice = BigDecimal.valueOf(0.05);
    final BigDecimal retailPrice = BigDecimal.valueOf(0.25);
    final Integer quantity = 7;

    Product prod = new Product(upc, name, wholesalePrice, retailPrice, quantity);

    assertEquals(upc, prod.getUpc());
    assertEquals(name, prod.getName());
    assertEquals(wholesalePrice, prod.getWholesalePrice());
    assertEquals(retailPrice, prod.getRetailPrice());
    assertEquals(quantity, prod.getQuantity());
  }

  @Test
  public void testConstructorNullUpc() {
    final String upc = null;
    final String name = "banana";
    final BigDecimal wholesalePrice = BigDecimal.valueOf(0.05);
    final BigDecimal retailPrice = BigDecimal.valueOf(0.25);
    final Integer quantity = 7;

    assertThrows(NullPointerException.class, () -> new Product(upc, name, wholesalePrice, retailPrice, quantity), "Expected a NullPointerException to be thrown when construction a Product with a null upc");
  }

  @Test
  public void testConstructorNullName() {
    final String upc = "1263419857";
    final String name = null;
    final BigDecimal wholesalePrice = BigDecimal.valueOf(0.05);
    final BigDecimal retailPrice = BigDecimal.valueOf(0.25);
    final Integer quantity = 7;

    assertThrows(NullPointerException.class, () -> new Product(upc, name, wholesalePrice, retailPrice, quantity),
      "Expected a NullPointerException to be thrown when construction a Product with a null name");
  }

  @Test
  public void testConstructorNullWholesalePrice() {
    final String upc = "1263419857";
    final String name = "banana";
    final BigDecimal wholesalePrice = null;
    final BigDecimal retailPrice = BigDecimal.valueOf(0.25);
    final Integer quantity = 7;

    assertThrows(NullPointerException.class, () -> new Product(upc, name, wholesalePrice, retailPrice, quantity),
      "Expected a NullPointerException to be thrown when construction a Product with a null wholesalePrice");
  }

  @Test
  public void testConstructorNullRetailPrice() {
    final String upc = "1263419857";
    final String name = "banana";
    final BigDecimal wholesalePrice = BigDecimal.valueOf(0.05);
    final BigDecimal retailPrice = null;
    final Integer quantity = 7;

    assertThrows(NullPointerException.class, () -> new Product(upc, name, wholesalePrice, retailPrice, quantity),
      "Expected a NullPointerException to be thrown when construction a Product with a null retailPrice");
  }

  @Test
  public void testConstructorNullQuantity() {
    final String upc = "1263419857";
    final String name = "banana";
    final BigDecimal wholesalePrice = BigDecimal.valueOf(0.05);
    final BigDecimal retailPrice = BigDecimal.valueOf(0.25);
    final Integer quantity = null;

    assertThrows(NullPointerException.class, () -> new Product(upc, name, wholesalePrice, retailPrice, quantity),
      "Expected a NullPointerException to be thrown when construction a Product with a null quantity");
  }

}
