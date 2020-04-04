//package utils;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.io.FileUtils;
//import org.json.JSONException;
//
//import wiki.Wiki;
//
//public class TMP {
//	public static void main(String...args) throws JSONException, IOException, SQLException{
//		List<String> lines = FileUtils.readLines(new File("wissensbereiche.txt"));
//		List<String>outputLines = new ArrayList<String>();
//		for (String string : lines) {
//			outputLines.add(string);
//			if(string.startsWith("Q")){
//				outputLines.add("studies:");
//				for (String studies : Wiki.getStudies(string.trim())) {
//					outputLines.add(studies);
//				};
//			}
//		}
//		
//		outputLines.forEach(System.out::println);
//		
//		
//	}
//}
