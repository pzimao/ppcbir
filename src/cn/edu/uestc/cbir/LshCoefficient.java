package cn.edu.uestc.cbir;

import java.io.Serializable;
import java.util.Random;

public class LshCoefficient implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -134283924245332489L;
    public Float[] a;
    public Float b;
    public int w;

    public LshCoefficient() {
    }

    public LshCoefficient(int d, int w, Random random) {
        this.a = new Float[d];
        this.b = random.nextFloat() * w;
        this.w = w;
        for (int i = 0; i < d; i++) {
            a[i] = (float) random.nextGaussian();
        }
    }

    public LshCoefficient(Float[] a, Float b, int w) {
        super();
        this.a = a;
        this.b = b;
        this.w = w;
    }

    public Float[] getA() {
        return a;
    }

    public void setA(Float[] a) {
        this.a = a;
    }

    public Float getB() {
        return b;
    }

    public void setB(Float b) {
        this.b = b;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

}
