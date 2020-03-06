//package graph;
//
//import java.io.File;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import javax.swing.JFrame;
//
//import org.hibernate.cfg.annotations.reflection.XMLContext.Default;
//import org.jgrapht.DirectedGraph;
//import org.jgrapht.Graph;
//import org.jgrapht.GraphPath;
//import org.jgrapht.alg.DirectedNeighborIndex;
//import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
//import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
//import org.jgrapht.ext.JGraphModelAdapter;
//import org.jgrapht.graph.DefaultDirectedGraph;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.io.EdgeProvider;
//import org.jgrapht.io.GmlImporter;
//import org.jgrapht.io.ImportException;
//import org.jgrapht.io.VertexProvider;
//
//
//public class GraphSimilarity {
//
////	public static void main(String...args) throws  InterruptedException, ImportException{
////		Graph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);	
////
////
////		VertexProvider<String> vp = new VertexProvider<String>()
////		{
////			@Override
////			public String buildVertex(String label, Map<String, String> attributes)
////			{
////
////				return attributes.get("label");
////			}
////		};
////
////		EdgeProvider<String, DefaultEdge> ep = new EdgeProvider<String, DefaultEdge>()
////		{
////			@Override
////			public DefaultEdge buildEdge(String from, String to, String label, Map<String, String> attributes) {
////				// TODO Auto-generated method stub
////				return directedGraph.getEdgeFactory().createEdge(from, to);
////			}
////		};
////
////
////
////		GmlImporter<String, DefaultEdge> importer = new GmlImporter<String, DefaultEdge>(vp,ep);
////		importer.importGraph(directedGraph, new File("graph/als_test.gml"));
////
////		//		JFrame frame = new JFrame();
////		//		frame.setSize(400, 400);
////		//		JGraph jgraph = new JGraph(new JGraphModelAdapter(directedGraph));
////		//		
////		//		JGraphLayout layout = new JGraphOrganicLayout(); // or whatever layouting algorithm
////		//	    JGraphFacade facade = new JGraphFacade(jgraph);
////		//	    layout.run(facade);
////		//	    Map nested = facade.createNestedMap(false, false);
////		//	    jgraph.getGraphLayoutCache().edit(nested);
////		//		
////		//		frame.getContentPane().add(jgraph);
////		//		frame.setVisible(true);
////		//		
////		//		
////		//		
////		//		while (true) {
////		//			Thread.sleep(2000);
////		//		}
////	}
//
//	public Graph joinGraph(Graph a, Graph b){
//		Graph<String,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
//		for (Object vertex : a.vertexSet()) {
//			graph.addVertex(vertex.toString());
//		}
//		for (Object vertex : b.vertexSet()) {
//			graph.addVertex(vertex.toString());
//		}
//		for (Object edge : a.edgeSet()) {
//			graph.addEdge(a.getEdgeSource(edge).toString(), a.getEdgeTarget(edge).toString());
//		}
//		for (Object edge : b.edgeSet()) {
//			graph.addEdge(b.getEdgeSource(edge).toString(), b.getEdgeTarget(edge).toString());
//		}
//		return graph;
//	}
//
//	public float getSimilarity(CachedGraph a, CachedGraph b){
//		//		Set<String> verticies = new HashSet<>(a.vertexSet());
//		//		verticies.addAll(b.vertexSet());
//
//		Graph<String,DefaultEdge> union = joinGraph(a.graph, b.graph);
//		//		DirectedNeighborIndex<String, DefaultEdge> neigborIndexA = new DirectedNeighborIndex<String,DefaultEdge>(a);
//		//		DirectedNeighborIndex<String, DefaultEdge> neigborIndexB = new DirectedNeighborIndex<String,DefaultEdge>(b);
//
//
//		FloydWarshallShortestPaths shortestA = a.fwsPath;
//		FloydWarshallShortestPaths shortestB  = b.fwsPath;
//		double diametarA = shortestA.getDiameter();
//		double diametarB = shortestB.getDiameter();
//
//		System.out.println("end");
//		//		DijkstraShortestPath<String, DefaultEdge> dijkstraAlgA =new DijkstraShortestPath<>(a);
//		//		DijkstraShortestPath<String, DefaultEdge> dijkstraAlgB =new DijkstraShortestPath<>(b);
//		float unionSize = 0;
//		float similarity = 0;
//
//		int count = 0;
//		System.out.println(union.edgeSet().size());
//
//		System.out.println("edgecount a : " + a.graph.edgeSet().size());
//		System.out.println("edgecount b : " + b.graph.edgeSet().size());
//
//		System.out.println("vertex a : " + a.graph.vertexSet().size());
//		System.out.println("vertex b : " + b.graph.vertexSet().size());
//		
//		for (DefaultEdge edgeInUnion : union.edgeSet()) {
//			String source = union.getEdgeSource(edgeInUnion);
//			String target = union.getEdgeTarget(edgeInUnion);
//			if(source.equals(target))
//				continue;
//			count++;
//
//			//			if(a.containsVertex(source) && a.containsVertex(target)){
//			//				GraphPath pathA = shortestA.getPath(source, target);
//			//				if(pathA != null){
//			//					double Alpha =1 - ((pathA.getLength() - 1)/diametarA);  
//			//					similarity += Alpha;
//			//					System.out.println(Alpha);
//			//				}
//			//			}
//			//			if(b.containsVertex(source) && b.containsVertex(target)){
//			//				GraphPath pathB = shortestB.getPath(source, target);
//			//				if(pathB != null){
//			//					double Alpha =1 - ((pathB.getLength() - 1)/diametarB) ;  
//			//					similarity += Alpha;
//			//					System.out.println(Alpha);
//			//				}
//			//			}
//			if(a.graph.containsVertex(source) && a.graph.containsVertex(target) && b.graph.containsVertex(source) && b.graph.containsVertex(target)){
//				GraphPath pathA = shortestA.getPath(source, target);
//				GraphPath pathB = shortestB.getPath(source, target);
//				if(pathA != null && pathB !=null ){
//					double AlphaA =1 - ((pathA.getLength() - 1)/(diametarA+1));  
////					if(AlphaA > 1)
////						AlphaA = 1;
//					double AlphaB =1 - ((pathB.getLength() - 1)/(diametarB+1));
////					if(AlphaB > 1)
////						AlphaB = 1;
//					similarity += AlphaA + AlphaB;
//				}
//			}
//		}
//
//		//		int count = 0;
//		//		for (String string : verticies) {
//		//			if(count++%1000==0)
//		//			System.out.println((float)count/verticies.size());
//		//			Set<String> neighborsOfA = new HashSet<>();
//		//			Set<String> neighborsOfB = new HashSet<>();
//		//			SingleSourcePaths<String, DefaultEdge> pathsA = null; 
//		//			SingleSourcePaths<String, DefaultEdge> pathsB = null; 
//		//			if(a.containsVertex(string)){
//		//				pathsA = dijkstraAlgA.getPaths(string);
//		//				neighborsOfA = (neigborIndexA.successorsOf(string));
//		//
//		//			}
//		//			if(b.containsVertex(string)){
//		//				pathsB = dijkstraAlgB.getPaths(string);
//		//				neighborsOfB = (neigborIndexB.successorsOf(string));
//		//			}
//		//
//		////			System.out.println(string);
//		//
//		//
//		//			Set<String> intersection = new HashSet<String>(neighborsOfA);
//		//			Set<String> union = new HashSet<String>(neighborsOfA);
//		//			intersection.retainAll(neighborsOfB);
//		//			union.addAll(neighborsOfB);
//		//			Set<String> disjunction = new HashSet<String>(union);
//		//			disjunction.removeAll(intersection);
//		//
//		//			similarity += intersection.size();
//		//
//		////
//		////			System.out.println(neighborsOfA);
//		////			System.out.println(neighborsOfB);
//		////			System.out.println("union: " + union);
//		////			System.out.println("intersection: " + intersection);
//		////			System.out.println("disjunction: " + disjunction);
//		////			System.out.println(intersection.size());
//		//			if(pathsA != null && pathsB != null)
//		//				for (String string2 : disjunction) {
//		//					GraphPath<String, DefaultEdge> pathA = (pathsA.getPath(string2));
//		//					GraphPath<String, DefaultEdge> pathB = (pathsB.getPath(string2));
//		////					System.out.println(pathA);
//		////					System.out.println(pathB);
//		//					if(pathA != null && pathB != null){
//		//						if(pathA.getLength() < pathB.getLength()){
//		//							float pathSim = (float) (1 - ((pathB.getLength()-1)/diameterB));
//		//							similarity +=pathSim;
//		//						}
//		//						else{
//		//							float pathSim = (float) (1 - ((pathA.getLength()-1)/diameterA));
//		//							similarity +=pathSim;
//		//						}
//		//						
//		////						Float pathSim = pathA.getLength()<pathB.getLength() ? pathA.getLength()/(float)pathB.getLength():pathB.getLength()/(float)pathA.getLength();
//		////						if(!pathSim.isNaN()){
//		////							similarity += pathSim;
//		////						}
//		////						System.out.println(similarity);
//		////						System.out.println(pathA.getLength()<pathB.getLength() ? pathA.getLength()/(float)pathB.getLength():pathB.getLength()/(float)pathA.getLength());
//		//					}
//		////					else
//		////						System.out.println(0);
//		//				}
//		////			System.out.println();
//		//			unionSize += union.size();
//		//			//			
//		//			//			for (DefaultEdge defaultEdge : edgeSetA) {
//		//			//				System.out.println(defaultEdge);
//		//			//			}
//		//		}
//		////		System.out.println(similarity/unionSize);
//		//		System.out.println(similarity);
//		//		System.out.println(unionSize);
//		//		return similarity/unionSize;
//		return similarity/(2*count);
//	}
//
//}
