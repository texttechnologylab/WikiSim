import sys
import os
import igraph
import numpy as np
from joblib import Parallel, delayed

sys.path.extend(['../', './'])
from similaritymeasures.deltacon import deltaCon, personalized_rw_affinities, shortest_path_affinities, load_all_graphs
from similaritymeasures.othersim import edge_jaccard_similarity, ged_similarity, vertex_edge_jaccard_similarity, vertex_jaccard_similarity, intersection_rw_kernel, intersection_rw_kernel_kiter


def has_loops(g: igraph.Graph):
    '''The interface of igraph is strange here. g.is_loop() returns a list of boolean values, one for each edge.
    We want to know if the graph has loops, which is true if at least one of the edge values are true.
    '''
    loops = g.is_loop()
    for is_loop in loops:
        if is_loop:
            return True
    return False


def make_ER_simulation(g: igraph.Graph, directed=True):
    '''Create an Erdos Renyi random graph on the same vertex set. (respecting labels)'''
    erg = igraph.Graph.Erdos_Renyi(g.vcount(), m=g.ecount(), directed=directed, loops=has_loops(g))
    erg.vs['label'] = g.vs['label']
    return erg


def er_similarities(similarity, repetitions, root_in, output_folder, types):
    """
    Create repetitions many er graphs on the same vertex set (with identical labels and ids) for each graph
    in the datasets pointed at by root_in.

    Create a csv file with the mean similarity score among these repetitions and the standard deviation.
    :param similarity: a similarity function that accepts two graphs
    :param repetitions: number of er graphs created for each instance
    :param root_in: folder in which the data resides
           (expected are, that there are 'gml' and 'fullgml' subfolders present)
    :return: nothing, but store a csv file in the working directory
    """
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    with open(os.path.join(output_folder, 'ER_similarities_' + similarity.__name__ + '.csv'), 'w') as f:
        f.write('Root path: ' + root_in + '\n' + 'Sample Size: ' + str(repetitions) + '\n\n')
        f.write('type; dataset; language; mean; std; ' + '; '.join([f'rep{i}' for i in range(repetitions)]) + '\n')

        for type in types:
            datasets = os.listdir(os.path.join(root_in, type))
            for d in datasets:
                in_folder = os.path.join(root_in, type, d)

                print('Processing', in_folder)
                unaligned_graphs = load_all_graphs(in_folder)

                for lang in unaligned_graphs:
                    g = unaligned_graphs[lang]
                    if g.vcount() > 0:
                        similarity_score = np.zeros(repetitions)
                        for i in range(repetitions):
                            erg = make_ER_simulation(g)
                            similarity_score[i] = similarity(erg, g)
                        f.write(f'{type}; {d}; {lang}; {np.mean(similarity_score)}; {np.std(similarity_score)}; ')
                        f.write('; '.join([str(s) for s in similarity_score]))
                        f.write('\n')
                    else:
                        f.write(f'{type}; {d}; {lang}; nan; graph is empty\n')
                    f.flush()


def deltacon_rw(G1, G2, onMemoryError=np.nan):
    '''Wrapper function for deltacon with personalized random walk similarities'''
    try:
        sim = deltaCon(G1, G2, affinities=personalized_rw_affinities)
    except MemoryError as e:
        print(e)
        sim = onMemoryError
    return sim


def deltacon_sp(G1, G2, onMemoryError=np.nan):
    '''Wrapper function for deltacon with shortest path similarities'''
    try:
        sim = deltaCon(G1, G2, affinities=shortest_path_affinities)
    except MemoryError as e:
        print(e)
        sim = onMemoryError
    return sim

def intersection_rw_kernel_10iter(G1, G2, onMemoryError=np.nan):
    try:
        sim = intersection_rw_kernel_kiter(G1, G2, k=10)
    except MemoryError as e:
        print(e)
        sim = onMemoryError
    return sim

if __name__ == '__main__':
    if len(sys.argv) == 1:
        dataset_root = os.path.join('..', '..', 'graphsReduced')
        dataset_output = os.path.join('.')
    elif len(sys.argv) == 3:
        dataset_root = sys.argv[1]
        dataset_output = sys.argv[2]
    else:
        sys.stderr.write('either no or two args are required.\n '
                         'usage: PROG dataset_root output_root\n')
        sys.exit(1)

    types = ['gml', 'fullgml']

    # consider these similarity functions
    similarities = [edge_jaccard_similarity, ged_similarity,
                    vertex_edge_jaccard_similarity, vertex_jaccard_similarity,
                    intersection_rw_kernel,
                    deltacon_rw, deltacon_sp]

    # create this many er graphs per instance
    repetitions = 10

    element_run = Parallel(n_jobs=-1)(delayed(er_similarities)(similarity, repetitions, dataset_root, dataset_output, types) for similarity in similarities)