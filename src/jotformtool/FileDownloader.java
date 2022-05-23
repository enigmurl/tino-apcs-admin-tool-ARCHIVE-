package jotformtool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;

public class FileDownloader {

/*	Feature ideas
	
	Eliminate the need to sort the spreadsheet... Just download the CSV
	and specify the year to download.  Then the program filters out and
	downloads only submissions that match the given year
	Since submissions from the same student will be spread out, we'll need
	to store submissions in a data structure to be analyzed and sorted.
	
	Also add a tool to the downloader to download either All Students,
	specific periods, or specific students.  This will be helpful when
	applied to the Late-Resubmit downloads.
	
	When grading submissions, also check file types and sizes so that
	a "review" mark is placed on students that submitted no file, a .class
	file, or the sum of their .java files byte count is a certain number
	of standard deviations away from the mean (meaning the student has
	much less code than the average, in which case something is off).

	Combine the tools so that it downloads and then grades submissions
	in one fell swoop.  Might as well do it this way...or have it only
	download any new submissions that haven't already been accounted for.
	
	In any case, also have the program detect downloads for being either
	NOT a .java file or being too many standard deviations away from the
	average file size...in other words flag the code as something to review.
	
	Field for the due date and grace period (11:59pm, +5 min by default)

	JavaFX 8 TableView Sorting and Filtering
	https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/

	Creating columns dynamically (I used this code)
	https://community.oracle.com/message/10731570

	Table View tutorial & overview
	https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/table-view.htm
	
	Downloader Tool
	 1. download a spreadsheet of all the submissions.
	 2. sort by date from oldest to newest (oldest should be on top).
	    That way, older submissions will be overwritten by newer submissions
	 3. copy the column containing the links to the files submitted
	 4. paste it into urls.txt
	 5. run the program
	 6. choose a directory to save the files in
	 7. watch them download :)

	Quick Grade Tool
	 1. download a spreadsheet of all the submissions.
	 2. sort by date from oldest to newest (oldest should be on top).
	    That way, older submissions will be overwritten by newer submissions
	 3. copy/paste the entire spreadsheet into submissions.txt
	 5. run the program, choose option 2
	 6. submission_results.txt is created for you.  Load it into a spreadsheet
	    and copy the score column.  If student names don't match the rosters,
		  update the rosters.
*/	
	public static void main(String[] args) {
		downloadFiles();
	}

	public static void downloadFiles() {
		JFileChooser mainChooser = new JFileChooser();
		File saveDir = null;
        // choose a directory to save the results
		mainChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		mainChooser.setDialogTitle("Choose directory to save files in.");
		int retVal = mainChooser.showOpenDialog(null);
	    if(retVal == JFileChooser.APPROVE_OPTION) {
	    	saveDir = mainChooser.getSelectedFile();
	    } else {
	    	System.out.println("retVal = " + retVal);
	    }
	    
		if (saveDir == null) {
			abort();
		}
		List<Exception> exceptions = new ArrayList<>();
		try (Scanner scanner = new Scanner(new File("urls.txt"));) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] urls;
				if (line.contains("|")) {
					urls = line.split("[|]");
				} else {
					urls = line.split("[\n]");
				}				for (String url : urls) {
					url = url.trim();
					if (url.startsWith("http")) {
						// if there is no "/" this can't possibly be a valid url so OOB crash is ok
						String fileName = url.substring(url.lastIndexOf("/") + 1);
						String downloadPath = saveDir + File.separator + fileName;
						System.out.println("Downloading " + fileName + " from " + url + "...");
						try {
							downloadFile(downloadPath, url);
						} catch (Exception e) {
							exceptions.add(e);
							e.printStackTrace();
						}
	
						System.out.println("Downloaded " + downloadPath);
					} else {
						System.out.println("Invalid url: " + url);
					}
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		if (exceptions.size() > 0) {
			System.out.println("The following exceptions were encountered: ");
			for (Exception err : exceptions) {
				err.printStackTrace();
			}
		}		
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
	
	private static void abort() {
		System.out.println("Test Aborted.");
		System.exit(0);
	}
}