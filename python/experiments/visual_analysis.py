import sys
sys.path.extend(['../../', '../', './'])

from similaritymeasures.deltacon import *
import numpy as np
import matplotlib.pyplot as plt
import csv

def visualize(similarity_file, out_folder, dataset=None, show=True):
    if dataset is not None:
        folders = filter(lambda x: x.endswith(dataset), os.listdir(out_folder))
    else:
        folders = os.listdir(out_folder)

    for folder in folders:
        data = csv.DictReader(open(os.path.join(out_folder, folder, similarity_file)), skipinitialspace=True)
        l1 = list()
        l2 = list()
        similarity = list()
        kernel = list()
        for x in data:
            l1.append(x['Language1'])
            l2.append(x['Language2'])
            similarity.append(float(x['SimilarityScore']))
            try:
                kernel.append(float(x['Kernel']))
            except KeyError:
                # some tables may contain a Kernel column. if so, we read it here and plot it below
                pass

        def plot_it(sim, addendum=''):
            languages = np.unique(l1)
            np.sort(languages)
            n_lang = languages.shape[0]
            sim_matrix = np.zeros([n_lang, n_lang])

            #ugly
            lang_dict = {languages[i]: i for i in range(n_lang)}
            for i in range(len(l1)):
                sim_matrix[lang_dict[l1[i]], lang_dict[l2[i]]] = sim[i]
                sim_matrix[lang_dict[l2[i]], lang_dict[l1[i]]] = sim[i]

            # print similarity matrix
            with open(os.path.join(out_folder, folder, similarity_file[:-4] + addendum + '.matrix'), 'w') as f:
                # header
                f.write('\t'.join(languages) + '\n')
                # the lines
                for i, l in enumerate(languages):
                    f.write(l + '\t' + '\t'.join([str(x) for x in sim_matrix[i,:]]) + '\n')

            fig, ax = plt.subplots()
            im = ax.imshow(sim_matrix) #, norm=matplotlib.colors.Normalize(vmin=0.001, vmax=0.1, clip=False))

            # We want to show all ticks...
            ax.set_xticks(np.arange(len(languages)))
            ax.set_yticks(np.arange(len(languages)))
            # ... and label them with the respective list entries
            ax.set_xticklabels(languages)
            ax.set_yticklabels(languages)

            # Rotate the tick labels and set their alignment.
            plt.setp(ax.get_xticklabels(), rotation=45, ha="right",
                     rotation_mode="anchor")

            # Loop over data dimensions and create text annotations.
            for i in range(len(languages)):
                for j in range(len(languages)):
                    text = ax.text(j, i, f'{sim_matrix[i, j]:.2f}',
                                   ha="center", va="center", color="w", fontsize='xx-small')

            ax.set_title(f'{similarity_file[:-4]}\n of Wikipedia Topic \n{folder[:-3]} ({out_folder.split(os.path.sep)[-1]})')
            fig.tight_layout()
            plt.savefig(os.path.join(out_folder, folder, similarity_file[:-4] + addendum + '.png'))
            if show:
                plt.show()
            plt.close()

        plot_it(similarity)
        if len(kernel) > 0:
            plot_it(kernel, '_Kernel')


process_all = True

if __name__ == '__main__':
    if process_all:
        for similarity_file in ['deltaCON_personalized_rw_affinities.csv',
                                'deltaCON_intersection_personalized_rw_affinities.csv',
                                'deltaCON_intersection_personalized_rw_affinities_lowmem.csv',
                                'deltaCON_shortest_path_affinities.csv',
                                'otherSim_ged_similarity.csv',
                                'otherSim_ged_similarity_intersection.csv',
                                'otherSim_vertex_edge_jaccard_similarity.csv',
                                'otherSim_vertex_jaccard_similarity.csv',
                                'otherSim_edge_jaccard_similarity.csv',
                                'otherSim_intersection_rw_kernel.csv',
                                'otherSim_intersection_vertex_jaccard_similarity.csv',]:
            try:
                visualize(similarity_file=similarity_file, out_folder=os.path.join('..', '..', 'output', 'gml'), dataset=None, show=False)
            except IOError:
                pass
            except Exception as e:
                print(e)
            try:
                visualize(similarity_file=similarity_file, out_folder=os.path.join('..', '..', 'output', 'fullgml'), dataset=None, show=False)
            except IOError:
                pass
            except Exception as e:
                print(e)

    else:

        # similarity_file = 'deltaCON_personalized_rw_affinities.csv'
        similarity_file = 'deltaCON_intersection_personalized_rw_affinities.csv'
        # similarity_file = 'deltaCON_shortest_path_affinities.csv'
        # similarity_file = 'otherSim_ged_similarity.csv'
        # similarity_file = 'otherSim_vertex_edge_jaccard_similarity.csv'
        # similarity_file = 'otherSim_vertex_jaccard_similarity.csv'

        #  for root in ['gml', 'fullgml']:
        root = 'gml'
        # use 'GML' for all datasets
        dataset = 'holydayGML'
        out_folder = os.path.join('..', '..', 'output', root)

        visualize(similarity_file=similarity_file, out_folder=out_folder, dataset=dataset)





