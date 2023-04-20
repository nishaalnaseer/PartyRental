import javax.swing.*;
import java.awt.*;


public class PartyRental {

    private final JFrame mainFrame = new JFrame("Party Rentals");
    private final CardLayout cardLayout = new CardLayout();
    private final Navigator navigator = new Navigator(mainFrame, cardLayout);

    PartyRental() {
        mainFrame.setSize(800, 700);
//        mainFrame.minimumSize(new Dimension(700, 600));
        mainFrame.setMinimumSize(new Dimension(700, 700));
        mainFrame.getContentPane().setLayout(cardLayout);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPage();

        mainFrame.setVisible(true);
    }

    void loginPage() {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel label = new JLabel("Sign In");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField usernameField = new JTextField("Username");
        JPasswordField passwordField = new JPasswordField("Password");
        JButton loginButton = new JButton("Login");
        JButton createAccount = new JButton("Create Account");

        createAccount.addActionListener(e -> customerAccountCreation());
        loginButton.addActionListener(e -> customerPage());

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {label, usernameField, passwordField, loginButton, createAccount};
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "login");
    }

    void customerAccountCreation() {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel label = new JLabel("Account Creation Request");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField name = new JTextField("Name");
        JPasswordField passwordField = new JPasswordField("Password");
        JComboBox<CustomerType> type = new JComboBox<>(CustomerType.values());
        JButton submit = new JButton("Send Request");
        JButton back = new JButton("Back");

        back.addActionListener(e -> navigator.close());

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {label, name, passwordField, type, submit, back};
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    void officerPage() {

    }

    void customerPage() {
        JPanel panel = new JPanel();
        JButton makeReservationButton = new JButton("Make Reservation");
        JButton editReservationButton = new JButton("Edit Reservation");
        JButton cancelReservationButton = new JButton("Cancel Reservation");

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {makeReservationButton, editReservationButton, cancelReservationButton, };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        makeReservationButton.addActionListener(e -> createReservation());
        cancelReservationButton.addActionListener(e -> cancelReservation());

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    void createReservation() {

    }

    void cancelReservation() {

    }

    public static void main(String[] args) {
        new PartyRental();
    }
}