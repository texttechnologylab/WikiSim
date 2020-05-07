import sys
import os
import igraph
import numpy as np
from joblib import Parallel, delayed

sys.path.extend(['../', './'])
from similaritymeasures.deltacon import deltaCon, personalized_rw_affinities, shortest_path_affinities, load_all_graphs
from similaritymeasures.othersim import edge_jaccard_similarity, ged_similarity, vertex_edge_jaccard_similarity, vertex_jaccard_similarity, intersection_rw_kernel, intersection_rw_kernel_kiter
from experiments.compare_to_random_ER import make_ER_simulation

def er_instances(repetitions, root_in, root_out, types):
    """
    Create repetitions many er graphs on the same vertex set (with identical labels and ids) for each graph
    in the datasets pointed at by root_in.

    :param repetitions: number of er graphs created for each instance
    :param root_in: folder in which the data resides
           (expected are, that there are 'gml' and 'fullgml' subfolders present)
    :return: nothing, but store a csv file in the working directory
    """
    if not os.path.exists(root_out):
        os.makedirs(root_out)

    for type in types:
        datasets = os.listdir(os.path.join(root_in, type))
        for d in datasets:
            in_folder = os.path.join(root_in, type, d)

            print('Processing', in_folder)
            unaligned_graphs = load_all_graphs(in_folder)

            for lang in unaligned_graphs:
                g = unaligned_graphs[lang]
                for i in range(repetitions):
                    idir = os.path.join(root_out, type, str(i), d)
                    if not os.path.exists(idir):
                        os.makedirs(idir)
                    if g.vcount() > 0:
                        erg = make_ER_simulation(g)
                    else:
                        erg = igraph.Graph()
                    filename = os.path.join(idir, lang + '.gml')
                    erg.write(filename)


if __name__ == '__main__':
    if len(sys.argv) == 1:
        dataset_root = os.path.join('..', '..', 'graphsReduced')
        dataset_output = os.path.join('.')
    elif len(sys.argv) == 4:
        dataset_root = sys.argv[1]
        dataset_output = sys.argv[2]
        repetitions = int(sys.argv[3])
    else:
        sys.stderr.write('either no or three args are required.\n '
                         'usage: PROG dataset_root output_root repetitions\n')
        sys.exit(1)

    types = ['gml'] #, 'fullgml']



    er_instances(repetitions, dataset_root, dataset_output, types)
