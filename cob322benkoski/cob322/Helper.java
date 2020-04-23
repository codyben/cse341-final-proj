import java.io.*;
import java.util.*;
import java.sql.*;  
import java.text.SimpleDateFormat;
class Helper {
    private static Connection con;
    private static GenOperations ops;
    public static void set_con(Connection c) {
        con = c;
    }
    
    /**
     * Return an initialized database connection object.
     * @return
     */
    public static Connection con() {
        return con;
    }
    /**
     * Computed the GeneralOperations class as a singleton since its quite expensive to instantiate each time.
     * @return
     * @throws UnrecoverableException
     */
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

    /**
     * Destroy the connection and exit.
     */
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
    /**
     * Standard exit. No errors occurred.
     */
    public static void exit() {
        // Helper.cleanup();
        Helper.notify("success", "Goodbye.", true);
        System.exit(0);
    }

    /**
     * Exit and throw an error back to the shell.
     */
    public static void error_exit() {
        Helper.notify("error", "Terminated due to unrecoverable error or SIGTERM.", true);
        // Helper.cleanup();
        System.exit(1);
    }

    /**
     * Catch a sigterm and handle it.
     */
    public static void sigterm() {
        Helper.notify("warn", "\nPreparing for graceful halt. Cleaning up and closing.", true);
        Helper.cleanup();
        // System.exit(1);
    }

    /**
     * Get a string from the terminal, and prompt the user with a message.
     * @param msg
     * @return
     */
    public static String get_string(final String msg) {
        Scanner scnr = new Scanner(System.in); 
        System.out.print(msg);
        String res = scnr.nextLine();
        if(res.equals("")){
            notify("warn", "Blank string is not allowed.", true);
            return get_string(msg);
        }
        else 
            return res;    
    }

    /**
     * Allow a blank string as input, and prompt the user with a message.
     */
    public static String get_string_allow(final String msg) {
        Scanner scnr = new Scanner(System.in); 
        System.out.print(msg);
        String res = scnr.nextLine();
        return res;    
    }

    /**
     * Get a date from input using the formatting provided validate it.
     * @param msg
     * @param fmt
     * @return
     */
    public static java.util.Date get_date(final String msg, final String fmt) {
        String date_str = Helper.get_string(msg);

        java.util.Date now = new java.util.Date();
        try{
            java.util.Date date = new SimpleDateFormat(fmt).parse(date_str);
            long diff = now.getTime() - date.getTime();
            if(diff <= 0) {
                notify("warn", "Your date is in the future/is now.", true);
                throw new Exception();
            }
            return date;  
        }catch(Exception e) {
            notify("warn", "Invalid date string entered.", true);
            return get_date(msg, fmt);
        }
    }

    /**
     * Get an email from input and perform naive validation.
     * @param msg
     * @return
     */
    public static String get_email(final String msg) {

        String res = get_string(msg);
        //this is obviously a terrible way to validate an email, and would best be handled by an external library.
        boolean hasAt = res.contains("@");
        boolean hasDot = res.contains(".");

        if(hasAt && hasDot) {
            return res;
        } else {
            notify("warn", "Invalid email entered.", true);
            return get_email(msg);
        }
              
    }

    /**
     * Allow a blank string to be accepted as an email.
     * @param msg
     * @return
     */
    public static String get_email_allow(final String msg) {

        String res = get_string_allow(msg);
        if(res.equals("")) {
            return "";
        }
        //this is obviously a terrible way to validate an email, and would best be handled by an external library.
        boolean hasAt = res.contains("@");
        boolean hasDot = res.contains(".");

        if(hasAt && hasDot) {
            return res;
        } else {
            notify("warn", "Invalid email entered.", true);
            return get_email_allow(msg);
        }
              
    }

    /**
     * Prompt the user for a password/anything not wanting to be displayed on screen.
     * @param msg
     * @return
     */
    public static String prompt_sensitive(final String msg) {
        Console console = System.console(); 
        String password = new String(console.readPassword(msg));
        //no limitation on blank string here.
        return password;
    }

    /**
     * Get an int from input.
     * @param msg
     * @return
     */
    public static Integer get_int(final String msg) {
        Scanner scnr = new Scanner(System.in); 
        System.out.println(msg);
        if(scnr.hasNextInt()) 
            return scnr.nextInt();
        else {
            Helper.notify("error", "You did not enter an integer. Try again.", true);
            return get_int(msg);
        }  
    }

    /**
     * Get a double from input, and perform validation.
     * @param msg
     * @return
     */
    public static Double get_double(final String msg) {
        Scanner scnr = new Scanner(System.in); 
        System.out.print(msg);
        if(scnr.hasNextDouble()) 
            return scnr.nextDouble();
        else {
            Helper.notify("error", "You did not enter a valid amount. Try again.", true);
            return get_double(msg);
        }  
    }

    /**
     * Confirm the user's wish to continue.
     * @return
     */
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

    /**
     * Confirm a statement passed as a message.
     * @param msg
     * @return
     */
    public static boolean confirm(final String msg) {
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

    /**
     * Get a numeric choice. Used in promptmap.
     * @return
     */
    public static Integer prompt_choice() {
        Scanner scnr = new Scanner(System.in); 
        System.out.print("Enter number of your choice: ");
        if(scnr.hasNextInt()) 
            return scnr.nextInt();
        else 
            return null;

    }

    /**
     * Provide an easy way to prompt the user with a list of choice.
     * Used extensively throughout project.
     * Original function is a carryover from previous Homework.
     * @param results
     * @param msg
     * @return String
     */
    public static final String get_choice(final HashMap<Integer, String> results, String msg) {
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

    /**
     * Return a colored string to the user. 
     * @param color
     * @param msg
     * @param n
     * @return
     */
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

    /**
     * Print a colored number to the user.
     * @param color
     * @param ms
     * @param n
     */
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

    /**
     * Return a colored number to the user.
     * @param color
     * @param ms
     * @param n
     * @return
     */
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

    /**
     * Return a colored double to the user.
     * @param color
     * @param ms
     * @param n
     * @return
     */
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