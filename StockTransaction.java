import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockTransaction{
    private final int sequenceNumber, quantity;
    private final String type, productId, timeStamp;

    public StockTransaction(int sequenceNumber, String type, String productId, int quantity){
        if (sequenceNumber <= 0)
            throw new IllegalArgumentException("Sequence number must be positive.");
        if (type == null || type.trim().isEmpty())
            throw new IllegalArgumentException("Type cannot be empty.");
        if (productId == null || productId.trim().isEmpty())
            throw new IllegalArgumentException("Product ID cannot be empty.");
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity cannot be negative.");
        this.sequenceNumber = sequenceNumber;
        this.type = type.trim();
        this.productId = productId.trim();
        this.quantity = quantity;
        this.timeStamp = getCurrentTimeStamp();
    }

    private String getCurrentTimeStamp(){//sa inventory class dapat sya, dito muna sya
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(fmt);
    }

    //getters
    public int getSequenceNumber() {
        return sequenceNumber;
    }
    public String getType() {
        return type;
    }
    public String getProductId() {
        return productId;
    }
    public int getQuantity() {
        return quantity;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public String toString() {
    return String.format("#%-4d %-10s %-8s qty:%-5d %s",
        sequenceNumber, type, productId, quantity, timeStamp);
}
} 