package graph;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class GraphSimilarityTayammoto {

	public float getSimilarity(CachedGraph a, CachedGraph b){
		Set<String>edgeSetA = getEdgeSet(a.graph);
		Set<String>edgeSetB = getEdgeSet(b.graph);
		
		Set<String>union = new HashSet<>(edgeSetA);
		union.addAll(edgeSetB);
		
		Set<String>intersection = new HashSet<>(edgeSetA);
		intersection.retainAll(edgeSetB);
		
		System.out.println(union.size());
		System.out.println(intersection.size());
		
		return intersection.size()/(float)union.size();
	}
	
	public float getSimilarity(Graph a, Graph b){
		Set<String>edgeSetA = getEdgeSet(a);
		Set<String>edgeSetB = getEdgeSet(b);
		
		Set<String>union = new HashSet<>(edgeSetA);
		union.addAll(edgeSetB);
		
		Set<String>intersection = new HashSet<>(edgeSetB);
		intersection.retainAll(edgeSetB);
		
		System.out.println(union.size());
		System.out.println(intersection.size());
		
		return intersection.size()/(float)union.size();
	}
	
	public Set<String>getEdgeSet(Graph<String,DefaultEdge> a){
		Set<String>edgeSet = new HashSet<>();
		for (DefaultEdge edge : a.edgeSet()) {
			edgeSet.add(a.getEdgeSource(edge)+","+a.getEdgeTarget(edge));
		} 
		return edgeSet;
	}

}
