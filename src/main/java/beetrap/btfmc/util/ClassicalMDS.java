package beetrap.btfmc.util;


import static beetrap.btfmc.util.MoreMatrices.hadamardProduct;
import static beetrap.btfmc.util.MoreMatrices.isSquare;

import java.util.Arrays;
import java.util.stream.IntStream;
import org.ejml.dense.row.decomposition.eig.SymmetricQRAlgorithmDecomposition_DDRM;
import org.ejml.simple.SimpleMatrix;

/**
 * Implements the process described in
 * <a href="https://rich-d-wilkinson.github.io/MATH3030/6.1-classical-mds.html#">this article</a>.
 *
 * @author Kenneth Fei
 */
public class ClassicalMDS {
    private final SimpleMatrix D;
    private final int n;
    private final int k;
    private SimpleMatrix result;

    /**
     * @param D a Euclidean distance matrix of n data points in R^p.
     * @param k the target dimension of distance preserving data points.
     */
    public ClassicalMDS(SimpleMatrix D, int k) {
        if(!isSquare(D)) {
            throw new IllegalArgumentException("Distance matrix is not a square matrix.");
        }

        this.D = D;
        this.n = this.D.getNumRows();
        this.k = k;
    }

    /**
     * Sorts the eigenvalues in descending order, while reordering eigenvectors as well. The two
     * matrices are obtained from eigendecomposition
     *
     * @param eigenvalues  the eigenvalues in a diagonal matrix
     * @param eigenvectors the eigen column vectors in a matrix
     */
    private static void sort(SimpleMatrix eigenvalues, SimpleMatrix eigenvectors) {
        int n = eigenvalues.getNumRows();
        Integer[] idx = IntStream.range(0, n).boxed().toArray(Integer[]::new);

        // 1-line stable sort: largest eigenvalues first
        Arrays.sort(idx,
                (a, b) -> Double.compare(eigenvalues.get(b, b), eigenvalues.get(a, a)));

        // Apply the permutation in one pass
        double[] newEigenvalues = new double[n];
        SimpleMatrix newEigenvectors = new SimpleMatrix(eigenvectors.getNumRows(),
                n);

        for(int k = 0; k < n; ++k) {
            int j = idx[k];
            newEigenvalues[k] = eigenvalues.get(j, j);          // diagonal entry
            newEigenvectors.setColumn(k,
                    eigenvectors.getColumn(j));           // matching eigenvector
        }

        // Write back
        for(int k = 0; k < n; ++k) {
            eigenvalues.set(k, k, newEigenvalues[k]);
            eigenvectors.setColumn(k, newEigenvectors.getColumn(k));
        }
    }

    public void compute() {
        SimpleMatrix A = hadamardProduct(D, D).scale(-0.5);
        SimpleMatrix H = SimpleMatrix.identity(this.n).plus(-1.0 / this.n);
        SimpleMatrix B = H.mult(A).mult(H);
        SymmetricQRAlgorithmDecomposition_DDRM ed = new SymmetricQRAlgorithmDecomposition_DDRM(true);
        ed.setMaxIterations(100);
        ed.decompose(B.getDDRM());

        int n = ed.getNumberOfEigenvalues();
        SimpleMatrix U = new SimpleMatrix(n, n);

        for(int i = 0; i < n; ++i) {
            U.setColumn(i, 0, ed.getEigenVector(i).data);
        }

        SimpleMatrix Lambda = new SimpleMatrix(n, n);
        for(int i = 0; i < n; ++i) {
            Lambda.set(i, i, ed.getEigenvalue(i));
        }

        sort(Lambda, U);
        SimpleMatrix Lambda_ = MoreMatrices.getSubSquareMatrix(Lambda, this.k);
        SimpleMatrix U_ = MoreMatrices.truncateColumns(U, this.k);
        this.result = U_.mult(MoreMatrices.sqrtDiagonal(Lambda_));
    }

    public SimpleMatrix getResult() {
        return this.result;
    }
}
