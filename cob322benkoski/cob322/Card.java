import java.text.DecimalFormat;
import java.sql.*;
import java.util.*;
public class Card implements Cloneable{
    public String card_id;
    public String cvc;
    public String card_number;
    private Connection con;
    public int customer_id;
    private CustomerOperations ops;
    private ArrayList<CardActivity> recs;
    public String trunc_card_id;

    Card(String card_id, String cvc, String card_number, int cust_id) {
        con = Helper.con();
        ops = new CustomerOperations(con);
        this.card_id = card_id;
        this.cvc = cvc;
        this.card_number = card_number;
        this.customer_id = cust_id;
        this.trunc_card_id = card_number.substring(0, 4) + "************";
    }

    Card() {
        //stub constructor for deserialize_credit.
    }

    /**
     * Return a clone of the current card. Unusued.
     */
    public Card clone() {
        Card temp = new Card(card_id, cvc, card_number, customer_id);
        return temp;
    }

    /* Helper methods to handle the coercion from different data types */
    final public int get_cvc() {
        return Integer.parseInt(this.cvc);
    }

    final public int get_card_id() {
        return Integer.parseInt(this.card_id);
    }

    @Override
    public String toString() {
        if(this instanceof Debit) {
            return "Debit Card (NUM="+trunc_card_id+")";
        } else {
            return "Credit Card (NUM="+trunc_card_id+")"; 
        }
    }
    public void metadata() {
        Helper.notify("heading", "+Card Number: "+card_number, true);
        System.out.println("+Card ID: "+card_id);
        System.out.println("+Card CVC: "+cvc);
        
        System.out.println();
    }

    /**
     * Perform a purchase given an amount and a name.
     * @param amount
     * @param name
     * @return
     */
    final public int make_purchase(final double amount, final String name) {
        int i_card_id = Integer.parseInt(card_id);
        int result = -1;
        if(this instanceof Debit) {
            result = ops.do_debit_purchase(amount, name, i_card_id);
        } else if(this instanceof Credit) {
            result = ops.do_credit_purchase(amount, name, i_card_id);
        }

        return result;
    }

    /**
     * Handle the input relating to making a purchase. Returns false on quit.
     * @return
     */
    final public boolean prompt_and_confirm_purchase() {
        DecimalFormat df = new DecimalFormat("#.00");
        boolean confirm = false;
        int purchase_result = -1;
        double amt = 0;
        String name = "";
        // handle rounding:
        // https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
        do {
            do {
                amt = Helper.get_double("Please the amount of your purchase (0 to quit): $");

                if(amt == 0) {
                    return false;
                }

                if(amt < 0) {
                    Helper.notify("warn", "Please try again. Your purchase cannot be negative or zero.", true);
                }
            }while(amt <= 0);
            amt = Double.valueOf(df.format(amt));
            name = Helper.get_string("Please enter the name of your purchase: ");
            confirm = Helper.confirm("Is a purchase at: "+name+" for $"+amt+" ok?");
        }while(!confirm);

        purchase_result = make_purchase(amt, name);

        if(purchase_result == 0) {
            boolean redo = Helper.confirm("You had insufficient funds for a purchase of $"+amt+" would you like to try again?");
            if(redo) {
                return prompt_and_confirm_purchase();
            } else {
                return false;
            }
        } else if(purchase_result == -1) {
            Helper.notify("warn", "Due to an error occurring during your purchase, you will be returned to the previous screen.\n", true);
            return false;
        }
        Helper.notify("green", "\n++Purchase successful!\n", true);
        return true;
        
    }

    /**
     * Create an arraylist of Activity objects relating to this card.
     */
    final public void activity() {
        ArrayList<CardActivity> results = ops.do_activity(Integer.parseInt(card_id));

        if(results == null) {
            Helper.notify("warn", "No activity detected for "+this.toString(), true);
            return;
        }
        
        this.recs = results;

        System.out.println("\n-------------------------------------------------------\n");

        for(CardActivity r : results) {
            r.metdata();
            System.out.println();
        }

        System.out.println("\n-------------------------------------------------------\n");
        
    }



}