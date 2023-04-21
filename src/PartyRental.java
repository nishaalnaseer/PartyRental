import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PartyRental {

    private final JFrame mainFrame = new JFrame("Party Rentals");
    private final CardLayout cardLayout = new CardLayout();
    private final Navigator navigator = new Navigator(mainFrame, cardLayout);

    // TODO the following needs to be queried from DB
    ArrayList<Item> items = new ArrayList<>();


    PartyRental() {
        mainFrame.setSize(800, 700);
//        mainFrame.minimumSize(new Dimension(700, 600));
        mainFrame.setMinimumSize(new Dimension(700, 700));
        mainFrame.getContentPane().setLayout(cardLayout);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPage();

        // TODO the following needs to be queried from DB on runtime not on app init
        items.add(new Item(1, 1, new Date(), "Chair", 50));
        items.add(new Item(1, 1, new Date(), "Tabke", 50));
        items.add(new Item(1, 1, new Date(), "Poopopopo", 50));
        items.add(new Item(1, 1, new Date(), "nooo", 50));
        items.add(new Item(1, 1, new Date(), "this", 50));
;
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
        clearPasswordTextFields(usernameField, passwordField);

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {label, usernameField, passwordField, loginButton, createAccount};
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "login");
    }

    void clearTextField(JTextField textField) {
        textField.addMouseListener(new MouseAdapter() {
            private boolean reset = true;
            @Override
            public void mouseClicked(MouseEvent e) {
                if (reset) {
                    textField.setText("");
                    reset = false;
                }
            }
        });

    }
    void clearPasswordField(JPasswordField passwordField) {
        passwordField.addMouseListener(new MouseAdapter() {
            private boolean reset = true;
            @Override
            public void mouseClicked(MouseEvent e) {
                if (reset) {
                    passwordField.setText("");
                    reset = false;
                }
            }
        });

    }
    void clearPasswordTextFields(JTextField textField, JPasswordField passwordField) {
        clearTextField(textField);
        clearPasswordField(passwordField);
    }

    void clearManyTexts(JTextField[] textFields) {
        for(int x = 0; x < textFields.length; x++) {
            clearTextField(textFields[x]);
        }
    }

    void customerAccountCreation() {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel label = new JLabel("Account Creation Request");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField name = new JTextField("Name");
        JTextField email = new JTextField("Email");
        JPasswordField passwordField = new JPasswordField("Password");
        JComboBox<CustomerType> type = new JComboBox<>(CustomerType.values());
        JButton submit = new JButton("Send Request");
        JButton back = new JButton("Back");

        back.addActionListener(e -> navigator.close());
        clearPasswordTextFields(name, passwordField);
        clearTextField(email);


        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {label, name, email, passwordField, type, submit, back};
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
        JButton viewReservationButton = new JButton("View Reservations");

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {makeReservationButton, viewReservationButton, };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        makeReservationButton.addActionListener(e -> createReservation());
        viewReservationButton.addActionListener(e -> viewReservations());

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    void createReservation() {
        // TODO this code below needs to be queried form DB
        final HashMap<String, Integer> items = new HashMap<>();
        items.put("chair1", 5);
        items.put("table2", 5);
        items.put("chair3", 5);
        items.put("table4", 5);
        items.put("chair5", 5);
        items.put("table6", 5);
        items.put("chair7", 5);
        items.put("table8", 5);
        items.put("chair9", 5);
        items.put("table10", 5);
        items.put("chair11", 5);
        items.put("table12", 5);
        items.put("chair13", 5);
        items.put("table14", 5);
        items.put("chair15", 5);
        items.put("table16", 5);
        items.put("chair17", 5);
        items.put("table18", 5);
        items.put("chair19", 5);
        items.put("table20", 5);
        items.put("chair21", 5);
        items.put("table22", 5);
        items.put("chair23", 5);
        items.put("table24", 5);

        JPanel panel = new JPanel();

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
//        table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//        table.setPreferredSize(new Dimension(width, height));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel nameHeading = new JLabel("Item");
        JLabel qtyHeading = new JLabel("Qty");
        JLabel subTotal = new JLabel("Subtotal");
        JLabel gstLabel = new JLabel("GST");
        JLabel subTotalAmount = new JLabel("amount");
        JLabel gstLabelAmount = new JLabel("amount");
        JLabel gstRate = new JLabel("gstRate");
        JLabel total = new JLabel("Total");
        JLabel totalAmount = new JLabel("Total");

        JComboBox<String> itemDropDown = new JComboBox<>();
        for (Item item : this.items) {
            itemDropDown.addItem(item.getDescription());
        }
        JTextField qty = new JTextField("Quantity");
        qty.setPreferredSize(new Dimension(50, 24));
        qty.setMinimumSize(new Dimension(50, 24));
        JPanel itemAdder = new JPanel();
        JButton add = new JButton("Add");
        itemAdder.add(itemDropDown);
        itemAdder.add(qty);
        itemAdder.add(add);

        gbc.gridy = 0;
        gbc.gridx = 0;
        table.add(nameHeading, gbc);
        gbc.gridx = 1;
        table.add(qtyHeading, gbc);
        gbc.gridy = 1;

        for(String key : items.keySet()) {
            Integer value = items.get(key);

            JLabel name =  new JLabel(key);
            JLabel valueHolder =  new JLabel(value.toString());
            JLabel rate =  new JLabel("rate");
            JLabel amount =  new JLabel("amount");

            JButton edit = new JButton("Edit");
            JButton delete = new JButton("Delete");

            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    items.remove(key);
                }
            });
            edit.addActionListener(new ActionListener() {
                String text = "New Amount: ";
                @Override
                public void actionPerformed(ActionEvent e)  {
                    while (true) {
                        String input = JOptionPane.showInputDialog(text);
                        int newAmount;
                        if (input != null) {
                            try {
                                newAmount = Integer.parseInt(input);
                                items.put(key, newAmount);
                                valueHolder.setText(String.valueOf(newAmount));
                                return;
                            } catch (NumberFormatException ex) {
                                text = "Invalid!";
                            }
                        } else {
                            text = "Try Again!";
                            return;
                        }
                    }
//                    items.put(key, )
                }
            });

            gbc.gridx = 0;
            table.add(name, gbc);
            gbc.gridx = 1;

            table.add(valueHolder, gbc);

            gbc.gridx = 2;
            table.add(rate, gbc);
            gbc.gridx = 3;
            table.add(amount, gbc);

            gbc.gridx = 4;
            table.add(edit, gbc);
            gbc.gridx = 5;
            table.add(delete, gbc);
            gbc.gridy++;
        }

        JScrollPane scrollPane = new JScrollPane(table);
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 8);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.setMinimumSize(new Dimension(400, 200));
        scrollPane.setViewportView(table);
        JTextField remarks = new JTextField("Remarks");
        DatePicker datePicker = new DatePicker("Renting Date");
        JPanel rentDatePanel = datePicker.getPanel();
        DatePicker datePicker2 = new DatePicker("Return Date");
        JPanel returnDatePanel = datePicker2.getPanel();
        JButton submit = new JButton("Submit");
        JButton back = new JButton("Back");

        gbc.gridx = 0;
        table.add(subTotal, gbc);
        gbc.gridx = 3;
        table.add(subTotalAmount, gbc);
        gbc.gridy++;

        gbc.gridx = 0;
        table.add(gstLabel, gbc);
        gbc.gridx = 2;
        table.add(gstRate, gbc);
        gbc.gridx = 3;
        table.add(gstLabelAmount, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        table.add(total, gbc);
        gbc.gridx = 3;
        table.add(totalAmount, gbc);


        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigator.close();
            }
        });

        GuiPlacer placer = new GuiPlacer(400, 800);
        Component[] elements = {
                scrollPane, new JLabel(" "), remarks, rentDatePanel,
                returnDatePanel, itemAdder, submit, back
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        JTextField[] texts = {remarks, qty};
        clearManyTexts(texts);

        panel.add(container);
        navigator.open(panel, "makeReservation");

    }

//    void viewReservation() {
//        {
//            // TODO the following data needs to be queried from a DB
//
//            ArrayList<Reservation> reservations = new ArrayList<>();
//            ArrayList<Item> items = new ArrayList<>();
//            items.add(this.items.get(0));
//            items.add(this.items.get(1));
//            items.add(this.items.get(2));
//            reservations.add(new Reservation(1, 1, items, "String remarks", new Date(), new Date(), new Date()));
//            reservations.add(new Reservation(1, 1, items, "String remarks2", new Date(), new Date(), new Date()));
//            reservations.add(new Reservation(1, 1, items, "String remarks3", new Date(), new Date(), new Date()));
//            reservations.add(new Reservation(1, 1, items, "String remarks4", new Date(), new Date(), new Date()));
//
//            JPanel panel = new JPanel();
//
//            final GridBagConstraints gbc = new GridBagConstraints();
//            JPanel table = new JPanel(new GridBagLayout());
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            gbc.weightx = 1;
//            gbc.weighty = 1;
//
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//
//            for (int x = 0; x < reservations.size(); x++) {
//                gbc.gridx = x;
//                Reservation reservation = reservations.get(x);
//                JLabel id = new JLabel(String.valueOf(reservation.getReservationId()));
//                JLabel name = new JLabel(reservation.getCustomer().getName());
//                JLabel reserveDate = new JLabel(getFDate(reservation.getReservationDate()));
//                JLabel rentingDate = new JLabel(getFDate(reservation.getRentDate()));
//                JLabel returningDate = new JLabel(getFDate(reservation.getReturnDate()));
//                Component[] elements = {id, name, reserveDate, rentingDate, returningDate};
//                for (int i = 0; i < elements.length; i++) {
//                    gbc.gridy = i;
//                    table.add(elements[x]);
//                }
//            }
//
//            JScrollPane scrollPane = new JScrollPane();
//            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
//            scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 8);
//            scrollPane.setPreferredSize(new Dimension(400, 200));
//            scrollPane.setMinimumSize(new Dimension(400, 200));
//            scrollPane.setViewportView(table);
//            scrollPane.add(table);
//
//            panel.add(table);
//            navigator.open(panel, "viewReservations");
//        }
//    }

    void viewReservations() {
            // TODO the following data needs to be queried from a DB

        ArrayList<Reservation> reservations = new ArrayList<>();
        ArrayList<Item> items = new ArrayList<>();
        items.add(this.items.get(0));
        items.add(this.items.get(1));
        items.add(this.items.get(2));
        reservations.add(new Reservation(1, 1, items, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, items, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, items, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, items, "String remarks4", new Date(), new Date(), new Date()));

        JPanel panel = new JPanel();

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
//        table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//        table.setPreferredSize(new Dimension(width, height));
        gbc.gridy = 0;

        JLabel idHeading = new JLabel("ID");
        JLabel nameHeading = new JLabel("Name");
        JLabel remarksHeading = new JLabel("Remarks");
        JLabel reservationDateHeading = new JLabel("Reserved");
        JLabel rentingDateHeading = new JLabel("Renting");
        JLabel returningDateHeading = new JLabel("Returning");
        JComponent[] headingElements = {
                idHeading, nameHeading, remarksHeading, remarksHeading,
                reservationDateHeading, rentingDateHeading, returningDateHeading};
        for(int x = 0; x < headingElements.length; x++) {
            gbc.gridx = x;
            table.add(headingElements[x]);
        }
        gbc.gridy++;
        int y;

        for(y = 0; y < reservations.size(); y++) {
//            1, 1, items, "String remarks", new Date(), new Date(), new Date()
            gbc.gridy++;
            Reservation reservation = reservations.get(y);
            JLabel id = new JLabel(Integer.toString(reservation.getReservationId()));
            JLabel name = new JLabel(reservation.getCustomer().getName());
            JLabel remarks = new JLabel(reservation.getRemarks());
            JLabel resrvationDate = new JLabel(getFDate(reservation.getReservationDate()));
            JLabel rentingDate = new JLabel(getFDate(reservation.getRentDate()));
            JLabel returningDate = new JLabel(getFDate(reservation.getReservationDate()));
            JButton delete = new JButton("Delete");
            JComponent[] elements = {id, name, remarks, resrvationDate, rentingDate, returningDate, delete};

            int finalY = y;
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO delete reservation from db
                    reservations.remove(finalY);
                    for(int x = 0; x < elements.length; x++) {
                        table.remove(elements[x]);
                    }
                }
            });
            for(int x = 0; x < elements.length; x++) {
                gbc.gridx = x;
                table.add(elements[x], gbc);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 8);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        scrollPane.setMinimumSize(new Dimension(500, 200));
        scrollPane.setViewportView(table);

        panel.add(scrollPane);
        navigator.open(panel, "makeReservation");
    }
    private String getFDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
        return formatter.format(date);
    }

    public static void main(String[] args) {
        new PartyRental();
    }
}