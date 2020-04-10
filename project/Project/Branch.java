public class Branch extends Location {
    int branch_id;
    String hours;
    Branch(int id, String city, String state, String zip, String street, String street_num, String address, int branch_id, String hours) {
        super(id, city, state, zip, street, street_num, address);
        this.branch_id = branch_id;
        this.hours = hours;
    }
}