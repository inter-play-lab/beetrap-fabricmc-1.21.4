package beetrap.btfmc.util;

import org.ejml.simple.SimpleMatrix;

public final class MoreMatrices {

    private MoreMatrices() {
        throw new AssertionError();
    }

    public static boolean equalInDimensions(SimpleMatrix A, SimpleMatrix B) {
        return A.getNumRows() == B.getNumRows() && A.getNumCols() == B.getNumCols();
    }

    public static boolean isSquare(SimpleMatrix A) {
        return A.getNumRows() == A.getNumCols();
    }

    public static SimpleMatrix hadamardProduct(SimpleMatrix A, SimpleMatrix B) {
        if(!equalInDimensions(A, B)) {
            throw new IllegalArgumentException("The two matrices are different in dimensions.");
        }

        int r = A.getNumRows();
        int c = B.getNumCols();
        SimpleMatrix R = new SimpleMatrix(r, c);

        for(int i = 0; i < r; ++i) {
            for(int j = 0; j < c; ++j) {
                R.set(i, j, A.get(i, j) * B.get(i, j));
            }
        }

        return R;
    }

    public static SimpleMatrix sqrtDiagonal(SimpleMatrix A) {
        SimpleMatrix R = A.copy();
        int r = R.getNumRows();

        for(int i = 0; i < r; ++i) {
            R.set(i, i, Math.sqrt(A.get(i, i)));
        }

        return R;
    }

    public static SimpleMatrix truncateColumns(SimpleMatrix A, int k) {
        SimpleMatrix A_ = new SimpleMatrix(A.getNumRows(), k);

        for(int i = 0; i < k; ++i) {
            A_.setColumn(i, A.getColumn(i));
        }

        return A_;
    }

    public static SimpleMatrix getSubSquareMatrix(SimpleMatrix A, int k) {
        SimpleMatrix A_ = new SimpleMatrix(k, k);

        for(int i = 0; i < k; ++i) {
            for(int j = 0; j < k; ++j) {
                A_.set(i, j, A.get(i, j));
            }
        }

        return A_;
    }
}
