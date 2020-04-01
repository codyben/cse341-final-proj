import java.util.Date;
class User {
    public int customer_id;
    public String first_name;
    public String last_name;
    public Date dob;
    public String full_name;
    private int num_accounts = 0;
    private int num_credit = 0;
    private int num_debit = 0;
    private int num_loans = 0;
    private int total_cards = 0;

    User(int c, String f, String l, Date d) {
        customer_id = c;
        first_name = f;
        last_name = l;
        dob = d;
        full_name = first_name +" "+last_name;
        this.compute();
    }

    private void compute() {
        
    }
}