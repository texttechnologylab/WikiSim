
import sys
sys.path.extend([ '../', './'])

from similaritymeasures.deltacon import *
from similaritymeasures.othersim import edge_jaccard_similarity, main_otherSim_intersection

def run_experiment(dataset_root, dataset_output, gml_types):
    for type in gml_types:
        datasets = os.listdir(os.path.join(dataset_root, type))
        allgraphs = dict()
        for d in datasets:
            in_folder = os.path.join(dataset_root, type, d)
            out_folder = os.path.join(dataset_output, type, d)
            print('Processing', in_folder)
            tic = time.time()
            unaligned_graphs = load_all_graphs(in_folder)
            toc = time.time()
            print('load', toc - tic)

            print('ged_similarity')
            # it is important to compute this function on unaligned graphs, as otherwise the
            # vertex set similarity will always be 1.0
            main_otherSim_intersection(unaligned_graphs, similarity=edge_jaccard_similarity, output_folder=out_folder)
