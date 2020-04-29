import itertools
import os
import time

import igraph
import numpy as np

from similaritymeasures.deltacon import restrict_to_intersection


def vertex_edge_set_on_labels(g: igraph.Graph):
    """
    Compute a representation of the graph that is based on the 'label' attributes of the vertices in g.
    That is, we assume that each vertex in g has a 'label' attribute and furthermore we assume that these
    labels are unique, that is, they can be used as vertex ids, instead of the integer ids used by igraph.
    :param g: a graph
    :return: vertices, edges as sets
    """
    if g.vcount() > 0:
        labels = g.vs['label'] # throws a KeyError for empty graphs
        edges = {(labels[e.source], labels[e.target]) for e in g.es}
        return set(labels), edges
    else:
        return set(), set()


def intersection_graph(g1: igraph.Graph, g2: igraph.Graph, directed: bool=True):
    """
    - Compute the intersection V' of the vertex sets of g1 and g2 (based on their labels, not ids).
    - compute g1[V'] and g2[V'], i.e. the induced subgraphs of V' of both graphs.
    :param g1, g2 graphs
    :return: g1_new, g2_new
    """

    v1, e1 = vertex_edge_set_on_labels(g1)
    v2, e2 = vertex_edge_set_on_labels(g2)

    v1.intersection_update(v2)
    e1.intersection_update(e2)
    v_new = list(v1)
    e_new = list(e1)

    # new unique vertex ids in the large graph
    label_ids = {l: i for i, l in enumerate(v1)}

    g_new = igraph.Graph(directed=directed)
    g_new.add_vertices(len(v1))
    g_new.vs['label'] = v1
    g_new.add_edges([(label_ids[e[0]], label_ids[e[1]]) for e in e1])
    return g_new


def intersection_rw_kernel(g1: igraph.Graph, g2: igraph.Graph, lamda=0.001, directed=True):
    """
    We compute the similarity of two wikipedias as the number of simultaneous random walks on them,
    which is the number of random walks on the intersection graph, as both graphs are node aligned
    :param g1: first graph
    :param g2: second graph
    :param lamda: convergence parameter of the random walk kernel. Must be set small enough. See
    Gärtner, Wrobel, Flach: On graph kernels: Hardness results and efficient alternatives
    or
    S. V. N. Vishwanathan and Nicol N. Schraudolph and Risi Kondor and Karsten M. Borgwardt: Graph Kernels
    :return:
    """
    g = intersection_graph(g1, g2, directed=directed)
    if g.vcount() > 0:
        A = np.array(g.get_adjacency().data)
        n = g.vcount()
        S = np.linalg.inv(np.eye(n) - lamda * A)
        # this computes the kernel for normalized uniform weight vertex weight vectors
        return S.sum() / (n * n)
    else:
        return 0.0


def intersection_rw_kernel_unnormalized(g1: igraph.Graph, g2: igraph.Graph, lamda=0.001, directed=True):
    """
    We compute the similarity of two wikipedias as the number of simultaneous random walks on them,
    which is the number of random walks on the intersection graph, as both graphs are node aligned
    :param g1: first graph
    :param g2: second graph
    :param lamda: convergence parameter of the random walk kernel. Must be set small enough. See
    Gärtner, Wrobel, Flach: On graph kernels: Hardness results and efficient alternatives
    or
    S. V. N. Vishwanathan and Nicol N. Schraudolph and Risi Kondor and Karsten M. Borgwardt: Graph Kernels
    :return:
    """
    g = intersection_graph(g1, g2, directed=directed)
    if g.vcount() > 0:
        A = np.array(g.get_adjacency().data)
        n = g.vcount()
        S = np.linalg.inv(np.eye(n) - lamda * A)
        # this gives an unnormalized version. large intersections with many random walks win.
        return S.sum()
    else:
        return 0.0


def intersection_rw_kernel_kiter(g1: igraph.Graph, g2: igraph.Graph, k=10, directed=True):
    """
    We compute the similarity of two wikipedias as the number of simultaneous random walks of length k them,
    which is the number of random walks on the intersection graph, as both graphs are node aligned
    :param g1: first graph
    :param g2: second graph
    :param k: length of the random walk.

    See
    Gärtner, Wrobel, Flach: On graph kernels: Hardness results and efficient alternatives
    or
    S. V. N. Vishwanathan and Nicol N. Schraudolph and Risi Kondor and Karsten M. Borgwardt: Graph Kernels
    :return:
    """
    g = intersection_graph(g1, g2, directed=directed)
    if g.vcount() > 0:
        A = np.array(g.get_adjacency().data)
        n = g.vcount()
        S = np.power(A, k)
        # this computes the kernel for normalized uniform weight vertex weight vectors
        return S.sum() / (n * n)
    else:
        return 0.0


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
            g1, g2 = restrict_to_intersection(graphs[l1], graphs[l2])
            tic = time.time()
            sim = similarity(g1, g2)
            toc = time.time()
            print(l1, l2, sim, toc-tic)
            out_csv.write(f'{l1}, {l2}, {sim}, {toc-tic}\n')
