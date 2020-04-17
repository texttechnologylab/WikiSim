import sys
import os
import igraph
import numpy as np

sys.path.extend(['../', './'])

from similaritymeasures.deltacon import deltaCon, personalized_rw_affinities, shortest_path_affinities, load_all_graphs
from similaritymeasures.othersim import edge_jaccard_similarity, ged_similarity, vertex_edge_jaccard_similarity, vertex_jaccard_similarity, intersection_rw_kernel
from experiments_assessment_of_similarities.compare_to_random_ER import er_similarities


def has_loops(g: igraph.Graph):
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


def er_similarities(similarity, repetitions, root_in):
    with open('ER_similarities_' + similarity.__name__ + '.csv', 'w') as f:
        f.write('Root path: ' + root_in + '\n' + 'Sample Size: ' + str(repetitions) + '\n\n')
        f.write('type; dataset; language; mean; std')

        for type in ['gml', 'fullgml']:
            datasets = os.listdir(os.path.join(root_in, type))
            for d in datasets:
                in_folder = os.path.join(root_in, type, d)

                print('Processing', in_folder)
                unaligned_graphs = load_all_graphs(in_folder)

                for lang in unaligned_graphs:
                    g = unaligned_graphs[lang]
                    similarity_score = np.zeros(repetitions)
                    for i in range(repetitions):
                        erg = make_ER_simulation(g)
                        similarity_score[i] = similarity(erg, g)
                    f.write(f'{type}; {d}; {lang}; {np.mean(similarity_score)}; {np.std(similarity_score)}\n')


def deltacon_rw(G1, G2):
    '''Wrapper function for deltacon with personalized random walk similarities'''
    return deltaCon(G1, G2, affinities=personalized_rw_affinities)


def deltacon_sp(G1, G2):
    '''Wrapper function for deltacon with shortest path similarities'''
    return deltaCon(G1, G2, affinities=shortest_path_affinities)


if __name__ == '__main__':
    # look for datasets here
    root_in = os.path.join('..', '..', 'graphs', 'oecd')

    # consider these similarity functions
    similarities = [edge_jaccard_similarity, ged_similarity,
                    vertex_edge_jaccard_similarity, vertex_jaccard_similarity,
                    intersection_rw_kernel,
                    deltacon_rw, deltacon_sp]

    # create this many er graphs per instance
    repetitions = 20

    for similarity in similarities:
        er_similarities(similarity, repetitions, root_in)
