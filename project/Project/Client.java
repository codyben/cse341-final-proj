import java.sql.*;  
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class Client extends ProjectInterface {
	CustomerOperations ops;
	private Location loc;
	Client(String n, String e) {
		super(n,e);
	}

	public void provision(CustomerOperations o) {
		ops = o;
	}

	public void set_location(Location l) {
		loc = l;
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
		String quit = "Restart client interface...";
		paths.put(1, cust_id);
		paths.put(2, cust_name);
		paths.put(3, all);
		paths.put(4, quit);

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

		} else if(result.equals(quit)) {
			return null;
		}

		return customer;	
	}
	/**
	 * Force the user to select a location where they wish to carry out actions.
	 * @param c
	 * @return
	 */
	public static Location force_location() throws UnrecoverableException {
		HashMap<Integer, String> paths = new HashMap<>();
		paths.put(1, "ATM");
		paths.put(2, "Branch");
		paths.put(3, "Select new interface.");
		String choice = Helper.get_choice(paths, Helper.notify_str("heading", "\nA location is required. Choose a type from below.\n", false));
		GenOperations data_container = Helper.compute_general(); //only compute this if the user has made a choice.
		String choice_key = "Quit";
		String header_str = Helper.notify_str("heading", "\nChoose a location.\n", true);

		if(choice.equals("ATM")) {
			choice_key = Helper.get_choice(data_container.atms_prompt, header_str);
		} else if(choice.equals("Branch")) {
			choice_key = Helper.get_choice(data_container.branches_prompt, header_str);
		} else {
			return null;
		}

		return data_container.locations.get(choice_key);
	}

	public boolean intent(final User c) {
		boolean cont = true;
		do {
			c.compute();
			c.format_data();
			String interface2a = "Account Deposit";
			String interface2b = "Account withdrawal";
			String interface3Alpha = "Loan Payment.";
			String interface3Beta = "Credit Card Payment.";
			// String interface4 = "Open a new account.";
			// String interface5 = "Obtain a new / replacement credit card";
			// String interface6 = "Take out a new loan";
			String interface7Alpha = "Make a purchase with your cards";
			String interface7Beta = "View activity on your cards";
			String interface8 = "View account summary.";
			String quit = "Return to previous.";
			HashMap<Integer, String> paths = new HashMap<>();
			int i = 1;
			HashMap<Integer, String> accounts = new HashMap<>();
			if(c.num_accounts > 0 ) {
				accounts = c.get_accounts();
				paths.put(i++, interface2a);
				paths.put(i++, interface2b);
				// paths.put(i++, interface8);
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
			boolean isOK = false;
			do {
				if(choice.equals(interface2a) || choice.equals(interface2b)) {
					String acct_key = Helper.get_choice(accounts, Helper.notify_str("heading", "\nPlease choose an account.\n", true));
					Account acct = c.accounts.get(acct_key);
					this.display_account_metadata(acct);
					double action_amt = 0;
					
					if(choice.equals(interface2b)) {
						action_amt = this.prompt_withdrawal(acct.balance, acct.min_balance);
						isOK = Helper.confirm("Is the amount: "+Double.toString(action_amt)+"$ ok?");
						if(isOK)
							ops.do_withdrawal((-1)*action_amt, this.loc.location_id, acct.acct_id);
	
					} else {
						action_amt = this.prompt_deposit(acct.balance);
						isOK = Helper.confirm("Is the amount: "+Double.toString(action_amt)+"$ ok?");
						if(isOK)
							ops.do_deposit(action_amt, this.loc.location_id, acct.acct_id);
					}
					c.get_accounts();
					double old_bal = acct.balance;
					Account new_acc = c.accounts.get(acct_key);
					double new_bal = new_acc.balance;
					double delta = new_bal - old_bal;
					this.display_account_metadata(new_acc);
					if(delta <= 0) {
						System.out.println("Balance change: "+Helper.notify_str("yellow",delta,false)+"$");
					} else {
						System.out.println("Balance change: +"+Helper.notify_str("green",delta,false)+"$");
					}
					System.out.println("\n-------------------------------------------------------\n");
				} else if(choice.equals(interface3Alpha)) {
					
				} else if(choice.equals(interface3Beta)) {
	
				} else if(choice.equals(interface7Alpha)) {
	
				} else if(choice.equals(interface7Beta)) {
	
				} else if(choice.equals(quit)) {
					cont = false;
				}
				break;
			}while(!isOK);
			
		} while(cont);
		
		return true;
	}

	public Account list_accounts(User customer) {
		ArrayList<Account> accounts = ops.account_details_for_user(customer);

		if(accounts.size() == 0) {
			Helper.notify("warn", customer.full_name+" has no accounts.",true);
		}
		return null;
	}

	private double prompt_withdrawal(double curr_bal, double min_balance) {
		if(min_balance == -1) {
			min_balance = 0;
		}
		boolean correct = false;
		do{
			String msg = Helper.notify_str("heading", "Please enter an amount to withdraw from your account: ", false);
			double amt = Helper.get_double(msg);
			if(amt <= 0) {
				Helper.notify("warn", "You cannot enter a negative/zero amount.", true);
			} else if(amt > curr_bal-min_balance) {
				Helper.notify("warn", "You cannot withdraw more than your account balance, including the minimum balance.", true);
			} else return amt;
		}while(!correct);
		return 0;
	}

	private double prompt_deposit(double curr_bal) {
		boolean correct = false;
		do{
			String msg = Helper.notify_str("heading", "Please enter an amount to deposit into your account: ", false);
			double amt = Helper.get_double(msg);
			if(amt <= 0) {
				Helper.notify("warn", "You cannot enter a negative/zero amount.", true);
			} else return amt;
		}while(!correct);
		return 0;
	}

	private void display_account_metadata(final Account a) {
		DateFormat date_fmt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String cdate = date_fmt.format(a.creation_date);
		String adate = date_fmt.format(a.added_date);
		Helper.notify("heading", "Your account metadata, provided for your convenience.", true);
		System.out.println("+Identifier:\t"+a.toString());
		System.out.println("+Creation date: "+cdate);
		System.out.println("+Add date: "+adate);
		System.out.println("+Current interest rate:\t "+ Double.toString(a.interest)+"%");
		System.out.println("+Current balance:\t "+ Double.toString(a.balance)+"$");
		if(a.min_balance != -1) {
			System.out.println("+Minimum balance:\t "+Double.toString(a.min_balance)+"$");
		}
	}

}
