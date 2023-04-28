public enum ReservationStatus {
    REQUESTED, // when a customer makes reservation
    DENIED, // when an employee deletes it
    RESERVATION_CANCELLED, // when customer cancels before approval it
    RESERVED, // employee approves
    LEASING_CANCELLED, // when customer cancels after approval
    RENTED, // customer picks it up, updated by employee
    RETURNED, // customer returns
}