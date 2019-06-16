package com.bridgephase.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

/**
 *
 */
public class InventoryThreadSafeTest {

	private Inventory inventory;

	@BeforeEach
	public void setup() {
		inventory = new Inventory();
	}

	/**
	 * verify that more than one product can be replenished by multiple threads
	 * simultaneously
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@RepeatedTest(10)
	public void testReplenishAndAdjustOnMultipleThreads() throws UnsupportedEncodingException {
		assertEquals(0, inventory.list().size());

		/*
		 * missedAdjustments holds the total quantity for each upc of updateQuantity
		 * invocations which failed to update the product because the product was not
		 * yet added to inventory by the replenish method.
		 */
		final ConcurrentMap<String, Integer> missedAdjustments = new ConcurrentHashMap<>();

		// number of threads
		final int threads = 10;

		// the service that will assign submitted runnables to threads
		final ExecutorService service = Executors.newFixedThreadPool(threads);

		/*
		 * latch: prevents threads from executing their submitted runnable until all
		 * runnables have been submitted to executor service
		 */
		final CountDownLatch latch = new CountDownLatch(1);

		/*
		 * futures: collection of futures to wait for before verifying results of test
		 */
		final Collection<Future<?>> futures = new ArrayList<>(threads);

		for (int t = 0; t < threads; ++t) {
			final Runnable runnable;
			// we will submit 3 different types of runnables (case 0 - 3)
			// one for each thread
			switch (Math.floorMod(t, 3)) {
			case 0:
				// replenish runnable, creating 4 products with positive quantities
				final InputStream inputStream = new ByteArrayInputStream(
						"upc,name,wholesalePrice,retailPrice,quantity\nA123,Apple,0.50,1.00,1\nB234,Peach,0.35,0.75,10\nC123,Milk,2.15,4.50,100\nA234,Avocado,.50,1,1000"
								.getBytes("UTF-8"));
				runnable = () -> {
					// make thread wait until latch is released
					try {
						latch.await();
						inventory.replenish(inputStream);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				};
				break;
			case 1:
				// adjustQuantity runnable with positive quantities, collect missed adjustments
				runnable = () -> {
					// make thread wait until latch is released
					try {
						latch.await();
					// @formatter:off
            inventory.adjustQuantity("A123",    1).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("A123",    1, Math::addExact));
            inventory.adjustQuantity("B234",   10).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("B234",   10, Math::addExact));
            inventory.adjustQuantity("C123",  100).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("C123",  100, Math::addExact));
            inventory.adjustQuantity("A234", 1000).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("A234", 1000, Math::addExact));
          // @formatter:on
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				};
				break;
			case 2:
				// adjustQuantity runnable with negative quantities, collect missed adjustments
				runnable = () -> {
					// make thread wait until latch is released
					try {
						latch.await();
					// @formatter:off
            inventory.adjustQuantity("A123",    -1).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("A123",    -1, Math::addExact));
            inventory.adjustQuantity("B234",   -10).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("B234",   -10, Math::addExact));
            inventory.adjustQuantity("C123",  -100).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("C123",  -100, Math::addExact));
            inventory.adjustQuantity("A234", -1000).ifPresentOrElse(p -> {}, () -> missedAdjustments.merge("A234", -1000, Math::addExact));
          // @formatter:on
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				};
				break;
			default:
				throw new IllegalStateException();
			}
			// hold onto future returned by submit
			futures.add(service.submit(runnable));
		}

		// release the latch so all replenish/adjustQuantity runnables proceed
		latch.countDown();

		// wait till all futures complete
		for (Future<?> f : futures) {
			try {
				f.get();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e.getCause());
			}
		}
		if (!missedAdjustments.isEmpty()) {
			// for debug, uncomment line below to see all missedAdjustment quantities
			 System.out.println("missedAdjustments=" + missedAdjustments);
		}
		assertEquals(4, inventory.list().size(), "Expected inventory to contain four products after replenishment");

		// calculate base quantity using number of threads and 3 types of runnables
		int baseCount = Math.floorDiv(threads, 3) + Math.floorMod(threads, 3);
		/*
		 * for each product assert that the quantity is as expected (adjusting each
		 * expected quantity by number of missed adjustments)
		 */
		// @formatter:off
		assertEquals(    1 * baseCount - missedAdjustments.getOrDefault("A123", 0), inventory.find("A123").get().getQuantity(), "A123 quantity");
		assertEquals(   10 * baseCount - missedAdjustments.getOrDefault("B234", 0), inventory.find("B234").get().getQuantity(), "B234 quantity");
		assertEquals(  100 * baseCount - missedAdjustments.getOrDefault("C123", 0), inventory.find("C123").get().getQuantity(), "C123 quantity");
		assertEquals( 1000 * baseCount - missedAdjustments.getOrDefault("A234", 0), inventory.find("A234").get().getQuantity(), "A234 quantity");
		// @formatter:on
	}

}
