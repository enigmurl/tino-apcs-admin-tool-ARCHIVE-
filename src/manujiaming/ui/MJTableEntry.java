package manujiaming.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import manujiaming.MJAssignmentManager;
import manujiaming.MJStudent;
import manujiaming.MJSubmission;
import manujiaming.MJTool;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MJTableEntry extends VBox {


    private ArrayList<MJSubmissionEntry> subEntries = new ArrayList<>();
    private MJHeader subHeader;
    //has user, and autograder comment
    public MJTableEntry(MJStudent student, VBox parent, MJTool a) {
        super(0);
        //field for expanding submissions
        //the actual name and student ID
        //autograder comments
        this.maxWidthProperty().bind(parent.widthProperty());
        this.setAlignment(Pos.CENTER);

        Line l1 = new Line(0,0,0,0);
        l1.endXProperty().bind(this.widthProperty());

        l1.setStroke(Color.WHITE);

        HBox hb = getMain(student, student.getSubs().size());
        hb.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(l1, hb);

        for (MJSubmission m : student.getSubs()) {
            this.subEntries.add(new MJSubmissionEntry(m, this, a));
        }

        this.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, null, null)));

        this.subHeader = new MJHeader(26, new int[] {150, 150, 100, 300}, new String[] {"Sub #", "Date", "Complete","User Comments"});
    }

    private MJLabel comments;
    private boolean expandedSubmissions;

    private HBox getMain(MJStudent student, int count) {
        MJButton b = new MJButton("Subs " + count);
        b.setMinWidth(80);
        b.setMaxWidth(80);

        b.setAlignment(Pos.CENTER);

        //also has SID
        MJLabel name = new MJLabel(student.getFirstName() + " " + student.getLastName() + " (" + student.getStudentID() + ")");
        name.setWidth(200);

        String comment = student.getGeneratedComments();
        comments = new MJLabel(comment == null ? "{no comments auto generated}" : comment);

        b.setOnMousePressed(event -> {
            //toggle subchain
            if (expandedSubmissions) {
                b.setBackground(MJConstants.DEFAULT_BACKGROUND);
                this.getChildren().remove(2, this.getChildren().size());
            } else {
                b.setBackground(MJConstants.YELLOW_BACKGROUND);
                this.getChildren().add(2, subHeader);
                for (int i = 0; i < subEntries.size(); i++) {
                    this.getChildren().add(3 + i, this.subEntries.get(i));
                }
            }

            expandedSubmissions = !expandedSubmissions;
        });

        HBox.setMargin(b, new Insets(2,10,2,10));
        HBox.setMargin(name, new Insets(2,10,2,10));
        HBox.setMargin(comments, new Insets(2,40,2,0));
        return new HBox(b, name, comments);
    }
}
