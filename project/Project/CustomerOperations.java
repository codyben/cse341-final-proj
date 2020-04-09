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

    CustomerOperations(Connection c) {
        super(c);
    }

    public void test() {
        System.out.println("hi");
    }

    public HashMap<String, User> list_all_users() {
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
        try {
            ArrayList<Account> accounts = new ArrayList<>();

            ResultSet result;

            account_details = con.prepareStatement("SELECT * FROM ACCOUNT NATURAL JOIN HOLDS WHERE customer_id = ?");
            account_details.setInt(1, customer.customer_id);
            result = account_details.executeQuery();
            while(result.next()) {
                int acct_id = result.getInt("acct_id");
                double balance = result.getDouble("balance");
                double interest = result.getDouble("interest");
                java.util.Date creation_date = result.getDate("creation_date");
                java.util.Date add_date = result.getDate("add_date");
                Account temp = new Account(balance, interest, creation_date, add_date);
                accounts.add(temp);
            }
            return accounts;

        } catch(Exception e) {
            Helper.notify("error", "\nAn error occurred while checking account details. Please try again.\n", true);
            return null;
        }
    }

    public int num_accounts_for_user(final User customer) {
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
}