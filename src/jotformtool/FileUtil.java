package jotformtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;


public class FileUtil {
	public static final PrintStream SYS_OUT = System.out;
	public static final String UTF8 = java.nio.charset.StandardCharsets.UTF_8.toString();
	public static final Charset UTF_8 = java.nio.charset.StandardCharsets.UTF_8;
	
	public static String read(File file) throws FileNotFoundException, IOException {
		InputStreamReader reader = null;
		String str = "";
		reader = new InputStreamReader(new FileInputStream(file), UTF_8);
		while (reader.ready()) {
			str += (char)reader.read();
		}
		reader.close();
		return str;
	}
	
	public static List<String[]> readCSVFile(File file) throws IOException, CsvException {
		return readCSVFile(file, 0);
	}
	
	// read the CSV file starting after skipLines lines (i.e. 1 skips header)
	public static List<String[]> readCSVFile(File file, int skipLines) throws IOException, CsvException {
        return new CSVReaderBuilder(new FileReader(file)).withSkipLines(skipLines).build().readAll(); 
	}
	
	public static String[][] readTabDelimitedSpreadSheet(File file) throws FileNotFoundException, IOException {
		String str = read(file).replaceAll("\r\n", "\n");
		String[] rows = str.split("\n");
		if (rows.length > 0) {
			int headerRow = 0;
			while (rows[headerRow].trim().length() == 0) {
				headerRow++;
				if (headerRow == rows.length) {
					headerRow = 0;
					break;
				}
			}
			int numCols = rows[headerRow].trim().split("\t").length;
			String[][] arr = new String[rows.length - headerRow][numCols];
			for (int row = headerRow; row < rows.length; row++) {
				String[] values = rows[row].split("\t");
				for (int col = 0; col < values.length; col++) {
					arr[row][col] = values[col];
				}
			}
			return arr;
		}
		return new String[0][0];
	}
	
	public static JsonElement getObj(File file) throws JsonSyntaxException, IOException, FileNotFoundException {
		String str = StringUtil.getCodeWithoutComments(FileUtil.read(file));
		if (str.length() == 0) str = "{}";
		return new JsonParser().parse(str);
	}
	
	public static JsonElement getObj(String fileName) throws JsonSyntaxException, IOException, FileNotFoundException {
		return getObj(new File(fileName));
	}
	
	public static void writeString(String path, String str) {
		writeString(new File(path), str);
	}
	
	public static void writeString(File file, String str) {
		try {
			//SYS_OUT.println("writing to file: " + file.getAbsolutePath());
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), UTF_8);
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				writer.write(c);
			}
			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public static String getExtension(String fileName) {
		int start = fileName.lastIndexOf('.');
		if (start >= 0) {
			return fileName.substring(start);
		}
		return "";
	}
	
	public static String getExtension(File file) {
		return getExtension(file.getName());
	}
	
	public static String getNameWithoutExtension(String fileName) {
		return fileName.substring(0, fileName.length() - getExtension(fileName).length());
	}
	
	public static String getNameWithoutExtension(File file) {
		return getNameWithoutExtension(file.getName());
	}
	
	public static String fileSafeName(String s) {
		return s.replaceAll("\\s", "+").replaceAll("[^+a-zA-Z0-9.-]", "");
	}
}
