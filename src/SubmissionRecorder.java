import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.opencsv.exceptions.CsvException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/* NOTE: Download the Excel spreadsheet and save as csv. Downloading jotform's csv file directly
 * leads to errors due to invalid format in some cases.
 * 
 */

public class SubmissionRecorder extends Application {

	private static final String PREF_FILE = "gatChooserPrefs";
	private static final String START_WINDOW_KEY = "startWindow";
	private static final String END_WINDOW_KEY = "endWindow";
	private static final String DUE_DATE_KEY = "dueDate";
	private static final String LAST_REPORT_PATH_KEY = "lastReportPath";
	private static final String LAST_DOWNLOAD_PATH_KEY = "lastDownloadPath";
	private static final String LAST_ROSTER_NAMES_KEY = "lastRosterNames";
	private static final String LAST_CSV_FILE_KEY = "lastTestRun";
	private static final String LAST_LATE_FILE_KEY = "lastLateFile";
	private static final String LAST_URLS_PATH_KEY = "lastUrlsFile";

	private static final String LAST_SUB_DATE_HEADER_KEY = "subDateHeader";
	private static final String LAST_SUB_FIRST_NAME_HEADER_KEY = "firstNameHeader";
	private static final String LAST_SUB_LAST_NAME_HEADER_KEY = "lastNameHeader";
	private static final String LAST_SUB_PERIOD_HEADER_KEY = "periodHeader";
	private static final String LAST_SUB_EMAIL_HEADER_KEY = "emailHeader";
	private static final String LAST_SUB_UPLOAD_HEADER_KEY = "uploadHeader";
	private static final String LAST_SUB_COMMENTS_HEADER_KEY = "commentsHeader";

	private static final String LAST_LATE_SUB_DATE_HEADER_KEY = "lateSubDateHeader";
	private static final String LAST_LATE_SUB_FIRST_NAME_HEADER_KEY = "lateFirstNameHeader";
	private static final String LAST_LATE_SUB_LAST_NAME_HEADER_KEY = "lateLastNameHeader";
	private static final String LAST_LATE_SUB_PERIOD_HEADER_KEY = "latePeriodHeader";
	private static final String LAST_LATE_SUB_EMAIL_HEADER_KEY = "lateEmailHeader";
	private static final String LAST_LATE_SUB_UPLOAD_HEADER_KEY = "lateUploadHeader";
	private static final String LAST_LATE_SUB_COMMENTS_HEADER_KEY = "lateCommentsHeader";
	private static final String LAST_LATE_SUB_FORM_ID_HEADER_KEY = "lateFormIdHeader";

	private static final String CSV_FILES_DIR_PATH = "csv_files";
	private static final String ROSTERS_DIR = "rosters";

	// Expected column titles in the CSV file
	private static final String SUBMISSION_DATE_HEADER = "Submission Date";
	private static final String SUBMISSION_FIRST_NAME_HEADER = "First Name";
	private static final String SUBMISSION_LAST_NAME_HEADER = "Last Name";
	private static final String SUBMISSION_PERIOD_HEADER = "Period";
	private static final String SUBMISSION_EMAIL_HEADER = "E-mail";
	private static final String SUBMISSION_UPLOAD_HEADER = "Upload";
	private static final String SUBMISSION_COMMENTS_HEADER = "Any comments";

	// Expected Column only for late form
	private static final String SUBMISSION_FORM_ID_HEADER = "Which assignment are you submitting? (choose one)";

	// Expected column indexes for the roster files (not CSVs)
	private static int ROSTER_LAST_NAME_INDEX = 0;
	private static int ROSTER_FIRST_NAME_INDEX = 1;
	private static int ROSTER_PERIOD_INDEX = 6;
	private static int ROSTER_ID_INDEX = 2;
	private static int ROSTER_EMAIL_INDEX = 5;

	private Stage stage;
	private Scene mainMenuScene;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private DatePicker dueDatePicker;

	private TextField dateHeaderField;
	private TextField firstNameHeaderField;
	private TextField lastNameHeaderField;
	private TextField periodHeaderField;
	private TextField emailHeaderField;
	private TextField uploadHeaderField;
	private TextField commentsHeaderField;

	private TextField lateDateHeaderField;
	private TextField lateFirstNameHeaderField;
	private TextField lateLastNameHeaderField;
	private TextField latePeriodHeaderField;
	private TextField lateEmailHeaderField;
	private TextField lateUploadHeaderField;
	private TextField lateCommentsHeaderField;
	private TextField lateFormIdHeaderField;

	public static void main(String[] args) {    	
		Locale.setDefault(Locale.US);
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		stage.setTitle("Jotform Tool");
		initUI();
		stage.show();
		stage.sizeToScene();
	}

	private void initUI() {

		JsonObject prefs = new JsonObject();
		try {
			prefs = getPrefs();
		} catch (JsonSyntaxException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Main menu scene
		VBox vbox = new VBox(5);
		vbox.setAlignment(Pos.CENTER);
		vbox.setStyle("-fx-padding: 10;");

		vbox.getChildren().add(new Label("Enter the corresponding headers from the submissions CSV file:"));
		HBox headersRow = new HBox();
		vbox.getChildren().add(headersRow);

		dateHeaderField = new TextField(prefs.has(LAST_SUB_DATE_HEADER_KEY) ? prefs.get(LAST_SUB_DATE_HEADER_KEY).getAsString() : SUBMISSION_DATE_HEADER);
		firstNameHeaderField = new TextField(prefs.has(LAST_SUB_FIRST_NAME_HEADER_KEY) ? prefs.get(LAST_SUB_FIRST_NAME_HEADER_KEY).getAsString() : SUBMISSION_FIRST_NAME_HEADER);
		lastNameHeaderField = new TextField(prefs.has(LAST_SUB_LAST_NAME_HEADER_KEY) ? prefs.get(LAST_SUB_LAST_NAME_HEADER_KEY).getAsString() : SUBMISSION_LAST_NAME_HEADER);
		periodHeaderField = new TextField(prefs.has(LAST_SUB_PERIOD_HEADER_KEY) ? prefs.get(LAST_SUB_PERIOD_HEADER_KEY).getAsString() : SUBMISSION_PERIOD_HEADER);
		emailHeaderField = new TextField(prefs.has(LAST_SUB_EMAIL_HEADER_KEY) ? prefs.get(LAST_SUB_EMAIL_HEADER_KEY).getAsString() : SUBMISSION_EMAIL_HEADER);
		uploadHeaderField = new TextField(prefs.has(LAST_SUB_UPLOAD_HEADER_KEY) ? prefs.get(LAST_SUB_UPLOAD_HEADER_KEY).getAsString() : SUBMISSION_UPLOAD_HEADER);
		commentsHeaderField = new TextField(prefs.has(LAST_SUB_COMMENTS_HEADER_KEY) ? prefs.get(LAST_SUB_COMMENTS_HEADER_KEY).getAsString() : SUBMISSION_COMMENTS_HEADER);
		headersRow.getChildren().add(new VBox(new Label("Date"), dateHeaderField));
		headersRow.getChildren().add(new VBox(new Label("First Name"), firstNameHeaderField));
		headersRow.getChildren().add(new VBox(new Label("Last Name"), lastNameHeaderField));
		headersRow.getChildren().add(new VBox(new Label("Period"), periodHeaderField));
		headersRow.getChildren().add(new VBox(new Label("E-mail"), emailHeaderField));
		headersRow.getChildren().add(new VBox(new Label("Upload"), uploadHeaderField));
		headersRow.getChildren().add(new VBox(new Label("Comments"), commentsHeaderField));

//		vbox.getChildren().add(new Label("Enter the corresponding headers from the late CSV file:"));
//		HBox lateHeadersRow = new HBox();
//		vbox.getChildren().add(lateHeadersRow);
//
//		lateDateHeaderField = new TextField(prefs.has(LAST_LATE_SUB_DATE_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_DATE_HEADER_KEY).getAsString() : SUBMISSION_DATE_HEADER);
//		lateFirstNameHeaderField = new TextField(prefs.has(LAST_LATE_SUB_FIRST_NAME_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_FIRST_NAME_HEADER_KEY).getAsString() : SUBMISSION_FIRST_NAME_HEADER);
//		lateLastNameHeaderField = new TextField(prefs.has(LAST_LATE_SUB_LAST_NAME_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_LAST_NAME_HEADER_KEY).getAsString() : SUBMISSION_LAST_NAME_HEADER);
//		latePeriodHeaderField = new TextField(prefs.has(LAST_LATE_SUB_PERIOD_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_PERIOD_HEADER_KEY).getAsString() : SUBMISSION_PERIOD_HEADER);
//		lateEmailHeaderField = new TextField(prefs.has(LAST_LATE_SUB_EMAIL_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_EMAIL_HEADER_KEY).getAsString() : SUBMISSION_EMAIL_HEADER);
//		lateUploadHeaderField = new TextField(prefs.has(LAST_LATE_SUB_UPLOAD_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_UPLOAD_HEADER_KEY).getAsString() : SUBMISSION_UPLOAD_HEADER);
//		lateCommentsHeaderField = new TextField(prefs.has(LAST_LATE_SUB_COMMENTS_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_COMMENTS_HEADER_KEY).getAsString() : SUBMISSION_COMMENTS_HEADER);
//		lateFormIdHeaderField = new TextField(prefs.has(LAST_LATE_SUB_FORM_ID_HEADER_KEY) ? prefs.get(LAST_LATE_SUB_FORM_ID_HEADER_KEY).getAsString() : SUBMISSION_FORM_ID_HEADER);
//		lateHeadersRow.getChildren().add(new VBox(new Label("Date"), lateDateHeaderField));
//		lateHeadersRow.getChildren().add(new VBox(new Label("First Name"), lateFirstNameHeaderField));
//		lateHeadersRow.getChildren().add(new VBox(new Label("Last Name"), lateLastNameHeaderField));
//		lateHeadersRow.getChildren().add(new VBox(new Label("Period"), latePeriodHeaderField));
//		lateHeadersRow.getChildren().add(new VBox(new Label("E-mail"), lateEmailHeaderField));
//		lateHeadersRow.getChildren().add(new VBox(new Label("Upload"), lateUploadHeaderField));
//		lateHeadersRow.getChildren().add(new VBox(new Label("Comments"), lateCommentsHeaderField));
//		lateHeadersRow.getChildren().add(new VBox(new Label("Form ID"), lateFormIdHeaderField));

		// Submission recorder scene
		HBox hbox = new HBox(20);
		hbox.setStyle("-fx-padding: 10;");

		String startWindow = null;
		String endWindow = null;
		String dueDate = null;

		if (!prefs.has(START_WINDOW_KEY)) {
			// If there is no key for start window then set the start date
			// to August of this year in yyyy-mm-dd format
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, 7);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String august = sdf.format(cal.getTime());
			startWindow = august;
		} else
			startWindow = prefs.get(START_WINDOW_KEY).getAsString();

		if (!prefs.has(END_WINDOW_KEY)) {
			// If there is no key for end window then set the end date
			// to today's date in yyyy-mm-dd format
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String today = sdf.format(cal.getTime());
			endWindow = today;
		} else
			endWindow = prefs.get(END_WINDOW_KEY).getAsString();

		if (!prefs.has(DUE_DATE_KEY)) {
			// If there is no key for the due date then set it
			// to today's date in yyyy-mm-dd format
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String today = sdf.format(cal.getTime());
			dueDate = today;
		} else
			dueDate = prefs.get(DUE_DATE_KEY).getAsString();

		LocalDate d = LocalDate.parse(startWindow);
		startDatePicker = new DatePicker(d);
		startDatePicker.valueProperty().addListener(new MyChangeListener());

		LocalDate d2 = LocalDate.parse(endWindow);        
		endDatePicker = new DatePicker(d2);
		endDatePicker.valueProperty().addListener(new MyChangeListener());
		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);

		Label checkInlabel = new Label("Start:");
		gridPane.add(checkInlabel, 0, 0);
		GridPane.setHalignment(checkInlabel, HPos.LEFT);
		gridPane.add(startDatePicker, 0, 1);

		Label checkInlabel2 = new Label("End:");
		gridPane.add(checkInlabel2, 1, 0);
		GridPane.setHalignment(checkInlabel2, HPos.LEFT);
		gridPane.add(endDatePicker, 1, 1);

		vbox.getChildren().add(new Label("Submission window:"));
		//        hbox.getChildren().add(gridPane);
		hbox.getChildren().addAll(checkInlabel, startDatePicker, checkInlabel2, endDatePicker);
		vbox.getChildren().add(hbox);

		Label checkInLabel3 = new Label("Due Date:");
		vbox.getChildren().add(checkInLabel3);
		LocalDate d3 = LocalDate.parse(dueDate);
		dueDatePicker = new DatePicker(d3);
		dueDatePicker.valueProperty().addListener(new MyChangeListener());
		vbox.getChildren().add(dueDatePicker);

		// Define the UI button
		Button downloadButton = new Button("Download from file listing urls");
		Button recordSubmissionsButton = new Button("Record Submissions from CSV");
		Button downloadAndRecordButton = new Button("Download & Record from CSV");

		// Event handlers for the buttons
		downloadButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						saveHeaderPrefs();
						File urlsFile = promptUrlsFile();
						if (urlsFile != null) {
							File downloadDir = promptDownloadDir();
							if (downloadDir != null) {
								downloadFiles(getUrlsFromFile(urlsFile), downloadDir);
							} else {
								System.out.println("Operation Aborted");
							}
						} else {
							System.out.println("Operation Aborted");
						}
					}
				});
			}
		});

		recordSubmissionsButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						saveHeaderPrefs();
						try {
							RecordingOptions options = promptRecordingOptions();
							if (options == null) {
								System.out.println("Operation Aborted");
							} else {
								recordSubmissions(buildCSVDatabase(options.getCsvFile(), false), buildCSVDatabase(options.getLateFile(), true), buildRosters(options.getRosterNames()), options.getResultsDir(), getDueDate());
							}
						} catch (JsonSyntaxException | IOException | ParseException | CsvException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		downloadAndRecordButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						saveHeaderPrefs();
						try {
							RecordingOptions options = promptRecordingOptions();
							if (options == null) {
								System.out.println("Operation Aborted");
							} else {
								File downloadDir = promptDownloadDir();
								if (downloadDir == null) {
									System.out.println("Operation Aborted");
								} else {
									downloadAndRecord(buildCSVDatabase(options.getCsvFile(), false), buildCSVDatabase(options.getLateFile(), true), buildRosters(options.getRosterNames()), options.getResultsDir(), getDueDate(), downloadDir);
								}
							}
						} catch (JsonSyntaxException | IOException | ParseException | CsvException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		// Add buttons to the UI
		vbox.getChildren().add(downloadButton);
		vbox.getChildren().add(recordSubmissionsButton);
		vbox.getChildren().add(downloadAndRecordButton);

		mainMenuScene = new Scene(vbox);
		stage.setScene(mainMenuScene);        
	}
	
	private void saveHeaderPrefs() {
		JsonObject prefs = new JsonObject();
		try {
			prefs = getPrefs();
		} catch (JsonSyntaxException | IOException e1) {
			e1.printStackTrace();
		}
		//prefs.addProperty(LAST_LATE_SUB_DATE_HEADER_KEY, lateDateHeaderField.getText());
		prefs.addProperty(LAST_SUB_DATE_HEADER_KEY, dateHeaderField.getText());
		//prefs.addProperty(LAST_LATE_SUB_FIRST_NAME_HEADER_KEY, lateFirstNameHeaderField.getText());
		prefs.addProperty(LAST_SUB_FIRST_NAME_HEADER_KEY, firstNameHeaderField.getText());
		//prefs.addProperty(LAST_LATE_SUB_LAST_NAME_HEADER_KEY, lateLastNameHeaderField.getText());
		prefs.addProperty(LAST_SUB_LAST_NAME_HEADER_KEY, lastNameHeaderField.getText());
		//prefs.addProperty(LAST_LATE_SUB_PERIOD_HEADER_KEY, latePeriodHeaderField.getText());
		prefs.addProperty(LAST_SUB_PERIOD_HEADER_KEY, periodHeaderField.getText());
		//prefs.addProperty(LAST_LATE_SUB_EMAIL_HEADER_KEY, lateEmailHeaderField.getText());
		prefs.addProperty(LAST_SUB_EMAIL_HEADER_KEY, emailHeaderField.getText());
		//prefs.addProperty(LAST_LATE_SUB_UPLOAD_HEADER_KEY, lateUploadHeaderField.getText());
		prefs.addProperty(LAST_SUB_UPLOAD_HEADER_KEY, uploadHeaderField.getText());
		//prefs.addProperty(LAST_LATE_SUB_COMMENTS_HEADER_KEY, lateCommentsHeaderField.getText());
		prefs.addProperty(LAST_SUB_COMMENTS_HEADER_KEY, commentsHeaderField.getText());
		//prefs.addProperty(LAST_LATE_SUB_FORM_ID_HEADER_KEY, lateFormIdHeaderField.getText());
		FileUtil.writeString(new File(PREF_FILE), prefs.toString());
	}

	private File promptDownloadDir() {
		JFileChooser mainChooser = null;
		JsonObject prefs = null;
		try {
			prefs = getPrefs();
			if (!prefs.has(LAST_DOWNLOAD_PATH_KEY)) {
				mainChooser = new JFileChooser();
				System.out.println("Did not find LAST_DOWNLOAD_PATH_KEY");
			} else {
				mainChooser = new JFileChooser(prefs.get(LAST_DOWNLOAD_PATH_KEY).getAsString());
				System.out.println(prefs.get(LAST_DOWNLOAD_PATH_KEY).getAsString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}    	

		File saveDir = null;
		// choose a directory to save the results
		mainChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		mainChooser.setDialogTitle("Choose directory to save files in.");
		int retVal = mainChooser.showOpenDialog(null);
		if(retVal == JFileChooser.APPROVE_OPTION) {
			saveDir = mainChooser.getSelectedFile();
			prefs.addProperty(LAST_DOWNLOAD_PATH_KEY, saveDir.getAbsolutePath());
			FileUtil.writeString(new File(PREF_FILE), prefs.toString());
		} else {
			System.out.println("Action cancelled...");
		}

		return saveDir;
	}

	private File promptUrlsFile() {
		JFileChooser mainChooser = null;
		JsonObject prefs = null;
		try {
			prefs = getPrefs();
			if (!prefs.has(LAST_URLS_PATH_KEY)) {
				mainChooser = new JFileChooser();
				System.out.println("Did not find LAST_URLS_PATH_KEY");
			} else {
				mainChooser = new JFileChooser(prefs.get(LAST_URLS_PATH_KEY).getAsString());
				System.out.println(prefs.get(LAST_URLS_PATH_KEY).getAsString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}    	

		File urlsFile = null;
		// choose a urls file
		mainChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		mainChooser.setDialogTitle("Choose file containing URLS.");
		int retVal = mainChooser.showOpenDialog(null);
		if(retVal == JFileChooser.APPROVE_OPTION) {
			urlsFile = mainChooser.getSelectedFile();
			prefs.addProperty(LAST_URLS_PATH_KEY, urlsFile.getParentFile().getAbsolutePath());
			FileUtil.writeString(new File(PREF_FILE), prefs.toString());
		} else {
			System.out.println("Action cancelled...");
		}

		return urlsFile;
	}

	private void downloadAndRecord(SubmissionsDatabase submissionDatabase, SubmissionsDatabase lateSubmitDb, List<Roster> rosters, File resultsDir, String dueDate, File downloadDir) {
		recordSubmissions(submissionDatabase, lateSubmitDb, rosters, resultsDir, dueDate);
		List<Exception> exceptions = new ArrayList<>();
		Map<String, String> urlsMap = submissionDatabase.getUrlsMap(lateSubmitDb, rosters);
		downloadFiles(urlsMap, downloadDir);
		if (exceptions.size() > 0) {
			System.out.println("The following exceptions were encountered: ");
			for (Exception err : exceptions) {
				err.printStackTrace();
			}
		}		
	}

	private static JsonObject getPrefs() throws JsonSyntaxException, FileNotFoundException, IOException {
		File prefFile = new File(PREF_FILE);
		if(prefFile.exists() && !prefFile.isDirectory()) { 
			return FileUtil.getObj(PREF_FILE).getAsJsonObject();
		}
		return new JsonObject(); // no prefs file
	}

	private static File promptCSVFile(String prompt, String prefKey) throws JsonSyntaxException, FileNotFoundException, IOException {
		JsonObject prefs = getPrefs();

		// Choose a CSV file to load
		File csvDir = new File(CSV_FILES_DIR_PATH);
		File[] csvFiles = csvDir.listFiles();
		ArrayList<String> csvFileNames = new ArrayList<>();
		for (int i = 0; i < csvFiles.length; i++) {
			if (csvFiles[i].getName().toLowerCase().endsWith(".csv")) csvFileNames.add(csvFiles[i].getName());
		}

		String lastRunFileName = prefs.has(prefKey) ? prefs.get(prefKey).getAsString() : null;
		String initialSelection = lastRunFileName != null && csvFileNames.contains(lastRunFileName) ? lastRunFileName : csvFileNames.get(0);
		System.out.println("Prompting options");
		String fileSelected = (String)JOptionPane.showInputDialog(null, prompt,
				"Load CSV file", JOptionPane.QUESTION_MESSAGE, null, csvFileNames.toArray(), initialSelection);
		if (fileSelected == null) {
			return null;
		}

		prefs.addProperty(prefKey, fileSelected);
		FileUtil.writeString(new File(PREF_FILE), prefs.toString());

		String path = CSV_FILES_DIR_PATH + File.separator + fileSelected;
		System.out.println("Selected" + prefKey + ": " + path);
		return new File(path);
	}

	private static List<String> promptRosterNames() throws JsonSyntaxException, FileNotFoundException, IOException {
		JsonObject prefs = getPrefs();
		// Choose which rosters to use
		JsonArray lastRosterNames = prefs.has(LAST_ROSTER_NAMES_KEY) && prefs.get(LAST_ROSTER_NAMES_KEY).isJsonArray() ? prefs.get(LAST_ROSTER_NAMES_KEY).getAsJsonArray() : null;
		JPanel gui = new JPanel(new BorderLayout());

		File[] rosterFiles = new File(ROSTERS_DIR).listFiles();
		//String[] rosterNames = new String[rosterFiles.length];
		ArrayList<String> rosterNames = new ArrayList<>();
		for (int i = 0; i < rosterFiles.length; i++) {
			if (rosterFiles[i].getName().toLowerCase().endsWith(".txt")) rosterNames.add(rosterFiles[i].getName());
		}
		JList<String> list = new JList<String>(rosterNames.toArray(new String[rosterNames.size()]));
		List<Integer> preSelectedRosters = new ArrayList<>();
		if (lastRosterNames != null) {
			for (JsonElement el : lastRosterNames) {
				String rosName = el.getAsString();
				if (rosName != null) {
					for (int i = 0; i < list.getModel().getSize(); i++) {
						if (rosName.equals(list.getModel().getElementAt(i))) {
							preSelectedRosters.add(i);
						}
					}
				}
			}
		}
		int[] selectedIndices = new int[preSelectedRosters.size()];
		for (int i = 0; i < preSelectedRosters.size(); i++) {
			selectedIndices[i] = preSelectedRosters.get(i);
		}
		//System.out.println("Preselected Indices:" + Arrays.toString(selectedIndices));
		list.setSelectedIndices(selectedIndices);
		gui.add(new JScrollPane(list));
		JOptionPane.showMessageDialog(null, gui, "Which rosters would you like to use?", JOptionPane.QUESTION_MESSAGE);
		List<String> chosenRosterNames = (List<String>)list.getSelectedValuesList();
		//System.out.println("Selected Rosters: " + chosenRosterNames);
		JsonArray lastRosters = new JsonArray();
		for (String rosName : chosenRosterNames) {
			lastRosters.add(rosName);
		}

		prefs.add(LAST_ROSTER_NAMES_KEY, lastRosters);
		FileUtil.writeString(new File(PREF_FILE), prefs.toString());

		return chosenRosterNames;
	}

	// choose a directory to save the results
	private static File promptResultsDir() throws JsonSyntaxException, FileNotFoundException, IOException {
		JsonObject prefs = getPrefs();
		File resultsDir = null;
		JFileChooser mainChooser = null;
		if (prefs == null || !prefs.has(LAST_REPORT_PATH_KEY)) {
			mainChooser = new JFileChooser();
			if (prefs == null) {
				prefs = new JsonObject();
			}
		} else {
			mainChooser = new JFileChooser(prefs.get(LAST_REPORT_PATH_KEY).getAsString());
		}
		mainChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		mainChooser.setDialogTitle("Choose directory for reports.");
		int retVal = mainChooser.showOpenDialog(null);
		if(retVal == JFileChooser.APPROVE_OPTION) {
			resultsDir = mainChooser.getSelectedFile();
		} else {
			System.out.println("retVal = " + retVal);
		}

		if (resultsDir == null) {
			return null;
		}

		prefs.addProperty(LAST_REPORT_PATH_KEY, resultsDir.getAbsolutePath());
		FileUtil.writeString(new File(PREF_FILE), prefs.toString());
		return resultsDir;
	}

	private String getDueDate() {
		// Due date is next day at 0:5:00 AM
		LocalDate nextDay = dueDatePicker.getValue().plusDays(1);
		Calendar cal = Calendar.getInstance();
		cal.set(nextDay.getYear(), nextDay.getMonthValue()-1, nextDay.getDayOfMonth(), 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss");
		return sdf.format(cal.getTime());
	}

	private void recordSubmissions(SubmissionsDatabase submissionDatabase, SubmissionsDatabase lateSubmitDb, List<Roster> rosters, File resultsDir, String dueDate) {
		for (Roster roster : rosters) {
			JsonObject prefs = null;
			try {
				prefs = getPrefs();
			} catch (JsonSyntaxException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String resultsPath = StringUtil.removeJSONquotes(prefs.get(LAST_REPORT_PATH_KEY).toString());
			String csvFileName = StringUtil.removeJSONquotes(prefs.get(LAST_CSV_FILE_KEY).toString());
			String resultsFile = csvFileName.substring(0, csvFileName.lastIndexOf(".")) + "_results-P" + roster.getPeriod() + ".txt";
			String resultsFullFileName = resultsPath + File.separator + resultsFile;

			FileWriter outputFileWriter = null;

			File outputFile = new File(resultsFullFileName);

			try {
				outputFileWriter = new FileWriter(outputFile);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}

			List<String[]> resultsTable = submissionDatabase.getSubmissionsTable(roster, dueDate, lateSubmitDb);
			for (String[] row : resultsTable) {
				writeToFile(outputFileWriter, row);
			}
			try {
				outputFileWriter.close();
				System.out.println("Writing to file: " + resultsFullFileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeToFile(FileWriter outputFile, String[] arr) {
		String EOL = System.lineSeparator();
		for (int i = 0; i < arr.length; i++) {
			try {
				if (arr[i] != null)
					outputFile.write("\"" + arr[i].replaceAll("\"", "\"\"") + "\"");
				if (i != arr.length - 1) outputFile.write("\t");
				else outputFile.write(EOL);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private List<Roster> buildRosters(List<String> rosterNames) {
		List<Roster> rosters = new ArrayList<>();
		for (String rosterName : rosterNames) {
			rosters.add(buildRoster(rosterName));
		}
		return rosters;
	}

	private Roster buildRoster(String rosterName) {

		//System.out.println("Chosen roster names: " + chosenRosterNames);
		ArrayList<Student> dataBase = new ArrayList<>();

		String fileName = ROSTERS_DIR + File.separator + rosterName;
		//System.out.println("Loading roster file: " + fileName);

		try (Scanner in = new Scanner(new File(fileName))) {
			// **** Note: Must add a column for period into School Loop export
			// ****       Read below.
			// Assuming the roster is always in this format:
			// LastName \t FirstName \t ID \t ID \t Grade \t Email \t Period
			// And that it is in alphabetical order

			while (in.hasNextLine()) {

				String line = in.nextLine();
				String[] entry = line.split("\t");
				System.out.println(Arrays.toString(entry));
				// Trim any accidental whitespace from edges
				for(int i = 0; i < entry.length; i++) {
					entry[i] = entry[i].trim();
				}

				dataBase.add(new Student(entry[ROSTER_FIRST_NAME_INDEX], entry[ROSTER_LAST_NAME_INDEX], entry[ROSTER_ID_INDEX], entry[ROSTER_PERIOD_INDEX], entry[ROSTER_EMAIL_INDEX]));
				//System.out.println("Read ROSTER entry: " + Arrays.toString(entry));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new Roster(dataBase);
	}

	private SubmissionsDatabase buildCSVDatabase(File file, boolean isLateForm) throws IOException, ParseException, CsvException {
		if (file == null) return null;
		String dateH = isLateForm ? lateDateHeaderField.getText() : dateHeaderField.getText();
		String firstNameH = isLateForm ? lateFirstNameHeaderField.getText() : firstNameHeaderField.getText();
		String lastNameH = isLateForm ? lateLastNameHeaderField.getText() : lastNameHeaderField.getText();
		String periodH = isLateForm ? latePeriodHeaderField.getText() : periodHeaderField.getText();
		String emailH = isLateForm ? lateEmailHeaderField.getText() : emailHeaderField.getText();
		String uploadH = isLateForm ? lateUploadHeaderField.getText() : uploadHeaderField.getText();
		String commentsH = isLateForm ? lateCommentsHeaderField.getText() : commentsHeaderField.getText();
		String formIdH = isLateForm ? lateFormIdHeaderField.getText() : null;
		
		return new SubmissionsDatabase(file, startDatePicker.getValue().toString(),
				endDatePicker.getValue().toString(), dateH, firstNameH,
				lastNameH, periodH, emailH, uploadH, commentsH, formIdH);
	}

	private class MyChangeListener implements ChangeListener<LocalDate> {

		@Override
		public void changed(ObservableValue<? extends LocalDate> observableObj, LocalDate oldValue, LocalDate newValue) {
			try {
				JsonObject prefs = getPrefs();
				if (observableObj == startDatePicker.valueProperty()) {
					prefs.addProperty(START_WINDOW_KEY, newValue.toString());
					FileUtil.writeString(new File(PREF_FILE), prefs.toString());
				}
				else if (observableObj == endDatePicker.valueProperty()) {
					prefs.addProperty(END_WINDOW_KEY, newValue.toString());
					FileUtil.writeString(new File(PREF_FILE), prefs.toString());
				}
				else if (observableObj == dueDatePicker.valueProperty()) {
					prefs.addProperty(DUE_DATE_KEY, newValue.toString());
					FileUtil.writeString(new File(PREF_FILE), prefs.toString());
				}
			} catch (JsonSyntaxException | IOException e) {
				e.printStackTrace();
			}		
		}
	}

	public Map<String, String> getUrlsFromFile(File file) {
		Map<String, String> map = new HashMap<>();
		try (Scanner scanner = new Scanner(file);) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] urls = line.split("[|]");
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
						map.put(fileName, url);
					} else {
						System.out.println("Invalid url: " + url);
					}
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return map;
	}

	public void downloadFiles(Map<String, String> filePaths, File saveDir) {

		List<Exception> exceptions = new ArrayList<>();
		for (Map.Entry<String, String> entry : filePaths.entrySet()) {
			String fileName = entry.getKey();
			String url = entry.getValue();
			String downloadPath = saveDir + File.separator + fileName;
			System.out.println("Downloading " + fileName + " from " + url + "...");
			try {
				downloadFile(downloadPath, url);
			} catch (Exception e) {
				exceptions.add(e);
				e.printStackTrace();
			}

			System.out.println("Downloaded " + downloadPath);
		}

		if (exceptions.size() > 0) {
			System.out.println("The following exceptions were encountered: ");
			for (Exception err : exceptions) {
				err.printStackTrace();
			}
		}
		System.out.println("Done!");
	}

	// Download the file at the fileUrl and save it at the fileName path
	public static void downloadFile(String fileName, String fileUrl)
			throws MalformedURLException, IOException {

		try (BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
				FileOutputStream fout = new FileOutputStream(fileName);) {

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		}
	}

	public static RecordingOptions promptRecordingOptions() throws JsonSyntaxException, FileNotFoundException, IOException {
		File csvFile = promptCSVFile("Choose a CSV file containing submissions.", LAST_CSV_FILE_KEY);
		if (csvFile == null) return null;
		File lateFile = null;//promptCSVFile("Choose a late/resubmit file if one exists.", LAST_LATE_FILE_KEY);
		List<String> rosterNames = promptRosterNames();
		if (rosterNames.size() == 0) return null;
		File resultsDir = promptResultsDir();
		if (resultsDir == null) return null;
		return new RecordingOptions(csvFile, rosterNames, resultsDir, lateFile);
	}

	private static class RecordingOptions {
		private File csvFile;
		private List<String> rosterNames;
		private File resultsDir;
		private File lateFile;

		public RecordingOptions(File csvFile, List<String> rosterNames, File resultsDir, File lateFile) {
			this.csvFile = csvFile;
			this.rosterNames = rosterNames;
			this.resultsDir = resultsDir;
			this.lateFile = lateFile;
		}

		public File getCsvFile() {
			return csvFile;
		}

		public List<String> getRosterNames() {
			return rosterNames;
		}

		public File getResultsDir() {
			return resultsDir;
		}

		public File getLateFile() {
			return lateFile;
		}
	}
}