package jotformtool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Roster {
	private Student[] roster;
	private Map<String, Student> rosterByName;
	private Map<String, Student> rosterByNameUpperCaseNoSpace;
	private Map<String, Student> rosterByEmailLowerCase;
	
	
	public Roster(List<Student> students) {
		roster = new Student[students.size()];
		rosterByName = new HashMap<>();
		rosterByNameUpperCaseNoSpace = new HashMap<>();
		rosterByEmailLowerCase = new HashMap<>();
		int i = 0;
		for (Student s : students) {
			roster[i++] = s;
			rosterByName.put(nameKey(s.getLastName(), s.getFirstName()), s);
			rosterByNameUpperCaseNoSpace.put(nameKeyUpperNoSpace(s.getLastName(), s.getFirstName()), s);
			rosterByEmailLowerCase.put(s.getEmail().toLowerCase(), s);
		}
	}
	
	public int size() {
		return roster.length;
	}
	
	public Student[] getStudents() {
		return roster.clone();
	}
	
	public Student getStudent(String lastName, String firstName) {
		return rosterByName.get(nameKey(lastName, firstName));
	}
	
	public Student getStudentIgnoreCaseAndSpace(String lastName, String firstName) {
		return rosterByNameUpperCaseNoSpace.get(nameKeyUpperNoSpace(lastName, firstName));
	}
	
	public Student getStudentByEmail(String email) {
		return rosterByEmailLowerCase.get(email.toLowerCase());
	}
	
	// We can assume names won't legitimately contain any commas
	public static String nameKey(String lastName, String firstName) {
		return lastName + "," + firstName;
	}
	
	// We can assume names won't legitimately contain any commas
	public static String nameIdKey(String lastName, String firstName, String id) {
		return nameKey(lastName, firstName) + id;
	}
	
	// removes white space and makes all characters upper case in key
	public static String nameKeyUpperNoSpace(String lastName, String firstName) {
		return lastName.replaceAll("\\s", "").toUpperCase() + "," + firstName.replaceAll("\\s", "").toUpperCase();
	}
	
	// removes white space and makes all characters upper case in key
	public static String nameIdKeyUpperNoSpace(String lastName, String firstName, String id) {
		return nameKeyUpperNoSpace(lastName, firstName) + id;
	}
	
	public String getPeriod() {
		return roster[0].getPeriod();
	}

	@Override
	public String toString() {
		String str = "";
		for (Student s : roster) {
			str += s;
		}
		return str;
	}
}
