import java.sql.*; 
import java.util.*; 
class ProjectInterface {
	// DatabaseOperations ops;
	String interface_name = "";
	String expository = "";

	ProjectInterface(String n,String e) {
		interface_name = n;
		expository = e;
	}

	final public void launch() {
		System.out.println("\n");
		Helper.notify("notify", "Hello! Welcome to the "+interface_name+" interface.\n", true);
		Helper.notify("notify", expository, true);
	}

	final public boolean confirm() {
		return Helper.confirm();
	}

}