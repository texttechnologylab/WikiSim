import sys
sys.path.extend(['../../', '../', './'])

from similaritymeasures.deltacon import *
from similaritymeasures.othersim import main_otherSim, intersection_rw_kernel

similarity = intersection_rw_kernel

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

            selected_graphs = dict()
            for l in ['ceb', 'war', 'vi', 'fr', 'es']:
                selected_graphs[l] = unaligned_graphs[l]


            print(similarity.__name__)
            # it is important to compute this function on unaligned graphs, as otherwise the
            # vertex set similarity will always be 1.0
            main_otherSim(selected_graphs, similarity=similarity, output_folder=out_folder)
