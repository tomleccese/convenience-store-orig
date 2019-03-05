package com.bridgephase.store;

import java.io.InputStream;
import java.util.List;

import com.bridgephase.store.interfaces.IInventory;

public class Inventory implements IInventory {
    @Override
    public void replenish(InputStream inputStream) {
    }

    @Override
    public List<Product> list() {
        return null;
    }
}
