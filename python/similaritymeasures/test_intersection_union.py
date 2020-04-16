import igraph
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

if __name__ == '__main__':
    atest_intersection()