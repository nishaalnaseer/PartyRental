// sql and db
import java.sql.*;

import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.mariadb.jdbc.Driver;

// hashing
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// csv dependencies
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

// json dependencies
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;


// scraping dependencies
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// GUI
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

// others
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

    private final JSONParser jsonParser = new JSONParser();
    private String mastodonToken;
    private String mastodonServer;

    private Employee employee;
    private Customer customer;
    private final Connection connection;

    PartyRental() throws SQLException {
        boolean exit = false;

        String username = "";
        String password = "";
        String connectionString = "";

        mainFrame.setSize(800, 700);
        mainFrame.setMinimumSize(new Dimension(700, 700));
        mainFrame.getContentPane().setLayout(cardLayout);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            FileReader fileReader = new FileReader("config/config.json");
            JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);

            mastodonToken = (String) jsonObject.get("mastodon_token");
            mastodonServer = (String) jsonObject.get("mastodon_server");
            String db_host = (String) jsonObject.get("db_host");
            long db_port = (long) jsonObject.get("db_port");
            String database = (String) jsonObject.get("database");
            username = (String) jsonObject.get("username");
            password = (String) jsonObject.get("password");

            connectionString = "jdbc:mariadb://" + db_host + ":" + db_port + "/" + database;

            fileReader.close();
        } catch (IOException | org.json.simple.parser.ParseException e) {
            System.out.println("Error reading JSON file, exiting APP now");
            JOptionPane.showMessageDialog(mainFrame, "Error reading JSON file, exiting APP now");
            DriverManager.registerDriver(new Driver());
            e.printStackTrace();
            exit = true;
        }

        DriverManager.registerDriver(new Driver());
        connection = DriverManager.getConnection(connectionString, username, password);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)  {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        if (exit) {
            // exit if json doesnt read
            return;
        }

        loginPage();
        
        back.addActionListener(e -> navigator.close());
        logout.addActionListener(e -> {
            customer = null;
            employee = null;
            navigator.close();
        });

        mainFrame.setVisible(true);
    }

    private String sha256(String input) {
        /*
        get sha256 hash of input
         */
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
        /*
        display login page
         */
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel label = new JLabel("Sign In");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField usernameField = new JTextField("nishawl.naseer2@outlook.com");
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
        /*
        clear a text field when clicked on
         */
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
        /*
        clear a password field when clicked on
         */
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
        /*
        Clear a text and password field when clicked on
         */
        clearTextField(textField);
        clearPasswordField(passwordField);
    }

    private void clearManyTexts(JTextField[] textFields) {
        /*
        enter an array of textfield, when clicked on they will clear themselves when first
        clicked on
         */
        for (JTextField textField : textFields) {
            clearTextField(textField);
        }
    }

    private boolean checkUser(String[] scripts, String username) {
        /* check if user is on db
        * */
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
        /*
        validate user credentials
         */
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
        /*
        customer account creation page
         */
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
        /*
        a 'padding' which is actually a Jlabel
         */
        JLabel padding = new JLabel();
        padding.setPreferredSize(new Dimension(width, height));
        padding.setMinimumSize(new Dimension(width, height));
        padding.setMaximumSize(new Dimension(width, height));
        return padding;
    }

    private void officerPage() {
        /*
        display officers page
         */
        JPanel panel = new JPanel(new GridBagLayout());
        JButton makeReservationButton = new JButton("Make Reservation");
        JButton viewReservationButton = new JButton("View Reservations");
        JButton registrations = new JButton("Approve Registrations");
        JButton returnOrder = new JButton("Return Order");
        JButton rentOrder = new JButton("Rent Order");
        JButton returns = new JButton("Returns");
        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
            registrations, getPadding(5, 5),
            viewReservationButton, getPadding(5, 5),
            rentOrder, getPadding(5, 5),
            returnOrder, getPadding(5, 5),
            returns, getPadding(5, 5),
            logout
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        returns.addActionListener(e -> viewCompletedReservations());

        HashMap<String, Integer> itemsForReservation = new HashMap<>();
        Object[] selectedStartDate = new String[]{"Select Day", "Select Month", "Select Year"};
        Object[] selectedEndDate = new String[]{"Select Day", "Select Month", "Select Year"};
        makeReservationButton.addActionListener(e ->
                createReservation(itemsForReservation, 0, 0, selectedStartDate, selectedEndDate, "officer")
        );
        viewReservationButton.addActionListener(e ->
            viewReservations("officer")
        );
        registrations.addActionListener(e -> approveRegistration());
        rentOrder.addActionListener(e -> recordRentOrder());
        returnOrder.addActionListener(e -> recordReturnOrder());

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void recordRentOrder() {
        /*
        recording rent order page
         */
        ArrayList<Reservation> reservations;
        try {
            reservations = getReservations(
                    scripts.selectReservedReservations, new Object[]{}, ""
            );
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }

        for(int x = 0; x < reservations.size(); x++) {
            Reservation reservation = reservations.get(0);
            reservation.setStatus("RESERVED");
        }

        displayReservations(reservations, "recordRent", "officerRent");
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
        /*
        if a requested customers account creation form is not up to standard
        and an employee rejects it, this can be called to delete that entry from db
        this is done so the user can request again without any issues
         */
        Object[] values = new Object[]{id};
        valuedQuery(scripts.delRequestClient, values);
    }

    private void customerApproveReject(Customer customer) {
        /*
        display page to reject or approve a customers account creation
         */
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

    private void rentingReservation() {
        /*
        display reservations with RESERVED status
         */

        ArrayList<Reservation> reservations;
        try {
            Object[] values = new Object[]{customer.getClientId()};
            reservations = getReservations(scripts.selectRentingReservations, values, "no");
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }

        displayReservations(reservations, "rentingReservations", "customerRentingPay");
    }

    private void customerPage() {
        /*
        display customer page
         */
        JPanel panel = new JPanel();
        JButton makeReservationButton = new JButton("Make Reservation");
        JButton viewReservationButton = new JButton("View Reservations");
        JButton payRenting = new JButton("Pay Renting");

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                makeReservationButton, getPadding(10, 5),
                payRenting, getPadding(10, 5),
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
        viewReservationButton.addActionListener(e ->
            viewReservations("customer")
        );
        payRenting.addActionListener(e -> rentingReservation());

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void dateFormatter(HashMap<String, Integer> itemsForReservation, DatePicker datePicker1,
                               DatePicker datePicker2, Item item, String userType, Integer amount, float finalGstValue) {
        /*
        date objects is inserted into gui and before reservation it can be read
         */
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

        if(daysBetween < 1) {
            itemsForReservation.put(item.getDescription(), amount);
            navigator.close();
            createReservation(itemsForReservation, finalGstValue, 0, datePicker1.getDateRaw(), datePicker2.getDateRaw(), userType);
            return;
        }
        itemsForReservation.put(item.getDescription(), amount);
        navigator.close();
        createReservation(itemsForReservation, finalGstValue, daysBetween, datePicker1.getDateRaw(), datePicker2.getDateRaw(), userType);
    }

    private float getGst(String userType, String cType) throws SQLException {
        /*
        get the gst of a customer type

         */
        Object[] values;
        if(userType.equals("customer")) {
            values = new Object[]{customer.getType()};
        } else {
            values = new Object[]{cType};
        }

        ResultSet resultSet = valuedQuery(scripts.getGstRate, values);
        resultSet.next();
        float gstValue = resultSet.getFloat("tax");
        resultSet.close();
        return gstValue;
    }

    private void createReservation(
            HashMap<String, Integer> itemsForReservation, float gstValue, long days,
            Object[] selectedStartDate, Object[] selectedEndDate, String userType

    ) {
        /*
        display create a reservation page
         */
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
                gstValue = getGst(userType, "");
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
                if((amount += prevQty) < 0) {
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

            if (value<1){
                continue;
            }

            float amountValue = value * item.getRate();

            JLabel name =  new JLabel(item.getDescription());
            JLabel valueHolder =  new JLabel(value.toString());
            JLabel rate =  new JLabel(String.valueOf(item.getRate()));
            JLabel amount = new JLabel(String.valueOf(amountValue));
            JLabel allDaysLabel = new JLabel(String.valueOf(value*days*item.getRate()));

            JButton edit = new JButton("Edit");
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

            LocalDate today = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysAhead = ChronoUnit.DAYS.between(today, date1);

            if(daysAhead < 2) {
                JOptionPane.showMessageDialog(mainFrame, "Reservation needs to be done 2 before renting!");
                return;
            }

            if(daysBetween < 1) {
                JOptionPane.showMessageDialog(mainFrame, "Rent Duration Can Not be Negative!");
                return;
            }

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

    private void deletingReservation(Reservation reservation, String userType) throws SQLException {
        /*
        delete a reservatition
         */
        String script;

        switch (userType) {
            case "customer", "customerRentingPay" -> script = scripts.cancelReservation;
            case "officer" -> script = scripts.denyReservation;
            case "officerRent" -> script = scripts.setLeasingCancelled;
            default -> script = "";
        }

        ResultSet resultSet = valuedQuery(script, new Object[]{reservation.getReservationId()});
        resultSet.close();
    }

    private void displayReservations(ArrayList<Reservation> reservations, String panelDesc, String userType) {
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
            JLabel returningDate = new JLabel(getFDate(reservation.getReturnDate(), "dd-MMM-yy"));
            JButton delete = new JButton("Delete");
            JButton view = new JButton("View");

            JComponent[] elements;
            if (!userType.equals("officerReturn")) {
                elements = new JComponent[]{
                        id, name, remarks, reservationDate, rentingDate, returningDate,
                        view,
                };
            } else {
                elements = new JComponent[]{
                        id, name, remarks, reservationDate, rentingDate, returningDate,
                        view, delete
                };
            }
            delete.addActionListener(e -> {
                try {
                    deletingReservation(reservation, userType);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                }
                navigator.close();
                viewReservations(userType);
            });
            view.addActionListener(e ->
                viewReservation(reservation, userType, "made")
            );
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
        /*
        get a customer from db result
         */
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        String status = resultSet.getString("status");
        String type = resultSet.getString("label");

        return new Customer(id, name, password, email, type, status);
    }

    ArrayList<Reservation> getReservations(String script, Object[] values, String userType) throws SQLException {
        /*
        get reservations from db
         */

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

            if(userType.equals("customerPay")) {
                // employee is checking for today's reservation that needs to be rented but this check
                // skips reservations not fully paid

                if(paid == subtotal+gst) {
                    continue;
                }
            }

            String rentDateString = getFDate(rentDate, "dd-MM-yyyy");
            String returnDateString = getFDate(returnDate, "dd-MM-yyyy");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
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
                reservations = getReservations(
                        scripts.selectClientReservations, new Object[]{customer.getClientId()}, userType
                );
            } else {
                reservations = getReservations(scripts.selectReservationsOnStatus, new Object[]{"REQUESTED"}, userType);
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }

        displayReservations(reservations, "viewReservations", userType);
    }

    private void makePayment(Reservation reservation, float amount, String time) {
        /*
        display payment screen
         */
        JPanel panel = new JPanel(new GridBagLayout());
        // todo check if card expired?

        JTextField name = new JTextField("Name on Card: ");
        JTextField expMM = new JTextField("Expiry Month(MM): ");
        JTextField expYY = new JTextField("Expiry Year(YY): ");
        JTextField sec = new JTextField("Security Code: ");
        JButton submit = new JButton("Make Transaction");
        JButton back = new JButton("Back");
        JTextField[] fields = new JTextField[]{
                name, expMM, expYY, sec
        };
        back.addActionListener(e -> navigator.close());
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
                    reservation.getSubTotal(),
                    reservation.getTotal() / 2
            };
            int reservationID;
            if(time.equals("first")) {
                try {
                    ResultSet resultSet = valuedQuery(scripts.insertReservation, values);
                    resultSet.close();
                } catch (SQLException exception) {
                    JOptionPane.showMessageDialog(mainFrame, "Error: " + exception.getMessage());
                    return;
                }

                try {
                    values = new Object[] {customer.getClientId()};
                    ResultSet resultSet = valuedQuery(scripts.selectReservationID, values);
                    resultSet.next();
                    reservationID = resultSet.getInt("id");
                } catch (SQLException exception) {
                    JOptionPane.showMessageDialog(mainFrame, "Error: " + exception.getMessage());
                    return;
                }

                // reservation items details to db
                HashMap<String, Integer> reservationItems = reservation.getItems();
                HashMap<String, Item> items;
                try {
                    items = dbToHashMap(false);
                } catch (SQLException exception) {
                    JOptionPane.showMessageDialog(mainFrame, "Error: " + exception.getMessage());
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
                        JOptionPane.showMessageDialog(mainFrame, "Error: " + exception.getMessage());
                        return;
                    }
                }
            } else {
                try {
                    reservationID = reservation.getReservationId();
                    ResultSet resultSet = valuedQuery(
                            scripts.selectPreviousTransactions, new Object[]{reservationID});
                    float paid = 0;
                    while(resultSet.next()) {
                        paid += resultSet.getFloat("amount");
                    }
                    resultSet.close();

                    ResultSet updateSet = valuedQuery(
                            scripts.updateReservationPayment, new Object[]{paid + amount, reservationID}
                            );
                    updateSet.close();
                } catch (SQLException exception) {
                    JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
                    return;
                }
            }

            values = new Object[]{
                    amount, reservationID, month, year, secCode, cardName
            };
            try {
                ResultSet resultSet = valuedQuery(scripts.insertTransaction, values);
                resultSet.close();
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, "Error: " + exception.getMessage());
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
            gstValue = getGst(userType, client.getType());
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

        JLabel alreadyPaidLabel = new JLabel("Total Paid");
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
        JButton pay = new JButton("Pay Remaining");
        pay.addActionListener(e -> makePayment(reservation, reservation.getTotal() - reservation.getPaid(), "second"));
        JButton deleteApprove;
        if(info.equals("make")) {
            deleteApprove = new JButton("Submit");
            deleteApprove.addActionListener(e ->
                makePayment(reservation, reservation.getTotal()/2, "first")
            );
        } else {
            deleteApprove = new JButton("Delete");
            deleteApprove.addActionListener(e -> {
                try
                {
                    HashMap<String, Integer>  itemsUsed = reservation.getItems();
                    ResultSet deleteSet;

                    if (userType.equals("officerRent") || userType.equals("customerRentingPay")) {
                        for(String description : itemsUsed.keySet()) {
                            int quantity = itemsUsed.get(description);
                            Item dbItem = items.get(description);
                            int itemID = dbItem.getId();

                            deleteSet = valuedQuery(scripts.selectItem, new Object[]{itemID});
                            deleteSet.next();

                            int available = deleteSet.getInt("available");
                            int reserved = deleteSet.getInt("reserved");
                            int rented = deleteSet.getInt("rented");

                            if (reserved - quantity < 0) {
                                JOptionPane.showMessageDialog(mainFrame, dbItem.getDescription() + " qty is below zero");
                                return;
                            } else {
                                reserved -= quantity;
                                available += quantity;
                            }

                            deleteSet.close();
//                    "UPDATE item SET available = ?, reserved = ?, rented = ?" +
//                            " WHERE id = ?"
                            Object[] updateValues = new Object[]{available, reserved, rented, itemID};
                            ResultSet updateSet = valuedQuery(scripts.updateInventory, updateValues);
                            updateSet.close();
                        }
                    }

                    deletingReservation(reservation, userType);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                }
                navigator.close();
                navigator.close();
                viewReservations(userType);
            });
        }

        String script;
        JButton variedButton;
        Object[] values;
        GuiPlacer main = new GuiPlacer(400, 500);
        Component[] mainElements;
        switch (userType) {
            case "customer" -> {
                variedButton = new JButton("");
                script = "";
                values = new Object[]{};
                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        deleteApprove, getPadding(5, 5),
                        back, getPadding(5, 5)
                };
            }
            case "officerRent" -> {
                variedButton = new JButton("Record Rent Order");
                script = scripts.setRented;
                values = new Object[]{employee.getId(), reservation.getReservationId()};
                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        variedButton, getPadding(5, 5),
                        deleteApprove, getPadding(5, 5),
                        back, getPadding(5, 5),
                };
            }
            case "officer" -> {
                script = scripts.approveReservation;
                variedButton = new JButton("Approve Reservation");
                values = new Object[]{employee.getId(), reservation.getReservationId()};
                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        variedButton, getPadding(5, 5),
                        deleteApprove, getPadding(5, 5),
                        back, getPadding(5, 5),
                };
            }
            case "officerReturn" -> {
                variedButton = new JButton("Record Return Order");
                script = scripts.setReturned;
                values = new Object[]{employee.getId(), reservation.getReservationId()};
                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        variedButton, getPadding(5, 5),
                        back, getPadding(5, 5),
                };
            }
            case "customerRentingPay" -> {
                variedButton = new JButton("Delete");  // just so compiler can be sane
                script = ""; // just so compiler can be sane
                values = new Object[]{}; // just so compiler can be sane

                mainElements = new Component[]{
                        container, getPadding(5, 5),
                        scrollPane, getPadding(5, 5),
                        container2, getPadding(5, 5),
                        pay, getPadding(5, 5),
                        deleteApprove, getPadding(5, 5),
                        back, getPadding(5, 5),
                };
            }
            default -> {
                mainElements = new Component[]{};
                script = "";
                variedButton = new JButton();
                values = new Object[]{};
            }
        }

        variedButton.addActionListener(e -> {
            try {
                ResultSet itemSet;
                final  HashMap<String, Integer> itemsUsed =  reservation.getItems();
                for(String description : itemsUsed.keySet()) {
                    int quantity = itemsUsed.get(description);
                    Item dbItem = items.get(description);
                    int itemID = dbItem.getId();

                    itemSet = valuedQuery(scripts.selectItem, new Object[]{itemID});
                    itemSet.next();

                    int available = itemSet.getInt("available");
                    int reserved = itemSet.getInt("reserved");
                    int rented = itemSet.getInt("rented");

                    switch (userType) {
                        case "officer" -> {
                            if (available - quantity < 0) {
                                JOptionPane.showMessageDialog(mainFrame, dbItem.getDescription() + " qty is below zero");
                                return;
                            } else {
                                available -= quantity;
                                reserved += quantity;
                            }
                        }
                        case "officerRent" -> {
                            if (reserved - quantity < 0) {
                                JOptionPane.showMessageDialog(mainFrame, dbItem.getDescription() + " qty is below zero");
                                return;
                            } else {
                                reserved -= quantity;
                                rented += quantity;
                            }
                        }
                        case "officerReturn" -> {
                            if (rented - quantity < 0) {
                                JOptionPane.showMessageDialog(mainFrame, dbItem.getDescription() + " qty is below zero");
                                return;
                            } else {
                                rented -= quantity;
                                available += quantity;
                            }
                        }
                        // do nothing
                        default -> {
                            JOptionPane.showMessageDialog(mainFrame, dbItem.getDescription() + "userType: " + userType);
                            return;
                        }
                    }
                    itemSet.close();

                    Object[] updateValues = new Object[]{available, reserved, rented, itemID};
                    ResultSet updateSet = valuedQuery(scripts.updateInventory, updateValues);
                    updateSet.close();
                }

                ResultSet resultSet = valuedQuery(
                        script, values
                );
                resultSet.close();
                navigator.close();
                navigator.close();
                viewReservations(userType);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            }
        });

        main.verticalPlacer(mainElements);
        JPanel panel = main.getContainer();
        navigator.open(panel, "viewReservation");
    }

    private void recordReturnOrder() {
        /*
        display record return order screen
         */

        ArrayList<Reservation> reservations;
        try {
            reservations = getReservations(
                    scripts.selectRentedReservations, new Object[]{}, "no"
            );
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }
        displayReservations(reservations, "recordReturnOrder", "officerReturn");
    }

    private void adminPage() {
        /*
        display admin page
         */
        JPanel panel = new JPanel(new GridBagLayout());
        JButton management = new JButton("User Management");
        JButton sales = new JButton("Sales Report");
        JButton inventory = new JButton("Inventory Management");
        JButton importData = new JButton("Import Data");
        JButton mastodonScrape = new JButton("Search Mastodon");

        management.addActionListener(e -> userManagement());
        sales.addActionListener(e -> salesReportOption());
        inventory.addActionListener(e -> inventoryManagement());
        importData.addActionListener(e -> {
            boolean[] loadedArray = {false};
            loadDataFiles("", loadedArray);
        });
        mastodonScrape.addActionListener(e -> mastodonSearch());

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                management, getPadding(10, 5),
                sales, getPadding(10, 5),
                inventory, getPadding(10, 5),
                importData, getPadding(10, 5),
                mastodonScrape, getPadding(10, 5),
                logout
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "createAccount");
    }

    private void addUser() {
        /*
        display add user page
         */
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
        /*
        display remove user page
         */
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
        /*
        display users page
         */
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
        }
    }

    private String dateToDB(Date date) {
        /*
        turn date from db to string
         */
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    private String getEntries(String script, Object[] values) {
        /*
        get the number of rows of a script on db
         */
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
        /*
        display user management page
         */
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
        /*
        dispaly inventory management screen
         */
        JButton add = new JButton("Add Item");
        JButton adjust = new JButton("Adjust Inventory");
        JButton back = new JButton("Back");
        back.addActionListener(e -> navigator.close());
        add.addActionListener(e -> addItem());
        adjust.addActionListener(e -> viewItems());

        HashMap<String, Item> items;
        try {
            items = dbToHashMap(false);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }
        JPanel table = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 0;

        JLabel idHeading = new JLabel("ID");
        JLabel descriptionHeading = new JLabel("Description");
        JLabel rateHeading = new JLabel("Rate");
        JLabel createdByHeading = new JLabel("Created By");
        JLabel createdOnHeading = new JLabel("Created On");
        JLabel stockHeading = new JLabel("Stock");
        JLabel availableHeading = new JLabel("Available");
        JLabel reservedHeading = new JLabel("Reserved");
        JLabel rentedHeading = new JLabel("Rented");
        JComponent[] horizontalElements = new JComponent[]{
                idHeading, descriptionHeading, rateHeading,
                createdByHeading, createdOnHeading, stockHeading,
                availableHeading, reservedHeading, rentedHeading
        };
        for(int x = 0; x < horizontalElements.length; x++) {
            gbc.gridx = x;
            JComponent element = horizontalElements[x];
            table.add(element, gbc);
        }

        for(Item item : items.values()) {
            gbc.gridy++;
            JLabel id = new JLabel(String.valueOf(item.getId()));
            JLabel description = new JLabel(item.getDescription());
            JLabel rate = new JLabel(String.valueOf(item.getRate()));
            JLabel createdBy = new JLabel(String.valueOf(item.getCreatedBy()));
            JLabel createdOn = new JLabel(String.valueOf(item.getDate()));
            JLabel stock = new JLabel(String.valueOf(item.getStock()));
            JLabel available = new JLabel(String.valueOf(item.getAvailable()));
            JLabel reserved = new JLabel(String.valueOf(item.getReserved()));
            JLabel rented = new JLabel(String.valueOf(item.getRented()));

            JComponent[] elements = new JComponent[]{
                    id, description, rate, createdBy,
                    createdOn, stock, available, reserved,
                    rented
            };
            for(int x = 0; x < elements.length; x++) {
                gbc.gridx = x;
                JComponent element = elements[x];

                table.add(element, gbc);
            }
        }

        JScrollPane scrollPane = scrollTable(table, 600, 400);

        GuiPlacer mainPlacer = new GuiPlacer(600, 700);
        JComponent[] mainElements = new  JComponent[]{
                scrollPane, getPadding(10, 5),
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
        /*
        add item screen
         */
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
        /*
        items in db to hashmap
         */
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
        /*
        items in csv file to arraylist
         */
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
        /*
        load csv file page
         */
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
        /*
        get a scroll table
         */
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
        /*
        edit item table
         */

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
        /*
        edit item page
         */
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
        /*
        view items page
         */
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

    private void mastodonSearch() {
        /*
        mastodon search page
         */
        JPanel panel = new JPanel(new GridBagLayout());
        JButton search = new JButton("Search");
        JTextField field = new JTextField("Phrase");

        clearTextField(field);

        search.addActionListener(e -> searched(field.getText()));

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                field, getPadding(10, 5),
                search, getPadding(10, 5),
                back
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "mastodonSearch");
    }

    private void searched(String phrase) {
        /*
        mastodon searched page, display results
         */
        JPanel panel = new JPanel(new GridBagLayout());

        String urlString = mastodonServer + "/api/v2/search?q=" + phrase;
        StringBuilder response;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", "Bearer " + mastodonToken);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } else {
                JOptionPane.showMessageDialog(mainFrame, "GET request failed. Response Code: " + responseCode);
                return;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Unknown Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(response.toString());
        } catch (org.json.simple.parser.ParseException e) {
            JOptionPane.showMessageDialog(mainFrame, "Unknown Error: " + e.getMessage());
            return;
        }

        JSONArray accountsArray = (JSONArray) jsonObject.get("accounts");
        ArrayList<JPanel> panels = new ArrayList<>();
        for (Object accountObj : accountsArray) {
            JSONObject account = (JSONObject) accountObj;

            String note = (String) account.get("note");
            String display_name = (String) account.get("display_name");
            String time = (String) account.get("created_at");
            panels.add(post(display_name, note, time));
        }

        JComponent[] elements = new JComponent[panels.size() + 1];
        for (int x = 0; x < panels.size(); x++) {
            elements[x] = panels.get(x);
        }
        JButton back = new JButton("Back");
        back.addActionListener(e -> navigator.close());
        elements[panels.size()] = back;

        JPanel table = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 0;

        for(int y = 0; y < elements.length; y++) {
            gbc.gridy++;
            table.add(elements[y], gbc);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 8);
        scrollPane.setPreferredSize(new Dimension(700, 700));
        scrollPane.setMinimumSize(new Dimension(700, 700));
        scrollPane.setViewportView(table);

        panel.add(scrollPane);

        navigator.open(panel, "mastodonSearch");
    }

    private JPanel post(String username, String text, String time) {
        /*
        format a post into a Jpanel
         */
        JPanel panel = new JPanel(new BorderLayout());

        JLabel usernameLabel = new JLabel("Username: " + username);
        JLabel textLabel = new JLabel("<html>" + text + "</html>");
        JLabel timeLabel = new JLabel("Time: " + time);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(usernameLabel, BorderLayout.NORTH);
        contentPanel.add(textLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.add(timeLabel);

        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.setMinimumSize(new Dimension(500, 100));
        panel.setPreferredSize(new Dimension(500, 400));
        panel.setMaximumSize(new Dimension(500, 1000));

        return panel;
    }

    private void salesReportOption() {
        /*
        sales report page
         */
        JPanel panel = new JPanel(new GridBagLayout());

        JButton button = new JButton("Generate");
        String[] types = {"Customer Type", "Reservations", "Income Report"};
        JComboBox<String> type = new JComboBox<>(types);
        button.addActionListener(e -> generateReport((String) Objects.requireNonNull(type.getSelectedItem())));

        JButton back = new JButton("Back");
        back.addActionListener(e -> navigator.close());

        GuiPlacer placer = new GuiPlacer(400, 500);
        Component[] elements = {
                type, getPadding(10, 5),
                button, getPadding(10, 5),
                back
        };
        placer.verticalPlacer(elements);
        JPanel container = placer.getContainer();

        panel.add(container);
        navigator.open(panel, "mastodonSearch");
    }

    private void generateReport(String type) {
        /*
        generate report depending on type
         */
        if (type.equals("Customer Type")) {
            customersReport();
        } else if (type.equals("Income Report")) {
            incomeReport();
        } else {
            reservationsReport();
        }
    }

    private void customersReport() {
        /*
        customers report page
         */
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        ResultSet rs;
        try {
            rs = noValueQuery(scripts.customerTypeReport);

            HashMap<String, Integer> countDom = new HashMap<>();
            HashMap<String, Integer> countGov = new HashMap<>();
            HashMap<String, Integer> countDip = new HashMap<>();
            HashMap<String, Integer> countPriv = new HashMap<>();
            HashMap<String, Integer> countRes = new HashMap<>();

            while (rs.next()) {
                Date reservationDate = dateFormat.parse(rs.getString("reservation_date"));
                int typeID = Integer.parseInt(rs.getString("type"));
                String month = monthFormat.format(reservationDate);

                switch (typeID) {
                    case 1:
                        accumulateData(countDom, month, 1);
                        break;
                    case 2:
                        accumulateData(countGov, month, 1);
                        break;
                    case 3:
                        accumulateData(countDip, month, 1);
                        break;
                    case 4:
                        accumulateData(countPriv, month, 1);
                        break;
                    case 5:
                        accumulateData(countRes, month, 1);
                        break;
                    default:
                        JOptionPane.showMessageDialog(mainFrame, "Error reading SQL1");
                        return;
                }
            }

            addDataToDataset(dataset, countDom, "DOMESTIC");
            addDataToDataset(dataset, countGov, "GOVERNMENT");
            addDataToDataset(dataset, countDip, "DIPLOMATIC");
            addDataToDataset(dataset, countPriv, "PRIVATE");
            addDataToDataset(dataset, countRes, "RESORTS");

        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "SQL Error");
            return;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Reservation Activity by Month and Customer Type",
            "Month",
            "Count",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.BLUE);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(back, BorderLayout.SOUTH);
        panel.validate();

        navigator.open(panel, "reservationsReport");
    }

    private void reservationsReport() {
        /*
        reservations report page
         */
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        ResultSet rs;
        try {
            rs = noValueQuery(scripts.reservationsReport);

            HashMap<String, Integer> reservations = new HashMap<>();
            HashMap<String, Integer> rents = new HashMap<>();
            HashMap<String, Integer> returns = new HashMap<>();

            while (rs.next()) {
                Date reservationDate = dateFormat.parse(rs.getString("reservation_date"));
                Date rentDate = dateFormat.parse(rs.getString("rent_date"));
                Date returnDate = dateFormat.parse(rs.getString("return_date"));

                String reservationMonth = monthFormat.format(reservationDate);
                String rentMonth = monthFormat.format(rentDate);
                String returnMonth = monthFormat.format(returnDate);

                accumulateData(reservations, reservationMonth, 1);
                accumulateData(rents, rentMonth, 1);
                accumulateData(returns, returnMonth, 1);
            }

            addDataToDataset(dataset, reservations, "Reservations");
            addDataToDataset(dataset, rents, "Rents");
            addDataToDataset(dataset, returns, "Returns");

        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "SQL Error");
            return;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Reservation Activity by Month",
                "Month",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.BLUE);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(back, BorderLayout.SOUTH);
        panel.validate();

        navigator.open(panel, "reservationsReport");
    }

    private void accumulateData(Map<String, Integer> dataMap, String month, int amount) {
        /*
        acumulate data
         */
        int count = dataMap.getOrDefault(month, 0);
        dataMap.put(month, count + amount);
    }

    private void accumulateFloat(Map<String, Float> dataMap, String month, Float amount) {
        /*
        acumulate data
         */
        Float count = dataMap.getOrDefault(month, Float.parseFloat("0"));
        dataMap.put(month, count + amount);
    }

    private void addDataToDataset(DefaultCategoryDataset dataset, Map<String, Integer> dataMap, String category) {
        /*
        add data to dataset for it to be displayed
         */
        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            String month = entry.getKey();
            Integer count = entry.getValue();
            dataset.addValue(count, category, month);
        }
    }

    private void viewCompletedReservations() {
        /*
        view all completed reservations
         */
        ArrayList<Reservation> reservations;
        try {
            Object[] values = new Object[]{};
            reservations = getReservations(scripts.selectReturnedReservations, values, "no");
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(mainFrame, exception.getMessage());
            return;
        }
        displayReservations(reservations, "viewCompletedReservations", "officer");
    }

    private void addFloatDataToDataset(DefaultCategoryDataset dataset, Map<String, Float> dataMap, String category) {
        /*
        add data to dataset for it to be displayed
         */
        for (Map.Entry<String, Float> entry : dataMap.entrySet()) {
            String month = entry.getKey();
            Float count = entry.getValue();
            dataset.addValue(count, category, month);
        }
    }

    private void incomeReport() {
        /*
        display an income report by different customers over time
         */

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        ResultSet rs;
        try {
            rs = noValueQuery(scripts.incomeReport);

            HashMap<String, Float> amountDom = new HashMap<>();
            HashMap<String, Float> amountGov = new HashMap<>();
            HashMap<String, Float> amountDip = new HashMap<>();
            HashMap<String, Float> amountPriv = new HashMap<>();
            HashMap<String, Float> amountRes = new HashMap<>();
            HashMap<String, Float> amountTotal = new HashMap<>();

            while (rs.next()) {
                Date transactionDate = dateFormat.parse(rs.getString("date"));
                int typeID = Integer.parseInt(rs.getString("type"));
                String month = monthFormat.format(transactionDate);
                float amount = Float.parseFloat(rs.getString("amount"));

                accumulateFloat(amountTotal, month, amount);
                switch (typeID) {
                    case 1:
                        accumulateFloat(amountDom, month, amount);
                        break;
                    case 2:
                        accumulateFloat(amountGov, month, amount);
                        break;
                    case 3:
                        accumulateFloat(amountDip, month, amount);
                        break;
                    case 4:
                        accumulateFloat(amountPriv, month, amount);
                        break;
                    case 5:
                        accumulateFloat(amountRes, month, amount);
                        break;
                    default:
                        JOptionPane.showMessageDialog(mainFrame, "Error reading SQL1");
                        return;
                }
            }

            addFloatDataToDataset(dataset, amountDom, "DOMESTIC");
            addFloatDataToDataset(dataset, amountGov, "GOVERNMENT");
            addFloatDataToDataset(dataset, amountDip, "DIPLOMATIC");
            addFloatDataToDataset(dataset, amountPriv, "PRIVATE");
            addFloatDataToDataset(dataset, amountRes, "RESORTS");
            addFloatDataToDataset(dataset, amountTotal, "TOTAL");

        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "SQL Error");
            return;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Income From Customer Category by Month",
                "Month",
                "Rufiyaa",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.BLUE);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(back, BorderLayout.SOUTH);
        panel.validate();

        navigator.open(panel, "incomeReport");
    }

    public static void main(String[] args) throws SQLException {
        new PartyRental();
    }
}