package valoeghese.salt.tests;

import org.junit.Test;
import valoeghese.salt.LinearAlgebra;

import static junit.framework.Assert.assertEquals;

public class TestLinearAlgebra {
	/**
	 * Tests the linear algebra class' system solver.
	 * Rather fittingly this test was written by Github Copilot as well.
	 */
	@Test
	public void testSystemSolver() {
		// Test first matrix
		double[][] matrix = new double[][] {
				{ 1, 2, 3 },
				{ 4, -5, 6 },
				{ 7, 8, 9 }
		};

		double[] b = new double[] { 1, 2, 3 };
		double[] solution = LinearAlgebra.solveSystem(matrix, b);

		// Print solution vector
		printVector(solution);

		// Test values
		assertEquals(solution[0], 0, 0.0001);
		assertEquals(solution[1], 0, 0.0001);
		assertEquals(solution[2], 1.0 / 3.0, 0.0001);

		// Test second matrix
		matrix = new double[][] {
				{ 1, 2, 3 },
				{ 0, 9, -1 },
				{ 7, 8, 9 }
		};

		b = new double[] { 1, 2, 0 };
		solution = LinearAlgebra.solveSystem(matrix, b);

		// Print solution vector
		printVector(solution);

		// Test values
		assertEquals(solution[0], -101.0 / 114.0, 0.0001);
		assertEquals(solution[1], 31.0 / 114.0, 0.0001);
		assertEquals(solution[2], 17.0 / 38.0, 0.0001);
	}

	private static void printVector(double[] vector) {
		System.out.print("[");

		for (int i = 0; i < vector.length; ++i) {
			System.out.print(vector[i]);

			if (i != vector.length - 1) {
				System.out.print(", ");
			}
		}

		System.out.println("]");
	}
}
