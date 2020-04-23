import java.sql.*;
import java.util.*;
class CustomerOperations extends DatabaseOperations {

    private PreparedStatement all_users;
    private PreparedStatement all_users_partial;
    private PreparedStatement account_details;
    private PreparedStatement num_accounts;
    private PreparedStatement num_checking_acct;
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
    private PreparedStatement create_credit_card;
    private PreparedStatement create_debit_card;
    private PreparedStatement deserialize_credit;
    private PreparedStatement deserialize_debit;
    private PreparedStatement create_checking_account;
    private PreparedStatement create_savings_account;
    private PreparedStatement create_new_user;
    private PreparedStatement update_user;
    private PreparedStatement replace_credit_card;
    private PreparedStatement replace_debit_card;

    CustomerOperations(final Connection c) {
        super(c);
    }

    /**
     * do a selection over every user in the database.
     * @return
     */
    public HashMap<String, User> list_all_users() {
        /* get all users for use in a promptmap */
        try {
            HashMap<String, User> m = new HashMap<>();
            ResultSet result;
            all_users = con.prepareStatement("SELECT * FROM customer ORDER BY customer_id");
            result = all_users.executeQuery();

            while(result.next()) {
                String fname = result.getString("first_name");
                String lname = result.getString("last_name");
                String full_name = result.getString("name");
                // String d_name = fname +" "+lname;
                java.util.Date dob = result.getDate("dob");
                int c_id = result.getInt("customer_id");
                String email = result.getString("email");
                String addy  = result.getString("address");
                java.util.Date creaDate = result.getDate("creation_date");
                User temp = new User(c_id, fname, lname, dob, email, full_name, creaDate, addy);
                m.put(temp.toString(), temp);
            }
            return m;
        } catch(Exception e) {
            Helper.notify("error", "An error occurred while selecting all users. Due to the severity, the program will now exit.", true);
            return null;
        }
    }

    /**
     * Do a lookup using a partial name to find a customer.
     * @param name
     * @return
     */
    public HashMap<String, User> list_all_users(final String name) {
        /* get all users for use in a promptmap */
        try {
            HashMap<String, User> m = new HashMap<>();
            ResultSet result;
            all_users_partial = con.prepareStatement("SELECT * FROM customer WHERE UPPER(name) LIKE UPPER(?) ORDER BY customer_id");
            all_users_partial.setString(1, "%"+name+"%");
            result = all_users_partial.executeQuery();

            while(result.next()) {
                String fname = result.getString("first_name");
                String lname = result.getString("last_name");
                String full_name = result.getString("name");
                // String d_name = fname +" "+lname;
                java.util.Date dob = result.getDate("dob");
                int c_id = result.getInt("customer_id");
                String email = result.getString("email");
                String addy  = result.getString("address");
                java.util.Date creaDate = result.getDate("creation_date");
                User temp = new User(c_id, fname, lname, dob, email, full_name, creaDate, addy);
                m.put(temp.toString(), temp);
            }
            return m;
        } catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("error", "An error occurred while selecting all users. Due to the severity, the program will now exit.", true);
            return null;
        }
    }
    
    /**
     * List all the accounts for a customer. Returns an Arraylist of Accounts
     * @param customer
     * @return
     */
    public ArrayList<Account> account_details_for_user(final User customer) {
        /* list account metadata */
        try {
            ArrayList<Account> accounts = new ArrayList<>();

            ResultSet result;

            account_details = con.prepareStatement("SELECT acct_id, balance, interest, creation_date, customer_id, add_date, nvl(min_balance, -1) as min_balance FROM ACCOUNT NATURAL JOIN customer_accounts NATURAL LEFT OUTER JOIN CHECKING_ACCOUNT WHERE customer_id = ?");
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

    /**
     * Get the number of accounts of a customer.
     * @param customer
     * @return
     */
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

    /**
     * Get the number of checking accounts for a user.
     * @param customer
     * @return
     */
    public int num_checking_accounts(final User customer) {
        /* get the number of checking accounts held by a user */
        try {
            ResultSet result;
            int customer_id = customer.customer_id;
            // System.out.println(customer_id);
            num_checking_acct = con.prepareStatement("SELECT num_checking_accounts(?) as c from dual");
            num_checking_acct.setInt(1, customer_id);
            result = num_checking_acct.executeQuery();
            result.next();
            return result.getInt("c");
        } catch(Exception e) {
            // System.out.println(e.getMessage());
            Helper.notify("warn", "\nUnable to retrieve count of user checking accounts. This feature will be unavailable.\n", true);
            return -1;
        }

    }
    
    /**
     * Get the number of loans associated with a user. TODO
     * @param customer
     * @return
     */
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

    /**
     * Get number of debit cards associated with a customer.
     * @param customer
     * @return
     */
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

    /**
     * Get the number of credit cards associated with a customer.
     * @param customer
     * @return
     */
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

    /**
     * Get total number of cards associated with a customer. 
     * @param customer
     * @return
     */
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
            e.printStackTrace();
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
    public boolean do_withdrawal(double amount, final int loc_id, final int acct_id, final int cust_id) {
        amount *= -1;
        try {
            ResultSet result;
            num_cards = con.prepareStatement("SELECT do_account_action(?, ?, ?, ?) as c from dual");
            num_cards.setDouble(1, amount);
            num_cards.setInt(2, loc_id);
            num_cards.setInt(3, acct_id);
            num_cards.setInt(4, cust_id);
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
    final public boolean do_deposit(final double amount, final int loc_id, final int acct_id, final int cust_id) {
        
        try {
            ResultSet result;
            num_cards = con.prepareStatement("SELECT do_account_action(?, ?, ?, ?) as c from dual");
            num_cards.setDouble(1, amount);
            num_cards.setInt(2, loc_id);
            num_cards.setInt(3, acct_id);
            num_cards.setInt(4, cust_id);
            result = num_cards.executeQuery();
            result.next();
            if(result.getInt("c") < 0 ) return false; else return true; 
        } catch(Exception e) {
            Helper.notify("warn", "\nUnable to do an account deposit.\n", true);
            return false;
        }
    }

    /**
     * Return an arraylist of all the customer's debit cards.
     * @param c
     * @return
     */
    public ArrayList<Debit> user_debit_cards(final User c) {
        //since we did the check previously, assume the user has a debit card.
        ArrayList<Debit> accumulator = new ArrayList<>();
        try {
            ResultSet result;
            get_debit = con.prepareStatement("SELECT customer_id, to_char(pin) as p, to_char(card_id) as c_id, to_char(cvc) as c, card_number, acct_id FROM DEBIT_CARD NATURAL JOIN CARD NATURAL JOIN CUSTOMER_CARDS NATURAL JOIN CARD_ACCOUNT WHERE customer_id = ?");
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
            // e.printStackTrace();
            Helper.notify("warn", "\nUnable to return debit card data.\n", true);
            return null;
        }
    }


    /**
     * Return an arraylist of the customer's associated credit cards.
     * @param c
     * @return
     */
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
            // e.printStackTrace();
            Helper.notify("warn", "\nUnable to return credit card data.\n", true);
            return null;
        }
    }

    /**
     * Create a new credit card and perform an inplace modification of the stub card object passed.
     * @param card
     * @return
     */
    public Credit create_credit_card(final Credit card) {
        /* SQL Steps:
         * Insert a new row into CARD
         * Insert a new row into Credit_Card
         * Link them in customer_cards
         * Good2GO
         */

        try {
            //https://sqljana.wordpress.com/2017/01/22/oracle-return-select-statement-results-like-sql-server-sps-using-pipelined-functions/
            //TODO
            ResultSet result;
            create_credit_card = con.prepareStatement("SELECT create_credit_card(?,?,?,?,?,?) as c from dual");
            create_credit_card.setString(1, card.card_number);
            create_credit_card.setInt(2, card.get_cvc());
            create_credit_card.setDouble(3, card.interest);
            create_credit_card.setDouble(4, card.running_balance);
            create_credit_card.setDouble(5, card.credit_limit);
            create_credit_card.setInt(6, card.customer_id);
            result = create_credit_card.executeQuery();
            result.next();
            int card_id = result.getInt("c");
            return deserialize_credit(card_id);
        } catch(SQLDataException precision) {
            Helper.notify("warn", "\nAn error occurred while using your provided inputs. Please try something else.\n", true);
            return null;
        } catch(SQLIntegrityConstraintViolationException integrity) {
            Helper.notify("warn", "\nYour entered amounts are incorrect. Please try new values.\n", true);
            return null;
        } catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("warn", "\nUnable to create a new Credit Card. Please try again.\n", true);
            return null;
        }
        
        // return null;
    }

    /**
     * Create a new credit card and perform an inplace modification of the stub card object passed.
     * @param card
     * @return
     */
    public Debit create_debit_card(final Debit card) {
        /* SQL Steps:
         * Insert a new row into CARD
         * Insert a new row into Debit_Card
         * Link in customer cards
         */ 
        try {
            //https://sqljana.wordpress.com/2017/01/22/oracle-return-select-statement-results-like-sql-server-sps-using-pipelined-functions/
            //TODO
            ResultSet result;
            create_debit_card = con.prepareStatement("SELECT create_debit_card(?,?,?,?,?) as d from dual");
            create_debit_card.setString(1, card.card_number);
            create_debit_card.setInt(2, card.get_cvc());
            create_debit_card.setInt(3, card.get_pin());
            create_debit_card.setDouble(4, card.acct_id);
            create_debit_card.setInt(5, card.customer_id);
            result = create_debit_card.executeQuery();
            result.next();
            //Ideally, I would eventually like to pass the result object to a static helper method which could handle the deserialization.
            int card_id = result.getInt("d");
            return deserialize_debit(card_id);
        } catch(SQLIntegrityConstraintViolationException integrity) {
            Helper.notify("warn", "\nYour entered amounts are incorrect. Please try new values.\n", true);
            return null;
        } catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("warn", "\nUnable to create a new Debit Card. Please try again.\n", true);
            return null;
        }
        
        // return null;
    }

    /**
     * Change the cvc, card number, and PIN of a debit card. Does not modify previous transactions.
     * @param cvc
     * @param num
     * @param pin
     * @param card_id
     * @return
     */
    public boolean replace_debit_card(final String cvc, final String num, final String pin, final int card_id) {

        ResultSet result;
        try {
            replace_debit_card = con.prepareStatement("SELECT replace_debit_card(?,?,?,?) as d from dual");
            replace_debit_card.setString(1,num);
            replace_debit_card.setString(2,cvc);
            replace_debit_card.setString(3,pin);
            replace_debit_card.setInt(4,card_id);
            result = replace_debit_card.executeQuery();
            result.next();
            if(!result.getString("d").equals(num)) {
                throw new Exception();
            }
        }catch(Exception e) {
            Helper.notify("error", "Failed Debit Card replacement. Your original card is unaltered.", true);
            return false;
        }
        return true;
    }

    /**
     * Change a card's number and cvc. Does not modify any previous purchases.
     * @param cvc
     * @param num
     * @param card_id
     * @return
     */
    public boolean replace_credit_card(final String cvc, final String num, final int card_id) {
        ResultSet result;
        try {
            // System.out.println("Old: "+num);
            replace_credit_card = con.prepareStatement("SELECT replace_credit_card(?,?,?) as d from dual");
            replace_credit_card.setString(1,num);
            replace_credit_card.setString(2,cvc);
            replace_credit_card.setInt(3,card_id);
            result = replace_credit_card.executeQuery();
            result.next();
            // System.out.println("New: "+result.getString("d"));
            // System.out.println("Old: "+num);
            if(!result.getString("d").equals(num)) {
                throw new Exception();
            }
        }catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("error", "Failed Credit Card replacement. Your original card is unaltered.", true);
            return false;
        }
        return true;
    }

    /**
     * Deserialize our Card tuple into a Java Object.
     * @param card_id
     * @return
     */
    public Debit deserialize_debit(final int card_id) {
        try {
            ResultSet result;
            deserialize_debit = con.prepareStatement("SELECT customer_id, to_char(pin) as p, to_char(cvc) as c, card_number, acct_id FROM DEBIT_CARD NATURAL JOIN CARD NATURAL JOIN CUSTOMER_CARDS NATURAL JOIN CARD_ACCOUNT WHERE card_id = ?");
            deserialize_debit.setInt(1, card_id);
            result = deserialize_debit.executeQuery();
            if(result.next()) {
                String pin = result.getString("p");
                String cvc = result.getString("c");
                String card_num = result.getString("card_number");
                int acct_id = result.getInt("acct_id");
                int cust_id = result.getInt("customer_id");
                
                return new Debit(Integer.toString(card_id), cvc, card_num, pin, acct_id, cust_id);
            }
        }catch(Exception e) {
            // e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Return a Credit stub object to be used for checking success.
     * @param card_id
     * @return
     */
    public Credit deserialize_credit(final int card_id) {
        try {
            ResultSet result;
            deserialize_credit = con.prepareStatement("SELECT customer_id, credit_limit, interest, to_char(cvc) as cvc, card_number, balance_due, running_balance FROM CREDIT_CARD NATURAL JOIN CARD NATURAL JOIN CUSTOMER_CARDS WHERE card_id = ?");
            deserialize_credit.setInt(1, card_id);
            result = deserialize_credit.executeQuery();

            if(result.next()) return new Credit();
        } catch(Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Create a checking account by passing in an Account object.
     * @param a
     * @return
     */
    public boolean create_checking_account(final Account a) {
        ResultSet result;
        try {
            create_checking_account = con.prepareStatement("SELECT create_checking_account(?,?,?,?) as c from dual");
            create_checking_account.setDouble(1, a.balance);
            create_checking_account.setDouble(2, a.interest);
            create_checking_account.setDouble(3, a.min_balance);
            create_checking_account.setInt(4, a.customer_id);
            result = create_checking_account.executeQuery();
            // result.next();
            //Ideally, I would eventually like to pass the result object to a static helper method which could handle the deserialization.
            return true;
        } catch(SQLDataException precision) {
            Helper.notify("warn", "\nAn error occurred while using your provided inputs. Please try something else.\n", true);
            return false;
        } catch(SQLIntegrityConstraintViolationException integrity) {
            Helper.notify("warn", "\nA duplicate card number was encountered. Unlikely, but it did.\n", true);
            return false;
        } catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("warn", "\nUnable to create a new Credit Card. Please try again.\n", true);
            return false;
        }
    }


    /**
     * Create a savings account by passing in an Account object.
     * @param a
     * @return
     */
    public boolean create_savings_account(final Account a) {
        ResultSet result;
        try {
            create_checking_account = con.prepareStatement("SELECT create_savings_account(?,?,?) as s from dual");
            create_checking_account.setDouble(1, a.balance);
            create_checking_account.setDouble(2, a.interest);
            create_checking_account.setInt(3, a.customer_id);
            result = create_checking_account.executeQuery();
            // result.next();
            //Ideally, I would eventually like to pass the result object to a static helper method which could handle the deserialization.
            return true;
        } catch(SQLDataException precision) {
            Helper.notify("warn", "\nAn error occurred while using your provided inputs. Please try something else.\n", true);
            return false;
        } catch(SQLIntegrityConstraintViolationException integrity) {
            Helper.notify("warn", "\nA duplicate card number was encountered. Unlikely, but it did.\n", true);
            return false;
        } catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("warn", "\nUnable to create a new Credit Card. Please try again.\n", true);
            return false;
        }
    }

    /**
     * Create a new customer by passing in a User object.
     * @param c
     * @return
     */
    public User create_new_user(User c) {
        ResultSet result;
        try {
            create_new_user = con.prepareStatement("SELECT create_new_customer(?,?,?,?,?) as n from dual");
            create_new_user.setString(1, c.first_name);
            create_new_user.setString(2, c.last_name);
            create_new_user.setDate(3, new java.sql.Date(c.dob.getTime()));
            create_new_user.setString(4, c.email);
            create_new_user.setString(5, c.address);
            result = create_new_user.executeQuery();
            result.next();
            c.customer_id = result.getInt("n");
            return c;
        }catch(SQLIntegrityConstraintViolationException i) {
            Helper.notify("error", "Duplicate customer ID detected. Please try again later.", true);
            return null;
        }catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("warn", "Failed creating a new user account. Please try again.", true);
            return null;
        }
    }

    /**
     * Perform a customer update, by passing in a modified User object.
     * @param c
     * @return
     */
    public User update_user(User c) {
        ResultSet result;
        try {
            update_user = con.prepareStatement("SELECT update_customer(?,?,?,?, ?) as n from dual");
            update_user.setInt(1, c.customer_id);
            update_user.setString(2, c.first_name);
            update_user.setString(3, c.last_name);
            update_user.setString(4, c.email);
            update_user.setString(5, c.address);
            result = update_user.executeQuery();
        }catch(SQLIntegrityConstraintViolationException i) {
            Helper.notify("error", "Duplicate customer ID detected. Please try again later.", true);
            return null;
        }catch(Exception e) {
            // e.printStackTrace();
            Helper.notify("warn", "Failed updating an account. Please try again.", true);
            return null;
        }
        return null;
    }

}