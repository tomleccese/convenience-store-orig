package com.bridgephase.store;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.junit.Before;
import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 *
 */
public class InventoryTest {

	private Inventory inventory;

	@Before
	public void setup() {
		inventory = new Inventory();
	}

	/**
	 * Assert that a new Inventory object has an empty product list.
	 */
	@Test
	public void testNewInventoryHasEmptyProductList() {
		assertNotNull("non-null product list", inventory.list());
		assertTrue("an empty product list", inventory.list().isEmpty());
	}

	/**
	 * Assert that a null input stream passed to replenish will throw a
	 * NullPointerException
	 */
	@Test(expected = NullPointerException.class)
	public void testReplenishNullInputStream() {
		inventory.replenish(null);
		fail("Expected a NullPointerException to be thrown when passing a null reference to replenish method");
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
				throw new AssertionFailedError("Input stream should not be closed by the replenish method");
			}
		};
		inventory.replenish(inputStream);
	}

	/**
	 * Assert that a UncheckedIOException is thrown from replenish when input stream
	 * read throws IOException
	 */
	@Test(expected = UncheckedIOException.class)
	public void testReplenishThrowsUncheckedIOExceptionWhenIOExceptionOccursOnRead() {
		final InputStream inputStream = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("surprise");
			}
		};
		inventory.replenish(inputStream);
	}

}
