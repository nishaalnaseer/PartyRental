public class SqlScripts {
    /*
    sql scripts handled separately to avoid writing code prone to sql injection
    idea is to keep all code completely away from raw sql code
     */
    final String createClient =
            "INSERT INTO requested_customer (name, password, email, type)" +
            "VALUES (?, ?, ?, ?)";

    final String checkEmployee =
            "SELECT * FROM party_rental.employee WHERE email = ?";

    final String checkNewClient =
            "SELECT * FROM party_rental.requested_customer WHERE email = ?";

    final String checkClient =
            "SELECT * FROM party_rental.customer WHERE email = ?";

    final String loginEmployee =
            "SELECT * FROM employee WHERE email = ? AND password = ? AND status = ?";

    final String loginClient =
            """
            SELECT * FROM customer\s
            JOIN customer_type ON customer.type = customer_type.id\s
            WHERE email = ? AND password = ?\s
            AND STATUS = ?
            """;

    final String getItems =
            "SELECT * FROM item";

    final String updateItem =
            "UPDATE item SET rate = ?, stock = ?, available = ? WHERE id = ?";

    final String updateItem2 =
            "UPDATE item SET description = ?, rate = ?, stock = ?, available = ? WHERE id = ?";

    final String insertItem =
            "INSERT INTO item (description, rate, created_by, created_on, " +
                    "stock, available, reserved, rented) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    final String getPendingClients =
            "SELECT * FROM requested_customer WHERE status = ?";

    final String getCurrentEmployees =
            "SELECT * FROM employee WHERE role = ? AND status = ?";

    final String getCurrentClients =
            "SELECT * FROM customer WHERE status = ?";

    final String addEmployee =
            "INSERT INTO employee (name, password, email, role, status)" +
                    " VALUES (?, ?, ?, ?, ?)";

    final String searchAllEmployees =
            "SELECT * FROM employee WHERE email = ?";

    final String getEmployees =
            "SELECT * FROM employee";

    final String getAllClients =
            """
              SELECT * FROM customer\s
              JOIN customer_type ON customer.type = customer_type.id\s
            """;

    final String updateEmployeeStatus =
            "UPDATE employee SET status = ? WHERE id = ?";

    final String updateClientStatus =
            "UPDATE customer SET status = ? WHERE id = ?";

    final String checkItem =
            "SELECT * FROM item WHERE description = ?";

    final String getRequestedClients =
            """
                SELECT * FROM requested_customer
                    JOIN customer_type ON requested_customer.type = customer_type.id
                    WHERE status = 'REQUESTED'
            """;

    final String delRequestClient =
            "DELETE FROM requested_customer WHERE id = ?";

    final String insertCustomer =
            "INSERT INTO customer (name, password, email, type, status)" +
                    "VALUES (?, ?, ?, ?, ?)";

    final String getTypeID =
            "SELECT id FROM customer_type WHERE label = ?";

    final String getGstRate =
            "SELECT tax FROM customer_type WHERE label = ?";

    final String insertReservation =
            "INSERT INTO reservation (customer, remarks, reservation_date, " +
                    "rent_date, return_date, gst, subtotal, paid)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    final String insertTransaction =
            "INSERT INTO transaction(amount, reservation_id, card_mm, " +
                    "card_yy, card_sec, card_name) VALUES " +
                    "(?, ?, ?, ?, ?, ?)";

    final String insertReservationItem =
            "INSERT INTO items (reservation_id, item_id, qty) VALUES (?, ?, ?)";

    final String selectReservationID =
            "SELECT id FROM reservation WHERE customer = ? ORDER BY id DESC LIMIT 1";

    final String selectClient =
            "SELECT * FROM customer JOIN customer_type ON " +
            "customer.type = customer_type.id WHERE customer.id = ?";

    final String selectReservationItems =
            "SELECT * FROM items JOIN item on items.item_id = " +
            "item.id WHERE reservation_id = ?";

    final String selectClientReservations =
            "SELECT * FROM reservation WHERE customer = ? and STATUS != 'RETURNED'";

    final String selectReservationsOnStatus =
            "SELECT * FROM reservation WHERE status = ?";

    final String denyReservation =
            "UPDATE reservation SET status = 'DENIED' WHERE id = ?";

    final String cancelReservation =
            "UPDATE reservation SET status = 'RESERVATION_CANCELLED' WHERE id = ?";

    final String approveReservation =
            "UPDATE reservation SET status = 'RESERVED', approved_by = ? WHERE id = ?";

    final String selectReservedReservations =
            "SELECT * FROM reservation WHERE status = 'RESERVED'";

    final String setLeasingCancelled =
            "UPDATE reservation SET status = 'LEASING_CANCELLED' WHERE id = ?";

    final String setRented =
            "UPDATE reservation SET status = 'RENTED', rented_by = ? WHERE id = ?";

    final String setReturned =
            "UPDATE reservation SET status = 'RETURNED', return_accepted_by = ? WHERE id = ?";

    final String selectRentedReservations =
            "SELECT * FROM reservation WHERE status = 'RENTED' AND return_date = CURDATE()";

    final String selectItem =
            "SELECT * FROM item WHERE id = ?";

    final String updateInventory =
            "UPDATE item SET available = ?, reserved = ?, rented = ?" +
                    " WHERE id = ?";

    final String selectRentingReservations =
            "SELECT * FROM reservation WHERE status = 'RESERVED' AND customer = ?";

    final String selectPreviousTransactions =
            "SELECT * FROM transaction WHERE reservation_id = ?";

    final String updateReservationPayment =
            "UPDATE reservation SET paid = ? WHERE id = ?";

    final String reservationsReport =
            "SELECT reservation_date, rent_date, return_date FROM reservation\n" +
                    "WHERE reservation_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH);";

    final String customerTypeReport =
            "SELECT `customer`.`type`, `reservation`.`reservation_date` FROM `reservation`" +
            "INNER JOIN `customer`" +
            "ON `customer`.`id` = `reservation`.`customer`" +
            "WHERE `reservation`.`reservation_date` >= DATE_SUB(NOW(), INTERVAL 6 MONTH);";

    final String selectReturnedReservations =
            "SELECT * FROM reservation WHERE status = 'RETURNED'";

    final String incomeReport =
            "SELECT transaction.amount, customer.`type`, transaction.date FROM transaction\n" +
                    "\n" +
                    "JOIN reservation\n" +
                    "ON reservation.id = transaction.reservation_id\n" +
                    "\n" +
                    "JOIN customer\n" +
                    "ON customer.id = reservation.customer;";
}