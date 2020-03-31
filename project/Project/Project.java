import java.sql.*;  
import java.io.*;
import java.util.*;
class Project{  

    private static String username;
    private static String password;

    public static String prompt_username() {
        Scanner scnr = new Scanner(System.in); 
        System.out.println("Enter username: ");
        String username = scnr.nextLine();
        // scnr.close();
        return username;
    }

    public static String prompt_password() {
        Scanner scnr = new Scanner(System.in); 
        System.out.println("Enter password: ");
        String password = scnr.nextLine();
        // scnr.close();
        return password;
    }

    public static String prompt_department() {
        Scanner scnr = new Scanner(System.in); 
        System.out.println("Enter department (partial names are allowed): ");
        String dept = scnr.nextLine();
        // scnr.close();
        if(dept.equals("")) {
            return prompt_department();
        }
        return dept;
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

    public static HashMap<Integer, String> check_department(String dept_name, Connection con) throws SQLException {
        ResultSet result;
        String query = "SELECT dept_name from department WHERE UPPER(dept_name) LIKE UPPER(?)"; //perform a case insenstive search.
        PreparedStatement ps = con.prepareStatement(query);
        // System.out.println("\n\n\n\n"+dept_name+"\n\n\n\n");
        ps.setString(1, "%"+dept_name+"%");
        result = ps.executeQuery();
        HashMap<Integer, String> map = new HashMap<>();
        // System.out.println(result.next());
        if(!result.isBeforeFirst()) {
            System.out.println("No results returned. Would you like to retry?");
            if(prompt_retry()) {
                return check_department(prompt_department(), con);
            }
        } else {
            // System.out.println("did we eat the loop?");
            int count = 1;
            while(result.next()) {
                // System.out.println("here");
                String d_name = result.getString("dept_name");
                // System.out.printf("%-10s %-10s %-10s %-10s\n",inst_id, inst_name, student_id, student_name);
                map.put(count, d_name);
                count++;
            }
        return map;
        }
        return map;
    }

    private static Integer prompt_choice() {
        Scanner scnr = new Scanner(System.in); 
        System.out.println("Enter number: ");
        int c = scnr.nextInt();
        return c;
    }

    private static boolean has_instructors(String dept_name, Connection con) throws SQLException {
        ResultSet result;
        String query = "SELECT * from department NATURAL JOIN instructor where dept_name = ?"; //perform a case insenstive search.
        PreparedStatement ps = con.prepareStatement(query);
        // System.out.println("\n\n\n\n"+dept_name+"\n\n\n\n");
        ps.setString(1, dept_name);
        result = ps.executeQuery();
        return !(!result.next());
    }
    private static String get_choice(HashMap<Integer, String> results) {
        System.out.println("Choose a department from the results below by entering the corresponding number.");
        System.out.println();
        for (Integer key : results.keySet()) {
            String dept_name = results.get(key);
            System.out.printf("%-10s %-10s\n", key.toString()+"].", dept_name);
        }
        Integer choice = prompt_choice();
        if(choice > results.size() || choice <= 0) {
            System.out.println("Invalid choice entered. Try again.");
            System.out.println();
            return get_choice(results);
        } else {
            return results.get(choice);
        }
    }
    public static void main(String args[]) {
        String username;
        String password;
        if(args != null && args.length == 2) {
            username = args[0];
            password = args[1];
        } else {
            username = prompt_username();
            password = prompt_password();
        }
        String host = "edgar1.cse.lehigh.edu";
        AdvList.username = username;
        AdvList.password = password;
        try (Connection con=DriverManager.getConnection("jdbc:oracle:thin:@"+host+":1521:cse241",username,password);Statement s=con.createStatement();) {
            String dept = prompt_department();
            // System.out.println(dept);
            String query;
            ResultSet result;
            HashMap<Integer, String> d_results = check_department(dept, con);
            String chosen_dept_name = get_choice(d_results);
            if(!has_instructors(chosen_dept_name, con)) {
                System.out.println(chosen_dept_name + " has no instructors.");
                return;
            }
            query = "SELECT to_char(advisor.i_id, \'00009\') instructorID, instructor.name instructorName, to_char(advisor.s_id, \'00009\') adviseeID, student.name adviseeName FROM advisor JOIN instructor on advisor.i_id = instructor.id JOIN student on student.id = advisor.s_id WHERE instructor.dept_name = ? ORDER BY to_char(advisor.i_id, \'00009\'),advisor.s_id";
            String query_nob = "SELECT to_char(advisor.i_id, \'00009\') instructorID, instructor.name instructorName, to_char(advisor.s_id, \'00009\') adviseeID, student.name adviseeName FROM advisor JOIN instructor on advisor.i_id = instructor.id JOIN student on student.id = advisor.s_id WHERE instructor.dept_name = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, chosen_dept_name);
            result = ps.executeQuery();
            if (!result.isBeforeFirst()) {
                System.out.println("No results returned for "+chosen_dept_name+". Would you like to retry?");
                if(prompt_retry()) {
                    String[] arg = new String[2];
                    arg[0] = username;
                    arg[1] = password;
                    main(arg);
                } 
            } else {
                System.out.println();
                System.out.printf("%-10s %-10s %-10s %-10s\n", "instructorID", "instructorName", "adviseeID", "adviseeName");
                System.out.println();
            }
            int count = 0;
            while(result.next()) {
                String inst_id = result.getString("instructorID");
                String inst_name = result.getString("instructorName");
                String student_id = result.getString("adviseeID");
                String student_name = result.getString("adviseeName");
                System.out.printf("%-10s %-10s %-10s %-10s\n",inst_id, inst_name, student_id, student_name);
                count++;
                
            }
            System.out.println("Printed "+count+" records");
            String query_null = "(SELECT to_char(advisor.i_id, '00009') instructorID, instructor.name instructorName, to_char(advisor.s_id, '00009') adviseeID, student.name adviseeName " +
            "FROM instructor LEFT OUTER JOIN advisor ON instructor.id = advisor.i_id LEFT OUTER JOIN student ON student.id = advisor.s_id" +
            " WHERE instructor.dept_name = ? and advisor.s_id is NULL) MINUS ("+query_nob+")";
            // System.out.println(query_null);
            PreparedStatement ps_null = con.prepareStatement(query_null);
            ps_null.setString(1, chosen_dept_name);
            ps_null.setString(2, chosen_dept_name);
            result = ps_null.executeQuery();

            if (!result.isBeforeFirst()) {

            } else {
                System.out.println();
                System.out.printf("%-10s\n", "instructorName");
                System.out.println();
            }
            int count_null = 0;
            while(result.next()) {
                String inst_name = result.getString("instructorName");
                System.out.printf("%-10s\n",inst_name);
                count_null++;
                
            }
            System.out.println("Instructors who have no advisees count: "+count_null);
        } catch(Exception e) {
            String msg = e.getMessage();
            // e.printStackTrace();
            System.out.println(msg);
            if(msg.contains("01017")) {
                System.out.println();
                System.out.println("Invalid login credentials");
                System.out.println();
                main(null);
            } else if(msg.contains("IO Error")) {
                System.out.println("Failed to reach host: "+host);
            }
        }
    }
}