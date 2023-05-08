public class SqlScripts {
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
                    "rent_date, return_date) VALUES (?, ?, ?, ?, ?)";

    final String insertTransaction =
            "INSERT INTO transaction(amount, reservation_id, card_mm, " +
                    "card_yy, card_sec, card_name) VALUES " +
                    "(?, ?, ?, ?, ?, ?)";

    final String insertReservationItem =
            "INSERT INTO items (reservation_id, item_id, qty) VALUES (?, ?, ?)";

    final String selectReservationID =
            "SELECT id FROM reservation WHERE customer = ? ORDER BY id DESC LIMIT 1";
}