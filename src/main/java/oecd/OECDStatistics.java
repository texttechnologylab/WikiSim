package oecd;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wiki.Wiki;

public class OECDStatistics {

	public static void main(String[] args) throws JSONException, IOException {

		//		System.out.println(Wiki.countInstenceOfs("Q246672"));
		getSubclassesAndStudies(false,false);
		formatTable();
	}

	public static void getSubclassesAndStudies(boolean recursiveSubclasses,boolean recursiveInstances) throws JSONException, IOException{
		JSONArray Wissensbereiche = new JSONArray(FileUtils.readFileToString(new File("wissensbereicheStudies.json")));
		for (Object object : Wissensbereiche) {
			JSONObject wissensbereich = (JSONObject) object;
			System.out.println(wissensbereich.getString("name"));
			if(wissensbereich.has("qid")){
				wissensbereich.put("studies", Wiki.getStudies(wissensbereich.getString("qid")));
				for (Object study : wissensbereich.getJSONArray("studies")) {
					JSONObject studyJson = (JSONObject) study;
					try{
						studyJson.put("instanceCounts", Wiki.countInstenceOfs(studyJson.getString("qid"),recursiveInstances));
					}catch(Exception e){
						studyJson.put("instanceCounts", -1);
					}
				}
//				"Q11862829"
				wissensbereich.put("subclasses",Wiki.getWikidataSubclassOfInstanceOf(wissensbereich.getString("qid"), "Q11862829" ,recursiveSubclasses));
				for (Object subclass : wissensbereich.getJSONArray("subclasses")) {
					JSONObject subclassJson = (JSONObject) subclass;
					subclassJson.put("studies", Wiki.getStudies(subclassJson.getString("qid")));
					for (Object study : subclassJson.getJSONArray("studies")) {
						JSONObject studyJson = (JSONObject) study;
						try{
							studyJson.put("instanceCounts", Wiki.countInstenceOfs(studyJson.getString("qid"),recursiveInstances));
						}catch(Exception e){
							studyJson.put("instanceCounts", -1);
						}
					}
				}
			}

		}
		FileUtils.writeStringToFile(new File("wissensbereicheMitSubclass.json"), Wissensbereiche.toString(4),Charset.forName("utf-8"));
	}

	public static void formatTable() throws JSONException, IOException{
		JSONArray json = new JSONArray(FileUtils.readFileToString(new File("wissensbereicheMitSubclass.json"),Charset.forName("utf-8")));
		StringBuilder rows = new StringBuilder();
		for (Object object : json) {
			rows.append(formatBereich(null,(JSONObject)object));
			rows.append("\\midrule").append(System.lineSeparator());
			rows.append("\\midrule").append(System.lineSeparator());
		}
		FileUtils.writeStringToFile(new File("texOutputs/listeBereiche.tex"),FileUtils.readFileToString(new File("texOutputs/listeBereichePlain.tex"),"utf-8").replace("xxxxxx", rows),"utf-8");
	}

	public static String formatBereich(String hypernymName, JSONObject bereich){
		StringBuilder rows = new StringBuilder();
		if(hypernymName != null)
			rows.append("").append("$\\Leftarrow$ ");
		rows.append(bereich.getString("name"));
		if(bereich.has("studies") && bereich.getJSONArray("studies").length() > 0){
			for (Object study : bereich.getJSONArray("studies")) {
				rows.append(" & ").append(((JSONObject)study).getString("name")).append(" & ").append(((JSONObject)study).getInt("instanceCounts")).append(" \\\\").append(System.lineSeparator());
			}
		}else{
			rows.append(" & $\\emptyset$ \\\\").append(System.lineSeparator());
		}
		if(bereich.has("subclasses")){
			for (Object subclass : bereich.getJSONArray("subclasses")) {
				rows.append("\\cdashlinelr{2-3}").append(System.lineSeparator());
				rows.append(formatBereich(bereich.getString("name"), (JSONObject)subclass));

			}
		}
		return rows.toString();
	}
}
