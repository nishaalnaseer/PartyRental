import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Reservation {
    private final int reservationId;
    private final Customer customer;
    private final HashMap<Item, Integer> items = new HashMap<>();
    private String remarks;
    private final Date reservationDate;
    private Date rentDate;
    private Date returnDate;
    private String internalRemarks = "";

    private float subtotal = 0;
    private float gst = 0;

    public Reservation(int reservationId, int customerId, Item[] items,
                       String remarks, Date reservationDate, Date rentDate,
                       Date returnDate) {
        this.reservationId = reservationId;
        addItems(items);
        this.remarks = remarks;
        this.reservationDate = reservationDate;
        this.rentDate = rentDate;
        this.returnDate = returnDate;

        // TODO query the parameters for the following variable from DB
        this.customer = new Customer("String name", "String password",  1, "DOMESTIC", "daw");

    }

    float getTotal() {
        return gst + subtotal;
    }

    float getGst() { return gst; }

    public int getReservationId() {
        return reservationId;
    }
    public HashMap<Item, Integer> getItems() {
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

            int prevQty;
            try {
                prevQty = this.items.get(item);
                prevQty++;
                this.items.put(item, prevQty);
            } catch (NullPointerException ex) {
                this.items.put(item, 1);
            }

            float rate = item.getRate();
//            gst+= rate * 0.06;
            subtotal+=rate;
        }
    }

    public Customer getCustomer() {
        return customer;
    }
}
