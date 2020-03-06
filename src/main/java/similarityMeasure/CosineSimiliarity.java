package similarityMeasure;

public class CosineSimiliarity {


	private static double[] convert(String string){
		String[]split = string.split("\\s+");
		double[]output = new double[split.length];
		for (int i = 0; i< split.length;i++){
			output[i] = Double.parseDouble(split[i]);
		}
		return output;
	}

	private static double[]subtract(double[]a,double[]b){
		double []output = new double[a.length];
		for (int i = 0; i < output.length; i++) {
			output[i]= a[i]-b[i];
		}
		return output;
	}

	public static double calc(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}   
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	public static double cosineSimilarity(String vectorA, String vectorB) {
		return calc(convert(vectorA), convert(vectorB));
	}

}