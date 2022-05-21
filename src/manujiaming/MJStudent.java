package manujiaming;

import java.util.ArrayList;

public class MJStudent {

    /* optional */
    private String email;
    private String firstName, lastName;
    private int studentID;
    private int period;

    private String generatedComments;

    private ArrayList<MJSubmission> subs = new ArrayList<>();



    public MJStudent(String firstName, String lastName, int studentID, String email, int period) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentID = studentID;
        this.email = email;
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public String getGeneratedComments() {
        return generatedComments;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getStudentID() {
        return studentID;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<MJSubmission> getSubs() {
        return subs;
    }

    public void setGeneratedComments(String generatedComments) {
        this.generatedComments = generatedComments;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
