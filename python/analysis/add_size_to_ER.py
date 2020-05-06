import os
import igraph

er_result_path = os.path.join('/', 'home', 'pascal', 'Documents', 'Uni_synced', 'frankfurt', 'experimental_results', 'WikiSimNew', 'graphsReduced', 'random')
er_new_result_path = os.path.join('..', '..', 'output', 'pascal', 'random')

"""Add columns giving size (n) and order (m) of each graph."""
if __name__ == '__main__':
    for f in os.listdir(er_result_path):
        fin = open(os.path.join(er_result_path, f), 'r')
        fout = open(os.path.join(er_new_result_path, f), 'w')

        for line in fin:
            if line.startswith('Root path: '):
                file_root = line[11:-1]
                fout.write(line)
            elif line.startswith('type; dataset; language;'):
                tokens = line.split('; ')
                tokens.insert(3, 'order')
                tokens.insert(3, 'size')
                fout.write('; '.join(tokens))
            elif line.startswith('gml') or line.startswith('fullgml'):
                tokens = line.split('; ')
                g = igraph.read(os.path.join(file_root, tokens[0], tokens[1], tokens[2] + '.gml'))
                tokens.insert(3, str(g.ecount()))
                tokens.insert(3, str(g.vcount()))
                fout.write('; '.join(tokens))
            else:
                fout.write(line)

        fin.close()
        fout.close()