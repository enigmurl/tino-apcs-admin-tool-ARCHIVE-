package jotformtool;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.exceptions.CsvException;

public class SubmissionsDatabase {
	
	private String startDate;
	private String endDate;
	
	/* These header variables are used to link headers with jotformtool.Submission fields.
	 * For each of these header variables, we will look for a header cell matching
	 * or containing the string and store the index of that header in the
	 * headerMap with a key matching the header variable.
	 */
	private String dateHeader;
	private String firstNameHeader;
	private String lastNameHeader;
	private String periodHeader;
	private String emailHeader;
	private String uploadHeader;
	private String commentsHeader;
	private String formIdHeader; // only applicable to late/resubmit csv files
	
	private String[] header;
	private Map<String, Integer> headerMap;
	private Map<Integer, String> headersByIndex;
	private List<Submission> submissions;
	private Map<String, List<Submission>> submissionsByName;
	private Map<String, List<Submission>> submissionsByNameUpperNoSpace;
	private Map<String, List<Submission>> submissionsByPeriod;
	private Map<String, List<Submission>> submissionsByFormId;
	private Map<String, List<Submission>> submissionsByNameFormId;
	private Map<String, List<Submission>> submissionsByNameUpperNoSpaceFormId;
	private Map<String, List<Submission>> submissionsByEmailLowercase;

	// create a submissions database from a reference to a csv file
	public SubmissionsDatabase(File file, String startDate, String endDate, String dateHeader, String firstNameHeader,
			String lastNameHeader, String periodHeader, String emailHeader, String uploadHeader,
			String commentsHeader, String formIdHeader) throws IOException, ParseException, CsvException {
		this(FileUtil.readCSVFile(file), startDate, endDate, dateHeader, firstNameHeader,
				lastNameHeader, periodHeader, emailHeader, uploadHeader, commentsHeader, formIdHeader);
	}
	
	// Create a submissions database from the given table data
	public SubmissionsDatabase(List<String[]> tableData, String startDate, String endDate, String dateHeader, String firstNameHeader,
			String lastNameHeader, String periodHeader, String emailHeader, String uploadHeader,
			String commentsHeader, String formIdHeader) throws ParseException {
		
		this.startDate = startDate;
		this.endDate = endDate;
		
		// initialize header names
		this.dateHeader = dateHeader;
		this.firstNameHeader = firstNameHeader;
		this.lastNameHeader = lastNameHeader;
		this.periodHeader = periodHeader;
		this.emailHeader = emailHeader;
		this.uploadHeader = uploadHeader;
		this.commentsHeader = commentsHeader;
		this.formIdHeader = formIdHeader;
		
		// Create header map
		header = tableData.get(0);
		headerMap = new HashMap<>();
		headersByIndex = new HashMap<>();
		submissions = new ArrayList<>();
		submissionsByName = new HashMap<>();
		submissionsByNameUpperNoSpace = new HashMap<>();
		submissionsByPeriod = new HashMap<>();
		submissionsByFormId = new HashMap<>();
		submissionsByNameFormId = new HashMap<>();
		submissionsByNameUpperNoSpaceFormId = new HashMap<>();
		submissionsByEmailLowercase = new HashMap<>();
		
		// required map headers (will throw exceptions if missing or invalid)
		mapSubmissionHeader(header, dateHeader, true);
		mapSubmissionHeader(header, firstNameHeader, true);
		mapSubmissionHeader(header, lastNameHeader, true);
		mapSubmissionHeader(header, periodHeader, true);
		
		// optional submission headers (no need to throw errors if missing)
		mapSubmissionHeader(header, emailHeader, false);
		mapSubmissionHeader(header, uploadHeader, false);
		mapSubmissionHeader(header, commentsHeader, false);
		mapSubmissionHeader(header, formIdHeader, false);
		
		for (int i = 0; i < header.length; i++) {
			if (!headersByIndex.containsKey(i)) {
				headerMap.put(header[i], i);
				headersByIndex.put(i, header[i]);
			}
		}
		GregorianCalendar startD = rangeDateFromStr(startDate, 0, 0);
		GregorianCalendar endD = rangeDateFromStr(endDate, 23, 59);
		System.out.println("START DATE: " + startDate + " --> " + startD.getTime());
		System.out.println("END DATE: " + endDate + " --> " + endD.getTime());
		for (int i = 1; i < tableData.size(); i++) {
			String[] row = tableData.get(i);
			GregorianCalendar subD = submissionDateFromStr(row[getDateIndex()]);
			//System.out.println(row[getDateIndex()] + " --> " + subD.getTime());
			if (subD.compareTo(startD) >= 0 && 
					subD.compareTo(endD) <= 0) {
				addSubmission(new Submission(row, this));
			}
		}
		Collections.sort(submissions);
		sortListsInMap(submissionsByName);
		sortListsInMap(submissionsByNameUpperNoSpace);
		sortListsInMap(submissionsByPeriod);
		sortListsInMap(submissionsByFormId);
		sortListsInMap(submissionsByNameFormId);
		sortListsInMap(submissionsByNameUpperNoSpaceFormId);
		sortListsInMap(submissionsByEmailLowercase);
	}
	
	// convert a start/end date string to GregorianCalendar instance
		public GregorianCalendar dueDateFromStr(String str) {
			String[] dateTimeSplit = str.split(" ");
			String[] sds = dateTimeSplit[0].split("-");
			int yr = Integer.parseInt(sds[0]);
			int month = Integer.parseInt(sds[1]) - 1;
			int day = Integer.parseInt(sds[2]);
			String[] timeSplit = dateTimeSplit[1].split(":");
			int hour = Integer.parseInt(timeSplit[0]);
			int minute = Integer.parseInt(timeSplit[1]);
			return new GregorianCalendar(yr, month, day, hour, minute);
		}
	
	// convert a start/end date string to GregorianCalendar instance
	public GregorianCalendar rangeDateFromStr(String str, int hour, int minute) {
		String[] sds = str.split("-");
		int yr = Integer.parseInt(sds[0]);
		int month = Integer.parseInt(sds[1]) - 1;
		int day = Integer.parseInt(sds[2]);
		return new GregorianCalendar(yr, month, day, hour, minute);
	}

	// convert a submission date string to GregorianCalendar instance
	public GregorianCalendar submissionDateFromStr(String str) {
		str = str.trim();
		String dateSep = "/";
		if (str.contains("-")) {
			dateSep = "-";
		}
		String[] subDateTimeSplit = str.split(" ");
		String[] subDs = subDateTimeSplit[0].split(dateSep);
		int subMonth;
		int subDay;
		int subYear;
		if (subDs[0].length() == 4) {
			subYear = Integer.parseInt(subDs[0]);
			subMonth = Integer.parseInt(subDs[1]) - 1;
			subDay = Integer.parseInt(subDs[2]);
		} else {
			subMonth = Integer.parseInt(subDs[0]) - 1;
			subDay = Integer.parseInt(subDs[1]);
			subYear = Integer.parseInt(subDs[2]);
		}
		String[] subTimeSplit = subDateTimeSplit[1].split(":");
		int subHour = Integer.parseInt(subTimeSplit[0]);
		int subMin = Integer.parseInt(subTimeSplit[1]);
		return new GregorianCalendar(subYear, subMonth, subDay, subHour, subMin);
	}
	
	private <A extends Comparable<? super A>> void sortListsInMap(Map<String, List<A>> list) {
		for (Map.Entry<String, List<A>> entry : list.entrySet()) {
			Collections.sort(entry.getValue());
		}
	}
	
	private List<Submission> combineSubmissionLists(List<List<Submission>> lists) {
		Set<Submission> combined = new HashSet<>();
		for (List<Submission> subs : lists) {
			combined.addAll(subs);
		}
		return new ArrayList<>(combined);
	}
	
	public List<String[]> getSubmissionsTable(Roster roster, String dueDate, SubmissionsDatabase lateSubmissionsDb) {
		// jotformtool.Submission Date	First Name	Last Name	Period	E-mail	Any comments?	Did you name your zip file properly?  Ex: P3_Wang_Michael_Centipede.zip	Name your project folder PX_LastName_FirstName_Centipede. Then right click and Send To -> Compressed (zipped) folder. In a mac compressing is similar, using a right click, but has a slightly different option. Upload that file. You should end up with a zip file named PX_LastName_FirstName_Centipede.zip. For example if you were in 3rd period and named Michael Wang, then you would name the file P3_Wang_Michael_Centipede.zip
		// Oldest Date, Newest Date, First Name, Last Name, Period, E-mail, Id, Oldest Comments, Newest Comments, Oldest Uploads, Newest Uploads, Score, Grader Comments
		String oldestDateHeader = "Oldest Date";
		String newestDateHeader = "Newest Date";
		String firstNameHeader = "First Name";
		String lastNameHeader = "Last Name";
		String periodHeader = "Period";
		String emailHeader = "E-mail";
		String idHeader = "ID";
		String oldestCommentsHeader = "Oldest Comments";
		String newestCommentsHeader = "Newest Comments";
		String oldestUploadsHeader = "Oldest Uploads";
		String newestUploadsHeader = "Newest Uploads";
		String scoreHeader = "Score";
		String graderCommentsHeader = "Grader Comments";
		
		String[] outHeader = {oldestDateHeader, newestDateHeader, firstNameHeader,
				lastNameHeader, periodHeader, emailHeader, idHeader, oldestCommentsHeader,
				newestCommentsHeader, oldestUploadsHeader, newestUploadsHeader, scoreHeader,
				graderCommentsHeader};
		Map<String, Integer> outHeaderMap = new HashMap<>();
		for (int i = 0; i < outHeader.length; i++) {
			outHeaderMap.put(outHeader[i], i);
		}
		
		ArrayList<String[]> table = new ArrayList<>();
		table.add(outHeader);
		String formId = submissions.size() > 0 ? submissions.get(0).getFormId() : null;
		if (formId == null) {
			if (submissions.size() == 0) System.out.println("No submissions found. Check the start and end date range");
			System.out.println("Could not find form ID - late submits will not be included");
		}
		GregorianCalendar dueD = dueDateFromStr(dueDate);
		System.out.println("dueDate: " + dueDate + " --> " + dueD.getTime());
		Student[] students = roster.getStudents();
		for (Student stud : students) {
			String[] rowData = new String[outHeader.length];
			Arrays.fill(rowData, "");
			ArrayList<String> graderComments = new ArrayList<>();
			
			rowData[outHeaderMap.get(firstNameHeader)] = stud.getFirstName();
			rowData[outHeaderMap.get(lastNameHeader)] = stud.getLastName();
			rowData[outHeaderMap.get(periodHeader)] = stud.getPeriod();
			rowData[outHeaderMap.get(emailHeader)] = stud.getEmail();
			rowData[outHeaderMap.get(idHeader)] = stud.getId();
			
			List<List<Submission>> submissionLists = new ArrayList<>();
			List<Submission> subsByName = submissionsByNameUpperNoSpace.get(Roster.nameKeyUpperNoSpace(stud.getLastName(), stud.getFirstName())); 
			if (subsByName != null) submissionLists.add(subsByName);
			List<Submission> subsByEmail = submissionsByEmailLowercase.get(stud.getEmail().toLowerCase());
			if (subsByEmail != null) submissionLists.add(subsByEmail);
			if (formId != null && lateSubmissionsDb != null) {
				List<Submission> lateSubs = lateSubmissionsDb.submissionsByNameUpperNoSpaceFormId.get(Roster.nameIdKeyUpperNoSpace(stud.getLastName(), stud.getFirstName(), formId));
				if (lateSubs != null) submissionLists.add(lateSubs);
				List<Submission> lateSubsByEmail = lateSubmissionsDb.submissionsByEmailLowercase.get(stud.getEmail().toLowerCase());
				if (lateSubsByEmail != null) submissionLists.add(lateSubsByEmail);
			}
			List<Submission> subs = combineSubmissionLists(submissionLists);
			Collections.sort(subs);
			if (subs.size() > 0) {
				Submission firstSub = subs.get(0);
				rowData[outHeaderMap.get(scoreHeader)] = "10";
				rowData[outHeaderMap.get(oldestDateHeader)] = firstSub.getDate();
				rowData[outHeaderMap.get(oldestCommentsHeader)] = firstSub.getComments();
				rowData[outHeaderMap.get(oldestUploadsHeader)] = firstSub.getUpload();
				GregorianCalendar subDate = submissionDateFromStr(firstSub.getDate());
				if (subDate.compareTo(dueD) > 0) {
					graderComments.add("Late");
				}
				
				Submission latestSub = subs.get(subs.size() - 1);
				
				
				rowData[outHeaderMap.get(newestDateHeader)] = latestSub.getDate();
				rowData[outHeaderMap.get(newestCommentsHeader)] = latestSub.getComments();
				rowData[outHeaderMap.get(newestUploadsHeader)] = latestSub.getUpload();
				
				if (!latestSub.getPeriod().equals(stud.getPeriod())) {
					graderComments.add("Submission period does not match roster period");
				}
				
				if (latestSub.getUpload().length() == 0) {
					graderComments.add("Latest submission contains no files");
				}else if (!latestSub.getUpload().toUpperCase().contains(stud.getFirstName().toUpperCase()) ||
						!latestSub.getUpload().toUpperCase().contains(stud.getLastName().toUpperCase())) {
					graderComments.add("Review File Names");
				}
				
				if (!submissionsByName.containsKey(Roster.nameKey(stud.getLastName(), stud.getFirstName()))) {
					System.out.println("None of the entries by " + stud.getFirstName() + " " + stud.getLastName() + " are an exact name match");
				}	
			} else {
				rowData[outHeaderMap.get(scoreHeader)] = "M";
				graderComments.add("Missing");
			}
			String comments = "";
			for (String comment : graderComments) {
				if (comments.length() == 0) comments += comment;
				else comments += ". " + comment;
			}
			rowData[outHeaderMap.get(graderCommentsHeader)] = comments;
			table.add(rowData);
		}
		
		List<Submission> periodSubmissions = submissionsByPeriod.get(roster.getPeriod());
		List<Submission> nonRosterSubmissions = new ArrayList<>();
		if (periodSubmissions != null) {
			for (Submission sub : periodSubmissions) {
				if (roster.getStudentIgnoreCaseAndSpace(sub.getLastName(), sub.getFirstName()) == null &&
						roster.getStudentByEmail(sub.getEmail()) == null) {
					nonRosterSubmissions.add(sub);
				}
			}
		}
		
		if (nonRosterSubmissions.size() > 0) {
			String[] rowData = new String[outHeader.length];
			Arrays.fill(rowData, "");
			rowData[0] = "Submissions not found on rosters";
			table.add(rowData);
			for (Submission sub : nonRosterSubmissions) {
				rowData = new String[outHeader.length];
				Arrays.fill(rowData, "");
				rowData[outHeaderMap.get(firstNameHeader)] = sub.getFirstName();
				rowData[outHeaderMap.get(lastNameHeader)] = sub.getLastName();
				rowData[outHeaderMap.get(periodHeader)] = sub.getPeriod();
				rowData[outHeaderMap.get(emailHeader)] = sub.getEmail();
				rowData[outHeaderMap.get(oldestDateHeader)] = sub.getDate();
				rowData[outHeaderMap.get(oldestCommentsHeader)] = sub.getComments();
				rowData[outHeaderMap.get(oldestUploadsHeader)] = sub.getUpload();
				table.add(rowData);
			}
		}
		return table;
	}
	
	private void addSubmission(Submission sub) {
		submissions.add(sub);
		String firstName = sub.getFirstName();
		String lastName = sub.getLastName();
		addSubmissionToMap(submissionsByName, Roster.nameKey(lastName, firstName), sub);
		addSubmissionToMap(submissionsByNameUpperNoSpace, Roster.nameKeyUpperNoSpace(lastName, firstName), sub);
		addSubmissionToMap(submissionsByPeriod, sub.getPeriod(), sub);
		String formId = sub.getFormId();
		if (formId != null) {
			addSubmissionToMap(submissionsByFormId, formId, sub);
			addSubmissionToMap(submissionsByNameFormId, Roster.nameIdKeyUpperNoSpace(lastName, firstName, formId), sub);
			addSubmissionToMap(submissionsByNameUpperNoSpaceFormId, Roster.nameIdKeyUpperNoSpace(lastName, firstName, formId), sub);
		}
		addSubmissionToMap(submissionsByEmailLowercase, sub.getEmail().toLowerCase(), sub);
	}
	
	private void addSubmissionToMap(Map<String, List<Submission>> map, String key, Submission sub) {
		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<>());
		}
		map.get(key).add(sub);
	}
	
	private void mapSubmissionHeader(String[] header, String headerName, boolean required) throws ParseException {
		if (isValidHeader(headerName)) {
			int index = getArrayColumnForField(header, headerName);
			if (index != -1) {
				if (headersByIndex.containsKey(index)) {
					throw new ParseException("Attempted to map both \"" + headerName + "\" and \"" + headersByIndex.get(index) + "\" to the same index (" + index + ")", 0);
				}
				headerMap.put(headerName, index);
				headersByIndex.put(index, headerName);
			} else if (required) {
				throw new ParseException("Could not determine index of required header \"" + dateHeader + "\" in header " + Arrays.toString(header), 0);
			}
		} else if (required) {
			throw new IllegalArgumentException("Invalid header name for required header: \"" + headerName + "\". State of headerMap = " + headerMap);
		}
	}
	
	private static boolean isValidHeader(String name) {
		return name != null && name.trim().length() > 0;
	}

	/**
	 * Given an array containing the CSV header (ex: Date FirstName LastName Period ...)
	 * this method returns the array column that fieldTitle appears in or -1 if the
	 * fieldTitle cannot be found.
	 */
	private static int getArrayColumnForField(String[] arr, String fieldTitle) {
		// Search for an exact match
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].trim().compareToIgnoreCase(fieldTitle.trim()) == 0)
				return i;
		}
		// Search for a partial match
		// using startsWith rather than contains since contains
		// results in false positives (i.e. Upload in late submit form)
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].trim().toLowerCase().startsWith(fieldTitle.trim().toLowerCase()))
				return i;
		}
		// Nothing fits
		return -1;
	}

	public String getDateHeader() {
		return dateHeader;
	}

	public String getFirstNameHeader() {
		return firstNameHeader;
	}

	public String getLastNameHeader() {
		return lastNameHeader;
	}

	public String getPeriodHeader() {
		return periodHeader;
	}

	public String getEmailHeader() {
		return emailHeader;
	}

	public String getUploadHeader() {
		return uploadHeader;
	}
	
	public String getCommentsHeader() {
		return commentsHeader;
	}
	
	public String getFormIdHeader() {
		return formIdHeader;
	}
	
	public int getDateIndex() {
		return getIndexOfField(dateHeader);
	}

	public int getFirstNameIndex() {
		return getIndexOfField(firstNameHeader);
	}

	public int getLastNameIndex() {
		return getIndexOfField(lastNameHeader);
	}

	public int getPeriodIndex() {
		return getIndexOfField(periodHeader);
	}

	public int getEmailIndex() {
		return getIndexOfField(emailHeader);
	}

	public int getUploadIndex() {
		return getIndexOfField(uploadHeader);
	}
	
	public int getCommentsIndex() {
		return getIndexOfField(commentsHeader);
	}
	
	public int getFormIdIndex() {
		return getIndexOfField(formIdHeader);
	}
	
	public int getIndexOfField(String headerName) {
		if (headerName == null || !headerMap.containsKey(headerName)) return -1;
		return headerMap.get(headerName);
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public List<Submission> getSubmissions() {
		return new ArrayList<>(submissions);
	}
	
	public List<Submission> getSubmissionsByFormId(String formId) {
		List<Submission> submissions = new ArrayList<>();
		List<Submission> list = submissionsByFormId.get(formId);
		if (list != null) submissions.addAll(list);
		return submissions;
	}

	public List<Submission> getSubmissionsByPeriod(String period) {
		List<Submission> submissions = new ArrayList<>();
		List<Submission> list = submissionsByPeriod.get(period);
		if (list != null) submissions.addAll(list);
		return submissions;
	}
	
	public List<Submission> getSubmissionsByPeriodFormId(String period, String formId) {
		List<Submission> submissions = getSubmissionsByFormId(formId);
		List<Submission> filtered = new ArrayList<>();
		for (Submission sub : submissions) {
			if (sub.getPeriod().equals(period)) {
				filtered.add(sub);
			}
		}
		return filtered;
	}
	
	public Map<String, String> getUrlsMap(SubmissionsDatabase lateSubmitDb, List<Roster> rosters) {
		Map<String, String> urlsMap = new HashMap<>();
		for (Roster roster : rosters) {
			String period = roster.getPeriod();
			List<Submission> submissions = getSubmissionsByPeriod(period);
			String formId = null;
			if (submissions.size() > 0) formId = submissions.get(0).getFormId();
			if (formId != null && lateSubmitDb != null) {
				 submissions.addAll(lateSubmitDb.getSubmissionsByPeriodFormId(period, formId));
			}
			Collections.sort(submissions);
			// make newer submissions come first (will already be sorted by date)
			Collections.reverse(submissions);
			for (Submission sub : submissions) {
				String line = sub.getUpload();
				String[] urls = line.split("[|\n]");
				for (String url : urls) {
					url = url.trim();
					if (url.startsWith("http")) {
						String urlDec = url;
						try {
							urlDec = URLDecoder.decode(url, FileUtil.UTF8);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						// if there is no "/" this can't possibly be a valid url so OOB crash is ok
						String fileName = urlDec.substring(urlDec.lastIndexOf("/") + 1);
						if (!urlsMap.containsKey(fileName)) {
							urlsMap.put(fileName, url);
						}
					} else {
						System.out.println("Invalid url: " + url);
					}
				}
			}
		}
		return urlsMap;
	}
}
