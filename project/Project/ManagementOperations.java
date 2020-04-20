import java.sql.*;
import java.util.*;
class ManagementOperations extends DatabaseOperations {
    private PreparedStatement create_new_user;
    ManagementOperations(Connection c) {
        super(c);
    }
    

    // public User create_new_user(User c) {
    //     ResultSet result;
    //     try {
    //         create_new_user = con.prepareStatement("SELECT create_new_customer(?,?,?,?) as n from dual");
    //         create_new_user.setString(1, c.first_name);
    //         create_new_user.setString(2, c.last_name);
    //         create_new_user.setDate(3, new java.sql.Date(c.dob.getTime()));
    //         create_new_user.setString(4, c.email);
    //         result = create_new_user.executeQuery();
    //         result.next();
    //         c.customer_id = result.getInt("n");
    //         return c;
    //     }catch(SQLIntegrityConstraintViolationException i) {
    //         return null;
    //     }catch(Exception e) {
    //         return null;
    //     }
    //     return null;
    // }
}