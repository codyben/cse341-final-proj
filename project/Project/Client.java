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

	public HashMap<Integer, String> coerce(HashMap<String, User> h) {
		HashMap<Integer, String> temp = new HashMap<>();
		int count = 1;
		for (User customer : h.values()) {
			temp.put(count++, customer.full_name+" (ID="+customer.customer_id+")");
		}
		return temp;
	}
	public User divergent_paths() {
		HashMap<Integer, String> paths = new HashMap<>();
		HashMap<String, User> query_result = new HashMap<>();
		HashMap<Integer, String> get_user = new HashMap<>();
		String cust_id = "Select customer by ID.";
		String cust_name = "Select customer by name.";
		String all = "Show all customers.";
		paths.put(1, cust_id);
		paths.put(2, cust_name);
		paths.put(3, all);

		String result = Helper.get_choice(paths, null);
		User customer = null; 

		if(result.equals(cust_id)) {
			return customer;
		} else if(result.equals(cust_name)) {
			return customer;
		} else if(result.equals(all)) {
			query_result = ops.list_all_users();
			if(query_result == null) {
				Helper.error_exit();
			}

			boolean confirm = true;
			do {

				String choice = Helper.get_choice(this.coerce(query_result), "Select a user from the list below.");
				 customer = query_result.get(choice);
				Helper.notify("notify", "\nYou have selected "+choice+".", true);
				confirm = Helper.confirm("Is this the correct user?");
			}while(!confirm && customer != null);

			return customer;

		}

		return customer;	
	}

	public String intent() {
		Helper.notify("heading", "\nSelect an action to perform on the account.\n", true);
		HashMap<Integer, String> paths = new HashMap<>();
		String interface2 = "Account Deposit / Withdrawal.";
		String interface3Alpha = "Loan Payment.";
		paths.put(1, );
	}

	public Account list_accounts(User customer) {
		ArrayList<Account> accounts = ops.account_details_for_user(customer);

		if(accounts.size() == 0) {
			Helper.notify("warn", customer.full_name+" has no accounts.",n);
		}
	}
}