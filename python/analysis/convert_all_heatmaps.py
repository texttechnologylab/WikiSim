import sys
import os
sys.path.extend(['../'])
import similarity_heatmap_T

# similarity_root = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'outputComplete', 'oecdTopics')
similarity_root = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'outputComplete', 'oecd')
# similarity_root = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'output')
types = ['gml', 'fullgml']

if __name__ == '__main__':
    for type in types:
        similarity_folder = os.path.join(similarity_root, type)
        for folder in filter(lambda x: os.path.isdir(os.path.join(similarity_folder,x)), os.listdir(similarity_folder)):
            for file in filter(lambda x: x.endswith('.matrix'), os.listdir(os.path.join(similarity_folder, folder))):
                inp = os.path.join(similarity_folder, folder, file)
                out = os.path.join(similarity_folder, folder, file[:-7] + '.tex')
                # print(inp, out)
                similarity_heatmap_T.transform(inp, out, 'similarity')
