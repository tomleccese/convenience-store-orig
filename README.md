# BridgePhase Java Assignment - Convenience Store

## Instructions

This assignment consists of various steps, *at a mininum*, **commit your code at the end of every step** with a meaningful commit message including the part number you're addressing. Make sure you push your code often.

Class and method names that are specifically defined in this document must be exact (for example `Inventory`) and be placed under the `com.bridgephase.store` package. Any additional classes or methods that you require have no naming or package restrictions. Pay special attention to quality. Quality is more important than quantity in this exercise. If you are unable to complete all the tasks don't worry. 

If you have any questions or need clarifications regarding any part of the exercise, add an **Issue record** under this project and a BridgPhase representative will respond to you. Your submission must be pushed to the Git server in order to be reviewed, inform a BridgePhase representative via email when you've completed the assignment.

The project is already able to be imported into Eclipse, just use Eclipse to import it as an **Existing Project**. If you use another IDE, you should be able to import the `src/main/java` and `src/test/java` folders as well just make sure you add jUnit 4.X to the classpath (if not using Eclipse).

It's recommended that you include jUnit tests with your assignment.

Good luck!

## Background

BridgePhase is opening up a new convenience store and needs a Point-of-Sale system to control serve as the cash register and inventory system.

## Part 1. Inventory System

Create a class `Inventory` that implements the methods in the `IInventory` interface. An `Inventory` contains a list of `Product`. Each `Product` contains `upc` (`String`), `name` (`String`), `wholesalePrice` (`Number` or subclass), `retailPrice` (`Number` or subclass), and `quantity` (`Number` or subclass).  Add standard getter and setters `getName()`, `setName()`, etc to the `Product` class. 

The `IInventory` interfaces supports the following **public** operations:

1. `void replenish(InputStream is)` - Reads the inventory from a comma separated `InputStream`, each line in the `InputStream` represents a different `Product`. An example input stream looks like this (the header will be part of the input stream as well):
```
upc,name,wholesalePrice,retailPrice,quantity
A123,Apple,0.50,1.00,100
B234,Peach,0.35,0.75,200
C123,Milk,2.15,4.50,40
```

The `replenish()` method will populate the list of `Product` on the `Inventory`.

2. `List<Product> list()` - returns an unmodifiable `List<Product>` representing the `Product` inside the `Inventory`.


* **Note**: Pay attention to what class you'll use to represent prices and quantity...

## Part 2. Cash Register

Create a class called `CashRegister` that accepts an `IInventory` as part of its constructor. The `CashRegister` will be used to calculate the total price of `Product` bought by a user and must therefore provide the following methods: 

1. `void beginTransaction()` - will setup the `CashRegister` to create a new transaction which means that a new customer is checking out

2. `boolean scan(String upc)` - will scan a product based on its UPC (Universal Product Code). Returns **true** when successful.

3. `BigDecimal getTotal()` - returns the total retail price of all goods purchased. This value is a currency.

4. `BigDecimal pay(BigDecimal cashAmount)` - mimics a customer paying for their total and returns the amount of change due to the customer. This is also the end of the transaction.

5. `void printReceipt(OutputStream os)` - prints the receipt in the following format (the format **is** important):

```
BridgePhase Convenience Store
-----------------------------
Total Products Bought: 17

3 Apple @ $1.00: $3.00
2 Milk @ $4.50: $9.00
12 Peach @ $0.75: $9.00
-----------------------------
Total: $21.00
Paid: $25.00
Change: $4.00
-----------------------------
```

## Part 3. Inventory reduction

The `CashRegister` in Part 2 doesn't decrease the amount of items in the inventory. Add the neccessary logic to the `CashRegister` and `Inventory` classes to make sure that when the customer pays for their goods the `Inventory` is updated to reflect the new amounts.

On the `CashRegister`, if an item that is out-of-stock is attempted to be scanned, you should return **false** when scanning.

Allow the `Inventory` to be replenished via the `replenish(InputStream is)` method. When invoked on an existing `Inventory` if an item exists (based on UPC), it should update the `wholesalePrice`, `retailPrice`, `name`, and add to the `quantity` of items. For example, if the current `Inventory` contains:

```
upc,name,wholesalePrice,retailPrice,quantity
A123,Apple,0.50,1.00,100
```
When the `Inventory` is replenished with:
```
upc,name,wholesalePrice,retailPrice,quantity
A123,Apple Red,0.60,1.20,100
```

The final `Inventory` should look like this: 
```
upc,name,wholesalePrice,retailPrice,quantity
A123,Apple Red,0.60,1.20,200
```
