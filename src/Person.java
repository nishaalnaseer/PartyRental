public class Person {
    private final String name;
    private String password;
    private final String email;

    Person(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }
    String getName() { return name; }
    boolean verifyPassword(String enteredPass) {
        return password.equals(enteredPass);
    }

    public String getEmail() {
        return email;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
