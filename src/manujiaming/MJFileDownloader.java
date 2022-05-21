package manujiaming;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.control.ProgressBar;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MJFileDownloader {

    private String urlBase;
    private String apiEntry;//API key
    public MJFileDownloader(String url, String apiEntry) {
        this.urlBase = url;
        this.apiEntry = apiEntry;
    }

    private String fullApiKeyEntry() {
        return "?key=" + this.apiEntry;
    }

    private void ensureFolder(MJTool root) {
        File output = new File(root.getExportURL().toString() +"/" + root.getActiveAssignment().getName());
        output.mkdir();
    }

    public void download(MJTool root, MJSubmission sub) {
        //ensure folder is created
        this.ensureFolder(root);

        for (String fileName : sub.getJavaFiles()) {
            String url = this.urlBase + "/submissions/" + root.getActiveAssignment().getLessonPath() + "/" + sub.getStudent().getEmail() + "/" + sub.getSubNum() + "/" + fileName + fullApiKeyEntry();

            downloadFile(url, root.getExportURL().toString() + "/" + root.getActiveAssignment().getName() + "/" + fileName);
        }
    }

    public void openDownloadsDirectory(MJTool root) {
        File output = new File(root.getExportURL().toString() + "/" + root.getActiveAssignment().getName());

        try {
            Desktop.getDesktop().open(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String fileUrl, String fileName)
    {

        try (BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                FileOutputStream fout = new FileOutputStream(fileName);) {

            //read in 1024 chunks
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } catch (ConnectException e) {
            System.err.println("Please make sure the server is online!");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public String downloadFile(String fileUrl) {
        StringBuilder ret = new StringBuilder();

        try (BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
             /*FileOutputStream fout = new FileOutputStream(fileName);*/) {

            //read in 1024 chunks
            byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                for (int i = 0; i < count; i++) {
                    ret.append((char) data[i]);
                }
            }
        } catch (ConnectException e) {
            System.err.println("Please make sure the server is online!");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return ret.toString();
    }

    public JsonObject queryAssignmentMetadata() {
        String url = this.urlBase + "/assignments" + fullApiKeyEntry();

        String jsonString = downloadFile(url);

        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

    private JsonArray queryAllMetaData() {
        String url = this.urlBase + "/submissions/metadata" + fullApiKeyEntry();
        String jsonString = downloadFile(url);

        return new JsonParser().parse(jsonString).getAsJsonArray();
    }

    private JsonArray queryFileNames(String lessonPath, String email, int subOneIndex) {
        String url = this.urlBase + "/submissions/" + lessonPath + "/" + email + "/" + subOneIndex + fullApiKeyEntry();

        String jsonString = downloadFile(url);

        return new JsonParser().parse(jsonString).getAsJsonArray();
    }

    private HashMap<String, MJStudent> genEmailMap(MJRosterLoader.Roster roster) {
        HashMap<String, MJStudent> ret = new HashMap<>();

        for (MJStudent s : roster.getUsers()) {
            ret.put(s.getEmail(), s);
        }

        return ret;
    }

    public ArrayList<MJSubmission> getSubmissions(MJAssignmentManager.Assignment assignment, MJRosterLoader.Roster r, ProgressBar out) {
        ArrayList<MJSubmission> ret = new ArrayList<>();

        //create a hashmap of email to user
        HashMap<String, MJStudent> emailMap = genEmailMap(r);

        //query meta data
        JsonArray metaData = queryAllMetaData();

        for (int i = 0; i < metaData.size(); i++) {
            //is it on the roster, and is it this assignment
            JsonObject current = metaData.get(i).getAsJsonObject();

            MJStudent user = emailMap.get(current.get("email").getAsString());

            if (user != null && current.get("lesson").getAsString().equals(assignment.getLessonPath())) {
                //make the submission
                boolean isWorking = current.get("is_working").getAsByte() != 0;
                int subCount = current.get("submission_cnt").getAsInt();
                String comments = current.get("comment").getAsString();
                LocalDateTime time = LocalDateTime.parse(current.get("timestamp").getAsString(), DateTimeFormatter.ISO_DATE_TIME);

                JsonArray fileNames = this.queryFileNames(assignment.getLessonPath(), user.getEmail(), subCount);

                ArrayList<String> converted = new ArrayList<>();
                for (int k = 0; k < fileNames.size(); k++) {
                    converted.add(fileNames.get(k).getAsString());
                }

                MJSubmission sub = new MJSubmission(user, subCount, time, converted, isWorking, comments);
                ret.add(sub);
            }

            out.setProgress((double) (i + 1) / metaData.size());
        }

        return ret;
    }

}
