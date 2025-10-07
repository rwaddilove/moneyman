// changed SortingTransactions()
// changed GetNewDate()
// recurring transactions would be easier if transfers named account being transferred to

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BankAccount {
    String name;
    String bankID;      // a unique ID for each bank account
    ArrayList<ArrayList<String>> transactions = new ArrayList<>();

    public BankAccount(String bankName) {
        name = bankName;
        Random random = new Random();               // random number for new recurring transaction ID
        bankID = Long.toString(random.nextInt(Integer.MAX_VALUE));
    }

    public String getPayee(int row) {
        return transactions.get(row).get(0);
    }
    public String getDate(int row) {
        return transactions.get(row).get(1);
    }
    public String getAmount(int row) {
        return transactions.get(row).get(2);
    }
    public String getCategory(int row) {
        return transactions.get(row).get(3);
    }
    public String getNotes(int row) {
        return transactions.get(row).get(4);
    }
    public String getID(int row) {
        return transactions.get(row).get(5);
    }
    public String getRepeat(int row) {
        return transactions.get(row).get(6);
    }

    public void setPayee(int row, String id) {
        transactions.get(row).set(0, id);
    }
    public void setDate(int row, String id) {
        transactions.get(row).set(1, id);
    }
    public void setAmount(int row, String id) {
        transactions.get(row).set(2, id);
    }
    public void setCategory(int row, String id) {
        transactions.get(row).set(3, id);
    }
    public void setNotes(int row, String id) {
        transactions.get(row).set(4, id);
    }
    public void setID(int row, String id) {
        transactions.get(row).set(5, id);
    }
    public void setRepeat(int row, String repeat) {
        transactions.get(row).set(6, repeat);
    }

    public double getBalance(String category) {
        double total = 0.0;
        for (int i = 0; i < transactions.size(); ++i)
            if (category.isEmpty() || category.equals(getCategory(i)))
                total += Double.parseDouble(getAmount(i));
        return total;
    }

    public void SortTransactions() {
        if (transactions.size() < 3) return;    // not enough transactions for sorting
        for (int i = 0; i < transactions.size() - 1; ++i)     // 3 or more transactions
            for (int j = transactions.size() - 2; j >= i; --j)
//                if (transactions.get(j).get(1).compareToIgnoreCase(transactions.get(j + 1).get(1)) > 0)
                if (getDate(j).compareToIgnoreCase(getDate(j + 1)) > 0)
                    Collections.swap(transactions, j, j + 1);
    }

    public LocalDate getNewDate(int i) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate newDate = LocalDate.parse(getDate(i), formatter); // date of transaction
        if (getRepeat(i).equals("week")) return newDate.plusWeeks(1);
        if (getRepeat(i).equals("2week")) return newDate.plusWeeks(2);
        if (getRepeat(i).equals("4week")) return newDate.plusWeeks(4);
        if (getRepeat(i).equals("month")) return newDate.plusMonths(1);
        if (getRepeat(i).equals("year")) return newDate.plusYears(1);
        return newDate;     // next date of a recurring transaction
    }

    public void UpdateRecurringTransactions(List<BankAccount> accounts) {
        while (true) {
            boolean updatedTransaction = false;   // loop until no more recurring transactions
            for (int i = 0; i < transactions.size(); ++i) {
                if (getRepeat(i).equals("none")) continue;    // not a recurring transaction? next loop

                // this is a recurring transaction
                LocalDate newDate = getNewDate(i);                  // next day it occurs
                if (newDate.isAfter(LocalDate.now())) continue;     // in the future? next loop

                // this recurring transaction is today or earlier - add it to transactions
                String id = getID(i);                       // id is for account transfers, otherwise ""
                WriteTransaction(getPayee(i), newDate.toString(), getAmount(i), getCategory(i), getNotes(i), id, getRepeat(i));
                setRepeat(i, "none");                // remove repeat from original transaction
                updatedTransaction = true;                  // do another loop
                if (id.isBlank()) continue;                 // if transaction was not an account transfer, we're done

                Random random = new Random();               // random number for new recurring transaction ID
                String newid = Long.toString(random.nextInt(Integer.MAX_VALUE));
                setID(i + 1, newid);                // set a new ID for the transaction just added

                // this recurring transaction transfers money between accounts
                for (BankAccount account : accounts) {                          // for each bank account
                    if (account.name.equals(name)) continue;                    // but not this one!
                    for (int j = 0; j < account.transactions.size(); ++j) {     // for each transaction
                        if (account.getID(j).equals(id)) {                      // is it the corresponding transfer?
                            account.WriteTransaction(account.getPayee(j), newDate.toString(), account.getAmount(j), getCategory(j), getNotes(j), newid, "none");
                            break; }            // we're done, so stop
                    }
                }
            }
            if (!updatedTransaction) break;     // stop when no more recurring transactions updated
        }
    }

    public void WriteTransaction(String payee, String date, String amount, String category, String notes, String id, String repeat) {
        transactions.add(new ArrayList<>());
        Collections.addAll(transactions.getLast(), payee, date, amount, category, notes, id, repeat,"","","","","*");
    }

    public void DeleteTransaction(List<BankAccount> accounts, int row) {
        if (row < 0 || row >= transactions.size()) return;

        String id = getID(row);             // transfers between accounts have an ID, otherwise ""
        transactions.remove(row);
        if (id.isBlank()) return;   // we're done if this was not a transfer

        // find and delete corresponding transfer in other accounts
        for (BankAccount account : accounts) {                          // for each bank account
            if (account.name.equals(name)) continue;                    // but not this one!
            for (int j = 0; j < account.transactions.size(); ++j) {     // for each transaction
                if (account.getID(j).equals(id)) {                      // is it the corresponding transfer?
                    account.transactions.remove(j);
                    break; }            // we're done, so stop
            }
        }
    }

    /** Seach transactions for a string. Returns row number of the match, or -1 if not found. */
    public int FindTransaction(String text, int startRow) {
        if (text == null || text.isBlank() || startRow < 0 || startRow >= transactions.size()) return -1;
        for (int i = startRow; i < transactions.size(); ++i)
            if (getPayee(i).toLowerCase().contains(text)
                    || getDate(i).toLowerCase().contains(text)
                    || getCategory(i).toLowerCase().contains(text)
                    || getNotes(i).toLowerCase().contains(text))
                return i;
        return -1;
    }
}