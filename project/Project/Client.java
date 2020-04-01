import java.sql.*;  
import java.util.*;
class Client extends ProjectInterface {
	CustomerOperations ops;
	Client(String n, String e) {
		super(n,e);
	}

	public void provision(CustomerOperations o) {
		ops = o;
	}

	public void divergent_paths() {
		HashMap<Integer, String> paths = new HashMap<>();
		HashMap<String, User> query_result = new HashMap<>();
		String cust_id = "Select customer by ID.";
		String cust_name = "Select customer by name.";
		String all = "Show all customers.";
		paths.put(1, cust_id);
		paths.put(2, cust_name);
		paths.put(3, all);

		String result = Helper.get_choice(paths, null);

		if(result.equals(cust_id)) {
			
		} else if(result.equals(cust_name)) {

		} else if(result.equals(all)) {
			query_result = ops.list_all_users();
			if(query_result == null) {
				Helper.error_exit();
			}
		}

		
	}
}