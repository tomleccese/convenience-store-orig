package com.bridgephase.store;

import java.math.BigDecimal;
import static com.google.common.base.Preconditions.*;


class TransactionLineItem {
  private final String name;
  private final BigDecimal price;
  private final Integer quantity;

  TransactionLineItem(String name, BigDecimal price, Integer quantity) {
    super();
    this.name = checkNotNull(name, "The 'String name' argument is required; it must not be null");
    this.price = checkNotNull(price, "The 'BigDecimal price' argument is required; it must not be null");
    this.quantity = checkNotNull(quantity, "The 'Integer quantity' argument is required; it must not be null");
  }

  BigDecimal extendedPrice() {
    return price.multiply(BigDecimal.valueOf(this.quantity));
  }

  String getName() {
		return name;
	}

	BigDecimal getPrice() {
		return price;
	}

	Integer getQuantity() {
		return quantity;
	}

	/**
   * Creates a new {@link TransactionLineItem} that contains all the same values as the new
   * LineItem, except for the quantity which is set to the sum of the old quantity
   * and the new quantity.
   * 
   * @param oldValue
   * @param newValue
   * @return
   */
  static TransactionLineItem merge(TransactionLineItem oldValue, TransactionLineItem newValue) {
    checkNotNull(newValue, "The 'LineItem newValue' argument is required; it must not be null");
    if (oldValue == null) {
      return newValue;
    } else {
      return new TransactionLineItem(newValue.name, newValue.price, oldValue.quantity + newValue.quantity);
    }
  }
}