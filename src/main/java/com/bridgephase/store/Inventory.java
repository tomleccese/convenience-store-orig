package com.bridgephase.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.bridgephase.store.interfaces.IInventory;

public class Inventory implements IInventory {

	/**
	 * @throws UncheckedIOException if IOException occurs while reading from input
	 *                              Stream
	 */
	@Override
	public void replenish(InputStream inputStream) {
		Objects.requireNonNull(inputStream, "The inputSteam argument is required; it must not be null");
		try {
			assert inputStream.read() == -1;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public List<Product> list() {
		List<Product> list = new ArrayList<>();

		assert list != null : "The returned list is required; it must not be null";
		return list;
	}

}