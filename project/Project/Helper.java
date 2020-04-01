import java.io.*;
import java.util.*;
import java.sql.*;  
class Helper {
    private static Connection con;
    public static void set_con(Connection c) {
        con = c;
    }
    
    public static Connection con() {
        return con;
    }

    public static void cleanup() {
        try {
            Helper.con.close();
            Helper.notify("green", "Successfully closed connection", true);
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
}