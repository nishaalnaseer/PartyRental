public class Customer extends Person {
    private final int clientId;
    private final CustomerType type;

    Customer(String name, String password, int id, String type) {
        super(name, password);
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
