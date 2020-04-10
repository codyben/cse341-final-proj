public class ATM extends Location {
    int atm_id;
    String operator;
    String hours;
    ATM(int id, String city, String state, String zip, String street, String street_num, String address, int atm_id, String op_name, String hours) {
        super(id, city, state, zip, street, street_num, address);
        this.atm_id = atm_id;
        this.operator = op_name;
        this.hours = hours;
    }
}