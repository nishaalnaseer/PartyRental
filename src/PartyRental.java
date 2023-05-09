// sql and db
import java.sql.*;
import org.mariadb.jdbc.Driver;

// hashing
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// csv parser
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

public class PartyRental {

    private final JFrame mainFrame = new JFrame("Party Rentals");
    private final CardLayout cardLayout = new CardLayout();
    private final Navigator navigator = new Navigator(mainFrame, cardLayout);
    private final JButton back = new JButton("Back");
    private final JButton logout = new JButton("Logout");
    private final SqlScripts scripts = new SqlScripts();
    private Employee employee;
    private Customer customer;

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
//        Item chair = new Item(1, 1, new Date(), "Chair", 50, 0, 0, 0);
//        Item table = new Item(1, 1, new Date(), "Tabke", 50, 0, 0, 0);
//        Item poopoo = new Item(1, 1, new Date(), "Poopopopo", 50, 0, 0, 0);
//        Item nooo = new Item(1, 1, new Date(), "nooo", 50, 0, 0, 0);
//        Item thisItem = new Item(1, 1, new Date(), "this", 50, 0, 0, 0);
//        items.put(chair.getDescription(), chair);
//        items.put(table.getDescription(), table);
//        items.put(poopoo.getDescription(), poopoo);
//        items.put(nooo.getDescription(), nooo);
//        items.put(thisItem.getDescription(), thisItem);

        HashMap<Item, Integer> fadas = new HashMap<>();
//        fadas.put(chair, 3);
//        fadas.put(table, 2);
//        fadas.put(poopoo, 4);
//        fadas.put(nooo, 7);
//        fadas.put(thisItem, 2);

//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));

        back.addActionListener(e -> navigator.close());
        logout.addActionListener(e -> navigator.close());


        mainFrame.setVisible(true);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not supported", e);
        }
    }

    private void loginPage() {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel label = new JLabel("Sign In");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField usernameField = new JTextField("nishawl.naseer@outlook.com");
        JPasswordField passwordField = new JPasswordField("123");
        JButton loginButton = new JButton("Login");
        JButton createAccount = new JButton("Create Account");

        createAccount.addActionListener(e -> customerAccountCreation());
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            String[] texts = new String[]{username, password};

            if(validateCredentials(texts, username, password)) {
                return;
            }

            password = sha256(password);
            try {
                Object[] values = new Object[]{username, password, "ENABLED"};
                ResultSet rs = valuedQuery(scripts.loginClient, values);
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String userPassword = rs.getString("password");
                    String userEmail = rs.getString("email");
                    String type = rs.getString("label");
                    String status = rs.getString("status");

                    customer = new Customer(id, name, userPassword, userEmail, type, status);
                    customerPage();
                    rs.close();
                    return;
                }
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, "SQL Error!");
                return;
            }
            try {
                Object[] values = new Object[]{username, password, "ENABLED"};
                ResultSet rs = valuedQuery(scripts.loginEmployee, values);
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String userPassword = rs.getString("password");
                    String userEmail = rs.getString("email");
                    String role = rs.getString("role");
                    String status = rs.getString("status");

                    employee = new Employee(id, name, userPassword, userEmail, role, status);

                    if (employee.getRole() == Role.ADMINISTRATOR) {
                        adminPage();
                        rs.close();
                        return;
                    } else if (employee.getRole() == Role.OFFICER) {
                        officerPage();
                        rs.close();
                        return;
                    }
                }
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, "SQL Error!");
                return;
            }
            JOptionPane.showMessageDialog(mainFrame, "Invalid Credentials!");
        });
        clearPasswordTextFields(usernameField, passwordField);

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                label,
                usernameField,
                passwordField, getPadding(400, 5),
                loginButton, getPadding(400, 5),
                createAccount
        };
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

    private boolean checkUser(String[] scripts, String username) {
        for(String script : scripts) {
            try {
                // Create a prepared statement object
                PreparedStatement stmt = connection.prepareStatement(script);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if(!rs.next()) {
                    rs.close();
                } else {
                    rs.close();
                    return false;
                }
            } catch (SQLException ex) {
                // do nothing
            }
        }
        return true;
    }

    private boolean validateCredentials(String[] texts, String username, String password) {
        for (String text : texts) {
            if(text.isBlank()) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Option");
                return true;
            }
        }
        if (username.equals("Name") || username.equals("Email") || password.equals("Password")) {
            JOptionPane.showMessageDialog(mainFrame, "Invalid Option");
            return true;
        }
        return false;
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

        submit.addActionListener(e -> {
            String customerName = name.getText();
            String customerEmail = email.getText();
            char[] passRaw = passwordField.getPassword();
            String customerPassword = new String(passRaw);
            String customerType =  String.valueOf(type.getSelectedItem());
            String[] texts = new String[]{customerName, customerEmail, customerPassword, customerType};

            if((!customerEmail.contains("@"))
                    && (!customerEmail.contains("."))){
                JOptionPane.showMessageDialog(mainFrame, "Invalid Option");
                return;
            }
            if(validateCredentials(texts, customerName, customerPassword)) {
                return;
            }
            try {
                customerPassword = sha256(customerPassword);
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Password");
                return;
            }
            String[] checkScripts = new String[]{
                    scripts.checkClient, scripts.checkEmployee, scripts.checkNewClient
            };
            if(!checkUser(checkScripts, customerEmail)) {
                JOptionPane.showMessageDialog(mainFrame, "Email taken!");
                return;
            }

            try {
                Object[] values = {customerType};
                ResultSet resultSet = valuedQuery(scripts.getTypeID, values);
                resultSet.next();
                int typeID = resultSet.getInt("id");
                values = new Object[]{
                        customerName, customerPassword, customerEmail, typeID
                };

                resultSet.close();
                resultSet = valuedQuery(scripts.createClient, values);
                resultSet.close();
                JOptionPane.showMessageDialog(mainFrame, "Your request has been sent and is awaiting approval!");
                navigator.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        clearPasswordTextFields(name, passwordField);
        clearTextField(email);

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                label,
                name,
                email,
                passwordField,
                type, getPadding(400, 5),
                submit, getPadding(400, 5),
                back
        };
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
        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                registrations, getPadding(5, 5),
                viewReservationButton, getPadding(5, 5),
                rentOrder, getPadding(5, 5),
                returnOrder, getPadding(5, 5),
                logout
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        HashMap<String, Integer> itemsForReservation = new HashMap<>();
        Object[] selectedStartDate = new String[]{"Select Day", "Select Month", "Select Year"};
        Object[] selectedEndDate = new String[]{"Select Day", "Select Month", "Select Year"};
        makeReservationButton.addActionListener(e -> createReservation(itemsForReservation, 0, 0, selectedStartDate, selectedEndDate, "officer"));
        viewReservationButton.addActionListener(e -> {
            viewReservations("officer");
        });
        registrations.addActionListener(e -> approveRegistration());
        rentOrder.addActionListener(e -> recordRentOrder());
        returnOrder.addActionListener(e -> recordReturnOrder());

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void recordRentOrder() {
        // TODO query approved reservations from db
        //  if rentdate = today add a record button to gui
//        Item chair = new Item(1, 1, new Date(), "Chair", 50, stock, reserved, rented);
//        Item table22 = new Item(1, 1, new Date(), "Tabke", 50, stock, reserved, rented);
//        Item poopoo = new Item(1, 1, new Date(), "Poopopopo", 50, stock, reserved, rented);
//        Item nooo = new Item(1, 1, new Date(), "nooo", 50, stock, reserved, rented);
//        Item thisItem = new Item(1, 1, new Date(), "this", 50, stock, reserved, rented);

        HashMap<Item, Integer> fadas = new HashMap<>();
//        fadas.put(chair, 3);
//        fadas.put(table22, 2);
//        fadas.put(poopoo, 4);
//        fadas.put(nooo, 7);
//        fadas.put(thisItem, 2);

        ArrayList<Reservation> reservations = new ArrayList<>();
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));

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
        ArrayList<Customer> customers = new ArrayList<>();
        ResultSet resultSet;
        try {
            resultSet = noValueQuery(scripts.getRequestedClients);
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String label = resultSet.getString("label");
                String status = resultSet.getString("status");
                customers.add(new Customer(
                        id, name, password, email, label, status
                ));
            }
            resultSet.close();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception);
            return;
        }

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

        for(y = 0; y < customers.size(); y++) {
            gbc.gridy++;
            Customer customer = customers.get(y);
            JLabel id = new JLabel(Integer.toString(customer.getClientId()));
            JLabel name = new JLabel(customer.getName());
            JLabel type = new JLabel(customer.getType());
            JLabel email = new JLabel(customer.getEmail());
            JLabel status = new JLabel(customer.getStatus());
            JButton view = new JButton("View Details");
            JComponent[] elements = {
                    id, name, type, email, status, view
            };
            view.addActionListener(e -> customerApproveReject(customer));

            for(int x = 0; x < elements.length; x++) {
                gbc.gridx = x;
                table.add(elements[x], gbc);
            }
        }

        JScrollPane scrollPane = scrollTable(table, 550, 200);
        panel.add(scrollPane, gbc2);
        gbc2.gridy = 1;
        panel.add(back, gbc2);
        navigator.open(panel, "makeReservation");
    }

    public void delRequestedClient(int id) throws SQLException {
        Object[] values = new Object[]{id};
        valuedQuery(scripts.delRequestClient, values);
    }

    private void customerApproveReject(Customer customer) {
        int idRaw = customer.getClientId();
        String type = customer.getType();
        String customerName = customer.getName();
        String customerEmail = customer.getEmail();
        String customerStatus = customer.getStatus();

        JLabel id = new JLabel("id: " + idRaw);
        JLabel name = new JLabel("Name: " + customerName);
        JLabel email = new JLabel("Name: " + customerEmail);
        JLabel typeLabel = new JLabel("Type: " + type);
        JLabel status = new JLabel("Status: " + customerStatus);
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton back = new JButton("Back");
        Component[] elements = {
                id, name, email, typeLabel, status, getPadding(40, 5),
                approve, getPadding(40, 5), reject, getPadding(40, 5),
                back
        };
        back.addActionListener(e -> {
            navigator.close();
            navigator.close();
            approveRegistration();
        });
        approve.addActionListener(e -> {
            try {
                Object[] values = new Object[]{type};
                ResultSet resultSet = valuedQuery(scripts.getTypeID, values);
                resultSet.next();
                int typeId = resultSet.getInt("id");
                String customerPassword = sha256("123");
                values = new Object[]{
                        customerName, customerPassword, customerEmail,
                        typeId, "ENABLED"
                };
                valuedQuery(scripts.insertCustomer, values);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                return;
            }
            try {
                delRequestedClient(idRaw);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                return;
            }

            navigator.close();
            navigator.close();
            approveRegistration();
        });
        reject.addActionListener(e -> {
            try {
                delRequestedClient(idRaw);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                return;
            }

            navigator.close();
            navigator.close();
            approveRegistration();
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


        makeReservationButton.addActionListener(e -> {
            HashMap<String, Integer> itemsForReservation = new HashMap<>();
            String[] selectedStartDate = new String[]{"Select Day", "Select Month", "Select Year"};
            String[] selectedEndDate = new String[]{"Select Day", "Select Month", "Select Year"};
            createReservation(itemsForReservation, 0, 0,
                    selectedStartDate, selectedEndDate, "customer");
        });
        viewReservationButton.addActionListener(e -> {
            viewReservations("customer");
        });

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void dateFormatter(HashMap<String, Integer> itemsForReservation, DatePicker datePicker1,
                               DatePicker datePicker2, Item item, String userType, Integer amount, float finalGstValue) {
        String startDateRaw = datePicker1.getDate();
        String endDateRaw = datePicker2.getDate();
        Date start;
        Date end;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
            start = format.parse(startDateRaw);
            end = format.parse(endDateRaw);
        } catch (java.text.ParseException exception) {
            itemsForReservation.put(item.getDescription(), amount);
            navigator.close();
            createReservation(itemsForReservation, finalGstValue, 0, datePicker1.getDateRaw(), datePicker2.getDateRaw(), userType);
            return;
        }
        LocalDate date1 = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date2 = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(date1, date2);

        if(daysBetween <= 1) {
            itemsForReservation.put(item.getDescription(), amount);
            navigator.close();
            createReservation(itemsForReservation, finalGstValue, 0, datePicker1.getDateRaw(), datePicker2.getDateRaw(), userType);
        }
        itemsForReservation.put(item.getDescription(), amount);
        navigator.close();
        createReservation(itemsForReservation, finalGstValue, daysBetween, datePicker1.getDateRaw(), datePicker2.getDateRaw(), userType);
    }

    private float getGst() throws SQLException {
        Object[] values = new Object[]{customer.getType()};
        ResultSet resultSet = valuedQuery(scripts.getGstRate, values);
        resultSet.next();
        float gstValue = resultSet.getFloat("tax");
        resultSet.close();
        return gstValue;
    }

    private void createReservation(HashMap<String, Integer> itemsForReservation, float gstValue, long days,
                                   Object[] selectedStartDate, Object[] selectedEndDate, String userType

    ) {
        // todo make delete button work
        // todo make sure rent date is not past today
        JPanel panel = new JPanel(new GridBagLayout());
        HashMap<String, Item> items;

        try {
            items = dbToHashMap(false);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception);
            return;
        }
        if(gstValue == 0) {
            try {
                gstValue = getGst();
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
                return;
            }
        }

        float subtotalValue = 0;
        JComboBox<String> itemDropDown = new JComboBox<>();
        for(String description : itemsForReservation.keySet()) {
            int quantity = itemsForReservation.get(description);
            Item item = items.get(description);
            subtotalValue += (item.getRate() * quantity)*days;
        }
        float gstAmount = subtotalValue * gstValue / 100;
        float totalValue = subtotalValue + gstAmount;

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel nameHeading = new JLabel("Item");
        JLabel qtyHeading = new JLabel("Qty");
        JLabel subTotal = new JLabel("Subtotal");
        JLabel gstLabel = new JLabel("GST");
        JLabel subTotalAmount = new JLabel(String.valueOf(subtotalValue));
        JLabel gstLabelAmount = new JLabel(String.valueOf(subtotalValue * gstValue /100));
        JLabel gstRate = new JLabel(gstValue+"%");
        JLabel total = new JLabel("Total");
        JLabel itemRateHeading = new JLabel("Rate");
        JLabel perDayHeading = new JLabel("Per Day");
        JLabel allDays = new JLabel(days + " Days");
        JLabel totalAmount = new JLabel(String.valueOf(totalValue));

        DatePicker datePicker = new DatePicker("Renting Date", selectedStartDate);
        JPanel rentDatePanel = datePicker.getPanel();
        DatePicker datePicker2 = new DatePicker("Return Date", selectedEndDate);
        JPanel returnDatePanel = datePicker2.getPanel();

        for(Item item : items.values()) {
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
        float finalGstValue = gstValue;
        add.addActionListener(e ->  {
            String itemDesc = (String) itemDropDown.getSelectedItem();
            if(itemDesc == null) {
                return;
            }
            Item item = items.get(itemDesc);

            int amount;
            try {
                amount = Integer.parseInt(qty.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                return;
            }

            int prevQty;
            try {
                prevQty = itemsForReservation.get(item.getDescription());
                if((amount += prevQty) > 0) {
                    amount += prevQty;
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Total Quantity Cant Be Below Zero");
                    return;
                }
            } catch (NullPointerException ex) {
                // do nothing
            }

            dateFormatter(itemsForReservation, datePicker, datePicker2, item, userType, amount, finalGstValue);
        });

        gbc.gridy = 0;
        gbc.gridx = 0;
        table.add(nameHeading, gbc);
        gbc.gridx = 1;
        table.add(qtyHeading, gbc);
        gbc.gridx = 2;
        table.add(itemRateHeading, gbc);
        gbc.gridx = 3;
        table.add(perDayHeading, gbc);
        gbc.gridx = 4;
        table.add(allDays, gbc);
        gbc.gridy = 1;

        for(String description : itemsForReservation.keySet()) {
            Item item = items.get(description);
            Integer value = itemsForReservation.get(description);
            float amountValue = value * item.getRate();

            JLabel name =  new JLabel(item.getDescription());
            JLabel valueHolder =  new JLabel(value.toString());
            JLabel rate =  new JLabel(String.valueOf(item.getRate()));
            JLabel amount = new JLabel(String.valueOf(amountValue));
            JLabel allDaysLabel = new JLabel(String.valueOf(value*days*item.getRate()));

            JButton edit = new JButton("Edit");
            JButton delete = new JButton("Delete");
            delete.addActionListener(e -> {
                itemsForReservation.remove(description);
                dateFormatter(itemsForReservation, datePicker, datePicker2, item, userType, value, finalGstValue);
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
                                dateFormatter(itemsForReservation, datePicker, datePicker2, item, userType, newAmount, finalGstValue);
                                valueHolder.setText(String.valueOf(newAmount));
//                                itemsForReservation.put(item.getDescription(), newAmount);
//                                navigator.close();
//                                createReservation(itemsForReservation, finalGstValue, 0, datePicker.getDateRaw(), datePicker2.getDateRaw(), userType);
                                return;
                            } catch (NumberFormatException ex) {
                                text = "Invalid!";
                            }
                        } else {
                            text = "Try Again!";
                            return;
                        }
                    }
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
            table.add(allDaysLabel, gbc);

            gbc.gridx = 5;
            table.add(edit, gbc);
            gbc.gridx = 6;
            table.add(delete, gbc);
            gbc.gridy++;
        }

        JScrollPane scrollPane = scrollTable(table, 600, 200);
        JTextField remarks = new JTextField("Remarks");
        JButton submit = new JButton("Submit");
        float finalSubtotalValue = subtotalValue;
        submit.addActionListener(e -> {
            String startDateRaw = datePicker.getDate();
            String endDateRaw = datePicker2.getDate();
            Date start;
            Date end;
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                start = format.parse(startDateRaw);
                end = format.parse(endDateRaw);
            } catch (java.text.ParseException exception) {
                navigator.close();
                createReservation(itemsForReservation, finalGstValue, 0, datePicker.getDateRaw(), datePicker2.getDateRaw(), userType);
                return;
            }
            LocalDate date1 = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate date2 = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(date1, date2);

            if(daysBetween < 1) {
                JOptionPane.showMessageDialog(mainFrame, "Number of Days Can Not be Negative!");
                return;
            }

//            float gstAmount = subtotalValue * gstValue / 100;
//            float totalValue = subtotalValue + gstAmount;
            Reservation reservation = new Reservation(0, customer, itemsForReservation,
                    remarks.getText(), new Date(), start, end, gstAmount, finalSubtotalValue);

            viewReservation(reservation, userType, "make");
        });

        gbc.gridx = 0;
        table.add(subTotal, gbc);
        gbc.gridx = 4;
        table.add(subTotalAmount, gbc);
        gbc.gridy++;

        gbc.gridx = 0;
        table.add(gstLabel, gbc);
        gbc.gridx = 2;
        table.add(gstRate, gbc);
        gbc.gridx = 4;
        table.add(gstLabelAmount, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        table.add(total, gbc);
        gbc.gridx = 4;
        table.add(totalAmount, gbc);

        GuiPlacer placer = new GuiPlacer(400, 800);
        JButton back = new JButton("Back");
        back.addActionListener(e -> navigator.close());
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
            JLabel reservationDate = new JLabel(getFDate(reservation.getReservationDate(), "dd-MMM-yy"));
            JLabel rentingDate = new JLabel(getFDate(reservation.getRentDate(), "dd-MMM-yy"));
            JLabel returningDate = new JLabel(getFDate(reservation.getReservationDate(), "dd-MMM-yy"));
            JButton delete = new JButton("Delete");
            JButton view = new JButton("View");
            JComponent[] elements = {
                    id, name, remarks, reservationDate, rentingDate, returningDate,
                    view, delete
            };

            int finalY = y;
            delete.addActionListener(e -> {
                // TODO update reservation from db
                reservations.remove(finalY);
                navigator.close();

                viewReservations(userType);
            });
            view.addActionListener(e -> viewReservation(reservation, userType, "made"));
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
        navigator.open(panel, panelDesc);
    }

    private Customer makeCustomer(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        String status = resultSet.getString("status");
        String type = resultSet.getString("label");

        return new Customer(id, name, password, email, type, status);
    }

    ArrayList<Reservation> getReservations(String script, Object[] values) throws SQLException {

        ResultSet resultSet = valuedQuery(script, values);
        ArrayList<Reservation> reservations = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int clientID = resultSet.getInt("customer");
            String remarks = resultSet.getString("remarks");
            float paid = resultSet.getFloat("paid");
            Date reservationDate = resultSet.getDate("reservation_date");
            Date rentDate = resultSet.getDate("rent_date");
            Date returnDate = resultSet.getDate("return_date");
            String status = resultSet.getString("status");
            float gst = resultSet.getFloat("gst");
            float subtotal = resultSet.getFloat("subtotal");

            String rentDateString = getFDate(rentDate, "dd-MM-yyyy");
            String returnDateString = getFDate(returnDate, "dd-MM-yyyy");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                rentDate = dateFormat.parse(rentDateString);
            } catch (ParseException e) {
                break;
            }
            try {
                returnDate = dateFormat.parse(returnDateString);
            } catch (ParseException e) {
                break;
            }

            Object[] customerValues = new Object[] {clientID};
            ResultSet customerSet = valuedQuery(scripts.selectClient, customerValues);
            customerSet.next();
            Customer customer = makeCustomer(customerSet);
            customerSet.close();

            HashMap<String, Integer> reservationItems = new HashMap<>();
            Object[] itemValues = new Object[] {id};
            ResultSet itemSet = valuedQuery(scripts.selectReservationItems, itemValues);
            while (itemSet.next()) {
                String description = itemSet.getString("description");
                int quantity = itemSet.getInt("qty");
                reservationItems.put(description, quantity);
            }

            Reservation reservation = new Reservation(
                    id, customer, reservationItems, remarks, reservationDate,
                    rentDate, returnDate, gst, subtotal
            );
            reservation.setStatus(status);
            reservation.setPaid(paid);

            int approvedBy = resultSet.getInt("approved_by");
            if (resultSet.wasNull()) {
                reservation.setApprovedBy(approvedBy);
            }
            int rentedBy = resultSet.getInt("rented_by");
            if (resultSet.wasNull()) {
                reservation.setRentedBy(rentedBy);
            }
            int returnBy = resultSet.getInt("return_accepted_by");
            if (resultSet.wasNull()) {
                reservation.setReturnAcceptedBy(returnBy);
            }

            reservations.add(reservation);
        }
        resultSet.close();
        return reservations;
    }

    private void viewReservations(String userType) {
        /*
        function to view all of user's reservations
         */

        ArrayList<Reservation> reservations;
        try {
            if(userType.equals("customer")) {
                reservations = getReservations(scripts.selectClientReservations, new Object[]{customer.getClientId()});
            } else {
                reservations = getReservations(scripts.selectReservationsOnStatus, new Object[]{"REQUESTED"});
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }

        displayReservation(reservations, "viewReservations", userType);
    }

    private void makePayment(Reservation reservation, float amount) {
        JPanel panel = new JPanel(new GridBagLayout());
        // todo check if card expired?

        JTextField name = new JTextField("Name on Card: ");
        JTextField expMM = new JTextField("Expiry Month(MM): ");
        JTextField expYY = new JTextField("Expiry Year(YY): ");
        JTextField sec = new JTextField("Security Code: ");
        JButton submit = new JButton("Make Transaction");
        JTextField[] fields = new JTextField[]{
                name, expMM, expYY, sec
        };
        clearManyTexts(fields);

        submit.addActionListener(e -> {
            int month, year;
            try {
                month = Integer.parseInt(expMM.getText());
                year = Integer.parseInt(expYY.getText());
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Expiry Date");
                return;
            }

            String cardName = name.getText();
            if(cardName.isBlank() || cardName.equals("Name on Card: ")) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Name!");
                return;
            }

            int secCode;
            try {
                secCode = Integer.parseInt(sec.getText());
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Security Code");
                return;
            }

            SimpleDateFormat format = new SimpleDateFormat("MM/yy");
            try {
                Date date = format.parse(month + "/" + year);
            } catch (ParseException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Success!");
                return;
            }

            Object[] values;

            // reservation details to db
            values = new Object[]{
                    reservation.getCustomer().getClientId(),
                    reservation.getRemarks(),
                    dateToDB(reservation.getReservationDate()),
                    dateToDB(reservation.getRentDate()),
                    dateToDB(reservation.getReturnDate()),
                    reservation.getGst(),
                    reservation.getSubTotal()
            };

            try {
                ResultSet resultSet = valuedQuery(scripts.insertReservation, values);
                resultSet.close();
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Erorr: " + exception.getMessage());
                return;
            }

            // reservation entry just got inserted lets query the id now
            int reservationID;
            try {
                values = new Object[] {customer.getClientId()};
                ResultSet resultSet = valuedQuery(scripts.selectReservationID, values);
                resultSet.next();
                reservationID = resultSet.getInt("id");
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Erorr: " + exception.getMessage());
                return;
            }

            // reservation items details to db
            HashMap<String, Integer> reservationItems = reservation.getItems();
            HashMap<String, Item> items;
            try {
                items = dbToHashMap(false);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Erorr: " + exception.getMessage());
                return;
            }
            for(String description : reservationItems.keySet()) {
                Integer quantity = reservationItems.get(description);
                Item item = items.get(description);
                values = new Object[]{
                        reservationID, item.getId(), quantity
                };

                try {
                    ResultSet resultSet = valuedQuery(scripts.insertReservationItem, values);
                    resultSet.close();
                } catch (SQLException exception) {
                    JOptionPane.showMessageDialog(mainFrame, "Erorr: " + exception.getMessage());
                    return;
                }
            }

            // transaction details to db
            values = new Object[]{
                    amount, reservationID, month, year, secCode, cardName
            };

            try {
                ResultSet resultSet = valuedQuery(scripts.insertTransaction, values);
                resultSet.close();
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Erorr: " + exception.getMessage());
                return;
            }

            JOptionPane.showMessageDialog(mainFrame, "Success!");
            navigator.close();
            navigator.close();
            navigator.close();
            viewReservations("customer");
        });

        GuiPlacer guiPlacer = new GuiPlacer(400, 500);
        JComponent[] elements = new JComponent[]{
                name, getPadding(400, 5),
                expMM, getPadding(400, 5),
                expYY, getPadding(400, 5),
                sec, getPadding(400, 5),
                submit, getPadding(400, 5),
                back
        };
        guiPlacer.verticalPlacer(elements);
        JPanel container = guiPlacer.getContainer();
        panel.add(container);
        navigator.open(panel, "makePayment");
    }

    private void viewReservation(Reservation reservation, String userType, String info) {
        /*
        function to view a single reservation
         */

        Customer client = reservation.getCustomer();
        JLabel id = new JLabel("ID: " + reservation.getReservationId());
        JLabel clientID = new JLabel("Client ID: " + client.getClientId());
        JLabel clientName = new JLabel("Client Name: " + client.getName());
        JLabel remarks = new JLabel("Remarks: " + reservation.getRemarks());
        JLabel reservationDate = new JLabel("Reservation Date: " + getFDate(reservation.getReservationDate(), "dd-MMM-yy"));
        JLabel rentDate = new JLabel("Rent Date: " + getFDate(reservation.getRentDate(), "dd-MMM-yy"));
        JLabel returnDate = new JLabel("Return Date: " + getFDate(reservation.getReturnDate(), "dd-MMM-yy"));
        JLabel status = new JLabel("Status: " + reservation.getStatus());

        Component[] elements;
        if(reservation.getReservationId() == 0) {
            elements = new Component[]{
                    clientID, clientName, status, remarks, reservationDate,
                    rentDate, returnDate
            };
        } else {
            elements = new Component[]{
                    id, clientID, clientName, status, remarks, reservationDate,
                    rentDate, returnDate
            };
        }

        float gstValue;
        try {
            gstValue = getGst();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }

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
        JLabel perDay = new JLabel("Per Day");
        JLabel amount = new JLabel("For " + reservation.getDays() + " Days");

        JLabel durationLabel = new JLabel("#days");
        JLabel duration = new JLabel(String.valueOf(reservation.getDays()));
        JLabel subTotal = new JLabel("Subtotal");
        JLabel gstLabel = new JLabel("GST");
        JLabel subTotalAmount = new JLabel(String.valueOf(reservation.getSubTotal()));
        JLabel gstLabelAmount = new JLabel(String.valueOf(reservation.getGst()));
        JLabel gstRate = new JLabel(String.valueOf(gstValue));
        JLabel total = new JLabel("Total");
        JLabel totalAmount = new JLabel(String.valueOf(reservation.getTotal()));

        JLabel alreadyPaidLabel = new JLabel("Initially Paid");
        JLabel alreadyPaidAmount = new JLabel(String.valueOf(reservation.getPaid()));

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
        table.add(perDay, gbc);
        gbc.gridx = 4;
        table.add(amount, gbc);

        HashMap<String, Item> items;
        try {
            items = dbToHashMap(false);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }

        HashMap<String, Integer> reservationItems = reservation.getItems();
        for(String description : reservationItems.keySet()) {
//            Item item = items.get(x);
            Item item = items.get(description);
            int quantity = reservationItems.get(description);
            JLabel qty = new JLabel(String.valueOf(quantity));
            JLabel itemName = new JLabel(item.getDescription());
            JLabel rate = new JLabel(String.valueOf(item.getRate()));
            JLabel perDayAmount = new JLabel(String.valueOf(item.getRate()*quantity));
            JLabel allDaysAmount = new JLabel(String.valueOf(item.getRate()*quantity*reservation.getDays()));
            gbc.gridy++;

            gbc.gridx = 0;
            table.add(itemName, gbc);
            gbc.gridx = 1;
            table.add(qty, gbc);
            gbc.gridx = 2;
            table.add(rate, gbc);
            gbc.gridx = 3;
            table.add(perDayAmount, gbc);
            gbc.gridx = 4;
            table.add(allDaysAmount, gbc);
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

        JButton deleteApprove;
        if(info.equals("make")) {
            deleteApprove = new JButton("Submit");
            deleteApprove.addActionListener(e -> {
                makePayment(reservation, reservation.getTotal()/2);
            });
        } else {
            deleteApprove = new JButton("Delete");
            deleteApprove.addActionListener(e -> {
                // todo delete reservation from DB
                navigator.close();
                navigator.close();
                viewReservations(userType);
            });
        }

        GuiPlacer main = new GuiPlacer(400, 500);
        Component[] mainElements;
        switch (userType) {
            case "customer" -> mainElements = new Component[]{
                    container, getPadding(5, 5),
                    scrollPane, getPadding(5, 5),
                    container2, getPadding(5, 5),
                    deleteApprove, getPadding(5, 5),
                    back, getPadding(5, 5)
            };
            case "officerRent" -> {
                JButton rentOrder = new JButton("Record Rent Order");
                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        rentOrder, getPadding(5, 5),
                        deleteApprove, getPadding(5, 5),
                        back, getPadding(5, 5),
                };
            }
            case "officer" -> {
                JButton approve = new JButton("Approve");
                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        approve, getPadding(5, 5),
                        deleteApprove, getPadding(5, 5),
                        back, getPadding(5, 5),
                };
            }
            case "officerReturn" -> {
                JButton recordReturn = new JButton("Record Return Order");
                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        recordReturn, getPadding(5, 5),
                        deleteApprove, getPadding(5, 5),
                        back, getPadding(5, 5),
                };
            }
            default -> mainElements = new Component[]{};
        }

        main.verticalPlacer(mainElements);
        JPanel panel = main.getContainer();
        navigator.open(panel, "viewReservation");
    }

    private void recordReturnOrder() {
        // TODO query approved reservations from db
        //  if rentdate = today add a record button to gui
//        Item chair = new Item(1, 1, new Date(), "Chair", 50, stock, reserved, rented);
//        Item table22 = new Item(1, 1, new Date(), "Tabke", 50, stock, reserved, rented);
//        Item poopoo = new Item(1, 1, new Date(), "Poopopopo", 50, stock, reserved, rented);
//        Item nooo = new Item(1, 1, new Date(), "nooo", 50, stock, reserved, rented);
//        Item thisItem = new Item(1, 1, new Date(), "this", 50, stock, reserved, rented);

        HashMap<Item, Integer> fadas = new HashMap<>();
//        fadas.put(chair, 3);
//        fadas.put(table22, 2);
//        fadas.put(poopoo, 4);
//        fadas.put(nooo, 7);
//        fadas.put(thisItem, 2);

        ArrayList<Reservation> reservations = new ArrayList<>();
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks2", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks3", new Date(), new Date(), new Date()));
//        reservations.add(new Reservation(1, 1, fadas, "String remarks4", new Date(), new Date(), new Date()));

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
        importData.addActionListener(e -> {
            boolean[] loadedArray = {false};
            loadDataFiles("", loadedArray);
        });

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

    private void addUser() {
        JPanel panel = new JPanel(new GridBagLayout());
        JComboBox<Role> role = new JComboBox<>(Role.values());
        JPanel heading = new JPanel();
        heading.add(new JLabel("Add Employee"));
        JTextField name = new JTextField("Name");
        JTextField email = new JTextField("Email");
        JButton add = new JButton("Add Employee");
        JTextField[] textFields = new JTextField[]{email, name};
        clearManyTexts(textFields);
        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            navigator.close();
            navigator.close();
            userManagement();
        });

        add.addActionListener(e -> {
            String employeeEmail = email.getText();
            String employeeName = name.getText();
            if(!(employeeEmail.contains("@") || employeeEmail.contains("."))) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Email");
                return;
            }
            if (employeeName.equals("Name")) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Name");
                return;
            }
            Object[] values = new Object[]{employeeEmail};
            if(!getEntries(scripts.searchAllEmployees, values).equals("0")) {
                JOptionPane.showMessageDialog(mainFrame, "Email Already Taken");
                return;
            }

            String password = sha256("123");
            String employeeRole = String.valueOf(role.getSelectedItem());
            values = new Object[] {
                    employeeName, password, employeeEmail, employeeRole, "ENABLED"
            };
            try {
                valuedQuery(scripts.addEmployee, values);
                JOptionPane.showMessageDialog(mainFrame, "Success!");
                navigator.close();
                addUser();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, "SQL Error");
            }
        });

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] mainElements = {
                heading, getPadding(10, 5),
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
        JPanel table = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        table.setPreferredSize(new Dimension(650, 300));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty= 0;
        gbc.weightx= 1;
        table.add(new Label("Customers:"), gbc);
        gbc.gridy++;

        JComponent[] headings = new JComponent[] {
                new JLabel("ID"), new JLabel("Name"), new JLabel("Email"), new JLabel("Role/Type"),
                new JLabel("Status")
        };
        gbc.gridy++;
        for(int x = 0; x < headings.length; x++) {
            JComponent element = headings[x];
            gbc.gridx = x;
            table.add(element, gbc);
        }
        displayUsers(scripts.getAllClients, table, gbc);
        gbc.gridy++;
        table.add(getPadding(5, 10), gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        table.add(new Label("Employees:"), gbc);
        gbc.gridy++;
        displayUsers(scripts.getEmployees, table, gbc);

        JScrollPane scrollPane = scrollTable(table, 670, 300);

        GuiPlacer placer = new GuiPlacer(700, 320);
        JLabel title = new JLabel("Change Statuses of Accounts");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JComponent[] elements = new JComponent[] {
                title, getPadding(5,10),
                scrollPane, getPadding(5,10),
                back
        };
        placer.verticalPlacer(elements);
        JPanel panel = placer.getContainer();
        navigator.open(panel, "removeUser");
    }

    private void displayUsers(String script, JPanel table, GridBagConstraints gbc) {
        ResultSet resultSet;
        try {
            resultSet = noValueQuery(script);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }
        String unique;
        if(script.equals(scripts.getEmployees)) {
            unique = "role";
        } else {
            unique = "label";
        }

        gbc.gridy++;
        try {
            while(resultSet.next()) {
                int idRaw = resultSet.getInt("id");
                JLabel id = new JLabel(String.valueOf(idRaw));
                JLabel name = new JLabel(resultSet.getString("name"));
                JLabel email = new JLabel(resultSet.getString("email"));
                JLabel roleType = new JLabel(resultSet.getString(unique));
                JLabel status = new JLabel(resultSet.getString("status"));
                JButton change;
                if(resultSet.getString("status").equals("ENABLED")) {
                    change = new JButton("DISABLE");
                } else {
                    change = new JButton("ENABLE");
                }
                JComponent[] elements = new JComponent[]{id, name, email, roleType, status, change};
                change.addActionListener(e -> {
//                    String selectedOption = (String) JOptionPane.showInputDialog(mainFrame, "Select New Status", "Status",
//                            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    String selectedOption = change.getText();
                    if(selectedOption != null) {
                        selectedOption += "D";
                        Object[] values = new Object[]{selectedOption, idRaw};
                        try {
                            if(unique.equals("role")) {
                                valuedQuery(scripts.updateEmployeeStatus, values);
                            } else {
                                valuedQuery(scripts.updateClientStatus, values);
                            }
//                            JOptionPane.showMessageDialog(mainFrame, "Success!");
                            navigator.close();
                            removeUser();
                        } catch (SQLException exception) {
                            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
                        }
                    }
                });
                for(int x = 0; x < elements.length; x++) {
                    JComponent element = elements[x];
                    gbc.gridx = x;
                    table.add(element, gbc);
                }
                gbc.gridy++;
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
        }

        try {
            resultSet.close();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }
    }

    private String dateToDB(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    private String getEntries(String script, Object[] values) {
        try {
            ResultSet rs = valuedQuery(script, values);
            int rowCount = 0;
            if (rs.last()) {
                rowCount = rs.getRow();
                rs.beforeFirst(); // move the cursor back to the beginning
            }
            return String.valueOf(rowCount);
        } catch (SQLException exception) {
            return exception.getMessage();
        }
    }

    private void userManagement() {
        Object[] values = {"REQUESTED"};
        String pending = getEntries(scripts.getPendingClients, values);
        values = new Object[]{"ENABLED"};
        String currentClients = getEntries(scripts.getCurrentClients, values);
        values = new Object[]{"OFFICER", "ENABLED"};
        String currentOfficers = getEntries(scripts.getCurrentEmployees, values);
        values = new Object[]{"ADMINISTRATOR", "ENABLED"};
        String currentAdmins = getEntries(scripts.getCurrentEmployees, values);

        values = new Object[]{"DISABLED"};
        String disabledClients = getEntries(scripts.getCurrentClients, values);
        values = new Object[]{"OFFICER", "DISABLED"};
        String disabledOfficers = getEntries(scripts.getCurrentEmployees, values);
        values = new Object[]{"ADMINISTRATOR", "DISABLED"};
        String disabledAdmins = getEntries(scripts.getCurrentEmployees, values);

        JPanel panel = new JPanel(new GridBagLayout());
        JButton addUserButton = new JButton("Add User");
        JButton removeUserButton = new JButton("Remove User");
        JLabel heading = new JLabel("Accounts Statistics");
        Label numPendingCustomers = new Label("Pending Customer Accounts: " + pending);
        Label enabledCustomerAccounts = new Label("Enabled Customer Accounts: " + currentClients);
        Label disabledCustomerAccounts = new Label("Disabled Customer Accounts: " + disabledClients);
        Label enabledOfficerAccounts = new Label("Enabled Officer Accounts: " + currentOfficers);
        Label disabledOfficerAccounts = new Label("Disabled Officer Accounts: " + disabledOfficers);
        Label enabledAdminAccounts = new Label("Enabled Admin Accounts: " + currentAdmins);
        Label disabledAdminAccounts = new Label("Disabled Admin Accounts: " + disabledAdmins);
        JButton back = new JButton("Back");

        addUserButton.addActionListener(e -> addUser());
        removeUserButton.addActionListener(e -> removeUser());
        back.addActionListener(e -> navigator.close());

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] mainElements = {
                heading, getPadding(10, 5),
                numPendingCustomers, getPadding(10, 5),
                enabledCustomerAccounts, getPadding(10, 5),
                disabledCustomerAccounts, getPadding(10, 5),
                enabledOfficerAccounts, getPadding(10, 5),
                disabledOfficerAccounts, getPadding(10, 5),
                enabledAdminAccounts, getPadding(10, 5),
                disabledAdminAccounts, getPadding(10, 5),
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
        JButton add = new JButton("Add Item");
        JButton adjust = new JButton("Adjust Inventory");
        JButton back = new JButton("Back");
        back.addActionListener(e -> navigator.close());
        add.addActionListener(e -> addItem());
        adjust.addActionListener(e -> viewItems());

        GuiPlacer mainPlacer = new GuiPlacer(400, 500);
        JComponent[] mainElements = new  JComponent[]{
                add, getPadding(10, 5),
                adjust, getPadding(10, 5),
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
        JTextField qty = new JTextField("Quantity");
        JTextField[] fields = new JTextField[]{description, rate, qty};
        clearManyTexts(fields);

        JComponent[] elements = new JComponent[]{
                heading, getPadding(10, 5),
                description, getPadding(10, 5),
                rate, getPadding(10, 5),
                qty, getPadding(10, 5),
                add, getPadding(10, 5),
                back
        };

        add.addActionListener(e -> {
            int quantity;
            float rateValue;
            try {
                quantity = Integer.parseInt(qty.getText());
                rateValue = Float.parseFloat(qty.getText());
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
                return;
            }

            String descriptionValue = description.getText();
            Object[] values = new Object[]{descriptionValue};
            String entries = getEntries(scripts.checkItem, values);
            if(!entries.equals("0")) {
                JOptionPane.showMessageDialog(mainFrame, "Item already exists");
                return;
            }
//            "INSERT INTO item (description, rate, created_by, created_on, " +
//                    "stock, available, reserved, rented) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";


            values = new Object[]{
                    descriptionValue, rateValue, employee.getId(), dateToDB(new Date()),
                    quantity, quantity, 0, 0};
            try {
                valuedQuery(scripts.insertItem, values);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(mainFrame, "Success!!!");
            navigator.close();
            addItem();
        });

        GuiPlacer placer = new GuiPlacer(400,500);
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "addItem");
    }

    private ResultSet noValueQuery(String script) throws SQLException {
        /*
        Query only a predetermined statement from db with no values
         */
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(script);
    }

    private ResultSet valuedQuery(String script, Object[] values) throws SQLException {
        /*
        Query only a predetermined statement from db with values
         */
        PreparedStatement statement = connection.prepareStatement(script);
        for(int x = 0; x < values.length; x++) {
            String value = String.valueOf(values[x]);
            statement.setString(x+1,  value);
        }
        return statement.executeQuery();
    }

    private HashMap<String, Item> dbToHashMap(boolean num) throws SQLException {
        ResultSet data = noValueQuery(scripts.getItems);
        HashMap<String, Item> map = new HashMap<>();

        while (data.next()){
            int id = data.getInt("id");
            String description = data.getString("description");
            float rate = data.getFloat("rate");
            int createdBy = data.getInt("created_by");
            Date createdOn = data.getDate("created_on");
            int stock = data.getInt("stock");
            int reserved = data.getInt("reserved");
            int available = data.getInt("available");
            int rented = data.getInt("rented");
            Item item = new Item(
                    id, description, rate, createdBy, createdOn,
                    stock, available, reserved, rented
            );
            if(num) {
                map.put(String.valueOf(item.getId()), item);
            } else {
                map.put(description, item);
            }
        }

        data.close();
        return map;
    }

    private ArrayList<Object[]> csvToList(String filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        CSVParser csvParser = new CSVParser(fileReader, csvFormat);
        List<CSVRecord> csvRecords = csvParser.getRecords();
        ArrayList<Object[]> items = new ArrayList<>();

        for (CSVRecord csvRecord : csvRecords) {
            String description;
            float rate;
            int qty;

            try {
                description = csvRecord.get("description");
                String rawRate = csvRecord.get("rate");
                String rawQty = csvRecord.get("adding_qty");
                rate = Float.parseFloat(rawRate);
                qty = Integer.parseInt(rawQty);
            } catch (IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
                csvParser.close();
                fileReader.close();
                return new ArrayList<>();
            }
            Object[] item = new Object[]{description, rate, qty};
            items.add(item);
        }
        csvParser.close();
        fileReader.close();
        return items;
    }

    private void loadDataFiles(String csvFile, boolean[] loadedArray) {
        JPanel panel = new JPanel(new GridBagLayout());
        HashMap<String, Item> items;
//        final boolean[] loadedArray = {false};
        try {
            items = dbToHashMap(false);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }

        ArrayList<Object[]> itemList;
        if(!csvFile.equals("")) {
            try {
                itemList = csvToList(csvFile);
            } catch (IOException exception) {
                itemList = new ArrayList<>();
                JOptionPane.showMessageDialog(mainFrame, "File Not Found!");
            }
        } else {
            itemList = new ArrayList<>();
        }

        JPanel table = new JPanel(new GridBagLayout());
        GridBagConstraints tableGbc = new GridBagConstraints();
        tableGbc.gridx = 0;
        tableGbc.gridy = 0;
        tableGbc.weightx = 1;
        tableGbc.weighty = 1;

        itemTable(table, tableGbc, "Items Before", items, true);
        tableGbc.gridy++;
        table.add(getPadding(10,40), tableGbc);

        HashMap<String, Item> itemsBackup = new HashMap<>(items); // create backup in case of BelowZeroError
        for (Object[] row : itemList) {
            String description = (String) row[0];
            float newRate = (float) row[1];
            int qty = (int) row[2];

            try {
                Item item = items.get(description);
                item.adjustStock(qty);
                item.adjustAvailable(qty);
            } catch (NullPointerException exception) {
                Item item = new Item(
                        -1, description, newRate, employee.getId(), new Date(),
                        qty, qty, 0, 0
                        );
                items.put(description, item);
            } catch (BelowZeroError exception) {
                JOptionPane.showMessageDialog(mainFrame, exception);
                items = itemsBackup;
                break;
            }
        }

        itemTable(table, tableGbc, "Items After Approval", items, loadedArray[0]);
        tableGbc.gridy++;
        table.add(getPadding(10,40), tableGbc);

        JButton approve = new JButton("Approve Changes");
        JButton load = new JButton("Load CSV File");
        JScrollPane scroll = scrollTable(table, 720, 450);

        JComponent[] elements = new JComponent[]{
                scroll,  getPadding(10, 5),
                load, getPadding(10, 5),
                approve,  getPadding(10, 5),
                back
        };
        GuiPlacer mainPlacer = new GuiPlacer(750, 600);
        mainPlacer.verticalPlacer(elements);
        JPanel container = mainPlacer.getContainer();

        load.addActionListener(e -> {
            JFrame frame = new JFrame("Select CSV File");

            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();
                loadedArray[0] = true;
                navigator.close();
                loadDataFiles(selectedFile, loadedArray);
            }
        });
        final HashMap<String, Item> toDB = items;
        approve.addActionListener(e -> {
            for(Item item : toDB.values()) {
                if(item.getId() == -1) {
                    Object[] values = new Object[]{
                        item.getDescription(), item.getRate(), item.getCreatedBy(),
                        dateToDB(item.getDate()), item.getStock(), item.getAvailable(),
                        item.getReserved(), item.getRented()
                    };
                    try {
                        ResultSet rs = valuedQuery(scripts.insertItem, values);
                        rs.close();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(mainFrame, "SQL Error");
                    }
                } else {
                    Object[] values = new Object[]{
                            item.getRate(), item.getStock(), item.getAvailable(),
                            item.getId()
//                              "UPDATE item SET rate = ?, stock = ?, available = ? WHERE id = ?";
                    };
                    try {
                        ResultSet rs = valuedQuery(scripts.updateItem, values);
                        rs.close();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(mainFrame, "SQL Error");
                    }
                }
            }
            navigator.close();
            boolean[] loadedArray1 = {false};
            loadDataFiles("", loadedArray1);
        });

        panel.add(container);
        navigator.open(panel, "loadDataFiles");
    }

    private JScrollPane scrollTable(JPanel table, int width, int height) {
        JScrollPane scrollPane = new JScrollPane(table);
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 8);
        scrollPane.setPreferredSize(new Dimension(width, height));
        scrollPane.setMinimumSize(new Dimension(width, height));
        scrollPane.setViewportView(table);

        return scrollPane;
    }

    private void itemTable(JPanel table, GridBagConstraints tableGbc, String heading,
            HashMap<String, Item> items, boolean loaded
            ) {

        tableGbc.gridy++;
        tableGbc.gridx=1;
        JLabel details = new JLabel(heading);
        table.add(details, tableGbc);

        JLabel idHeading = new JLabel("ID");
        JLabel descriptionHeading = new JLabel("Description");
        JLabel rateHeading = new JLabel("Rate");
        JLabel createdHeading = new JLabel("Created By");
        JLabel dateHeading = new JLabel("Created On");
        JLabel stockHeading = new JLabel("In Stock");
        JLabel availableHeading = new JLabel("Available");
        JLabel rentedHeading = new JLabel("Rented");
        JComponent[] headings = new JComponent[]{
                idHeading, descriptionHeading, rateHeading, createdHeading,
                dateHeading, stockHeading, availableHeading
        };
        tableGbc.gridy++;
        for(int x = 0; x < headings.length; x++) {
            tableGbc.gridx = x;
            JComponent element = headings[x];
            table.add(element, tableGbc);
        }

        if (!loaded) {
            return;
        }
        for(Item item : items.values()) {
            tableGbc.gridy++;
            String idString;
            int idInt = item.getId();
            if(idInt == -1) {
                idString = "NEW";
            } else {
                idString = Integer.toString(idInt);
            }
            JLabel id = new JLabel(idString);
            JLabel description = new JLabel(item.getDescription());
            JLabel rate = new JLabel(String.valueOf(item.getRate()));
            JLabel createdBy = new JLabel(String.valueOf(item.getCreatedBy()));
            JLabel createdOn = new JLabel(getFDate(item.getDate(), "dd-MMM-yy"));
            JLabel stock = new JLabel(String.valueOf(item.getStock()));
            JLabel available = new JLabel(String.valueOf(item.getAvailable()));
            JLabel rented = new JLabel(String.valueOf(item.getRented()));
            JLabel reserved = new JLabel(String.valueOf(item.getReserved()));

            JComponent[] elements = new JComponent[]{
                    id, description, rate, createdBy, createdOn, stock,
                    available
            };
            for(int x = 0; x < elements.length; x++) {
                tableGbc.gridx = x;
                JComponent element = elements[x];
                table.add(element, tableGbc);
            }
        }
    }

    private void editItem(Item item) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel id = new JLabel("ID: " + item.getId());
        JTextField description = new JTextField(item.getDescription());
        JTextField rate = new JTextField(String.valueOf(item.getRate()));
        JTextField qty = new JTextField("Amount");
        JButton update = new JButton("Update");
        JButton back = new JButton("Back");
        clearTextField(qty);
        JComponent[] elements = new JComponent[]{
            id, description, rate, qty, update, back
        };
        back.addActionListener(e -> {
            navigator.close();
            navigator.close();
            viewItems();
        });

        update.addActionListener(e -> {
            int quantity;
            float rateValue;

            try {
                quantity = Integer.parseInt(qty.getText());
                rateValue = Float.parseFloat(rate.getText());
                if(rateValue < 1) {
                    throw new BelowZeroError("Rate Below One");
                }
            } catch (NumberFormatException | BelowZeroError exception) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid values for quantity and or rate");
                return;
            }

            try {
                item.adjustAvailable(quantity);
                item.adjustStock(quantity);
            } catch (BelowZeroError exception) {
                JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
                return;
            }
            item.setDescription(description.getText());
            item.setRate(rateValue);

            Object[] values = new Object[] {
                    item.getDescription(), item.getRate(),
                    item.getStock(), item.getAvailable(),
                    item.getId()
            };
            try {
                valuedQuery(scripts.updateItem2, values);
                JOptionPane.showMessageDialog(mainFrame, "Success");
                navigator.close();
                editItem(item);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
            }
        });

        GuiPlacer placer = new GuiPlacer(400, 500);
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "editItem");
    }

    private void viewItems() {
        JPanel panel = new JPanel(new GridBagLayout());

        HashMap<String, Item> items;
        try {
            items = dbToHashMap(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
            return;
        }

        final GridBagConstraints gbc = new GridBagConstraints();
        JPanel table = new JPanel(new GridBagLayout());
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 0;

        JLabel idHeading = new JLabel("ID");
        JLabel descriptionHeading = new JLabel("Description");
        JLabel rateHeading = new JLabel("Rate");
        JLabel createdByHeading = new JLabel("Created By");
        JLabel createdOnHeading = new JLabel("Created On");

        JComponent[] elements = new JComponent[]{
                idHeading,
                descriptionHeading,
                rateHeading,
                createdByHeading,
                createdOnHeading,
        };
        for(int x = 0; x < elements.length; x++) {
            gbc.gridx = x;
            JComponent element = elements[x];
            table.add(element, gbc);
        }

        for(Item item : items.values()) {
            gbc.gridy++;
            JLabel id = new JLabel(String.valueOf(item.getId()));
            JLabel description = new JLabel(item.getDescription());
            JLabel rate = new JLabel(String.valueOf(item.getRate()));
            JLabel createdBy = new JLabel(String.valueOf(item.getCreatedBy()));
            JLabel createdOn = new JLabel(getFDate(item.getDate(), "dd-MMM-yy"));
            JButton edit = new JButton("Adjust");

            JComponent[] tableElements = new JComponent[]{
                    id, description, rate, createdBy, createdOn, edit
            };
            edit.addActionListener(e -> editItem(item));

            for(int i = 0; i < tableElements.length; i++) {
                gbc.gridx = i;
                JComponent element = tableElements[i];
                table.add(element, gbc);
            }
        }
        JScrollPane scroll = scrollTable(table, 380,480);
        JLabel title = new JLabel("Current Inventory");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        elements = new JComponent[]{
                title, getPadding(10, 5),
                scroll, getPadding(10, 5),
                back
        };
        GuiPlacer placer = new GuiPlacer(400,550);
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();
        panel.add(container);
        navigator.open(panel, "removeItem");
    }

    private String getFDate(Date date, String format) {
        /// get formatted date into a string
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static void main(String[] args) throws SQLException {
        new PartyRental();
    }
}