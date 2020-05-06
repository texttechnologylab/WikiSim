import sys
sys.path.extend([ '../', './'])


from similaritymeasures.deltacon import *


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

            tic = time.time()
            graphs = vertex_set_union(unaligned_graphs)
            toc = time.time()
            print('union', toc - tic)

            # compute the union of all language graphs, for comparison
            # graphs['union'] = union(graphs)

            tic = time.time()
            affinities = {lang: shortest_path_affinities(graphs[lang], verbose=lang) for lang in graphs.keys()}
            toc = time.time()
            print('affinities', toc - tic)

            print('Writing to', out_folder)

            print(shortest_path_affinities.__name__)
            main_deltaCon_cached(affinities, name=shortest_path_affinities.__name__, output_folder=out_folder)


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