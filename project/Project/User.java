import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    public HashMap<Integer, String> get_accounts() {
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

        for(Account a : this.user_accounts) {
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

    public void credit_metadata() {
        Helper.notify("heading", "Credit card metadata for "+this.toString(), true);
        for(Credit c : user_credit) {
            c.metadata();
        }
    }

    public void debit_metadata() {
        Helper.notify("heading", "Debit card metadata for "+this.toString(), true);
        for(Debit d : user_debit) {
            d.metadata();
        }
    }

    public void card_metadata() {
        this.debit_metadata();
        this.credit_metadata();
    }


}