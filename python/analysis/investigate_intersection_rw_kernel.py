import sys
sys.path.extend(['../../', '../', './'])

from similaritymeasures.deltacon import *
from similaritymeasures.othersim import main_otherSim, intersection_rw_kernel

similarity = intersection_rw_kernel


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
        edges = [(label_ids[g.vs[e.source]['label']], label_ids[g.vs[e.target]['label']]) for e in intersection_edges]
        g_new.add_edges(edges)
        return g_new

    return __restrict_to_intersection(g1), __restrict_to_intersection(g2)

if __name__ == '__main__':
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
            i=0
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
                if i==len(unaligned_graphs):
                    break
            toc = time.time()
            print('load', toc - tic)
