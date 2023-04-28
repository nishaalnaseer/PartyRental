public class ReturnOrder {
    private final Reservation reservation;
    private final int rentOrderId;

    public ReturnOrder(Reservation reservation, int rentOrderId) {
        this.reservation = reservation;
        this.rentOrderId = rentOrderId;
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
        return reservation.getStatus();
    }
    void setStatus(String status) {
        reservation.setStatus(status);
    }
}
