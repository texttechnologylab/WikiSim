package graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jgrapht.Graph;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.SupplierUtil;

public class RandomGraph {

	public static void main(String[] args) throws IOException {
//		File[]categories = new File("graphsCleaned/oecdTopics/content").listFiles();
//		for (File category : categories) {
//			File[]languages = category.listFiles();
//			for (File language : languages) {
//				System.out.println(language);
//				List<String>lines = FileUtils.readLines(language);
//				List<String>output = new ArrayList<String>();
//				for (String string : lines) {
//					output.add(string);
//					if(string.trim().equals("Edges:"))
//						break;
//				}
//				for (int count = 0; count < 100; count++) {
//					ArrayList<String>random = new ArrayList<>(output);
//					Path resultFile = Paths.get("graphsCleaned/oecdTopics/randomGml",""+count,category.getName(),language.getName());
//					List<String> randomBF = FileUtils.readLines(resultFile.toFile());
//					boolean inEdges = false;
//					
//					for (String line : randomBF) {
//						if(inEdges)
//							random.add(line);
//						if(line.trim().equals("Edges:"))
//							inEdges = true;
//					}
//					FileUtils.writeLines(Paths.get("graphsCleaned/oecdTopics/randomContent",""+count,category.getName(),language.getName()).toFile(), lines);
//				}
//			}
//		}
		randomCleandGraphs("graphsCleaned/oecdTopics/gml","graphsCleaned/oecdTopics/randomGml" );
	}

	public static void randomCleandGraphs(String inputPath,String outputPath) throws IOException{
		File[]categories = new File(inputPath).listFiles();
		for (int count = 0; count < 100; count++) {
			System.out.println(count);
			for (File category : categories) {
				File[]languages = category.listFiles();
				for (File language : languages) {
					if(language.getName().endsWith(".bf")){
						Graph<String,DefaultEdge> graph = BF2JGraphT.readBFGraph(language);

						GraphGenerator<String, DefaultEdge,String> randomGen = new GnmRandomGraphGenerator(graph.vertexSet().size(),graph.edgeSet().size(),System.currentTimeMillis(),true,false);
						Graph<String, DefaultEdge> randomGraph = new DefaultDirectedGraph<String, DefaultEdge>(SupplierUtil.createStringSupplier(),SupplierUtil.createDefaultEdgeSupplier(),false);	
						randomGen.generateGraph(randomGraph);

						ArrayList<String>originalVertices = new ArrayList<>(graph.vertexSet());
						ArrayList<String>randomVertices = new ArrayList<>(randomGraph.vertexSet());

						for (int i = 0; i < originalVertices.size(); i++) {
							replaceVertex(randomGraph, randomVertices.get(i), originalVertices.get(i));
						}

						Path resultFile = Paths.get(outputPath,""+count,category.getName(),language.getName().replace(".bf", ""));
						resultFile.toFile().getParentFile().mkdirs();								
						CachedGraph.saveGraph(randomGraph, resultFile.toString(),true);
					}
				}
			}
		}	
	}

	public static <V, E> void replaceVertex(Graph<V, E> graph, V vertex, V replace) {
		graph.addVertex(replace);
		for (E edge : graph.outgoingEdgesOf(vertex)){ 
			graph.addEdge(replace, graph.getEdgeTarget(edge));
		}
		for (E edge : graph.incomingEdgesOf(vertex)) 
			graph.addEdge(graph.getEdgeSource(edge), replace);
		graph.removeVertex(vertex);
	}

}
