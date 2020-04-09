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

	public String intent(final User c) {
		c.compute();
		c.format_data();
		String interface2 = "Account Deposit / Withdrawal.";
		String interface3Alpha = "Loan Payment.";
		String interface3Beta = "Credit Card Payment.";
		// String interface4 = "Open a new account.";
		// String interface5 = "Obtain a new / replacement credit card";
		// String interface6 = "Take out a new loan";
		String interface7Alpha = "Make a purchase with your cards";
		String interface7Beta = "View activity on your cards";
		String quit = "Return to previous.";
		HashMap<Integer, String> paths = new HashMap<>();
		int i = 1;
		if(c.num_accounts > 0 ) {
			paths.put(i++, interface2);
		} else if(c.num_credit > 0) {
			paths.put(i++, interface3Beta);
		} else if(c.num_loans > 0 ) {
			paths.put(i++, interface3Alpha);
		} else if(c.total_cards > 0 ) {
			paths.put(i++, interface7Beta);
			paths.put(i++, interface7Alpha);
		} else {
			Helper.notify("warn", "\nYour account does not appear to have any accounts,/loans/cards associated with it. Please choose an option from below.\n", true);

		}
		// paths.put(i++, interface4);
		// paths.put(i++, interface5);
		// paths.put(i++, interface6);
		paths.put(i++, quit);
		

		String choice = Helper.get_choice(paths, Helper.notify_str("heading", "\nSelect an action to perform on the account.\n", true));
		
		if(choice.equals(interface2)) {

		} else if(choice.equals(interface3Alpha)) {
			
		} else if(choice.equals(interface3Beta)) {

		} else if(choice.equals(interface7Alpha)) {

		} else if(choice.equals(interface7Beta)) {

		} else {
			
		}
	}

	public Account list_accounts(User customer) {
		ArrayList<Account> accounts = ops.account_details_for_user(customer);

		if(accounts.size() == 0) {
			Helper.notify("warn", customer.full_name+" has no accounts.",true);
		}
		return null;
	}
}
