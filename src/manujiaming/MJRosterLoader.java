package manujiaming;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MJRosterLoader {
    private static final String ROSTER = "rosters";


    public static class Roster {
        private final String rosterName;
        private ArrayList<MJStudent> students;

        public Roster(String rosterName, ArrayList<MJStudent> students) {
            this.rosterName = rosterName;
            this.students = students;
        }

        public String getRosterName() {
            return rosterName;
        }

        public ArrayList<MJStudent> getUsers() {
            return students;
        }
    }

    public ArrayList<Roster> genRosters() {
        ArrayList<Roster> ret = new ArrayList<>();

        //get all the files in the roster directory
        File[] rosterFiles = new File(ROSTER).listFiles();

        //generate a single roster
        for (File f : rosterFiles) {
            if (isRosterFile(f)) {
                ret.add(this.createRosterFromFile(f));
            }
        }

        return ret;
    }

    //basic checks
    private boolean isRosterFile(File f) {
        return f.exists() && f.isFile() && f.getName().endsWith(".txt");
    }

    private Roster createRosterFromFile(File f) {
        Scanner in;
        try {
            in = new Scanner(f);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ArrayList<MJStudent> students = new ArrayList<>();
        while (in.hasNextLine()) {
            String current = in.nextLine();
            students.add(this.processSingleStudent(current));
        }

        return new Roster(f.getName().substring(0, f.getName().length() - ".txt".length()), students);
    }

    /**
     *  EXPECTED FORMAT:
     *  Delimetter is tab
     *  Last First Student_ID Student_ID Grade email period
     *  Example
     *  Bhat Manu 5180246 5180246 12 mbhat246@student.fuhsd.org 1
     */
    private MJStudent processSingleStudent(String s) {
        String[] tokens = s.split("\t");

        String first = tokens[1];
        String last  = tokens[0];
        int studentId = Integer.parseInt(tokens[2]);
        String email = tokens[5];
        int period = Integer.parseInt(tokens[6]);
        //everything else is not used

        return new MJStudent(first, last, studentId, email, period);
    }

}
