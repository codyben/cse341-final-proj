import java.util.Date;
class Account {
    public int acct_id;
    public double balance;
    public double interest;
    public Date creation_date;
    public Date added_date;
    public double min_balance;
    public int customer_id;

    Account(double b, double i, Date c, Date a) {
        balance = b;
        interest = i;
        creation_date = c;
        added_date = a;
        min_balance = -1;
    }

    @Override
    public String toString() {
        String type = "Savings Account";
        if(this.min_balance < 0) {
            type = "Checking Account";
        }
        return type+" (ACCT_ID="+this.acct_id+")";
    }
    Account(double b, double i, Date c, Date a, double m, int id) {
        acct_id = id;
        balance = b;
        interest = i;
        creation_date = c;
        added_date = a;
        min_balance = m;
    }
}