package mjtool;

import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class MJAssignmentManager {


    private MJFileDownloader fd;
    public MJAssignmentManager(MJFileDownloader fd) {
        this.fd = fd;
    }

    public static class Assignment {
        private String name;
        private String lessonPath;

        public Assignment(String name, String lessonPath) {
            this.name = name;
            this.lessonPath = lessonPath;
        }

        public String getName() {
            return name;
        }


        public String getLessonPath() {
            return lessonPath;
        }
    }

    public ArrayList<Assignment> generateAssignments() {
        ArrayList<Assignment> assignments = new ArrayList<>();


        JsonObject jsonAssignments = this.fd.queryAssignmentMetaData();
        for (String key : jsonAssignments.keySet()) {
            String path = jsonAssignments.get(key).getAsString();
            path = path.substring(0, path.length() - ".md".length());
            Assignment a = new Assignment(key, path);

            assignments.add(a);
        }


        return assignments;
    }
}
