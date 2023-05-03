public class Employee extends Person {
    private final int id;
    private final Role role;
    Employee(int id, String name, String password, String email, String role, String status) {
        super(name, password, email, status);
        this.id = id;
        this.role = Role.valueOf(role);
    }

    int getId() {
        return id;
    }

    Role getRole() {
        return role;
    }
}
