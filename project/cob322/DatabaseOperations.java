import java.sql.*;
class DatabaseOperations {
    Connection con;

    DatabaseOperations(Connection c) {
        con = c;
    }

    public Connection con() {
        return con;
    }
}