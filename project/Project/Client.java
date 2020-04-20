import java.sql.*;  
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

class Client extends ProjectInterface {
	CustomerOperations ops;
	GenOperations gops;
	private Location loc;
	Client(String n, String e) {
		super(n,e);
	}

	public void provision(CustomerOperations o, GenOperations g) {
		ops = o;
		gops = g;
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
	/**
	 * Have the user pick how they want to go about using the interface.
	 * @return
	 */
	public User divergent_paths() {
		int user_count = gops.get_user_count();
		int pmap = 1;
		HashMap<Integer, String> paths = new HashMap<>();
		HashMap<String, User> query_result = new HashMap<>();
		HashMap<Integer, String> get_user = new HashMap<>();
		String creat_cust = "Create new customer. (TODO)";
		String cust_id = "Select customer by ID. (TODO)";
		String cust_name = "Select customer by name. (TODO)";
		String all = "Show all customers.";
		String quit = "Restart client interface...";
		if(user_count <= 0) {
			paths.put(pmap++, creat_cust);
		} else {
			paths.put(pmap++, creat_cust);
			paths.put(pmap++, cust_id);
			paths.put(pmap++, cust_name);
			paths.put(pmap++, all);
			paths.put(pmap++, quit);
		}


		String result = Helper.get_choice(paths, null);
		User customer = null; 

		if(result.equals(cust_id)) {
			int cust_search_id = Helper.get_int("Please enter the customer ID.");
			return customer;
		} else if(result.equals(cust_name)) {
			String cust_search_name = Helper.get_string("Please enter the customer name.");
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
		} else if(result.equals(creat_cust)) {
			String customer_first_name = Helper.get_string("Enter the customer's first name: ");
			String customer_last_name = Helper.get_string("Enter the customer's last name: ");
			String customer_email = Helper.get_email("Enter the customer's email: ");
			String format = "MM/dd/yyyy";
			java.util.Date customer_DOB = Helper.get_date("Enter a date in the form ("+format+"): ", format);
			User new_user = new User(customer_first_name, customer_last_name, customer_DOB, customer_email);
			new_user = new_user.commit();
			if(new_user == null) {
				return null; //shoot user back to prompt.
			}

			if(new_user != null) {
				Helper.notify("green", "\n++Created new user: "+new_user+"\n", true);
				boolean do_we_continue_with_new_user = Helper.confirm("Would you like to continue with the newly created user?");
				if(do_we_continue_with_new_user) {
					return new_user; //proceed with new user.
				} else {
					return null; //shoot back to prompt.
				}
			}
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
		paths.put(3, "Select new interface (quit option available).");
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

	public boolean intent(final User c) throws UnrecoverableException{
		boolean cont = true;
		boolean isAtBranch = false;
		if(this.loc instanceof Branch) {
			isAtBranch = true; //keep this here so we can do some filtering.
		}
		do {
			c.compute();
			c.format_data();
			// final String interface1 = "Refresh account details.";
			final String interface2a = "Account Deposit.";
			final String interface2b = "Account Withdrawal.";
			final String interface3Alpha = "Loan Payment.";
			final String interface3Beta = "Credit Card Payment. (TODO)";
			final String interface4 = "Open a new account.";
			// final String interface5 = "Obtain a new / replacement credit card";
			// final String interface6 = "Take out a new loan";
			final String interface7Alpha = "Make a purchase with your cards.";
			final String interface7Beta = "View activity on your cards.";
			final String interface5a = "Obtain a replacement card. (TODO)";
			final String interface5b = "Request a card.";
			final String interface8 = "View account summary.";
			final String interface8b = "View card summary.";
			final String quit = "Return to previous.";
			final String edit = "View/edit my details.";
			HashMap<Integer, String> paths = new HashMap<>();
			int i = 1;
			HashMap<Integer, String> accounts = new HashMap<>();
			paths.put(i++, edit); //always allow a user to see/edit their details.
			paths.put(i++, interface5b); //always allow a user to request a card.
			paths.put(i++, interface4); //always allow a user to open a new account.
			
			/* Dynamically create a menu based on the user's current data */
			boolean acc_ops = c.num_accounts > 0;
			boolean cred_ops = c.num_credit > 0;
			boolean loan_ops = c.num_credit > 0;
			boolean card_ops = c.total_cards > 0;
			if(acc_ops) {
				accounts = c.get_accounts();
				paths.put(i++, interface2a);
				paths.put(i++, interface2b);
				paths.put(i++, interface8);
				
			}  
			
			if(cred_ops) {
				paths.put(i++, interface3Beta);
			} 
			
			if(loan_ops) {
				paths.put(i++, interface3Alpha);
			} 
			
			if(card_ops) {
				paths.put(i++, interface7Beta);
				paths.put(i++, interface7Alpha);
				paths.put(i++, interface8b);
				paths.put(i++, interface5a);
			}  
			
			if (!acc_ops && !card_ops && !cred_ops && !loan_ops){
				Helper.notify("warn", "\nYour account does not appear to have any accounts/loans/cards associated with it. Please choose an option from below.\n", true);
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
					boolean rejection = false; //use this to modify the control flow below so the user sees the rejection.
					if(choice.equals(interface2b)) {
						action_amt = this.prompt_withdrawal(acct.balance, acct.min_balance);
						if(action_amt <= 0) {
							action_amt *= -1; //do the flip to make it a withdrawal.
							isOK = true; //short circuit confirmation for a penalty.
							rejection = true;
						} else {
							isOK = Helper.confirm("Is the amount: "+Double.toString(action_amt)+"$ ok?");
						}
						if(isOK)
							ops.do_withdrawal(action_amt, this.loc.location_id, acct.acct_id);
	
					} else {
						action_amt = this.prompt_deposit(acct.balance);
						isOK = Helper.confirm("Is the amount: "+Double.toString(action_amt)+"$ ok?");
						if(isOK)
							ops.do_deposit(action_amt, this.loc.location_id, acct.acct_id);
					}
					if(!rejection) {
						c.get_accounts();
						double old_bal = acct.balance;
						Account new_acc = c.accounts.get(acct_key);
						double new_bal = new_acc.balance;
						double delta = new_bal - old_bal;
						DecimalFormat df = new DecimalFormat("#.0#");
						// handle rounding:
						// https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
						delta = Double.valueOf(df.format(delta));
						this.display_account_metadata(new_acc);
						String succ_str = Helper.notify_str("green", "\nDeposit Successful!\n", true);
						if(delta <= 0) {
							System.out.println("Balance change: "+Helper.notify_str("yellow",delta,false)+"$");
							succ_str = Helper.notify_str("green", "\nWithdrawal Successful!\n", true);
						} else {
							System.out.println("Balance change: +"+Helper.notify_str("green",delta,false)+"$");
						}
						System.out.println("\n-------------------------------------------------------\n"+succ_str);
					}

				} else if(choice.equals(interface3Alpha)) {
					
				} else if(choice.equals(interface3Beta)) {
	
				} else if(choice.equals(interface7Alpha)) {
					String card_choice = Helper.get_choice(c.card_promptmap(), Helper.notify_str("heading", "Please choose a card from your accout below.", true));
					Card temp_pay = c.user_cards.get(card_choice);
					boolean purchase_result = temp_pay.prompt_and_confirm_purchase(); //true for a success, false for an error/wishing to exit. Messages handled for success/error.
					if(purchase_result) {
						c.compute();
						System.out.println("\n-------------------------------------------------------\n");
						c.account_metadata();
					}
				} else if(choice.equals(interface7Beta)) {

					String card_choice = Helper.get_choice(c.card_promptmap(), Helper.notify_str("heading", "Please choose a card from your accout below.", true));
					Card temp_view = c.user_cards.get(card_choice);
					temp_view.activity();


				} else if(choice.equals(quit)) {
					cont = false;
				} else if(choice.equals(interface8)) {
					c.compute(); //recompute user data.
					c.user_metadata();
					c.account_metadata();
				} else if(choice.equals(interface8b)) {
					c.compute();
					c.card_metadata();
				} else if(choice.equals(interface5a)) {
					//obtain a replacement card.
				} else if(choice.equals(interface5b)) {
					c.request_card(ops);
				} else if(choice.equals(interface4)) {
					c.create_new_account();
				} else if(choice.equals(edit)) {
					c.prompt_new_details();
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
				
				if(curr_bal >= 10.0) {
					Helper.notify("error", "Your deposit has been rejected, and a 5$ penalty has been imposed.", true);
					return -5.0;
				} else if(min_balance <= curr_bal * 0.1){
					Helper.notify("error", "Your deposit has been rejected, a penalty of "+Double.toString(curr_bal * 0.1)+"$ has been imposed.", true);
					return curr_bal * -0.1;
				}
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
