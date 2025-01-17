public class Debit extends Card {
    public String pin;
    public int acct_id;

    Debit(String card_id, String cvc, String card_number, String pin, int acct_id, int cust_id) {
        super(card_id, cvc, card_number, cust_id);
        this.pin = pin;
        this.acct_id = acct_id;
    }

    /**
     * print out the details of the card.
     */
    public void metadata() {
        super.metadata();
        System.out.println("+PIN: "+pin);
        System.out.println("+Associated Account ID: "+acct_id);
    }

    public int get_pin() {
        return Integer.parseInt(this.pin);
    }

}