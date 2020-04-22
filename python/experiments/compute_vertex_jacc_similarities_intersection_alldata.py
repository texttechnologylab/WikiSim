import sys
sys.path.extend([ '../', './'])


from similaritymeasures.deltacon import *
from similaritymeasures.othersim import vertex_jaccard_similarity, main_otherSim_intersection

def run_experiment(dataset_root, dataset_output, gml_types):
    for type in gml_types:
        datasets = os.listdir(os.path.join(dataset_root, type))
        allgraphs = dict()
        for d in datasets:
            in_folder = os.path.join(dataset_root, type, d)
            out_folder = os.path.join(dataset_output, type, d)
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


if __name__ == '__main__':
    if len(sys.argv) == 1:
        dataset_root = os.path.join('..', '..', 'graphs')
        dataset_output = os.path.join('..', '..', 'output')
    elif len(sys.argv) == 3:
        dataset_root = sys.argv[1]
        dataset_output = sys.argv[2]
    else:
        sys.stderr.write('either no or two args are required.\n '
                         'usage: PROG dataset_root output_root\n')
        sys.exit(1)

    gml_types = ['gml', 'fullgml']

    run_experiment(dataset_root=dataset_root, dataset_output=dataset_output, gml_types=gml_types)
