import java.util.Date;

public class Item {
    private final int id;
    private final int createdBy;
    private final Date date;
    private String description;
    private float rate;

    Item(int id, int createdBy, Date date, String description, float rate) {
        this.id = id;
        this.createdBy = createdBy;
        this.date = date;
        this.description = description;
        this.rate = rate;
    }

    int getId() { return id; }
    Date getDate() { return date; }
    int getCreatedBy() { return createdBy; }
    String getDescription() { return description; }
    float getRate() { return rate; }
    void setRate(float newRate) { rate = newRate; }
    void setDescription(String description) { this.description = description; }
}
