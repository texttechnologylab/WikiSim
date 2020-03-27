# We want to use the DeltaCon Framework to Compute Graph Similarities that are based on different
# vertex Similarities.

import numpy as np
import igraph
import os
import itertools
import time

def rootED(S1: np.array, S2: np.array):
    """
    RootED distance function.

    Assumes that all entries in S1 and S2 are nonnegative.

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


def personalized_rw_affinities(g: igraph.Graph, weights: np.array=None, verbose: str=None):
    """
    Compute node affinities using personalized_pagerank for each node.
    Guarantees that all entries in the result are nonnegative.
    :param g: a graph
    :param weights: edge weights, if necessary (higher means more important)
    :return: affinity matrix
    """
    if verbose is not None:
        print(f'Computing Personalized RW affinities for {verbose}')

    affinities = np.vstack([g.personalized_pagerank(vertices=None, reset_vertices=v, weights=weights) for v in range(g.vcount())])
    affinities[affinities < 0.0] = 0.0
    return affinities


def shortest_path_dists(g: igraph.Graph, weights: np.array=None):
    """
    Compute Shortest Path Distance Matrix of a Graph
    :param g: a graph
    :param weights: edge weights, if necessary (higher means more expensive, i.e., less important)
    :return:
    """
    dists = np.array(g.shortest_paths(source=None, target=None, weights=weights))
    return dists


def shortest_path_affinities(g: igraph.Graph, weights: np.array=None, verbose: str=None):
    """
    Shortest Path distances are great. But DeltaCON expect affinities,
    which are high if similarity is high. Hence we fix this as follows:

    1) We find the `diameter` d  of the graph, that is, the largest finite distance in g.
    2) We set each finite distance pq to (d + 1) - pq and each infinite distance to 0
    3) we divide this result by d+1 to get values between 0 and 1

    Thus, each vertex has affinity between 0 and 1 to all other vertices and affinity equal to
    1 exactly to itself (assuming positive edge lengths).

    :param g: some graph
    :param weights: edge weights, if necessary (higher means more expensive, i.e., less important)
    :return: affinity matrix
    """
    if verbose is not None:
        print(f'Computing Shortest Path affinities for {verbose}')

    dists = shortest_path_dists(g, weights)
    finite_filter = np.isfinite(dists)
    diameter = np.max(dists[finite_filter])
    affinities = (diameter + 1) - dists
    affinities[~finite_filter] = 0.0
    return affinities / (diameter+1)


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


def deltaCon_cached(S1: np.ndarray, S2: np.ndarray):
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
    if S1.shape != S2.shape:
        raise ValueError('Graphs must have same vertex sets.')

    d = rootED(S1, S2)
    return 1.0 / (1.0 + d)


def vertex_set_union(graphs, directed=True):
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
        g_new = igraph.Graph(directed=directed)
        g_new.add_vertices(len(labels))
        for i, v in enumerate(g_new.vs):
            v['label'] = id_labels[i]
        edges = [(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']]) for e in g.es]
        g_new.add_edges(edges)
        new_graphs[k] = g_new

    return new_graphs


def union(graphs, directed=True):
    """
    Compute a graph union of all graphs in the dict.
    :param graphs:
    :return:
    """

    labels = set()
    for k in graphs.keys():
        g = graphs[k]
        for v in g.vs:
            labels.add(v['label'])

    # new unique vertex ids in the large graph
    label_ids = {l: i for i, l in enumerate(labels)}
    id_labels = {i: l for i, l in enumerate(labels)}

    g_new = igraph.Graph(directed=directed)
    g_new.add_vertices(len(labels))
    for k in graphs.keys():
        g = graphs[k]
        for i, v in enumerate(g_new.vs):
            v['label'] = id_labels[i]
        edges = [(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']]) for e in g.es]
        g_new.add_edges(edges)

    return g_new


def intersection(g1: igraph.Graph, g2: igraph.Graph, directed: bool=True):
    """
    - Compute the intersection V' of the vertex sets of g1 and g2 (based on their labels, not ids).
    - compute g1[V'] and g2[V'], i.e. the induced subgraphs of V' of both graphs.
    :param g1, g2 graphs
    :return: g1_new, g2_new
    """

    labels = {v['label'] for v in g1.vs}
    labels.intersection_update({v['label'] for v in g2.vs})
    labels = list(labels)

    # new unique vertex ids in the large graph
    label_ids = {l: i for i, l in enumerate(labels)}

    def restrict_to_intersection(g):
        g_new = igraph.Graph(directed=directed)
        g_new.add_vertices(len(labels))
        g_new.vs['label'] = labels
        for e in g.es:
            try:
                g_new.add_edge(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']])
            except KeyError:
                pass
        return g_new

    return restrict_to_intersection(g1), restrict_to_intersection(g2)


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


def main_deltaCon(graphs, output_folder='../output/gml/fussballligaGML', affinities=personalized_rw_affinities):
    """
    Generic, rather agnostic variant of deltacon. computes a similarity score based on the affinity
    function provided for each pair of graphs in the graphs dict.
    :param graphs:
    :param output_folder:
    :param affinities:
    :return:
    """

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    with open(os.path.join(output_folder, f'deltaCON_{affinities.__name__}.csv'), 'w') as out_csv:
        out_csv.write('Language1, Language2, SimilarityScore, Time\n')

        for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
            tic = time.time()
            sim = deltaCon(graphs[l1], graphs[l2], affinities=affinities)
            toc = time.time()
            print(l1, l2, sim, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim}, {toc-tic}\n')


def main_deltaCon_intersection(graphs, output_folder='../output/gml/fussballligaGML', affinities=personalized_rw_affinities):
    """
    Intersection variant of deltacon: For each pair of graphs, compute the induced subgraphs on the vertex set
    intersection and then proceed with the affinity computation.

    :param graphs:
    :param output_folder:
    :param affinities:
    :return:
    """

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    with open(os.path.join(output_folder, f'deltaCON_intersection_{affinities.__name__}.csv'), 'w') as out_csv:
        out_csv.write('Language1, Language2, SimilarityScore, Time\n')

        for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
            g1, g2 = intersection(graphs[l1], graphs[l2])
            tic = time.time()
            sim = deltaCon(g1, g2, affinities=affinities)
            toc = time.time()
            print(l1, l2, sim, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim}, {toc-tic}\n')


def main_deltaCon_cached(affinities, name, output_folder='../output/gml/fussballligaGML'):
    """
    Faster variant of DeltaCon that precomputes affinities for all graphs, instead of recomputing them for each pair.
    May be used for union, but not for intersection, as here graphs change based on the current pair.
    :param affinities:
    :param name:
    :param output_folder:
    :return:
    """

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    with open(os.path.join(output_folder, f'deltaCON_{name}.csv'), 'w') as out_csv:
        out_csv.write('Language1, Language2, SimilarityScore, Time\n')

        for l1, l2 in itertools.combinations_with_replacement(affinities.keys(), 2):
            tic = time.time()
            sim = deltaCon_cached(affinities[l1], affinities[l2])
            toc = time.time()
            print(l1, l2, sim, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim}, {toc-tic}\n')


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

    try:
        V = (len(v1) + len(v2) - 2 * len(v_intersection)) / (len(v1) + len(v2))
    except ZeroDivisionError:
        V = 0.0
    try:
        E = (len(e1) + len(e2) - 2 * len(e_intersection)) / (len(e1) + len(e2))
    except ZeroDivisionError:
        E = 0.0

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

    try:
        v_jaccard = len(v_intersection) / (len(v1) + len(v2) - len(v_intersection))
    except ZeroDivisionError:
        v_jaccard = 0.0
    try:
        e_jaccard = len(e_intersection) / (len(e1) + len(e2) - len(e_intersection))
    except ZeroDivisionError:
        e_jaccard = 0.0

    return 0.5 * (v_jaccard + e_jaccard)


def vertex_jaccard_similarity(G1: igraph.Graph, G2: igraph.Graph):
    """
    Compute Jaccard Similarity between vertex sets.
    :param G1: a graph
    :param G2: a graph
    :return: jaccard_similarity(V(G1), V(G2))
    """
    v1, e1 = vertex_edge_set_on_labels(G1)
    v2, e2 = vertex_edge_set_on_labels(G2)

    v_intersection = v1.intersection(v2)

    try:
        v_jaccard = len(v_intersection) / (len(v1) + len(v2) - len(v_intersection))
    except ZeroDivisionError:
        v_jaccard = 0.0

    return v_jaccard


def edge_jaccard_similarity(G1: igraph.Graph, G2: igraph.Graph):
    """
    Compute Jaccard Similarity between edge sets and vertex sets and return their average.
    :param G1: a graph
    :param G2: a graph
    :return: 0.5 * (jaccard_similarity(V(G1), V(G2)) + jaccard_similarity(E(G1), E(G2)))
    """
    v1, e1 = vertex_edge_set_on_labels(G1)
    v2, e2 = vertex_edge_set_on_labels(G2)

    e_intersection = e1.intersection(e2)

    try:
        e_jaccard = len(e_intersection) / (len(e1) + len(e2) - len(e_intersection))
    except ZeroDivisionError:
        e_jaccard = 0.0

    return e_jaccard


def main_otherSim(graphs, output_folder='../output/gml/fussballligaGML', similarity=vertex_edge_jaccard_similarity):
    """
    Nice and general similarity computation for all pairs of graphs in the graphs dict.
    :param graphs:
    :param output_folder:
    :param similarity:
    :return:
    """

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    with open(os.path.join(output_folder, f'otherSim_{similarity.__name__}.csv'), 'w') as out_csv:
        out_csv.write('Language1, Language2, SimilarityScore, Time\n')

        for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
            tic = time.time()
            sim = similarity(graphs[l1], graphs[l2])
            toc = time.time()
            print(l1, l2, sim, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim}, {toc-tic}\n')


def main_otherSim_intersection(graphs, output_folder='../output/gml/fussballligaGML', similarity=vertex_edge_jaccard_similarity):
    """
    Compute specified similarity on pairs of graphs in graphs dict. However, before computing the similarity of a pair
    of graphs, compute the induced graphs on the vertex set intersection before proceeding.
    :param graphs:
    :param output_folder:
    :param similarity:
    :return:
    """

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    with open(os.path.join(output_folder, f'otherSim_intersection_{similarity.__name__}.csv'), 'w') as out_csv:
        out_csv.write('Language1, Language2, SimilarityScore, Time\n')

        for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
            g1, g2 = intersection(graphs[l1], graphs[l2])
            tic = time.time()
            sim = similarity(g1, g2)
            toc = time.time()
            print(l1, l2, sim, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim}, {toc-tic}\n')
