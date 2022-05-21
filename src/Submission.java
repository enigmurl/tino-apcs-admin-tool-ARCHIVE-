import java.util.Arrays;

public class Submission implements Comparable<Submission> {
	private SubmissionsDatabase db;
	private String[] data;
	
	
	public Submission(String[] data, SubmissionsDatabase db) {
		this.data = data;
		this.db = db;
		trimData();
	}
	
	public void trimData() {
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i].trim();
		}
	}
	
	public String getField(String headerName) {
		int index = db.getIndexOfField(headerName);
		if (index == -1) return null;
		return data[index];
	}
	
	public String getDate() {
		return getField(db.getDateHeader());
	}
	
	public String getFirstName() {
		return getField(db.getFirstNameHeader());
	}
	
	public String getLastName() {
		return getField(db.getLastNameHeader());
	}
	
	public String getPeriod() {
		return getField(db.getPeriodHeader());
	}
	
	public String getEmail() {
		String val = getField(db.getEmailHeader());
		return val == null ? "" : val;
	}
	
	public String getUpload() {
		String val = getField(db.getUploadHeader());
		return val == null ? "" : val;
	}
	
	public String getComments() {
		String val = getField(db.getCommentsHeader());
		return val == null ? "" : val;
	}
	
	public String getFormId() {
		String formId = getField(db.getFormIdHeader());
		if (formId == null) {
			String uploadUrl = getUpload();
			if (uploadUrl != null) {
				formId = getFormIdFromUrl(uploadUrl);
			}
		}
		return formId;
	}
	
	// parses a jotform upload URL to get the form ID
	// Example: https://www.jotform.com/uploads/MrFerrante/70618888581168/4296886084528227151/P4_Lee_Jane_GridView.java
	// Form ID = 70618888581168
	private String getFormIdFromUrl(String url) {
		String[] urlSplit = url.split("/");
		if (urlSplit.length >= 6) {
			return urlSplit[5];
		}
		return null;
	}

	public SubmissionsDatabase getDb() {
		return db;
	}

	public String[] getData() {
		return data;
	}

	@Override
	public int compareTo(Submission o) {
		return getDate().compareTo(o.getDate());
	}

	@Override
	public String toString() {
		return Arrays.toString(data);
	}
}
