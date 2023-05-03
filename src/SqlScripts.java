public class SqlScripts {
    final String createClient =
            "INSERT INTO party_rental.requested_customer (name, password, email, type)" +
            "VALUES (?, ?, ?, ?)";

    final String checkEmployee =
            "SELECT * FROM party_rental.employee WHERE email = ?";

    final String checkNewClient =
            "SELECT * FROM party_rental.requested_customer WHERE email = ?";

    final String checkClient =
            "SELECT * FROM party_rental.customer WHERE email = ?";

    final String getEmployee =
            "SELECT * FROM employee WHERE email = ? AND password = ?";

    final String getClient =
            "SELECT * FROM customer WHERE email = ? AND password = ?";

    final String getItems =
            "SELECT * FROM item";

}