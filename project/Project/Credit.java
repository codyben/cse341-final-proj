public class Credit extends Card {
    public double interest;
    public double balance_due;
    public double running_balance;

    Credit(String card_id, String cvc, String card_number, double interest, double balance_due, double running_balance) {
        super(card_id, cvc, card_number);
        this.interest = interest;
        this.balance_due = balance_due;
        this.running_balance = running_balance;
    }

    public void metadata() {
        super.metadata();
        System.out.println("+Interest: "+Double.toString(interest)+"%");
        System.out.println("+Balance Due: "+Double.toString(balance_due)+"$");
        System.out.println("+Running Balance: "+Double.toString(running_balance)+"$");
    }
}