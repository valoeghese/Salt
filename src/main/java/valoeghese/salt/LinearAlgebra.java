package valoeghese.salt;

public class LinearAlgebra {
	/**
	 * Solves the given system of equations using gaussian elimination. Written by Github Copilot.
	 * @param matrix the matrix of coefficients.
	 * @param b the vector of constants.
	 * @return the solution vector.
	 */
	public static double[] solveSystem(double[][] matrix, double[] b) {
		int n = matrix.length;
		double[][] augmented = new double[n][n + 1];

		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				augmented[i][j] = matrix[i][j];
			}

			augmented[i][n] = b[i];
		}

		for (int i = 0; i < n; ++i) {
			int max = i;

			for (int j = i + 1; j < n; ++j) {
				if (Math.abs(augmented[j][i]) > Math.abs(augmented[max][i])) {
					max = j;
				}
			}

			double[] temp = augmented[i];
			augmented[i] = augmented[max];
			augmented[max] = temp;

			for (int j = i + 1; j < n; ++j) {
				double factor = augmented[j][i] / augmented[i][i];

				for (int k = i; k < n + 1; ++k) {
					augmented[j][k] -= factor * augmented[i][k];
				}
			}
		}

		double[] solution = new double[n];

		for (int i = n - 1; i >= 0; --i) {
			double sum = 0.0;

			for (int j = i + 1; j < n; ++j) {
				sum += augmented[i][j] * solution[j];
			}

			solution[i] = (augmented[i][n] - sum) / augmented[i][i];
		}

		return solution;
	}
}
