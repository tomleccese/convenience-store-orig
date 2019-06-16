package com.bridgephase.store;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.bridgephase.store.Inventory.ProductParser;
import com.bridgephase.store.Inventory.ProductParser.ProductParseException;
import com.google.common.base.Splitter;

class ProductParserTest {

  private ProductParser parser;

  @BeforeEach
  void setup() {
    parser = new ProductParser();
  }

  @Test
  void testParseNull() {
    assertThrows(NullPointerException.class, () -> parser.parse(0, null));
  }

  @Test
  void testParseNoInput() {
    assertThrows(IllegalArgumentException.class, () -> parser.parse(0, ""));
  }

  @Test
  void testParseInvalidLineOneField() {
    assertThrows(IllegalArgumentException.class, () -> parser.parse(0, "abc"));
  }

  @Test
  void testReadHeader() {
    assertEquals(true, parser.readHeader("upc,name,wholesalePrice,retailPrice,quantity"));
  }

  @Test
  void testReadHeaderNull() {
    assertEquals(false, parser.readHeader(null));
  }

  private final Splitter splitter = Splitter.on(',').trimResults();

  void assertHeaderFieldCountMismatchMessage(Throwable expected, int actualFieldCount) {
    final String expectedMessage = format(
      "Unexpected header: field count mismatch: expected 5 fields but got %d fields instead:", actualFieldCount);
    assertTrue(expected.getMessage().startsWith(expectedMessage),
      () -> format("Unexpected exception message: expected message starting with '%s' but got actual message of '%s'",
        expectedMessage, expected.getMessage()));
  }

  @ParameterizedTest()
  @ValueSource(strings = { "upc", "upc,name", "upc,name,wholesalePrice", "upc,name,wholesalePrice,retailPrice",
      "upc,name,wholesalePrice,retailPrice,quantity,extra" })
  void testReadHeaderFieldCountMismatch(String header) {
    assertHeaderFieldCountMismatchMessage(assertThrows(IllegalArgumentException.class, () -> parser.readHeader(header)),
      splitter.splitToList(header).size());
  }

  void assertHeaderFieldNameMismatchMessage(Throwable expected, int actualFieldCount) {
    final String expectedMessage = format("Unexpected header field: number=%s", actualFieldCount);
    assertTrue(expected.getMessage().startsWith(expectedMessage),
      () -> format("Unexpected exception message: expected message starting with '%s' but got actual message of '%s'",
        expectedMessage, expected.getMessage()));
  }

  @ParameterizedTest()
  @ValueSource(strings = { "", "upc,", "upc,name,", "upc,name,wholesalePrice,", "upc,name,wholesalePrice,retailPrice,",
      "upc1", "upc,name1", "upc,name,wholesalePrice1", "upc,name,wholesalePrice,retailPrice1",
      "upc,name,wholesalePrice,retailPrice,quantity1" })
  void testReadHeaderFieldNameMismatch(String header) {
    assertHeaderFieldNameMismatchMessage(assertThrows(IllegalArgumentException.class, () -> parser.readHeader(header)),
      splitter.splitToList(header).size());
  }

  void assertLineFieldCountMismatchMessage(Throwable expected, int actualFieldCount) {
    final String expectedMessage = format("Line does not contain the correct number of fields: expected=5, actual=%d",
      actualFieldCount);
    assertTrue(expected.getMessage().startsWith(expectedMessage),
      () -> format("Unexpected exception message: expected message starting with '%s' but got actual message of '%s'",
        expectedMessage, expected.getMessage()));
  }

  @ParameterizedTest()
  @ValueSource(strings = { "A123", "A123,Apple", "A123,Apple,0.50", "A123,Apple,0.50,1.00",
      "A123,Apple,0.50,1.00,100,extra" })
  void testReadLineCountMismatch(String line) {
    assertLineFieldCountMismatchMessage(assertThrows(IllegalArgumentException.class, () -> parser.parse(0, line)),
      splitter.splitToList(line).size());
  }

  @ParameterizedTest()
  @ValueSource(strings = { "A123,Apple,0.50x,1.00,100", "A123,Apple,0.50,1.0.0,100", "A123,Apple,0.50,100,a100" })
  void testReadLineFieldFormatError(String line) {
    assertThrows(ProductParseException.class, () -> parser.parse(0, line));
  }

}
