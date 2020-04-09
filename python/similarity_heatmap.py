import optparse
import sys
import os
get_path = lambda path: os.path.join(os.path.dirname(__file__), path)

def transform(input,output,modus):
    tikzpicture = "\\begin{tikzpicture}\n\\node [inner sep=0pt] (tab) {\n"
    tikzpicture += "\\begin{tabular}{r"
    
    first = True
    fo = open(input, "r")
    
    for line in fo.readlines():
        if(first):
            for i in line.split("\t"):
                tikzpicture += "R"
            tikzpicture += "}"
            tikzpicture += "\n & "
            
            for i in range(0,len(line.split("\t"))):
                tikzpicture += " \\mcvert{" + line.split("\t")[i].strip() +"} "
                if(i < len(line.split("\t"))-1):
                    tikzpicture += "&"
            tikzpicture += "\\\\\n"
            first = False
        else:
            split = line.strip().split("\t")
            for i in range(0,len(split)):
                if(i == 0):
                    tikzpicture += "\\mchori{" + split[0] + "} "
                else:
                    value = round(float(split[i]),2)
                    if(modus == "distance"):
                        value = 1 - value
                    tikzpicture += " " + str(value) + " " 
                if(i < len(split)-1):
                    tikzpicture += " & " 
            tikzpicture += "\\\\\n"
    tikzpicture += "\\end{tabular}\n};\n\\end{tikzpicture}"
    
    
    with open(get_path('../texOutputs/base_similarity_heatmap.tex'), 'r') as file:
        data = file.read()
        data = data.replace("xxxxxx",tikzpicture)
        
    with open(output, "w") as text_file:
        text_file.write(data)
        print("Output file created at: " + os.path.abspath(output))

if __name__ == '__main__':
    parser = optparse.OptionParser()
    
    parser.add_option('-i', '--input', metavar="FILE", dest="input",
        help="Path to input matrix file")
    
    parser.add_option('-m', '--modus', dest="modus",
        help="Input matrix is distance or similarity",
        choices=['distance', 'similarity'], default='distance')
    
    
    
    parser.add_option('-o', '--output', metavar="FILE", dest="output",
        help="Path to output tex file")
    
    options, args = parser.parse_args()
    
    if(not options.input or not options.output):
        parser.error("input and output must be defined")
        
    transform(options.input,options.output,options.modus)

