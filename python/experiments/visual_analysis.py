from similaritymeasures.deltacon import *
import numpy as np
import matplotlib.pyplot as plt
import csv

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

if __name__ == '__main__':
    out_folder = os.path.join('../..', 'output', root)

    folders = filter(lambda x: x.endswith(dataset), os.listdir(out_folder))
    graphs = dict()
    for folder in folders:
        data = csv.DictReader(open(os.path.join(out_folder, folder, similarity_file)), skipinitialspace=True)
        l1 = list()
        l2 = list()
        sim = list()
        for x in data:
            l1.append(x['Language1'])
            l2.append(x['Language2'])
            sim.append(float(x['SimilarityScore']))


        languages = np.unique(l1)
        np.sort(languages)
        n_lang = languages.shape[0]
        sim_matrix = np.zeros([n_lang, n_lang])

        #ugly
        lang_dict = {languages[i]: i for i in range(n_lang)}
        for i in range(len(l1)):
            sim_matrix[lang_dict[l1[i]], lang_dict[l2[i]]] = sim[i]
            sim_matrix[lang_dict[l2[i]], lang_dict[l1[i]]] = sim[i]

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
                               ha="center", va="center", color="w")

        ax.set_title(f'{similarity_file[:-4]}\n of Wikipedia Topic \n{dataset[:-3]} ({root})')
        fig.tight_layout()
        plt.savefig(os.path.join(out_folder, folder, similarity_file[:-4] + '.png'))
        plt.show()
