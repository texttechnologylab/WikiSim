import sys
import os

sys.path.extend([ '../', './'])
from similaritymeasures.deltacon import personalized_rw_affinities
from experiments.compute_sp_similarities_union import run_experiment

affinity_measure = personalized_rw_affinities

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

    run_experiment(dataset_root=dataset_root, dataset_output=dataset_output, gml_types=gml_types, affinity_measure=affinity_measure)