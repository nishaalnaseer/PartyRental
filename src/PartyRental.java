import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class PartyRental {

    private String username;

    private final JFrame mainFrame = new JFrame("Party Rentals");
    private final Navigator navigator = new Navigator(mainFrame);
    private CardLayout cardLayout;


    PartyRental() {
        mainFrame.setSize(800, 700);
//        mainFrame.minimumSize(new Dimension(700, 600));
        mainFrame.setMinimumSize(new Dimension(700, 700));
        cardLayout = new CardLayout();
        mainFrame.getContentPane().setLayout(cardLayout);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPage();

        mainFrame.setVisible(true);
    }

    void loginPage() {

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

        createAccount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                customerAccountCreation();
            }
        });

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

        navigator.open(panel, cardLayout, "login");
    }

    void customerAccountCreation() {
        JPanel panel = new JPanel();



        navigator.open(panel, cardLayout, "createAccount");
    }

    public static void main(String[] args) {
        new PartyRental();
    }
}