package cn.edu.uestc.cbir;

import java.io.Serializable;
import java.util.Random;

public class HCoeffiecient implements Serializable {
    public static final long C = 4294967291L;
    /**
     *
     */
    private static final long serialVersionUID = -1957025229591502936L;
    public int[] randNums;

    public HCoeffiecient(int K, Random random) {
        randNums = new int[K];
        for (int i = 0; i < K; i++) {
            this.randNums[i] = random.nextInt();
        }
    }

    public HCoeffiecient(int[] randNums) {
        super();
        this.randNums = randNums;
    }

    public int[] getRandNums() {
        return randNums;
    }

    public void setRandNums(int[] randNums) {
        this.randNums = randNums;
    }

}
