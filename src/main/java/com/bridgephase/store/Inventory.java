package com.bridgephase.store;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bridgephase.store.interfaces.IInventory;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Maintains a collection of {@link Product} objects keyed by
 * {@link Product#gtUpc() UPC}.
 * <p>
 * The {@link #replenish(InputStream) replenish} method is used to populate this
 * object with the products
 * 
 * <p>
 * This is a simple in-memory store of products which would likely only be
 * appropriate for dev environments. In a production environment the product
 * store would most likely would be held in a persistent database and the data
 * access layer would manage safe concurrent access to the products.
 */
public class Inventory implements IInventory {
  private final Map<String, Product> products = new HashMap<>();
  private final ProductParser parser = new ProductParser();

  /**
   * This implementation of replenishment will insert or replace any existing
   * products in this inventory. Any existing products that are not included in
   * the replenishment will remain in inventory unchanged.
   * 
   * @throws UncheckedIOException if IOException occurs while reading from input
   *                              Stream
   */
  @Override
  public void replenish(InputStream inputStream) {
    requireNonNull(inputStream, "The inputSteam argument is required; it must not be null");
    // not going to close input stream here
    // it is the responsibility of the caller to close the input stream.
    final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
    int lineNumber = 0;
    try {
      String line;
      if ((line = r.readLine()) != null) {
        // first line must be header
        parser.validateHeader(line);
        while ((line = r.readLine()) != null) {
          Product product = parser.parse(lineNumber, line);
          products.put(product.getUpc(), product);
          lineNumber++;
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Error reading input stream: lineNumber=" + lineNumber, e);
    }
  }

  /**
   * To be truly unmodifiable the list should contain unmodifiable (i.e.
   * immutable) objects or if that is not possible then it must contain a copy of
   * all the products held in inventory. I have chosen to make Product immutable.
   * 
   * @return returns an unmodifiable <code>List</code> of <code>Product</code>
   *         representing products inside the inventory.
   */
  @Override
  public List<Product> list() {
    ArrayList<Product> list = new ArrayList<>(this.products.values());
    return Collections.unmodifiableList(list);
  }

  Optional<Product> find(String upc) {
    return Optional.ofNullable(products.get(upc));
  }

  static class ProductParser {
    @SuppressWarnings("serial")
    static class ProductParseException extends RuntimeException {

      public ProductParseException(String message, Throwable cause) {
        super(message, cause);
      }

      public ProductParseException(String message) {
        super(message);
      }

      public ProductParseException(Throwable cause) {
        super(cause);
      }
    }

    static enum Field {
      UPC("upc"), NAME("name"), WHOLESALE_PRICE("wholesalePrice"), RETAIL_PRICE("retailPrice"), QUANTITY("quantity");

      private final int number;
      private final String headerName;

      Field(String headerName) {
        this.number = this.ordinal() + 1;
        this.headerName = headerName;
      }

      public int getNumber() {
        return number;
      }

      public String getHeaderName() {
        return headerName;
      }
    }

    private final List<Field> fields = ImmutableList.copyOf(Field.values());

    private final Splitter splitter = Splitter.on(',').trimResults();

    Product parse(final int lineNumber, final String line) {
      assert line != null;
      Product product = new Product();
      int fieldNum = 0;
      for (String value : splitter.split(line)) {
        Field field = fields.get(fieldNum);
        switch (field) {
        case UPC:
          product.setUpc(value);
          break;
        case NAME:
          product.setName(value);
          break;
        case WHOLESALE_PRICE:
          product.setWholesalePrice(parseBigDecimal(lineNumber, field.getNumber(), "wholesale price", value));
          break;
        case RETAIL_PRICE:
          product.setRetailPrice(parseBigDecimal(lineNumber, field.getNumber(), "retail price", value));
          break;
        case QUANTITY:
          product.setQuantity(parseInteger(lineNumber, field.getNumber(), "quantity", value));
          break;
        default:
          throw new IllegalArgumentException("Line contains an unsupported Field: " + field);
        }
        fieldNum++;
      }
      checkArgument(fieldNum == fields.size(),
        "Line does not contain the correct number of fields: expected=" + fields.size() + ", actual=" + fieldNum);
      return product;
    }

    void validateHeader(String line) {
      int i = 0;
      for (String value : splitter.split(line)) {
        Field field = fields.get(i);
        if (!field.getHeaderName().equals(value)) {
          throw new IllegalStateException("Unexpected header: number=" + field.number + ", expectedName="
            + field.getHeaderName() + ", actualName=" + value);
        }
        i++;
      }
    }

    private BigDecimal parseBigDecimal(int lineNumber, int fieldNumber, String fieldName, String fieldValue) {
      try {
        return new BigDecimal(fieldValue);
      } catch (NumberFormatException e) {
        throw new ProductParseException(
          String.format("Error parsing BigDecimal from field #%d (%s): lineNumber=%d, fieldValue=%s", fieldNumber,
            fieldName, lineNumber, fieldValue),
          e);
      }
    }

    private Integer parseInteger(int lineNumber, int fieldNumber, String fieldName, String fieldValue) {
      try {
        return Integer.valueOf(fieldValue);
      } catch (NumberFormatException e) {
        throw new ProductParseException(
          String.format("Error parsing Integer from field #%d (%s): lineNumber=%d, fieldValue=%s", fieldNumber,
            fieldName, lineNumber, fieldValue),
          e);
      }
    }

  }
}