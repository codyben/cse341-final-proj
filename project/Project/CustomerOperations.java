import java.sql.*;
import java.util.*;
class CustomerOperations extends DatabaseOperations {

    private PreparedStatement all_users;

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
}