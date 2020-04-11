import java.sql.*;
import java.util.*;
public class GenOperations extends DatabaseOperations {
    private PreparedStatement all_atms;
    private PreparedStatement all_branches;
    public HashMap<String, Location> locations;
    public HashMap<String, ATM> atms;
    public HashMap<String, Branch> branches;
    public HashMap<Integer, String> locations_prompt;
    public HashMap<Integer, String> atms_prompt;
    public HashMap<Integer, String> branches_prompt;
    private Random rand;
    GenOperations(final Connection c) throws SQLException {
        super(c);
        locations = new HashMap<>();
        atms = new HashMap<>();
        branches = new HashMap<>();
        locations_prompt = new HashMap<>();
        atms_prompt = new HashMap<>();
        branches_prompt = new HashMap<>();
        this.list_locations();
        rand = new Random();
    }

    public final String compute_random_num_str(final int length) {
        String ret = "";
        for(int i = 0; i < length; i++) {
            ret += Integer.toString(this.rand.nextInt(10));
        }

        return ret;
    }

    public final String compute_card_num() {
        return compute_random_num_str(15);
    }

    public final String compute_cvc() {
        return compute_random_num_str(3);
    }
    public final String compute_pin() {
        return compute_random_num_str(4);
    }

    public void list_locations() throws SQLException{
        /* get all the locations, partition them, and coerce into a promptmap */
        int total = 1;
        int local = 1;
        ResultSet result = null; //use Java pseudo pointers to save on typing.
        ResultSet branch_loc;
        ResultSet atm_loc;
        /* execute all queries and store result */
        all_branches = con.prepareStatement("SELECT * from location NATURAL JOIN branch");
        branch_loc = all_branches.executeQuery();

        all_atms = con.prepareStatement("SELECT * from LOCATION natural join ATM_locations NATURAL JOIN atm");
        atm_loc =  all_atms.executeQuery();


        /* do data coercion */
        result = atm_loc;
        while(atm_loc.next()) {
            int atm_id = result.getInt("atm_id");
            int location_id = result.getInt("location_id");
            String city = result.getString("city");
            String state = result.getString("state");
            String zip = Integer.toString(result.getInt("zip"));
            String street = result.getString("zip");
            String street_num = Integer.toString(result.getInt("street_num"));
            String operator = result.getString("operator_name");
            String address = result.getString("address");
            String hours = result.getString("hours_of_operation");
            ATM temp = new ATM(location_id, city, state, zip, street, street_num, address, atm_id, operator, hours);
            String key = temp.toString();
            locations_prompt.put(total++, key);
            locations.put(key, temp);
            atms_prompt.put(local++, key);
            atms.put(key, temp);
        }

        result = branch_loc;
        local = 1;
        while(result.next()) {
            int branch_id = result.getInt("branch_id");
            int location_id = result.getInt("location_id");
            String city = result.getString("city");
            String state = result.getString("state");
            String zip = Integer.toString(result.getInt("zip"));
            String street = result.getString("zip");
            String street_num = Integer.toString(result.getInt("street_num"));
            String address = result.getString("address");
            String hours = result.getString("open") + " "+result.getString("close");
            Branch temp = new Branch(location_id, city, state, zip, street, street_num, address, branch_id, hours);
            String key = temp.toString();
            locations_prompt.put(total++, key);
            locations.put(key, temp);
            branches_prompt.put(local++, key);
            branches.put(key, temp);
        }
    }
}