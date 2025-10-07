// File Operations: Read and write data files, select save folder.

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

class FileOp {
    public static void Read(List<BankAccount> accounts, String fileExt) {
        String folderPath = GetConfig("path");              // get file location
        if (folderPath.isBlank()) folderPath = SetSaveFolder();   // select a folder
        if (folderPath.isBlank()) return;                         // still blank? quit!

        if (!accounts.isEmpty()) accounts.clear();                // clear all bank accounts
        int n = 0;                                                // files are numbered 0/1/2.csv, etc.
        while (true) {
            String filePath = folderPath + "MoneyManager" + n++ + fileExt;   // next filename
            try (Scanner readFile = new Scanner(new File(filePath))) {
                String name = readFile.nextLine();
                String bankID = readFile.nextLine();
                accounts.add(new BankAccount(name));                        // add bank account
                accounts.getLast().bankID = bankID;
                while (readFile.hasNextLine()) {
                    String line = readFile.nextLine();                      // read a transaction
                    line = line.substring(1, line.length() - 1);            // strip first and last "
                    String[] values = line.split("\",\"");            // values[] = transaction items
                    accounts.getLast().transactions.add(new ArrayList<>()); // create empty transaction
                    Collections.addAll(accounts.getLast().transactions.getLast(), values);  // add transaction data
                }
                accounts.getLast().UpdateRecurringTransactions(accounts);
                accounts.getLast().SortTransactions(); }
            catch (FileNotFoundException e) { return; }
        }
    }

    public static void Write(List<BankAccount> accounts, String fileExt) {
        if (accounts.isEmpty()) {
            CleanUpFiles(0, fileExt);                                // delete all files if no accounts
            return; }
        String folderPath = GetConfig("path");                    // get file location
        if (folderPath.isBlank()) folderPath = SetSaveFolder();         // select a folder
        if (folderPath.isBlank()) return;                               // still blank? quit!

        int n = 0;                                                      // files are numbered 0/1/2.csv, etc.
        for (BankAccount account : accounts) {                          // for each bank account
            String fname = folderPath + "MoneyManager" + n++ + fileExt;  // get next filename
            try (FileWriter writeFile = new FileWriter(fname)) {
                writeFile.write(account.name + "\n");
                writeFile.write(account.bankID + "\n");
                for (ArrayList<String> transaction : account.transactions) {    // for each transaction
                    String line = "\"";                 // start with a "
                    for (String item : transaction)     // for all items in transaction
                        line += item + "\",\"";         // add each item & end separated by ","
                    writeFile.write(line.substring(0, (line.length() - 2)) + "\n");     // chop final ,"
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not save " + fname, "Alert", JOptionPane.ERROR_MESSAGE);
            }
        }
        CleanUpFiles(n, fileExt);                    // clean up: delete old files if/when account is deleted
    }

    /** Delete MoneyManager01/02/03.csv etc. files no longer used. */
    public static void CleanUpFiles(int n, String fileExt) {
        String folderPath = GetConfig("path");
        if (folderPath.isBlank()) return;
        for (int i = n; i < (n + 15); ++i) {
            File file = new File(folderPath + "MoneyManager" + i + fileExt);
            file.delete(); }
    }

    public static String GetHomeFolder() {
        String fileSep = System.getProperty("file.separator");
        String homeFolder = System.getProperty("user.home");
        return homeFolder.endsWith(fileSep) ? homeFolder : homeFolder + fileSep;
    }


    /** Select folder to load/save data files. Path saved in RAW-MoneyMan.cfg */
    public static String SetSaveFolder() {
        String path = GetHomeFolder();                      // path = home folder (default)
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select a FOLDER for data files");
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)    // select different folder?
            path = fileChooser.getSelectedFile().getAbsolutePath();
        else
            return "";
        if (!path.endsWith(System.getProperty("file.separator"))) path += System.getProperty("file.separator");

        SetConfig("path", path);

        JOptionPane.showMessageDialog(null,
                "The following folder will be used for MoneyMan data files:\n" + path,
                "MoneyMan",
                JOptionPane.PLAIN_MESSAGE);
        return path;
    }

    /** Get a config value in RAW-MoneyMan.cfg. */
    public static String GetConfig(String name) {
        ArrayList<String> settings = new ArrayList<>(10);
        // try to read settings from RAW-MoneyMan.cfg into settings array
        try (Scanner readFile = new Scanner(new File(GetHomeFolder() + "RAW-MoneyMan.cfg"))) {
            for (int i = 0; i < 10; ++i)
                settings.add(readFile.nextLine().trim()); }
        catch (Exception e) {
            return ""; }

        if (name.equals("path")) return settings.get(0);
        if (name.equals("theme")) return settings.get(1);
        if (name.equals("font")) return settings.get(2);
        if (name.equals("fontsize")) return settings.get(3);
        if (name.equals("backup")) return settings.get(4);
        return "";
    }

    /** Set a config value in RAW-MoneyMan.cfg. */
    public static void SetConfig(String name, String value) {
        ArrayList<String> settings = new ArrayList<>(10);
        try (Scanner readFile = new Scanner(new File(GetHomeFolder() + "RAW-MoneyMan.cfg"))) {
            for (int i = 0; i < 10; ++i)
                settings.add(readFile.nextLine().trim()); }
        catch (Exception e) {
            settings.clear();   // reset settings if not found or error
            for (int i = 0; i < 10; ++i) settings.add(""); }

        if (name.equals("path")) settings.set(0, value);
        if (name.equals("theme")) settings.set(1, value);
        if (name.equals("font")) settings.set(2, value);
        if (name.equals("fontsize")) settings.set(3, value);
        if (name.equals("backup")) settings.set(4, value);

        // save RAW-MoneyMan.cfg in user's home folder
        try (FileWriter writeFile = new FileWriter(GetHomeFolder() + "RAW-MoneyMan.cfg")) {
            for (int i = 0; i < 10; ++i)
                writeFile.write(settings.get(i) + "\n"); }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error writing RAW-MoneyMan.cfg", "Alert", JOptionPane.ERROR_MESSAGE); }
    }
}