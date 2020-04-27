import sys
import os
import subprocess

sys.path.extend(['../'])
import similarity_heatmap

# similarity_root = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'outputComplete', 'oecdTopics')
similarity_root = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'outputComplete', 'oecd')
# similarity_root = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'output')
types = ['gml', 'fullgml']

if __name__ == '__main__':
    for type in types:
        similarity_folder = os.path.join(similarity_root, type)
        for folder in filter(lambda x: os.path.isdir(os.path.join(similarity_folder,x)), os.listdir(similarity_folder)):
            for file in filter(lambda x: x.endswith('.tex'), os.listdir(os.path.join(similarity_folder, folder))):
                try:
                    subprocess.call(args=['latexmk', '-lualatex', '-silent', file], cwd=os.path.join(similarity_folder, folder), timeout=20)
                except subprocess.TimeoutExpired:
                    print('Timeout for latexmk on', os.path.join(similarity_folder, folder, file))

