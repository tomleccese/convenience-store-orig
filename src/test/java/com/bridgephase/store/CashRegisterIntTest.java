package com.bridgephase.store;

import static com.bridgephase.store.TestUtils.bigdec;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bridgephase.store.interfaces.IInventory;

class CashRegisterIntTest {
	private CashRegister register;

	private IInventory inventory;

	@BeforeEach
	private void setup() {
		inventory = new Inventory(new Product("A123", "Apple", 0.50, 1.00, 100),
				new Product("B234", "Peach", 0.35, 0.75, 200), new Product("C123", "Milk", 2.15, 4.50, 40),
				new Product("MC123", "Mr Coffee 12-cup", 10.00, 20.00, 1), new Product("A234", "Avocado", .50, 1, 2));
		register = new CashRegister(inventory);
	}

	@Test
	void testScanProductNotInInventory() {
		register.beginTransaction();
		assertEquals(false, register.scan("P9889"),
				"Expected register.scan to return false when scanning a upc that is not in inventory");
		assertEquals(bigdec(0.00), register.getTotal());
	}

	@Test
	void testScanProductOutOfStock() {
		register.beginTransaction();
		assertEquals(true, register.scan("A234"), "Expected register.scan to return true");
		assertEquals(true, register.scan("A234"), "Expected register.scan to return true");
		assertEquals(false, register.scan("A234"), "Expected register.scan to return true");
		/*
		 * the third scan of A234 returns false, but the product is still added to the
		 * transaction, because this is a convenience store. I made the assumption that
		 * only products in the hands of the customer will be scanned in a convenience
		 * store. For example if the inventory says there are only two avocados in stock
		 * but the customer has brought 3 avocados to the checkout then you are not
		 * going to prevent the sale of the third avocado just because the inventory
		 * says it is out of stock.
		 */
		assertEquals(bigdec(3.00), register.getTotal());
	}

	@Test
	void testScanPaidAndReceiptPrinted() {
		register.beginTransaction();

		// verify that transaction total is 0.00
		assertEquals(bigdec(0.00), register.getTotal());

		// add one A123 product
		assertEquals(true, register.scan("A123"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(bigdec(1.00), register.getTotal());

		// add another A123 product
		assertEquals(true, register.scan("A123"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(bigdec(2.00), register.getTotal());

		// add B234 product
		assertEquals(true, register.scan("B234"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(bigdec(2.75), register.getTotal());

		// add P9889 product
		assertEquals(false, register.scan("P9889"),
				"Expected register.scan to return false when scanning a upc that is not in inventory");
		// and verify that the total has not changed
		assertEquals(bigdec(2.75), register.getTotal());

		// add C123 product
		assertEquals(true, register.scan("C123"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(bigdec(7.25), register.getTotal());

		// add 3 of A234 product
		assertEquals(true, register.scan("A234"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(true, register.scan("A234"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(false, register.scan("A234"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		// customer brought three avocados to the register, but inventory says there is
		// only 2 in stock so the scan for the third avocado returns false, but that
		// avocado is still added to the
		// transaction anyway
		assertEquals(bigdec(10.25), register.getTotal());

		register.pay(bigdec(11.0));

		assertEquals(98, inventory.find("A123").map(p -> p.getQuantity()).orElseGet(() -> 0));
		assertEquals(39, inventory.find("C123").map(p -> p.getQuantity()).orElseGet(() -> 0));
		assertEquals(-1, inventory.find("A234").map(p -> p.getQuantity()).orElseGet(() -> 0));

		assertEquals(bigdec(10.25), register.getTotal());

		final String expectedReceipt;
		{
			final NumberFormat currency = NumberFormat.getCurrencyInstance();
			// @formatter:off
      StringBuilder b = new StringBuilder();
      b.append(format("BridgePhase Convenience Store%n"));
      b.append(format("-----------------------------%n"));
      b.append(format("Total Products Bought: 7%n"));
      b.append(format("%n"));
      b.append(format("2 Apple @ %s: %s%n", currency.format(1), currency.format(2)));
      b.append(format("1 Peach @ %s: %s%n", currency.format(.75), currency.format(.75)));
      b.append(format("1 Milk @ %s: %s%n", currency.format(4.5), currency.format(4.5)));
      b.append(format("3 Avocado @ %s: %s%n", currency.format(1), currency.format(3)));
      b.append(format("-----------------------------%n"));
      b.append(format("Total: %s%n", currency.format(10.25)));
      b.append(format("Paid: %s%n", currency.format(11)));
      b.append(format("Change: %s%n", currency.format(.75)));
      b.append(format("-----------------------------%n"));
      // @formatter:on
			expectedReceipt = b.toString();
		}
		final String actualReceipt;
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			register.printReceipt(out);
			actualReceipt = out.toString();
		}
		assertEquals(expectedReceipt, actualReceipt);

	}

	@Test
	void testPaidWithInsufficientFunds() {
		register.beginTransaction();

		// verify that transaction total is 0.00
		assertEquals(bigdec(0.00), register.getTotal());

		// add one A123 product
		assertEquals(true, register.scan("A123"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(bigdec(1.00), register.getTotal());

		// add another A123 product
		assertEquals(true, register.scan("A123"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(bigdec(2.00), register.getTotal());

		// add B234 product
		assertEquals(true, register.scan("B234"),
				"Expected register.scan to return true when scanning a upc that is in inventory");
		assertEquals(bigdec(2.75), register.getTotal());

		// add P9889 product
		assertEquals(false, register.scan("P9889"),
				"Expected register.scan to return false when scanning a upc that is not in inventory");
		// and verify that the total has not changed
		assertEquals(bigdec(2.75), register.getTotal());

		assertThrows(InsufficientFundsException.class, () -> register.pay(bigdec(2.00)));
	}

	@Test
	void testTwoSequentialTransactions() {
		register.beginTransaction();
		register.pay(bigdec(0));
		register.beginTransaction();
		register.pay(bigdec(0));
	}
}