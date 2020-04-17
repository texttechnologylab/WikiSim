import sys

sys.path.extend(['../', './'])

from similaritymeasures.deltacon import *
from similaritymeasures.othersim import edge_jaccard_similarity

# look for datasets here
root_in = os.path.join('..', '..', 'graphs', 'oecd')
root_out = os.path.join('..', '..', 'graphs', 'oecd')

def has_loops(g: igraph.Graph):
    loops = g.is_loop()
    for is_loop in loops:
        if is_loop:
            return True
    return False


def make_ER_simulation(g: igraph.Graph, directed=True):
    '''Create an Erdos Renyi random graph on the same vertex set. (respecting labels)'''
    erg = igraph.Graph.Erdos_Renyi(g.vcount(), m=g.ecount(), directed=directed, loops=has_loops(g))
    erg.vs['label'] = g.vs['label']
    return erg


def er_similarities(similarity, repetitions):
    with open('ER_similarities_' + similarity.__name__ + '.csv', 'w') as f:
        f.write('Root path: ' + root_in + '\n' + 'Sample Size: ' + str(repetitions) + '\n\n')
        f.write('type; dataset; language; mean; std')

        for type in ['gml', 'fullgml']:
            datasets = os.listdir(os.path.join(root_in, type))
            for d in datasets:
                in_folder = os.path.join(root_in, type, d)
                out_folder = os.path.join(root_out, type, d)

                print('Processing', in_folder)
                unaligned_graphs = load_all_graphs(in_folder)

                for lang in unaligned_graphs:
                    g = unaligned_graphs[lang]
                    similarity_score = np.zeros(repetitions)
                    for i in range(repetitions):
                        erg = make_ER_simulation(g)
                        similarity_score[i] = similarity(erg, g)
                    f.write(f'{type}; {d}; {lang}; {np.mean(similarity_score)}; {np.std(similarity_score)}\n')


if __name__ == '__main__':
    similarity = edge_jaccard_similarity
    repetitions = 10
    er_similarities(similarity, repetitions)