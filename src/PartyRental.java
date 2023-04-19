import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PartyRental {

    private String username;

    private final JFrame mainFrame = new JFrame("Party Rentals");
    private final Navigator navigator = new Navigator(mainFrame);


    PartyRental() {
        mainFrame.setSize(800, 700);
//        mainFrame.minimumSize(new Dimension(700, 600));
        mainFrame.setMinimumSize(new Dimension(700, 700));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPage();

        mainFrame.setVisible(true);
    }

    void loginPage() {
//        JPanel panel = new JPanel();
//        JPanel container = new JPanel();
//
//        Border border = BorderFactory.createLineBorder(Color.BLACK);
//        container.setBorder(border);
//        container.setPreferredSize(new Dimension(400, 500));
//
//        // Use a BorderLayout to center the panel within the JFrame
//        container.setLayout(new BorderLayout());
//        panel.add(container, BorderLayout.CENTER);
//
//        System.out.println(x);

        JPanel panel = new JPanel(new GridBagLayout());
        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;

        // Set the padding as the container's border
        container.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        container.setPreferredSize(new Dimension(400, 500));
        panel.add(container, gbc);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;

        JLabel label = new JLabel("Sign In");
        JTextField usernameField = new JTextField("Username");
        JPasswordField passwordField = new JPasswordField("Password");
        JButton loginButton = new JButton("Login");
        JButton createAccount = new JButton("Create Account");

//        loginButton.setPreferredSize(new Dimension(50, loginButton.getPreferredSize().height));

        label.setHorizontalAlignment(JLabel.CENTER);

        container.add(label);
        gbc2.gridy = 1;
        container.add(usernameField, gbc2);
        gbc2.gridy = 2;
        container.add(passwordField, gbc2);
        gbc2.gridy = 3;
        container.add(loginButton, gbc2);
        gbc2.gridy = 4;
        container.add(createAccount, gbc2);

        navigator.open(panel);
    }

    public static void main(String[] args) {
        new PartyRental();
    }
}