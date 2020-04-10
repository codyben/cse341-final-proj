
public class Location {
    int location_id;
    String city;
    String state;
    String zip;
    String street;
    String street_num;
    String address;

    Location(int id, String city, String state, String zip, String street, String street_num, String address) {
        location_id = id;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.street = street;
        this.street_num = street_num;
        this.address = address;
    }

    @Override
    public String toString() {
        String key = "Branch located at: "+this.address;
        if(this instanceof ATM) {
            key = "ATM located at: "+this.address;
        }
        return key;
    }
}