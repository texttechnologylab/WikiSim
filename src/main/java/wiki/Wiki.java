package wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

public class Wiki {

	static Connection con;
	static Statement stmt;
	static HashMap<String, Boolean> isEntityMap = deserializeHashMap();

	public Wiki() throws SQLException{
		con =  DriverManager.getConnection("jdbc:mysql://141.2.89.28:3306/wikipedia?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "ahemati","tXD3b%ld");
		stmt = con.createStatement();
	}

	public static List<String> getWikidataSubclassOf(String hypernymId) throws JSONException, IOException{
		String query = 
				"PREFIX wd: <http://www.wikidata.org/entity/> "+
						"PREFIX wdt: <http://www.wikidata.org/prop/direct/> "+
						"SELECT DISTINCT ?item "+
						"WHERE { "+
						"?item wdt:P279 wd:"+hypernymId+
						"}";
		String url = "https://query.wikidata.org/sparql?query="+query+"&format=json";
		JSONArray wikidataId = new JSONObject(Jsoup.connect(url).ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
		return getWikidataInstanceOfByJson(wikidataId);
	}



	public static List<String> getWikidataInstanceOf(String hypernymId) throws JSONException, IOException{
		String query = 
				"PREFIX wd: <http://www.wikidata.org/entity/> "+
						"PREFIX wdt: <http://www.wikidata.org/prop/direct/> "+
						"SELECT DISTINCT ?item "+
						"WHERE { "+
						"?item wdt:P31 wd:"+hypernymId+
						"}";
		//		String url = "https://query.wikidata.org/sparql?query="+query+"&format=json";
		String url = "http://rawindra.hucompute.org:9999/blazegraph/sparql?query="+query+"&format=json";

		JSONArray wikidataId = new JSONObject(Jsoup.connect(url).ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
		return getWikidataInstanceOfByJson(wikidataId);
	}

	public static boolean isEntity(String wikidataID) throws JSONException, IOException{
		if(isEntityMap.containsKey(wikidataID))
			return isEntityMap.get(wikidataID);
		String url = "https://query.wikidata.org/sparql?format=json&query=SELECT%20DISTINCT%20%3Fitem%20WHERE%20%7B%20%3Fitem%20%28wdt%3AP31%2Fwdt%3AP279%29%20wd%3A"+wikidataID+".%20%7D%0ALIMIT%201%0A";
		try{
			JSONArray wikidataId = new JSONObject(Jsoup.connect(url).ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
			isEntityMap.put(wikidataID, wikidataId.length() == 0);
			return wikidataId.length() == 0 ;
		}catch(SocketTimeoutException e){
			System.out.println("wait");
			System.out.println(url);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return isEntity(wikidataID);			
		}
	}

	public static List<String>getWikidataInstanceOfByJson(JSONArray json){
		List<String>output = new ArrayList<String>();
		for(int i = 0; i < json.length(); i++){
			if(json.getJSONObject(i).get("item") instanceof JSONObject)
				output.add(json.getJSONObject(i).getJSONObject("item").getString("value").replace("http://www.wikidata.org/entity/", ""));
			else
				output.add(json.getJSONObject(i).getString("item").replace("http://www.wikidata.org/entity/", ""));
		}
		return output;
	}

	public static List<String>getWikidataInstanceOfByFile(String path) throws JSONException, IOException{
		return getWikidataInstanceOfByJson(new JSONArray(FileUtils.readFileToString(new File(path), Charset.defaultCharset())));
	}


	public static String getLinkById(String id){
		try {
			String query = "Select ips_site_page from wb_items_per_site where ips_site_id = 'enwiki' and ips_item_id=" + id.replace("Q", "");
			ResultSet rs = stmt.executeQuery(query);

			while(rs.next()) {
				return IOUtils.toString(rs.getBlob("ips_site_page").getBinaryStream(),"UTF-8");
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return null;
	}

	/**
	 * Returns the Wikipedia links for a given wikidata Id
	 * @param id
	 * @return [language, link]
	 */
	public static List<String[]> getWikipediaLanguagesForWikidataId(String id){
		List<String[]> output = new ArrayList<>();
		try {
			String query = "Select ips_site_page,ips_site_id from wb_items_per_site where ips_item_id=" + id.replace("Q", "");
			ResultSet rs = stmt.executeQuery(query);


			while(rs.next()) {
				if(IOUtils.toString(rs.getBlob("ips_site_id").getBinaryStream(),"UTF-8").endsWith("quote") || IOUtils.toString(rs.getBlob("ips_site_id").getBinaryStream(),"UTF-8").equals("commonswiki") )
					continue;
				output.add(new String[]{
						IOUtils.toString(rs.getBlob("ips_site_id").getBinaryStream(),"UTF-8").replace("_", "-"),

						IOUtils.toString(rs.getBlob("ips_site_page").getBinaryStream(),"UTF-8")
				});
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public static HashMap<String, List<String>> getWikipediaByLanguagesForWikidataHyponym(String wikidataId) throws SQLException, JSONException, IOException{
		return getWikipediaByLanguagesForWikidataHyponym(Arrays.asList(new String[]{wikidataId}));
	}

	public static HashMap<String, List<String>> getWikipediaByLanguagesForWikidataHyponym(List<String> wikidataIds) throws SQLException, JSONException, IOException{
		HashMap<String, List<String>>linksByLanguage = new HashMap<>();

		con =  DriverManager.getConnection("jdbc:mysql://141.2.89.28:3306/wikipedia?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "ahemati","tXD3b%ld");
		stmt = con.createStatement();

		for (String wikidataId : wikidataIds) {
			int i = 1;
			for (String wikiId : getWikidataInstanceOf(wikidataId)) {
				for (String[] string : getWikipediaLanguagesForWikidataId(wikiId)) {
					if(!linksByLanguage.containsKey(string[0]))
						linksByLanguage.put(string[0],new ArrayList<>());
					linksByLanguage.get(string[0]).add(string[1]);
				}
				if(i++%1000==0)
					System.out.println(i);
			};
		}


		return linksByLanguage;
	}

	public static HashMap<String, List<String>> getWikipediaByLanguagesForWikidataHyponym(File jsonFile) throws SQLException, JSONException, IOException{
		HashMap<String, List<String>>linksByLanguage = new HashMap<>();

		con =  DriverManager.getConnection("jdbc:mysql://141.2.89.28:3306/wikipedia?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "ahemati","tXD3b%ld");
		stmt = con.createStatement();

		int i = 1;
		for (String wikiId : getWikidataInstanceOfByFile(jsonFile.getPath())) {
			for (String[] string : getWikipediaLanguagesForWikidataId(wikiId)) {
				if(!linksByLanguage.containsKey(string[0]))
					linksByLanguage.put(string[0],new ArrayList<>());
				linksByLanguage.get(string[0]).add(string[1]);
			}
			if(i++%1000==0)
				System.out.println(i);
		};
		return linksByLanguage;
	}

	public static HashSet<String>getEntities(String pid, String id) throws JSONException, IOException{
		HashSet<String>ret = new HashSet<>();
		try{
			String query = 
					"SELECT distinct ?cid  WHERE { "+
							"?item wdt:"+pid+"* wd:"+id+". "+
							(!pid.equals("P106")?"?cid wdt:P31 ?item. ?article schema:about ?cid . ":"?item wdt:P135 ?p FILTER (?p IN (wd:Q207591, wd:Q17723 ) ).  ?article schema:about ?item . BIND(?item  AS ?cid) .")+ 
							"filter( regex(str(?article), \"wikipedia.org\" )) "+
							"} ";
			//		String url = "http://rawindra.hucompute.org:9999/blazegraph/sparql?query="+query+"&format=json";
			String url = "https://query.wikidata.org/sparql?query="+query+"&format=json";
			System.out.println(url);
			
			JSONArray wikidataId = new JSONObject(Jsoup.connect(url)
				     .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
				     .timeout(0).maxBodySize(0).ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
			for (Object object : wikidataId) {
				JSONObject current = (JSONObject)object;
				String wikidata = current.getJSONObject("cid").getString("value").replaceAll(".*/", "");
				ret.add(wikidata);
			}
		}catch(HttpStatusException e){
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return getEntities(pid,id);
		}
		return ret;
	}

	public static HashMap<String, List<WikipeidaWikidata>> getWikipediaLinksByLanguagesForWikidataHyponym(String pid, String id) throws SQLException, JSONException, IOException{
		try{
			HashMap<String, List<WikipeidaWikidata>>linksByLanguage = new HashMap<>();
			String query = 
					"SELECT ?cid ?country ?article WHERE {" +
//							"?cid wdt:" + pid + " wd:" + id + " . "+
							(pid.equals("P106")?"?cid wdt:P135 ?p FILTER (?p IN (wd:Q207591, wd:Q17723 ) ).":"?cid wdt:" + pid + " wd:" + id + " . ")+ 
							"?article schema:about ?cid . "+
							"filter( regex(str(?article), \"wikipedia.org\" ))"+
							"} " ;
			String url = "https://query.wikidata.org/sparql?query="+query+"&format=json";
			System.out.println(url);
			JSONArray wikidataId = new JSONObject(Jsoup.connect(url) 
				     .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
					.maxBodySize(0).ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
			for (Object object : wikidataId) {
				JSONObject current = (JSONObject)object;
				String language = current.getJSONObject("article").getString("value").replace("https://", "").replaceAll("\\..*", "");
				String wikidata = current.getJSONObject("cid").getString("value").replaceAll(".*/", "");
				String wikipedia = current.getJSONObject("article").getString("value").replaceAll(".*/", "");
				if(!linksByLanguage.containsKey(language))
					linksByLanguage.put(language,new ArrayList<>());
				linksByLanguage.get(language).add(new WikipeidaWikidata(wikipedia,language,wikidata));
			}
			return linksByLanguage;
		}catch(SocketTimeoutException e){
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return getWikipediaLinksByLanguagesForWikidataHyponym(pid,id);
		}
	}
	
	public static HashSet<String>getStudies(String qid) throws JSONException, IOException{
		String query = 
				"SELECT ?studies ?studiesLabel WHERE {wd:" + qid + " wdt:P2578 ?studies SERVICE wikibase:label {bd:serviceParam wikibase:language \"en\" }} " ;
		String url = "https://query.wikidata.org/sparql?query="+query+"&format=json";
		JSONArray wikidataId = new JSONObject(Jsoup.connect(url) 
			     .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
				.maxBodySize(0)
				.timeout(0)
				.ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
		HashSet<String>output = new HashSet<>();
		for (Object object : wikidataId) {
			JSONObject current = (JSONObject)object;
			String wikidata = current.getJSONObject("studies").getString("value").replaceAll(".*/", "");
			String studiesLabel = current.getJSONObject("studiesLabel").getString("value").replaceAll(".*/", "");
			
			output.add(wikidata+"\t"+studiesLabel);
		}
		return output;
	}
	
	public static HashMap<String, List<WikipeidaWikidata>> getWikipediaLinksByLanguagesForWikidataScienceCategory(String qid) throws SQLException, JSONException, IOException{
		try{
			HashMap<String, List<WikipeidaWikidata>>linksByLanguage = new HashMap<>();
			String query = 
					"SELECT ?cid ?article WHERE {wd:" + qid + " wdt:P2578 ?studies. ?cid wdt:P31 ?studies. ?article schema:about ?cid } " ;
			String url = "https://query.wikidata.org/sparql?query="+query+"&format=json";
			System.out.println(url);
			JSONArray wikidataId = new JSONObject(Jsoup.connect(url) 
				     .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
					.maxBodySize(0)
					.timeout(0)
					.ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
			for (Object object : wikidataId) {
				JSONObject current = (JSONObject)object;
				String language = current.getJSONObject("article").getString("value").replace("https://", "").replaceAll("\\..*", "");
				String wikidata = current.getJSONObject("cid").getString("value").replaceAll(".*/", "");
				String wikipedia = current.getJSONObject("article").getString("value").replaceAll(".*/", "");
				if(!linksByLanguage.containsKey(language))
					linksByLanguage.put(language,new ArrayList<>());
				linksByLanguage.get(language).add(new WikipeidaWikidata(wikipedia,language,wikidata));
			}
			return linksByLanguage;
		}catch(SocketTimeoutException e){
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return getWikipediaLinksByLanguagesForWikidataScienceCategory(qid);
		}
	}

	public static HashMap<String, List<WikipeidaWikidata>> getWikipediaLinksByLanguagesForWikidataHyponym(String pid, String id,int depth) throws SQLException, JSONException, IOException{
		try{
			HashMap<String, List<WikipeidaWikidata>>linksByLanguage = new HashMap<>();
			String query = 

					"SELECT ?cid ?country ?article WHERE {" +
							"?item wdt:"+pid+"* wd:"+id+". "+
							"?cid wdt:P31 ?item. "+ 
							"?article schema:about ?cid . "+ 
							"filter( regex(str(?article), \"wikipedia.org\" ))"+
							"}";
			String url = "https://query.wikidata.org/sparql?query="+query+"&format=json";
			System.out.println(url);
			JSONArray wikidataId = new JSONObject(Jsoup.connect(url).userAgent("Mozilla/5.0").maxBodySize(0).timeout(0).ignoreContentType(true).execute().body()).getJSONObject("results").getJSONArray("bindings");
			for (Object object : wikidataId) {
				JSONObject current = (JSONObject)object;
				String language = current.getJSONObject("article").getString("value").replace("https://", "").replaceAll("\\..*", "");
				String wikidata = current.getJSONObject("cid").getString("value").replaceAll(".*/", "");
				String wikipedia = current.getJSONObject("article").getString("value").replaceAll(".*/", "");
				if(!linksByLanguage.containsKey(language))
					linksByLanguage.put(language,new ArrayList<>());
				linksByLanguage.get(language).add(new WikipeidaWikidata(wikipedia,language,wikidata));
			}
			return linksByLanguage;
		}catch(SocketTimeoutException e){
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return getWikipediaLinksByLanguagesForWikidataHyponym(pid,id,depth);
		}
	}



	//	public static void main(String...args) throws SQLException, JSONException, IOException{
	//		con =  DriverManager.getConnection("jdbc:mysql://141.2.89.28:3306/wikipedia?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "ahemati","tXD3b%ld");
	//		stmt = con.createStatement();
	//		List<String>links = new ArrayList<String>();
	//
	//		HashMap<String, List<String>>linksByLanguage = new HashMap<>();
	//		int i = 1;
	//		for (String wikiId : getWikidataInstanceOfByFile("src/main/resources/query.json")) {
	//			for (String[] string : getWikipediaLinksByWikidataId(wikiId)) {
	//				if(!linksByLanguage.containsKey(string[0]))
	//					linksByLanguage.put(string[0],new ArrayList<>());
	//				linksByLanguage.get(string[0]).add(string[1]);
	//			}
	//			//			String link = getLinkById(wikiId);
	//			//			if(link != null)
	//			//				links.add(link);
	//			if(i++%1000==0)
	//				//				break;
	//
	//				System.out.println(i);
	//		};
	//		for (Entry<String, List<String>> entry : linksByLanguage.entrySet()) {
	//			FileUtils.writeLines(new File("links/"+ entry.getKey()+ "_links.txt"), entry.getValue());
	//		}
	//
	//		//		FileUtils.writeLines(new File("links.txt"), links);
	//		//		System.out.println(links.size());
	//	}

	public static void main(String...args) throws JSONException, IOException, SQLException{
		//		long start = System.currentTimeMillis();
		//		for(int i = 0; i<1000; i++){
		//			System.out.println("return size: " + getEntities("P31","Q"+new Random().nextInt(65943)).size());
		//			System.out.println(i);
		//		}
		//		System.out.println(System.currentTimeMillis()-start);
		Wiki wiki = new Wiki();
		for (String[] strings : wiki.getWikipediaLanguagesForWikidataId("Q1")) {
			System.out.println(Arrays.toString(strings));
		};
	}

	public static void serializeHashMap(){
		try
		{
			FileOutputStream fos =
					new FileOutputStream("hashmap.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(isEntityMap);
			oos.close();
			fos.close();
			System.out.println("Serialized HashMap data is saved in hashmap.ser");
		}catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public static HashMap deserializeHashMap(){
		HashMap<String, Boolean> map = new HashMap<>();
		try
		{
			FileInputStream fis = new FileInputStream("hashmap.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		}catch(IOException ioe)
		{
			ioe.printStackTrace();
		}catch(ClassNotFoundException c)
		{
			System.out.println("Class not found");
			c.printStackTrace();
		}
		return map;
	}
}
