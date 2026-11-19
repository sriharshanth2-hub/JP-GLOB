import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BankingGUI extends JFrame {
    // Path to persistence file (auto-detects location whether run from workspace root or BankingSystem directory)
    private static final String DATA_FILE = resolveDataFilePath();

    // Helper to locate data.csv in current or parent directory
    private static String resolveDataFilePath() {
        if (new java.io.File("data.csv").exists()) return "data.csv";
        if (new java.io.File("../data.csv").exists()) return "../data.csv";
        if (new java.io.File("BankingSystem/data.csv").exists()) return "BankingSystem/data.csv";
        return "data.csv";
    }

    // Core business logic controller
    private Bank bank;

    // GUI Components
    private JTabbedPane tabbedPane;
    private JLabel statusBar;
    private JTable accountsTable;
    private DefaultTableModel accountsTableModel;

    // Mini Statement Table and Model
    private JTable miniStatementTable;
    private DefaultTableModel miniStatementTableModel;

    // Constructor to initialize the GUI window
    public BankingGUI() {
        // Initialize backend logic controller
        bank = new Bank();

        // STEP 6: Load existing data on startup
        try {
            bank.load(DATA_FILE);
        } catch (IOException e) {
            System.out.println("[GUI Warning] Failed to load data: " + e.getMessage());
        }

        // STEP 2 & 7: Window Setup with System Look & Feel
        initWindow();

        // Create main UI sections
        JPanel headerPanel = createHeaderPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel statusPanel = createStatusPanel();

        // Add main sections to JFrame content pane
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        // Populate the live accounts overview table
        refreshAccountsTable();

        // Set initial status bar message
        setStatus("System ready. Welcome to Core Java Banking System.", false);
    }

    // Configures primary JFrame properties and Look and Feel
    private void initWindow() {
        setTitle("Core Java Banking System");
        setSize(980, 640);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // STEP 6: Save data on window close using WindowListener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        // Set System Look and Feel for native OS appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    // Handles graceful save and window exit
    private void handleWindowClose() {
        try {
            bank.save(DATA_FILE);
            System.out.println("[GUI Success] Data saved to '" + DATA_FILE + "' on application exit.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save data on exit: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
        dispose();
        System.exit(0);
    }

    // Creates the colored top banner header panel
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(24, 43, 73)); // Deep navy blue
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("CORE JAVA BANKING SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("College Micro Project | Swing GUI + Core Java");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(200, 215, 235));

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(subLabel, BorderLayout.EAST);
        return panel;
    }

    // Creates the center split pane: Operation Tabs on Left, Live Overview on Right
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Left Side: Operations Tabbed Pane (STEP 2 & 3)
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabbedPane.addTab("Create Account", createAccountPanel());
        tabbedPane.addTab("Deposit", createDepositPanel());
        tabbedPane.addTab("Withdraw", createWithdrawPanel());
        tabbedPane.addTab("Transfer", createTransferPanel());
        tabbedPane.addTab("Balance", createBalancePanel());
        tabbedPane.addTab("Mini Statement", createMiniStatementPanel());
        tabbedPane.addTab("Interest", createInterestPanel());
        tabbedPane.addTab("Delete Account", createDeletePanel());

        // Right Side: Live Accounts Overview Table (STEP 4)
        JPanel overviewPanel = createOverviewPanel();

        // Split pane to allow flexible resizing between tabs and table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, overviewPanel);
        splitPane.setDividerLocation(560);
        splitPane.setResizeWeight(0.6);
        splitPane.setContinuousLayout(true);

        centerPanel.add(splitPane, BorderLayout.CENTER);
        return centerPanel;
    }

    // Creates the bottom status bar (STEP 2)
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 215, 225)),
                BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));
        panel.setBackground(new Color(245, 247, 250));

        statusBar = new JLabel("System ready.");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(new Color(40, 50, 60));

        JLabel fileNotice = new JLabel("Persistence: data.csv");
        fileNotice.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        fileNotice.setForeground(Color.GRAY);

        panel.add(statusBar, BorderLayout.WEST);
        panel.add(fileNotice, BorderLayout.EAST);
        return panel;
    }

    // Sets status bar message with color indication (green for success, red for error)
    private void setStatus(String message, boolean isError) {
        if (isError) {
            statusBar.setForeground(new Color(180, 20, 20)); // Dark red
            statusBar.setText("[Error] " + message);
        } else {
            statusBar.setForeground(new Color(20, 120, 40)); // Dark green
            statusBar.setText("[Success] " + message);
        }
    }

    // =========================================================================
    // STEP 4: Live Accounts Overview Table Panel
    // =========================================================================
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Live Accounts Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(24, 43, 73));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAccountsTable();
                setStatus("Accounts overview refreshed.", false);
            }
        });

        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);

        // Define table columns
        String[] columnNames = {"Acc No", "Holder Name", "Type", "Balance (Rs.)"};
        accountsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells read-only
            }
        };

        accountsTable = new JTable(accountsTableModel);
        accountsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountsTable.setRowHeight(22);
        accountsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Right-align balance column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        accountsTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(accountsTable);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // Refreshes the Accounts Overview Table from the Bank collection
    private void refreshAccountsTable() {
        accountsTableModel.setRowCount(0);
        Map<String, Account> all = bank.getAllAccounts();
        for (Account acc : all.values()) {
            accountsTableModel.addRow(new Object[]{
                    acc.getAccountNumber(),
                    acc.getHolderName(),
                    acc.getAccountType(),
                    String.format("%.2f", acc.getBalance())
            });
        }
    }

    // Helper method to automatically save after each successful operation (STEP 6)
    private void autoSaveData() {
        try {
            bank.save(DATA_FILE);
        } catch (IOException e) {
            System.out.println("[Auto-Save Error] " + e.getMessage());
        }
    }

    // =========================================================================
    // STEP 3: Individual Tab Panels
    // =========================================================================

    // Tab 1: Create Account
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = createGbc();

        JLabel nameLabel = new JLabel("Account Holder Name:");
        JTextField nameField = new JTextField(18);

        JLabel typeLabel = new JLabel("Account Type:");
        String[] types = {"Savings", "Current"};
        JComboBox<String> typeCombo = new JComboBox<>(types);

        JLabel depositLabel = new JLabel("Initial Deposit (Rs.):");
        JTextField depositField = new JTextField(18);

        JLabel noteLabel = new JLabel("<html><i>Note: Savings min. Rs. 500 | Current min. Rs. 0</i></html>");
        noteLabel.setForeground(Color.GRAY);

        JButton createBtn = new JButton("Create Account");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createBtn.setBackground(new Color(24, 110, 60));
        createBtn.setForeground(Color.WHITE);

        // Add components to GridBagLayout
        gbc.gridx = 0; gbc.gridy = 0; panel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(typeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(depositLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(depositField, gbc);

        gbc.gridx = 1; gbc.gridy = 3; panel.add(noteLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        gbc.insets = new Insets(15, 10, 10, 10);
        panel.add(createBtn, gbc);

        // Event Handling (Anonymous Inner Class for Viva demonstration)
        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("Account holder name cannot be empty.");
                    }

                    String type = (String) typeCombo.getSelectedItem();
                    double deposit = parseDoubleInput(depositField.getText().trim(), "Initial Deposit");

                    // Call backend business method
                    String accNum = bank.createAccount(name, type, deposit);

                    // Show success dialog with generated account number
                    String msg = String.format("Account created successfully!\nAccount Number: %s\nHolder: %s\nType: %s\nInitial Balance: Rs. %.2f",
                            accNum, name, type, deposit);
                    JOptionPane.showMessageDialog(BankingGUI.this, msg, "Account Created", JOptionPane.INFORMATION_MESSAGE);

                    // Clear form fields
                    nameField.setText("");
                    depositField.setText("");

                    // Update UI state & persist
                    refreshAccountsTable();
                    autoSaveData();
                    setStatus("Account " + accNum + " created for " + name + ".", false);

                } catch (Exception ex) {
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // Tab 2: Deposit Funds
    private JPanel createDepositPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = createGbc();

        JLabel accLabel = new JLabel("Account Number:");
        JTextField accField = new JTextField(18);

        JLabel amountLabel = new JLabel("Deposit Amount (Rs.):");
        JTextField amountField = new JTextField(18);

        JButton depositBtn = new JButton("Deposit Funds");
        depositBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        depositBtn.setBackground(new Color(24, 75, 140));
        depositBtn.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(accLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(accField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(amountLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(amountField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(15, 10, 10, 10);
        panel.add(depositBtn, gbc);

        depositBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String accNum = validateAccNum(accField.getText());
                    double amount = parseDoubleInput(amountField.getText().trim(), "Deposit Amount");

                    bank.deposit(accNum, amount);
                    double newBal = bank.getBalance(accNum);

                    JOptionPane.showMessageDialog(BankingGUI.this,
                            String.format("Successfully deposited Rs. %.2f into %s.\nNew Balance: Rs. %.2f",
                                    amount, accNum, newBal),
                            "Deposit Successful", JOptionPane.INFORMATION_MESSAGE);

                    amountField.setText("");
                    refreshAccountsTable();
                    autoSaveData();
                    setStatus("Deposited Rs. " + amount + " into " + accNum + ". New balance: Rs. " + newBal, false);

                } catch (Exception ex) {
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // Tab 3: Withdraw Funds
    private JPanel createWithdrawPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = createGbc();

        JLabel accLabel = new JLabel("Account Number:");
        JTextField accField = new JTextField(18);

        JLabel amountLabel = new JLabel("Withdrawal Amount (Rs.):");
        JTextField amountField = new JTextField(18);

        JButton withdrawBtn = new JButton("Withdraw Funds");
        withdrawBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        withdrawBtn.setBackground(new Color(160, 45, 45));
        withdrawBtn.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(accLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(accField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(amountLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(amountField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(15, 10, 10, 10);
        panel.add(withdrawBtn, gbc);

        withdrawBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String accNum = validateAccNum(accField.getText());
                    double amount = parseDoubleInput(amountField.getText().trim(), "Withdrawal Amount");

                    bank.withdraw(accNum, amount);
                    double newBal = bank.getBalance(accNum);

                    JOptionPane.showMessageDialog(BankingGUI.this,
                            String.format("Successfully withdrew Rs. %.2f from %s.\nNew Balance: Rs. %.2f",
                                    amount, accNum, newBal),
                            "Withdrawal Successful", JOptionPane.INFORMATION_MESSAGE);

                    amountField.setText("");
                    refreshAccountsTable();
                    autoSaveData();
                    setStatus("Withdrew Rs. " + amount + " from " + accNum + ". New balance: Rs. " + newBal, false);

                } catch (Exception ex) {
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // Tab 4: Transfer Funds
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = createGbc();

        JLabel fromLabel = new JLabel("Sender Account Number:");
        JTextField fromField = new JTextField(18);

        JLabel toLabel = new JLabel("Receiver Account Number:");
        JTextField toField = new JTextField(18);

        JLabel amountLabel = new JLabel("Transfer Amount (Rs.):");
        JTextField amountField = new JTextField(18);

        JButton transferBtn = new JButton("Transfer Funds");
        transferBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        transferBtn.setBackground(new Color(110, 45, 140));
        transferBtn.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(fromLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(fromField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(toLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(toField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(amountLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(amountField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.insets = new Insets(15, 10, 10, 10);
        panel.add(transferBtn, gbc);

        transferBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String fromAcc = validateAccNum(fromField.getText());
                    String toAcc = validateAccNum(toField.getText());
                    double amount = parseDoubleInput(amountField.getText().trim(), "Transfer Amount");

                    bank.transfer(fromAcc, toAcc, amount);
                    double fromBal = bank.getBalance(fromAcc);

                    JOptionPane.showMessageDialog(BankingGUI.this,
                            String.format("Successfully transferred Rs. %.2f from %s to %s.\nSender New Balance: Rs. %.2f",
                                    amount, fromAcc, toAcc, fromBal),
                            "Transfer Successful", JOptionPane.INFORMATION_MESSAGE);

                    amountField.setText("");
                    refreshAccountsTable();
                    autoSaveData();
                    setStatus("Transferred Rs. " + amount + " from " + fromAcc + " to " + toAcc + ".", false);

                } catch (Exception ex) {
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // Tab 5: Check Balance
    private JPanel createBalancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = createGbc();

        JLabel accLabel = new JLabel("Account Number:");
        JTextField accField = new JTextField(18);

        JButton checkBtn = new JButton("Check Balance");
        checkBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Result display label
        JLabel resultLabel = new JLabel("<html><i>Enter account number and click Check Balance.</i></html>");
        resultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        resultLabel.setOpaque(true);
        resultLabel.setBackground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(accLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(accField, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 15, 10);
        panel.add(checkBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(resultLabel, gbc);

        checkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String accNum = validateAccNum(accField.getText());
                    Account acc = bank.findAccount(accNum);

                    String text = String.format(
                            "<html><b>Account Details:</b><br>" +
                            "&bull; <b>Account No:</b> %s<br>" +
                            "&bull; <b>Holder Name:</b> %s<br>" +
                            "&bull; <b>Account Type:</b> %s<br>" +
                            "&bull; <b>Current Balance:</b> <font color='#0a6628'>Rs. %.2f</font></html>",
                            acc.getAccountNumber(), acc.getHolderName(), acc.getAccountType(), acc.getBalance());
                    resultLabel.setText(text);
                    setStatus("Balance retrieved for " + accNum + ": Rs. " + acc.getBalance(), false);

                } catch (Exception ex) {
                    resultLabel.setText("<html><font color='red'>Could not fetch account details.</font></html>");
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // Tab 6: Mini Statement (Last 5 Transactions displayed in JTable)
    private JPanel createMiniStatementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Top input controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel accLabel = new JLabel("Account Number:");
        JTextField accField = new JTextField(14);
        JButton showBtn = new JButton("Show Mini Statement");
        showBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        topPanel.add(accLabel);
        topPanel.add(accField);
        topPanel.add(showBtn);

        // Table setup for transactions
        String[] cols = {"Type", "Amount (Rs.)", "Date & Time", "Balance After (Rs.)"};
        miniStatementTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        miniStatementTable = new JTable(miniStatementTableModel);
        miniStatementTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miniStatementTable.setRowHeight(22);
        miniStatementTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(miniStatementTable);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        showBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String accNum = validateAccNum(accField.getText());
                    Account acc = bank.findAccount(accNum);
                    ArrayList<Transaction> txList = bank.getMiniStatement(accNum);

                    miniStatementTableModel.setRowCount(0);
                    if (txList.isEmpty()) {
                        JOptionPane.showMessageDialog(BankingGUI.this,
                                "No transactions recorded yet for account " + accNum,
                                "Mini Statement", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        for (Transaction tx : txList) {
                            miniStatementTableModel.addRow(new Object[]{
                                    tx.getType(),
                                    String.format("%.2f", tx.getAmount()),
                                    tx.getDateTime(),
                                    String.format("%.2f", tx.getBalanceAfter())
                            });
                        }
                    }
                    setStatus("Mini statement loaded for " + accNum + " (" + acc.getHolderName() + ").", false);

                } catch (Exception ex) {
                    miniStatementTableModel.setRowCount(0);
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // Tab 7: Apply Interest
    private JPanel createInterestPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = createGbc();

        JLabel infoTitle = new JLabel("<html><h3>Apply Annual Interest (Polymorphism)</h3></html>");
        JLabel infoDesc = new JLabel("<html>This operation calls <code>calculateInterest()</code> polymorphically.<br>" +
                "&bull; <b>Savings Accounts:</b> Receives <b>4% annual interest</b> credited directly.<br>" +
                "&bull; <b>Current Accounts:</b> Receives <b>0% interest</b> (unchanged).</html>");
        infoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton applyBtn = new JButton("Apply 4% Interest to All Savings Accounts");
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        applyBtn.setBackground(new Color(24, 110, 60));
        applyBtn.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(infoTitle, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 20, 10);
        panel.add(infoDesc, gbc);

        gbc.gridy = 2;
        panel.add(applyBtn, gbc);

        applyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int count = bank.applyInterest();
                    String msg = String.format("Annual interest successfully applied to %d Savings account(s)!", count);
                    JOptionPane.showMessageDialog(BankingGUI.this, msg, "Interest Applied", JOptionPane.INFORMATION_MESSAGE);

                    refreshAccountsTable();
                    autoSaveData();
                    setStatus(msg, false);

                } catch (Exception ex) {
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // Tab 8: Delete Account
    private JPanel createDeletePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = createGbc();

        JLabel accLabel = new JLabel("Account Number to Delete:");
        JTextField accField = new JTextField(18);

        JButton deleteBtn = new JButton("Delete Account");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteBtn.setBackground(new Color(180, 25, 25));
        deleteBtn.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(accLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(accField, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(15, 10, 10, 10);
        panel.add(deleteBtn, gbc);

        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String accNum = validateAccNum(accField.getText());
                    Account acc = bank.findAccount(accNum); // Checks existence

                    // JOptionPane confirmation (Yes/No)
                    int confirm = JOptionPane.showConfirmDialog(BankingGUI.this,
                            String.format("Are you sure you want to permanently delete account %s (%s)?\nAll balance and transaction history will be removed.",
                                    acc.getAccountNumber(), acc.getHolderName()),
                            "Confirm Account Deletion",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        bank.deleteAccount(accNum);
                        JOptionPane.showMessageDialog(BankingGUI.this,
                                "Account " + accNum + " has been successfully deleted.",
                                "Account Deleted", JOptionPane.INFORMATION_MESSAGE);

                        accField.setText("");
                        refreshAccountsTable();
                        autoSaveData();
                        setStatus("Account " + accNum + " deleted.", false);
                    } else {
                        setStatus("Account deletion cancelled.", false);
                    }

                } catch (Exception ex) {
                    handleException(ex);
                }
            }
        });

        return panel;
    }

    // =========================================================================
    // STEP 5: Validation & Custom Exception Handling
    // =========================================================================

    // Validates account number is non-empty and converts to uppercase
    private String validateAccNum(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }
        return input.trim().toUpperCase();
    }

    // Parses double input and rejects non-numeric or non-positive values
    private double parseDoubleInput(String input, String fieldName) throws InvalidAmountException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidAmountException(fieldName + " cannot be empty. Please enter a valid number.");
        }
        try {
            double val = Double.parseDouble(input.trim());
            return val;
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Invalid numeric input for " + fieldName + ": '" + input + "'. Numbers only.");
        }
    }

    // Centralized exception handler updating status bar and showing error dialog
    private void handleException(Exception ex) {
        String title = "Error";
        String message = ex.getMessage();

        if (ex instanceof InsufficientBalanceException) {
            title = "Insufficient Balance";
        } else if (ex instanceof AccountNotFoundException) {
            title = "Account Not Found";
        } else if (ex instanceof InvalidAmountException) {
            title = "Invalid Amount";
        } else if (ex instanceof NumberFormatException) {
            title = "Format Error";
            message = "Please enter valid numeric digits.";
        } else if (ex instanceof IllegalArgumentException) {
            title = "Validation Error";
        }

        // Show error dialog
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);

        // Update status bar in dark red
        setStatus(message, true);
    }

    // Helper to generate consistent GridBagConstraints
    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }


    // =========================================================================
    // Entry Point for GUI
    // =========================================================================
    public static void main(String[] args) {
        // Run GUI construction on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BankingGUI gui = new BankingGUI();
                gui.setVisible(true);
            }
        });
    }
}
