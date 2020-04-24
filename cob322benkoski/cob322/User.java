import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
class User {
    public Integer customer_id;
    public String first_name;
    public String last_name;
    public Date dob;
    public Date creation_date;
    public String email;
    public String full_name;
    public String address;
    public int num_accounts = 0;
    public int num_checking = 0;
    public int num_savings = 0;
    public int num_credit = 0;
    public int num_debit = 0;
    public int num_loans = 0;
    public int total_cards = 0;
    public HashMap<String, Account> accounts;
    public ArrayList<Account> user_accounts;
    public ArrayList<Debit> user_debit;
    public ArrayList<Credit> user_credit;
    public HashMap<String, Card> user_cards;
    public final CustomerOperations ops = new CustomerOperations(Helper.con());
    CustomerOperations sync = new CustomerOperations(Helper.con());
    private GenOperations genops = null;
    DecimalFormat df_interest = new DecimalFormat("##.0####");
    DecimalFormat df = new DecimalFormat("#.##");
    DateFormat dfmt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    private final String safe_quit = Helper.notify_str("warn", "\nYou have been returned to the previous screen. No changes have occurred.\n", true);  

    User(int c, String f, String l, Date d, String e, String n, Date creation_date, String addy) throws UnrecoverableException {
        customer_id = c;
        first_name = f;
        last_name = l;
        dob = d;
        full_name = n;
        email = e;
        this.creation_date = creation_date;
        address = addy;
        genops = Helper.compute_general();
        
    }

    User(String f, String l, Date d, String e, String addy) {
        first_name = f;
        last_name = l;
        dob = d;
        full_name = first_name +" "+last_name; //do this for a new account so we're not forced to run a query to get this column.
        email = e; 
        address = addy;
        
    }

    @Override
    public String toString() {
        return full_name + " (ID="+Integer.toString(customer_id)+")";
    }

    /**
     * Allow a user to modify their account details and modify the object in place. Returns a reference to the current object.
     * @return
     */
    public User prompt_new_details() {
        HashMap<Integer, String> promptmap = new HashMap<>();
        Helper.notify("heading", "\nMy current details: \n", true);
        System.out.println("+First Name: "+first_name);
        System.out.println("+Last Name: "+last_name);
        System.out.println("+Email: "+email);
        System.out.println("+Address: "+address);
        if(creation_date != null) //we don't set the creation date of a user originally, so until the user is recomputed, this is null
            System.out.println("+Account creation date: "+dfmt.format(creation_date));

        int i = 1;

        final String correct = "These details are correct. Continue.";
        final String incorrect = "This is incorrect. Let me re-enter information.";
        final String quit = "Quit.";

        promptmap.put(i++, correct);
        promptmap.put(i++, incorrect);
        promptmap.put(i++, quit);

        
        boolean do_edit = Helper.confirm("Do you wish to edit your account details?");

        if(do_edit) {
            String new_first = Helper.get_string_allow("Enter new first name (press enter to keep current): ");
            String new_last = Helper.get_string_allow("Enter new last name (press enter to keep current): ");
            String new_email = Helper.get_email_allow("Enter new email (press enter to keep current): ");
            String new_addy = Helper.get_string_allow("Enter new address (press enter to keep current): ");

            //test if any changes have happened.
            boolean fchange = new_first.equals("");
            boolean lchange = new_last.equals("");
            boolean echange = new_email.equals("");
            boolean addchange = new_addy.equals("");



            if(fchange && lchange && echange && addchange) {
                Helper.notify("warn", "No changes have occured. Returning to previous.", true);
                return this; //no changes so don't do any updates.
            }
            Helper.notify("heading", "\nUpdates: ", true);

            if(!fchange) {
                System.out.print("New first name: ");
                Helper.notify("warn", new_first, true);
            }

            if(!lchange) {
                System.out.print("New last name: ");
                Helper.notify("warn", new_last, true);
            }

            if(!echange) {
                System.out.print("New email: ");
                Helper.notify("warn", new_email, true);
            }

            if(!addchange) {
                System.out.print("New address: ");
                Helper.notify("warn", new_addy, true);
            }

            String choice = Helper.get_choice(promptmap, null);

            if(choice.equals(quit)) {
                return this; //return unchanged User to the uhhm, user.
            } else if(choice.equals(incorrect)) {
                return prompt_new_details(); //give it anotha try.
            }

            //implicit else for continuing onwards.

            this.first_name = fchange ? this.first_name : new_first;
            this.last_name = lchange ? this.last_name : new_last;
            this.email = echange ? this.email : new_email;
            this.full_name = this.first_name+" "+this.last_name;
            this.address = addchange ? this.address : new_addy;
            this.commit();
            Helper.notify("green", "++Successfully upated account information.", true);
            return this;
        }

        return this; //return the unchanged object.
    }

    /**
     * Sync user data back to the application.
     */
    public void compute() {
        num_accounts = sync.num_accounts_for_user(this);
        num_checking = sync.num_checking_accounts(this);
        num_savings = num_accounts - num_checking;
        num_credit = sync.num_credit_for_user(this);
        num_debit = sync.num_debit_for_user(this);
        total_cards = sync.num_cards_for_user(this);
        if(num_credit > 0 ) {
            user_credit = sync.user_credit_cards(this);
        } 
        if(num_debit > 0) {
            System.out.println();
            user_debit = sync.user_debit_cards(this);
        }  
        
        // if (total_cards > 0 ) {}
        // num_loans = sync.num_loans_for_user(this);
        user_accounts = sync.account_details_for_user(this);
    }

    /**
     * Print out a welcome message for the user.
     */
    public void welcome_message() {
        Helper.notify("green", "\t++Welcome "+full_name+"!", true);


    }

    /**
     * Display data on screen about the number of acounts/loans/cards a user currently holds.
     */
    public void format_data() {
        /* GENERALIZE COLUMN PRINTING */
        // String heading = Helper.notify_str("heading",this.toString(), true );
        // String account_header = "Savings Accounts\tChecking Accounts\tTotal Accounts";
        String acc = "Total savings/checking accounts: ";
        String checking = "Number of checking accounts: ";
        String savings = "Number of savings accounts: ";
        String loan = "Number of loans: ";
        String debit = "Number of debit cards: ";
        String credit = "Number of credit cards: ";
        String card = "Total credit/debit cards: ";
        String collat;
        String unsecured;
        String z_str = Helper.notify_str("warn", 0, false);

        if(num_accounts == 0) {
            acc += z_str;
        } else {
            acc += Helper.notify_str("success", num_accounts, false);
        }

        if(num_savings == 0){
            savings += z_str;
        } else {
            savings += Helper.notify_str("success", num_savings, false);
        }

        if(num_checking == 0) {
            checking += z_str;
        } else {
            checking += Helper.notify_str("success", num_checking, false);
        }

        if(num_loans == 0) {
            loan += z_str;
        } else {
            loan += Helper.notify_str("success", num_loans, false);
        }

        if(num_credit == 0) {
            credit += z_str;
        } else {
            credit += Helper.notify_str("success", num_credit, false);
        }

        if(num_debit == 0) {
            debit += z_str;
        } else {
            debit += Helper.notify_str("success", num_debit, false);
        }

        if(total_cards == 0) {
            card += z_str;
        } else {
            card += Helper.notify_str("success", total_cards, false);
        }

        System.out.println(savings);
        System.out.println(checking);
        System.out.println(acc);
        System.out.println(loan);
        System.out.println(credit);
        System.out.println(debit);
        System.out.println(card);
    }
    /**
     * Get a list of all accounts a user has, categorize and store them for future use. Additionally, build a promptmap
     * @return
     */
    public final HashMap<Integer, String> get_accounts() {
        /*  */
        accounts = new HashMap<>();
        HashMap<Integer, String> ret = new HashMap<>();
        CustomerOperations sync = new CustomerOperations(Helper.con());
        ArrayList<Account> deets = sync.account_details_for_user(this);
        int i = 1;
        for (Account a : deets) {
            // System.out.println(a.toString());
            String key = a.toString();
            ret.put(i++, key);
            accounts.put(key, a);
        }

        return ret;
    }

    /**
     * Build a heading containing the customer's metadata.
     */
    public void user_metadata() {
        Helper.notify("heading", "\nUser metadata for: "+this.toString(), true);
        this.format_data();

    }

    /**
     * Display associated metadata for each account of the customer.
     */
    public void account_metadata() {

        for(final Account a : this.user_accounts) {
            DateFormat date_fmt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            String cdate = date_fmt.format(a.creation_date);
            String adate = date_fmt.format(a.added_date);
            Helper.notify("heading", a.toString(), true);
            System.out.println("+Creation date: "+cdate);
            System.out.println("+Add date: "+adate);
            System.out.println("+Current interest rate:\t "+ Double.toString(a.interest)+"%");
            System.out.println("+Current balance:\t "+ String.format("%.2f",a.balance)+"$");
            if(a.min_balance != -1) {
                System.out.println("+Minimum balance:\t "+String.format("%.2f",a.min_balance)+"$");
            }
            System.out.println();
        }
        System.out.println();
    }
    /**
     * Display and return a user's Account after displaying them a promptmap.
     * @return
     */
    public Account prompt_checking() {
        HashMap<Integer, String> promptmap = new HashMap<>();
        int i = 1;
        for(final Account a : user_accounts) {
            if(a.min_balance > 0) {
                promptmap.put(i++, a.toString());
            }
        }

        String acc_choice = Helper.get_choice(promptmap, "Choose a Checking Account from below.");
        return accounts.get(acc_choice);
    }
    /**
     * Build a promptmap for the user's credit cards.
     * @return
     */
    public Credit credit_card_prompt() {
        HashMap<Integer, String> promptmap = new HashMap<>();
        int i = 1;
        for(final Credit c : user_credit) {
            promptmap.put(i++, c.toString());
        }
        return null;    
    }

    public Debit debit_card_prompt() {
        return null;
    }
    /**
     * Build a promptmap for a user's card selection.
     * @return
     */
    public HashMap<Integer, String> card_promptmap() {
        HashMap<Integer, String> promptmap = new HashMap<>();
        HashMap<String, Card> sync = new HashMap<>();

        //make sure we recompute so if a customer requests a new card, we can try and grab the current one here.
        //since the request (replacement) only changes the auxiliary details of the card, the transaction will still succeed here.
        //although the card number displayed will be outdated.

        if(this.num_credit > 0 ) {
            user_credit = this.sync.user_credit_cards(this);
        } 
        if(this.num_debit > 0) {
            System.out.println();
            user_debit = this.sync.user_debit_cards(this);
        } 
        
        int i = 1;
        
        if(this.num_credit > 0) {
            for(Credit c : user_credit) {
                String key = c.toString();
                promptmap.put(i++, key);
                sync.put(key, c);
            }
        }

        if(this.num_debit > 0) {
            for(Debit d : user_debit) {
                String key = d.toString();
                promptmap.put(i++, key);
                sync.put(key, d);
            }
        }
        user_cards = sync;
        return promptmap;
    }
    /**
     * Print out credit card metadata.
     */
    public void credit_metadata() {
        Helper.notify("heading", "\nCredit card metadata for "+this.toString()+"\n", true);
        // if(user_credit == null) return;
        for(final Credit c : user_credit) {
            c.metadata();
            System.out.println("****");
        }
        System.out.println("\n-------------------------------------------------------\n");
    }
    /**
     * Print out debit card metadata.
     */
    public void debit_metadata() {
        System.out.println("\n-------------------------------------------------------\n");
        Helper.notify("heading", "\nDebit card metadata for "+this.toString()+"\n", true);

        for(final Debit d : user_debit) {
            d.metadata();
            System.out.println("****");
        }
    }
    /**
     * Print out all card metadata by submitting work to individual metadata loops.
     * @throws UnrecoverableException
     */
    public void card_metadata() throws UnrecoverableException {
        this.compute();
        try {
            if(num_debit > 0) this.debit_metadata();
            if(num_credit > 0) this.credit_metadata();

        }catch(Exception e) {
            // e.printStackTrace();
            throw new UnrecoverableException();
        }
    }

    /**
     * Card request delegate method.
     * @param ops
     * @return
     * @throws UnrecoverableException
     */
    public boolean request_card(final CustomerOperations ops) throws UnrecoverableException{
        /* Let the user pick the card they want, then delegate the task. */
        HashMap<Integer, String> promptmap = new HashMap<>();
        String credit = "Request a new Credit Card.";
        String debit = "Request a new Debit Card.";
        promptmap.put(1, credit);
        if(num_accounts <= 0) {
            Helper.notify("warn", "A Debit Card cannot be requested as you have no accounts.", true);
        } else if(num_checking > 0) {
            promptmap.put(2, debit);
        }
        
        String choice = Helper.get_choice(promptmap,null);
        boolean result = false;
        if(choice.equals(credit)) {
            
            result = this.create_credit_card(ops);
        } else if(choice.equals(debit)) {
            Account a = this.prompt_checking();
            result = this.create_debit_card(ops, a);
        }

        if(result) Helper.notify("green", "++Successfully created new Card.", true);

        return result;



    }
    /**
     * Specialized debit card creation method.
     * @param ops
     * @param a
     * @return
     * @throws UnrecoverableException
     */
    public boolean create_debit_card(final CustomerOperations ops, final Account a) throws UnrecoverableException{
        final GenOperations gops = Helper.compute_general();
        final String pin = gops.compute_pin();
        final String card_num = gops.compute_card_num();
        final String cvc = gops.compute_cvc();
        Debit new_card = new Debit(null, cvc, card_num,  pin, a.acct_id, customer_id); //create a new "debit card" that we can serialize into the db.
        Card result = ops.create_debit_card(new_card);

        if(result != null) {
            return true;
        }

        return false;

    }
    /**
     * Specialized credit card creation method.
     */
    public boolean create_credit_card(final CustomerOperations ops) throws UnrecoverableException {
        final GenOperations gops = Helper.compute_general();
        final String card_num = gops.compute_card_num();
        final String cvc = gops.compute_cvc();
        double interest; 
        double c_lim; 
        
        boolean confirm = false;
        String conf_warn = Helper.notify_str("warn", "\nDo you wish to proceed?\n", false);
        // handle rounding:
        do {
            interest = Helper.get_double("Please enter the interest rate (0 to cancel): %");
            if(interest == 0) {
                System.out.println(safe_quit);
                return false;
            }
            c_lim = Helper.get_double("Please enter the credit limit (0 to cancel): $");
            if(c_lim == 0) {
                System.out.println(safe_quit);
                return false;
            }

            if(interest < 0 || c_lim < 0) {
                Helper.notify("warn", "You cannot enter negative numbers. Please try again.", true);
                continue;
            }

            interest = Double.valueOf(df_interest.format(interest));
            c_lim = Double.valueOf(df.format(c_lim));
            confirm = Helper.confirm("Creating card with details:\nInterest Rate: "+interest+"%\nCredit Limit: $"+c_lim+conf_warn);
        }while(!confirm);

        Credit new_card = new Credit(null, cvc, card_num, interest, null, 0, c_lim, customer_id);
        Card result = ops.create_credit_card(new_card);

        if(result != null) {
            System.out.println("here");
            return true;
        }

        return false;
    }
    /**
     * Lets a user pick the type of account to be created, then delegates the task out after collecting needed data.
     * @return
     */
    public boolean create_new_account() {
        /**
         * SQL Steps:
         * Create an account.
         * Insert into customer_accounts.
         */
        HashMap<Integer, String> promptmap = new HashMap<>();
        promptmap.put(1, "Savings Account");
        promptmap.put(2, "Checking Account");
        promptmap.put(3, "Return to Previous.");
        boolean confirm = false;
        // DecimalFormat df = new DecimalFormat("#.00");
        double new_interest = 0;
        double minimum_balance = 0;
        String acc_choice = "";

        do {
            acc_choice = Helper.get_choice(promptmap, "Please choose an account type to create.");
            
            if(acc_choice.equals("Return to Previous.")) {
                return false;
            } 
            do {
                new_interest = Helper.get_double("Enter the interest rate for the account (-1 to cancel): %");

                if(new_interest == -1.0) {
                    System.out.print(safe_quit);
                    return false;
                }

                if(new_interest <= 0) {
                    Helper.notify("warn", "Your interest cannot be zero, nor negative.", true);
                }
            }while(new_interest <= 0);

            if(acc_choice.equals("Checking Account")) {
                do {
                    minimum_balance = Helper.get_double("Enter the minimum balance for the account (-1 to cancel): $");


                if(minimum_balance == -1.0) {
                    System.out.print(safe_quit);
                    return false;
                }

                    if(minimum_balance < 0) {
                        Helper.notify("warn", "Your minimum balance cannot be negative", true);
                    }
                }while(minimum_balance < 0);
                confirm = Helper.confirm("Creating a "+acc_choice+" with interest of "+df_interest.format(new_interest)+"% and minimum balance of "+df.format(minimum_balance)+"$\nIs this ok?");
            } else {
                confirm = Helper.confirm("Creating a "+acc_choice+" with interest of "+df_interest.format(new_interest)+"%\nIs this ok?");
            }
            
        }while(!confirm);
        
        boolean status = false;

        new_interest = Double.valueOf(df_interest.format(new_interest));

        if(acc_choice.equals("Savings Account")) {
            Account new_account = new Account(0, new_interest, null, null);
            new_account.customer_id = customer_id;
            status = this.create_savings_account(new_account);
        } else if(acc_choice.equals("Checking Account")) {
            Account new_account = new Account(0, new_interest, null, null, minimum_balance, 0);
            new_account.customer_id = customer_id;
            status = this.create_checking_account(new_account);
        } 

        if(!status) {
            boolean do_again = Helper.confirm("Would you like to retry the account creation dialog?");
            if(do_again) {
                return create_new_account();
            }
        } else {
            Helper.notify("green", "+Successfully created new "+acc_choice, true);
        }

        return status;

    }
    /**
     * Kinda stub methods to delegate account creation to the CustomerOperations class.
     * @param a
     * @return
     */
    private boolean create_checking_account(final Account a) {
        CustomerOperations ops = new CustomerOperations(Helper.con());
        return ops.create_checking_account(a);
    }

    private boolean create_savings_account(final Account a) {
        CustomerOperations ops = new CustomerOperations(Helper.con());
        return ops.create_savings_account(a);
    }

    /**
     * Takes our user class and either creates a new account (for a null ID) or updates an existing account.
     * Returns object of new user account.
     * @return
     */
    public User commit() {
        if(this.customer_id == null) {
            //create a new acccount.
            ops.create_new_user(this);
        } else {
            ops.update_user(this);
        }
        return this;
    }

    /**
     * Private helper used to offload the task of verifying input for account transfers.
     * @param a
     * @return
     */
    private double prompt_transfer_amt(final Account a) {
        // DecimalFormat df = new DecimalFormat("#.00");
        Helper.notify("heading", "Brief account details for: "+a.toString(), true);
        System.out.println("Balance: $"+df.format(a.balance));
        double min_balance = (a.min_balance < 0) ? 0 : a.min_balance;
        double amt = Helper.get_double("Amount to transfer (0 to quit): $");
        if(amt > a.balance - min_balance) {
            Helper.notify("warn", "The entered amount is too large for your balance", true);
            return prompt_transfer_amt(a);
        } else if(amt == 0) {
            return -1;
        } else if(amt < 0) {
            Helper.notify("warn", "You cannot enter a negative amount.", true);
            return prompt_transfer_amt(a);
        } else {
            return amt;
        }
    }
    /**
     * Main handler of the account fund transfer logic.
     * @param acctmap
     * @param l
     * @return
     */
    public boolean fund_transfer(final HashMap<Integer, String> acctmap, final Location l) {
        // DecimalFormat df = new DecimalFormat("#.00");
        //do a fund transfer.
        HashMap<Integer, String> promptmap = new HashMap<>();
        HashMap<Integer, String> sans_accounts = new HashMap<>();

        String sender_str = Helper.notify_str("header", "Choose an account from which you want to transfer funds from: ", true);
        String rec_str = Helper.notify_str("header", "Choose an account from which you want to transfer funds to: ", true);
        
        String orig_account = Helper.get_choice(acctmap, sender_str);
        
        int u = 1;
        for(Integer k : acctmap.keySet()) {
            String acct = acctmap.get(k);
            if(!acct.equals(orig_account)){
                sans_accounts.put(u++,acct );
            } //add all but the same account.
        }

        String second_account = Helper.get_choice(sans_accounts, rec_str);
        
        Account sender = accounts.get(orig_account);
        Account receiver = accounts.get(second_account);

        int m = 1;
        System.out.println("\n");
        String key = "Sending Account: "+Helper.notify_str("warn",sender.toString(), false)+" \nReceiving Account: "+Helper.notify_str("warn", receiver.toString(), false)+"\nIs this OK?";
        String looks_good = "These accounts are correct. Let me continue and transfer funds.";
        String all_bad = "All the accounts are incorrect and I would like to retry entering them.";
        String bye = "Quit without proceeding.";

        promptmap.put(m++, looks_good);
        promptmap.put(m++, all_bad);
        promptmap.put(m++, bye);

        String oh_no_what_do_we_do = Helper.get_choice(promptmap, key);

        if(oh_no_what_do_we_do.equals(bye)) {
            return false;
        } else if(oh_no_what_do_we_do.equals(looks_good)) {
            double send_amt = Double.parseDouble(df.format(this.prompt_transfer_amt(sender)));
            if(send_amt <= 0) {
                return false;
            }
            boolean confirm_transfer = Helper.confirm("Is transfering the amount: $"+send_amt+" ok?");
            if(confirm_transfer) {
                int loc_id = l.location_id;
                boolean with_ok = ops.do_withdrawal(send_amt, loc_id, sender.acct_id, this.customer_id);
                if(with_ok) {
                    boolean dep_ok = ops.do_deposit(send_amt, loc_id, receiver.acct_id, this.customer_id);
                    if(dep_ok) {
                        Helper.notify("green", "++Successfully transfered $"+send_amt,  true);
                        return true;
                    }
                }
            } else {
                return fund_transfer(acctmap, l); //redo the prompting.
            }
            // return false;
        } else if(oh_no_what_do_we_do.equals(all_bad)) {
            return fund_transfer(acctmap, l);
        }
        return false;
    }
    /**
     * Delegate function for requesting a new card. Pawns off work to more specialized ones.
     */
    public boolean request_new_card(final Card orig) {

        // Card temp = orig.clone(); //partially clone the object.

        Debit temp_debit = null;
        Credit temp_credit = null;
        String new_pin = genops.compute_pin();
        String new_cvc = genops.compute_cvc();
        String new_card_num = genops.compute_card_num();
        boolean status = false;
        int card_id = orig.get_card_id();
       
        // System.out.println(orig.getClass().getName());
        //ideally I would have liked to implement cloneable and allow for a deep copy of the card so we could pass that around
        //instead of passing around params willy nilly, but time crunch.
        if(orig instanceof Debit) {
            temp_debit = (Debit)orig;
            status = ops.replace_debit_card(new_cvc, new_card_num, new_pin, card_id);
            if(status) {
                temp_debit.pin = new_pin;
                temp_debit.cvc = new_cvc;
                temp_credit.card_number = new_card_num;
                Helper.notify("green", "\n++New PIN: "+new_pin,true);
            }
        } else if(orig instanceof Credit) {
            // System.out.println("here");
            temp_credit = (Credit)orig;
            status = ops.replace_credit_card(new_cvc, new_card_num, card_id);
            if(status) {
                temp_credit.cvc = new_cvc;
                temp_credit.card_number = new_card_num;
                
            }
        }
        if(status) {
            Helper.notify("green", "++New CVC: "+new_cvc, true);
            Helper.notify("green", "++New Card Number: "+new_card_num, true);
        }
        return status;
        
    }

}