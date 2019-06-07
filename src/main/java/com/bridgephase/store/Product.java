package com.bridgephase.store;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;

public class Product {
  // force all big decimals to be scale 2
  private static final int SCALE = 2;

  private String upc;
  private String name;
  private BigDecimal wholesalePrice;
  private BigDecimal retailPrice;
  private Integer quantity;

  public Product(String upc, String name, BigDecimal wholesalePrice, BigDecimal retailPrice, Integer quantity) {
    super();
    setUpc(upc);
    setName(name);
    setWholesalePrice(wholesalePrice);
    setRetailPrice(retailPrice);
    setQuantity(quantity);
  }

  public Product() {
  }

  public Product(Product source) {
    this(requireNonNull(source, "The 'Product source' argument is required; it must not be null").getUpc(),
      source.getName(), source.getWholesalePrice(), source.getRetailPrice(), source.getQuantity());
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

  public void setUpc(String upc) {
    this.upc = requireNonNull(upc, "The 'String upc' argument is required; it must not be null");
  }

  public void setName(String name) {
    this.name = requireNonNull(name, "The 'String name' argument is required; it must not be null");
  }

  public void setWholesalePrice(BigDecimal wholesalePrice) {
    this.wholesalePrice = requireNonNull(wholesalePrice,
      "The 'BigDecimal wholesalePrice' argument is required; it must not be null").setScale(SCALE);
  }

  public void setRetailPrice(BigDecimal retailPrice) {
    this.retailPrice = requireNonNull(retailPrice,
      "The 'BigDecimal retailPrice' argument is required; it must not be null").setScale(SCALE);
  }

  public void setQuantity(Integer quantity) {
    this.quantity = requireNonNull(quantity, "The 'Integer quantity' argument is required; it must not be null");
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
}
