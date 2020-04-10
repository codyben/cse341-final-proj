public class Debit extends Card {
    public String pin;
    public int acct_id;

    Debit(String card_id, String cvc, String card_number, String pin, int acct_id) {
        super(card_id, cvc, card_number);
        this.pin = pin;
        this.acct_id = acct_id;
    }

    public void metadata() {
        super.metadata();
        System.out.println("+PIN: "+pin);
        System.out.println("+Associated Account ID: "+acct_id);
    }
}