package manujiaming.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import manujiaming.MJAssignmentManager;
import manujiaming.MJSubmission;
import manujiaming.MJStudent;
import manujiaming.MJTool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;


public class MJSubmissionEntry extends VBox {


    private MJTool a;
    //has user, and autograder comment
    public MJSubmissionEntry(MJSubmission submission, VBox parent, MJTool a) {
        super(0);
        this.a = a;

        //field for expanding submissions
        //the actual name and student ID
        //autograder comments
        this.maxWidthProperty().bind(parent.widthProperty());
        this.setAlignment(Pos.CENTER);

        Line l1 = new Line(0,0,0,0);
        l1.endXProperty().bind(this.widthProperty());

        l1.setStroke(Color.WHITE);

        HBox hb = getMain(submission);
        hb.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(l1, hb);

        this.setBackground(new Background(new BackgroundFill(MJConstants.BLUE_GRAY, null, null)));
    }

    private HBox getMain(MJSubmission sub) {
        Circle subIndicator = new Circle(8, MJConstants.BLUE);

        MJLabel lab = new MJLabel(sub.getStudent().getFirstName() + " " + sub.getStudent().getLastName() + " (Sub " + sub.getSubNum() + ")");
        lab.setPrefWidth(150);

        boolean late = sub.isLateFor(a.getActiveAssignment());

        LocalDateTime realTime =  sub.getSubmissionTime().plus(MJConstants.UTC_OFFSET.getTotalSeconds(), ChronoUnit.SECONDS);
        String realTimeString =  realTime.format(MJConstants.STD_FORMATTER);
        MJLabel time = new MJLabel(" " + realTimeString + (late ? " [LATE]" : ""));
        time.setWidth(130);

        Rectangle complete = new Rectangle(22, 22, sub.isComplete() ? Color.GREEN : Color.RED);
//        complete.setWidth(80);


        MJLabel comments = new MJLabel(sub.getAdditionalComments());
        comments.setWidth(280);

        MJButton download = new MJButton("download.png",20);

        HBox.setMargin(subIndicator, new Insets(0,0, 0, 10));
        HBox.setMargin(lab, new Insets(0,0, 0, 0));
        HBox.setMargin(time, new Insets(2,10, 2, 10));
        HBox.setMargin(complete, new Insets(2,(100 - complete.getWidth()) / 2, 2, (100 - complete.getWidth()) / 2));
        HBox.setMargin(comments, new Insets(2,10, 2, 10));
        HBox.setMargin(download, new Insets(4,10, 4, 20));

        download.setOnMousePressed((event) -> {
           this.a.getFd().download(this.a, sub);
           this.a.getFd().openDownloadsDirectory(this.a);
        });

        return new HBox(subIndicator, lab, time, complete, comments, download);
    }
}
