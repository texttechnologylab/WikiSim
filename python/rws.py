# We want to use the DeltaCon Framework to Compute Graph Similarities that are based on different
# vertex Similarities.

import numpy as np
import igraph
import os
import itertools


def rootED(S1: np.array, S2: np.array):
    """
    RootED distance function.

    See Equation 3.3 of
    Koutra, Vogelstein, and Faloutsos (2012)
    DELTACON A principled Massive-Graph Similarity Function
    """
    if S1.shape != S2.shape:
        raise ValueError(f'S1 ({S1.shape}) and S2 ({S2.shape}) must have same dimensions')

    sqrt_diff = np.sqrt(S1) - np.sqrt(S2)
    flat = sqrt_diff.flatten()
    sqared_sum = np.dot(flat, flat)

    return np.sqrt(sqared_sum)


def test_rootED1():
    S1 = np.array([[1, 2], [3, 4]])
    S2 = np.array([[1, 1], [2, 2]])
    return rootED(S1, S2)


def _test_rootED2():
    S1 = np.array([[1, 2], [3, 4]])
    S2 = np.array([[1, 1], [2, 2], [3, 3]])
    try:
        rootED(S1, S2)
    except ValueError:
        # everything is fine
        return True
    finally:
        raise Exception('Should raise a ValueError, but doesn\'t.')


def personalized_rw_affinities(g: igraph.Graph, weights: np.array=None):
    """
    Compute node affinities using personalized_pagerank for each node.
    :param g: a graph
    :param weights: edge weights, if necessary (higher means more important)
    :return: affinity matrix
    """
    return np.vstack([g.personalized_pagerank(vertices=None, reset_vertices=v, weights=weights) for v in range(g.vcount())])



def shortest_path_dists(g: igraph.Graph, weights: np.array=None):
    """
    Compute Shortest Path Distance Matrix of a Graph
    :param g: a graph
    :param weights: edge weights, if necessary (higher means more expensive, i.e., less important)
    :return:
    """
    dists = np.array(g.shortest_paths(source=None, target=None, weights=weights))
    return dists


def shortest_path_affinities(g: igraph.Graph, weights: np.array=None):
    """
    Shortest Path distances are great. But DeltaCON expect affinities,
    which are high if similarity is high. Hence we fix this by subtracting
    the distance matrix from the matrix that has diameter in every entry.
    :param g: some graph
    :param weights: edge weights, if necessary (higher means more expensive, i.e., less important)
    :return: affinity matrix
    """
    dists = shortest_path_dists(g, weights)
    diameter = np.max(dists)
    return diameter - dists


def deltaCon(G1: igraph.Graph, G2: igraph.Graph, affinities=personalized_rw_affinities):
    """
    A similarity measure for two graphs on the same vertex set. That is, we assume that
    G1 and G2 have the same vertex set but may have different edge sets. The implementation
    expects the vertex sets of G1 and G2 to be ordered in the same way, i.e.
    the ith vertex in G1 is also the ith vertex in G2.

    See Algorithm 1 of
    Koutra, Vogelstein, and Faloutsos (2012)
    DELTACON A principled Massive-Graph Similarity Function

    :param G1: first graph
    :param G2: second graph
    :param affinities: some function that returns a numpy array of node affinities
    :return: a similarity of G1 and G2
    """
    if G1.vcount() != G2.vcount():
        raise ValueError('Graphs must have same vertex sets.')

    S1 = affinities(G1)
    S2 = affinities(G2)

    d = rootED(S1, S2)
    return 1.0 / (1.0 + d)


def test_deltaCon1(affinities=personalized_rw_affinities):
    g1 = igraph.Graph()
    g1.add_vertices(4)
    g1.add_edges([(0,1), (1,2), (2,3), (3,0)])
    if deltaCon(g1, g1, affinities=affinities) != 1.0:
        raise Exception('result should be equal to one')


def test_deltaCon2():
    """
    Load the different wikipedias for some topic
    :param data_folder:
    :return:
    """
    data_folder = '../graphs/gml/fussballligaGML'

    files = filter(lambda x: x.endswith('.gml'), os.listdir(data_folder))
    files = filter(lambda x: x == 'en.gml' or x == 'pl.gml', files)

    graphs = dict()
    for file in files:
        g = igraph.read(os.path.join(data_folder, file))
        graphs[file.split('.')[0]] = g

    graphs = vertex_set_union(graphs)
    for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
        sim = deltaCon(graphs[l1], graphs[l2], affinities=personalized_rw_affinities)
        print(l1, l2, sim)

    # en en 1.0
    # en pl 0.038854209956132514


def vertex_set_union(graphs):
    """
    We want the different wikipedias to be on the same vertex sets.
    That is, for each label (i.e., an article) the vertex ids in the different graphs must be identical
    :param graphs:
    :return:
    """
    new_graphs = dict()

    labels = set()
    for k in graphs.keys():
        g = graphs[k]
        for v in g.vs:
            labels.add(v['label'])

    # new unique vertex ids in the large graph
    label_ids = {l: i for i, l in enumerate(labels)}
    id_labels = {i: l for i, l in enumerate(labels)}

    for k in graphs.keys():
        g = graphs[k]
        g_new = igraph.Graph()
        g_new.add_vertices(len(labels))
        for i, v in enumerate(g_new.vs):
            v['label'] = id_labels[i]
        edges = [(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']]) for e in g.es]
        g_new.add_edges(edges)
        new_graphs[k] = g_new

    return new_graphs


def test_vertex_set_union():
    l1 = ['a', 'b', 'c', 'd']
    g1 = igraph.Graph()
    g1.add_vertices(len(l1))
    for i, v in enumerate(g1.vs):
        v['label'] = l1[i]
        g1.add_edge(i, (i+1) % len(l1))

    l2 = ['b', 'x', 'c', 'd', 'y']
    g2 = igraph.Graph()
    g2.add_vertices(len(l2))
    for i, v in enumerate(g2.vs):
        v['label'] = l2[i]
        g2.add_edge(i, (i+1) % len(l2))

    graphs = {'g1': g1, 'g2': g2}

    return vertex_set_union(graphs)


def load_multilayer_graph(data_folder):
    """
    Load the different wikipedias for some topic
    :param data_folder:
    :return:
    """
    files = filter(lambda x: x.endswith('.gml'), os.listdir(data_folder))
    graphs = dict()
    for file in files:
        g = igraph.read(os.path.join(data_folder, file))
        graphs[file.split('.')[0]] = g

    return graphs


def main_deltaCon(data_folder='../graphs/gml/fussballligaGML', output_folder='../output/gml/fussballligaGML', affinities=personalized_rw_affinities):

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    out_csv = open(os.path.join(output_folder, f'deltaCON_{affinities.__name__}.csv'), 'w')
    out_csv.write('Language1, Language2, SimilarityScore\n')

    unaligned_graphs = load_multilayer_graph(data_folder)
    graphs = vertex_set_union(unaligned_graphs)
    for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
        sim = deltaCon(graphs[l1], graphs[l2], affinities=affinities)
        print(l1, l2, sim)
        out_csv.write(f'{l1}, {l2}, {sim}\n')


def vertex_edge_set_on_labels(g: igraph.Graph):
    """
    Compute a representation of the graph that is based on the 'label' attributes of the vertices in g.
    That is, we assume that each vertex in g has a 'label' attribute and furthermore we assume that these
    labels are unique, that is, they can be used as vertex ids, instead of the integer ids used by igraph.
    :param g: a graph
    :return: vertices, edges
    """

    vertices = set()
    for v in g.vs:
        vertices.add(v['label'])

    # new unique vertex ids in the large graph
    id_labels = {i: l for i, l in enumerate(vertices)}

    edges = {(id_labels[e.source], id_labels[e.target]) for e in g.es}

    return vertices, edges


def computeGED(G1: igraph.Graph, G2: igraph.Graph):
    """
    Symmetric difference, normalized by sum of sizes.

    Distance function. See ged_similarity for accompanying similarity score

    See computeGED in ged.py for a networkx based implementation
    :param G1:
    :param G2:
    :return:
    """

    v1, e1 = vertex_edge_set_on_labels(G1)
    v2, e2 = vertex_edge_set_on_labels(G2)

    v_intersection = v1.intersection(v2)
    e_intersection = e1.intersection(e2)

    V = (len(v1) + len(v2) - 2 * len(v_intersection)) / (len(v1) + len(v2))
    E = (len(e1) + len(e2) - 2 * len(e_intersection)) / (len(e1) + len(e2))

    return 0.5 * (V + E)


def ged_similarity(G1: igraph.Graph, G2: igraph.Graph):
    return 1.0 / (1.0 + computeGED(G1, G2))


def vertex_edge_jaccard_similarity(G1: igraph.Graph, G2: igraph.Graph):
    """
    Compute Jaccard Similarity between edge sets and vertex sets and return their average.
    :param G1: a graph
    :param G2: a graph
    :return: 0.5 * (jaccard_similarity(V(G1), V(G2)) + jaccard_similarity(E(G1), E(G2)))
    """
    v1, e1 = vertex_edge_set_on_labels(G1)
    v2, e2 = vertex_edge_set_on_labels(G2)

    v_intersection = v1.intersection(v2)
    e_intersection = e1.intersection(e2)

    v_jaccard = len(v_intersection) / (len(v1) + len(v2) - len(v_intersection))
    e_jaccard = len(e_intersection) / (len(e1) + len(e2) - len(e_intersection))

    return 0.5 * (v_jaccard + e_jaccard)


def main_otherSim(data_folder='../graphs/gml/fussballligaGML', output_folder='../output/gml/fussballligaGML', similarity=vertex_edge_jaccard_similarity):

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    out_csv = open(os.path.join(output_folder, f'otherSim_{similarity.__name__}.csv'), 'w')
    out_csv.write('Language1, Language2, SimilarityScore\n')

    unaligned_graphs = load_multilayer_graph(data_folder)
    graphs = vertex_set_union(unaligned_graphs)
    for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
        sim = similarity(graphs[l1], graphs[l2])
        print(l1, l2, sim)
        out_csv.write(f'{l1}, {l2}, {sim}\n')


if __name__ == '__main__':
    main_otherSim(similarity=ged_similarity)
    main_otherSim(similarity=vertex_edge_jaccard_similarity)
    main_deltaCon(affinities=personalized_rw_affinities)
    main_deltaCon(affinities=shortest_path_affinities)
