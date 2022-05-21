

import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class StringUtil {
	
	// Removes "" from beginning and end of JSON strings
	public static String removeJSONquotes(String str) {
		if (str.indexOf("\"") == 0)
			str = str.substring(1);
		if (str.lastIndexOf("\"") == str.length() - 1)
			str = str.substring(0, str.length() - 1);
		return str;
	}
	
//	public static void main(String[] args) {
//		// testing addQuotes
//		try {
//			String quotes = addQuotesForCommaDelimitedString("Submission Date,First Name,Last Name,Period,\"\"\"E-mail\"\"\",Score,Any comments?,\"Did you name your zip, file properly?  Ex: P3_Wang_Michael_Centipede.zip\",Upload");
//			System.out.println(quotes);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public static String addQuotesForCommaDelimitedString(String str) throws ParseException {
		String s = "";
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != '"') {
				s += '"';
				int nextComma = str.indexOf(',', i);
				if (nextComma == -1) {
					s += str.substring(i) + '"';
					break;
				}
				s += str.substring(i, nextComma) + '"' + str.charAt(nextComma);
				i = nextComma;
			} else {
				int endOfCell = str.indexOf("\",", i + 1);
				if (endOfCell == -1) throw new ParseException("Unpaired quote parse exception!", i);
				s += str.substring(i, endOfCell + 2);
				i = endOfCell + 1;
			}
		}
		return s;
	}
	
	public static String htmlEncodedString(String str) {
	    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
	
	// for this, assume words are separated by spaces
	public static List<String> wordsInArrayNotContainedInString(String str, String[] strArr) {
		str = " " + str + " ";
		List<String> list = new ArrayList<>();
		for (String s : strArr) {
			if (!str.contains(" " + s + " ")) list.add(s);
		}
		return list;
	}
	
	public static String getNChars(int n, char c) {
		String s = "";
		for (int i = 0; i < n; i++) {
			s += c;
		}
		return s;
	}
	
	public static String getCodeWithoutComments(String code) {
		String[] lines = code.replaceAll("\r\n", "\n").split("\n");
	    removeCommentsFromLines(lines);
	    return String.join("\n", lines);
	}
	
	public static int indexOfNextCharNotInQuotes(String str, int i, String subStr) {
	    do {
	        if (i < str.length() && str.charAt(i) == '\"') {
	            i++;
	            i = indexOfNextNonEscapedDoubleQuote(str, i);
	        }
	        if (i + subStr.length() > str.length() || !subStr.equals(str.substring(i, i + subStr.length()))) {
	            i++;
	        }
	    } while (i < str.length() && (i + subStr.length() > str.length() || !subStr.equals(str.substring(i, i + subStr.length()))));
	    return i;
	}
	
	public static int indexOfNextNonEscapedDoubleQuote(String str, int i) {
	    while (i < str.length() && (i - 1 >= 0 && str.charAt(i - 1) == '\\' && (i - 2 < 0 || str.charAt(i - 2) != '\\') || str.charAt(i) != '\"')) {
	        i++;
	    }
	    i++;
	    return i;
	}
	
	public static void removeCommentsFromLines(String[] lines) {
	    // removing line comments
	    for (int i = 0; i < lines.length; i++) {
	        String line = lines[i];
	        int indexOfLineComment = indexOfNextCharNotInQuotes(line, 0, "//");
	        if (indexOfLineComment < line.length()) {
	            if (indexOfLineComment == 0) {
	                line = "";
	            } else {
	                line = line.substring(0, indexOfLineComment);
	            }
	        }
	        lines[i] = line;
	    }
	}
	
	public static String removeTrailingWhiteSpace(String str) {
		int endIndex = str.length() - 1;
		for (int i = str.length() - 1; i >= 0; i--) {
			if (Character.isWhitespace(str.charAt(i))) endIndex = i;
			else break;
		}
		return str.substring(0, endIndex);
	}
	
	public static String stringCenteredInChars(String str, char c, int totalLength) {
		int numCharsLeft = (totalLength - str.length()) / 2;
		int numCharsRight = totalLength - str.length() - numCharsLeft;
		if (numCharsLeft <= 0) return str;
		String s = getNChars(numCharsLeft, c);
		s += str;
		s += getNChars(numCharsRight, c);
		return s;
	}
	
	public static void printTitleWithBorder(String title, char borderChar, char innerChar, int width, PrintStream out) {
		out.println(StringUtil.getNChars(width, borderChar));
		out.println(borderChar + StringUtil.stringCenteredInChars(title, innerChar, width - 2) + borderChar);
		out.println(StringUtil.getNChars(width, borderChar));
	}
	
	public static String roundNum(double num, int decimalPlaces) throws IllegalArgumentException {
		if (decimalPlaces < 0) throw new IllegalArgumentException(decimalPlaces + " is not a valid number of decimal places. Number of decimal places must be >= 0.");
		double roundedVal = Math.round(num * Math.pow(10, decimalPlaces))/Math.pow(10, decimalPlaces);
		return "" + (decimalPlaces == 0 ? (int)roundedVal : roundedVal);
	}
}
