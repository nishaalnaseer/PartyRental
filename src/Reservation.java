import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;

public class Reservation {
    private final int reservationId;
    private final Customer customer;
    private final HashMap<String, Integer> items;
    private String remarks;
    private final Date reservationDate;
    private Date rentDate;
    private Date returnDate;
    private ReservationStatus status;
    private String internalRemarks = "";

    private final float subtotal;
    private final float gst;
    private long days;
    private float paid = 0;
    private int approvedBy;
    private int rentedBy;
    private int returnAcceptedBy;

    public Reservation(int reservationId, Customer customer, HashMap<String, Integer> items,
                       String remarks, Date reservationDate, Date rentDate,
                       Date returnDate, float gst, float subtotal) {
        this.reservationId = reservationId;
        this.items = items;
        this.remarks = remarks;
        this.reservationDate = reservationDate;
        this.rentDate = rentDate;
        this.returnDate = returnDate;
        this.status = ReservationStatus.valueOf("REQUESTED");
        LocalDate date1 = rentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date2 = returnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//                           start    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.days = ChronoUnit.DAYS.between(date1, date2);
        this.customer = customer;
        this.subtotal = subtotal;
        this.gst = gst;
    }

    float getTotal() {
        return gst + subtotal;
    }

    float getGst() { return gst; }

    public int getReservationId() {
        return reservationId;
    }
    public HashMap<String, Integer> getItems() {
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

    public Date getReturnDate() {
        return returnDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    String getStatus() {
        return status.toString();
    }

    void setStatus(String status) {
        this.status = ReservationStatus.valueOf(status);
    }

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public float getSubTotal() {return subtotal;}

    public float getPaid() {
        return paid;
    }

    public void setPaid(float paid) {
        this.paid = paid;
    }

    public int getApprovedBy() {
        return approvedBy;
    }

    public int getRentedBy() {
        return approvedBy;
    }
    public int getReturnAcceptedBy() {
        return returnAcceptedBy;
    }

    public void setApprovedBy(int id) {
        approvedBy = id;
    }
    public void setRentedBy(int id) {
        rentedBy = id;
    }
    public void setReturnAcceptedBy(int id) {
        returnAcceptedBy = id;
    }
}
