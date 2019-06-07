package com.bridgephase.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class InventoryTest {

  private Inventory inventory;

  @BeforeEach
  public void setup() {
    inventory = new Inventory();
  }

  /**
   * Assert that a new Inventory object has an empty product list.
   */
  @Test
  public void testNewInventoryHasEmptyProductList() {
    assertNotNull(inventory.list(), "non-null product list");
    assertTrue(inventory.list().isEmpty(), "an empty product list");
  }

  /**
   * Assert that a null input stream passed to replenish will throw a
   * NullPointerException
   */
  @Test
  public void testReplenishNullInputStream() {
    assertThrows(NullPointerException.class, () -> inventory.replenish(null),
      "Expected a NullPointerException to be thrown when passing a null reference to replenish method");
  }

  /**
   * Assert that a null input stream passed to replenish will throw a
   * NullPointerException
   */
  @Test
  public void testReplenishDoesNotCloseInputStream() {
    final InputStream inputStream = new ByteArrayInputStream(new byte[0]) {
      @Override
      public void close() throws IOException {
        fail("Input stream should not be closed by the replenish method");
      }
    };
    inventory.replenish(inputStream);
  }

  /**
   * Assert that a UncheckedIOException is thrown from replenish when input stream
   * read throws IOException
   */
  @Test
  public void testReplenishThrowsUncheckedIOExceptionWhenIOExceptionOccursOnRead() {
    final InputStream inputStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("surprise");
      }
    };
    assertThrows(UncheckedIOException.class, () -> inventory.replenish(inputStream),
      "Expected a UncheckedIOException to be thrown when IOException is thrown by read in the replenish method");
  }

  @Test
  public void testReplenishEmptyString() throws UnsupportedEncodingException {
    inventory.replenish(new ByteArrayInputStream("".getBytes("UTF-8")));
    assertTrue(inventory.list().isEmpty(), "Expected inventory to be empty after replenishment using empty string");
  }

  /**
   * test that one product can be replenished
   * 
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testReplenishOneRecord() throws UnsupportedEncodingException {
    inventory.replenish(
      new ByteArrayInputStream("upc,name,wholesalePrice,retailPrice,quantity\n123,apple,.30,.50,10".getBytes("UTF-8")));
    assertEquals(1, inventory.list().size(), "Expected inventory to contain one product");
    assertEquals(new Product("123", "apple", new BigDecimal("0.3"), new BigDecimal(".5"), 10), inventory.list().get(0));
  }

  /**
   * verify that more than one product can be replenished
   * 
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testReplenishThreeRecords() throws UnsupportedEncodingException {
    inventory.replenish(new ByteArrayInputStream(
      "upc,name,wholesalePrice,retailPrice,quantity\nA123,Apple,0.50,1.00,100\nB234,Peach,0.35,0.75,200\nC123,Milk,2.15,4.50,40"
        .getBytes("UTF-8")));
    List<Product> list = new ArrayList<>(inventory.list());
    assertEquals(3, list.size(), "Expected inventory to contain three products");
    Collections.sort(list, (Product a, Product b) -> a.getUpc().compareTo(b.getUpc()));
    assertEquals(new Product("A123", "Apple", new BigDecimal("0.50"), new BigDecimal("1.00"), 100), list.get(0));
    assertEquals(new Product("B234", "Peach", new BigDecimal("0.35"), new BigDecimal("0.75"), 200), list.get(1));
    assertEquals(new Product("C123", "Milk", new BigDecimal("2.15"), new BigDecimal("4.50"), 40), list.get(2));
  }

  /**
   * test that a duplicate upc causes existing product to be overwritten
   */
  @Test
  public void testReplenishDupUpcRecords() throws UnsupportedEncodingException {
    inventory.replenish(new ByteArrayInputStream(
      "upc,name,wholesalePrice,retailPrice,quantity\nA123,Apple,0.50,1.00,100\nB234,Peach,0.35,0.75,200\nC123,Milk,2.15,4.50,40\nA123,Applesauce,0.55,1.05,105"
        .getBytes("UTF-8")));
    List<Product> list = new ArrayList<>(inventory.list());
    assertEquals(3, list.size(), "Expected inventory to contain three products");
    Collections.sort(list, (Product a, Product b) -> a.getUpc().compareTo(b.getUpc()));
    assertEquals(new Product("A123", "Applesauce", new BigDecimal("0.55"), new BigDecimal("1.05"), 105), list.get(0),
      "Expected the product with upc=A123 to be equal to the last product record read for that upc");
    assertEquals(new Product("B234", "Peach", new BigDecimal("0.35"), new BigDecimal("0.75"), 200), list.get(1));
    assertEquals(new Product("C123", "Milk", new BigDecimal("2.15"), new BigDecimal("4.50"), 40), list.get(2));
  }

  @Test
  public void testListEmpty() {
    assertTrue(inventory.list().isEmpty(), "Expected inventory to be empty after creating new inventory object");
  }

  @Test
  public void testListUnmodifiable() throws UnsupportedEncodingException {
    inventory.replenish(
      new ByteArrayInputStream("upc,name,wholesalePrice,retailPrice,quantity\n123,apple,.30,.50,10".getBytes("UTF-8")));
    assertThrows(UnsupportedOperationException.class, () -> inventory.list().remove(0),
      "Expected UnsupportedOperationException to be thrown when attempting to remove a product from list");
  }

}
