import java.util.Date;

public class Item {
    private final int id;
    private final int createdBy;
    private final Date date;
    private String description;
    private float rate;
    private int stock, available, reserved, rented;

    Item(
            int id, String description, float rate, int createdBy,
            Date date, int stock, int available, int reserved, int rented
    ) {
        this.id = id;
        this.createdBy = createdBy;
        this.date = date;
        this.description = description;
        this.rate = rate;
        this.stock = stock;
        this.available = available;
        this.reserved = reserved;
        this.rented = rented;
    }

    int getId() { return id; }
    Date getDate() { return date; }
    int getCreatedBy() { return createdBy; }
    String getDescription() { return description; }
    float getRate() { return rate; }
    void setRate(float newRate) { rate = newRate; }
    void setDescription(String description) { this.description = description; }
    void adjustAvailable(int amount) {
        if(available + amount < 0) {
            throw new BelowZeroError("Available can't go below Zero");
        } else {
            available+=amount;
        }
    }
    void adjustRented(int amount) {
        if(rented + amount < 0) {
            throw new BelowZeroError("Rented can't go below Zero");
        } else {
            rented+=amount;
        }
    }
    void adjustReserved(int amount) {
        if(reserved + amount < 0) {
            throw new BelowZeroError("Reserved can't go below Zero");
        } else {
            reserved+=amount;
        }
    }
    void adjustStock(int amount) {
        if(stock + amount < 0) {
            throw new BelowZeroError("Stock can't go below Zero");
        } else {
            stock+=amount;
        }
    }
    int getStock() {return stock;}
    int getAvailable() {return available;}
    int getRented() {return rented;}
    int getReserved() {return reserved;}
}
