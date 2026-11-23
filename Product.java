import java.time.LocalDate;
import java.time.format.DateTimeParseException;

//abstract class
public abstract class Product {
    // private fields
    private String name;
    private String id;
    private double unitPrice;
    private int quantity;
    private int reorderLevel;

    // constructor: calls the setters so the validation rules live in one place
    public Product(String name, String id, double unitPrice, int quantity, int reorderLevel) {
        setName(name);
        setId(id);
        setUnitPrice(unitPrice);
        setQuantity(quantity);
        setReorderLevel(reorderLevel);
    }

    // getters
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    public double getUnitPrice() {
        return unitPrice;
    }
    public int getQuantity() {
        return quantity;
    }
    public int getReorderLevel() {
        return reorderLevel;
    }

    // setters (final, because the constructor calls them)
    public final void setName(String name) {
        this.name = requireText(name, "Name");
    }
    public final void setId(String id) {
        this.id = requireText(id, "ID");
    }
    public final void setUnitPrice(double unitPrice) {
        if (Double.isNaN(unitPrice) || Double.isInfinite(unitPrice) || unitPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.unitPrice = unitPrice;
    }
    public final void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity = quantity;
    }
    public final void setReorderLevel(int reorderLevel) {
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative.");
        }
        this.reorderLevel = reorderLevel;
    }

    // shared logic
    public double getInventoryValue() {
        return unitPrice * quantity;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive number.");
        }
        if ((long) quantity + amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("That amount is too large.");
        }
        quantity += amount;
    }

    public void removeStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive number.");
        }
        if (amount > quantity) {
            throw new IllegalArgumentException(
                "Insufficient stock. Cannot remove " + amount + " (available: " + quantity + ").");
        }
        quantity -= amount;
    }

    // abstract methods: every subclass writes its own version (polymorphism)
    public abstract String getCategory();
    public abstract String getDetails();
    public abstract String getHandlingNote();

    // field = { fieldName, newValue }, built in InventoryApp from the user's input
    public abstract void updateSpecificField(String[] field);

    //small helpers shared by the subclasses
    // returns the trimmed text, or throws if it is null or empty
    protected static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        return value.trim();
    }

    // checks that updateSpecificField received both a field name and a value
    protected static void requireFieldPair(String[] field) {
        if (field == null || field.length < 2 || field[0] == null || field[1] == null) {
            throw new IllegalArgumentException("Please provide both the field name and the new value.");
        }
    }

    // converts text to a decimal number, or throws a clear error
    protected static double parseNumber(String text, String label) {
        try {
            double value = Double.parseDouble(text.trim());
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    // converts text to a whole number, or throws a clear error
    protected static int parseWhole(String text, String label) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }
}

// PetFood
class PetFood extends Product {
    private String targetAnimal;
    private double weightKg;

    public PetFood(String name, String id, double unitPrice, int quantity, int reorderLevel,String targetAnimal, double weightKg) {
        super(name, id, unitPrice, quantity, reorderLevel);
        setTargetAnimal(targetAnimal);
        setWeightKg(weightKg);
    }

    public String getTargetAnimal() {
        return targetAnimal;
    }
    public double getWeightKg() {
        return weightKg;
    }

    public void setTargetAnimal(String targetAnimal) {
        this.targetAnimal = requireText(targetAnimal, "Target animal");
    }
    public void setWeightKg(double weightKg) {
        if (Double.isNaN(weightKg) || Double.isInfinite(weightKg) || weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero.");
        }
        this.weightKg = weightKg;
    }

    @Override
    public String getCategory() {
        return "Pet Food";
    }
    @Override
    public String getDetails() {
        return "Target Animal: " + targetAnimal + ", Weight: " + weightKg + "kg";
    }
    @Override
    public String getHandlingNote() {
        return "Store in a cool, dry place. Keep away from direct sunlight.";
    }
    @Override
    public void updateSpecificField(String[] field) {
        requireFieldPair(field);
        String fieldName = field[0].trim();
        String value = field[1];

        if (fieldName.equalsIgnoreCase("targetAnimal")) {
            setTargetAnimal(value);
        } else if (fieldName.equalsIgnoreCase("weightKg")) {
            setWeightKg(parseNumber(value, "Weight"));
        } else {
            throw new IllegalArgumentException(
                "Invalid field name '" + fieldName + "'. Use 'targetAnimal' or 'weightKg'.");
        }
    }
}

// PetMedicine
class PetMedicine extends Product {
    private double dosageMg;
    private String expirationDate;   // yyyy-MM-dd

    public PetMedicine(String name, String id, double unitPrice, int quantity, int reorderLevel,double dosageMg, String expirationDate) {
        super(name, id, unitPrice, quantity, reorderLevel);
        setDosageMg(dosageMg);
        setExpirationDate(expirationDate);
    }

    public double getDosageMg() {
        return dosageMg;
    }
    public String getExpirationDate() {
        return expirationDate;
    }

    public void setDosageMg(double dosageMg) {
        if (Double.isNaN(dosageMg) || Double.isInfinite(dosageMg) || dosageMg <= 0) {
            throw new IllegalArgumentException("Dosage must be greater than zero.");
        }
        this.dosageMg = dosageMg;
    }
    public void setExpirationDate(String expirationDate) {
        String text = requireText(expirationDate, "Expiration date");
        try {
            LocalDate.parse(text);   // throws if it is not a real yyyy-MM-dd date
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Expiration date must be a valid date in yyyy-MM-dd format (for example 2027-06-30).");
        }
        this.expirationDate = text;
    }

    @Override
    public String getCategory() {
        return "Pet Medicine";
    }
    @Override
    public String getDetails() {
        return "Dosage: " + dosageMg + "mg, Expiration Date: " + expirationDate;
    }
    @Override
    public String getHandlingNote() {
        return "Store in a cool, dry place. Keep away from direct sunlight. Follow dosage instructions carefully.";
    }
    @Override
    public void updateSpecificField(String[] field) {
        requireFieldPair(field);
        String fieldName = field[0].trim();
        String value = field[1];

        if (fieldName.equalsIgnoreCase("dosageMg")) {
            setDosageMg(parseNumber(value, "Dosage"));
        } else if (fieldName.equalsIgnoreCase("expirationDate")) {
            setExpirationDate(value);
        } else {
            throw new IllegalArgumentException(
                "Invalid field name '" + fieldName + "'. Use 'dosageMg' or 'expirationDate'.");
        }
    }
}

// PetToy
class PetToy extends Product {
    private int durability;   // 1 to 5, 1 is lowest and 5 is highest

    public PetToy(String name, String id, double unitPrice, int quantity, int reorderLevel,int durability) {
        super(name, id, unitPrice, quantity, reorderLevel);
        setDurability(durability);
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        if (durability < 1 || durability > 5) {
            throw new IllegalArgumentException("Durability must be a whole number from 1 to 5.");
        }
        this.durability = durability;
    }

    @Override
    public String getCategory() {
        return "Pet Toy";
    }
    @Override
    public String getDetails() {
        return "Durability: " + durability;
    }
    @Override
    public String getHandlingNote() {
        return "Inspect regularly for wear and tear. Replace if damaged.";
    }
    @Override
    public void updateSpecificField(String[] field) {
        requireFieldPair(field);
        String fieldName = field[0].trim();
        String value = field[1];

        if (fieldName.equalsIgnoreCase("durability")) {
            setDurability(parseWhole(value, "Durability"));
        } else {
            throw new IllegalArgumentException(
                "Invalid field name '" + fieldName + "'. Use 'durability'.");
        }
    }
}

// PetAccessory
class PetAccessory extends Product {
    private String material;   // collars, leashes, cages, bowls

    public PetAccessory(String name, String id, double unitPrice, int quantity, int reorderLevel,
                        String material) {
        super(name, id, unitPrice, quantity, reorderLevel);
        setMaterial(material);
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = requireText(material, "Material");
    }

    @Override
    public String getCategory() {
        return "Pet Accessory";
    }
    @Override
    public String getDetails() {
        return "Material: " + material;
    }
    @Override
    public String getHandlingNote() {
        return "Handle with care.";
    }
    @Override
    public void updateSpecificField(String[] field) {
        requireFieldPair(field);
        String fieldName = field[0].trim();
        String value = field[1];

        if (fieldName.equalsIgnoreCase("material")) {
            setMaterial(value);
        } else {
            throw new IllegalArgumentException(
                "Invalid field name '" + fieldName + "'. Use 'material'.");
        }
    }
}
