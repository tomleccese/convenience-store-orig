package com.bridgephase.store;

import static java.lang.String.format;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.bridgephase.store.interfaces.IInventory;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Maintains a collection of {@link Product} objects keyed by
 * {@link Product#gtUpc() UPC}.
 * <p>
 * The {@link #replenish(InputStream) replenish} method is used to populate this
 * object with the products
 * <p>
 * The {@link #adjustQuantity(String, Integer) adjustQuantity} method is used to
 * adjust the quantity of a given product (e.g. after completion of sale)
 * 
 * <p>
 * Note: This inventory is thread-safe.
 */
public class Inventory implements IInventory {

	/**
	 * ConcurrentMap is used so that multiple-threads can update the inventory in a
	 * thread-safe manner. This allows this inventory can be shared safely between
	 * multiple cash registers and callers to the replenish methods.
	 */
	private final ConcurrentMap<String, Product> products = new ConcurrentHashMap<>();

	private final ProductParser parser = new ProductParser();

	Inventory(final Product... products) {
		checkNotNull(products, "The 'Product[] products' argument is required; it must not be null");
		int i = 0;
		for (Product product : products) {
			checkNotNull(product,
					"All elements in the 'Product[] products' array are required; the element at index %d is null; it must not be null",
					i);
			final Product versionedProduct = new Product(product);
			this.products.merge(product.getUpc(), versionedProduct, Product::merge);
			i++;
		}
	}

	/**
	 * This implementation of replenishment will insert or update any existing
	 * products in this inventory. Any existing products that are not included in
	 * the replenishment will remain in inventory unchanged. The quantity on a
	 * replenishment {@link Product} is {@link Product#merge(Product, Product) added
	 * to the existing quantity}.
	 * 
	 * @see Product#merge(Product, Product)
	 * @throws UncheckedIOException if IOException occurs while reading from input
	 *                              Stream
	 */
	@Override
	synchronized public void replenish(InputStream inputStream) {
		checkNotNull(inputStream, "The inputSteam argument is required; it must not be null");
		// not going to close input stream here
		// it is the responsibility of the caller to close the input stream.
		final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		int lineNumber = 1;
		try {
			// first line is required to be the header (no blank/empty lines allowed before
			// header)
			if (parser.readHeader(r.readLine())) {
				lineNumber++;
				for (String line; (line = r.readLine()) != null; lineNumber++) {
					if (line.trim().isEmpty()) {
						// we'll allow and ignore any blank, empty lines
						continue;
					} else {
						final Product parsed = parser.parse(lineNumber, line);
						products.merge(parsed.getUpc(), new Product(parsed), Product::merge);
					}
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading input stream: lineNumber=" + lineNumber, e);
		}
	}

	/**
	 * To be truly unmodifiable the list should contain unmodifiable (i.e.
	 * immutable) objects That is why I have chosen to make {@link Product}
	 * immutable.
	 * 
	 * @return returns an unmodifiable <code>List</code> of <code>Product</code>
	 *         representing products inside the inventory.
	 */
	@Override
	public List<Product> list() {
		return ImmutableList.copyOf(this.products.values());
	}

	@Override
	public Optional<Product> find(String upc) {
		return Optional.ofNullable(products.get(upc));
	}

	@Override
	synchronized public Optional<Product> adjustQuantity(final String upc, final Integer delta) {
		checkNotNull(upc, "The 'String upc' argument is required; it must not be null");
		checkNotNull(delta, "The 'Integer delta' argument is required; it must not be null");
		return Optional.ofNullable(products.computeIfPresent(upc, (key, oldProduct) -> {
			return new Product(upc, oldProduct.getName(), oldProduct.getWholesalePrice(), oldProduct.getRetailPrice(),
					oldProduct.getQuantity() + delta);
		}));
	}

	/**
	 * Handles header line and data line parsing of {@link Inventory}
	 * {@link Inventory#replenish(InputStream) replenishment}
	 */
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
		private final String expectedHeader = Joiner.on(',')
				.join(Arrays.stream(Field.values()).map(e -> e.headerName).toArray());

		/**
		 * The {@link Splitter} used for parsing product data lines
		 */
		private final Splitter splitter = Splitter.on(',').trimResults();

		/**
		 * Parses the given data line of text into a {@link Product}
		 * 
		 * @param lineNumber the line number
		 * @param line       the data line of text
		 * @return the Product parsed from the given data line of text
		 */
		Product parse(final int lineNumber, final String line) {
			checkNotNull(line, "The 'String line' argument is required; it must not be null");
			Product.Builder product = new Product.Builder();
			int fieldNum = 0;
			for (String value : splitter.split(line)) {
				if (fieldNum < fields.size()) {
					Field field = fields.get(fieldNum);
					switch (field) {
					case UPC:
						product.withUpc(value);
						break;
					case NAME:
						product.withName(value);
						break;
					case WHOLESALE_PRICE:
						product.withWholesalePrice(parseBigDecimal(lineNumber, field.getNumber(), "wholesale price", value));
						break;
					case RETAIL_PRICE:
						product.withRetailPrice(parseBigDecimal(lineNumber, field.getNumber(), "retail price", value));
						break;
					case QUANTITY:
						product.withQuantity(parseInteger(lineNumber, field.getNumber(), "quantity", value));
						break;
					default:
						throw new IllegalArgumentException("Line contains an unsupported Field: " + field);
					}
				}
				fieldNum++;
			}
			checkArgument(fieldNum == fields.size(),
					"Line does not contain the correct number of fields: expected=" + fields.size() + ", actual=" + fieldNum);
			return product.build();
		}

		/**
		 * Reads the given line and validates that it is a proper header line
		 * @param line a header line
		 * @return false if line is null else true if line is the header
		 * @throws IllegalArgumentException if line is not null and does not match the
		 *                                  expected header line
		 */
		boolean readHeader(String line) {
			if (line == null) {
				return false;
			} else {
				validateHeader(line);
				return true;
			}
		}

		/**
		 * Validates that the line of input is a header line as defined in
		 * {@link #expectedHeader}
		 * 
		 * @param line a line of input
		 * @throws NullPointerException     if input line is null
		 * @throws IllegalArgumentException if input line is not a valid header line
		 */
		void validateHeader(String line) {
			checkNotNull(line);
			int i = 0;
			for (String value : splitter.split(line)) {
				if (i < fields.size()) {
					Field field = fields.get(i);
					checkArgument(field.getHeaderName().equals(value),
							"Unexpected header field: number=%s, expectedName=%s, actualName=%s, expectedHeader=%s, actualHeader=%s",
							field.number, field.getHeaderName(), value, expectedHeader, line);
				}
				i++;
			}
			checkArgument(i == fields.size(), format(
					"Unexpected header: field count mismatch: expected %d fields but got %d fields instead: expectedHeader=%s, actualHeader=%s",
					fields.size(), i, expectedHeader, line));
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