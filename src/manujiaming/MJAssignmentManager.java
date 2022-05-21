package manujiaming;

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
        private LocalDate dueDate;

        public Assignment(String name, LocalDate dueDate, String lessonPath) {
            this.name = name;
            this.dueDate = dueDate;
            this.lessonPath = lessonPath;
        }

        public String getName() {
            return name;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public String getLessonPath() {
            return lessonPath;
        }
    }

    public ArrayList<Assignment> generateAssignments() {
        ArrayList<Assignment> assignments = new ArrayList<>();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH);

        JsonObject jsonAssignments = this.fd.queryAssignmentMetadata();
        for (String key : jsonAssignments.keySet()) {
            JsonObject subDict = jsonAssignments.get(key).getAsJsonObject();
            String path = subDict.get("path").getAsString();
            String date = subDict.get("date").getAsString();

            Assignment a = new Assignment(key, LocalDate.parse(date, formatter), path);

            assignments.add(a);
        }


        return assignments;
    }
}
