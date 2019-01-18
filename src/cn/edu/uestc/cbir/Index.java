package cn.edu.uestc.cbir;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class Index implements Serializable {
    private static final long serialVersionUID = -7601000893336920290L;
    public HashMap<BucketInfo, HashMap<String, HashSet<Integer>>> bucketMap = new HashMap<>();
    public LshParam lshParam;

    public Index(LshParam lshParam) {
        this.lshParam = lshParam;
    }

    public void add(String imageId, Integer descriptorId, BucketInfo bucket) {
        if (bucketMap.containsKey(bucket)) {
            HashMap<String, HashSet<Integer>> bucketContent = bucketMap.get(bucket);
            if (bucketContent.containsKey(imageId)) {
                bucketContent.get(imageId).add(descriptorId);
            } else {
                HashSet<Integer> set = new HashSet<>();
                set.add(descriptorId);
                bucketContent.put(imageId, set);
            }
        } else {
            HashMap<String, HashSet<Integer>> bucketContent = new HashMap<>();
            HashSet<Integer> descriptorSet = new HashSet<>();
            descriptorSet.add(descriptorId);
            bucketContent.put(imageId, descriptorSet);
            bucketMap.put(bucket, bucketContent);
        }
    }

    public HashMap<String, HashSet<Integer>> match(Short[] queryDescriptor) {
        // 计算查询向量落到的桶
        BucketInfo bucket = this.compute(queryDescriptor);
        return this.bucketMap.get(bucket);
    }

//	public BucketInfo bowMatch(Short[] queryDescriptor) {
//		// 计算查询向量落到的桶
//		BucketInfo bucket = this.compute(queryDescriptor);
//		for (BucketInfo b : this.bucketMap.keySet()) {
//			if (b.compareTo(bucket) == 0) {
//				bucket = b;
//				break;
//			}
//		}
//		return this.getBucket(bucket);
//	}

    public BucketInfo compute(Short[] descriptor) {
        int[] gResult = new int[lshParam.K];
        for (int lshIndex = 0; lshIndex < lshParam.K; lshIndex++) {
            Float result = 0.0f;
            for (int i = 0; i < lshParam.d; i++) {
                result = result + descriptor[i] * lshParam.lshCoefficients[lshIndex].a[i];
            }
            gResult[lshIndex] = (int) Math.floor((result + lshParam.lshCoefficients[lshIndex].b) / lshParam.w);

        }
        long id1 = 0;
        long id2 = 0;
        for (int i = 0; i < lshParam.K; i++) {
            id1 = id1 + gResult[i] * lshParam.h1Coefficient.randNums[i];
            id2 = id2 + gResult[i] * lshParam.h2Coefficient.randNums[i];
        }
        id1 = id1 % HCoeffiecient.C % lshParam.tableSize;
        id1 = id1 >= 0 ? id1 : id1 + lshParam.tableSize;

        id2 = id2 % HCoeffiecient.C;
        id2 = id2 >= 0 ? id2 : id2 + HCoeffiecient.C;
        return new BucketInfo(id1, id2);
    }

    public BucketInfo compute(Short[] encryptedDescriptor, Float[] result2s, Short serverId) {
        int[] gResult = new int[lshParam.K];
        for (int lshIndex = 0; lshIndex < lshParam.K; lshIndex++) {
            Float result = 0.0f;
            for (int i = 0; i < lshParam.d; i++) {
                result = result + encryptedDescriptor[i] * lshParam.lshCoefficients[lshIndex].a[i];
            }
            if (serverId == 1) {
                gResult[lshIndex] = (int) Math
                        .floor((result - result2s[lshIndex] + lshParam.lshCoefficients[lshIndex].b) / lshParam.w);

            } else {
                gResult[lshIndex] = (int) Math
                        .floor((result2s[lshIndex] - result + lshParam.lshCoefficients[lshIndex].b) / lshParam.w);
            }
        }
        long id1 = 0;
        long id2 = 0;
        for (int i = 0; i < lshParam.K; i++) {
            id1 = id1 + gResult[i] * lshParam.h1Coefficient.randNums[i];
            id2 = id2 + gResult[i] * lshParam.h2Coefficient.randNums[i];
        }
        id1 = id1 % HCoeffiecient.C % lshParam.tableSize;
        id1 = id1 >= 0 ? id1 : id1 + lshParam.tableSize;

        id2 = id2 % HCoeffiecient.C;
        id2 = id2 >= 0 ? id2 : id2 + HCoeffiecient.C;
        return new BucketInfo(id1, id2);
    }

    public Float[] computeResult2(Short[] descriptor_v) {
        Float[] result2s = new Float[lshParam.K];
        for (int lshIndex = 0; lshIndex < lshParam.K; lshIndex++) {
            Float result = 0.0f;
            for (int i = 0; i < lshParam.d; i++) {
                result = result + descriptor_v[i] * lshParam.lshCoefficients[lshIndex].a[i];
            }
            result2s[lshIndex] = result;
        }
        return result2s;
    }

    public BucketInfo compute2(Short[] v1, Float[] result2s) {
        int[] gResult = new int[lshParam.K];
        for (int lshIndex = 0; lshIndex < lshParam.K; lshIndex++) {
            Float result = 0.0f;
            for (int i = 0; i < lshParam.d; i++) {
                result = result + v1[i] * lshParam.lshCoefficients[lshIndex].a[i];
            }
            gResult[lshIndex] = (int) Math.floor(result * 1.0 / lshParam.w - result2s[lshIndex]);
        }
        long id1 = 0;
        long id2 = 0;
        for (int i = 0; i < lshParam.K; i++) {
            id1 = id1 + gResult[i] * lshParam.h1Coefficient.randNums[i];
            id2 = id2 + gResult[i] * lshParam.h2Coefficient.randNums[i];
        }
        id1 = id1 % HCoeffiecient.C % lshParam.tableSize;
        id1 = id1 >= 0 ? id1 : id1 + lshParam.tableSize;

        id2 = id2 % HCoeffiecient.C;
        id2 = id2 >= 0 ? id2 : id2 + HCoeffiecient.C;
        return new BucketInfo(id1, id2);
    }
//	public Bucket getBucket(BucketInfo bucket) {
//		for (BucketInfo realBucket : this.bucketMap.keySet() ){
//			if (realBucket.equals(bucket)) {
//				return new Bucket(bucket, this.bucketMap.get(bucket));
//			}
//		}
//		return new Bucket(bucket, new HashMap<>());
//	}
}
