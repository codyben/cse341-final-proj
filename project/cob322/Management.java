import java.sql.*;  
import java.util.*;
class Management extends ProjectInterface {
	ManagementOperations ops;
	Management(String n, String e) {
		super(n,e);
	}
	public void provision(ManagementOperations o) {
		ops = o;
	}
}