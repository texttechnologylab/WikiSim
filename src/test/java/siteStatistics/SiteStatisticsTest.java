package siteStatistics;


import org.apache.commons.io.FileUtils;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SiteStatisticsTest {
	
	@Test
	public void testSetPageStatistic() throws UnsupportedEncodingException{
		PageStatistics pageStatistics = new PageStatistics("de","Thomas_Steinherr");
		assertEquals(7, pageStatistics.getCategories().size());
		assertEquals(0, pageStatistics.getImages().size());
		assertEquals(20, pageStatistics.getLinks().size());
		assertEquals(5, pageStatistics.getExtlinks().size());
		assertEquals(7860944, pageStatistics.getPageId());
		assertEquals(3, pageStatistics.getSections().size());
		assertEquals(3, pageStatistics.getBreadthOfTOC());
		assertEquals(1, pageStatistics.getDepthOfTOC());
		assertEquals(0, pageStatistics.getNumberOfTables());

		PageStatistics pageStatisticsFussball = new PageStatistics("de","Fußball-Weltmeisterschaft");
		assertEquals(13, pageStatisticsFussball.getNumberOfTables());

		PageStatistics pageStatistics3 = new PageStatistics("en","1992_Federal_Express_International");
		assertEquals(3, pageStatistics3.getNumberOfItemize());
		

		PageStatistics pageStatistics4 = new PageStatistics("en","AF+BG theorem");
		assertEquals(3, pageStatistics4.getNumberOfItemize());
		assertEquals(4, pageStatistics4.getBreadthOfTOC());
		assertEquals(4, pageStatistics4.getCategories().size());
	}

	@Test
	public void testOutput() throws IOException{
		File[]createdFiles = new File("/resources/public/hemati/WikipediaGraphs/V2/content").listFiles();
		for (File category : createdFiles) {
			File[]languages = category.listFiles();
			for (File file : languages) {
				System.out.println(file);

				List<String> created  = FileUtils.readLines(file);
				List<String> original= FileUtils.readLines(new File(file.getPath().replace("content", "gml")));

				assertEquals(created.size(), original.size());

				boolean inVertices = false;
				for (int i = 0; i < created.size(); i++) {
					if(created.get(i).trim().equals("Edges:"))
						inVertices = false;

					if(inVertices){
						System.out.println(created.get(i).split("¤")[0]);
						assertEquals(created.get(i).split("¤")[0], original.get(i).split("¤")[0]);
						Pattern pattern = Pattern.compile("\\[(.*?)\\]");
						Matcher matcher = pattern.matcher(created.get(i));
						boolean foundSV1 = false;
						boolean foundSV2 = false;
						while (matcher.find())
						{
							String[] match = matcher.group(1).split("¤");
							if(match[0].equals("SV1")){
								assertEquals(10, match.length);
								foundSV1 = true;
							}
							else if(match[0].equals("SV2")){
								assertEquals(101, match.length);
								foundSV2 = true;
							}
						}
						assertTrue(foundSV1);
						assertTrue(foundSV2);
					}
					else{
						if(i != 2)
							assertEquals(created.get(i), original.get(i));
					}

					if(created.get(i).trim().equals("Vertices:"))
						inVertices = true;


				}
			}
		}

	}
}
