public class SqlScripts {
    final String createClient =
            "INSERT INTO requested_customer (name, password, email, type)" +
            "VALUES (?, ?, ?, ?)";

    final String getEmployee =
            "SELECT * FROM employee WHERE email = ? AND password = ?";

    final String getNewClient =
            "SELECT * FROM requested_customer WHERE email = ? AND password = ?";

    final String getClient =
            "SELECT * FROM customer WHERE email = ? AND password = ?";
}
