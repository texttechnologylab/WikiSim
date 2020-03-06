package graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultEdge;

public class InputVertexFactory implements VertexFactory<String>{
	List<String>vertextSet;

	public InputVertexFactory(Graph<String, DefaultEdge> inputGraph) {
		this.vertextSet = new ArrayList<>(inputGraph.vertexSet());
	}
	@Override
	public String createVertex() {
		int pic = new Random().nextInt(vertextSet.size());
		String ret = vertextSet.get(pic);
		vertextSet.remove(ret);
		return ret;
	}

}
