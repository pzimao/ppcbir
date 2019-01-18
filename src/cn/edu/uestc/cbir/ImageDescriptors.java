package cn.edu.uestc.cbir;

import java.io.Serializable;

public class ImageDescriptors implements Serializable {


    public String imageId;
    public Integer[] descriptorIdArray;

    public ImageDescriptors(String imageId, Integer[] descriptorIdArray) {
        this.imageId = imageId;
        this.descriptorIdArray = descriptorIdArray;
    }
}
