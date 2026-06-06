import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.table.*;
import java.nio.file.*;

public class DomestiFarmApp {
    public static void main(String[] args) {
        // Initialize MySQL Database and schema
        DBConnection.initializeDatabase();
        new SplashScreen();
    }
}

// Splash Screen
class SplashScreen extends JWindow {
    public SplashScreen() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("DomestiFarm", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setForeground(new Color(34, 139, 34));

        JLabel quote = new JLabel("\"Caring for animals, nurturing the future.\"", SwingConstants.CENTER);
        quote.setFont(new Font("Serif", Font.ITALIC, 18));
        quote.setForeground(Color.DARK_GRAY);

        content.add(title, BorderLayout.CENTER);
        content.add(quote, BorderLayout.SOUTH);

        setSize(400, 200);
        setLocationRelativeTo(null);
        setContentPane(content);
        setVisible(true);

        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
            dispose();
            new LoginScreen();
        });
        timer.setRepeats(false);
        timer.start();
    }
}

// Login Screen
class LoginScreen extends JFrame {
    JTextField emailField;
    JPasswordField passwordField;
    JCheckBox rememberMe;

    public LoginScreen() {
        setTitle("DomestiFarm - Login");
        setSize(650, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        try {
            ImageIcon originalIcon = new ImageIcon("farm_logo.png");
            Image scaled = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaled);
            JLabel logoLabel = new JLabel(resizedIcon);
            logoLabel.setBounds(30, 30, 200, 200);
            panel.add(logoLabel);
        } catch (Exception ex) {
            // Logo fallback sequence
        }

        JLabel title = new JLabel("Sign in to start your session");
        title.setBounds(260, 60, 300, 30);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title);

        JLabel emailLabel = new JLabel("E-mail:");
        emailLabel.setBounds(260, 120, 80, 25);
        panel.add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(340, 120, 200, 25);
        panel.add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(260, 160, 80, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(340, 160, 200, 25);
        panel.add(passwordField);

        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setBounds(340, 190, 150, 20);
        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
        });
        panel.add(showPassword);

        rememberMe = new JCheckBox("Remember Me");
        rememberMe.setBounds(340, 215, 150, 25);
        panel.add(rememberMe);

        loadRemembered();

        JButton loginButton = new JButton("Sign In");
        loginButton.setBounds(340, 255, 200, 30);
        loginButton.addActionListener(e -> login());
        panel.add(loginButton);

        JButton fbButton = new JButton("Sign in with Facebook");
        fbButton.setBounds(340, 295, 200, 30);
        fbButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Facebook Login clicked (not implemented).");
        });
        panel.add(fbButton);

        JButton googleButton = new JButton("Sign in with Google+");
        googleButton.setBounds(340, 335, 200, 30);
        googleButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Google Login clicked (not implemented).");
        });
        panel.add(googleButton);

        JLabel forgotPassword = new JLabel("<HTML><U>I forgot my password</U></HTML>");
        forgotPassword.setBounds(340, 375, 200, 25);
        forgotPassword.setForeground(Color.BLUE.darker());
        forgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginScreen.this, "To reset your password, please contact support or re-register.");
            }
        });
        panel.add(forgotPassword);

        JLabel register = new JLabel("<HTML><U>Register a new membership</U></HTML>");
        register.setBounds(340, 405, 200, 25);
        register.setForeground(Color.BLUE.darker());
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        register.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new RegisterScreen();
                dispose();
            }
        });
        panel.add(register);

        add(panel);
        setVisible(true);
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean valid = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?")) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    valid = true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }

        if (valid) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            if (rememberMe.isSelected()) {
                saveRemembered(email, password);
            } else {
                new File("remember.properties").delete();
            }
            dispose();
            new DashboardScreen();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    private void saveRemembered(String email, String password) {
        try (FileOutputStream out = new FileOutputStream("remember.properties")) {
            Properties props = new Properties();
            props.setProperty("email", email);
            props.setProperty("password", password);
            props.store(out, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadRemembered() {
        try (FileInputStream in = new FileInputStream("remember.properties")) {
            Properties props = new Properties();
            props.load(in);
            emailField.setText(props.getProperty("email", ""));
            passwordField.setText(props.getProperty("password", ""));
            rememberMe.setSelected(true);
        } catch (IOException ignored) {
        }
    }
}

// Register Screen
class RegisterScreen extends JFrame {
    public RegisterScreen() {
        setTitle("Register - DomestiFarm");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        JLabel emailLabel = new JLabel("E-mail:");
        emailLabel.setBounds(60, 60, 80, 25);
        panel.add(emailLabel);

        JTextField emailField = new JTextField();
        emailField.setBounds(180, 60, 200, 25);
        panel.add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(60, 100, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(180, 100, 200, 25);
        panel.add(passwordField);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(180, 150, 120, 30);
        registerBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            boolean exists = false;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (exists) {
                JOptionPane.showMessageDialog(this, "User already exists.");
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)")) {
                ps.setString(1, email);
                ps.setString(2, password);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration successful!");
                dispose();
                new LoginScreen();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        });
        panel.add(registerBtn);

        add(panel);
        setVisible(true);
    }
}

// Dashboard Screen
class DashboardScreen extends JFrame {
    public DashboardScreen() {
        setTitle("Dashboard - DomestiFarm");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton animalBtn = new JButton("1. Animal Management");
        JButton financeBtn = new JButton("2. Farm Finance");
        JButton employeeBtn = new JButton("3. Employees");
        JButton stockfeedBtn = new JButton("4. Stockfeed");

        animalBtn.addActionListener(e -> new AnimalManagementScreen());
        financeBtn.addActionListener(e -> new FarmFinanceScreen());
        employeeBtn.addActionListener(e -> new EmployeeScreen());
        stockfeedBtn.addActionListener(e -> new StockfeedScreen());

        panel.add(animalBtn);
        panel.add(financeBtn);
        panel.add(employeeBtn);
        panel.add(stockfeedBtn);

        add(panel);
        setVisible(true);
    }
}

// Farm Finance Screen
class FarmFinanceScreen extends JFrame {
    private JPanel expensePanel, salesPanel, monthlyProfitPanel;
    private JLabel totalExpenseLabel, totalSellLabel, profitLabel;
    private JTextField dateField;

    private java.util.List<Row> expenseRows = new ArrayList<>();
    private java.util.List<Row> salesRows = new ArrayList<>();
    private java.util.List<MonthlyRow> monthlyRows = new ArrayList<>();

    public FarmFinanceScreen() {
        setTitle("Farm Finance - DomestiFarm");
        setSize(850, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === Date ===
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Date:"));
        dateField = new JTextField(10);
        datePanel.add(dateField);
        mainPanel.add(datePanel);

        // === Daily Expenses (Centered) ===
        JLabel expenseTitle = new JLabel("Daily Expenses", SwingConstants.CENTER);
        expenseTitle.setFont(new Font("Arial", Font.BOLD, 20));
        expenseTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(expenseTitle);

        expensePanel = new JPanel();
        expensePanel.setLayout(new BoxLayout(expensePanel, BoxLayout.Y_AXIS));
        expensePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        mainPanel.add(expensePanel);

        addExpenseRow();

        JButton addExpenseBtn = new JButton("+ Add Expense");
        addExpenseBtn.addActionListener(e -> addExpenseRow());
        JPanel addExpensePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addExpensePanel.add(addExpenseBtn);
        mainPanel.add(addExpensePanel);

        // === Daily Sales (Centered) ===
        JLabel salesTitle = new JLabel("Daily Sales", SwingConstants.CENTER);
        salesTitle.setFont(new Font("Arial", Font.BOLD, 20));
        salesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(salesTitle);

        salesPanel = new JPanel();
        salesPanel.setLayout(new BoxLayout(salesPanel, BoxLayout.Y_AXIS));
        salesPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        mainPanel.add(salesPanel);

        addSalesRow();

        JButton addSalesBtn = new JButton("+ Add Sale");
        addSalesBtn.addActionListener(e -> addSalesRow());
        JPanel addSalesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addSalesPanel.add(addSalesBtn);
        mainPanel.add(addSalesPanel);

        // === Totals Section ===
        totalExpenseLabel = new JLabel("Total Expenses: 0");
        totalSellLabel = new JLabel("Total Sales: 0");
        profitLabel = new JLabel("Daily Profit: 0");

        JPanel totalsPanel = new JPanel(new GridLayout(3, 1));
        totalsPanel.add(totalExpenseLabel);
        totalsPanel.add(totalSellLabel);
        totalsPanel.add(profitLabel);
        mainPanel.add(totalsPanel);

        // === Save/Delete Daily Buttons ===
        JPanel dailyBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveDailyBtn = new JButton("💾 Save Daily Data");
        JButton deleteDailyBtn = new JButton("🗑 Delete Daily Data");
        dailyBtnPanel.add(saveDailyBtn);
        dailyBtnPanel.add(deleteDailyBtn);
        mainPanel.add(dailyBtnPanel);

        saveDailyBtn.addActionListener(e -> saveDailyData());
        deleteDailyBtn.addActionListener(e -> deleteDailyData());

        // === Monthly Profit (Centered) ===
        JLabel monthlyTitle = new JLabel("Monthly Profit", SwingConstants.CENTER);
        monthlyTitle.setFont(new Font("Arial", Font.BOLD, 20));
        monthlyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(monthlyTitle);

        monthlyProfitPanel = new JPanel();
        monthlyProfitPanel.setLayout(new BoxLayout(monthlyProfitPanel, BoxLayout.Y_AXIS));
        monthlyProfitPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        mainPanel.add(monthlyProfitPanel);

        // Dynamic Loading of Monthly Profits from database!
        loadMonthlyProfit();

        JButton addMonthlyBtn = new JButton("+ Add Monthly Row");
        addMonthlyBtn.addActionListener(e -> addMonthlyRow());
        JPanel addMonthlyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addMonthlyPanel.add(addMonthlyBtn);
        mainPanel.add(addMonthlyPanel);

        // === Save/Delete Monthly Buttons ===
        JPanel monthButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveMonthBtn = new JButton("💾 Save Monthly Profit");
        JButton deleteMonthBtn = new JButton("🗑 Delete Monthly Profit");
        monthButtonsPanel.add(saveMonthBtn);
        monthButtonsPanel.add(deleteMonthBtn);
        mainPanel.add(monthButtonsPanel);

        saveMonthBtn.addActionListener(e -> saveMonthlyProfit());
        deleteMonthBtn.addActionListener(e -> deleteMonthlyProfit());

        // === Scroll Pane ===
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);

        setVisible(true);
    }

    private void addExpenseRow() {
        Row row = new Row("Expense Name", expensePanel, expenseRows);
        expenseRows.add(row);
        expensePanel.add(row.panel);
        expensePanel.revalidate();
        expensePanel.repaint();
    }

    private void addSalesRow() {
        Row row = new Row("Sales Name", salesPanel, salesRows);
        salesRows.add(row);
        salesPanel.add(row.panel);
        salesPanel.revalidate();
        salesPanel.repaint();
    }

    private void addMonthlyRow() {
        MonthlyRow row = new MonthlyRow(monthlyProfitPanel, monthlyRows);
        monthlyRows.add(row);
        monthlyProfitPanel.add(row.panel);
        monthlyProfitPanel.revalidate();
        monthlyProfitPanel.repaint();
    }

    private void updateTotals() {
        double totalExpenses = expenseRows.stream().mapToDouble(Row::getTotal).sum();
        double totalSales = salesRows.stream().mapToDouble(Row::getTotal).sum();
        double profit = totalSales - totalExpenses;

        totalExpenseLabel.setText("Total Expenses: " + totalExpenses);
        totalSellLabel.setText("Total Sales: " + totalSales);
        profitLabel.setText("Daily Profit: " + profit);
    }

    private void saveDailyData() {
        String date = dateField.getText().trim();
        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a date.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete existing for this date first to avoid duplicates
                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM daily_finance_records WHERE record_date = ?")) {
                    psDel.setString(1, date);
                    psDel.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO daily_finance_records (record_date, type, name, expression, total) VALUES (?, ?, ?, ?, ?)")) {
                    for (Row r : expenseRows) {
                        ps.setString(1, date);
                        ps.setString(2, "Expense");
                        ps.setString(3, r.nameField.getText().trim());
                        ps.setString(4, r.expressionField.getText().trim());
                        ps.setDouble(5, r.getTotal());
                        ps.executeUpdate();
                    }
                    for (Row r : salesRows) {
                        ps.setString(1, date);
                        ps.setString(2, "Sale");
                        ps.setString(3, r.nameField.getText().trim());
                        ps.setString(4, r.expressionField.getText().trim());
                        ps.setDouble(5, r.getTotal());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Daily data saved successfully to database!");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void deleteDailyData() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM daily_finance_records");

            expensePanel.removeAll();
            salesPanel.removeAll();
            expenseRows.clear();
            salesRows.clear();

            addExpenseRow();
            addSalesRow();

            dateField.setText("");
            totalExpenseLabel.setText("Total Expenses: 0");
            totalSellLabel.setText("Total Sales: 0");
            profitLabel.setText("Daily Profit: 0");

            expensePanel.revalidate();
            expensePanel.repaint();
            salesPanel.revalidate();
            salesPanel.repaint();

            JOptionPane.showMessageDialog(this, "All Daily data deleted and cleared from database!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void saveMonthlyProfit() {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM monthly_profits");
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO monthly_profits (month, expression, result) VALUES (?, ?, ?)")) {
                    for (MonthlyRow r : monthlyRows) {
                        ps.setString(1, r.monthField.getText().trim());
                        ps.setString(2, r.expressionField.getText().trim());
                        ps.setDouble(3, evaluateExpression(r.expressionField.getText()));
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Monthly profit saved to database!");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void deleteMonthlyProfit() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM monthly_profits");
            monthlyProfitPanel.removeAll();
            monthlyRows.clear();
            addMonthlyRow();
            monthlyProfitPanel.revalidate();
            monthlyProfitPanel.repaint();
            JOptionPane.showMessageDialog(this, "Monthly profit deleted from database!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void loadMonthlyProfit() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM monthly_profits")) {
            boolean hasRows = false;
            monthlyProfitPanel.removeAll();
            monthlyRows.clear();
            while (rs.next()) {
                hasRows = true;
                MonthlyRow row = new MonthlyRow(monthlyProfitPanel, monthlyRows);
                row.monthField.setText(rs.getString("month"));
                row.expressionField.setText(rs.getString("expression"));
                row.resultField.setText(String.valueOf(rs.getDouble("result")));
                monthlyRows.add(row);
                monthlyProfitPanel.add(row.panel);
            }
            if (!hasRows) {
                addMonthlyRow();
            } else {
                monthlyProfitPanel.revalidate();
                monthlyProfitPanel.repaint();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            addMonthlyRow();
        }
    }

    private double evaluateExpression(String expr) {
        if (expr == null || expr.trim().isEmpty()) return 0;
        expr = expr.replaceAll("\\s+", ""); 
        try {
            double total = 0;
            String[] addParts = expr.split("\\+");
            for (String addPart : addParts) {
                if (addPart.contains("-")) {
                    String[] subParts = addPart.split("-");
                    double subTotal = 0;
                    if (!subParts[0].isEmpty()) {
                        subTotal = Double.parseDouble(subParts[0]);
                    }
                    for (int i = 1; i < subParts.length; i++) {
                        if (!subParts[i].isEmpty()) {
                            subTotal -= Double.parseDouble(subParts[i]);
                        }
                    }
                    total += subTotal;
                } else {
                    if (!addPart.isEmpty()) {
                        total += Double.parseDouble(addPart);
                    }
                }
            }
            return total;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    class Row {
        JPanel panel;
        JTextField nameField, expressionField, totalField;
        JButton deleteBtn;
        java.util.List<Row> containerList;
        JPanel parentPanel;

        Row(String placeholder, JPanel parent, java.util.List<Row> list) {
            this.containerList = list;
            this.parentPanel = parent;

            panel = new JPanel(new GridLayout(1, 4, 5, 5));
            nameField = new JTextField(placeholder, 10);
            expressionField = new JTextField("0", 8);
            totalField = new JTextField("0", 5);
            totalField.setEditable(false);

            deleteBtn = new JButton("Delete");
            deleteBtn.setFont(new Font("Arial", Font.PLAIN, 11));
            deleteBtn.setMargin(new Insets(2, 5, 2, 5));

            expressionField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    double val = evaluateExpression(expressionField.getText());
                    totalField.setText(String.valueOf(val));
                    updateTotals();
                }
            });

            deleteBtn.addActionListener(e -> {
                containerList.remove(this);
                parentPanel.remove(panel);
                parentPanel.revalidate();
                parentPanel.repaint();
                updateTotals();
            });

            panel.add(nameField);
            panel.add(expressionField);
            panel.add(totalField);
            panel.add(deleteBtn);
        }

        double getTotal() {
            try {
                return Double.parseDouble(totalField.getText());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    class MonthlyRow {
        JPanel panel;
        JTextField monthField, expressionField, resultField;
        JButton deleteBtn;
        java.util.List<MonthlyRow> containerList;
        JPanel parentPanel;

        MonthlyRow(JPanel parent, java.util.List<MonthlyRow> list) {
            this.containerList = list;
            this.parentPanel = parent;

            panel = new JPanel(new GridLayout(1, 4, 5, 5));

            monthField = new JTextField("Month", 10);
            expressionField = new JTextField("0", 15);
            resultField = new JTextField("0", 10);
            resultField.setEditable(false);

            deleteBtn = new JButton("Delete");
            deleteBtn.setFont(new Font("Arial", Font.PLAIN, 11));
            deleteBtn.setMargin(new Insets(2, 5, 2, 5));

            expressionField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    double val = evaluateExpression(expressionField.getText());
                    resultField.setText(String.valueOf(val));
                }
            });

            deleteBtn.addActionListener(e -> {
                containerList.remove(this);
                parentPanel.remove(panel);
                parentPanel.revalidate();
                parentPanel.repaint();
            });

            panel.add(monthField);
            panel.add(expressionField);
            panel.add(resultField);
            panel.add(deleteBtn);
        }
    }
}

// Employee screen class
class EmployeeScreen extends JFrame {
    CardLayout cardLayout;
    JPanel mainPanel;
    DefaultTableModel globalModel;
    JTable employeeTable;

    public EmployeeScreen() {
        setTitle("Employees - DomestiFarm");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        JMenuItem addEmpOption = new JMenuItem("Add Employee");
        JMenuItem empListOption = new JMenuItem("Employee List");
        menu.add(addEmpOption);
        menu.add(empListOption);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel employeeListPanel = createEmployeeListPanel();
        JPanel addEmployeePanel = createAddEmployeePanel();

        mainPanel.add(addEmployeePanel, "AddEmployee");
        mainPanel.add(employeeListPanel, "EmployeeList");

        addEmpOption.addActionListener(e -> cardLayout.show(mainPanel, "AddEmployee"));
        empListOption.addActionListener(e -> {
            loadEmployees(globalModel);
            cardLayout.show(mainPanel, "EmployeeList");
        });

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createAddEmployeePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel col1 = new JPanel(new GridLayout(6, 2, 10, 10));
        col1.add(new JLabel("Employee ID:"));
        JTextField idField = new JTextField();
        col1.add(idField);

        col1.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        col1.add(nameField);

        col1.add(new JLabel("Age:"));
        JTextField ageField = new JTextField();
        col1.add(ageField);

        col1.add(new JLabel("Gender:"));
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female"});
        col1.add(genderBox);

        col1.add(new JLabel("Contact Number:"));
        JTextField contactField = new JTextField();
        col1.add(contactField);

        col1.add(new JLabel("Address:"));
        JTextField addressField = new JTextField();
        col1.add(addressField);

        JPanel col2 = new JPanel(new GridLayout(6, 2, 10, 10));
        col2.add(new JLabel("User Type:"));
        JComboBox<String> userTypeBox = new JComboBox<>(new String[]{"Administrator", "Employee"});
        col2.add(userTypeBox);

        col2.add(new JLabel("Date Hired:"));
        JTextField dateHiredField = new JTextField();
        col2.add(dateHiredField);

        col2.add(new JLabel("Basic Salary:"));
        JTextField salaryField = new JTextField();
        col2.add(salaryField);

        col2.add(new JLabel("Job Title:"));
        JTextField jobTitleField = new JTextField();
        col2.add(jobTitleField);

        col2.add(new JLabel("Login Password:"));
        JPasswordField passwordField = new JPasswordField();
        col2.add(passwordField);

        JButton saveButton = new JButton("Save");
        col2.add(new JLabel(""));
        col2.add(saveButton);

        saveButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String salary = salaryField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (id.isEmpty() || name.isEmpty() || salary.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Please fill all required fields.");
                return;
            }

            boolean idExists = false;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM employees WHERE id = ?")) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        idExists = true;
                    }
                }
            } catch (SQLException ignored) {}

            if (idExists) {
                JOptionPane.showMessageDialog(this, "⚠️ Employee ID already exists. Please use a unique ID.");
                return;
            }

            boolean validPassword = false;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE password = ?")) {
                ps.setString(1, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        validPassword = true;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (!validPassword) {
                JOptionPane.showMessageDialog(this, "❌ Invalid login password! Cannot add employee.");
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO employees (id, name, age, gender, contact, address, user_type, date_hired, salary, job_title, password) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, id);
                ps.setString(2, name);
                
                int age = 0;
                try { age = Integer.parseInt(ageField.getText().trim()); } catch (NumberFormatException ignored) {}
                ps.setInt(3, age);
                
                ps.setString(4, (String) genderBox.getSelectedItem());
                ps.setString(5, contactField.getText().trim());
                ps.setString(6, addressField.getText().trim());
                ps.setString(7, (String) userTypeBox.getSelectedItem());
                ps.setString(8, dateHiredField.getText().trim());
                
                double sal = 0;
                try { sal = Double.parseDouble(salary); } catch (NumberFormatException ignored) {}
                ps.setDouble(9, sal);
                
                ps.setString(10, jobTitleField.getText().trim());
                ps.setString(11, password);
                
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Employee added successfully!");
                idField.setText(""); nameField.setText(""); ageField.setText("");
                contactField.setText(""); addressField.setText(""); dateHiredField.setText("");
                salaryField.setText(""); jobTitleField.setText(""); passwordField.setText("");
                loadEmployees(globalModel);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        });

        panel.add(col1);
        panel.add(col2);
        return panel;
    }

    private JPanel createEmployeeListPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftCol = new JPanel(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        JLabel searchLabel = new JLabel("Search:");
        JTextField searchField = new JTextField();
        JButton searchBtn = new JButton("🔍");
        searchBtn.setFocusable(false);
        searchBtn.setMargin(new Insets(2, 6, 2, 6));

        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.add(searchField, BorderLayout.CENTER);
        searchFieldPanel.add(searchBtn, BorderLayout.EAST);
        searchFieldPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);

        String[] columns = {"ID", "Name", "Salary"};
        globalModel = new DefaultTableModel(columns, 0);
        employeeTable = new JTable(globalModel);
        employeeTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(employeeTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JPanel actionPanel = new JPanel();
        actionPanel.add(updateBtn);
        actionPanel.add(deleteBtn);

        leftCol.add(searchPanel, BorderLayout.NORTH);
        leftCol.add(scrollPane, BorderLayout.CENTER);
        leftCol.add(actionPanel, BorderLayout.SOUTH);

        loadEmployees(globalModel);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() { searchEmployee(searchField.getText(), globalModel); }
        });

        searchBtn.addActionListener(e -> searchEmployee(searchField.getText(), globalModel));
        deleteBtn.addActionListener(e -> deleteEmployee(employeeTable, globalModel));

        JPanel rightCol = new JPanel(new GridLayout(6, 2, 10, 10));
        rightCol.add(new JLabel("Employee ID:"));
        JTextField idField = new JTextField();
        rightCol.add(idField);

        rightCol.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        rightCol.add(nameField);

        rightCol.add(new JLabel("Salary:"));
        JTextField salaryField = new JTextField();
        rightCol.add(salaryField);

        rightCol.add(new JLabel("Update salary by (%):"));
        JTextField percentField = new JTextField();
        rightCol.add(percentField);

        rightCol.add(new JLabel("Current salary:"));
        JTextField currentSalaryField = new JTextField();
        rightCol.add(currentSalaryField);

        JButton updateSalaryBtn = new JButton("Update Salary");
        rightCol.add(new JLabel(""));
        rightCol.add(updateSalaryBtn);

        employeeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow != -1) {
                    idField.setText(globalModel.getValueAt(selectedRow, 0).toString());
                    nameField.setText(globalModel.getValueAt(selectedRow, 1).toString());
                    currentSalaryField.setText(globalModel.getValueAt(selectedRow, 2).toString());
                    salaryField.setText(globalModel.getValueAt(selectedRow, 2).toString());
                }
            }
        });

        MouseAdapter clearSelectionListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getSource() != employeeTable) {
                    employeeTable.clearSelection();
                }
            }
        };
        panel.addMouseListener(clearSelectionListener);
        leftCol.addMouseListener(clearSelectionListener);
        rightCol.addMouseListener(clearSelectionListener);

        updateSalaryBtn.addActionListener(e -> {
            try {
                double percent = Double.parseDouble(percentField.getText());
                double current = Double.parseDouble(currentSalaryField.getText());
                double newSalary = current + (current * percent / 100);
                salaryField.setText(String.valueOf(newSalary));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for salary update.");
            }
        });

        updateBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String salary = salaryField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || salary.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ All fields must be filled for update.");
                return;
            }

            double sal = 0;
            try { sal = Double.parseDouble(salary); } catch (NumberFormatException ignored) {}

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE employees SET name = ?, salary = ? WHERE id = ?")) {
                ps.setString(1, name);
                ps.setDouble(2, sal);
                ps.setString(3, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    loadEmployees(globalModel);
                    JOptionPane.showMessageDialog(this, "✅ Employee details updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ No employee found with this ID. Please check and try again.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error updating employee: " + ex.getMessage());
            }
        });

        panel.add(leftCol);
        panel.add(rightCol);
        return panel;
    }

    private void loadEmployees(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, salary FROM employees")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("id"), rs.getString("name"), rs.getDouble("salary")});
            }
        } catch (SQLException ignored) {}
    }

    private void searchEmployee(String query, DefaultTableModel model) {
        loadEmployees(model); 
        if (query.trim().isEmpty()) return;
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            String id = model.getValueAt(i, 0).toString();
            String name = model.getValueAt(i, 1).toString();
            if (!id.contains(query) && !name.toLowerCase().contains(query.toLowerCase())) {
                model.removeRow(i);
            }
        }
    }

    private void deleteEmployee(JTable table, DefaultTableModel model) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        String idToDelete = model.getValueAt(selected, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            ps.setString(1, idToDelete);
            ps.executeUpdate();
            model.removeRow(selected);
            JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error deleting employee: " + e.getMessage());
        }
    }
}

// Stockfeed Screen Module
class StockfeedScreen extends JFrame {
    private JPanel stockPanel;                       
    private JScrollPane scrollPane;                  
    private java.util.List<String> stockNames;       
    private JPanel mainPanel;                        

    public StockfeedScreen() {
        setTitle("Stockfeed - DomestiFarm");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("Stock", SwingConstants.CENTER);
        heading.setFont(new Font("Serif", Font.BOLD, 26));
        heading.setForeground(new Color(34, 139, 34));

        stockPanel = new JPanel();
        stockPanel.setLayout(new BoxLayout(stockPanel, BoxLayout.Y_AXIS));
        stockPanel.setBackground(Color.WHITE);
        scrollPane = new JScrollPane(stockPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        JButton addStockBtn = new JButton("+ Add Stock");
        addStockBtn.setFont(new Font("Arial", Font.BOLD, 13));
        addStockBtn.addActionListener(e -> addNewStockButton());
        topPanel.add(addStockBtn);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(Color.WHITE);
        topWrapper.add(heading, BorderLayout.NORTH);
        topWrapper.add(topPanel, BorderLayout.SOUTH);

        mainPanel.add(topWrapper, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveAllStockData());
        bottomPanel.add(saveBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        stockNames = new ArrayList<>();

        add(mainPanel);
        loadStockList(); 
        setVisible(true);
    }

    private void addNewStockButton() {
        String stockName = JOptionPane.showInputDialog(this, "Enter Stock Name:");
        if (stockName != null && !stockName.trim().isEmpty()) {
            String trimmedName = stockName.trim();
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO stockfeed_categories (name) VALUES (?)")) {
                ps.setString(1, trimmedName);
                ps.executeUpdate();
                stockNames.add(trimmedName);
                refreshStockPanel();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding stock: " + ex.getMessage());
            }
        }
    }

    private void createStockButton(String stockName) {
        JButton stockBtn = new JButton(stockName);
        stockBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        stockBtn.setMaximumSize(new Dimension(200, 40));

        stockBtn.addActionListener(e -> openStockBox(stockName));

        JPopupMenu menu = new JPopupMenu();
        JMenuItem renameItem = new JMenuItem("Rename");
        JMenuItem deleteItem = new JMenuItem("Delete");

        renameItem.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(this, "Enter new name:", stockBtn.getText());
            if (newName != null && !newName.trim().isEmpty()) {
                String oldName = stockBtn.getText();
                String newNameTrimmed = newName.trim();
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE stockfeed_categories SET name = ? WHERE name = ?")) {
                    ps.setString(1, newNameTrimmed);
                    ps.setString(2, oldName);
                    ps.executeUpdate();

                    int idx = stockNames.indexOf(oldName);
                    if (idx >= 0) {
                        stockNames.set(idx, newNameTrimmed);
                        stockBtn.setText(newNameTrimmed);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error renaming stock: " + ex.getMessage());
                }
            }
        });

        deleteItem.addActionListener(e -> {
            String name = stockBtn.getText();
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM stockfeed_categories WHERE name = ?")) {
               ps.setString(1, name);
               ps.executeUpdate();

               int idx = stockNames.indexOf(name);
               if (idx >= 0) stockNames.remove(idx);

               stockPanel.remove(stockBtn);
               refreshStockPanel();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting stock: " + ex.getMessage());
            }
        });

        menu.add(renameItem);
        menu.add(deleteItem);

        stockBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    menu.show(stockBtn, e.getX(), e.getY());
                }
            }
        });

        stockPanel.add(stockBtn);
    }

    private void refreshStockPanel() {
        stockPanel.removeAll();
        for (String name : stockNames) {
            createStockButton(name);
        }
        stockPanel.revalidate();
        stockPanel.repaint();
    }

    private void loadStockList() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM stockfeed_categories")) {
            stockNames.clear();
            while (rs.next()) {
                stockNames.add(rs.getString("name"));
            }
            refreshStockPanel();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveAllStockData() {
        JOptionPane.showMessageDialog(this, "Stock list saved successfully!");
    }

    private void openStockBox(String stockName) {
        JFrame stockFrame = new JFrame(stockName + " - Management");
        stockFrame.setSize(750, 450);
        stockFrame.setLocationRelativeTo(this);
        stockFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.setBackground(Color.WHITE);

        JButton addItemBtn = new JButton("Add Item");
        addItemBtn.addActionListener(e -> addStockItem(boxPanel));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(addItemBtn);

        JScrollPane sp = new JScrollPane(boxPanel);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveItemsToFile(boxPanel, stockName));
        bottomPanel.add(saveBtn);

        stockFrame.add(topPanel, BorderLayout.NORTH);
        stockFrame.add(sp, BorderLayout.CENTER);
        stockFrame.add(bottomPanel, BorderLayout.SOUTH);

        loadItemsFromFile(boxPanel, stockName);
        stockFrame.setVisible(true);
    }

    private void addStockItem(JPanel parentPanel) {
        JPanel itemPanel = new JPanel(null);
        itemPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        itemPanel.setPreferredSize(new Dimension(700, 90));

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(10, 10, 60, 25);
        itemPanel.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(70, 10, 120, 25);
        itemPanel.add(nameField);

        JLabel qtyLabel = new JLabel("Quantity (kg/number):");
        qtyLabel.setBounds(210, 10, 160, 25);
        itemPanel.add(qtyLabel);

        JTextField qtyField = new JTextField("0");
        qtyField.setBounds(370, 10, 80, 25);
        itemPanel.add(qtyField);

        JLabel modLabel = new JLabel("Add/Remove Quantity:");
        modLabel.setBounds(470, 10, 160, 25);
        itemPanel.add(modLabel);

        JTextField modField = new JTextField();
        modField.setBounds(630, 10, 80, 25);
        itemPanel.add(modField);

        JButton plusBtn = new JButton("+");
        plusBtn.setMargin(new Insets(0, 5, 0, 5));
        plusBtn.setBounds(370, 40, 40, 25);
        plusBtn.addActionListener(e -> {
            try {
                int qty = Integer.parseInt(qtyField.getText());
                int mod = Integer.parseInt(modField.getText());
                qtyField.setText(String.valueOf(qty + mod));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Enter valid numbers.");
            }
        });
        itemPanel.add(plusBtn);

        JButton minusBtn = new JButton("-");
        minusBtn.setMargin(new Insets(0, 6, 0, 6));
        minusBtn.setBounds(420, 40, 40, 25);
        minusBtn.addActionListener(e -> {
            try {
                int qty = Integer.parseInt(qtyField.getText());
                int mod = Integer.parseInt(modField.getText());
                qtyField.setText(String.valueOf(qty - mod));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Enter valid numbers.");
            }
        });
        itemPanel.add(minusBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBounds(630, 40, 80, 25);
        deleteBtn.addActionListener(e -> {
            parentPanel.remove(itemPanel);
            parentPanel.revalidate();
            parentPanel.repaint();
        });
        itemPanel.add(deleteBtn);

        parentPanel.add(itemPanel);
        parentPanel.revalidate();
        parentPanel.repaint();
    }

    private void saveItemsToFile(JPanel parentPanel, String stockName) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM stockfeed_items WHERE category_name = ?")) {
                    psDel.setString(1, stockName);
                    psDel.executeUpdate();
                }
                try (PreparedStatement psIns = conn.prepareStatement(
                        "INSERT INTO stockfeed_items (category_name, item_name, quantity) VALUES (?, ?, ?)")) {
                    for (Component comp : parentPanel.getComponents()) {
                        if (comp instanceof JPanel) {
                            JPanel itemP = (JPanel) comp;
                            JTextField nameField = (JTextField) itemP.getComponent(1);
                            JTextField qtyField = (JTextField) itemP.getComponent(3);
                            
                            int qty = 0;
                            try { qty = Integer.parseInt(qtyField.getText().trim()); } catch (NumberFormatException ignored) {}
                            
                            psIns.setString(1, stockName);
                            psIns.setString(2, nameField.getText().trim());
                            psIns.setInt(3, qty);
                            psIns.executeUpdate();
                        }
                    }
                }
                conn.commit();
                JOptionPane.showMessageDialog(parentPanel, "Stock data saved successfully to database!");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parentPanel, "Error saving stock data: " + ex.getMessage());
        }
    }

    private void loadItemsFromFile(JPanel parentPanel, String stockName) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT item_name, quantity FROM stockfeed_items WHERE category_name = ?")) {
            ps.setString(1, stockName);
            try (ResultSet rs = ps.executeQuery()) {
                parentPanel.removeAll();
                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    String qty = String.valueOf(rs.getInt("quantity"));
                    addStockItem(parentPanel);
                    JPanel itemP = (JPanel) parentPanel.getComponent(parentPanel.getComponentCount() - 1);
                    ((JTextField) itemP.getComponent(1)).setText(itemName);
                    ((JTextField) itemP.getComponent(3)).setText(qty);
                }
                parentPanel.revalidate();
                parentPanel.repaint();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}