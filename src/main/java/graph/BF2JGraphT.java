package graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class BF2JGraphT {

	public static void main(String[] args) throws IOException {
		File[]categories = new File("graphs/gml").listFiles();
		for (File category : categories) {
			for (File language : category.listFiles()) {
				Graph<String, DefaultEdge> graph = readBFGraph(language);
				CachedGraph.saveGraph(graph, language.getAbsolutePath().replace(".bf", ""),false);
			}
		}
		
		
	}

	public static Graph<String, DefaultEdge> readBFGraph(File bfFile) throws IOException{
		Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		List<String> lines = FileUtils.readLines(bfFile);
		boolean inVertices = false;
		boolean inEdges = false;
		for (String string : lines) {
			if(string.trim().equals("Vertices:")){
				inVertices = true;
				inEdges = false;
				continue;
			}
			else if(string.trim().equals("Edges:")){
				inVertices = false;
				inEdges = true;
				continue;
			}
			if(inVertices){
				graph.addVertex(string.trim().split("¤")[0]);
			}
			else if(inEdges){
				String[] split = string.trim().split("¤");
				graph.addEdge(split[0], split[1]);
			}
		}
		return graph;
	}

}
