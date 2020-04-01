import java.sql.*;
import java.util.*;
class CustomerOperations extends DatabaseOperations {

    private PreparedStatement all_users;
    private PreparedStatement account_details;

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
                m.put(temp.full_name, temp);
            }
            return m;
        } catch(Exception e) {
            Helper.notify("error", "An error occurred while selecting all users. Due to the severity, the program will now exit.", true);
            return null;
        }
    }

    public ArrayList<Account> account_details_for_user(User customer) {
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

    public int num_accounts_for_user(User customer) {

    }

    public int num_loans_for_user(User customer) {

    }

    public int num_debit_for_user(User customer) {

    }

    public int num_credit_for_user(User customer) {
        
    }
}