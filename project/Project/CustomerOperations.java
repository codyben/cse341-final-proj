import java.sql.*;
import java.util.*;
class CustomerOperations extends DatabaseOperations {

    private PreparedStatement all_users;
    private PreparedStatement account_details;
    private PreparedStatement num_accounts;
    private PreparedStatement num_credit;
    private PreparedStatement num_debit;
    private PreparedStatement num_cards;
    private PreparedStatement num_loans;
    private PreparedStatement do_deposit;
    private PreparedStatement get_debit;
    private PreparedStatement get_credit;
    private PreparedStatement do_credit_purchase;
    private PreparedStatement do_debit_purchase;
    private PreparedStatement do_card_activity;

    CustomerOperations(final Connection c) {
        super(c);
    }

    public HashMap<String, User> get_user_by_id() {
        return null;
        //TODO
    }

    public HashMap<String, User> get_user_by_name() {
        return null;
        //TODO
    }
    public HashMap<String, User> list_all_users() {
        /* get all users for use in a promptmap */
        try {
            HashMap<String, User> m = new HashMap<>();
            ResultSet result;
            all_users = con.prepareStatement("SELECT * FROM customer");
            result = all_users.executeQuery();

            while(result.next()) {
                String fname = result.getString("first_name");
                String lname = result.getString("last_name");
                // String d_name = fname +" "+lname;
                java.util.Date dob = result.getDate("dob");
                int c_id = result.getInt("customer_id");
                String email = result.getString("email");
                User temp = new User(c_id, fname, lname, dob);
                m.put(temp.full_name+" (ID="+temp.customer_id+")", temp);
            }
            return m;
        } catch(Exception e) {
            Helper.notify("error", "An error occurred while selecting all users. Due to the severity, the program will now exit.", true);
            return null;
        }
    }

    public ArrayList<Account> account_details_for_user(final User customer) {
        /* list account metadata */
        try {
            ArrayList<Account> accounts = new ArrayList<>();

            ResultSet result;

            account_details = con.prepareStatement("SELECT acct_id, balance, interest, creation_date, customer_id, add_date, nvl(min_balance, -1) as min_balance FROM ACCOUNT NATURAL JOIN HOLDS NATURAL LEFT OUTER JOIN CHECKING_ACCOUNT WHERE customer_id = ?");
            account_details.setInt(1, customer.customer_id);
            result = account_details.executeQuery();
            while(result.next()) {
                int acct_id = result.getInt("acct_id");
                double balance = result.getDouble("balance");
                double interest = result.getDouble("interest");
                java.util.Date creation_date = result.getDate("creation_date");
                java.util.Date add_date = result.getDate("add_date");
                double min_balance = result.getDouble("min_balance");
                Account temp = new Account(balance, interest, creation_date, add_date, min_balance, acct_id);
                accounts.add(temp);
            }
            return accounts;

        } catch(Exception e) {
            Helper.notify("error", "\nAn error occurred while checking account details. Please try again.\n", true);
            return null;
        }
    }

    public int num_accounts_for_user(final User customer) {
        /* get the number of accounts held by a user */
        try {
            ResultSet result;
            int customer_id = customer.customer_id;
            // System.out.println(customer_id);
            num_accounts = con.prepareStatement("SELECT num_accounts(?) as c from dual");
            num_accounts.setInt(1, customer_id);
            result = num_accounts.executeQuery();
            result.next();
            return result.getInt("c");
        } catch(Exception e) {
            System.out.println(e.getMessage());
            Helper.notify("warn", "\nUnable to retrieve count of user accounts. This feature will be unavailable.\n", true);
            return -1;
        }

    }

    public int num_loans_for_user(final User customer) {//TODO
        try {
            ResultSet result;
            int customer_id = customer.customer_id;
            num_loans = con.prepareStatement("SELECT num_loans(?) as c from dual");
            num_loans.setInt(1, customer_id);
            result = num_loans.executeQuery();
            result.next();
            return result.getInt("c");
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to retrieve count of user loans. This feature will be unavailable.\n", true);
            return -1;
        }
    }

    public int num_debit_for_user(final User customer) {
        /* count user debit cards */
        try {
            ResultSet result;
            int customer_id = customer.customer_id;
            num_debit = con.prepareStatement("SELECT num_debit(?) as c from dual");
            num_debit.setInt(1, customer_id);
            result = num_debit.executeQuery();
            result.next();
            return result.getInt("c");
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to retrieve count of user debit cards. This feature will be unavailable.\n", true);
            return -1;
        }
    }

    public int num_credit_for_user(final User customer) {
        /* get a count of a user's credit cards */
        try {
            ResultSet result;
            int customer_id = customer.customer_id;
            num_credit = con.prepareStatement("SELECT num_credit(?) as c from dual");
            num_credit.setInt(1, customer_id);
            result = num_credit.executeQuery();
            result.next();
            return result.getInt("c");
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to retrieve count of user credit cards. This feature will be unavailable.\n", true);
            return -1;
        }
    }

    public int num_cards_for_user(final User customer) {
        /* get total number of cards */
        try {
            ResultSet result;
            int customer_id = customer.customer_id;
            num_cards = con.prepareStatement("SELECT num_cards(?) as c from dual");
            num_cards.setInt(1, customer_id);
            result = num_cards.executeQuery();
            result.next();
            return result.getInt("c");
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to retrieve count of total user cards. This feature will be unavailable.\n", true);
            return -1;
        }
    }

    /**
     * Performs a purchase using a user's credit card. Returns -1 on exception, 0 on insufficient funds, otherwise on success.  
     * @param amount
     * @param loc_id
     * @param acct_id
     * @return
     */
    public int do_credit_purchase(final double amount, final String name, final int card_id) {
        try {
            ResultSet result;
            do_credit_purchase = con.prepareStatement("SELECT make_purchase_credit(?, ?, ?) as r from dual");
            do_credit_purchase.setDouble(1, amount);
            do_credit_purchase.setString(2, name);
            do_credit_purchase.setInt(3, card_id);
            result = do_credit_purchase.executeQuery();
            result.next();
            if(result.getInt("r") < 0 ) return 0; else return 1; 
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to perform a credit card purchase.\n", true);
            return -1;
        }
    }

    /**
     * Performs a purchase using a user's debit card. Returns -1 on exception, 0 on insufficient funds, otherwise on success. 
     * @param amount
     * @param loc_id
     * @param acct_id
     * @return
     */
    public int do_debit_purchase(final double amount, final String name, final int card_id) {
        try {
            ResultSet result;
            do_debit_purchase = con.prepareStatement("SELECT make_purchase_debit(?, ?, ?) as r from dual");
            do_debit_purchase.setDouble(1, amount);
            do_debit_purchase.setString(2, name);
            do_debit_purchase.setInt(3, card_id);
            result = do_debit_purchase.executeQuery();
            result.next();
            if(result.getInt("r") < 0 ) return 0; else return 1; 
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to perform a debit card purchase.\n", true);
            return -1;
        }
    }
    /**
     * Performs a withdrawal on a user account. Returns false on failure.
     * @param amount
     * @param loc_id
     * @param acct_id
     * @return
     */
    public boolean do_withdrawal(double amount, final int loc_id, final int acct_id) {
        amount *= -1;
        try {
            ResultSet result;
            num_cards = con.prepareStatement("SELECT do_account_action(?, ?, ?) as c from dual");
            num_cards.setDouble(1, amount);
            num_cards.setInt(2, loc_id);
            num_cards.setInt(3, acct_id);
            result = num_cards.executeQuery();
            result.next();
            if(result.getInt("c") < 0 ) return false; else return true; 
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to do an account withdrawal.\n", true);
            return false;
        }
    }
    /**
     * Get the activity (purchases) that happens on a card. Returns null for error, blank ArrayList for no results, full ArrayList otherwise.
     * @param card_id
     * @return
     */
    final public ArrayList<CardActivity> do_activity(final int card_id) {
        ArrayList<CardActivity> records = new ArrayList<>();
        try {
            ResultSet result;
            do_card_activity = con.prepareStatement("SELECT SUBSTR(card_number, 1, 5) || '*********' as card_num, purchase_name, purchase_time, purchase_amount FROM CARD NATURAL JOIN BUYS NATURAL JOIN purchases WHERE card_id = ? ORDER BY purchase_time DESC");
            do_card_activity.setInt(1, card_id);
            result = do_card_activity.executeQuery();
            while(result.next()) {
                String card_num = result.getString("card_num");
                String purchase_name = result.getString("purchase_name");
                java.util.Date purchase_time = result.getDate("purchase_time");
                double p_amt = result.getDouble("purchase_amount");
                CardActivity temp = new CardActivity(card_num, purchase_name, purchase_time, p_amt);
                records.add(temp);
            }
            
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to get account activity.\n", true);
            return null;
        }

        return records;
    }

    /**
     * Performs a deposit on a user account. Returns false on failure.
     * @param amount
     * @param loc_id
     * @param acct_id
     * @return
     */
    final public boolean do_deposit(final double amount, final int loc_id, final int acct_id) {
        
        try {
            ResultSet result;
            num_cards = con.prepareStatement("SELECT do_account_action(?, ?, ?) as c from dual");
            num_cards.setDouble(1, amount);
            num_cards.setInt(2, loc_id);
            num_cards.setInt(3, acct_id);
            result = num_cards.executeQuery();
            result.next();
            if(result.getInt("c") < 0 ) return false; else return true; 
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to do an account deposit.\n", true);
            return false;
        }
    }

    public ArrayList<Debit> user_debit_cards(final User c) {
        //since we did the check previously, assume the user has a debit card.
        ArrayList<Debit> accumulator = new ArrayList<>();
        try {
            ResultSet result;
            get_debit = con.prepareStatement("SELECT customer_id, to_char(pin) as p, to_char(card_id) as c_id, to_char(cvc) as c, card_number, acct_id FROM DEBIT_CARD NATURAL JOIN CARD NATURAL JOIN CUSTOMER_CARDS WHERE customer_id = ?");
            get_debit.setInt(1, c.customer_id);
            result = get_debit.executeQuery();
            while(result.next()) {
                String card_id = result.getString("c_id");
                String pin = result.getString("p");
                String cvc = result.getString("c");
                String card_num = result.getString("card_number");
                int acct_id = result.getInt("acct_id");
                int cust_id = result.getInt("customer_id");
                
                Debit temp = new Debit(card_id, cvc, card_num, pin, acct_id, cust_id);
                accumulator.add(temp);
            }
            return accumulator;
        } catch(Exception e) {
            e.printStackTrace();
            Helper.notify("warn", "\nUnable to return debit card data.\n", true);
            return null;
        }
    }

    public ArrayList<Credit> user_credit_cards(final User c) {
        //since we did the check previously, assume the user has a credit card.
        ArrayList<Credit> accumulator = new ArrayList<>();
        try {
            ResultSet result;
            get_credit = con.prepareStatement("SELECT customer_id, credit_limit, interest, to_char(card_id) as card_id, to_char(cvc) as cvc, card_number, balance_due, running_balance FROM CREDIT_CARD NATURAL JOIN CARD NATURAL JOIN CUSTOMER_CARDS WHERE customer_id = ?");
            get_credit.setInt(1, c.customer_id);
            result = get_credit.executeQuery();
            while(result.next()) {
                String card_id = result.getString("card_id");
                String cvc = result.getString("cvc");
                String card_num = result.getString("card_number");
                double balance = result.getDouble("balance_due");
                double running_balance = result.getDouble("running_balance");
                double interest = result.getDouble("interest");
                double credit_limit = result.getDouble("credit_limit");
                int cust_id = result.getInt("customer_id");
                
                Credit temp = new Credit(card_id, cvc, card_num, interest, balance, running_balance, credit_limit, cust_id);
                accumulator.add(temp);
            }
            return accumulator;
        } catch(Exception e) {
            e.printStackTrace();
            Helper.notify("warn", "\nUnable to return credit card data.\n", true);
            return null;
        }
    }

    public Credit create_credit_card(final Credit c) {
        /* SQL Steps:
         * Insert a new row into CARD
         * Insert a new row into Credit_Card
         * Link them in customer_cards
         * Good2GO
         */

        
        return null;
    }

    public Debit create_debit_card(final Debit d) {
        /* SQL Steps:
         * Insert a new row into CARD
         * Insert a new row into Debit_Card
         * Link in customer cards
         */ 
        return null;
    }

    public Debit replace_debit_card(final User c, final Debit d) {
        return null;
    }

    public Credit replace_credit_card(final User c) {
        return null;
    }
}