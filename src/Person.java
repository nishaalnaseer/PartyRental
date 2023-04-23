public class Person {
    private final String name;
    private String password;
    private final String email;

    private AccountStatus status;

    Person(String name, String password, String email, String status) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.status = AccountStatus.valueOf(status);
    }
    String getName() { return name; }
    boolean verifyPassword(String enteredPass) {
        return password.equals(enteredPass);
    }

    public String getEmail() {
        return email;
    }

    void setStatus(String status) {
        this.status = AccountStatus.valueOf(status);
    }

    String getStatus() {
        return status.toString();
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
