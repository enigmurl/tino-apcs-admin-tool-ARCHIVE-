package manujiaming;

import manujiaming.ui.MJConstants;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;

public class MJSubmission implements Comparable<MJSubmission> {

    /*
    meta data
     */
    private MJStudent student;
    private int subNum; // 1 indexed
    private LocalDateTime submissionTime;

    /*
    actual submission content
     */
    private List<String> javaFile;//see what to do about this later
    private boolean isComplete;
    private String additionalComments;

    /* assignment specific content */
    private String extraneous;

    public MJSubmission(MJStudent student, int subNum, LocalDateTime submissionTime, List<String> files, boolean isComplete, String comments) {
        this.student = student;
        this.subNum = subNum;
        this.submissionTime = submissionTime;
        this.javaFile = files;
        this.isComplete = isComplete;
        this.additionalComments = comments;

        this.student.getSubs().add(this);
    }

    public boolean isLateFor(MJAssignmentManager.Assignment a) {
        return this.submissionTime.isAfter(a.getDueDate().atTime(LocalTime.of(23,59, 59)).plus(-MJConstants.UTC_OFFSET.getTotalSeconds(), ChronoUnit.SECONDS));
    }

    public List<String> getJavaFiles() {
        return javaFile;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public String getAdditionalComments() {
        return additionalComments;
    }

    public MJStudent getStudent() {
        return student;
    }

    public int getSubNum() {
        return subNum;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    @Override
    public int compareTo(MJSubmission o) {
        return this.submissionTime.compareTo(o.submissionTime);
    }
}
