abstract public class Product{
    //private fields
    private String name;
    private String id;
    private double unitPrice;
    private int quantity;
    private int reorderLevel;

    //constructor
    public Product(String name, String id, double unitPrice, int quantity, int reorderLevel) {
        this.name = name;
        this.id = id;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    //getters
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

    //setters
    public void setName(String name) {
        this.name = name;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    //other methods
    public double getInventoryValue() {
        return unitPrice * quantity;
    }
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }
    public void addStock(int amount){
        if(amount>0){
            quantity += amount;
        }else{
            System.out.println("Invalid amount. Please enter a positive value.");
            return;
        }
    }
    public void removeStock(int amount){
        if(amount>0){
            if(quantity >= amount){
                quantity -= amount;
            }else{
                System.out.println("Insufficient stock. Cannot remove more than available.");
                return;
            }
        }else{
            System.out.println("Invalid amount. Please enter a positive value.");
            return;
        }
    }
    public abstract String getCategory();
    public abstract String getDetails();
    public abstract String getHandlingNote();
    public abstract void updateSpecificField(String[] field);
    
}

class PetFood extends Product{
    private String targetAnimal;
    private int weightKg;

    PetFood(String name, String id, double unitPrice, int quantity, int reorderLevel, String targetAnimal, int weightKg) {
        super(name, id, unitPrice, quantity, reorderLevel);
        this.targetAnimal = targetAnimal;
        this.weightKg = weightKg;
    }
    public String getTargetAnimal() {
        return targetAnimal;
    }
    public int getWeight() {
        return weightKg;
    }
    public void setTargetAnimal(String targetAnimal) {
        this.targetAnimal = targetAnimal;
    }
    public void setWeight(int weightKg) {
        if(weightKg>0)
        this.weightKg = weightKg;
    else{
        System.out.println("Invalid weight value. Please enter a positive integer.");
        return; 
        }
    }
    @Override 
    public String getCategory() {
        return "Pet Food";
    }
    @Override 
    public String getDetails(){
        return "Target Animal: " + targetAnimal + ", Weight: " + weightKg + "kg";
    }
    @Override 
    public String getHandlingNote() {
        return "Store in a cool, dry place. Keep away from direct sunlight.";
    }
    @Override 
    public void updateSpecificField(String[] field) {// note: this string array field here will be declared in the inventoryApp file(main file) (based on user input):) //same for all string fields in this file<333
        if(field.length >= 2){
            if(field[0].equalsIgnoreCase("targetAnimal")){//an array field is passed to the method,the first element of the array is checked to see if it matches the string "targetAnimal"(case-insensitive).
            // if it matches, the second element of the array(field[1])is used to update the targetAnimal field of the PetFood object by calling the setTargetAnimal method.
                setTargetAnimal(field[1]); 
            }else if(field[0].equalsIgnoreCase("weightKg")){//same here
                try{
                    int weight = Integer.parseInt(field[1]);
                    setWeight(weight);
                }catch(NumberFormatException e){
                    System.out.println("Invalid weight value. Please enter a valid integer.");
                }
            }else{
                System.out.println("Invalid field name. Please use 'targetAnimal' or 'weightKg'.");
            }
        }else{
            System.out.println("Insufficient data provided for updating specific field.");
        }
    }
}

class PetMedicine extends Product{
    private double dosageMg;
    private String expirationDate;

    //constructor
    PetMedicine(String name, String id, double unitPrice, int quantity, int reorderLevel, double dosageMg, String expirationDate) {
        super(name, id, unitPrice, quantity, reorderLevel);
        this.dosageMg = dosageMg;
        this.expirationDate = expirationDate;
    }
    //getters and setters
    public double getDosage() {
        return dosageMg;
    }
    public String getExpirationDate() {
        return expirationDate;
    }
    public void setDosage(double dosageMg) { 
        if(dosageMg>0){
            this.dosageMg = dosageMg;
        }else{
            System.out.println("Invalid dosage value. Please enter a positive number.");
            return;
        }
    }
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
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
        if(field.length >= 2){
            if(field[0].equalsIgnoreCase("dosageMg")){//same as in pet food method 
                try{
                    dosageMg = Double.parseDouble(field[1]);
                    setDosage(dosageMg);
                }catch(NumberFormatException e){
                    System.out.println("Invalid dosage value. Please enter a valid number.");
                    return;
                }
            }else if(field[0].equalsIgnoreCase("expirationDate")){
                setExpirationDate(field[1]);
            }else{
                System.out.println("Invalid field name. Please use 'dosageMg' or 'expirationDate'.");
            }
        }else{
            System.out.println("Insufficient data provided for updating specific field.");
        }
    }

class PetToy extends Product{
    private int durability;//1 to 5, 1 is lowest and 5 is highest

    PetToy(String name, String id, double unitPrice, int quantity, int reorderLevel, int durability) {
        super(name, id, unitPrice, quantity, reorderLevel);
        this.durability = durability;
    }
    public int getDurability() {
        return durability;
    }
    public void setDurability(int durability) {
        if(durability>0 && durability<=5){
            this.durability = durability;
        }else{
            System.out.println("Invalid durability value. Please enter an integer between 1 and 5.");
        }
    }

    //overriding abstract methods
    @Override
    public String getCategory(){
        return "PetToy";
    }
    @Override
    public String getDetails(){
        return "Durability: " + durability;
    }
    @Override 
    public String getHandlingNote(){
        return "Inspect regularly for wear and tear. Replace if damaged.";
    }
    @Override
    public void updateSpecificField(String[] field) {
        if(field.length >= 2){
            if(field[0].equalsIgnoreCase("durability")){//same case in petfood and petmedicine method
                try{
                    durability = Integer.parseInt(field[1]);//setting durability here
                    setDurability(durability);
                }catch(NumberFormatException e){
                    System.out.println("Invalid durability value. Please enter a valid integer.");
                    return;
                }
            }else{
                System.out.println("Invalid field name. Please use 'durability'.");
            }
        }else{
            System.out.println("Insufficient data provided for updating specific field.");
        }
    }

    
}

class PetAccessory extends Product{
    private String material;//collars, leashes, cages, bowls

    PetAccessory(String name, String id, double unitPrice, int quantity, int reorderLevel, String material) {
        super(name, id, unitPrice, quantity, reorderLevel);
        this.material = material;
    }
    public String getMaterial() {
        return material;
    }
    public void setMaterial(String material) {
        this.material = material;
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
        if(field.length >= 2){
            if(field[0].equalsIgnoreCase("material")){//same case in petfood and petmedicine method
                setMaterial(field[1]);
            }else{
                System.out.println("Invalid field name. Please use 'material'.");
            }
        }else{
            System.out.println("Insufficient data provided for updating specific field.");
        }
    }
    
}
}