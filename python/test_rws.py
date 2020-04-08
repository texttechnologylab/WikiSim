from rws import *

from joblib import delayed, Parallel, cpu_count

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


def test_intersection_graph():
    l1 = ['a', 'b', 'c', 'd']
    g1 = igraph.Graph(directed=True)
    print(g1.is_directed())
    g1.add_vertices(len(l1))
    for i, v in enumerate(g1.vs):
        v['label'] = l1[i]
        g1.add_edge(i, (i + 1) % len(l1))
        print(i, v, v['label'], l1[i])
        print(g1.neighbors(v))

    l2 = ['b', 'x', 'c', 'd', 'y']
    g2 = igraph.Graph(directed=True)
    g2.add_vertices(len(l2))
    for i, v in enumerate(g2.vs):
        v['label'] = l2[i]
        g2.add_edge(i, (i + 1) % len(l2))
        print(i, v, v['label'], l2[i])
        print(g2.neighbors(v))

    print(intersection_graph(g1, g2))



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

def test_rootED1():
    S1 = np.array([[1, 2], [3, 4]])
    S2 = np.array([[1, 1], [2, 2]])
    return rootED(S1, S2)


def test_rootED2():
    S1 = np.array([[1, 2], [3, 4]])
    S2 = np.array([[1, 1], [2, 2], [3, 3]])
    try:
        rootED(S1, S2)
    except ValueError:
        # everything is fine
        return True
    finally:
        raise Exception('Should raise a ValueError, but doesn\'t.')


def test_deltaCon_edgeAddition(data_folder='../graphs/gml/fussballligaGML', output_folder='../output/gml/fussballligaGML', affinities=personalized_rw_affinities, lang='de'):
    """
    Test how the deltaCon based mehtods behave when adding random edges.

    It seems that adding random edges dramatically decreases the similarity between a graph and its
    perturbed copy.

    :param data_folder:
    :param output_folder:
    :param affinities:
    :param lang:
    :return:
    """

    import numpy.random as rnd

    unaligned_graphs = load_multilayer_graph(data_folder)
    graphs = vertex_set_union(unaligned_graphs)

    g = graphs[lang]
    h = g.copy()

    print('n =', g.vcount())
    print('e =', g.ecount())
    print(0,deltaCon(g, h, affinities=affinities))
    for i in range(1, g.vcount()):
        h.add_edge(rnd.randint(0, g.vcount()), rnd.randint(0, g.vcount()))
        sim = deltaCon(g, h, affinities=affinities)
        print(i, sim)

def test_deltaCon_edgeRemoval(data_folder='../graphs/gml/fussballligaGML', output_folder='../output/gml/fussballligaGML', affinities=personalized_rw_affinities, lang='de'):
        """
        Test how the deltaCon based methods behave when removing random edges from a graph.

        It seems that removing edges behaves as expected, with a slow drop in similarity..

        :param data_folder:
        :param output_folder:
        :param affinities:
        :param lang:
        :return:
        """

        import numpy.random as rnd

        unaligned_graphs = load_multilayer_graph(data_folder)
        graphs = vertex_set_union(unaligned_graphs)

        g = graphs[lang]
        h = g.copy()

        print('n =', g.vcount())
        print('e =', g.ecount())

        print(0, deltaCon(g, h, affinities=affinities))
        for i in range(1, g.ecount()):
            h.delete_edges(rnd.randint(0, g.ecount()))
            sim = deltaCon(g, h, affinities=affinities)
            print(i, sim)



def personalized_rw_affinities_parallel(g: igraph.Graph, weights: np.array=None):
    """
    Compute node affinities using personalized_pagerank for each node.
    Attempt to parallelize, but did not work (i.e., is much slower). maybe igraph locks internally?
    :param g: a graph
    :param weights: edge weights, if necessary (higher means more important)
    :return: affinity matrix
    """
    # affinities = np.zeros([g.vcount(), g.vcount()])
    # for v in range(g.vcount()):

    def apply(v):
        return g.personalized_pagerank(vertices=None, reset_vertices=v, weights=weights)

    sub_arrays = Parallel(n_jobs=cpu_count())(
        delayed(apply)(v)  # Apply my_function
        for v in range(g.vcount()))  # For each 3rd dimension

    # ... Concatenate the list of returned arrays
    parallel_results = np.stack(sub_arrays, axis=1)

    return parallel_results
    # return affinities


def test_personalized_rw_affinities_parallel():
    for root in ['gml']:
        datasets = os.listdir(os.path.join('..', 'graphs', root))
        for d in datasets[:1]:
            in_folder = os.path.join('..', 'graphs', root, d)
            out_folder = os.path.join('..', 'output', root, d)
            print('Processing', in_folder)
            tic = time.time()
            unaligned_graphs = load_multilayer_graph(in_folder)
            toc = time.time()
            print('load', toc - tic)

            tic = time.time()
            graphs = vertex_set_union(unaligned_graphs)
            toc = time.time()
            print('union', toc - tic)
            print('Writing to', out_folder)

            print('personalized_rw_affinities')
            tic = time.time()
            main_deltaCon(graphs, affinities=personalized_rw_affinities, output_folder=out_folder)
            toc = time.time()
            print('sequential', toc - tic)
            tic = time.time()
            main_deltaCon(graphs, affinities=personalized_rw_affinities_parallel, output_folder=out_folder)
            toc = time.time()
            print('parallel', toc - tic)

            # np.equal(serial_result, parallel_results).all()