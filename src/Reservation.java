import java.util.ArrayList;
import java.util.Date;

public class Reservation {
    private final int reservationId;
    private final int customerId;
    private final ArrayList<Item> items;
    private String remarks;
    private final Date reservationDate;
    private Date rentDate;
    private Date returnDate;
    private String internalRemarks;

    private float subtotal = 0;
    private float gst = 0;

    public Reservation(int reservationId, int customerId, ArrayList<Item> items,
                       String remarks, Date reservationDate, Date rentDate,
                       Date returnDate) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.items = items;
        this.remarks = remarks;
        this.reservationDate = reservationDate;
        this.rentDate = rentDate;
        this.returnDate = returnDate;
    }

    float getTotal() {
        return gst + subtotal;
    }

    float getGst() { return gst; }

    public int getReservationId() {
        return reservationId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public String getInternalRemarks() {
        return internalRemarks;
    }

    public void setInternalRemarks(String internalRemarks) {
        this.internalRemarks = internalRemarks;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public Date getRentDate() {
        return rentDate;
    }

    public void setRentDate(Date rentDate) {
        this.rentDate = rentDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public float getSubtotal() {
        return subtotal;
    }

    void addItems(Item[] items) {
        for(int x = 0; x < items.length; x++) {
            Item item = items[x];

            float rate = item.getRate();
//            gst+= rate * 0.06;
            this.items.add(item);
            subtotal+=rate;
        }
    }
}
