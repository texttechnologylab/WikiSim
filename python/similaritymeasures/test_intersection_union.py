import itertools
import os
import time

import igraph

from similaritymeasures.deltacon import load_all_graphs, restrict_to_intersection
from similaritymeasures.othersim import vertex_edge_set_on_labels

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
    for e in e1:
       g_new.add_edge(label_ids[e[0]], label_ids[e[1]])

    return g_new


def intersection_graph2(g1: igraph.Graph, g2: igraph.Graph, directed: bool=True):
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

def atest_intersection():
    l1 = ['a', 'b', 'c', 'd']
    g1 = igraph.Graph(directed=True)
    g1.add_vertices(len(l1))
    g1.vs['label'] = l1
    for i, v in enumerate(g1.vs):
        g1.add_edge(i, (i+1) % len(l1))

    l2 = ['b', 'x', 'c', 'd', 'y']
    g2 = igraph.Graph(directed=True)
    g2.add_vertices(len(l2))
    g2.vs['label'] = l2
    for i, v in enumerate(g2.vs):
        g2.add_edge(i, (i+1) % len(l2))

    u1 = intersection_graph(g1, g2)
    u2 = intersection_graph2(g1, g2)

    print(u1)
    print(u2)


    def restrict_to_intersection_old(g1: igraph.Graph, g2: igraph.Graph, directed: bool = True):
        """
        - Compute the intersection V' of the vertex sets of g1 and g2 (based on their labels, not ids).
        - compute g1[V'] and g2[V'], i.e. the induced subgraphs of V' of both graphs.
        :param g1, g2 graphs
        :return: g1_new, g2_new
        """

        if g1.vcount() > 0:
            labels_set = set(g1.vs['label'])
        else:
            labels_set = set()
        if g2.vcount() > 0:
            labels_set.intersection_update(g2.vs['label'])
        else:
            labels_set = set()  # intersection will be empty

        # new unique vertex ids in the small intersection graph
        labels = list(labels_set)
        label_ids = {l: i for i, l in enumerate(labels)}

        def __restrict_to_intersection(g):
            """Very slow implementation of the variant below. igraph does some nonconstant time operattion for
            each addition of an edge. Hence adding multiple edges at once is faster than individual addition."""
            g_new = igraph.Graph(directed=directed)
            g_new.add_vertices(len(labels))
            g_new.vs['label'] = labels
            for e in g.es:
                try:
                    g_new.add_edge(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']])
                except KeyError:
                    pass  # here, we have encountered an edge with at least one endpoint not in the vertex set intersection
            return g_new

        def __restrict_to_intersection_2(g):
            g_new = igraph.Graph(directed=directed)
            g_new.add_vertices(len(labels))
            g_new.vs['label'] = labels

            intersection_edges = filter(lambda e:
                                        g.vs[e.source]['label'] in labels_set and g.vs[e.target]['label'] in labels_set,
                                        g.es)
            edges = [(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']]) for e in
                     intersection_edges]
            g_new.add_edges(edges)
            return g_new

        return __restrict_to_intersection(g1), __restrict_to_intersection(g2)

    def atest_restrict_to_intersection():
        '''Test novel, faster implementation of the restrict to intersection method.'''
        for root in ['gml', 'fullgml']:
            datasets = os.listdir(os.path.join('../..', 'graphs', root))
            for d in datasets:
                in_folder = os.path.join('../..', 'graphs', root, d)
                out_folder = os.path.join('../..', 'investigation', root, d)
                print('Processing', in_folder)
                tic = time.time()
                unaligned_graphs = load_all_graphs(in_folder)
                toc = time.time()
                print('load', toc - tic)

                # selected_graphs = dict()
                # for l in ['ceb', 'war', 'vi', 'fr', 'es']:
                #     selected_graphs[l] = unaligned_graphs[l]

                sample = itertools.combinations(unaligned_graphs.keys(), 2)

                tic = time.time()
                i = 0
                for l1, l2 in sample:
                    g1, g2 = restrict_to_intersection_old(unaligned_graphs[l1], unaligned_graphs[l2])
                    h1, h2 = restrict_to_intersection(unaligned_graphs[l1], unaligned_graphs[l2])
                    if h1.isomorphic(g1):
                        print('jup')
                    else:
                        print('nope')

                    if h2.isomorphic(g2):
                        print('jup')
                    else:
                        print('nope')
                    i += 1
                    if i == len(unaligned_graphs):
                        break
                toc = time.time()
                print('load', toc - tic)


if __name__ == '__main__':
    atest_intersection()