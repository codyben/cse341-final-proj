public class Credit extends Card {
    public double interest;
    public Double balance_due;
    public double running_balance;
    public double credit_limit;

    Credit(String card_id, String cvc, String card_number, double interest, Double balance_due, double running_balance, double c_lim, int cust_id) {
        super(card_id, cvc, card_number, cust_id);
        this.interest = interest;
        this.balance_due = balance_due;
        this.running_balance = running_balance;
        this.credit_limit = c_lim;
    }

    public void metadata() {
        super.metadata();
        String due = Double.toString(balance_due);

        if(balance_due == 0) {
            due = Helper.notify_str("green", due, false);
        } else {
            due = Helper.notify_str("red", due, false);
        }
        System.out.println("+Interest: "+Double.toString(interest)+"%");
        System.out.println("+Balance Due: "+due+"$");
        System.out.println("+Running Balance: "+Double.toString(running_balance)+"$");
        System.out.println("+Credit Limit: "+Double.toString(credit_limit)+"$");
    }
}