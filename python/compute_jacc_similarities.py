
from rws import *


if __name__ == '__main__':
    for root in ['gml', 'fullgml']:
        datasets = os.listdir(os.path.join('..', 'graphs', root))
        for d in datasets:
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

            # compute the union of all language graphs, for comparison
            graphs['union'] = union(graphs)

            print('vertex_edge_jaccard_similarity')
            main_otherSim(graphs, similarity=vertex_edge_jaccard_similarity, output_folder=out_folder)
