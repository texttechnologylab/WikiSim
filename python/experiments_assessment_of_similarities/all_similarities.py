import sys

sys.path.extend(['../', './'])

from similaritymeasures.deltacon import deltaCon, personalized_rw_affinities, shortest_path_affinities
from similaritymeasures.othersim import edge_jaccard_similarity, ged_similarity, vertex_edge_jaccard_similarity, vertex_jaccard_similarity, intersection_rw_kernel
from experiments_assessment_of_similarities.compare_to_random_ER import er_similarities

def deltacon_rw(G1, G2):
    return deltaCon(G1, G2, affinities=personalized_rw_affinities)

def deltacon_sp(G1, G2):
    return deltaCon(G1, G2, affinities=shortest_path_affinities)

if __name__ == '__main__':
    # edge_jaccard_similarity,
    similarities = [ged_similarity,
                    vertex_edge_jaccard_similarity, vertex_jaccard_similarity,
                    intersection_rw_kernel,
                    deltacon_rw, deltacon_sp]

    repetitions = 20
    for similarity in similarities:
        er_similarities(similarity, repetitions)