
import sys
sys.path.extend(['../../', '../', './'])

from similaritymeasures.deltacon import *


if __name__ == '__main__':
    for root in ['gml', 'fullgml']:
        datasets = os.listdir(os.path.join('../..', 'graphs', 'oecd', root))
        for d in datasets:
            in_folder = os.path.join('../..', 'graphs', 'oecd', root, d)
            out_folder = os.path.join('../..', 'output', 'oecd', root, d)
            print('Processing', in_folder)
            tic = time.time()
            unaligned_graphs = load_all_graphs(in_folder)
            toc = time.time()
            print('load', toc - tic)

            print('Writing to', out_folder)

            print('personalized_rw_affinities')
            tic = time.time()
            main_deltaCon_intersection_lowmem(unaligned_graphs, output_folder=out_folder)
            toc = time.time()
            print('proc', toc - tic)