package com.bridgephase.store;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

/**
 * Models a product in {@link Inventory}. The {@link #upc} is the key identifier
 * for a product (i.e. uniquely identifies a product). The {@link #name} is
 * assumed to not be a key identifier for a product. This means that there may
 * be two products with the same name but with different upc values.
 */
public final class Product {
  // force all big decimals to be have scale that matches the default fraction
  // digits for the default locale currency
  private static final int SCALE = Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits();
  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  private final String upc;
  private final String name;
  private final BigDecimal wholesalePrice;
  private final BigDecimal retailPrice;
  private final Integer quantity;

  public Product(String upc, String name, BigDecimal wholesalePrice, BigDecimal retailPrice, Integer quantity) {
    super();
    this.upc = checkNotNull(upc, "The 'String upc' argument is required; it must not be null");
    this.name = checkNotNull(name, "The 'String name' argument is required; it must not be null");
    this.wholesalePrice = checkNotNull(wholesalePrice,
      "The 'BigDecimal wholesalePrice' argument is required; it must not be null").setScale(SCALE, ROUNDING_MODE);
    this.retailPrice = checkNotNull(retailPrice,
      "The 'BigDecimal retailPrice' argument is required; it must not be null").setScale(SCALE, ROUNDING_MODE);
    this.quantity = checkNotNull(quantity, "The 'Integer quantity' argument is required; it must not be null");
  }

  public Product(Product source) {
    this(checkNotNull(source, "The 'Product source' argument is required; it must not be null").getUpc(),
      source.getName(), source.getWholesalePrice(), source.getRetailPrice(), source.getQuantity());
  }

  public Product(String upc, String name, double wholesalePrice, double retailPrice, int quantity) {
    this(upc, name, new BigDecimal(wholesalePrice), new BigDecimal(retailPrice), quantity);
  }

  public String getUpc() {
    return upc;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getWholesalePrice() {
    return wholesalePrice;
  }

  public BigDecimal getRetailPrice() {
    return retailPrice;
  }

  public Integer getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return "Product [upc=" + upc + ", name=" + name + ", wholesalePrice=" + wholesalePrice + ", retailPrice="
      + retailPrice + ", quantity=" + quantity + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
    result = prime * result + ((retailPrice == null) ? 0 : retailPrice.hashCode());
    result = prime * result + ((upc == null) ? 0 : upc.hashCode());
    result = prime * result + ((wholesalePrice == null) ? 0 : wholesalePrice.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Product other = (Product) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (quantity == null) {
      if (other.quantity != null)
        return false;
    } else if (!quantity.equals(other.quantity))
      return false;
    if (retailPrice == null) {
      if (other.retailPrice != null)
        return false;
    } else if (!retailPrice.equals(other.retailPrice))
      return false;
    if (upc == null) {
      if (other.upc != null)
        return false;
    } else if (!upc.equals(other.upc))
      return false;
    if (wholesalePrice == null) {
      if (other.wholesalePrice != null)
        return false;
    } else if (!wholesalePrice.equals(other.wholesalePrice))
      return false;
    return true;
  }

  static class Builder {

    private String upc;
    private String name;
    private BigDecimal wholesalePrice;
    private BigDecimal retailPrice;
    private Integer quantity;

    Builder() {
      super();
    }

    Builder(Product product) {
      super();
      this.upc = product.upc;
      this.name = product.name;
      this.wholesalePrice = product.wholesalePrice;
      this.retailPrice = product.retailPrice;
      this.quantity = product.quantity;
    }

    public Builder withUpc(String upc) {
      this.upc = upc;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withWholesalePrice(BigDecimal wholesalePrice) {
      this.wholesalePrice = wholesalePrice;
      return this;
    }

    public Builder withRetailPrice(BigDecimal retailPrice) {
      this.retailPrice = retailPrice;
      return this;
    }

    public Builder withQuantity(Integer quantity) {
      this.quantity = quantity;
      return this;
    }

    Product build() {
      return new Product(upc, name, wholesalePrice, retailPrice, quantity);
    }
  }

  /**
   * Creates a new {@link Product} that contains all the same values as the new
   * Product, except for the quantity which is set to the sum of the old product
   * quantity and the new product quantity.
   * 
   * @param oldValue
   * @param newValue
   * @return
   */
  public static Product merge(Product oldValue, Product newValue) {
    checkNotNull(newValue, "The 'Product newValue' argument is required; it must not be null");
    if (oldValue == null) {
      return newValue;
    } else {
      checkArgument(Objects.equal(oldValue.getUpc(), newValue.getUpc()),
        "The upc value is required to be the same for both the oldValue and newValue argument: oldValue.upc=%s, newValue.upc=%s");
      return new Product(newValue.getUpc(), newValue.getName(), newValue.getWholesalePrice(), newValue.getRetailPrice(),
        oldValue.getQuantity() + newValue.getQuantity());
    }
  }
}
