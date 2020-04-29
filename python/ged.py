import netcomp as nc
import networkx as nx
import glob, os
import numpy as np
from similarity_heatmap import *
from pathlib import Path
import pandas as pd
from pandas.tests.io.parser import skiprows

def read_bf(path):
    count = 0
    inVertices = False
    inEdges = False
    
    G=nx.Graph()
    
    with open(path) as fp: 
        Lines = fp.readlines() 
        for line in Lines: 
            if(line.strip() == "Vertices:"):
                inVertices = True
                inEdges = False
                continue
            if(line.strip() == "Edges:"):
                inVertices = False
                inEdges = True
                continue
            
            if(inVertices):
                G.add_node(line.strip().replace("¤",""))
            if(inEdges):
                edge = line.strip().split("¤")
                G.add_edge(edge[0], edge[1])
    return G

def intersection(lst1, lst2): 
    lst3 = [value for value in lst1 if value in lst2] 
    return lst3 

def computeGED(G1,G2):
    nodesG1 = G1.nodes()
    nodesG2 = G2.nodes()
    
    edgesG1 = G1.edges()
    edgesG2 = G2.edges()
    
    union = nodesG1 + list(set(nodesG2) - set(nodesG1))
    inters = intersection(nodesG1, nodesG2)  
    
    intersEdges = intersection(edgesG1, edgesG2)
    if((len(nodesG1) + len(nodesG2)) == 0):
        V = 0
    else:
        V = (len(nodesG1) + len(nodesG2) - 2 * len(inters))/(len(nodesG1) + len(nodesG2))
    
    if((len(edgesG1) +len(edgesG2))==0):
        E = 0
    else:
        E = (len(edgesG1) + len(edgesG2) - 2 * len(intersEdges))/(len(edgesG1) +len(edgesG2))
      
    return 0.5 * ( V + E )


def analyseCategory(categoryPath):
    if(not categoryPath.endswith("/")):
        categoryPath = categoryPath + "/"
    
    def printMatrix(matrix,names,normalize=True):
        if(normalize):
            matrix = (matrix - np.min(matrix))/np.ptp(matrix)
        
        output = ""
        for i in names:
            if(len(output) > 0):
                output = output + "\t"
            output = output + i
        
        output = output + "\n"
        for i in range(0,len(names)):
            output = output + names[i] + "\t"
            for j in range(0,len(names)):
                output = output + str(round(matrix[i][j],3)) + "\t"
            output = output + "\n"
        
        print(output)
        return output
        
    
    files = []
    for file in glob.glob(categoryPath + "*.gml.bf"):
        files.append(file)
         
    files = sorted(files)
    
    #del(files[2]) #en
    
    fileNames = []
    for i in files:
        fileNames.append(i.replace(categoryPath,"").replace(".gml.bf",""))
     
    print(files)
     
    output = [[0 for i in range(len(files))] for j in range(len(files))]
    for i in range(0,len(files)):
        G1=read_bf(files[i])
        for j in range(i,len(files)):
            print(files[i],files[j])
            G2=read_bf(files[j])
            nodesG1 = G1.nodes()
            nodesG2 = G2.nodes()
            edgesG1 = G1.edges()
            
            union = nodesG1 + list(set(nodesG2) - set(nodesG1))
            inters = intersection(nodesG1, nodesG2)            
            
            print(len(union),len(inters))
            #A1 = nx.adjacency_matrix(G1,nodelist=nodesG1)
            #A2 = nx.adjacency_matrix(G2,nodelist=nodesG2)
            #A1,A2 = [nx.adjacency_matrix(G) for G in [G1,G2]]
            #output[i][j] = nc.deltacon0(A1, A2)
#             output[i][j] = nc.deltacon0(A1, A2) * (len(inters)/len(totalNodes))
            #output[i][j] = nc.deltacon0(A1, A2) / len(union) #deltacon.matrix_1
#            output[i][j] = (int(nc.deltacon0(A1, A2)),len(nodesG1),len(nodesG2),len(inters),len(union))            
            #output[i][j] = nc.deltacon0(A1, A2) * (len(union)/(1 if len(inters) == 0 else len(inters)))
            
            output[i][j] = computeGED(G1,G2)
            output[j][i] = output[i][j] 
            outputDirectory = os.path.abspath(categoryPath).replace("graphs","output/wahed/dcor");
            if not os.path.exists(outputDirectory):
                os.makedirs(outputDirectory)
                
            with open(os.path.join(outputDirectory,"dcon.matrix"), "w") as text_file:
                text_file.write(printMatrix(output, fileNames,False))
                
            with open(os.path.join(outputDirectory,"dcon.matrix.normalized"), "w") as text_file_normalized:
                text_file_normalized.write(printMatrix(output, fileNames))

def parseMatrix(input):
    fo = open(input, "r")
    
    names = []
    data = []
    for line in fo.readlines():
        if(len(names)==0):
            names = line.split()
        else:
            data.append(line.split()[1:])
    
    return np.asfarray(np.array(data),float),names

def medianRandomGraphs(category):
    i = 0
    data = []
    names = []
    for i in range(0,100):
        path = (os.path.join("../output/wahed/dcorCleaned/oecdTopics/randomGml",str(i),category,"dcon.matrix.normalized"))
        currentData,names = parseMatrix(path)
        if(len(data)==0):
            data = currentData
        else:
            data = np.add(data,currentData)
    print(np.divide(data,100))
    
    transformNPArray(np.divide(data,100),names,"test.tex","distance")
    return None

# for i in os.listdir("../graphs/gml"):
#     #print("../graphs/gml/"+i)
#      
#     if(i!="warGML"):
#         analyseCategory("../graphs/gml/"+i)

# for i in os.listdir("../graphs/oecdTopics/gml"):
#     analyseCategory("../graphs/oecdTopics/gml/"+i)
#analyseCategory("../graphsCleaned/oecdTopics/gml/language")

'''for i in range(0,100):
    analyseCategory("../graphsCleaned/oecdTopics/randomGml/"+str(i)+"/language")
''' 

# for i in range(0,100):
#     analyseCategory("../graphsCleaned/oecdTopics/randomGml/"+str(i)+"/language")
     
# for path in Path('../output/wahed/').rglob('*.matrix.normalized'):
#     transform(str(path),str(path)+".tex","distance")
#     print(path)

#medianRandomGraphs("language")

# G1 = read_bf("/home/staff_homes/ahemati/projects/WikiSim/graphsCleaned/oecdTopics/gml/language/hu.gml.bf")
# G2 = read_bf("/home/staff_homes/ahemati/projects/WikiSim/graphsCleaned/oecdTopics/gml/language/ro.gml.bf")

# G1 = read_bf("/home/staff_homes/ahemati/projects/WikiSim/graphsCleaned/oecdTopics/randomGml/7/language/hu.gml.bf")
# G2 = read_bf("/home/staff_homes/ahemati/projects/WikiSim/graphsCleaned/oecdTopics/randomGml/7/language/ro.gml.bf")

data, names = parseMatrix(os.path.join("../output/wahed/dcorCleaned/oecdTopics/randomGml",str(2),"language","dcon.matrix.normalized"))
print(1-np.mean(data))

data, names = parseMatrix(os.path.join("../output/wahed/dcorCleaned/oecdTopics/gml","language","dcon.matrix.normalized"))
print(1-np.mean(data))

