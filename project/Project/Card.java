public class Card {
    public String card_id;
    public String cvc;
    public String card_number;

    Card(String card_id, String cvc, String card_number) {
        this.card_id = card_id;
        this.cvc = cvc;
        this.card_number = card_number;
    }

    public void metadata() {
        System.out.println("+Card ID: "+card_id);
        System.out.println("+Card CVC: "+cvc);
        System.out.println("+Card Number: "+card_number);
    }
}