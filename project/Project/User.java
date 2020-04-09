import java.util.Date;
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

    User(int c, String f, String l, Date d) {
        customer_id = c;
        first_name = f;
        last_name = l;
        dob = d;
        full_name = first_name +" "+last_name;
    }

    public void compute() {
        CustomerOperations sync = new CustomerOperations(Helper.con());
        num_accounts = sync.num_accounts_for_user(this);
        num_credit = sync.num_credit_for_user(this);
        num_debit = sync.num_debit_for_user(this);
        total_cards = sync.num_cards_for_user(this);
        // num_loans = sync.num_loans_for_user(this);
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


}