import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.mariadb.jdbc.Driver;

public class PartyRental {

    private final JFrame mainFrame = new JFrame("Party Rentals");
    private final CardLayout cardLayout = new CardLayout();
    private final Navigator navigator = new Navigator(mainFrame, cardLayout);
    private JButton back = new JButton("Back");
    private JButton logout = new JButton("Logout");

    // TODO the following needs to be queried from DB
//    ArrayList<Item> items = new ArrayList<>();
    HashMap<String, Item> items = new HashMap<>();
    // TODO following needs to be removed from production
    ArrayList<Reservation> reservations = new ArrayList<>();

    Connection connection;

    PartyRental() throws SQLException {
        DriverManager.registerDriver(new Driver());
        connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/party_rental", "root", "123");
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)  {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } );

        mainFrame.setSize(800, 700);
        mainFrame.setMinimumSize(new Dimension(700, 700));
        mainFrame.getContentPane().setLayout(cardLayout);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginPage();

        // TODO the following needs to be queried from DB on runtime not on app init
        Item chair = new Item(1, 1, new Date(), "Chair", 50);
        Item table = new Item(1, 1, new Date(), "Tabke", 50);
        Item poopoo = new Item(1, 1, new Date(), "Poopopopo", 50);
        Item nooo = new Item(1, 1, new Date(), "nooo", 50);
        Item thisItem = new Item(1, 1, new Date(), "this", 50);
        items.put(chair.getDescription(), chair);
        items.put(table.getDescription(), table);
        items.put(poopoo.getDescription(), poopoo);
        items.put(nooo.getDescription(), nooo);
        items.put(thisItem.getDescription(), thisItem);

        HashMap<Item, Integer> fadas = new HashMap<>();
        fadas.put(chair, 3);
        fadas.put(table, 2);
        fadas.put(poopoo, 4);
        fadas.put(nooo, 7);
        fadas.put(thisItem, 2);

        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));

        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigator.close();
            }
        });
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigator.close();
            }
        });


        mainFrame.setVisible(true);
    }

    private void loginPage() {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel label = new JLabel("Sign In");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField usernameField = new JTextField("Username");
        JPasswordField passwordField = new JPasswordField("Password");
        JButton loginButton = new JButton("Login");
        JButton createAccount = new JButton("Create Account");

        createAccount.addActionListener(e -> customerAccountCreation());
        loginButton.addActionListener(e -> adminPage());
        clearPasswordTextFields(usernameField, passwordField);

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {label, usernameField, passwordField, loginButton, createAccount};
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "login");
    }

    private void clearTextField(JTextField textField) {
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
    private void clearPasswordField(JPasswordField passwordField) {
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
    private void clearPasswordTextFields(JTextField textField, JPasswordField passwordField) {
        clearTextField(textField);
        clearPasswordField(passwordField);
    }

    private void clearManyTexts(JTextField[] textFields) {
        for(int x = 0; x < textFields.length; x++) {
            clearTextField(textFields[x]);
        }
    }

    private void customerAccountCreation() {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel label = new JLabel("Account Creation Request");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField name = new JTextField("Name");
        JTextField email = new JTextField("Email");
        JPasswordField passwordField = new JPasswordField("Password");
        JComboBox<CustomerType> type = new JComboBox<>(CustomerType.values());
        JButton submit = new JButton("Send Request");

        clearPasswordTextFields(name, passwordField);
        clearTextField(email);

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {label, name, email, passwordField, type, submit, back};
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private JLabel getPadding(int width, int height) {
        JLabel padding = new JLabel();
        padding.setPreferredSize(new Dimension(width, height));
        padding.setMinimumSize(new Dimension(width, height));
        padding.setMaximumSize(new Dimension(width, height));
        return padding;
    }

    private void officerPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton makeReservationButton = new JButton("Make Reservation");
        JButton viewReservationButton = new JButton("View Reservations");
        JButton registrations = new JButton("Approve Registrations");
        JButton returnOrder = new JButton("Renturn Order");
        JButton rentOrder = new JButton("Rent Order");
        JLabel padding1 = getPadding(400, 5);
        JLabel padding2 = getPadding(400, 5);
        JLabel padding3 = getPadding(400, 5);
        JLabel padding4 = getPadding(400, 5);

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                registrations, padding1, makeReservationButton,
                padding2, viewReservationButton, padding3, rentOrder,
                padding4, returnOrder, getPadding(5, 5), logout
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        HashMap<Item, Integer> itemsForReser = new HashMap<>();
        makeReservationButton.addActionListener(e -> createReservation(itemsForReser));
        viewReservationButton.addActionListener(e -> viewReservations("officer"));
        registrations.addActionListener(e -> approveRegistration());
        rentOrder.addActionListener(e -> recordRentOrder());
        returnOrder.addActionListener(e -> recordReturnOrder());

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void recordRentOrder() {
        // TODO query approved reservations from db
        //  if rentdate = today add a record button to gui
        Item chair = new Item(1, 1, new Date(), "Chair", 50);
        Item table22 = new Item(1, 1, new Date(), "Tabke", 50);
        Item poopoo = new Item(1, 1, new Date(), "Poopopopo", 50);
        Item nooo = new Item(1, 1, new Date(), "nooo", 50);
        Item thisItem = new Item(1, 1, new Date(), "this", 50);

        HashMap<Item, Integer> fadas = new HashMap<>();
        fadas.put(chair, 3);
        fadas.put(table22, 2);
        fadas.put(poopoo, 4);
        fadas.put(nooo, 7);
        fadas.put(thisItem, 2);

        ArrayList<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));

        for(int x = 0; x < reservations.size(); x++) {
            Reservation reservation = reservations.get(0);
            reservation.setStatus("RESERVED");
        }

        displayReservation(reservations, "recordRent", "officerRent");
    }

    private void approveRegistration() {
        /*
        function to approve/delete a customers registration request
         */
        // TODO the following needs to be queried from a DB
        Customer customer1 = new Customer("String name", "String password",  1, "DOMESTIC",
                "daw", "REQUESTED");
        Customer[] customers = {
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
        };
        JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.weightx = 1;
        gbc2.weighty = 0;
        gbc2.gridy = 0;
        gbc2.gridx = 0;

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;

        JLabel idHeading = new JLabel("ID");
        JLabel nameHeading = new JLabel("Name");
        JLabel typeHeading = new JLabel("Type");
        JLabel emailHeading = new JLabel("Email");
        JLabel statusHeading = new JLabel("Status");
        JComponent[] headingElements = {
                idHeading, nameHeading, typeHeading, emailHeading,
                statusHeading};
        for(int x = 0; x < headingElements.length; x++) {
            gbc.gridx = x;
            table.add(headingElements[x]);
        }
        gbc.gridy++;
        int y;

        for(y = 0; y < customers.length; y++) {
//            1, 1, items, "String remarks", new Date(), new Date(), new Date()
            gbc.gridy++;
            Customer customer = customers[y];
            JLabel id = new JLabel(Integer.toString(customer.getClientId()));
            JLabel name = new JLabel(customer.getName());
            JLabel type = new JLabel(customer.getType());
            JLabel email = new JLabel(customer.getEmail());
            JLabel status = new JLabel(customer.getStatus());
            JButton view = new JButton("View Details");
            JComponent[] elements = {
                    id, name, type, email, status, view
            };
            view.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    customerApproveReject(customer);
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
        scrollPane.setPreferredSize(new Dimension(550, 200));
        scrollPane.setMinimumSize(new Dimension(550, 200));
        scrollPane.setViewportView(table);

        panel.add(scrollPane, gbc2);
        gbc2.gridy = 1;
        panel.add(back, gbc2);
        navigator.open(panel, "makeReservation");
    }

    private void customerApproveReject(Customer customer) {
        JLabel id = new JLabel("id: " + customer.getClientId());
        JLabel name = new JLabel("Name: " + customer.getName());
        JLabel email = new JLabel("Name: " + customer.getEmail());
        JLabel type = new JLabel("Type: " + customer.getType());
        JLabel status = new JLabel("Name: " + customer.getName());
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton back = new JButton("Back");
        Component[] elements = {
                id, name, email, type, status, getPadding(40, 5),
                approve, getPadding(40, 5), reject, getPadding(40, 5),
                back
        };
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigator.close();
            }
        });
        approve.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO query db accordingly
                navigator.close();
            }
        });
        reject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO query db accordingly
                navigator.close();
            }
        });

        GuiPlacer placer = new GuiPlacer(400, 600);
        placer.verticalPlacer(elements);

        JPanel panel = new JPanel(new GridBagLayout());
        JPanel container = placer.getContainer();
        panel.add(container);

        navigator.open(panel, "customerApproveReject");
    }

    private void customerPage() {
        JPanel panel = new JPanel();
        JButton makeReservationButton = new JButton("Make Reservation");
        JButton viewReservationButton = new JButton("View Reservations");

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                makeReservationButton, getPadding(10, 5),
                viewReservationButton, getPadding(10, 5),
                logout
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        HashMap<Item, Integer> itemsForReser = new HashMap<>();
        makeReservationButton.addActionListener(e -> createReservation(itemsForReser));
        viewReservationButton.addActionListener(e -> viewReservations("customer"));

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void createReservation(HashMap<Item, Integer> itemsForReservation) {
        // TODO this code below needs to be queried form DB

        JPanel panel = new JPanel();
//        HashMap<Item, Integer> itemsForReser = new HashMap<>();

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
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
        for (Item item : this.items.values()) {
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
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String itemDesc = (String) itemDropDown.getSelectedItem();
                if(itemDesc == null) {
                    return;
                }
                Item item = items.get(itemDesc);

                int amount;
                try {
                    amount = Integer.parseInt(qty.getText());
                } catch (NumberFormatException ex) {
                    return;
                }

                int prevQty;
                try {
                    prevQty = itemsForReservation.get(item);
                    prevQty += amount;
                } catch (NullPointerException ex) {
                    prevQty = amount;
                }
                itemsForReservation.put(item, prevQty);
                navigator.close();
                createReservation(itemsForReservation);
            }
        });

        gbc.gridy = 0;
        gbc.gridx = 0;
        table.add(nameHeading, gbc);
        gbc.gridx = 1;
        table.add(qtyHeading, gbc);
        gbc.gridy = 1;

        for(Item item : itemsForReservation.keySet()) {
            Integer value = itemsForReservation.get(item);

            JLabel name =  new JLabel(item.getDescription());
            JLabel valueHolder =  new JLabel(value.toString());
            JLabel rate =  new JLabel("rate");
            JLabel amount =  new JLabel("amount");

            JButton edit = new JButton("Edit");
            JButton delete = new JButton("Delete");

            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    itemsForReservation.remove(item);
                    createReservation(itemsForReservation);
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
                                itemsForReservation.put(item, newAmount);
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

    private void displayReservation(ArrayList<Reservation> reservations, String panelDesc, String userType) {
        /*
        function to view all of user's reservations
         */
        JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.weightx = 1;
        gbc2.weighty = 0;
        gbc2.gridy = 0;
        gbc2.gridx = 0;

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;

        JLabel idHeading = new JLabel("ID");
        JLabel nameHeading = new JLabel("Name");
        JLabel remarksHeading = new JLabel("Remarks");
        JLabel reservationDateHeading = new JLabel("Reserved");
        JLabel rentingDateHeading = new JLabel("Renting");
        JLabel returningDateHeading = new JLabel("Returning");
        JButton back = new JButton("Back");
        back.addActionListener(e -> navigator.close());
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
            JLabel reservationDate = new JLabel(getFDate(reservation.getReservationDate()));
            JLabel rentingDate = new JLabel(getFDate(reservation.getRentDate()));
            JLabel returningDate = new JLabel(getFDate(reservation.getReservationDate()));
            JButton delete = new JButton("Delete");
            JButton view = new JButton("View");
            JComponent[] elements = {
                    id, name, remarks, reservationDate, rentingDate, returningDate,
                    view, delete
            };

            int finalY = y;
            delete.addActionListener(e -> {
                // TODO delete reservation from db
                reservations.remove(finalY);
                navigator.close();
                viewReservations(userType);
            });
            view.addActionListener(e -> viewReservation(reservation, userType));
            for(int x = 0; x < elements.length; x++) {
                gbc.gridx = x;
                table.add(elements[x], gbc);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 8);
        scrollPane.setPreferredSize(new Dimension(550, 200));
        scrollPane.setMinimumSize(new Dimension(550, 200));
        scrollPane.setViewportView(table);

        panel.add(scrollPane, gbc2);
        gbc2.gridy = 1;
        panel.add(back, gbc2);
        navigator.open(panel, "makeReservation");
    }

    private void viewReservations(String userType) {
        /*
        function to view all of user's reservations
         */
        displayReservation(reservations, "viewReservations", "officer");
    }

    private void viewReservation(Reservation reservation, String userType) {
        /*
        function to view a single reservation
         */

        Customer client = reservation.getCustomer();

        JLabel id = new JLabel("ID: " + reservation.getReservationId());
        JLabel clientID = new JLabel("Client ID: " + client.getClientId());
        JLabel clientName = new JLabel("Client Name: " + client.getName());
        JLabel remarks = new JLabel("Remarks: " + reservation.getRemarks());
        JLabel reservationDate = new JLabel("Reservation Date: " + getFDate(reservation.getReservationDate()));
        JLabel rentDate = new JLabel("Rent Date: " + getFDate(reservation.getRentDate()));
        JLabel returnDate = new JLabel("Return Date: " + getFDate(reservation.getReturnDate()));
        JLabel status = new JLabel("Status: " + reservation.getStatus());

        Component[] elements = {
                id, clientID, clientName, status, remarks, reservationDate,
                rentDate, returnDate
        };
        GuiPlacer placer = new GuiPlacer(400, 500);
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel nameHeading = new JLabel("Item");
        JLabel qtyHeading = new JLabel("Qty");
        JLabel rateHeading = new JLabel("Rate");
        JLabel amountHeading = new JLabel("Amount");

        JLabel durationLabel = new JLabel("#days");
        JLabel duration = new JLabel("%d%M%YYYY");
        JLabel subTotal = new JLabel("Subtotal");
        JLabel gstLabel = new JLabel("GST");
        JLabel subTotalAmount = new JLabel("amount");
        JLabel gstLabelAmount = new JLabel("amount");
        JLabel gstRate = new JLabel("gstRate");
        JLabel total = new JLabel("Total");
        JLabel totalAmount = new JLabel("Total");

        JLabel alreadyPaidLabel = new JLabel("Initially Paid");
        JLabel alreadyPaidAmount = new JLabel(String.valueOf(reservation.getTotal()));

        JScrollPane scrollPane = new JScrollPane(table);
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 8);
        scrollPane.setPreferredSize(new Dimension(550, 200));
        scrollPane.setMinimumSize(new Dimension(550, 200));
        scrollPane.setViewportView(table);

        gbc.gridy = 0;
        gbc.gridx = 0;
        table.add(nameHeading, gbc);
        gbc.gridx = 1;
        table.add(qtyHeading, gbc);
        gbc.gridx = 2;
        table.add(rateHeading, gbc);
        gbc.gridx = 3;
        table.add(amountHeading, gbc);

        HashMap<Item, Integer> items = reservation.getItems();
        for(Item item : items.keySet()) {
//            Item item = items.get(x);
            JLabel qty = new JLabel(String.valueOf(items.get(item)));
            JLabel itemName = new JLabel(item.getDescription());
            JLabel rate = new JLabel(String.valueOf(item.getRate()));
            JLabel amount = new JLabel(String.valueOf(item.getRate()));
            gbc.gridy++;

            gbc.gridx = 0;
            table.add(itemName, gbc);
            gbc.gridx = 1;
            table.add(qty, gbc);
            gbc.gridx = 2;
            table.add(rate, gbc);
            gbc.gridx = 3;
            table.add(amount, gbc);
        }

        elements = new Component[]{scrollPane};
        placer.verticalPlacer(elements);

        Component[][] mDElements = {
                {durationLabel, new Label(" "), new Label(" "), duration},
                {subTotal, new Label(" "), new Label(" "), subTotalAmount},
                {gstLabel, new Label(" "), gstRate, gstLabelAmount},
                {total, new Label(" "), new Label(" "), totalAmount},
                {alreadyPaidLabel, new Label(" "), new Label(" "), alreadyPaidAmount},
        };
        GuiPlacer placer2 = new GuiPlacer(400, 500);
        placer2.vhPlacer(mDElements);
        JPanel container2 = placer2.getContainer();

        JButton delete = new JButton("Delete");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // todo delete item from DB
                navigator.close();
                navigator.close();
                viewReservations(userType);
            }
        });

        GuiPlacer main = new GuiPlacer(400, 500);
        Component[] mainElements;
        if (userType.equals("customer")) {
            mainElements = new Component[]{
                    container, getPadding(5, 5),
                    scrollPane, getPadding(5, 5),
                    container2, getPadding(5, 5),
                    delete, getPadding(5, 5),
                    back, getPadding(5, 5)
            };
        } else if (userType.equals("officerRent")){
            JButton rentOrder = new JButton("Record Rent Order");
            mainElements = new Component[]{
                    container, getPadding(5, 5),
                    scrollPane, getPadding(5, 5),
                    container2, getPadding(5, 5),
                    rentOrder, getPadding(5, 5),
                    delete, getPadding(5, 5),
                    back, getPadding(5, 5),
            };
        } else if (userType.equals("officer")) {
            JButton approve = new JButton("Approve");
            mainElements = new Component[]{
                    container, getPadding(5, 5),
                    scrollPane, getPadding(5, 5),
                    container2, getPadding(5, 5),
                    approve, getPadding(5, 5),
                    delete, getPadding(5, 5),
                    back, getPadding(5, 5),
            };
        } else if(userType.equals("officerReturn")) {
            JButton recordReturn = new JButton("Record Return Order");
            mainElements = new Component[]{
                    container, getPadding(5, 5),
                    scrollPane, getPadding(5, 5),
                    container2, getPadding(5, 5),
                    recordReturn, getPadding(5, 5),
                    delete, getPadding(5, 5),
                    back, getPadding(5, 5),
            };
        } else {
            mainElements = new Component[]{};
        }

        main.verticalPlacer(mainElements);
        JPanel panel = main.getContainer();
        navigator.open(panel, "viewReservation");
    }

    private void recordReturnOrder() {
        // TODO query approved reservations from db
        //  if rentdate = today add a record button to gui
        Item chair = new Item(1, 1, new Date(), "Chair", 50);
        Item table22 = new Item(1, 1, new Date(), "Tabke", 50);
        Item poopoo = new Item(1, 1, new Date(), "Poopopopo", 50);
        Item nooo = new Item(1, 1, new Date(), "nooo", 50);
        Item thisItem = new Item(1, 1, new Date(), "this", 50);

        HashMap<Item, Integer> fadas = new HashMap<>();
        fadas.put(chair, 3);
        fadas.put(table22, 2);
        fadas.put(poopoo, 4);
        fadas.put(nooo, 7);
        fadas.put(thisItem, 2);

        ArrayList<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));

        for(int x = 0; x < reservations.size(); x++) {
            Reservation reservation = reservations.get(0);
            reservation.setStatus("RENTED");
        }

        displayReservation(reservations, "recordReturnOrder", "officerReturn");
    }

    private void adminPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton management = new JButton("User Management");
        JButton sales = new JButton("Sales Report");
        JButton inventory = new JButton("Inventory Management");
        JButton importData = new JButton("Import Data");
        JButton twitterScrape = new JButton("Search Twitter");

        management.addActionListener(e -> userManagement());
        inventory.addActionListener(e -> inventoryManagement());

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                management, getPadding(10, 5),
                sales, getPadding(10, 5),
                inventory, getPadding(10, 5),
                importData, getPadding(10, 5),
                twitterScrape, getPadding(10, 5),
                logout
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void addUser(){
        JPanel panel = new JPanel(new GridBagLayout());
        JComboBox<Role> role = new JComboBox<>(Role.values());
        JTextField name = new JTextField("Name");
        JTextField email = new JTextField("Email");
        JButton add = new JButton("Add User");
        JTextField[] textFields = new JTextField[]{email, name};
        clearManyTexts(textFields);

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] mainElements = {
                name, getPadding(10, 5),
                email, getPadding(10, 5),
                role, getPadding(10, 5),
                add, getPadding(10, 5),
                back
        };
        placer.verticalPlacer(mainElements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "addUser");
    }

    private void removeUser(){

        String[] types = new String[]{"Customer", "Employee"};
        JComboBox<String> type = new JComboBox<>(types);

        JPanel panel = new JPanel(new GridBagLayout());
        JButton delete = new JButton("Delete User");
        JButton load = new JButton("Load User Data");
        JTextField id = new JTextField("EmployeeID/ClientID");

        clearTextField(id);

        JLabel name = new JLabel("Name: ");
        JLabel idLabel = new JLabel("ID: ");
        JLabel typeLabel = new JLabel("Role/Type: ");
        JLabel email = new JLabel("Email: ");
        GuiPlacer smallPlacer = new GuiPlacer(400, 500);
        JComponent[] smallElements = new JComponent[]{
                name, idLabel, typeLabel, email
        };
        smallPlacer.verticalPlacer(smallElements);
        JPanel details = smallPlacer.getContainer();

        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selection = (String) type.getSelectedItem();

                if (selection == null) {
                    return;
                } else if (selection.equals("Customer")) {
                    typeLabel.setText("Type: ");
                } else if (selection.equals("Employee")) {
                    typeLabel.setText("Role: ");
                }
            }
        });

        JComponent[] mainElements = new JComponent[]{
                details, getPadding(10, 5),
                id, getPadding(10, 5),
                type, getPadding(10, 5),
                load, getPadding(10, 5),
                delete, getPadding(10, 5),
                back
        };
        GuiPlacer placer = new GuiPlacer(400, 500);
        placer.verticalPlacer(mainElements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "removeUser");

    }

    private void userManagement() {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton addUserButton = new JButton("Add User");
        JButton removeUserButton = new JButton("Remove User");
        JLabel heading = new JLabel("Accounts Statistics");
        Label numPendingCustomers = new Label("Pending Customer Accounts: ");
        Label customerAccounts = new Label("Customer Accounts: ");
        Label officerAccounts = new Label("Officer Accounts: ");
        Label adminAccounts = new Label("Admin Accounts: ");
        JButton back = new JButton("Back");

        addUserButton.addActionListener(e -> addUser());
        removeUserButton.addActionListener(e -> removeUser());
        back.addActionListener(e -> navigator.close());

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] mainElements = {
                heading, getPadding(10, 5),
                numPendingCustomers, getPadding(10, 5),
                customerAccounts, getPadding(10, 5),
                officerAccounts, getPadding(10, 5),
                adminAccounts, getPadding(10, 5),
                addUserButton, getPadding(10, 5),
                removeUserButton, getPadding(10, 5),
                back
        };
        placer.verticalPlacer(mainElements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void inventoryManagement() {

//        JPanel stats;

        JButton add = new JButton("Add Item");
        JButton remove = new JButton("Remove Item");
        JButton back = new JButton("Back");
        back.addActionListener(e -> navigator.close());
        add.addActionListener(e -> addItem());
        remove.addActionListener(e -> removeItem());

        GuiPlacer mainPlacer = new GuiPlacer(400, 500);
        JComponent[] mainElements = new  JComponent[]{
                add, getPadding(10, 5),
                remove, getPadding(10, 5),
                back, getPadding(10, 5),
        };
        mainPlacer.verticalPlacer(mainElements);

        JPanel panel = new JPanel(new GridBagLayout());
        JPanel container = mainPlacer.getContainer();
        panel.add(container);

        navigator.open(panel, "inventoryManagement");
    }

    private void addItem() {
        JPanel panel = new JPanel(new GridBagLayout());

        JTextField description = new JTextField("Description");
        JTextField rate = new JTextField("Rate");
        JButton add = new JButton("Add");
        JLabel heading = new JLabel("Add Item");
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField[] fields = new JTextField[]{description, rate};
        clearManyTexts(fields);

        JComponent[] elements = new JComponent[]{
                heading, getPadding(10, 5),
                description, getPadding(10, 5),
                rate, getPadding(10, 5),
                add, getPadding(10, 5),
                back
        };

        GuiPlacer placer = new GuiPlacer(400,500);
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "addItem");
    }

    private void removeItem() {
        JPanel panel = new JPanel(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel idHeading = new JLabel("ID");
        JLabel descriptionHeading = new JLabel("Description");
        JLabel rateHeading = new JLabel("Rate");
        JLabel createdByHeading = new JLabel("Created By");
        JLabel createdOnHeading = new JLabel("Created On");

        table.add(idHeading, gbc);
        gbc.gridx = 1;
        table.add(descriptionHeading, gbc);
        gbc.gridx = 2;
        table.add(rateHeading, gbc);
        gbc.gridx = 3;
        table.add(createdByHeading, gbc);
        gbc.gridx = 4;
        table.add(createdOnHeading, gbc);

        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(1, 1, new Date(), "Chair", 50));
        items.add(new Item(1, 1, new Date(), "Chair", 50));
        items.add(new Item(1, 1, new Date(), "Chair", 50));
        items.add(new Item(1, 1, new Date(), "Chair", 50));

        for(int x = 0; x < items.size(); x++) {
            gbc.gridy++;

            Item item = items.get(x);
            JLabel id = new JLabel(String.valueOf(item.getId()));
            JLabel description = new JLabel(item.getDescription());
            JLabel rate = new JLabel(String.valueOf(item.getRate()));
            JLabel createdBy = new JLabel(String.valueOf(item.getCreatedBy()));
            JLabel createdOn = new JLabel(getFDate(item.getDate()));
            JButton delete = new JButton("Delete");

            JComponent[] tableElements = new JComponent[]{
                    id, description, rate, createdBy, createdOn, delete
            };

            for(int i = 0; i < tableElements.length; i++) {
                gbc.gridx = i;
                JComponent element = tableElements[i];
                table.add(element, gbc);
            }
        }

        JComponent[] elements = new JComponent[]{
                table, getPadding(10, 5),
                back
        };

        GuiPlacer placer = new GuiPlacer(400,500);
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "removeItem");
    }

    private String getFDate(Date date) {
        /// get formatted date into a string
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
        return formatter.format(date);
    }

    public static void main(String[] args) throws SQLException {
        new PartyRental();
    }
}