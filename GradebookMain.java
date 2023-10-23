import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

/**
 * PA2 class takes a csv file of students and their grades, and puts them into a details file and a summary file
 * @author Dillon Sherling
 * 10/16/23
 */
public class GradebookMain {
    private static final DecimalFormat decForm = new DecimalFormat("0.00"); // to make sure final grade decimals aren't too long

    /**
     * Main class sends user to all other methods to create files
     * @param args
     * @throws StringIndexOutOfBoundsException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws StringIndexOutOfBoundsException, FileNotFoundException , IOException{
        List<Integer> categoryTotals = new ArrayList<>();
        List<Integer> assignmentTotals = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        Hashtable<String, List<Integer>> categoryGrades = new Hashtable<>();
        Hashtable<String, List<Integer>> assignmentGrades = new Hashtable<>();
        Scanner s;
        List<String> files = userFiles(); // create all variables needed ^^

        try{
            for (String file : files) {
                s = new Scanner(new File("src/" + file)); // for scanning file, file path src/
                header(s, headers, categories, assignmentTotals, categoryTotals, file); // get headers
                data(s, assignmentGrades, categoryGrades); // get data
            }
            detailsFile(assignmentGrades, assignmentTotals,headers); // write details file
            summaryFile(categoryGrades, categoryTotals, categories); // write summary file
            System.out.println("Program has finished. New files saved as 'output.csv' and 'details.csv'");
        }
        catch(StringIndexOutOfBoundsException | FileNotFoundException e){
            System.out.println("ERROR: ONE OF YOUR FILES COULDN'T BE FOUND.");
        }
    }

    /**
     * takes user input and adds file names to an arraylist
     * @return list of all file names
     */
    public static List<String> userFiles() {
        Scanner console = new Scanner(System.in); // scanner for user input
        String file;
        List<String> files = new ArrayList<>(); // list to store file names
        System.out.println("Enter file names with format <category>_<number>.csv, then press enter");
        System.out.println("After you're done, type 'GO' then press enter");
        System.out.print("ENTER FILE NAME: ");
        file = console.next();
        while(file.compareToIgnoreCase("GO") != 0){ // if user types 'GO' then exit
            if (files.contains(file)) {
                System.out.println("File has already been put in.");
                System.out.print("ENTER FILE NAME: ");
                file = console.next();
            }
            else { // if file isn't contained in list already, add it and ask user for next input
                files.add(file);
                System.out.println("File added!");
                System.out.print("ENTER FILE NAME: ");
                file = console.next();
            }
        }
        return files;
    }

    /**
     * header class takes the top line and categories of csv file and stores them
     * @param s scanner for file
     * @param headers list of strings in top line
     * @param categories list of assignment categories
     * @param assignmentTotals possible amount of points per assignment
     * @param categoryTotals possible amount of points per category
     * @param file file we are scanning and storing headers of
     */
    public static void header(Scanner s, List<String> headers, List<String> categories, List<Integer> assignmentTotals, List<Integer> categoryTotals, String file) {
        String[] splitCategory = file.split("_");
        String category = splitCategory[0]; // category has been saved as string

        Scanner line = new Scanner(s.nextLine()); // scanner for specific line of file
        line.useDelimiter(","); // to split tokens up by , instead of a space, etc.

        if(categories.contains(category)) {
            return; // if category is already in the list, exit
        }

        categories.add(category); // else add to list
        String token = "";
        int count = 0;

        //get assignments and total spots
        while(line.hasNext()){
            token = line.next();
            if (count > 2) {
                headers.add(token); // only add to headers once we are out of "id" and "name" categories
            }
            else {
                count++;
            }
        }

        line = new Scanner(s.nextLine());
        line.useDelimiter(",");
        line.next();
        count = 0;
        // get possible points per assignment and category
        while (line.hasNext()){
            token = line.next();
            if (count == 1) {
                assignmentTotals.add(Integer.parseInt(token)); // if we've added the category already, start adding assignments
            }
            else {
                categoryTotals.add(Integer.parseInt(token));
                count ++;
            }
        }
    }

    /**
     * data class stores student info mapped to their scores
     * @param s Scanner for file
     * @param assignmentGrades // map from students to their individual assignment scores
     * @param categoryGrades // map from students to their category scores
     */
    public static void data(Scanner s, Hashtable<String, List<Integer>> assignmentGrades, Hashtable<String, List<Integer>> categoryGrades){
        Scanner line = new Scanner(s.nextLine()); // scan individual line of file
        line.useDelimiter(",");
        while(s.hasNextLine()) { // while file has more lines
            String nextLine;

            List<Integer> scores; // list of scores

            nextLine = s.nextLine(); // third line
            line = new Scanner(nextLine);
            line.useDelimiter(",");

            String studentInfo = line.next() + ", " + line.next() + " " + line.next() + ","; // first three values are ID, first, then last name

            // to add category grades to map
            if (categoryGrades.containsKey(studentInfo)) { // if the student is already in the map/system add new value to list in map
                scores = categoryGrades.get(studentInfo);
                scores.add(Integer.parseInt(line.next()));
                categoryGrades.replace(studentInfo, scores); // replace old map list with new list
            } else {
                scores = new ArrayList<>();
                scores.add(Integer.parseInt(line.next()));
                categoryGrades.put(studentInfo, scores); // add scores to list, add new key/value in map
            }

            // to add individual assignment grades to map
            if (assignmentGrades.containsKey(studentInfo)) { // if student is contained in map, add all their scores to list
                scores = assignmentGrades.get(studentInfo);
                while (line.hasNext()) {
                    scores.add(Integer.parseInt(line.next()));
                }
                assignmentGrades.replace(studentInfo, scores); // replace old list with new list
            } else {
                scores = new ArrayList<>();
                while (line.hasNext()) {
                    scores.add(Integer.parseInt(line.next()));
                }
                assignmentGrades.put(studentInfo, scores); // add new student and list to map
            }
        }
    }

    /**
     * finals takes all student grades, adds them up and finds a overall class grade
     * @param student student info
     * @param categoryGrades map from student to their category grades
     * @param categoryTotals list of total possible points per category
     * @return double class percentage
     */
    public static double finals(String student, Hashtable<String, List<Integer>> categoryGrades, List<Integer> categoryTotals){
        double points = 0;
        double total = 0;
        for(int i = 0; i < categoryTotals.size(); i++) {
            total += categoryTotals.get(i);
            points += categoryGrades.get(student).get(i); // add up all potential points and actual earned points
        }
        return (((points/total) * 100)); // return percentage
    }

    /**
     * summaryFile class writes data into a summary file with categoy grades and class grades
     * @param categoryGrades map from student to their category scores
     * @param categoryTotals list of all the total possible points per category
     * @param categories list of all the categories
     * @throws IOException
     */
    public static void summaryFile(Hashtable<String, List<Integer>> categoryGrades, List<Integer> categoryTotals, List<String> categories) throws IOException {
        File summary = new File("summary.csv");
        summary.createNewFile();
        FileWriter console = new FileWriter(summary); // to write to the new summary file

        String header = categories.toString();
        header = header.replace('[', ' ');
        header = header.replace(']', ' '); // removing brackets that come with the array toString method
        String assignmentLine = categoryTotals.toString();
        assignmentLine = assignmentLine.replace('[', ' ');
        assignmentLine = assignmentLine.replace(']', ' ');

        console.write("ID, Name, , Final Grade," + header + "\n"); // insert id, name, skip a column, insert final grade then header
        console.write(", Overall, , ," + assignmentLine + "\n"); // skip a column, add overall column, skip 2 columns then add assignments
        List<String> students = new ArrayList<>();
        Enumeration<String> store = categoryGrades.keys();
        while(store.hasMoreElements()){
            students.add(store.nextElement()); // create and store all student info into a list
        }

        for(int i = 0; i < students.size(); i++) {
            double grade = finals(students.get(i), categoryGrades, categoryTotals); // get final grade for each student
            String scoreList = categoryGrades.get(students.get(i)).toString();
            scoreList = scoreList.replace('[', ' ');
            scoreList = scoreList.replace(']', ' '); // string of all grades, remove brackets that come with array toString method
            console.write(students.get(i).replace('"', ' ') + "," + decForm.format(grade) + "%," + scoreList + "\n"); // insert student info, final grade to two decimal points, and then scores
        }
        console.close();
    }

    /**
     * detailsFile class writes data into a details file with all individual assignment scores
     * @param assignmentGrades map from students to their assignment scores
     * @param assignmentTotals list of potential points per assignment
     * @param headers list with all header categories
     * @throws IOException
     */
    public static void detailsFile(Hashtable<String, List<Integer>> assignmentGrades, List<Integer> assignmentTotals, List<String> headers) throws IOException {
        File details = new File("details.csv");
        details.createNewFile();
        FileWriter console = new FileWriter(details); // to write to new details file

        String scoreList;
        String header = headers.toString();
        header = header.replace('[', ' ');
        header = header.replace(']', ' '); // string of header categories, remove brackets that come with toString array method
        String assignmentLine = assignmentTotals.toString();
        assignmentLine = assignmentLine.replace('[', ' ');
        assignmentLine = assignmentLine.replace(']', ' '); // string of assingments, remove brackets

        console.write("ID,Name," + "," + header + "\n"); // insert id column, name column, skip a column then add headers
        console.write(", Overall," + "," + assignmentLine + "\n"); // skip a column, add overall column, skip a column then add assignments

        List<String> students = new ArrayList<>();
        Enumeration<String> store = assignmentGrades.keys();
        while(store.hasMoreElements()){
            students.add(store.nextElement()); // add all student names and id's to a list
        }

        for(int i = 0; i < students.size(); i++) {
            scoreList = assignmentGrades.get(students.get(i)).toString();
            scoreList = scoreList.replace('[', ' ');
            scoreList = scoreList.replace(']', ' '); // get string list of all assignment grades without brackets from toString array method
            console.write(students.get(i).replace('"', ' ') + "," + scoreList + "\n"); // insert student name and id, then the list of their scores
        }
        console.close();
    }
}

