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

    final String getAllEmployees =
            "SELECT * FROM employee WHERE email = ?";
}