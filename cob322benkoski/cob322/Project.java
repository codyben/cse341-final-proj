import java.sql.*;  
import java.io.*;
import java.util.*;


class Project{  

    private static String username;
    private static String password;
    private final static String host = "edgar1.cse.lehigh.edu";

    private static boolean customer() throws UnrecoverableException{
        Client client = new Client("Client", "After confirmation, select a method to view a user from below.");
        client.launch();
        Location l = Client.force_location();
        if(l == null) {
            return true; //shoot the user back to main prompt.
        }
        
        // boolean good_location = client.confirm();
        // if(!good_location) {
        //     l = Client.force_location();
        // }

        System.out.println("Your current location is: "+Helper.notify_str("notify", l.toString(), true));
        boolean sentinel = client.confirm();
        if(!sentinel) {
            return true; //have the user pick another role.
        }
        client.provision(new CustomerOperations(Helper.con()), Helper.compute_general());
        do {
            client.set_location(l);
            System.out.println("Your current location is: "+Helper.notify_str("notify", l.toString(), true));
            User customer = client.divergent_paths();
            if(customer == null) return customer();
            sentinel = client.intent(customer);
        }while(sentinel);
        return true; //return a true to start cycle over again.
    }

    private static boolean pick_role() throws UnrecoverableException {
        System.out.println("\n");
        HashMap<Integer, String> use_cases = new HashMap<>();
            use_cases.put(1, "Bank Management (NOT IMPLEMENTED)");
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
                return customer();
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
            username = Helper.get_string("Enter username: ");
            password = Helper.prompt_sensitive("Enter password: ");
        }

        try (Connection con=DriverManager.getConnection("jdbc:oracle:thin:@"+host+":1521:cse241",username,password);Statement s=con.createStatement();) {
            /* Add a shutdown hook in order to handle a ctrl-c exit */
            /* src: https://www.geeksforgeeks.org/jvm-shutdown-hook-java/ */
            Runtime.getRuntime().addShutdownHook(new Thread() { 
                public void run() { 
                    Helper.sigterm();
                } 
            }); 
            
            Helper.set_con(con); //don't reinitialize a connection, so set it here.
            
            System.out.println("Loading required metadata...");
            Helper.compute_general(); //this most likely isn't going to change, so compute once in beginning.
            System.out.println("Metadata load complete.");


            Helper.notify("success", "\n\n** Welcome to Nickel Savings Bank **", true);
            boolean status = true;
            do {
                status = pick_role();
            } while(status);

            Helper.exit();
        }catch(UnrecoverableException ue) {
            ue.printStackTrace();
            Helper.notify("error", "An unrecoverable error has occurred. The program will now exit.", true);
            System.exit(1);
        } catch(Exception e) {
            String msg = e.getMessage();
            // e.printStackTrace();
            // System.out.println(msg);
            if(msg == null) {
                Helper.notify("error", "An unrecoverable error has occurred. The program will now exit.", true);
                System.exit(1);
            }
            if(msg.contains("01017") || msg.contains("01005")) {
                System.out.println();
                Helper.notify("error", "Invalid login credentials.", true);
                System.out.println();
                boolean yn = Helper.confirm("Would you like to attempt to try your credentials again?");
                if(yn)
                    main(null);
                else{
                    Helper.exit();
                } 
                    
            } else if(msg.contains("IO Error")) {
                Helper.notify("error", "Failed to reach host. The program will now exit.", true);
                System.exit(6);
            }
        }
    }
}