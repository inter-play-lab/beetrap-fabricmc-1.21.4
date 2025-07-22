package beetrap.btfmc.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AlgorithmOfFloyd {

    private final Random r;
    private final int n;

    public AlgorithmOfFloyd(int n) {
        this.r = new Random();
        this.n = n;
    }

    public Set<Integer> sample(int k) {
        Set<Integer> r = new HashSet<>();

        for(int i = this.n - k; i < this.n; ++i) {
            int j = this.r.nextInt(0, i + 1);

            if(r.contains(j)) {
                r.add(i);
            } else {
                r.add(j);
            }
        }

        return r;
    }
}
