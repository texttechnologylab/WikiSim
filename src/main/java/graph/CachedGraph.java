package graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;

import org.apache.commons.io.FileUtils;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.GmlExporter;
import org.jgrapht.io.GmlImporter;
import org.jgrapht.io.ImportException;
import org.jgrapht.io.VertexProvider;


public class CachedGraph {
	public Graph<String, DefaultEdge> graph;

	public CachedGraph(String path,boolean calcDist) throws ImportException{
		graph = loadGraph(path);
		//		simplifyGraph(graph);
	}

	private static Graph<String, DefaultEdge> createStringGraph()
	{
		Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

		String v1 = "v1";
		String v2 = "v2";
		String v3 = "v3";
		String v4 = "v4";

		// add the vertices
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);

		// add edges to create a circuit
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		g.addEdge(v3, v4);

		return g;
	}

	public static void simplifyGraph(Graph graph,HashSet<String>filter){
		Set<String> verticies = graph.vertexSet();
		Set<String>toremove = new HashSet();
		for (String string : verticies) {
			//			if(graph.outDegreeOf(string)==0 && graph.inDegreeOf(string) > 0)
			if(!filter.contains(string))
				toremove.add(string);
		}
		graph.removeAllVertices(toremove);
	}



	public static Graph<String, DefaultEdge> loadGraph(String path) throws ImportException{
		Graph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);	


		VertexProvider<String> vp = new VertexProvider<String>()
		{
			@Override
			public String buildVertex(String id, Map<String, Attribute> attributes) {
				return attributes.get("label").toString();
			}
		};

		EdgeProvider<String, DefaultEdge> ep = new EdgeProvider<String, DefaultEdge>()
		{

			@Override
			public DefaultEdge buildEdge(String from, String to, String label, Map<String, Attribute> attributes) {
				return directedGraph.addEdge(from, to);
			}

		};



		GmlImporter<String, DefaultEdge> importer = new GmlImporter<String, DefaultEdge>(vp,ep);
		importer.importGraph(directedGraph, new File(path));
		return directedGraph;
	}

	public void saveGraph(String path){
		saveGraph(this.graph,path,true);
	}

	public static void saveGraph(Graph<String, DefaultEdge> graph,String path,boolean writeBF){
		FileWriter w;
		try {
			GmlExporter exporter = new GmlExporter<>();
			w = new FileWriter(path);
			exporter.setParameter(GmlExporter.Parameter.EXPORT_VERTEX_LABELS, true);
			//			exporter.setPrintLabels(GmlExporter.PRINT_VERTEX_LABELS);
			exporter.exportGraph(graph,w);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(writeBF){
			StringBuilder sb = new StringBuilder();
			sb.append("directed").append("\n");
			sb.append("SimilarityGraph").append("\n");
			sb.append("Vertex Attributes:").append("\n");
			sb.append("Edge Attributes:").append("\n");
			sb.append("Vertices:").append("\n");

			for (Object vertex : graph.vertexSet()) {
				sb.append(vertex.toString()+"¤").append("\n");
			}
			sb.append("Edges:").append("\n");
			for (DefaultEdge edge : graph.edgeSet()) {
				sb.append(graph.getEdgeSource(edge) + "¤" + graph.getEdgeTarget(edge) + "¤1.0¤").append("\n");
			} 
			try {
				FileUtils.writeStringToFile(new File(path+".bf"), sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
