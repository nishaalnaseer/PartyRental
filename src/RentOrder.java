public class RentOrder {
    private final Reservation reservation;
    private final int rentOrderId;
    private RentStatus status;

    public RentOrder(Reservation reservation, int rentOrderId, String status) {
        this.reservation = reservation;
        this.rentOrderId = rentOrderId;
        setStatus(status);
    }

    public int getRentOrderId() {
        return rentOrderId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    void makePayement() {

    }

    String getStatus() {
        return status.toString();
    }
    void setStatus(String status) {
        this.status = RentStatus.valueOf(status);
    }
}
