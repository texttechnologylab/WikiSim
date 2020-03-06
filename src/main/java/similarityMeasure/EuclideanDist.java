package similarityMeasure;

public class EuclideanDist {

    public static double calc(double[] a, double[] b) {
        double diff_square_sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            diff_square_sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(diff_square_sum);
    }

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
	
	public static double distance(String vectorA, String vectorB) {
		return calc(convert(vectorA), convert(vectorB));
	}

}