import sys
sys.path.extend(['../', './'])

from similaritymeasures.deltacon import *
import numpy as np
import csv
import random


def compute_sampled_statistics(similarity_file, similarity_root, type, sample_size=0.1, dataset=None, sample_relative=True, output_file=None):
    similarity_folder = os.path.join(similarity_root, type)
    if dataset is not None:
        folders = filter(lambda x: x.endswith(dataset), os.listdir(similarity_folder))
    else:
        folders = os.listdir(similarity_folder)

    # collect all similarities
    l1 = list()
    l2 = list()
    similarity = list()
    kernel = list()
    missing_files = 0
    for folder in folders:
        try:
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
        except FileNotFoundError:
            missing_files += 1

    # select a random subset of similarities for upper bound estimation
    tuples = zip(l1, l2, similarity)
    different_language_same_topic_pairs = list(filter(lambda x: x[0] != x[1], tuples))
    if sample_relative:
        n_pairs = round(sample_size * len(different_language_same_topic_pairs))
    else:
        n_pairs = sample_size
    sample_of_pairs = random.sample(different_language_same_topic_pairs, n_pairs)

    sampled_similarities = np.array([x[2] for x in sample_of_pairs])
    if len(sampled_similarities) > 0:
        result = f'{similarity_file}, {type}, {missing_files}, {n_pairs}, {np.max(sampled_similarities)}, {np.mean(sampled_similarities)}, {np.std(sampled_similarities)}, {np.median(sampled_similarities)}'
    else:
        result = f'{similarity_file}, {type}, {missing_files}, {n_pairs}, nan, nan, nan, nan'
    print(result)
    if output_file is not None:
        output_file.write(result + '\n')

    # if len(kernel) > 0:
    #     plot_it(kernel, '_Kernel')


# similarity_folder = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'outputComplete', 'oecdTopics')
# similarity_folder = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'outputComplete', 'oecd')
similarity_folder = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'output')

similarity_files = ['deltaCON_intersection_personalized_rw_affinities_lowmem.csv',
                    'deltaCON_intersection_shortest_path_affinities.csv',
                    'deltaCON_personalized_rw_affinities.csv',
                    'deltaCON_shortest_path_affinities.csv',
                    'otherSim_ged_similarity.csv',
                    'otherSim_intersection_edge_jaccard_similarity.csv',
                    'otherSim_intersection_ged_similarity.csv',
                    'otherSim_intersection_rw_kernel.csv',
                    'otherSim_intersection_rw_kernel_unnormalized.csv',
                    'otherSim_intersection_vertex_jaccard_similarity.csv',
                    'otherSim_vertex_edge_jaccard_similarity.csv',
                    'otherSim_vertex_jaccard_similarity.csv', ]

sample_size = 0.1
output_file = os.path.join(similarity_folder, 'SampledSimilaritiesSec231.csv')


if __name__ == '__main__':
    with open(output_file, 'w') as f:
        header = 'Similarity_file, type, missing_files, n_pairs, max, mean, std, median'
        print(header)
        f.write(header + '\n')
        for similarity_file in similarity_files:
            try:
                compute_sampled_statistics(similarity_file=similarity_file, similarity_root=similarity_folder, type='gml', sample_size=sample_size, sample_relative=True, output_file=f)
            except IOError:
                pass
            try:
                compute_sampled_statistics(similarity_file=similarity_file, similarity_root=similarity_folder, type='fullgml', sample_size=sample_size, sample_relative=True, output_file=f)
            except IOError:
                pass
