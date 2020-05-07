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
            print('Processing', in_folder)
            tic = time.time()
            unaligned_graphs = load_all_graphs(in_folder)
            toc = time.time()
            print('load', toc - tic)

            print(vertex_jaccard_similarity.__name__)
            # it is important to compute this function on unaligned graphs, as otherwise the
            # vertex set similarity will always be 1.0
            main_otherSim_intersection(unaligned_graphs, similarity=vertex_jaccard_similarity, output_folder=out_folder)


if __name__ == '__main__':
    if len(sys.argv) == 1:
        dataset_root = os.path.join('..', '..', 'graphs')
        dataset_output = os.path.join('..', '..', 'output')
        gml_switch = 'all'
    elif len(sys.argv) == 4:
        dataset_root = sys.argv[1]
        dataset_output = sys.argv[2]
        gml_switch = sys.argv[3]
    else:
        sys.stderr.write('either no or three args are required.\n '
                         'usage: PROG dataset_root output_root\n')
        sys.exit(1)

    if gml_switch == 'gml':
        gml_types = ['gml']
    if gml_switch == 'fullgml':
        gml_types = ['fullgml']
    if gml_switch == 'all':
        gml_types = ['gml', 'fullgml']
    if gml_switch == 'none':
        gml_types = ['']

    run_experiment(dataset_root=dataset_root, dataset_output=dataset_output, gml_types=gml_types)