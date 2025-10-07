#  MoneyMan

MoneyMan is a Java desktop app for Mac, PC, Linux that enables you to manage your money.

* Create multiple accounts like bank, savings, credit card.
* Record transactions and assign them to categories.
* Create recurring transactions like bills, income and transfers.
* Move money between accounts.
* See your net worth across all accounts.
* See your spending each month or in each category.
* Sync across computers.

![RAW MoneyMan Java app](https://github.com/rwaddilove/moneyman/blob/main/moneyman.jpg)

![RAW MoneyMan Java app](https://github.com/rwaddilove/moneyman/blob/main/account-menu.jpg)

![RAW MoneyMan Java app](https://github.com/rwaddilove/moneyman/blob/main/reports-menu.jpg)

![RAW MoneyMan Java app](https://github.com/rwaddilove/moneyman/blob/main/taction-menu.jpg)

![RAW MoneyMan Java app](https://github.com/rwaddilove/moneyman/blob/main/add-taction.jpg)

### Tips for using Money Manager

*MoneyMan was created for myself as a way to learn Java and Swing. There are no guarantees, use it at your own risk.*

1. **Select a folder**: When it is first run, it asks you to select a folder in which to save its data files. This can be anywhere and if you want to use Money Manager on multiple computers, select a synced folder, such as OneDrive, Google Drive, Dropbox and so on. You can use Mac or PC or both, the data files are the same.
2. **Save your files**: Use *File > Save* or *File > Quit* to save your accounts and transactions. *File > Save to* to lets you save to a different folder. The one you select becomes the new default save folder. *File > Save backup* saves a copy of the accounts with the .bak extension. To use a backup, delete the .csv files and rename the .bak files to .csv.
3. **When not to save**: Files are **NOT** saved when the window close button is clicked. Pros: This can be used as an 'undo' feature. For example, if you delete an account by mistake, click the window close button, restart the app and your deleted account reappears because the last saved state is loaded. Cons: Don't make a lot of changes and close the app with the window close button - you'll lose them. You must use *File > Save* or *File > Quit* to save changes.
4. **When to use the minus (-) sign**: When adding a transaction, enter into the credit or debit box. No minus sign is needed. When creating a new account, use a minus if necessary. A credit card account will be -253.86 for example.
5. **Account transfers**: Select the *from* and *to* account and enter the amount (no minus). It automatically appears as a debit in one and a credit in the other. When paying a credit card account or moving money to a savings account, use the *Transfer money* menu.
6. **The Filter and Amount boxes**: Type a category into the filter box and hit Enter to see only transactions with that category. The Amount box shows the total. For example, enter 'groceries' into the filter box to see what you have spent on groceries. Tick This month to see only this month's transactions. It's useful to see how much you've spent on a category. Clear the Filter box and press Enter to show all transactions.
7. **It didn't do anything!** If you select a menu, click a button, whatever, and nothing happens, it probably means you can't do it. Like asking it to produce a report when you don't have any transactions. Sometimes a default value is used, eg. if a number isn't recognised, 0.00 might be used.

**MoneyMan by R.A.Waddilove** https://github.com/raddilove

If you find it useful or have suggestions for improvements, features, or bugs, let me know. I can be reached at rwaddilove on Gmail and Hotmail. You are free to use and modify the code and give it to anyone you want. 

