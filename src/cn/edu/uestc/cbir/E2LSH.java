package cn.edu.uestc.cbir;

import java.util.HashMap;
import java.util.HashSet;

public class E2LSH {
    public Index[] indexArray;

    public HashMap<BucketInfo, Float>[] idfMapArray;

    public E2LSH(LshParam[] lshParamArray) {

        indexArray = new Index[lshParamArray.length];
        for (int i = 0; i < indexArray.length; i++) {
            indexArray[i] = new Index(lshParamArray[i]);
        }
        idfMapArray = new HashMap[lshParamArray.length];
        for (int i = 0; i < idfMapArray.length; i++) {
            idfMapArray[i] = new HashMap<>();
        }
    }

    // 桶中只有图像ID和向量ID，没有实际的向量
    public void add(String imageId, HashMap<Integer, BucketInfo[]> bucketArrayMap) {
        // 每个向量需要加到L个桶中
        for (Integer descriptorId : bucketArrayMap.keySet()) {
            for (int i = 0; i < bucketArrayMap.get(descriptorId).length; i++) {
                indexArray[i].add(imageId, descriptorId, bucketArrayMap.get(descriptorId)[i]);
            }
        }
    }

    // 桶中只有图像ID和向量ID，没有实际的向量
    public void add(String imageId, Integer descriptorId, BucketInfo[] bucketArray) {
        // 每个向量需要加到L个桶中
        for (int i = 0; i < indexArray.length; i++) {
            indexArray[i].add(imageId, descriptorId, bucketArray[i]);
        }
    }

    public HashMap<String, HashSet<Integer>> match(Short[] queryDescriptor) {
        HashMap<String, HashSet<Integer>> hashMap = new HashMap<>();
        // 合并这些HashMap
        // 分别在L个index 上match
        for (Index index : indexArray) {
            HashMap<String, HashSet<Integer>> map = index.match(queryDescriptor);
            if (map == null) {
                continue;
            }
            for (String imageId : map.keySet()) {
                if (hashMap.containsKey(imageId)) {
                    hashMap.get(imageId).addAll(map.get(imageId));
                } else {
                    hashMap.put(imageId, map.get(imageId));
                }
            }
        }
        return hashMap;
    }

    public void descriptorsAlloc(HashMap<BucketInfo, HashSet<Integer>>[] matchMapArray, Descriptor[] queryDescriptors) {

        for (Descriptor descriptor : queryDescriptors) {
//		    System.out.println("分配第" + co++ + "个.");
            // 对每个SIFT特征都进行匹配
            for (int i = 0; i < indexArray.length; i++) {
                // 分别在L个index上match
                Index index = indexArray[i];

                BucketInfo bucketInfo = index.compute(descriptor.descriptor);

                if (matchMapArray[i].containsKey(bucketInfo)) {
                    matchMapArray[i].get(bucketInfo).add(descriptor.id);
                } else {
                    HashSet<Integer> tempSet = new HashSet<>();
                    tempSet.add(descriptor.id);
                    matchMapArray[i].put(bucketInfo, tempSet);
                }
            }
        }
    }

    public BucketInfo[] compute(Short[] encryptedDescriptor, Float[][] intermediateResult, short serverId) {
        BucketInfo[] bucketArray = new BucketInfo[indexArray.length];
        for (int i = 0; i < indexArray.length; i++) {
            Index index = indexArray[i];
            bucketArray[i] = index.compute(encryptedDescriptor, intermediateResult[i], serverId);
        }
        return bucketArray;
    }

    @Override
    public String toString() {
        int bucketNumber = 0;
        int descriptorNumber = 0;
        for (Index index : indexArray) {
            bucketNumber += index.bucketMap.size();
            for (BucketInfo bucket : index.bucketMap.keySet()) {
                for (String imageId : index.bucketMap.get(bucket).keySet()) {
                    descriptorNumber += index.bucketMap.get(bucket).get(imageId).size();
                }
            }
        }
//		System.out.println("桶数量: " + bucketNumber + "；特征数量: " + descriptorNumber);
        return "索引大小:" + String.valueOf((bucketNumber * 12.0 + descriptorNumber * 4) / 1024 / 1024 + " MB");
    }

    public HashMap<String, HashSet<Integer>> getBucketContent(int indexIndex, BucketInfo bucketInfo) {
        HashMap<String, HashSet<Integer>> bucketContentMap = null;
        if (this.indexArray[indexIndex].bucketMap.containsKey(bucketInfo)) {
            bucketContentMap = this.indexArray[indexIndex].bucketMap.get(bucketInfo);
        }
        return bucketContentMap;
    }

    public float getIdf(int indexIndex, BucketInfo tempBucketInfo) {
        float idf = 0.0f;
        HashMap<BucketInfo, HashMap<String, HashSet<Integer>>> bucketContent = indexArray[indexIndex].bucketMap;
        for (BucketInfo bucketInfo : bucketContent.keySet()) {
            if (bucketInfo.equals(tempBucketInfo)) {
                idf = bucketInfo.idf;
            }
        }
        return idf;
    }
}
