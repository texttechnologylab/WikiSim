import sys
sys.path.extend(['../', './'])

from similaritymeasures.deltacon import *
import numpy as np
import csv
import random

def compute_sampled_statistics(similarity_file, similarity_folder, sample_size=0.1, dataset=None, sample_relative=True):
    if dataset is not None:
        folders = filter(lambda x: x.endswith(dataset), os.listdir(similarity_folder))
    else:
        folders = os.listdir(similarity_folder)

    # collect all similarities
    l1 = list()
    l2 = list()
    similarity = list()
    kernel = list()
    for folder in folders:
        data = csv.DictReader(open(os.path.join(similarity_folder, folder, similarity_file)), skipinitialspace=True)

        for x in data:
            l1.append(x['Language1'])
            l2.append(x['Language2'])
            similarity.append(float(x['SimilarityScore']))
            try:
                kernel.append(float(x['Kernel']))
            except KeyError:
                # some tables may contain a Kernel column. if so, we read it here and plot it below
                pass

    # select a random subset of similarities for upper bound estimation
    tuples = zip(l1, l2, similarity)
    different_language_same_topic_pairs = list(filter(lambda x: x[0] != x[1], tuples))
    if sample_relative:
        n_pairs = round(sample_size * len(different_language_same_topic_pairs))
    else:
        n_pairs = sample_size
    sample_of_pairs = random.sample(different_language_same_topic_pairs, n_pairs)

    sampled_similarities = np.array([x[2] for x in sample_of_pairs])
    print(f'{similarity_file}, {n_pairs}, {np.max(sampled_similarities)}, {np.mean(sampled_similarities)}, {np.median(sampled_similarities)}')

    # if len(kernel) > 0:
    #     plot_it(kernel, '_Kernel')


if __name__ == '__main__':
    similarity_files = ['deltaCON_personalized_rw_affinities.csv',
                        'deltaCON_intersection_personalized_rw_affinities.csv',
                        'deltaCON_intersection_personalized_rw_affinities_lowmem.csv',
                        'deltaCON_shortest_path_affinities.csv',
                        'deltaCON_intersection_shortest_path_affinities.csv',
                        'otherSim_ged_similarity.csv',
                        'otherSim_ged_similarity_intersection.csv',
                        'otherSim_vertex_edge_jaccard_similarity.csv',
                        'otherSim_vertex_jaccard_similarity.csv',
                        'otherSim_edge_jaccard_similarity.csv',
                        'otherSim_intersection_rw_kernel.csv',
                        'otherSim_intersection_rw_kernel_unnormalized.csv'
                        'otherSim_intersection_vertex_jaccard_similarity.csv',]
    sample_size = 0.1

    header = 'Similarity_file, n_pairs, max, mean, median'
    print(header)
    for similarity_file in similarity_files:
        try:
            compute_sampled_statistics(similarity_file=similarity_file, similarity_folder=os.path.join('..', '..', 'output', 'gml'), sample_size=sample_size, sample_relative=True)
        except IOError:
            pass
        # except Exception as e:
        #     print(e)
        try:
            compute_sampled_statistics(similarity_file=similarity_file, similarity_folder=os.path.join('..', '..', 'output', 'fullgml'), sample_size=sample_size, sample_relative=True)
        except IOError:
            pass
        # except Exception as e:
        #     print(e)
