import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
class User {
    public int customer_id;
    public String first_name;
    public String last_name;
    public Date dob;
    public String full_name;
    public int num_accounts = 0;
    public int num_credit = 0;
    public int num_debit = 0;
    public int num_loans = 0;
    public int total_cards = 0;
    public HashMap<String, Account> accounts;
    public ArrayList<Account> user_accounts;
    public ArrayList<Debit> user_debit;
    public ArrayList<Credit> user_credit;
    public HashMap<String, Card> user_cards;

    User(int c, String f, String l, Date d) {
        customer_id = c;
        first_name = f;
        last_name = l;
        dob = d;
        full_name = first_name +" "+last_name;
        
    }

    @Override
    public String toString() {
        return full_name + "(ID="+Integer.toString(customer_id)+")";
    }
    public void compute() {
        CustomerOperations sync = new CustomerOperations(Helper.con());
        num_accounts = sync.num_accounts_for_user(this);
        num_credit = sync.num_credit_for_user(this);
        num_debit = sync.num_debit_for_user(this);
        total_cards = sync.num_cards_for_user(this);
        if(num_credit > 0 ) {
            user_credit = sync.user_credit_cards(this);
        } else if(num_debit > 0) {
            user_debit = sync.user_debit_cards(this);
        } else if (total_cards > 0 ) {
            user_cards = new HashMap<>();
        }
        // num_loans = sync.num_loans_for_user(this);
        user_accounts = sync.account_details_for_user(this);
    }


    public void format_data() {
        String acc = "Number of accounts: ";
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

        System.out.println(acc);
        System.out.println(loan);
        System.out.println(credit);
        System.out.println(debit);
        System.out.println(card);
    }

    public final HashMap<Integer, String> get_accounts() {
        /* Get a list of all accounts a user has, categorize and store them for future use. Additionally, build a promptmap */
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

    public void user_metadata() {
        Helper.notify("heading", "\nUser metadata for: "+this.toString(), true);
        this.format_data();

    }

    public void account_metadata() {

        for(final Account a : this.user_accounts) {
            DateFormat date_fmt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            String cdate = date_fmt.format(a.creation_date);
            String adate = date_fmt.format(a.added_date);
            Helper.notify("heading", a.toString(), true);
            System.out.println("+Creation date: "+cdate);
            System.out.println("+Add date: "+adate);
            System.out.println("+Current interest rate:\t "+ Double.toString(a.interest)+"%");
            System.out.println("+Current balance:\t "+ Double.toString(a.balance)+"$");
            if(a.min_balance != -1) {
                System.out.println("+Minimum balance:\t "+Double.toString(a.min_balance)+"$");
            }
        }
    }

    public Account prompt_checking() {
        HashMap<Integer, String> promptmap = new HashMap<>();
        int i = 1;
        for(final Account a : user_accounts) {
            if(a.min_balance < 0) {
                promptmap.put(i++, a.toString());
            }
        }

        String acc_choice = Helper.get_choice(promptmap, "Choose a Checking Account from below.");
        return accounts.get(acc_choice);
    }

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

    public HashMap<Integer, String> card_promptmap() {
        HashMap<Integer, String> promptmap = new HashMap<>();
        HashMap<String, Card> sync = new HashMap<>();
        
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

    public void credit_metadata() {
        Helper.notify("heading", "\nCredit card metadata for "+this.toString()+"\n", true);
        // if(user_credit == null) return;
        for(final Credit c : user_credit) {
            c.metadata();
        }
    }

    public void debit_metadata() {
        Helper.notify("heading", "\nDebit card metadata for "+this.toString()+"\n", true);
        for(final Debit d : user_debit) {
            d.metadata();
        }
    }

    public void card_metadata() throws UnrecoverableException {
        try {
            if(num_debit > 0) this.debit_metadata();
            if(num_credit > 0) this.credit_metadata();

        }catch(Exception e) {
            e.printStackTrace();
            throw new UnrecoverableException();
        }
    }

    public boolean replace_card() {
        return false;
    }

    public boolean request_card(final CustomerOperations ops) throws UnrecoverableException{
        /* Let the user pick the card they want, then delegate the task. */
        HashMap<Integer, String> promptmap = new HashMap<>();
        String credit = "Request a new Credit Card.";
        String debit = "Request a new Debit Card.";
        promptmap.put(1, credit);
        if(num_accounts <= 0) {
            Helper.notify("warn", "A Debit Card cannot be requested as you have no accounts.", true);
        } else {
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

        return result;



    }

    public boolean create_debit_card(final CustomerOperations ops, final Account a) throws UnrecoverableException{
        final GenOperations gops = Helper.compute_general();
        final String pin = gops.compute_pin();
        final String card_num = gops.compute_card_num();
        final String cvc = gops.compute_cvc();
        Debit new_card = new Debit(null, cvc, card_num,  pin, a.acct_id); //create a new "debit card" that we can serialize into the db.
        ops.create_debit_card(new_card);
        return false;

    }

    public boolean create_credit_card(final CustomerOperations ops) throws UnrecoverableException {
        final GenOperations gops = Helper.compute_general();
        final String card_num = gops.compute_card_num();
        final String cvc = gops.compute_cvc();
        double interest; 
        double c_lim; 
        DecimalFormat df = new DecimalFormat("#.0#");
        boolean confirm = false;
        String conf_warn = Helper.notify_str("warn", "\nDo you wish to proceed?\n", false);
        // handle rounding:
        do {
            interest = Helper.get_double("Please enter the interest rate: %");
            c_lim = Helper.get_double("Please enter the credit limit: $");
            interest = Double.valueOf(df.format(interest));
            c_lim = Double.valueOf(df.format(c_lim));
            confirm = Helper.confirm("Creating card with details:\nInterest Rate: "+interest+"%\nCredit Limit: $"+c_lim+conf_warn);
        }while(!confirm);

        Credit new_card = new Credit(null, cvc, card_num, interest, null, 0, c_lim);
        ops.create_credit_card(new_card);
        return false;
    }


}