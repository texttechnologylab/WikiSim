import sys
sys.path.extend(['../../', '../', './'])

from similaritymeasures.deltacon import *
from similaritymeasures.othersim import vertex_jaccard_similarity, main_otherSim_intersection

if __name__ == '__main__':
    for root in ['gml']:
        datasets = os.listdir(os.path.join('../..', 'graphs', 'oecd', root))
        allgraphs = dict()
        for d in datasets:
            in_folder = os.path.join('../..', 'graphs', 'oecd', root, d)
            out_folder = os.path.join('../..', 'output', 'oecd', root, d)
            # print('Processing', in_folder)
            tic = time.time()
            unaligned_graphs = load_all_graphs(in_folder)
            for k in unaligned_graphs:
                allgraphs[d + '_' + k] = unaligned_graphs[k]
            toc = time.time()
            # print('load', toc - tic)


    for l1, l2 in itertools.combinations_with_replacement(allgraphs.keys(), 2):
        if allgraphs[l1].vcount() > 0:
            v = set(allgraphs[l1].vs['label'])
        else:
            v = set()
        if allgraphs[l2].vcount() > 0:
            v.intersection_update(allgraphs[l2].vs['label'])
        else:
            v = set()

        print(l1, l2, len(v))



