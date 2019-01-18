package cn.edu.uestc.cbir;

import java.io.Serializable;

public class Descriptor implements Serializable {
    public int id;
    public Short[] descriptor;

    public Descriptor(int id, Short[] descriptor) {
        this.id = id;
        this.descriptor = descriptor;
    }
}
