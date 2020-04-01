import java.util.Date;
class Account {

    public double balance;
    public double interest;
    public Date creation_date;
    public Date added_date;

    Account(double b, double i, Date c, Date a) {
        balance = b;
        interest = i;
        creation_date = c;
        added_date = a;
    }
}