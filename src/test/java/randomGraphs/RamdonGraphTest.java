package randomGraphs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import graph.BF2JGraphT;
import static org.junit.Assert.*;

public class RamdonGraphTest {
	@Test
	public void test() throws IOException{
		File[]categories = new File("graphsCleaned/gml").listFiles();
		for (File category : categories) {
			File[]languages = category.listFiles();
			for (File language : languages) {
				if(language.getName().trim().endsWith(".bf")){
					Graph<String,DefaultEdge> original = BF2JGraphT.readBFGraph(language);
					System.out.println(language);
					for (int i = 0; i < languages.length; i++) {
						Graph<String,DefaultEdge> random = BF2JGraphT.readBFGraph(Paths.get("graphsCleaned/randomGml",""+i,category.getName(),language.getName()).toFile());
						assertEquals(original.vertexSet(), random.vertexSet());
						assertEquals(original.edgeSet().size(), random.edgeSet().size());
						if(original.edgeSet().size() > 0)
							assertNotEquals(original.edgeSet(), random.edgeSet());

					}

				}

			}
		}

	}
}
