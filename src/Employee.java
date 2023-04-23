public class Employee extends Person {
    private final int id;
    private final Role role;
    Employee(String name, String password, int id, String role,
             String email, String status) {
        super(name, password, email, status);
        this.id = id;
        this.role = Role.valueOf(role);
    }

    int getId() {
        return id;
    }

    String getRole() {
        return role.toString();
    }
}
