package cn.edu.uestc.cbir;

import java.io.Serializable;

public class BucketInfo implements Comparable<BucketInfo>, Serializable {
    private static final long serialVersionUID = 7051453668219060710L;
    public long id1;
    public long id2;
    public float idf = 0.0f;

    public BucketInfo(long id1, long id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public BucketInfo(long[] hashResult) {
        this.id1 = hashResult[0];
        this.id2 = hashResult[1];
    }

    @Override
    public int hashCode() {
        return (int) (id1 + id2);
    }

    @Override
    public boolean equals(Object obj) {
        BucketInfo word2 = (BucketInfo) obj;
        return this.id1 == word2.id1 && this.id2 == word2.id2;
    }

    @Override
    public int compareTo(BucketInfo o) {
        if (this.id1 < o.id1) {
            return -1;
        }
        if (this.id1 > o.id1) {
            return 1;
        }
        if (this.id2 < o.id2) {
            return -1;
        }
        if (this.id2 > o.id2) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "(" + id1 + ", " + id2 + ", " + idf + ") ";
    }
}
