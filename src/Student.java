
public class Student implements Comparable<Student>{
	private String firstName;
	private String lastName;
	private String id;
	private String period;
	private String email;
	
	public Student(String firstName, String lastName, String id, String period, String email) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.id = id;
		this.period = period;
		this.email = email;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getId() {
		return id;
	}
	
	public String getPeriod() {
		return period;
	}
	
	public String getEmail() {
		return email;
	}

	@Override
	public int compareTo(Student o) {
		if (!getPeriod().equals(o.getPeriod()))
			return getPeriod().compareTo(o.getPeriod());
		else if (!getLastName().equals(o.getLastName()))
			return getLastName().compareTo(o.getLastName());
		else if (!getFirstName().equals(o.getFirstName()))
			return getFirstName().compareTo(o.getFirstName());
		else if (!getId().equals(o.getId()))
			return getId().compareTo(o.getId());
		return 0;
	}

	@Override
	public String toString() {
		return "[" + firstName + ", " + lastName + ", " + id + ", " + period + ", " + email + "]";
	}
	
	

}
