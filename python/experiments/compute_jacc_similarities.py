
from similaritymeasures.deltacon import *
from similaritymeasures.othersim import vertex_edge_jaccard_similarity, main_otherSim

if __name__ == '__main__':
    for root in ['gml', 'fullgml']:
        datasets = os.listdir(os.path.join('../..', 'graphs', root))
        for d in datasets:
            in_folder = os.path.join('../..', 'graphs', root, d)
            out_folder = os.path.join('../..', 'output', root, d)
            print('Processing', in_folder)
            tic = time.time()
            unaligned_graphs = load_multilayer_graph(in_folder)
            toc = time.time()
            print('load', toc - tic)

            # compute the union of all language graphs, for comparison
            unaligned_graphs['union'] = union(unaligned_graphs)

            print('vertex_edge_jaccard_similarity')
            # it is important to compute this function on unaligned graphs, as otherwise the
            # vertex set similarity will always be 1.0
            main_otherSim(unaligned_graphs, similarity=vertex_edge_jaccard_similarity, output_folder=out_folder)
