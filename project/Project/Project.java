import java.sql.*;  
import java.io.*;
import java.util.*;
class Project{  

    private static String username;
    private static String password;

    public static String prompt_username() {
        Scanner scnr = new Scanner(System.in); 
        System.out.print("Enter username: ");
        String username = scnr.nextLine();
        // scnr.close();
        return username;
    }

    public static String prompt_password() {
        // Scanner scnr = new Scanner(System.in);
        Console console = System.console(); 
        String password = new String(console.readPassword("Enter password: "));
        // System.out.println(password);
        // scnr.close();
        return password;
    }

    public static boolean prompt_retry() {
        Scanner scnr = new Scanner(System.in); 
        System.out.println("Enter Y to retry, else to quit");
        String input = scnr.next();
        // scnr.close();
        if(input.equals("Y") || input.equals("y")) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean pick_role() {
        System.out.println("\n");
        HashMap<Integer, String> use_cases = new HashMap<>();
            use_cases.put(1, "Bank Management");
            use_cases.put(2, "Client");
            use_cases.put(3, "Quit");
        String choice = Helper.get_choice(use_cases, Helper.notify_str("heading", "Choose a role you would like to assume.", true));
            if(choice.equals("Bank Management")) {
                Management mgt = new Management("Management", "After confirmation, select a metric you would like to view."); // transfer context to the management.
                mgt.launch();
                boolean sentinel = mgt.confirm();
                if(!sentinel) {
                    return true; //have the user pick another role.
                }
                mgt.provision(new ManagementOperations(Helper.con()));
                return true;
            } else if(choice.equals("Client")) {
                Client client = new Client("Client", "After confirmation, select a method to view a user from below.");
                client.launch();
                boolean sentinel = client.confirm();

                if(!sentinel) {
                    return true; //have the user pick another role.
                }
                client.provision(new CustomerOperations(Helper.con()));
                User customer = client.divergent_paths();
                client.intent(customer);
                return true;
            } else if(choice.equals("Quit")) {
                Helper.exit();
            }
        return true;
    }
    public static void main(String args[]) {
        String username;
        String password;
        if(args != null && args.length == 2) {
            username = args[0];
            password = args[1];
        } else {
            Helper.notify("notify", "Enter credentials to connect to Edgar1", true);
            username = prompt_username();
            password = prompt_password();
        }
        String host = "edgar1.cse.lehigh.edu";
        Project.username = username;
        Project.password = password;
        try (Connection con=DriverManager.getConnection("jdbc:oracle:thin:@"+host+":1521:cse241",username,password);Statement s=con.createStatement();) {
            /* Add a shutdown hook in order to handle a ctrl-c exit */
            /* src: https://www.geeksforgeeks.org/jvm-shutdown-hook-java/ */
            Runtime.getRuntime().addShutdownHook(new Thread() { 
                public void run() { 
                    Helper.sigterm();
                } 
            }); 
            
            Helper.set_con(con);
            Helper.notify("success", "\n\n** Welcome to Nickel Savings Bank **", true);
            boolean status = true;
            do {
                status = pick_role();
            } while(status);

            Helper.exit();
        } catch(Exception e) {
            String msg = e.getMessage();
            // e.printStackTrace();
            System.out.println(msg);
            if(msg.contains("01017")) {
                System.out.println();
                Helper.notify("error", "Invalid login credentials.", true);
                System.out.println();
                boolean yn = Helper.confirm("Would you like to attempt to try your credentials again?");
                if(yn)
                    main(null);
                else{
                    Helper.notify("success", "Goodbye.", true);
                    System.exit(0);
                } 
                    
            } else if(msg.contains("IO Error")) {
                Helper.notify("error", "Failed to reach host. The program will now exit.", true);
                System.exit(1);
            }
        }
    }
}