package cn.edu.uestc.cbir;

import java.io.Serializable;
import java.util.Random;

public class LshParam implements Serializable {
    private static final long serialVersionUID = -3897027846498372317L;
    public HCoeffiecient h1Coefficient;
    public HCoeffiecient h2Coefficient;
    public LshCoefficient[] lshCoefficients;
    public int K;
    public int w;
    public int d;
    public int tableSize;
    public Random rand;

    public LshParam() {
    }

    public LshParam(int K, int w, int tableSize) {
        this.tableSize = tableSize;
        this.K = K;
        this.d = 128;
        this.w = w;
        // 建立哈希表
        this.rand = new Random();

        // 生成K组LSH函数的系数
        this.lshCoefficients = new LshCoefficient[K];

        // 生成H1和H2的系数
        this.h1Coefficient = new HCoeffiecient(K, this.rand);
        this.h2Coefficient = new HCoeffiecient(K, this.rand);
        for (int i = 0; i < K; i++) {
            this.lshCoefficients[i] = new LshCoefficient(d, w, rand);
        }
    }
}