import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
public class CardActivity {
    public String card_num;
    public String purchase_name;
    public String purchase_time;
    public double purchase_amount;

    CardActivity(String num, String name, Date purchase_time, double purchase_amount) {
        DateFormat date_fmt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        this.purchase_time = date_fmt.format(purchase_time);
        this.purchase_amount = purchase_amount;
        this.purchase_name = name;
        this.card_num = num;
    }

    @Override
    public String toString() {
        return card_num;
    }

    public void metdata() {
        System.out.println(Helper.notify_str("heading", "+Purchase time: "+purchase_time, false));
        System.out.println("+Card Number: "+card_num);
        System.out.println("+Purchase name: "+purchase_name);
        System.out.println("+Purchase Amount: $"+purchase_amount);
    }
}