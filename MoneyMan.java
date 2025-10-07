// need better find - AND search terms or add start date
// need Find Next in Accounts

// MoneyMan v1.0.0 by R.A.Waddilove (github.com/raddilove)
// It was created for myself as a way to learn Java and Swing. If you find it
// useful or have suggestions, improvements, features, or bugs, let me know.
// I can be reached at rwaddilove at Gmail. Use it, modify it, and give it to
// anyone you want. No guarantees, use it at your own risk!

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.UIManager.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

class General {
    /** Used when an amount is input. */
    public static String getAmount(String input, boolean allowNegative) {
        double amount;
        try {
            amount = Double.parseDouble(input.trim()); }
        catch (NumberFormatException e) {
            amount = 0.00; }
        if (!allowNegative && amount < 0.00) amount = -amount;
        if (amount < -1000000.00 || amount > 1000000.00) amount = 0.00;     // ignore stupid numbers
        return String.format("%.2f", amount);
    }

    /** Set theme before using any Swing! */
    public static void ApplyTheme() {
        String theme = FileOp.GetConfig("theme");
        if (theme.equals("System")) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, "System theme not set! ", "Alert", JOptionPane.PLAIN_MESSAGE); }
            return; }

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if (theme.equals(info.getName())) UIManager.setLookAndFeel(info.getClassName()); }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, theme + " theme not set! ", "Alert", JOptionPane.PLAIN_MESSAGE); }
    }

    public static Color AppColor() {
        return new Color(158, 207, 224);    // toolbar colors
    }

    public static void SetFont() {
        int fontSize;
        try {
            fontSize = Integer.parseInt(FileOp.GetConfig("fontsize")); }
        catch (NumberFormatException e) {
            fontSize = 14; }
        String fontName = FileOp.GetConfig("font");
        if (fontName.isBlank()) fontName = "Arial";

        // Set default font for all components
        UIManager.put("Label.font", new Font(fontName, Font.PLAIN, fontSize));
        UIManager.put("Button.font", new Font(fontName, Font.PLAIN, fontSize));
        UIManager.put("TextField.font", new Font(fontName, Font.PLAIN, fontSize));
        UIManager.put("TextArea.font", new Font(fontName, Font.PLAIN, fontSize));
        UIManager.put("Spinner.font", new Font(fontName, Font.PLAIN, fontSize));
        UIManager.put("ComboBox.font", new FontUIResource(fontName, Font.PLAIN, fontSize));
        UIManager.put("Table.font", new FontUIResource(fontName, Font.PLAIN, fontSize));    // not working!
        UIManager.put("TableHeader.font", new FontUIResource(fontName, Font.PLAIN, fontSize));
    }

    public static void DeleteAccount(List<BankAccount> accounts, int acc, JFrame frame) {
        if (accounts.isEmpty() || acc < 0) return;
        String msg = "Are you sure you want to delete\nthis account: " + accounts.get(acc).name;
        if (JOptionPane.showConfirmDialog(frame, msg, "Delete Account", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            accounts.remove(acc);
    }

    public static void UpdateRecurringTransactions(List<BankAccount> accounts) {
        if (accounts.isEmpty()) return;
        for (BankAccount account : accounts) {
            account.UpdateRecurringTransactions(accounts);
            account.SortTransactions(); }
    }
}

class FormCategoriesReport {
    BankAccount account;
    JDialog cat;           // money in out dialog
    JTable table;
    DefaultTableModel tModel;
    JComboBox<String> comboCategory;

    public FormCategoriesReport(BankAccount acc, JFrame frame) {
        account = acc;
        cat = new JDialog(frame, "Categories report (12 months)");
        cat.setLayout(new BorderLayout(0,0));
        cat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        cat.setBounds(100, 100, 380, 385);
        cat.setLocationRelativeTo(frame);

        CreateTopPanel();
        CreateCenterPanel();
        CreateBottomPanel();
        SelectCategory();
        cat.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        cat.setVisible(true);
    }

    private void CreateBottomPanel() {
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,4));
        panelBottom.setBackground(General.AppColor());
        JLabel labelInfo = new JLabel("Zero months are ignored when calculating averages");
        panelBottom.add(labelInfo);
        cat.add(panelBottom, BorderLayout.SOUTH);
    }

    private void CreateCenterPanel() {
        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BorderLayout());
        tModel = new DefaultTableModel(new String[]{"Month", "Money In", "Money Out"}, 0);
        table = new JTable(tModel) {        // Create a JTable with the model
            @Override
            public boolean isCellEditable(int row, int column) {    // cells are not editable
                return false;
            }
        };
//        table.getColumnModel().getColumn(0).setPreferredWidth(50);
//        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
        // Set alignment for columns - default is left
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Align to center
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        TableColumnModel columnModel = table.getColumnModel();      // allign header columns
        columnModel.getColumn(0).setHeaderRenderer(centerRenderer);
        columnModel.getColumn(1).setHeaderRenderer(rightRenderer);
        columnModel.getColumn(2).setHeaderRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);    // add JTable to a JScrollPane
        panelCenter.add(scrollPane);                        // add JScrollPane to center panel
        cat.add(panelCenter, BorderLayout.CENTER);
    }

    private void CreateTopPanel() {
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        panelTop.setBackground(General.AppColor());
        JLabel labelCategory = new JLabel("Category:");
        panelTop.add(labelCategory);
        comboCategory = new JComboBox<>();
        FillComboCategory();
        if (comboCategory.getItemCount() > 0) comboCategory.setSelectedIndex(0);
        comboCategory.addActionListener(e -> SelectCategory());
        panelTop.add(comboCategory);

        JLabel spacer = new JLabel("               ");
        panelTop.add(spacer);
        JButton buttonClose = new JButton("Close");
        buttonClose.addActionListener(e -> cat.dispose());
        panelTop.add(buttonClose);
        cat.add(panelTop, BorderLayout.NORTH);
    }

    private void FillComboCategory() {
        for (int i = 0; i < account.transactions.size(); ++i) {
            if (account.getCategory(i).isBlank()) continue;
            if (comboCategory.getItemCount() == 0) {
                comboCategory.addItem(account.getCategory(i));
                continue; }
            boolean found = false;
            for (int j = 0; j < comboCategory.getItemCount(); ++j) {
                if (comboCategory.getItemAt(j).equalsIgnoreCase(account.getCategory(i))) {
                    found = true;
                    break; }
            }
            if (!found) comboCategory.addItem(account.getCategory(i));
        }
    }

    /** Fill the table with a monthly sum of the selected category */
    private void SelectCategory() {
        int tsize = account.transactions.size() - 1;
        if (tsize < 0 || comboCategory.getSelectedIndex() < 0) return;
        String category = (String) comboCategory.getSelectedItem();
        tModel.setRowCount(0);      // clear the table

        double inTotal = 0, outTotal = 0, nmonths = 0;          // count months with data for average
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusYears(1);  // one year ago

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int mnth = 1; mnth < 13; ++mnth) {
            String sdate = startDate.plusMonths(mnth).format(formatter);
            double in = 0.0, out = 0.0;

            for (int taction = 0; taction < account.transactions.size(); ++taction) {
                if (account.getDate(taction).startsWith(sdate) && account.getCategory(taction).equalsIgnoreCase(category)) {
                    double amount = Double.parseDouble(account.getAmount(taction));
                    if (amount < 0)
                        out += amount;
                    else
                        in += amount;
                }
            }
            inTotal += in;
            outTotal += out;
            if (in != 0 || out != 0) ++nmonths;     // count months for average, ignore zero months
            String[] s = new String[]{sdate, String.format("%.2f", in), String.format("%.2f", out)};
            tModel.addRow(s);
        }
        String[] s = new String[]{"Average", String.format("%.2f", (inTotal / nmonths)),
                String.format("%.2f", (outTotal / nmonths))};
        tModel.addRow(s);
        SelectLastTableRow();
    }

    public void SelectLastTableRow() {
        int lastrow = table.getRowCount() - 1;
        if (lastrow < 0) return;
        table.setRowSelectionInterval(lastrow, lastrow);    // select last row
        table.scrollRectToVisible(table.getCellRect(lastrow, 0, true)); // scroll to last row
    }
}


class FormHelpAbout {
    public FormHelpAbout(JFrame frame) {
        JDialog had = new JDialog(frame, "About MoneyMan");
        had.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        had.setBounds(100, 100, 400, 350);
        had.setLocationRelativeTo(frame);
//        had.setResizable(false);
        had.setLayout(new BorderLayout(0, 0));

        // create bottom panel ----------------------------
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBottom.setBackground(General.AppColor());
        JButton okButton = new JButton(" Close ");
        okButton.addActionListener(e -> had.dispose());
        panelBottom.add(okButton);

        // create center panel ----------------------------
        JPanel panelCenter = new JPanel(new BorderLayout(0, 0));
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        String text = """
                MoneyMan v1.0 by R.A.Waddilove
                https://github.com/raddilove
                
                It was created for myself as a way to learn Java and Swing. If you find \
                it useful or have suggestions for improvements, features, or bugs, let \
                me know. I can be reached at rwaddilove on Gmail and Hotmail.
                
                You are free to use and modify the code and give it to anyone you want. \
                There are no guarantees, use it at your own risk.""";
        textArea.setText(text);
        JScrollPane textScroll = new JScrollPane(textArea);
        panelCenter.add(textScroll);

        had.add(panelCenter, BorderLayout.CENTER);
        had.add(panelBottom, BorderLayout.SOUTH);
        had.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        had.setVisible(true);
    }
}

class FormTransfer {                // mostly a copy of FormTransaction
    List<BankAccount> accounts;
    JDialog mtd;                    // money transfer dialog
    JComboBox<String> comboFrom;
    JComboBox<String> comboTo;
    JSpinner spinDate;
    JTextField textAmount;
    JTextField textCategory;
    JTextField textNotes;
    JComboBox<String> comboRepeat;

    public FormTransfer(List<BankAccount> accountsList, JFrame frame) {
        accounts = accountsList;
        mtd = new JDialog(frame, "Move money between accounts");
        mtd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mtd.setBounds(100, 100, 650, 300);
        mtd.setLocationRelativeTo(frame);
        mtd.setResizable(false);
        mtd.setLayout(null);
        mtd.setAlwaysOnTop(true);

        int x = 5, y = 20, w = 100, h = 25, x1 = w + x + x, vspace = h + 2;

        // payee, date, amount, category, notes, id, repeat
        AddLabel("From:", x, y, w, h);
        comboFrom = new JComboBox<>();
        comboFrom.setBounds(x1, y, 150, h);
        mtd.add(comboFrom);

        AddLabel("To:", x1+90, y, w, h);
        comboTo = new JComboBox<>();
        comboTo.setBounds(x1+200, y, 150, h);
        mtd.add(comboTo);
        y += vspace;

        // add accounts to comboboxes
        for (BankAccount account : accounts) {
            comboFrom.addItem(account.name);
            comboTo.addItem(account.name); }
        comboFrom.setSelectedIndex(0);
        comboTo.setSelectedIndex(0);

        // day, month - year input...
        AddLabel("Day:", x, y, w, h);
        SpinnerDateModel model = new SpinnerDateModel();
        spinDate = new JSpinner(model);
        spinDate.setEditor(new JSpinner.DateEditor(spinDate, "dd MMM yyyy"));
        spinDate.setBounds(x1, y, 150, h);
        mtd.add(spinDate);
        y += vspace;

        AddLabel("Amount:", x, y, w, h);
        textAmount = new JTextField();
        textAmount.setBounds(x1, y, w, h);
        mtd.add(textAmount);
        y += vspace;

        AddLabel("Category:", x, y, w, h);
        textCategory = new JTextField();
        textCategory.setBounds(x1, y, w, h);
        mtd.add(textCategory);
        y += vspace;

        AddLabel("Notes:", x, y, w, h);
        textNotes = new JTextField();
        textNotes.setBounds(x1, y, w * 5, h);
        mtd.add(textNotes);
        y += vspace;

        AddLabel("Repeat:", x, y, w, h);
        comboRepeat = new JComboBox<>();
        comboRepeat.addItem("none");
        comboRepeat.addItem("week");
        comboRepeat.addItem("2week");
        comboRepeat.addItem("4week");
        comboRepeat.addItem("month");
        comboRepeat.addItem("year");
        comboRepeat.setBounds(x1, y, 100, h);
        mtd.add(comboRepeat);
        y += vspace + h;

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.setBounds(x1, y, 100, h);
        buttonCancel.addActionListener(e -> mtd.dispose());
//        buttonCancel.putClientProperty("JComponent.sizeVariant", "mini");  // only sith Nimbus theme?
        mtd.add(buttonCancel);

        JButton buttonOK = new JButton("OK");
        buttonOK.addActionListener(e -> okButton());
        buttonOK.setBounds(x1 + 120, y, 100, h);
//        buttonOK.putClientProperty("JComponent.sizeVariant", "mini");  // only sith Nimbus theme?
        mtd.add(buttonOK);

        mtd.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        mtd.setVisible(true);
    }

    private void AddLabel(String text, int x, int y, int w, int h) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, w, h);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        mtd.add(label);
    }

    private void okButton() {
        if (comboFrom.getSelectedIndex() == comboTo.getSelectedIndex()) {
            JOptionPane.showMessageDialog(mtd, "Can't transfer money to the same account!", "Notification", JOptionPane.WARNING_MESSAGE);
            return; }
        BankAccount baFrom = accounts.get(comboFrom.getSelectedIndex());
        BankAccount baTo = accounts.get(comboTo.getSelectedIndex());

        Date dateObj = (Date) spinDate.getValue();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(dateObj);

        String amount = General.getAmount(textAmount.getText(), false);
        if (amount.equals("0.00")) return;      // nothing to transfer or bad input

        String category = textCategory.getText().trim();
        if (category.length() > 15) category = category.substring(0, 15);   // trim very long categories

        String notes = textNotes.getText().trim();
        if (notes.length() > 200) notes = notes.substring(0, 100);

        String repeat = comboRepeat.getSelectedItem().toString();
        Random random = new Random();               // random number for new recurring transaction ID
        int randomNum = random.nextInt(Integer.MAX_VALUE);
        String id = Long.toString(randomNum);    // write new recurring transaction, new ID, same repeat

        // add transactions - From first because it's +, then To because it's -
        String payee = "Transfer from " + baFrom.name;
        baTo.transactions.add(new ArrayList<>());
        Collections.addAll(baTo.transactions.getLast(), payee, date, amount, category, notes, id, "none","","","","","*");
        baTo.SortTransactions();

        payee = "Transfer to " + baTo.name;
        amount = "-" + amount;
        baFrom.transactions.add(new ArrayList<>());
        Collections.addAll(baFrom.transactions.getLast(), payee, date, amount, category, notes, id, repeat,"","","","","*");
        baFrom.SortTransactions();

        General.UpdateRecurringTransactions(accounts);
        baTo.SortTransactions();
        baFrom.SortTransactions();
        mtd.dispose();
    }
}

class FormMoneyInOut {
    JDialog mind;           // money in out dialog
    JTable table;
    DefaultTableModel tModel;

    public FormMoneyInOut(BankAccount account, JFrame frame) {
        mind = new JDialog(frame, "Monthly report");
        mind.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mind.setBounds(100, 100, 400, 500);
        mind.setLocationRelativeTo(frame);
        mind.setLayout(new BorderLayout());
//        mind.setResizable(false);

        // create center panel -------------------------------------------
        JPanel panelCenter = new JPanel(new BorderLayout());
        tModel = new DefaultTableModel(new String[]{"Month", "Money In", "Money Out", "Balance"}, 0);
        table = new JTable(tModel) {        // Create a JTable with the model
            @Override
            public boolean isCellEditable(int row, int column) {    // cells are not editable
                return false;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
        // Set alignment for columns - default is left
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Align to center
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setHeaderRenderer(centerRenderer);
        columnModel.getColumn(1).setHeaderRenderer(rightRenderer);
        columnModel.getColumn(2).setHeaderRenderer(rightRenderer);
        columnModel.getColumn(3).setHeaderRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);    // add JTable to a JScrollPane
        panelCenter.add(scrollPane);                        // add scrollpane to center panel

        // create bottom panel -------------------------------------------
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBottom.setBackground(General.AppColor());
        JButton buttonClose = new JButton("Close");
        buttonClose.addActionListener(e -> mind.dispose());
        panelBottom.add(buttonClose);

        mind.add(panelCenter, BorderLayout.CENTER);
        mind.add(panelBottom, BorderLayout.SOUTH);
        mind.setAlwaysOnTop(true);
        FillTable(account);
        mind.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        mind.setVisible(true);
    }

    private void FillTable(BankAccount account) {
        if (account.transactions.isEmpty()) return;
        String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
        String sdate = account.getDate(0).substring(0, 7);     // yyyy-MM
        double in = 0.0, out = 0.0, amount = 0.0;
        for (int i = 0; i < account.transactions.size(); ++i) {
            if (account.getDate(i).startsWith(sdate) && i < account.transactions.size()-1) {
                amount = Double.parseDouble(account.getAmount(i));  // same month and not last transaction
                if (amount < 0)
                    out -= amount;
                else
                    in += amount;
            } else {                                                    // add month in/out totals
                String[] s = new String[]{sdate, String.format("%.2f", in), String.format("%.2f", out),
                        String.format("%s%.2f", currency, in - out)};   // "Month", "In", "Out", "Balance"
                tModel.addRow(s);
                amount = Double.parseDouble(account.getAmount(i));      // it's a new month
                out = amount < 0 ? amount : 0.0;
                in = amount < 0 ? 0.0 : amount;
                sdate = account.getDate(i).substring(0, 7);     // next month
            }
        }
    }
}

class FormUpcomingBills {
    List<BankAccount> accounts;
    JDialog ub;
    JTable table;
    DefaultTableModel tModel;

    public FormUpcomingBills(List<BankAccount> accountsList, JFrame frame) {
        accounts = accountsList;
        ub = new JDialog(frame, "Upcoming Bills");
        ub.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ub.setBounds(100, 100, 800, 350);
        ub.setLocationRelativeTo(frame);
        ub.setLayout(new BorderLayout());
//        ub.setResizable(false);

        // create center panel -------------------------------------------
        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BorderLayout());
        tModel = new DefaultTableModel(new String[]{"Date", "Payee", "Credit", "Debit", "Account"}, 0);
        table = new JTable(tModel) {        // Create a JTable with the model
            @Override
            public boolean isCellEditable(int row, int column) {    // cells are not editable
                return false;
            }
        };
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
        // Set alignment for columns - default is left
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Align to center
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);     // credit
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);     // debit
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);     // debit

        JScrollPane scrollPane = new JScrollPane(table);    // add JTable to a JScrollPane
        panelCenter.add(scrollPane);                        // add scrollpane to center panel

        // create bottom panel -------------------------------------------
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBottom.setBackground(General.AppColor());
        JButton buttonClose = new JButton("Close");
        buttonClose.addActionListener(e -> ub.dispose());
        panelBottom.add(buttonClose);

        ub.add(panelCenter, BorderLayout.CENTER);
        ub.add(panelBottom, BorderLayout.SOUTH);
        ub.setAlwaysOnTop(true);
        FillTable();
        ub.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        ub.setVisible(true);
    }

    private void FillTable() {
        tModel.setRowCount(0);                      // clear table
        if (accounts.isEmpty()) return;             // no accounts?
        for (BankAccount account : accounts)
            RecurringTransactions(account);
        }

    public void RecurringTransactions(BankAccount account) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();           // current date
        LocalDate nextWeek = today.plusWeeks(2);     // add 2 weeks
        while (true) {
            boolean updatedTransaction = false;   // loop until no more recurring transactions
            for (int i = 0; i < account.transactions.size(); ++i) {
                if (account.getRepeat(i).equals("none")) continue;    // not a recurring transaction?

                // this is a recurring transaction
                LocalDate newDate = account.getNewDate(i);                  // next day it occurs
                if (newDate.isAfter(nextWeek)) continue; // too far in the future?

                // this is a recurring transaction in the next 2 weeks
                String[] s;
                double amount = Double.parseDouble(account.getAmount(i));
                if (amount < 0)     // "Date", "Payee", "Credit", "Debit", "Account"
                    s = new String[]{newDate.format(formatter), account.getPayee(i), "", String.format("%.2f", amount), account.name};
                else
                    s = new String[]{newDate.format(formatter), account.getPayee(i), String.format("%.2f", amount), "", account.name};
                tModel.addRow(s);
            }
            if (!updatedTransaction) break;     // stop when no more recurring transactions updated
        }
    }
}

class FormNewAccount {
    List<BankAccount> accounts;
    JDialog nad;
    JTextField nadTextName;
    JSpinner spinDateNAD;
    JTextField textAmount;

    public FormNewAccount(List<BankAccount> accountsList, JFrame frame) {
        accounts = accountsList;
        nad = new JDialog(frame, "New account");
        nad.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nad.setBounds(100, 100, 350, 230);
        nad.setLocationRelativeTo(frame);
        nad.setResizable(false);
        nad.setLayout(null);
        nad.setAlwaysOnTop(true);

        int x = 5, y = 20, w = 80, w2 = 150, h = 25, x1 = w + x + x, vspace = h + 5;

        // enter account name
        AddLabel("Name: ", x, y, w, h);
        nadTextName = new JTextField();
        nadTextName.setLocation(x, y);
        nadTextName.setBounds(x1, y, w2, h);
        nad.add(nadTextName);
        y += vspace;

        // select the date with a spinner
        AddLabel("Date:", x, y, w, h);
        SpinnerDateModel model = new SpinnerDateModel();
        spinDateNAD = new JSpinner(model);
        spinDateNAD.setEditor(new JSpinner.DateEditor(spinDateNAD, "dd MMM yyyy"));
        spinDateNAD.setBounds(x1, y, w2, h);
        nad.add(spinDateNAD);
        y += vspace;

        // enter the amount
        AddLabel("Amount:", x, y, w, h);
        textAmount = new JTextField(10);
        textAmount.setBounds(x1, y, w2, h);
        textAmount.addActionListener(e -> okButton());  // Press Enter
        nad.add(textAmount);
        y += vspace;

        JLabel labelNote1 = new JLabel("Use negative amount for debit.");
        labelNote1.setBounds(x1, y, 250, h);
        nad.add(labelNote1);

        y += vspace + 20;

        // Cancel and OK buttons
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(x1, y, 80, h);
        cancelButton.addActionListener(e -> nad.dispose());
        nad.add(cancelButton);
        JButton buttonOK = new JButton("OK");
        buttonOK.setBounds(x1 + 100, y, 80, h);
        buttonOK.addActionListener(e -> okButton());
        nad.add(buttonOK);

        nad.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        nad.setVisible(true);       // all done, show dialog
    }

    private void AddLabel(String text, int x, int y, int w, int h) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, w, h);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        nad.add(label);
    }

    public void okButton() {
        String name = nadTextName.getText().trim();
        if (name.isBlank()) return;
        String payee = "Opening balance";

        Date dateObj = (Date) spinDateNAD.getValue();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(dateObj);

        String amount = General.getAmount(textAmount.getText(), true);
        String category = "";
        String notes = "";
        String id = "unused";
        String repeat = "none";
        accounts.add(new BankAccount(name));
        accounts.getLast().transactions.add(new ArrayList<>());     // "","","","","*" added for expansion
        Collections.addAll(accounts.getLast().transactions.getLast(), payee, date, amount, category, notes, id, repeat, "","","","","*");

        JOptionPane.showMessageDialog(nad, "Accounts list has new item.", "Notification", JOptionPane.INFORMATION_MESSAGE);
        nad.dispose();
    }
}

class FormAccountOverview {
    List<BankAccount> accounts;
    JDialog av;
    JTable table;
    DefaultTableModel tModel;
    JTextField totalText;
    JTextField textAccountName;

    public FormAccountOverview(List<BankAccount> accountsList, JFrame frame) {
        accounts = accountsList;
        av = new JDialog(frame, "Account Overview");
        av.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        av.setBounds(100, 100, 650, 300);
        av.setLocationRelativeTo(frame);
        av.setLayout(new BorderLayout());
//        av.setResizable(false);

        // create center panel -------------------------------------------
        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BorderLayout());
        tModel = new DefaultTableModel(new String[]{"Account", "Balance"}, 0);
        table = new JTable(tModel) {        // Create a JTable with the model
            @Override
            public boolean isCellEditable(int row, int column) {    // cells are not editable
                return false;
            }
        };
        table.getColumnModel().getColumn(0).setPreferredWidth(400);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
        // Set alignment for Amount column - default is left
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Align to center
        table.getSelectionModel().addListSelectionListener(e -> TableRowSelected());  //show notes

        JScrollPane scrollPane = new JScrollPane(table);    // add JTable to a JScrollPane
        panelCenter.add(scrollPane);                        // add scrollpane to center panel

        // create bottom panel -------------------------------------------
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelBottom.setBackground(General.AppColor());

        JLabel labelAccountName = new JLabel("Name:");
        panelBottom.add(labelAccountName);
        textAccountName = new JTextField(10);
        panelBottom.add(textAccountName);

        JButton buttonUpdate = new JButton("Rename");
        buttonUpdate.addActionListener(e -> UpdateAccountName());
        panelBottom.add(buttonUpdate);

        JLabel totalLabel = new JLabel("   Total:");
        panelBottom.add(totalLabel);
        totalText = new JTextField(8);
        totalText.setEditable(false);
        panelBottom.add(totalText);

        JLabel labelBlank = new JLabel("   ");
        panelBottom.add(labelBlank);
        JButton buttonClose = new JButton("Close");
        buttonClose.addActionListener(e -> av.dispose());
        panelBottom.add(buttonClose);

        av.add(panelCenter, BorderLayout.CENTER);
        av.add(panelBottom, BorderLayout.SOUTH);
        av.setAlwaysOnTop(true);
        FillTable();
        av.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        av.setVisible(true);
    }

    private void TableRowSelected() {
        if (table.getSelectedRow() < 0) return;
        textAccountName.setText(accounts.get(table.getSelectedRow()).name.trim());
    }

    private void UpdateAccountName() {
        int account = table.getSelectedRow();
        String name = textAccountName.getText().trim();
        if (account < 0 || name.isBlank()) return;
        accounts.get(account).name = name;
        FillTable();
    }

    private void FillTable() {
        tModel.setRowCount(0);                      // clear table
        if (accounts.isEmpty()) return;             // no accounts?
        double total = 0.0;
        String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
        for (BankAccount account : accounts) {
            double balance = account.getBalance("");
            total += balance;
            String[] s = {account.name, String.format("%s%.2f", currency, balance)};
            tModel.addRow(s);
        }
        totalText.setText(String.format("%s%.2f", currency, total));
    }
}

class FormTransaction {
    List<BankAccount> accounts;
    JTextField textPayee;
    JSpinner spinDate;
    JTextField textCredit;
    JTextField textDebit;
    JTextField textCategory;
    JTextField textNotes;
    JComboBox<String> comboRepeat;

    public FormTransaction(List<BankAccount> accountsList, int account, int editrow, JFrame frame) {
        accounts = accountsList;
        JDialog td = new JDialog(frame, "New transaction");
        td.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        td.setBounds(100, 100, 700, 320);
        td.setLocationRelativeTo(frame);
        td.setResizable(false);
        td.setLayout(null);

        int x = 5, y = 20, w = 100, h = 25, x1 = w + x + x, h1 = h + 2;

        // payee, date, amount, category, notes, id, repeat
        AddLabel("Payee:", x, y, w, h, "right", td);
        textPayee = new JTextField();
        textPayee.setBounds(x1, y, w * 4, h);
        td.add(textPayee);
        y += h1;

        // day, month - year input...
        AddLabel("Date:", x, y, w, h, "right", td);
        SpinnerDateModel model = new SpinnerDateModel();
        spinDate = new JSpinner(model);
        spinDate.setEditor(new JSpinner.DateEditor(spinDate, "dd MMM yyyy"));
        spinDate.setBounds(x1, y, 150, h);
        td.add(spinDate);
        JButton buttonToday = new JButton("Today");
        buttonToday.setBounds(x1 + 160, y, 80, h);
        buttonToday.addActionListener(e -> {
            spinDate.setValue(new Date());
            td.repaint(); });
        td.add(buttonToday);
        y += h1;

        AddLabel("Credit:", x, y, w, h, "right", td);
        textCredit = new JTextField();
        textCredit.setBounds(x1, y, w, h);
        td.add(textCredit);
        AddLabel("  (Income)", x1+w, y, w, h, "left", td);
        y += h1;

        AddLabel("Debit:", x, y, w, h, "right", td);
        textDebit = new JTextField();
        textDebit.setBounds(x1, y, w, h);
        td.add(textDebit);
        AddLabel("  (Expense)", x1+w, y, w, h, "left", td);
        y += h1;

        AddLabel("Category:", x, y, w, h, "right", td);
        textCategory = new JTextField();
        textCategory.setBounds(x1, y, w, h);
        td.add(textCategory);
        y += h1;

        AddLabel("Notes:", x, y, w, h, "right", td);
        textNotes = new JTextField();
        textNotes.setBounds(x1, y, w * 5, h);
        td.add(textNotes);
        y += h1;

        AddLabel("Repeat:", x, y, w, h, "right", td);
        comboRepeat = new JComboBox<>();
        comboRepeat.addItem("none");
        comboRepeat.addItem("week");
        comboRepeat.addItem("2week");
        comboRepeat.addItem("4week");
        comboRepeat.addItem("month");
        comboRepeat.addItem("year");
        comboRepeat.setBounds(x1, y, 100, h);

        // add values if editrow is valid table row
        if (!accounts.isEmpty() && editrow >= 0) {
            td.setTitle("Edit Transaction");
            ArrayList<String> t = accounts.get(account).transactions.get(editrow);  // get the transaction in t
            textPayee.setText(t.get(0));
            // set spinDate to transaction date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            try {
                date = sdf.parse(t.get(1)); }
            catch (Exception e) {
                date = new Date(); }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            spinDate.setValue(calendar.getTime());
            if (t.get(2).startsWith("-"))
                textDebit.setText(t.get(2).substring(1));
            else
                textCredit.setText(t.get(2));
            textCategory.setText(t.get(3));
            textNotes.setText(t.get(4));
            comboRepeat.setSelectedItem(t.get(6));
        }
        td.add(comboRepeat);
        y += h1 + h;

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.setBounds(x1, y, 100, h);
        buttonCancel.addActionListener(e -> td.dispose());
        td.add(buttonCancel);

        JButton buttonOK = new JButton("OK");
        buttonOK.addActionListener(e -> {
            okButton(account, editrow);
            td.dispose();
        });
        buttonOK.setBounds(x1 + 120, y, 100, h);
        td.add(buttonOK);

        td.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        td.setVisible(true);
    }

    private static void AddLabel(String text, int x, int y, int w, int h, String align, JDialog td) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, w, h);
        if (align.equals("right"))
            label.setHorizontalAlignment(SwingConstants.RIGHT);
//        else
//            label.setHorizontalAlignment(SwingConstants.LEFT);
        td.add(label);
    }

    private void okButton(int account, int row) {
        String payee = textPayee.getText().trim();
        if (payee.length() > 100) payee = payee.substring(0, 100);
        Date dateObj = (Date) spinDate.getValue();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(dateObj);

        String amount = General.getAmount(textCredit.getText(), false);
        if (!textDebit.getText().isBlank()) amount = "-" + General.getAmount(textDebit.getText(), false);
        if (amount.equals("0.00") || amount.equals("-0.00")) return;

        String category = textCategory.getText().trim();
        if (category.length() > 15) category = category.substring(0, 15);
        String notes = textNotes.getText().trim();
        if (notes.length() > 200) notes = notes.substring(0, 200);
        String repeat = row == 0 ? "none" : comboRepeat.getSelectedItem().toString();
        String id = "";     // used for bank transfers

        BankAccount ba = accounts.get(account);
        if (row < 0) {      // add new transaction
            ba.transactions.add(new ArrayList<>());
            Collections.addAll(ba.transactions.getLast(), payee, date, amount, category, notes, id, repeat,"","","","","*"); }
        else {              // update existing transaction
            ba.setPayee(row, payee);
            ba.setDate(row, date);
            ba.setAmount(row, amount);
            ba.setCategory(row, category);
            ba.setNotes(row, notes);
            // don't change id (5)
            ba.setRepeat(row, repeat); }
        ba.UpdateRecurringTransactions(accounts);
        ba.SortTransactions();
    }
}

class FormMain {
    List<BankAccount> accounts;         // so it can be accessed by other classes
    boolean canUpdate = false;          // delay updating components during startup
    String lastSearchTerm = "";

    JFrame frame;
    JLabel labelStatus;
    JTextField textBalance;
    JTextField textFilter;
    JCheckBox checkMonth;
    JComboBox<String> comboAccounts;
    JTable table;
    DefaultTableModel tModel;

    public FormMain(List<BankAccount> accountsList) {   // Create the JFrame main window
        accounts = accountsList;                        // so it can be accessed by other classes
        frame = new JFrame("RAW MoneyManager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);              // center the window on the screen
        frame.setLayout(new BorderLayout());

        CreateTopPanel();
        CreateCenterPanel();
        CreateBottomPanel();
        CreateMenu();

        frame.setVisible(true);
        canUpdate = true;               // now we can update components
        FileOp.Read(accounts, ".csv");
        RefreshcomboAccounts();
        SelectLastTableRow();
    }

    private void CreateBottomPanel() {
        JPanel panelBottom = new JPanel();
        panelBottom.setBackground(General.AppColor());
        labelStatus = new JLabel("Status: OK");
        panelBottom.add(labelStatus);
        frame.add(panelBottom, BorderLayout.SOUTH);
    }

    private void CreateTopPanel() {
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBackground(General.AppColor());
        JPanel panelTopLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelTopLeft.setBackground(General.AppColor());
        JPanel panelTopRight = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelTopRight.setBackground(General.AppColor());

        JButton saveButton = new JButton("💾 Save");
        saveButton.addActionListener(e -> FileSave("Save", ".csv"));
        panelTopLeft.add(saveButton);
        JButton addTransactionButton = new JButton("💰Add");
        addTransactionButton.addActionListener(e -> TransactionAdd());
        panelTopLeft.add(addTransactionButton);

        JLabel labelAccount = new JLabel(" |   Account:");
        panelTopRight.add(labelAccount);
        comboAccounts = new JComboBox<>();
        comboAccounts.addActionListener(e -> SelectAccount());
        panelTopRight.add(comboAccounts);

        JLabel labelFilter = new JLabel(" |   Filter:");
        panelTopRight.add(labelFilter);
        textFilter = new JTextField(10);
        textFilter.addActionListener(e -> SelectAccount());
        panelTopRight.add(textFilter);

        JLabel labelTotal = new JLabel(" Amount:");
        panelTopRight.add(labelTotal);
        textBalance = new JTextField(7);
        textBalance.setEditable(false);
        panelTopRight.add(textBalance);

        checkMonth = new JCheckBox("This month");
        checkMonth.setSelected(false);
        checkMonth.setBackground(General.AppColor());
        checkMonth.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) { SelectAccount(); }
        });
        panelTopRight.add(checkMonth);

        panelTop.add(panelTopLeft, BorderLayout.WEST);
        panelTop.add(panelTopRight, BorderLayout.CENTER);
        frame.add(panelTop, BorderLayout.NORTH);
    }

    private void CreateCenterPanel() {
        // payee, date, amount, category, notes, id, repeat
        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BorderLayout());
        // DefaultTableModel holds the transactions data
        tModel = new DefaultTableModel(new String[]{"Num", "Payee", "Date", "Category", "Repeat","Credit", "Debit", "Balance"}, 0);
        table = new JTable(tModel) {        // Create a JTable with the model
            @Override
            public boolean isCellEditable(int row, int column) {    // cells are not editable
                return false;
            }
        };
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(350);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
//        table.setIntercellSpacing(new java.awt.Dimension(1, 0));
        // Set alignment for each column
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);    // num
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);    // payee
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);    // date
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);    // category
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);    // repeat
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);     // credit
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);     // debit
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);     // balance

//        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
//        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Align to center
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setHeaderRenderer(centerRenderer);
        columnModel.getColumn(1).setHeaderRenderer(leftRenderer);
        columnModel.getColumn(2).setHeaderRenderer(centerRenderer);
        columnModel.getColumn(3).setHeaderRenderer(centerRenderer);
        columnModel.getColumn(4).setHeaderRenderer(centerRenderer);
        columnModel.getColumn(5).setHeaderRenderer(rightRenderer);
        columnModel.getColumn(6).setHeaderRenderer(rightRenderer);
        columnModel.getColumn(7).setHeaderRenderer(rightRenderer);

//        table.getSelectionModel().addListSelectionListener(e -> TableRowSelected(accounts));  //show notes
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) TableRowSelected();
                if (e.getClickCount() == 2) EditTransaction();
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> TableRowSelected());

        JScrollPane scrollPane = new JScrollPane(table);    // add JTable to a JScrollPane
        panelCenter.add(scrollPane);                        // add scrollpane to center panel
        frame.add(panelCenter, BorderLayout.CENTER);
    }

    public void TableRowSelected() {
        int row = table.getSelectedRow();
        int account = getAccount();
        if (account < 0 || row < 0) return;
        labelStatus.setText("📝 Note: " + accounts.get(account).getNotes(row));
    }

    /** When an account is selected, clear the table, fill from selected account.
     * Used by comboAccounts.addActionListener() and when we need to refresh the table. */
    public void SelectAccount() {
        if (!frame.isVisible()) return;                         // not yet displayed window
        if (canUpdate == false || getAccount() < 0) return;     // wait till after window is initialised
        tModel.setRowCount(0);                                  // clear transactions table
        BankAccount account = accounts.get(getAccount());       // bank account to display
        if (account.transactions.isEmpty()) return;     // no transactions?

        String startDate = "1970-01-01";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (checkMonth.isSelected())
            startDate = formatter.format(LocalDate.now().withDayOfMonth(1));

        String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
        double balance = 0.0, subtotal = 0.0;
        String category = textFilter.getText().trim();
        String[] s;         // table = "Num", "Payee", "Date", "Category", "Repeat","Credit", "Debit", "Balance"
        for (int i = 0; i < account.transactions.size(); i++) {
            double amount = Double.parseDouble(account.getAmount(i));
            balance += amount;      // file = payee, date, amount, category, notes, id, repeat
            if (checkMonth.isSelected() && startDate.compareTo(account.getDate(i)) > 0) continue;
            if (!category.isBlank() && !account.getCategory(i).equalsIgnoreCase(category.toLowerCase())) continue;
            String repeat = "none".equals(account.getRepeat(i)) ? "" : account.getRepeat(i);    // none = blank
            subtotal += amount;    // only visible transactions
            if (amount < 0)     //table = "Num,',"Payee", "Date", "Category", "Repeat","Credit", "Debit", "Balance"
                s = new String[]{String.format("%04d", i), account.getPayee(i), account.getDate(i),
                        account.getCategory(i), repeat, "", String.format("%.2f", amount),
                        String.format("%s%.2f", currency, balance)};
            else
                s = new String[]{String.format("%04d", i), account.getPayee(i), account.getDate(i),
                        account.getCategory(i), repeat, String.format("%.2f", amount), "",
                        String.format("%s%.2f", currency, balance)};
            tModel.addRow(s);
        }
        textBalance.setText(String.format("%s%.2f", currency, subtotal));
    }

    public void SelectLastTableRow() {
        int lastrow = table.getRowCount() - 1;
        if (lastrow < 0) return;
        table.setRowSelectionInterval(lastrow, lastrow);    // select last row
        table.scrollRectToVisible(table.getCellRect(lastrow, 0, true)); // scroll to last row
    }

    public void RefreshcomboAccounts() {
        comboAccounts.setSelectedIndex(-1);      // avoids triggering table refresh?
        comboAccounts.removeAllItems();
        tModel.setRowCount(0);
        if (accounts.isEmpty()) return;
        for (BankAccount account : accounts)
            comboAccounts.addItem(account.name);
        comboAccounts.setSelectedIndex(0);       // select first account - triggers table refresh
    }

    public void CreateMenu() {
        // File menu ------------------------------------------------
        JMenuItem menuFileLoad = new JMenuItem(" Load ");
        JMenuItem menuFileSave = new JMenuItem(" Save ");
        JMenuItem menuFileSaveAs = new JMenuItem(" Save to... ");
        JMenuItem menuFileSaveBackup = new JMenuItem(" Save backup ");
        JMenuItem menuFileQuit = new JMenuItem(" Quit (save + exit)     ");
        menuFileLoad.addActionListener(e -> FileLoad(".csv"));
        menuFileSave.addActionListener(e -> FileSave("Save", ".csv"));
        menuFileSaveAs.addActionListener(e -> FileSave("Save as", ".csv"));
        menuFileSaveBackup.addActionListener(e -> FileSave("Save", ".bak"));
        menuFileQuit.addActionListener(e -> FileQuit());
//        menuFileQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)); // Ctrl+Q
        JMenu fileMenu = new JMenu(" File");
        fileMenu.add(menuFileLoad);
        fileMenu.addSeparator();
        fileMenu.add(menuFileSave);
        fileMenu.add(menuFileSaveAs);
        fileMenu.add(menuFileSaveBackup);
        fileMenu.addSeparator();
        fileMenu.add(menuFileQuit);

        // Account menu ---------------------------------------------
        JMenuItem menuAccountNew = new JMenuItem(" New account ");
        JMenuItem menuAccountTransfer = new JMenuItem(" Transfer money ");
        JMenuItem menuAccountFind = new JMenuItem(" Find in accounts ");
        JMenuItem menuAccountEdit = new JMenuItem(" Rename accounts ");
        JMenuItem menuAccountDelete = new JMenuItem(" Delete this account     ");
        menuAccountNew.addActionListener(e -> AccountNew());
        menuAccountEdit.addActionListener(e -> AccountShowAll());   // edit in account overview
        menuAccountTransfer.addActionListener(e -> AccountTransfer());
        menuAccountFind.addActionListener(e -> FindInAccounts());
        menuAccountDelete.addActionListener(e -> AccountDelete());
        JMenu accountMenu = new JMenu(" Account");
        accountMenu.add(menuAccountNew);
        accountMenu.addSeparator();
        accountMenu.add(menuAccountTransfer);
        accountMenu.add(menuAccountFind);
        accountMenu.add(menuAccountEdit);
        accountMenu.add(menuAccountTransfer);
        accountMenu.addSeparator();
        accountMenu.add(menuAccountDelete);

        // Transaction menu -----------------------------------------
        JMenuItem menuTransactionAdd = new JMenuItem(" Add transaction ");
        JMenuItem menuTransactionDuplicate = new JMenuItem(" Duplicate transaction ");
        JMenuItem menuTransactionTransfer = new JMenuItem(" Transfer money ");
        JMenuItem menuTransactionEdit = new JMenuItem(" Edit transaction ");
        JMenuItem menuTransactionFind = new JMenuItem(" Find transaction ");
        JMenuItem menuTransactionFindNext = new JMenuItem(" Find next");
        JMenuItem menuTransactionDelete = new JMenuItem(" Delete transaction");
        menuTransactionAdd.addActionListener(e -> TransactionAdd());
        menuTransactionDuplicate.addActionListener(e -> DuplicateTransaction());
        menuTransactionTransfer.addActionListener(e -> AccountTransfer());
        menuTransactionEdit.addActionListener(e -> EditTransaction());
        menuTransactionFind.addActionListener(e -> FindTransaction());
        menuTransactionFindNext.addActionListener(e -> FindNextTransaction());
        menuTransactionDelete.addActionListener(e -> TransactionDelete());
        menuTransactionAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));  // Ctrl+A
        menuTransactionFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));  // Ctrl+F
        menuTransactionFindNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));  // Ctrl+N
        menuTransactionEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)); // Ctrl+E
        menuTransactionDuplicate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)); // Ctrl+D
        menuTransactionTransfer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)); // Ctrl+T
        JMenu transactionMenu = new JMenu(" Transaction ");
        transactionMenu.add(menuTransactionAdd);
        transactionMenu.add(menuTransactionDuplicate);
        transactionMenu.add(menuTransactionTransfer);
        transactionMenu.add(menuTransactionEdit);
        transactionMenu.addSeparator();
        transactionMenu.add(menuTransactionFind);
        transactionMenu.add(menuTransactionFindNext);
        transactionMenu.addSeparator();
        transactionMenu.add(menuTransactionDelete);

        // Reports menu ---------------------------------------------
        JMenuItem menuReportsShowAll = new JMenuItem(" Account overview ");
        JMenuItem menuReportsUpcomingBills = new JMenuItem(" Show upcoming bills          ");
        JMenuItem menuReportsMonthlyInOut = new JMenuItem(" Monthly in / out");
        JMenuItem menuReportsCategories = new JMenuItem(" Categories report");
        menuReportsShowAll.addActionListener(e -> AccountShowAll());
        menuReportsUpcomingBills.addActionListener(e -> new FormUpcomingBills(accounts, frame));
        menuReportsMonthlyInOut.addActionListener(e -> ReportsMonthly());
        menuReportsCategories.addActionListener(e -> CategoriesReport());
        JMenuItem reportsMenu = new JMenu(" Reports ");
        reportsMenu.add(menuReportsShowAll);
        reportsMenu.add(menuReportsUpcomingBills);
        reportsMenu.add(menuReportsUpcomingBills);
        reportsMenu.add(menuReportsMonthlyInOut);
        reportsMenu.add(menuReportsCategories);

        // create Help menu items ------------------------------------
        JMenuItem menuSystemTheme = new JMenuItem(" System theme  ");
        JMenuItem menuMetalTheme = new JMenuItem(" Metal theme  ");
        JMenuItem menuNimbusTheme = new JMenuItem(" Nimbus theme  ");
        menuSystemTheme.addActionListener(e -> FileOp.SetConfig("theme", "System") );
        menuMetalTheme.addActionListener(e -> FileOp.SetConfig("theme", "Metal") );
        menuNimbusTheme.addActionListener(e -> FileOp.SetConfig("theme", "Nimbus") );

        JMenuItem menuFont11 = new JMenuItem(" Font size 11  ");
        JMenuItem menuFont12 = new JMenuItem(" Font size 12  ");
        JMenuItem menuFont13 = new JMenuItem(" Font size 13  ");
        JMenuItem menuFont14 = new JMenuItem(" Font size 14  ");
        JMenuItem menuFont15 = new JMenuItem(" Font size 15  ");
        menuFont11.addActionListener(e -> FileOp.SetConfig("fontsize", "11") );
        menuFont12.addActionListener(e -> FileOp.SetConfig("fontsize", "12") );
        menuFont13.addActionListener(e -> FileOp.SetConfig("fontsize", "13") );
        menuFont14.addActionListener(e -> FileOp.SetConfig("fontsize", "14") );
        menuFont15.addActionListener(e -> FileOp.SetConfig("fontsize", "15") );

        JMenuItem menuArial = new JMenuItem(" Arial ");
        JMenuItem menuGeorgia = new JMenuItem(" Georgia ");
        JMenuItem menuHelvetica = new JMenuItem(" Helvetica ");
        JMenuItem menuTimesNewRoman = new JMenuItem(" Times New Roman ");
        JMenuItem menuVerdana = new JMenuItem(" Verdana ");
        menuArial.addActionListener(e -> FileOp.SetConfig("font", "Arial") );
        menuGeorgia.addActionListener(e -> FileOp.SetConfig("font", "Georgia") );
        menuHelvetica.addActionListener(e -> FileOp.SetConfig("font", "Helvetica") );
        menuTimesNewRoman.addActionListener(e -> FileOp.SetConfig("font", "Times New Roman") );
        menuVerdana.addActionListener(e -> FileOp.SetConfig("font", "Verdana") );

        JMenu menuSettings = new JMenu(" Settings (on next start) ");    // Help > Settings submenu
        menuSettings.add(menuSystemTheme);
        menuSettings.add(menuMetalTheme);
        menuSettings.add(menuNimbusTheme);
        menuSettings.addSeparator();
        menuSettings.add(menuFont11);
        menuSettings.add(menuFont12);
        menuSettings.add(menuFont13);
        menuSettings.add(menuFont14);
        menuSettings.add(menuFont15);
        menuSettings.addSeparator();
        menuSettings.add(menuArial);
        menuSettings.add(menuGeorgia);
        menuSettings.add(menuHelvetica);
        menuSettings.add(menuTimesNewRoman);
        menuSettings.add(menuVerdana);

        JMenuItem helpAbout = new JMenuItem("About MoneyMan...     ");
        helpAbout.addActionListener(e -> HelpAbout());
        JMenu helpMenu = new JMenu(" Help ");
        helpMenu.add(menuSettings);
        helpMenu.addSeparator();
        helpMenu.add(helpAbout);

        // finish up ------------------------------------------------
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(accountMenu);
        menuBar.add(transactionMenu);
        menuBar.add(reportsMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);
    }

    private void CategoriesReport() {
        if (accounts.isEmpty() || getAccount() < 0) return;
        new FormCategoriesReport(accounts.get(getAccount()), frame);
    }

    private void ReportsMonthly() {
        if (accounts.isEmpty() || getAccount() < 0) return;
        new FormMoneyInOut(accounts.get(getAccount()), frame);      // pass the current bank account
    }

    private void HelpAbout() {
        new FormHelpAbout(frame);
    }

    public void FileLoad(String fileExt) {
        if (!accounts.isEmpty()) {
            if (JOptionPane.showConfirmDialog(frame, "Save current accounts?", "Warning!", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                FileOp.Write(accounts, ".csv");
        }
        FileOp.SetSaveFolder();
        FileOp.Read(accounts, fileExt);
        RefreshcomboAccounts();
        SelectLastTableRow();
    }

    private void FileSave(String saveAs, String fileExt) {
        if (saveAs.equals("Save As"))
            FileOp.SetSaveFolder();
        FileOp.Write(accounts, fileExt);
        JOptionPane.showMessageDialog(null,
                "Accounts have been saved...",
                "MoneyMan",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void FileQuit() {
        FileOp.Write(accounts, ".csv");
        System.exit(0);
    }

    private void AccountNew() {
        new FormNewAccount(accounts, frame);
        RefreshcomboAccounts();
    }

    private void AccountShowAll() {
        new FormAccountOverview(accounts, frame);
        RefreshcomboAccounts();
    }

    private void AccountTransfer() {
        if (comboAccounts.getItemCount() < 2) {
            JOptionPane.showMessageDialog(null,
                    "Two or more accounts are required to transfer money.",
                    "MoneyMan",
                    JOptionPane.PLAIN_MESSAGE);
            return; }
        new FormTransfer(accounts, frame);
        SelectAccount();
    }

    private void AccountDelete() {
        if (comboAccounts.getItemCount() == 0) {
            JOptionPane.showMessageDialog(null,
                    "No accounts to delete!",
                    "MoneyMan",
                    JOptionPane.PLAIN_MESSAGE);
            return; }
        General.DeleteAccount(accounts, getAccount(), frame);
        RefreshcomboAccounts();
    }

    private void TransactionAdd() {
        if (getAccount() < 0) return;
        new FormTransaction(accounts, getAccount(), -1, frame);  // row = -1 = new transaction
        accounts.get(getAccount()).SortTransactions();
        SelectAccount();
    }

    private void TransactionDelete() {
        int row = getTableRow();
        if (row < 0 || getAccount() < 0) return;
        if (JOptionPane.showConfirmDialog(frame,"Are you sure?", "Delete Transaction", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            accounts.get(getAccount()).DeleteTransaction(accounts, row);
        SelectAccount();
    }

    private void EditTransaction() {
        int row = getTableRow();
        int account = getAccount();
        if (row < 0 || account < 0) return;
        if (!accounts.get(account).getID(row).isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Can't edit an account transfer. Delete \nit and then create a new one.",
                    "MoneyMan",
                    JOptionPane.PLAIN_MESSAGE);
            return; }
        new FormTransaction(accounts, account, row, frame);   // transaction to edit
        accounts.get(account).SortTransactions();
        SelectAccount();
    }

    /** Selected table row is different to transaction row in filtered view. Use Num in column 0. */
    private int getTableRow() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        return Integer.parseInt(table.getValueAt(row,0).toString());
    }

    private void DuplicateTransaction() {
        int row = getTableRow();
        int account = getAccount();
        if (account < 0 || row < 0) return;
        if (!accounts.get(account).getID(row).isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Can't duplicate an account transfer. \nCreate a new one instead.",
                    "MoneyMan",
                    JOptionPane.PLAIN_MESSAGE);
            return; }

        ArrayList<String> t = accounts.get(account).transactions.get(row);
        accounts.get(account).transactions.add(new ArrayList<>());  // add new transaction, copy the old one
        Collections.addAll(accounts.get(account).transactions.getLast(), t.get(0), t.get(1), t.get(2),
                t.get(3), t.get(4), t.get(5), t.get(6), t.get(7), t.get(8), t.get(9), t.get(10), t.get(11));
        accounts.get(account).SortTransactions();                   // sort transactions
        SelectAccount();                                            // refresh table
        table.setRowSelectionInterval(row + 1, row + 1);            // select duplicate row
        table.scrollRectToVisible(table.getCellRect(row+1, 0, true)); // scroll to row
    }

    public void FindInAccounts() {
        if (accounts.isEmpty() || getAccount() < 0) return;
        if (!textFilter.getText().isBlank() || checkMonth.isSelected()) {   // show all transactions
            checkMonth.setSelected(false);
            textFilter.setText("");
            SelectAccount(); }
        int startRow = 0;
        String text = JOptionPane.showInputDialog("What do you want to find?:");
        if (text == null || text.isBlank()) return;             // Cancel clicked
        lastSearchTerm = text.strip().toLowerCase();            // save search tern
        int lastrow = -1;
        for (int i = 0; i < accounts.size(); i++) {
            lastrow = accounts.get(i).FindTransaction(lastSearchTerm, startRow);
            if (lastrow < 0) continue;                  // not found
            comboAccounts.setSelectedIndex(i);          // select the account
            table.setRowSelectionInterval(lastrow, lastrow);
            table.scrollRectToVisible(table.getCellRect(lastrow, 0, true));
            break;      // stop if search term found
        }
        if (lastrow < 0) {          // not found anything
            JOptionPane.showMessageDialog(frame, "No results found for '" + lastSearchTerm + "'.");
        }
    }

    public void FindNextTransaction() {
        if (accounts.isEmpty() || getAccount() < 0) return;
        if (!textFilter.getText().isBlank() || checkMonth.isSelected()) {   // show all transactions
            checkMonth.setSelected(false);
            textFilter.setText("");
            SelectAccount(); }
        int startRow = (table.getSelectedRow() + 1) % table.getRowCount();   // next row or 0
        String text = lastSearchTerm;
        if (text.isBlank()) text = JOptionPane.showInputDialog("What to find?:");

        if (text == null || text.isBlank()) return;           // Cancel clicked
        lastSearchTerm = text.strip().toLowerCase();
        int lastrow = accounts.get(getAccount()).FindTransaction(lastSearchTerm, startRow);
        if (lastrow < 0) lastrow = table.getRowCount() - 1;            // not found
        table.setRowSelectionInterval(lastrow, lastrow);
        table.scrollRectToVisible(table.getCellRect(lastrow, 0, true));
    }

    public void FindTransaction() {
        if (accounts.isEmpty() || getAccount() < 0) return;
        if (!textFilter.getText().isBlank() || checkMonth.isSelected()) {   // remove filters
            checkMonth.setSelected(false);
            textFilter.setText("");
            SelectAccount(); }

        int startRow = 0;
        String text = JOptionPane.showInputDialog("What to find?:");
        if (text == null || text.isBlank()) return;           // Cancel clicked

        lastSearchTerm = text.strip().toLowerCase();
        int lastrow = accounts.get(getAccount()).FindTransaction(lastSearchTerm, startRow);
        if (lastrow < 0) lastrow = table.getRowCount() - 1;            // not found
        table.setRowSelectionInterval(lastrow, lastrow);
        table.scrollRectToVisible(table.getCellRect(lastrow, 0, true));
    }

    /** Get account index from comboAccount list */
    private int getAccount() {
        if (comboAccounts.getSelectedIndex() < comboAccounts.getItemCount()) return comboAccounts.getSelectedIndex();
        return -1;      // no selection or index out of range
    }
}

public class MoneyMan {
    public static void main(String[] args) {
        List<BankAccount> accounts = new ArrayList<>();
        General.ApplyTheme();           // Apply theme before using any Swing components
        General.SetFont();
        SwingUtilities.invokeLater(() -> {
            FormMain moneyman = new FormMain(accounts);
        });
    }
}