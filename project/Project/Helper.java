import java.io.*;
import java.security.UnrecoverableEntryException;
import java.util.*;
import java.sql.*;  
class Helper {
    private static Connection con;
    private static GenOperations ops;
    public static void set_con(Connection c) {
        con = c;
    }
    
    public static Connection con() {
        return con;
    }

    public static final GenOperations compute_general() throws UnrecoverableException {
        /* use a singleton to compute all the branches/locations/atms */
        try {
            if(Helper.ops == null) {
                Helper.ops = new GenOperations(con);
            }

            return Helper.ops;
        } catch(Exception e) {
            // e.printStackTrace();
            throw new UnrecoverableException();
        }
    }

    public static void cleanup() {
        /* handle a graceful halt */
        try {
            Helper.con.close();
            Helper.notify("green", "Successfully closed connection.", true);
        } catch(Exception e) {
            Helper.notify("error", "An additional error occurred while closing the db connection.", true);
            System.exit(1);
        }
            
    }
    public static void exit() {
        // Helper.cleanup();
        Helper.notify("success", "Goodbye.", true);
        System.exit(0);
    }

    public static void error_exit() {
        Helper.notify("error", "Terminated due to unrecoverable error or SIGTERM.", true);
        // Helper.cleanup();
        System.exit(1);
    }

    public static void sigterm() {
        Helper.notify("warn", "\nPreparing for graceful halt. Cleaning up and closing.", true);
        Helper.cleanup();
        // System.exit(1);
    }

    public static String get_string(String msg) {
        Scanner scnr = new Scanner(System.in); 
        System.out.println(msg);
        return scnr.nextLine();
    }

    public static Integer get_int(String msg) {
        Scanner scnr = new Scanner(System.in); 
        System.out.println(msg);
        if(scnr.hasNextInt()) 
            return scnr.nextInt();
        else {
            Helper.notify("error", "You did not enter an integer. Try again.", true);
            return get_int(msg);
        }  
    }

    public static Double get_double(final String msg) {
        Scanner scnr = new Scanner(System.in); 
        System.out.println(msg);
        if(scnr.hasNextDouble()) 
            return scnr.nextDouble();
        else {
            Helper.notify("error", "You did not enter a valid amount. Try again.", true);
            return get_double(msg);
        }  
    }
    public static boolean confirm() {
        Helper.notify("warn", "\nDo you wish to proceed?\n", true);
        HashMap<Integer, String> use_cases = new HashMap<>();
        use_cases.put(1, "Yes.");
        use_cases.put(2, "No.");
        String c = get_choice(use_cases, null);
        if(c.equals("Yes.")) {
            return true;
        }
        return false;

    }

    public static boolean confirm(String msg) {
        Helper.notify("warn", "\n"+msg+"\n", true);
        HashMap<Integer, String> use_cases = new HashMap<>();
        use_cases.put(1, "Yes.");
        use_cases.put(2, "No.");
        String c = get_choice(use_cases, null);
        if(c.equals("Yes.")) {
            return true;
        }
        return false;

    }

    public static Integer prompt_choice() {
        Scanner scnr = new Scanner(System.in); 
        System.out.println("Enter number of your choice: ");
        if(scnr.hasNextInt()) 
            return scnr.nextInt();
        else 
            return null;

    }

    private static void append_quit(HashMap<Integer, String> results) {

    }
    /**
     * Provide an easy way to prompt the user with a list of choice.
     * Used extensively throughout project.
     * Original function is a carryover from previous Homework.
     * @param results
     * @param msg
     * @return String
     */
    public static String get_choice(HashMap<Integer, String> results, String msg) {
        if(msg == null) {
            msg = "Choose an option below.";
        } 
        System.out.println(msg);
        System.out.println();
        for (Integer key : results.keySet()) {
            String dept_name = results.get(key);
            System.out.printf("%-10s %-10s\n", key.toString()+"].", dept_name);
        }
        Integer choice = Helper.prompt_choice();
        if(choice == null || choice > results.size() || choice <= 0) {
            System.out.println();
            Helper.notify("error", "Invalid choice entered. Try again.", true);
            System.out.println();
            return Helper.get_choice(results, msg);
        } else {
            return results.get(choice);
        }
    }
    /**
     * Add some color to messages presented to user.
     * @param color
     * @param msg
     * @param n
     */
    public static void notify(String color, String msg, boolean n) {
        String code = "";
        String reset = "\u001B[0m";

        switch(color.toLowerCase()) {
            case "error":
            case "red":
                code = "\u001B[31m";
                break;

            case "success":
            case "green": code = "\u001B[32m"; break;
            case "warn":
            case "yellow": code = "\u001B[33m"; break;
            case "heading":
            case "notify":
            case "cyan": code = "\u001B[36m"; break;
        }

        if(!code.equals("")) {
            System.out.print(code+msg+reset);
        } else {
            System.out.print(msg);
        }

        if(n) {
            System.out.println();
        }
    }

    public static String notify_str(String color, String msg, boolean n) {
        String code = "";
        String reset = "\u001B[0m";
        String ret = "";
        switch(color.toLowerCase()) {
            case "error":
            case "red":
                code = "\u001B[31m";
                break;

            case "success":
            case "green": code = "\u001B[32m"; break;
            case "warn":
            case "yellow": code = "\u001B[33m"; break;
            case "notify":
            case "heading":
            case "cyan": code = "\u001B[36m"; break;
        }

        if(!code.equals("")) {
            ret = code+msg+reset;
        } else {
            ret = msg;
        }

        if(n) {
            ret += "\n";
        }

        return ret;
    }

    public static void notify(String color, int ms, boolean n) {
        String msg = Integer.toString(ms);
        String code = "";
        String reset = "\u001B[0m";

        switch(color.toLowerCase()) {
            case "error":
            case "red":
                code = "\u001B[31m";
                break;

            case "success":
            case "green": code = "\u001B[32m"; break;
            case "warn":
            case "yellow": code = "\u001B[33m"; break;
            case "heading":
            case "notify":
            case "cyan": code = "\u001B[36m"; break;
        }

        if(!code.equals("")) {
            System.out.print(code+msg+reset);
        } else {
            System.out.print(msg);
        }

        if(n) {
            System.out.println();
        }
    }

    public static String notify_str(String color, int ms, boolean n) {
        String msg = Integer.toString(ms);
        String code = "";
        String reset = "\u001B[0m";
        String ret = "";
        switch(color.toLowerCase()) {
            case "error":
            case "red":
                code = "\u001B[31m";
                break;

            case "success":
            case "green": code = "\u001B[32m"; break;
            case "warn":
            case "yellow": code = "\u001B[33m"; break;
            case "notify":
            case "heading":
            case "cyan": code = "\u001B[36m"; break;
        }

        if(!code.equals("")) {
            ret = code+msg+reset;
        } else {
            ret = msg;
        }

        if(n) {
            ret += "\n";
        }

        return ret;
    }

    public static String notify_str(String color, double ms, boolean n) {
        String msg = Double.toString(ms);
        String code = "";
        String reset = "\u001B[0m";
        String ret = "";
        switch(color.toLowerCase()) {
            case "error":
            case "red":
                code = "\u001B[31m";
                break;

            case "success":
            case "green": code = "\u001B[32m"; break;
            case "warn":
            case "yellow": code = "\u001B[33m"; break;
            case "notify":
            case "heading":
            case "cyan": code = "\u001B[36m"; break;
        }

        if(!code.equals("")) {
            ret = code+msg+reset;
        } else {
            ret = msg;
        }

        if(n) {
            ret += "\n";
        }

        return ret;
    }
}