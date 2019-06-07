package com.bridgephase.store;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

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

}
