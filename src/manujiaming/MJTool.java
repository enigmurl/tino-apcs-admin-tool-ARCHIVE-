package manujiaming;

import com.google.gson.JsonObject;
import com.sun.xml.internal.ws.util.xml.CDATA;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import manujiaming.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * @author Manu Bhat
 */
public class MJTool extends Application {

    private static final String API_URL = "http://localhost:8000/";
    private static final String API_KEY = "happysparkyadmin12138";

    public static void main(String[] args) {
        launch(args);
    }

    private Stage stage;

    private Scene landing;
    private Scene success;

    private MJPreferenceManager pm;
    private MJFileDownloader fd;
    private ArrayList<MJRosterLoader.Roster> rosters = new ArrayList<>();
    private ArrayList<MJAssignmentManager.Assignment> assignments = new ArrayList<>();

    private String activeRoster, activeAssignment;
    private MJAssignmentManager.Assignment activeAssignmentObject;
    private MJRosterLoader.Roster activeRosterObject;
    private File exportURL;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        this.pm = new MJPreferenceManager();
        this.fd = new MJFileDownloader(API_URL, API_KEY);
        MJRosterLoader rl = new MJRosterLoader();
        MJAssignmentManager am = new MJAssignmentManager(this.fd);

        this.rosters = rl.genRosters();
        this.assignments = am.generateAssignments();

        this.initLanding();
        this.initSuccess();

        this.launchStage();
    }

    private void initLanding() {
        //main screen:
        //left is folder, roster
        //
        StackPane bgHolder = new StackPane();
        Rectangle bg = new Rectangle(0, 0, MJConstants.BLUE_GRAY);
        bg.widthProperty().bind(bgHolder.widthProperty());
        bg.heightProperty().bind(bgHolder.heightProperty());
        bgHolder.setPrefWidth(900);
        bgHolder.setPrefHeight(600);

        VBox root = new VBox();

        MJLabel title = new MJLabel("APCS Submission Viewer", MJFontManager.serif);
        Line line = new Line(-200, 0, 200, 0);
        line.setStroke(Color.WHITE);
        StackPane lineHolder = new StackPane(line);
        StackPane titleHolder = new StackPane(title);
        lineHolder.setAlignment(Pos.CENTER);
        titleHolder.setAlignment(Pos.CENTER);
        VBox.setMargin(lineHolder, new Insets(0, 0, 10, 0));


        HBox main = new HBox();

        /* left side: general config */

        MJLabel configLabel = new MJLabel("General Config", MJFontManager.sansSerifBold);


        MJButton updateExport = new MJButton("Update Export Directory");
        MJLabel exportDir = new MJLabel("None selected");
        HBox exportGroup = new HBox(10, updateExport, exportDir);
        exportGroup.setAlignment(Pos.CENTER);

        MJComboBox<String> rosterChoice = new MJComboBox<>();
        for (MJRosterLoader.Roster r : rosters) {
            rosterChoice.getItems().add(r.getRosterName());
        }
        MJLabel rosterHeader = new MJLabel("Select Roster", MJFontManager.sansSerif);


        VBox generalConfig = new VBox(configLabel, exportGroup, rosterChoice, rosterHeader);
        generalConfig.setMaxWidth(400);
        generalConfig.setPrefWidth(400);

        VBox.setMargin(exportGroup, new Insets(20));
        VBox.setMargin(rosterHeader, new Insets(5));

        /* right side: assignment specific */

        MJLabel assignmentLabel = new MJLabel("Assignment", MJFontManager.sansSerifBold);

        MJComboBox<String> assignments = new MJComboBox<>();

        for (MJAssignmentManager.Assignment i : this.assignments) {
            assignments.getItems().add(i.getName());
        }
        MJLabel labHeader = new MJLabel("Select Lab", MJFontManager.sansSerif);

        Button b = new MJButton("Download Submissions");

        VBox currentAssignment = new VBox(assignmentLabel, assignments, labHeader, b);


        VBox.setMargin(assignments, new Insets(20,0,5,0));
        VBox.setMargin(b, new Insets(20,0,5,0));


        HBox.setHgrow(currentAssignment, Priority.ALWAYS);

        generalConfig.setAlignment(Pos.TOP_CENTER);
        currentAssignment.setAlignment(Pos.TOP_CENTER);

        main.getChildren().addAll(generalConfig, currentAssignment);
        root.getChildren().addAll(titleHolder, lineHolder, main);

        bgHolder.getChildren().addAll(bg, root);

        this.landing = new Scene(bgHolder);

        this.loadLandingDefaults(rosterChoice, assignments, exportDir, updateExport, b);
    }

    private void loadLandingDefaults(MJComboBox<String> roster, MJComboBox<String> assignment, Label exportLabel, Button exportButton, Button downloadButton) {
        String exportPath = pm.getKey(MJPreferenceManager.EXPORT_KEY);
        String rosterName = pm.getKey(MJPreferenceManager.ROSTER_KEY);
        String assignmentName = pm.getKey(MJPreferenceManager.ASSIGNMENT_KEY);

        if (rosterName != null) {
            int index = roster.getItems().indexOf(rosterName);
            if (index != -1) {
                roster.getSelectionModel().select(index);
                activeRosterObject = this.rosters.get(index);
            } else {
                rosterName = null;
            }
            activeRoster = rosterName;
            System.out.println("Active roster: "+ rosterName);
        }
        if (assignmentName != null) {
            int index = assignment.getItems().indexOf(assignmentName);
            if (index != -1) {
                assignment.getSelectionModel().select(index);
                activeAssignmentObject = this.assignments.get(index);
            } else {
                assignmentName = null;
            }
            activeAssignment = assignmentName;
            System.out.println("Active assignment: "+ assignmentName);
        }
        if (exportPath != null && new File(exportPath).exists()) {
            exportURL = new File(exportPath);
            exportLabel.setText(exportURL.getName() + '/');
        }

        roster.valueProperty().addListener((observable, oldValue, newValue) -> {
            pm.setKey(MJPreferenceManager.ROSTER_KEY, newValue);
            this.activeRoster = newValue;

            int index = roster.getItems().indexOf(newValue);
            if (index != -1) {
                activeRosterObject = this.rosters.get(index);
            } else {
                activeRosterObject = null;
            }

            System.out.println("Active roster: " + activeRoster);
        });

        assignment.valueProperty().addListener((observable, oldValue, newValue) -> {
            pm.setKey(MJPreferenceManager.ASSIGNMENT_KEY, newValue);
            this.activeAssignment = newValue;

            int index = assignment.getItems().indexOf(newValue);
            if (index != -1) {
                activeAssignmentObject = this.assignments.get(index);
            } else {
                activeAssignmentObject = null;
            }
            System.out.println("Active assignment: " + activeAssignment);
        });

        //export path
        exportButton.setOnMousePressed(event -> {
            EventQueue.invokeLater(() -> {
                //prompt for a file change
                System.out.println("Initiating export JChooser ... ");

                JFileChooser mainChooser;
                try {
                    String url = pm.getKey(MJPreferenceManager.EXPORT_KEY);
                    File base = url == null ? null : new File(url);
                    if (base != null) base = base.getParentFile();

                    mainChooser = new JFileChooser(base);

                    System.out.println("Prompting export with base URL: " + base);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                // choose a directory to save the results
                mainChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                mainChooser.setDialogTitle("Choose directory to store submissions in.");
                int retVal = mainChooser.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File saveDir = mainChooser.getSelectedFile();
					Platform.runLater(() -> {
                    	exportLabel.setText(saveDir.getName() + '/');
					});
                    pm.setKey(MJPreferenceManager.EXPORT_KEY, saveDir.getAbsolutePath());
                    MJTool.this.exportURL = saveDir;
                } else {
                    System.out.println("Abort export selection");
                }
            });
        });

        //button
        downloadButton.setOnMousePressed(event -> {
            System.out.println("Downloading ... ");
            this.download();
        });
    }

    private void download() {
        if (this.activeAssignment == null || this.activeRoster == null || this.exportURL == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Cannot download!");
            a.setContentText("Please make sure the assignment, export path, and roster are all set!");
            a.show();
            return;
        }

        System.out.println("Starting download ...");

        this.goToSuccess();
        EventQueue.invokeLater(() -> {
            MJAssignmentManager.Assignment a = null;
            MJRosterLoader.Roster r = null;

            for (MJAssignmentManager.Assignment a1 : this.assignments) if (a1.getName().equals(this.activeAssignment)) {
                a = a1;
                break;
            }
            for (MJRosterLoader.Roster r1 : this.rosters) if (r1.getRosterName().equals(this.activeRoster)) {
                r = r1;
                break;
            }

            ArrayList<MJSubmission> subs = this.fd.getSubmissions(a, r, this.downloadPB);
            subs.sort(null);

            for (MJSubmission sub : subs) {
                if (sub.getSubNum() == sub.getStudent().getSubs().size()) {
                    this.fd.download(this, sub);
                }
            }
            this.fd.openDownloadsDirectory(this);

            this.applyComments();

            //and all of them
            MJRosterLoader.Roster finalR = r;
            Platform.runLater(() -> {
                for (MJStudent s : finalR.getUsers()) {
                    userVbox.getChildren().add(new MJTableEntry(s, userVbox, this));
                }

                //go through all submissions and add tables
                for (MJSubmission s : subs) {
                    dateVbox.getChildren().add(new MJSubmissionEntry(s, dateVbox, this));
                }
            });
        });
    }

    private ProgressBar downloadPB;
    private ScrollPane userDateHolder;
    private VBox userVbox;
    private VBox dateVbox;
    private void initSuccess() {
        //success screen:
        //automatically opens up directory with the stuff
        //indicates any duplicates
        //button to copy paste correct data
        //Shows general statistics
        StackPane bgHolder = new StackPane();
        Rectangle bg = new Rectangle(0, 0, MJConstants.BLUE_GRAY);
        bg.widthProperty().bind(bgHolder.widthProperty());
        bg.heightProperty().bind(bgHolder.heightProperty());
        bgHolder.setPrefWidth(800);
        bgHolder.setPrefHeight(600);

        VBox main = new VBox();

        MJLabel title = new MJLabel("APCS Submission Viewer", MJFontManager.serif);
        Line line = new Line(-200, 0, 200, 0);
        line.setStroke(Color.WHITE);
        StackPane lineHolder = new StackPane(line);
        StackPane titleHolder = new StackPane(title);

        MJComboBox<String> viewingMode = new MJComboBox<>();
        viewingMode.getItems().addAll("Student", "Date");
        viewingMode.getSelectionModel().selectFirst();

        MJLabel ml = new MJLabel("Viewing Mode");

        MJButton copyButton = new MJButton("Copy spreadsheet data");
        Line separator = new Line(0, 0, 0, 20);
        separator.setStroke(Color.WHITE);

        HBox hb = new HBox(10, ml, viewingMode,separator,copyButton);
        hb.setAlignment(Pos.CENTER);

        downloadPB = new ProgressBar(0);
        downloadPB.setBackground(MJConstants.DEFAULT_BACKGROUND);

        MJLabel loadingHeader = new MJLabel("Download Progress", MJFontManager.sansSerif);
        StackPane loadingHolder = new StackPane(downloadPB);
        StackPane headerHolder = new StackPane(loadingHeader);

        for (StackPane sp : new StackPane[] {lineHolder, titleHolder, loadingHolder, headerHolder}) {
            sp.setAlignment(Pos.CENTER);
        }

        VBox.setMargin(hb, new Insets(0, 0, 10, 0));
        VBox.setMargin(headerHolder, new Insets(0, 0, 5, 0));
        VBox.setMargin(lineHolder, new Insets(0, 0, 10, 0));

        userVbox = new VBox();
        dateVbox = new VBox();

        this.success = new Scene(bgHolder);

        userDateHolder = new ScrollPane(userVbox);
        userDateHolder.setFitToWidth(true);
        userDateHolder.prefWidthProperty().bind(this.success.widthProperty());
        userDateHolder.maxWidthProperty().bind(this.success.widthProperty());
        userDateHolder.setBackground(
                new Background(new BackgroundFill(Color.TRANSPARENT, null, null))
        );
        userDateHolder.getStyleClass().clear();

        userVbox.maxWidthProperty().bind(this.success.widthProperty());
        dateVbox.maxWidthProperty().bind(this.success.widthProperty());

        VBox.setVgrow(userDateHolder, Priority.ALWAYS);


        VBox headers = new MJHeader(100, new int[] {200, 450}, new String[] {"Name", "Auto Comments"});
        userVbox.getChildren().add(headers);
        VBox dateHeaders = new MJHeader(26, new int[] {150, 150, 100, 300}, new String[] {"Sub #", "Date", "Complete","User Comments"});
        dateVbox.getChildren().add(dateHeaders);

        main.getChildren().addAll(titleHolder, hb, loadingHolder, headerHolder, lineHolder, userDateHolder);

        bgHolder.getChildren().addAll(bg, main);

        viewingMode.setOnAction(event -> {
            if (viewingMode.getValue().equals("Student")) {
                switchToUser();
            } else {
                switchToDate();
            }
        });

        copyButton.setOnMousePressed((event -> {
            StringBuilder sb = new StringBuilder();
            for (MJStudent student : activeRosterObject.getUsers()) {
                String comments = student.getGeneratedComments();
                sb.append(comments == null ? "" : comments);
                sb.append('\n');
            }

            StringSelection stringSelection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }));
    }

    public File getExportURL() {
        return exportURL;
    }

    public MJAssignmentManager.Assignment getActiveAssignment() {
        return activeAssignmentObject;
    }

    public MJFileDownloader getFd() {
        return fd;
    }

    private void applyComments() {
        for (MJStudent mjStudent : this.activeRosterObject.getUsers()) {
            this.handleSingleComment(mjStudent);
        }
    }

    private void handleSingleComment(MJStudent student) {
        String comments = "";

        if (student.getSubs().size() == 0) {
            String realTimeString =  LocalDateTime.now().toLocalDate().format(MJConstants.STD_FORMATTER);
            comments += "MISSING: as of " + realTimeString + "\n";
        } else {
            if (student.getSubs().get(0).isLateFor(this.activeAssignmentObject)) {
                comments += "LATE: earliest submission was marked as late!\n";
            }

            String correctStart = "P" + student.getPeriod() + "_" + student.getLastName() + "_" + student.getFirstName() + "_";
            for (String fileName : student.getSubs().get(student.getSubs().size() - 1).getJavaFiles()) {
                if (!fileName.startsWith(correctStart)) {
                    comments += ("NAMING: file '" + fileName + "' does not start with '" + correctStart + "'\n");
                }
            }

            if (!student.getSubs().get(student.getSubs().size() - 1).isComplete()) {
                comments = "(RE NEEDED): The latest submission was marked as incomplete";
            }
        }

        if (comments.length() > 0) {
            student.setGeneratedComments(comments);
        }
        //apply other stuff
    }

    private void switchToUser() {
        this.userDateHolder.setContent(this.userVbox);
    }

    private void switchToDate() {
        this.userDateHolder.setContent(this.dateVbox);
    }

    private void launchStage() {
        this.stage.setTitle("APCS Submission Recorder");
        this.stage.setScene(this.landing);
        this.stage.show();
        this.stage.centerOnScreen();
    }

    private void goToSuccess() {
        this.stage.setScene(this.success);
    }

    private void returnHome() {
        this.stage.setScene(this.landing);
    }
}
