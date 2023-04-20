public class Customer extends Person {
    private final int clientId;
    private final CustomerType type;

    Customer(String name, String password, int id, String type, String email) {
        super(name, password, email);
        this.clientId = id;
        this.type = CustomerType.valueOf(type);
    }

    int getClientId() {
        return clientId;
    }

    String getType() {
        return type.toString();
    }
}
