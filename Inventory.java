import java.util.Arrays;

public class Inventory {
    //Transaction types
    private static final String TYPE_ADD = "ADD";
    private static final String TYPE_UPDATE = "UPDATE";
    private static final String TYPE_DELETE = "DELETE";
    private static final String TYPE_STOCK_IN = "STOCK_IN";
    private static final String TYPE_STOCK_OUT = "STOCK_OUT";

    //Sort modes
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_PRICE = 2;
    public static final int SORT_BY_QUANTITY = 3;

    //fields
    private Product[] products;
    private StockTransaction[] transactions;
    private int productCount, transactionCount;

    //constructor
    public Inventory() {
        products = new Product[50];
        productCount = 0;
        transactions = new StockTransaction[500];
        transactionCount = 0;
        loadSampleData();
    }

    //addProduct
    public void addProduct(Product product){
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if(isFull()){
            throw new IllegalArgumentException("Inventory is full (maximum " + 50 + " products).");
        }
        if(findIndexById(product.getId()) != -1){
            throw new IllegalArgumentException("A product with ID '" + product.getId() + "' already exists.");
        }
        products[productCount] = product;
        productCount++;
        recordTransaction(TYPE_ADD, product.getId(), product.getQuantity());
    }

    //find by id
    public Product findById(String id){
        int index = findIndexById(id);
        if (index == -1){
            return null;
        }
        return products[index];
    }

    //case insensitive, partial name search
    public Product[] searchByName(String keyword){
        if (keyword ==null || keyword.trim().isEmpty()){//returns empty array
            return new Product[0];
        }
        String key= keyword.trim().toLowerCase();

        Product[] matches = new Product[productCount];
        int found= 0;
        for(int i = 0; i<productCount; i++){
            if(products[i].getName().toLowerCase().contains(key)){//dito yung partial name search
                matches[found]= products[i];
                found++;
            }
        }
        return Arrays.copyOf(matches, found);
    }

    //update product- name, price, reorder level, specific fields, records transactions
    //if there is error, product details remains unchanged
    public Product updateProduct(String id, String name, double price, int reorderLevel, String[] specificField){
        Product product = findById(id);
        if(product == null){
            throw new IllegalArgumentException("Product '" + id + "' was not found.");
        }

        //old values, so nothing changes when there is error in the update
        String oldName = product.getName();
        double oldPrice = product.getUnitPrice();
        int oldReorderLevel = product.getReorderLevel();

        try{
            product.setName(name);
            product.setUnitPrice(price);
            product.setReorderLevel(reorderLevel);
            //subclass validates and stores its own field
            product.updateSpecificField(specificField);//last, so if it fails, the category field will never changed
        }catch(IllegalArgumentException e){
            product.setName(oldName);
            product.setUnitPrice(oldPrice);
            product.setReorderLevel(oldReorderLevel);
            throw e;
        }
        recordTransaction(TYPE_UPDATE, product.getId(), 0);
        return product;
    }

    //delete a product, return false when product not found and returns true when deleted
    //shifts array to left and records delete transaction 
    public boolean deleteProduct(String id){
        int index= findIndexById(id);
        if(index == -1){//if product is empty(no products)
            return false;
        }

        //saving old/deleted datas
        String deletedId= products[index].getId();
        int deletedQuantity= products[index].getQuantity();

        //closing the gap by shifting everything after it one slot to the left.
        for(int i = index; i < productCount - 1; i++){
            products[i]= products[i + 1];
        }
        products[productCount - 1]= null;//clearing the last used slot
        productCount--;

        recordTransaction(TYPE_DELETE, deletedId, deletedQuantity);
        return true;//return true when produc tis deleted successfully
    }

    //stocks
    //adds stock to a product. Throws if not found or the amount is invalid. 
    public void stockIn(String id, int amount){
        Product product = findById(id);
        if(product == null){
            throw new IllegalArgumentException("Product '" + id + "' was not found.");
        }
        product.addStock(amount);   // throws if amount <= 0
        recordTransaction(TYPE_STOCK_IN, product.getId(), amount);
    }
    //remove stock, throws if not found, or if the amount is invalid, or amount is larger than available quantity
    //return true if this stock-out left the product at or below its reorder level, so InventoryApp knows to show the low-stock warning
    public boolean stockOut(String id, int amount){
        Product product = findById(id);
        if(product == null){
            throw new IllegalArgumentException("Product '" + id + "' was not found.");
        }
        product.removeStock(amount); // throws if amount <= 0 or too large
        recordTransaction(TYPE_STOCK_OUT, product.getId(), amount);
        return product.isLowStock();
    }

    //view
    // Only the used part of the array, so there are no null slots.
    public Product[] getAllProducts(){
        return Arrays.copyOf(products, productCount);
    }

    //Every product at or below its reorder level. Empty array if none.
    public Product[] getLowStockProducts(){
        Product[] low = new Product[productCount];
        int found = 0;
        for(int i = 0; i < productCount; i++){
            if(products[i].isLowStock()){
                low[found]= products[i];
                found++;
            }
        }
        return Arrays.copyOf(low, found);
    }

    //Only the used part of the history
    public StockTransaction[] getTransactions(){
        return Arrays.copyOf(transactions, transactionCount);
    }

    // SORTING
    //sorts copy of the products  (the original order is not disturbed)// using selection sort.
    //param mode :SORT_BY_NAME, SORT_BY_PRICE, or SORT_BY_QUANTITY
    //param ascending: true for ascending, false for descending
    public Product[] sortProducts(int mode, boolean ascending){
        if(mode < SORT_BY_NAME || mode > SORT_BY_QUANTITY){
            throw new IllegalArgumentException("Invalid sort mode: " + mode);
        }

        Product[] sorted = getAllProducts(); // already a copy

        for(int i = 0; i < sorted.length - 1; i++){
            int best = i; // position of the smallest (or largest) so far
            for(int j = i + 1; j < sorted.length; j++){
                int result= compare(sorted[j], sorted[best], mode);
                boolean isBetter= ascending ? (result < 0) : (result > 0);
                if(isBetter){
                    best = j;
                }
            }
            Product temp= sorted[i];
            sorted[i]= sorted[best];
            sorted[best]= temp;
        }
        return sorted;
    }

    // Negative, zero, or positive, like a normal comparison
    private int compare(Product a, Product b, int mode){
        switch (mode) {
            case SORT_BY_NAME:
                return a.getName().compareToIgnoreCase(b.getName());
            case SORT_BY_PRICE:
                return Double.compare(a.getUnitPrice(), b.getUnitPrice());
            case SORT_BY_QUANTITY:
                return Integer.compare(a.getQuantity(), b.getQuantity());
            default:
                throw new IllegalArgumentException("Invalid sort mode: " + mode);
        }
    }

    //reports
    public int getTotalProducts(){
        return productCount;
    }

    public int getTotalUnits(){
        int total = 0;
        for(int i = 0; i < productCount; i++){
            total += products[i].getQuantity();
        }
        return total;
    }

    //Total inventory value, calculated with the recursive method
    public double getTotalValue(){
        return totalValueRecursive(0);
    }

    //The distinct category labels currently in stock (for example Food, Toy, Medicine), built from getCategory(). Grouping by this label only labels the report; it does not decide any behavior.
    public String[] getCategoryNames(){
        String[] names = new String[productCount];
        int found = 0;

        for(int i = 0; i < productCount; i++){
            String category= products[i].getCategory();

            boolean alreadyListed= false;
            for(int j = 0; j < found; j++){
                if(names[j].equalsIgnoreCase(category)){
                    alreadyListed = true;
                    break;
                }
            }
            if(!alreadyListed){
                names[found] = category;
                found++;
            }
        }
        return Arrays.copyOf(names, found);
    }

    public int countByCategory(String category){
        int count = 0;
        for(int i = 0; i < productCount; i++){
            if(products[i].getCategory().equalsIgnoreCase(category)){
                count++;
            }
        }
        return count;
    }

    public double valueByCategory(String category){
        double total = 0;
        for(int i = 0; i < productCount; i++){
            if(products[i].getCategory().equalsIgnoreCase(category)){
                total += products[i].getInventoryValue();
            }
        }
        return total;
    }

    // RECURSION
    /**
     * Total inventory value from position 'index' to the end of the products.
     * Base case: index has passed the last product, so there is nothing left (0).
     * Recursive case: this product's value + the total of all the rest.
     * Call it as totalValueRecursive(0) to get the whole inventory value.
     */
    public double totalValueRecursive(int index){
        if (index >= productCount) { // base case
            return 0;
        }
        return products[index].getInventoryValue() + totalValueRecursive(index + 1); // recursive case
    }

    /**
     * Optional second recursive method: searches for an ID starting at 'index'.
     * Base case: past the last product, so return null (not found).
     * Call it as findByIdRecursive(id, 0).
     */
    public Product findByIdRecursive(String id, int index){
        if(id == null || index >= productCount){ // base case: not found
            return null;
        }
        if(products[index].getId().equalsIgnoreCase(id.trim())){
            return products[index];// base case: found
        }
        return findByIdRecursive(id, index + 1); // recursive case
    }

    // INTERNAL HELPERS (private)
    /**
     * Creates and stores one StockTransaction. Called as the LAST step of
     * addProduct, updateProduct, deleteProduct, stockIn, and stockOut, so a
     * failed action never creates a record. Grows the array if it is full.
     */
    private void recordTransaction(String type, String productId, int quantity){
        if (transactionCount == transactions.length){
            transactions = Arrays.copyOf(transactions, transactions.length * 2);
        }
        transactions[transactionCount] = new StockTransaction(transactionCount + 1, type, productId, quantity);
        transactionCount++;
    }

    //Array position of the product with this ID, or -1 if not found
    private int findIndexById(String id){
        if(id == null){
            return -1;
        }
        String wanted = id.trim();
        for(int i = 0; i < productCount; i++){
            if(products[i].getId().equalsIgnoreCase(wanted)){
                return i;
            }
        }
        return -1;
    }

    private boolean isFull(){
        return productCount== products.length;
    }

    /**
     * Preloads 8 sample products across all categories so the demo can begin
     * immediately. F002, T002, and M003 start at or below their reorder level
     * so the low-stock alert can be shown right away.
     *
     * These go through addProduct, so each one is also recorded as an ADD
     * transaction (the history is not empty at startup).
     *
     * NOTE: the constructor arguments must match your subclasses. If your
     * PetMedicine has no expiryDate, remove the last argument of those lines.
     */
    private void loadSampleData(){
        // Food: id, name, price, quantity, reorderLevel, targetAnimal, weightKg
        addProduct(new PetFood("F001", "Dog Kibble", 850.00, 20, 5, "Dog", 5.0));
        addProduct(new PetFood("F002", "Cat Pouch Pack", 120.00, 4, 5, "Cat", 1.0));
        addProduct(new PetFood("F003", "Fish Flakes", 95.00, 15, 3, "Fish", 0.2));

        // Toys: name, id, price, quantity, reorderLevel, durability
        addProduct(new PetToy("Rope Chew Toy", "T001", 150.00, 12, 4, 5));
        addProduct(new PetToy("Squeaky Ball", "T002", 80.00, 3, 4, 3));

        // Medicine: id, name, price, quantity, reorderLevel, dosage, expiryDate
        addProduct(new PetMedicine("M001", "Flea Drops", 320.00, 8, 3, 2 , "2027-06-30"));
        addProduct(new PetMedicine("M002", "Dewormer Syrup", 275.00, 10, 4, 5, "2027-03-15"));
        addProduct(new PetMedicine("M003", "Ear Cleaner", 210.00, 2, 3, 50, "2026-12-31"));
    }
}