import os

from analysis.visual_analysis import visualize

similarity_root = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'graphsReduced')

similarity_files = ['deltaCON_intersection_personalized_rw_affinities_lowmem.csv',
                    'deltaCON_intersection_shortest_path_affinities.csv',
                    'deltaCON_personalized_rw_affinities.csv',
                    'deltaCON_shortest_path_affinities.csv',
                    'otherSim_ged_similarity.csv',
                    'otherSim_intersection_edge_jaccard_similarity.csv',
                    'otherSim_intersection_ged_similarity.csv',
                    'otherSim_intersection_rw_kernel.csv',
                    # 'otherSim_intersection_rw_kernel_10iter.csv',
                    # 'otherSim_intersection_rw_kernel_unnormalized.csv',
                    'otherSim_vertex_edge_jaccard_similarity.csv',
                    'otherSim_vertex_jaccard_similarity.csv',]

replace_nan_with_value = -1.0

if __name__ == '__main__':
        for similarity_file in similarity_files:
            print('Processing', similarity_file)
            try:
                visualize(similarity_file=similarity_file, out_root=similarity_root, type='gml', dataset=None, show=False, matplotlib=False, handle_nan=replace_nan_with_value)
            except IOError:
                pass
            except Exception as e:
                print(e)
            try:
                visualize(similarity_file=similarity_file, out_root=similarity_root, type='fullgml', dataset=None, show=False, matplotlib=False, handle_nan=replace_nan_with_value)
            except IOError:
                pass
            except Exception as e:
                print(e)


