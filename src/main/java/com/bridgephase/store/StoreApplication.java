package com.bridgephase.store;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.bridgephase.store.interfaces.IInventory;

/**
 * This can be used as an entry point to ensure that you have a working implementation for Part 1.
 * 
 * This class will initially contain compilation errors because the classes in
 * the exercise have not been implemented. Once you've implemented them the
 * compilation errors should disappear.
 * 
 * You are free to modify this class as you deem necessary although it's highly
 * recommended that you produce jUnit tests to verify the logic in your code.
 * 
 * @author Jaime Garcia Ramirez (jramirez@bridgephase.com)
 */
public class StoreApplication {

	/**
	 * This is the main entry point to this application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		// TODO: The wrapping of everything in a try/catch is not very clean
		// if only there was a way to avoid this?
		try {
			InputStream input = inputStreamFromString(
					"upc,name,wholesalePrice,retailPrice,quantity\n" + 
					"A123,Apple,0.50,1.00,100");
			IInventory inventory = new Inventory();
			inventory.replenish(input);

			for (Product product : inventory.list()) {
				System.out.println("Found a product " + product);
			}
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}
	}

	/**
	 * This is a simple way to convert a string to an input stream.
	 * 
	 * @param value
	 *            the String value to convert
	 * @return an InputStream that can read the values from the
	 *         <code>value</code> parameter
	 * @throws UnsupportedEncodingException
	 */
	private static InputStream inputStreamFromString(String value) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(value.getBytes("UTF-8"));
	}
}