package siteStatistics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.UnknownHostException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.UnsupportedAttributeException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.net.UrlEscapers;
import com.google.gson.GsonBuilder;

public  class PageStatistics{
	private HashSet<String>links = new HashSet<>();
	private HashSet<String>extlinks = new HashSet<>();
	private HashSet<String>categories = new HashSet<>();
	private HashSet<Section>sections = new HashSet<>();
	private HashSet<String>images = new HashSet<>();
	private long size;
	private long pageId;
	private int numberOfTables = 0;
	private int numberOfItemize = 0;
	private int numberOfNormdata = 0;
	private int numberOfReferences = 0;
	String apiOutput;
	Pattern normdatenPattern = null;

	public PageStatistics(String language,String title, NormdatenTemplate normdatenTemplate){
		if (normdatenTemplate != null) {
			// compile pattern based on language specific template name
			String normdatenTemplateName = normdatenTemplate.getNormdatenTemplate(language);
			normdatenPattern = Pattern.compile("\\{\\{" + normdatenTemplateName + "(.*?)}}");
		}

		//System.out.println(language+"\t"+title);
		title =  UrlEscapers.urlPathSegmentEscaper().escape(title).replace("+", "%2B").replace("&", "%26");
		initPageSizeAndId(language, title);
		//check, if page exists
		if(pageId > -1){
			initPageStatistic(language, title);
			initSections(language, title);
		}
	}

	public int getNumberOfReferences() {
		return numberOfReferences;
	}

	public void setNumberOfReferences(int numberOfReferences) {
		this.numberOfReferences = numberOfReferences;
	}

	public int getNumberOfNormdata() {
		return numberOfNormdata;
	}

	public void setNumberOfNormdata(int numberOfNormdata) {
		this.numberOfNormdata = numberOfNormdata;
	}

	public HashSet<String> getLinks() {
		return links;
	}

	public void setLinks(HashSet<String> links) {
		this.links = links;
	}

	public HashSet<String> getExtlinks() {
		return extlinks;
	}

	public void setExtlinks(HashSet<String> extlinks) {
		this.extlinks = extlinks;
	}

	public HashSet<String> getCategories() {
		return categories;
	}

	public void setCategories(HashSet<String> categories) {
		this.categories = categories;
	}

	public HashSet<Section> getSections() {
		return sections;
	}

	public void setSections(HashSet<Section> sections) {
		this.sections = sections;
	}

	public HashSet<String> getImages() {
		return images;
	}

	public void setImages(HashSet<String> images) {
		this.images = images;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getPageId() {
		return pageId;
	}

	public void setPageId(long pageId) {
		this.pageId = pageId;
	}

	public int getBreadthOfTOC(){
		int breadth = 0;
		for (Section section : getSections()) {
			if(section.level == 2)
				breadth++;
		}
		return breadth;
	}

	public int getDepthOfTOC(){
		int maxDepth = 0;
		for (Section section : getSections()) {
			if(section.level > maxDepth)
				maxDepth = section.level;
		}
		return maxDepth-1;
	}

	public int getNumberOfTables() {
		return numberOfTables;
	}

	public void setNumberOfTables(int numberOfTables) {
		this.numberOfTables = numberOfTables;
	}

	public int getNumberOfItemize() {
		return numberOfItemize;
	}

	public void setNumberOfItemize(int numberOfItemize) {
		this.numberOfItemize = numberOfItemize;
	}


	public void initPageStatistic(String language, String title){
		try {
			String clcontinue = null;
			String elcontinue = null;
			String plcontinue = null;
			String imcontinue = null;
			do{
				String url = "https://"+ language+ ".wikipedia.org/w/api.php?action=query&titles=" + title + ""
						+ "&prop=links|extlinks|categories|images"
						+ "&format=xml"
						+ "&cllimit=max&ellimit=max&pllimit=max&imlimit=max"+
						(clcontinue==null?"":"&clcontinue="+UrlEscapers.urlPathSegmentEscaper().escape(clcontinue))+ 
						(elcontinue==null?"":"&elcontinue="+UrlEscapers.urlPathSegmentEscaper().escape(elcontinue))+ 
						(plcontinue==null?"":"&plcontinue="+UrlEscapers.urlPathSegmentEscaper().escape(plcontinue))+ 
						(imcontinue==null?"":"&imcontinue="+UrlEscapers.urlPathSegmentEscaper().escape(imcontinue));
				Document doc =Jsoup.connect(url).get();

				getCategories().addAll(
						doc.select("api > query > pages > page > categories > cl")
						.stream()
						.map(element -> element.attr("title"))
						.collect(Collectors.toList())
						);

				getLinks().addAll(
						doc.select("api > query > pages > page > links > pl")
						.stream()
						.map(element -> element.attr("title"))
						.collect(Collectors.toList())
						);

				getExtlinks().addAll(
						doc.select("api > query > pages > page > extlinks > el")
						.stream()
						.map(element -> element.text())
						.collect(Collectors.toList())
						);

				getImages().addAll(
						doc.select("api > query > pages > page > images > im")
						.stream()
						.map(element -> element.attr("title"))
						.collect(Collectors.toList())
						);

				if(doc.select("continue").size() > 0){
					Element cont = doc.select("continue").first();
					if(cont.hasAttr("clcontinue"))
						clcontinue = cont.attr("clcontinue");
					else
						clcontinue = null;

					if(cont.hasAttr("elcontinue"))
						elcontinue = cont.attr("elcontinue");
					else
						elcontinue = null;

					if(cont.hasAttr("plcontinue"))
						plcontinue = cont.attr("plcontinue");
					else
						plcontinue = null;

					if(cont.hasAttr("imcontinue"))
						imcontinue = cont.attr("imcontinue");
					else
						imcontinue = null;
				}
				else{
					clcontinue = null;
					elcontinue = null;
					plcontinue = null;
					imcontinue = null;
				}
			}while(clcontinue!=null || elcontinue != null || plcontinue != null || imcontinue != null);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}



	public void initPageSizeAndId(String language, String title){
		try {
			String url = "https://" + language + ".wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=size&format=xml&titles=" + title + "&redirects";
			Document output = Jsoup.connect(url) 
					.maxBodySize(0).get();
			if(output.select("api > query > pages > page").first().hasAttr("pageid")){
				setPageId(Long.parseLong(output.select("api > query > pages > page").first().attr("pageid")));
				setSize(Long.parseLong(output.select("api > query > pages > page > revisions > rev").first().attr("size")));
			}
			else
				pageId = -1;
		} catch (IOException e) {
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			initPageSizeAndId(language, title);
		}
	}


	public void initSections(String language, String title){

		try {
			String url = "https://" + language + ".wikipedia.org/w/api.php?action=parse&page=" + title + "&prop=sections|wikitext|text&format=xml";
			Document doc;
			doc = Jsoup.connect(url).get();
			getSections().addAll(
					doc.select("api > parse > sections > s ")
					.stream()
					.map(element -> new Section(element.attr("line"),Integer.parseInt(element.attr("level"))))
					.collect(Collectors.toList())
					);

			try{
				String wikitext = doc.select("api > parse > wikitext").first().text();
				setNumberOfTables(StringUtils.countMatches(wikitext, "wikitable"));
				setNumberOfItemize(StringUtils.countMatches(wikitext, "*"));

				// count Normdaten
				// {{Normdaten|TYP=p|GND=119545373|LCCN=n/94/109915|NDL=001183709|VIAF=12584821}}
				if (normdatenPattern != null) {
					Matcher matcher = normdatenPattern.matcher(wikitext);
					while (matcher.find()) {
						String normdatenString = matcher.group();
						// count elements, they are separated by "|", subtract 2 for first "{{Normdaten" and "TYP"
						int normdatenCount = normdatenString.split("\\|").length - 2;
						setNumberOfNormdata(normdatenCount);
					}
				}

				// count References
				// <ref
				setNumberOfReferences(StringUtils.countMatches(wikitext, "<ref"));

			}catch(NullPointerException e){
				e.printStackTrace();
			}
			apiOutput = doc.toString();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			initSections(language, title);
		}
	}
}

