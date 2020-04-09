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


def deltaCon_rw_lowmem(G1: igraph.Graph, G2: igraph.Graph):
    """
    A similarity measure for two graphs on the same vertex set. That is, we assume that
    G1 and G2 have the same vertex set but may have different edge sets. The implementation
    expects the vertex sets of G1 and G2 to be ordered in the same way, i.e.
    the ith vertex in G1 is also the ith vertex in G2.

    This variant uses less memory by processing aligned vertices individually and is restricted to
    random_walk similarities right now.

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

    def rw(g: igraph.Graph, v: int, weights=None):
        affinities = np.array(g.personalized_pagerank(vertices=None, reset_vertices=v, weights=weights))
        affinities[affinities < 0.0] = 0.0
        return affinities

    dist = 0.0
    kern = 0.0
    for v in range(G1.vcount()):
        S1 = rw(G1, v)
        S2 = rw(G2, v)
        sqrt_diff = np.sqrt(S1) - np.sqrt(S2)
        flat = sqrt_diff.flatten()
        dist += np.dot(flat, flat)
        kern += np.dot(S1, S2)

    d = np.sqrt(dist)
    return 1.0 / (1.0 + d), kern


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

    # collect vertex labels of all graphs
    labels = set()
    for k in graphs.keys():
        g = graphs[k]
        labels.update(g.vs['label'])

    # new unique vertex ids in the large graph
    labels = list(labels)
    label_ids = {l: i for i, l in enumerate(labels)}

    for k in graphs.keys():
        g = graphs[k]
        g_new = igraph.Graph(directed=directed)
        g_new.add_vertices(len(labels))
        g_new.vs['label'] = labels
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

    # collect vertex labels of all graphs
    labels = set()
    for k in graphs.keys():
        g = graphs[k]
        labels.update(g.vs['label'])

    # new unique vertex ids in the large graph
    labels = list(labels)
    label_ids = {l: i for i, l in enumerate(labels)}

    g_new = igraph.Graph(directed=directed)
    g_new.add_vertices(len(labels))
    g_new.vs['label'] = labels
    for k in graphs.keys():
        g = graphs[k]
        edges = [(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']]) for e in g.es]
        g_new.add_edges(edges)

    return g_new


def restrict_to_intersection(g1: igraph.Graph, g2: igraph.Graph, directed: bool=True):
    """
    - Compute the intersection V' of the vertex sets of g1 and g2 (based on their labels, not ids).
    - compute g1[V'] and g2[V'], i.e. the induced subgraphs of V' of both graphs.
    :param g1, g2 graphs
    :return: g1_new, g2_new
    """

    labels = set(g1.vs['label'])
    labels.intersection_update(g2.vs['label'])

    # new unique vertex ids in the small intersection graph
    labels = list(labels)
    label_ids = {l: i for i, l in enumerate(labels)}

    def __restrict_to_intersection(g):
        g_new = igraph.Graph(directed=directed)
        g_new.add_vertices(len(labels))
        g_new.vs['label'] = labels
        for e in g.es:
            try:
                g_new.add_edge(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']])
            except KeyError:
                pass # here, we have encountered an edge with at least one endpoint not in the vertex set intersection
        return g_new

    return __restrict_to_intersection(g1), __restrict_to_intersection(g2)


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
            g1, g2 = restrict_to_intersection(graphs[l1], graphs[l2])
            if g1.vcount() != 0:
                tic = time.time()
                sim = deltaCon(g1, g2, affinities=affinities)
                toc = time.time()
            else:
                sim = 0.0 # no vertex overlap!
            print(l1, l2, sim, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim}, {toc-tic}\n')


def main_deltaCon_intersection_lowmem(graphs, output_folder='../output/gml/fussballligaGML'):
    """
    Intersection variant of deltacon: For each pair of graphs, compute the induced subgraphs on the vertex set
    intersection and then proceed with the affinity computation.

    :param graphs:
    :param output_folder:
    :return:
    """

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    with open(os.path.join(output_folder, f'deltaCON_intersection_{personalized_rw_affinities.__name__}_lowmem.csv'), 'w') as out_csv:
        out_csv.write('Language1, Language2, SimilarityScore, Kernel, Time\n')

        for l1, l2 in itertools.combinations_with_replacement(graphs.keys(), 2):
            g1, g2 = restrict_to_intersection(graphs[l1], graphs[l2])
            if g1.vcount() != 0:
                tic = time.time()
                sim1, sim2 = deltaCon_rw_lowmem(g1, g2)
                toc = time.time()
            else:
                sim1 = 0.0 # no vertex overlap!
                sim2 = 0.0  # no vertex overlap!
            print(l1, l2, sim1, sim2, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim1}, {sim2}, {toc-tic}\n')


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
