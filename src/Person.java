public class Person {
    private final String name;
    private final String password;

    Person(String name, String password) {
        this.name = name;
        this.password = password;
    }
    String getName() { return name; }
    boolean verifyPassword(String enteredPass) {
        return password.equals(enteredPass);
    }
}
