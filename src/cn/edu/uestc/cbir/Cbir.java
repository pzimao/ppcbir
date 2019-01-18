package cn.edu.uestc.cbir;

import cn.edu.uestc.utils.CbirConstant;
import cn.edu.uestc.utils.LogUtil;
import cn.edu.uestc.utils.SiftExt_test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class Cbir {
    public static HashSet<String> filteredSet = new HashSet<>();
    public static HashMap<String, Float> scoreMap = new HashMap<>();
    public static ArrayList<Similarity> scoreArray = new ArrayList<>();
    public static Server S1, S2;
    public LshParam[] lshParamArray;
    public Descriptor[] queryDescriptors;
    public int k; // The top-k images are returned.
    public HashSet<String> imageSet = new HashSet<>();

    public static HashMap<BucketInfo, HashSet<Integer>>[] queryDescriptorAllocResultMapArray; // 这个东西用来记录查询图像向量在L个哈希表上的分配情况

    public ArrayList<Similarity> resultList; // 图像检索的最终排序

    public String printInfo = "";

    public static Short[] vectorSub(Short[] vector1, Short[] vector2) {

        Short[] resultVector = new Short[vector1.length];
        for (int i = 0; i < vector1.length; i++) {
            resultVector[i] = (short) (vector1[i] - vector2[i]);
        }
        return resultVector;
    }

    public static Float vectorLength(Short[] vector) {
        int sum = 0;
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i] * vector[i];
        }
        return (float) Math.sqrt(sum);
    }

    public Cbir(int L, int K, int w, int tableSize, int curPort1, int curPort2) throws Exception {
        // 序列化参数
        lshParamArray = null;
        File file = new File("lsh_param.dat");
        if (file.exists()) {
//        if (1 < 0) {
            //如果已经存在，那么就反序列化
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            lshParamArray = (LshParam[]) in.readObject();
            in.close();
            fileIn.close();
            System.out.println("反序列化参数完成");
        } else {
            // 不存在系数
            // 现在生成并序列化
            lshParamArray = new LshParam[L];
            FileOutputStream fileOut =
                    new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            LogUtil.info("E2LSH系数生成");
            for (int i = 0; i < L; i++) {
                lshParamArray[i] = new LshParam(K, w, tableSize);
            }
            out.writeObject(lshParamArray);
            out.close();
            fileOut.close();
            System.out.println("序列化参数完成");
        }


        LshParam[] lshParams = new LshParam[L];
        for (int i = 0; i < lshParams.length; i++) {
            lshParams[i] = lshParamArray[i];
        }
        lshParamArray = lshParams;
        S1 = new Server((short) 1, curPort1, curPort2, lshParamArray);
        S2 = new Server((short) 2, curPort2, curPort1, lshParamArray);
        S1.service();
        S2.service();
        // 加载OPENCV库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        imageSet.add("paris_general_000002.jpg");
        imageSet.add("paris_general_000023.jpg");
        imageSet.add("paris_general_000043.jpg");
        imageSet.add("paris_general_000089.jpg");
        imageSet.add("paris_general_000091.jpg");
        imageSet.add("paris_general_000092.jpg");
        imageSet.add("paris_general_000107.jpg");
        imageSet.add("paris_general_000114.jpg");
        imageSet.add("paris_general_000120.jpg");
        imageSet.add("paris_general_000143.jpg");
        imageSet.add("paris_general_000241.jpg");
        imageSet.add("paris_general_000271.jpg");
        imageSet.add("paris_general_000277.jpg");
        imageSet.add("paris_general_000294.jpg");
        imageSet.add("paris_general_000297.jpg");
        imageSet.add("paris_general_000299.jpg");
        imageSet.add("paris_general_000313.jpg");
        imageSet.add("paris_general_000342.jpg");
        imageSet.add("paris_general_000343.jpg");
        imageSet.add("paris_general_000355.jpg");
        imageSet.add("paris_general_000361.jpg");
        imageSet.add("paris_general_000379.jpg");
        imageSet.add("paris_general_000404.jpg");
        imageSet.add("paris_general_000412.jpg");
        imageSet.add("paris_general_000422.jpg");
        imageSet.add("paris_general_000458.jpg");
        imageSet.add("paris_general_000465.jpg");
        imageSet.add("paris_general_000508.jpg");
        imageSet.add("paris_general_000594.jpg");
        imageSet.add("paris_general_000628.jpg");
        imageSet.add("paris_general_000692.jpg");
        imageSet.add("paris_general_000695.jpg");
        imageSet.add("paris_general_000697.jpg");
        imageSet.add("paris_general_000723.jpg");
        imageSet.add("paris_general_000727.jpg");
        imageSet.add("paris_general_000730.jpg");
        imageSet.add("paris_general_000785.jpg");
        imageSet.add("paris_general_000794.jpg");
        imageSet.add("paris_general_000809.jpg");
        imageSet.add("paris_general_000836.jpg");
        imageSet.add("paris_general_000838.jpg");
        imageSet.add("paris_general_000889.jpg");
        imageSet.add("paris_general_000893.jpg");
        imageSet.add("paris_general_000904.jpg");
        imageSet.add("paris_general_000908.jpg");
        imageSet.add("paris_general_000935.jpg");
        imageSet.add("paris_general_000936.jpg");
        imageSet.add("paris_general_000974.jpg");
        imageSet.add("paris_general_001019.jpg");
        imageSet.add("paris_general_001041.jpg");
        imageSet.add("paris_general_001046.jpg");
        imageSet.add("paris_general_001093.jpg");
        imageSet.add("paris_general_001098.jpg");
        imageSet.add("paris_general_001115.jpg");
        imageSet.add("paris_general_001235.jpg");
        imageSet.add("paris_general_001238.jpg");
        imageSet.add("paris_general_001239.jpg");
        imageSet.add("paris_general_001241.jpg");
        imageSet.add("paris_general_001265.jpg");
        imageSet.add("paris_general_001331.jpg");
        imageSet.add("paris_general_001445.jpg");
        imageSet.add("paris_general_001481.jpg");
        imageSet.add("paris_general_001615.jpg");
        imageSet.add("paris_general_001620.jpg");
        imageSet.add("paris_general_001645.jpg");
        imageSet.add("paris_general_001675.jpg");
        imageSet.add("paris_general_001676.jpg");
        imageSet.add("paris_general_001697.jpg");
        imageSet.add("paris_general_001704.jpg");
        imageSet.add("paris_general_001722.jpg");
        imageSet.add("paris_general_001728.jpg");
        imageSet.add("paris_general_001729.jpg");
        imageSet.add("paris_general_001732.jpg");
        imageSet.add("paris_general_001745.jpg");
        imageSet.add("paris_general_001806.jpg");
        imageSet.add("paris_general_001809.jpg");
        imageSet.add("paris_general_001821.jpg");
        imageSet.add("paris_general_001840.jpg");
        imageSet.add("paris_general_001846.jpg");
        imageSet.add("paris_general_001855.jpg");
        imageSet.add("paris_general_001869.jpg");
        imageSet.add("paris_general_001870.jpg");
        imageSet.add("paris_general_001876.jpg");
        imageSet.add("paris_general_001883.jpg");
        imageSet.add("paris_general_001886.jpg");
        imageSet.add("paris_general_001903.jpg");
        imageSet.add("paris_general_001919.jpg");
        imageSet.add("paris_general_001930.jpg");
        imageSet.add("paris_general_001931.jpg");
        imageSet.add("paris_general_001935.jpg");
        imageSet.add("paris_general_001938.jpg");
        imageSet.add("paris_general_001943.jpg");
        imageSet.add("paris_general_001954.jpg");
        imageSet.add("paris_general_001969.jpg");
        imageSet.add("paris_general_001972.jpg");
        imageSet.add("paris_general_002000.jpg");
        imageSet.add("paris_general_002004.jpg");
        imageSet.add("paris_general_002027.jpg");
        imageSet.add("paris_general_002032.jpg");
        imageSet.add("paris_general_002038.jpg");
        imageSet.add("paris_general_002043.jpg");
        imageSet.add("paris_general_002049.jpg");
        imageSet.add("paris_general_002102.jpg");
        imageSet.add("paris_general_002107.jpg");
        imageSet.add("paris_general_002122.jpg");
        imageSet.add("paris_general_002150.jpg");
        imageSet.add("paris_general_002173.jpg");
        imageSet.add("paris_general_002177.jpg");
        imageSet.add("paris_general_002186.jpg");
        imageSet.add("paris_general_002250.jpg");
        imageSet.add("paris_general_002209.jpg");
        imageSet.add("paris_general_002220.jpg");
        imageSet.add("paris_general_002231.jpg");
        imageSet.add("paris_general_002332.jpg");
        imageSet.add("paris_general_002337.jpg");
        imageSet.add("paris_general_002257.jpg");
        imageSet.add("paris_general_002282.jpg");
        imageSet.add("paris_general_002315.jpg");
        imageSet.add("paris_general_002322.jpg");
        imageSet.add("paris_general_002335.jpg");
        imageSet.add("paris_general_002338.jpg");
        imageSet.add("paris_general_002340.jpg");
        imageSet.add("paris_general_002352.jpg");
        imageSet.add("paris_general_002359.jpg");
        imageSet.add("paris_general_002362.jpg");
        imageSet.add("paris_general_002386.jpg");
        imageSet.add("paris_general_002391.jpg");
        imageSet.add("paris_general_002398.jpg");
        imageSet.add("paris_general_002416.jpg");
        imageSet.add("paris_general_002443.jpg");
        imageSet.add("paris_general_002444.jpg");
        imageSet.add("paris_general_002459.jpg");
        imageSet.add("paris_general_002473.jpg");
        imageSet.add("paris_general_002490.jpg");
        imageSet.add("paris_general_002493.jpg");
        imageSet.add("paris_general_002502.jpg");
        imageSet.add("paris_general_002509.jpg");
        imageSet.add("paris_general_002515.jpg");
        imageSet.add("paris_general_002522.jpg");
        imageSet.add("paris_general_002541.jpg");
        imageSet.add("paris_general_002575.jpg");
        imageSet.add("paris_general_002589.jpg");
        imageSet.add("paris_general_002592.jpg");
        imageSet.add("paris_general_002595.jpg");
        imageSet.add("paris_general_002609.jpg");
        imageSet.add("paris_general_002610.jpg");
        imageSet.add("paris_general_002641.jpg");
        imageSet.add("paris_general_002645.jpg");
        imageSet.add("paris_general_002697.jpg");
        imageSet.add("paris_general_002703.jpg");
        imageSet.add("paris_general_002708.jpg");
        imageSet.add("paris_general_002711.jpg");
        imageSet.add("paris_general_002721.jpg");
        imageSet.add("paris_general_002726.jpg");
        imageSet.add("paris_general_002743.jpg");
        imageSet.add("paris_general_002748.jpg");
        imageSet.add("paris_general_002764.jpg");
        imageSet.add("paris_general_002784.jpg");
        imageSet.add("paris_general_002794.jpg");
        imageSet.add("paris_general_002800.jpg");
        imageSet.add("paris_general_002803.jpg");
        imageSet.add("paris_general_002822.jpg");
        imageSet.add("paris_general_002832.jpg");
        imageSet.add("paris_general_002842.jpg");
        imageSet.add("paris_general_002926.jpg");
        imageSet.add("paris_general_002934.jpg");
        imageSet.add("paris_general_002948.jpg");
        imageSet.add("paris_general_002955.jpg");
        imageSet.add("paris_general_002961.jpg");
        imageSet.add("paris_general_002966.jpg");
        imageSet.add("paris_general_003005.jpg");
        imageSet.add("paris_general_003011.jpg");
        imageSet.add("paris_general_003020.jpg");
        imageSet.add("paris_general_003023.jpg");
        imageSet.add("paris_general_003038.jpg");
        imageSet.add("paris_general_003053.jpg");
        imageSet.add("paris_general_003082.jpg");
        imageSet.add("paris_general_003086.jpg");
        imageSet.add("paris_general_003088.jpg");
        imageSet.add("paris_general_003104.jpg");

    }

    public void featureExtract() throws Exception {
        LogUtil.start();
        S1.featureExtract();
        S2.featureExtract();
        LogUtil.info("FeaExt()完成-");
        LogUtil.end();
    }

    public void indexBuild() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        S1.countDownLatch = countDownLatch;
        S2.countDownLatch = countDownLatch;
        // 依次开始建立索引
        LogUtil.start();
        new Thread(() -> {
            try {
                S1.indexBuild();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                S2.indexBuild();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        countDownLatch.await();
        LogUtil.info("IdxBld()完成-");
        LogUtil.end();
    }

    public void updateIDF() throws Exception {
        // S1 更新自己的IDF
        CountDownLatch countDownLatch = new CountDownLatch(2);
        S1.countDownLatch = countDownLatch;
        S2.countDownLatch = countDownLatch;

        LogUtil.start();
        new Thread(() -> {
            try {
                S1.updateIDF1();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                S2.updateIDF1();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        countDownLatch.await();
        countDownLatch = new CountDownLatch(2);
        S1.countDownLatch = countDownLatch;
        S2.countDownLatch = countDownLatch;

        LogUtil.start();
        new Thread(() -> {
            try {
                S1.updateIDF2();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                S2.updateIDF2();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        countDownLatch.await();
        LogUtil.info("UdtIdf()完成-");
        LogUtil.end();
    }

    public void queryImageProcess() {
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIFT);
        DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        // 处理查询图像
        // 1. 读取查询图像
//		Scanner scanner = new Scanner(System.in);
//		System.out.print("输入查询图像路径：");
//		String queryImagePath = scanner.nextLine();
//		// TODO 不仅可以输入查询图像路径，还可以输入阈值
//		System.out.print("输入小阈值（浮点数）：");
//		float sigma = scanner.nextFloat();
//		System.out.print("输入大阈值（整数）：");
//		int Sigma = scanner.nextInt();

        String queryImagePath = "C:\\Users\\pzima\\Desktop\\0.jpg";
        float sigma = 0.3f;
        int Sigma = 50;
        S1.sigma = sigma;
        S1.Sigma = Sigma;
        S2.sigma = sigma;
        S2.Sigma = Sigma;
        k = 5;
        // 2.得到查询图像的关键点位置
        Mat srcImgMat = Highgui.imread(queryImagePath);
        MatOfKeyPoint srcMkp = new MatOfKeyPoint();
        fd.detect(srcImgMat, srcMkp);

        // 3.得到查询图像的特征描述符
        Mat srcDescMat = new Mat();
        de.compute(srcImgMat, srcMkp, srcDescMat);
        queryDescriptors = new Descriptor[srcDescMat.rows()];
        for (int i = 0; i < srcDescMat.rows(); i++) {
            Short[] descriptor = new Short[128];
            for (int j = 0; j < 128; j++) {
                descriptor[j] = (short) srcDescMat.get(i, j)[0];
            }
            queryDescriptors[i] = new Descriptor(i, descriptor);
        }
        // 分成两部分，模拟SECURING SIFT 的结果
        Descriptor[] encryptedQueryDescriptor1 = new Descriptor[queryDescriptors.length];
        Descriptor[] encryptedQueryDescriptor2 = new Descriptor[queryDescriptors.length];
        for (int i = 0; i < queryDescriptors.length; i++) {
            Short[][] encryptedQueryDescriptors = SiftExt_test.descriptorSplit(queryDescriptors[i].descriptor);
            encryptedQueryDescriptor1[i] = new Descriptor(i, encryptedQueryDescriptors[0]);
            encryptedQueryDescriptor2[i] = new Descriptor(i, encryptedQueryDescriptors[1]);
        }
        // TODO 改成隐私保护的方案
        S1.queryDescriptorArray = queryDescriptors;
//        S2.queryDescriptorArray = queryDescriptors;

        S1.encryptedQueryDescriptorArray = encryptedQueryDescriptor1;
        S2.encryptedQueryDescriptorArray = encryptedQueryDescriptor2;
        System.out.println("查询图像的特征数量: " + queryDescriptors.length);
    }


    public void queryDescriptorBucketAlloc() {
        // TODO 这里应该通过交互来完成
        // 这里的bucketInfo不带权重
        // L个E2LSH hash table
        queryDescriptorAllocResultMapArray = new HashMap[S1.e2lsh.indexArray.length];

        for (int i = 0; i < queryDescriptorAllocResultMapArray.length; i++) {
            queryDescriptorAllocResultMapArray[i] = new HashMap<>();
        }
        S1.e2lsh.descriptorsAlloc(queryDescriptorAllocResultMapArray, S1.queryDescriptorArray);
        // 就已经得到了这个查询向量在L个哈希表上的桶及这些桶内容
    }

    public void resultRetrieve() throws Exception {
        String imageOutPath = "C:\\Users\\pzima\\Documents\\cbir\\match_test\\";
        LogUtil.start();
        File file = new File(imageOutPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        for (int i = 0; i < resultList.size(); i++) {
            File srcFile = new File(CbirConstant.targetImagePath + resultList.get(i).imageId);// 可替换为任何路径何和文件名
            FileInputStream fileInputStream = new FileInputStream(srcFile);
            FileOutputStream fileOutputStream = new FileOutputStream(
                    new File(imageOutPath, String.valueOf(i) + ".jpg"));
            FileChannel fcin = fileInputStream.getChannel();
            FileChannel fcout = fileOutputStream.getChannel();
            fcin.transferTo(0, fcin.size(), fcout);
            fcin.close();
            fcout.close();
            fileInputStream.close();
            fileOutputStream.close();
        }
        LogUtil.info("查询结束");
        LogUtil.end();
    }

    public static Short[] string2ShortArray(String descriptor) {
        String strArr[] = descriptor.split(" ");
        Short[] array = new Short[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            array[i] = (short) Float.valueOf(strArr[i]).intValue();
        }
        return array;
    }

    public void indexSearch() throws Exception{
        LogUtil.start();
        this.queryDescriptorBucketAlloc();
        LogUtil.info("IdxSrh()完成-");
        LogUtil.end();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        S1.countDownLatch = countDownLatch;
        S2.countDownLatch = countDownLatch;

        LogUtil.start();
        new Thread(() -> {
            try {
                S1.filter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                S2.filter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        countDownLatch.await();
        // 合并L个哈希表上的分数
        for (HashMap<String, Float> subScoreMap : S1.scoreMapArray) {
            for (String imageId : subScoreMap.keySet()) {
                if (Cbir.scoreMap.containsKey(imageId)) {
                    Cbir.scoreMap.replace(imageId, subScoreMap.get(imageId) + Cbir.scoreMap.get(imageId));
                } else {
                    Cbir.scoreMap.put(imageId, subScoreMap.get(imageId));
                }
            }
        }

        for (HashMap<String, Float> subScoreMap : S2.scoreMapArray) {
            for (String imageId : subScoreMap.keySet()) {
                if (Cbir.scoreMap.containsKey(imageId)) {
                    Cbir.scoreMap.replace(imageId, subScoreMap.get(imageId) + Cbir.scoreMap.get(imageId));
                } else {
                    Cbir.scoreMap.put(imageId, subScoreMap.get(imageId));
                }
            }
        }

        // map 转为 array
        for (String imageId : Cbir.scoreMap.keySet()) {

            Cbir.scoreArray.add(new Similarity(imageId, Cbir.scoreMap.get(imageId)));
//            System.out.println(imageId + " : " + Cbir.scoreMap.get(imageId));
        }
        // 排序
        Collections.sort(Cbir.scoreArray);
    }

    public void filter() throws Exception {
        for (int i = k; i < Cbir.scoreArray.size(); i++) {
            Cbir.filteredSet.add(Cbir.scoreArray.get(i).imageId);
//            System.out.println("排除图像: " + Cbir.filterScoreArray.get(i).imageId);
        }
        System.out.println("过滤前大小: " + Cbir.scoreArray.size());
        System.out.println("过滤后大小: " + (Cbir.scoreArray.size() - Cbir.filteredSet.size()));

        LogUtil.info("Filter()完成-");
        LogUtil.end();
    }

    public void refine() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        S1.countDownLatch = countDownLatch;
        S2.countDownLatch = countDownLatch;

        LogUtil.start();
        new Thread(() -> {
            try {
                S1.exactDistanceCompute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                S2.exactDistanceCompute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        countDownLatch.await();

        countDownLatch = new CountDownLatch(2);
        S1.countDownLatch = countDownLatch;
        S2.countDownLatch = countDownLatch;

        new Thread(() -> {
            try {
                S1.exactSiftMatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                S2.exactSiftMatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        countDownLatch.await();

        // 对两个Map的结果进行排序
        HashMap<String, Integer> fullMap = new HashMap<>();
        for (String imageId : S1.refineMap.keySet()) {
            fullMap.put(imageId, S1.refineMap.get(imageId));
        }
        for (String imageId : S2.refineMap.keySet()) {
            if (fullMap.containsKey(imageId)) {
                fullMap.replace(imageId, fullMap.get(imageId) + S2.refineMap.get(imageId));
            } else {
                fullMap.put(imageId, S2.refineMap.get(imageId));
            }
        }
        resultList = new ArrayList<>();
        for (String imageId : fullMap.keySet()) {
            resultList.add(new Similarity(imageId, (float) fullMap.get(imageId)));
        }
//        System.out.println("结果集的大小: " + resultList.size());
        Collections.sort(resultList);
//        for (Similarity similarity : resultList) {
//            System.out.println(similarity.imageId + " " + similarity.similarity);
//        }


        LogUtil.info("Refine()完成-");
        LogUtil.end();
        int similarImageCount = 0;
        if (resultList.size() > 5) {
            for (int i = 0; i < 5; i++) {
                if (imageSet.contains(resultList.get(i).imageId)) {
                    similarImageCount++;
                }
            }
            System.out.println(k/ 5 + "k时相似图片比 " + similarImageCount * 1.0 / 5);
        } else {
            for (int i = 0; i < resultList.size(); i++) {
                if (imageSet.contains(resultList.get(i).imageId)) {
                    similarImageCount++;
                }
            }
            System.out.println(k/ 5 + "k时相似图片比 " + similarImageCount * 1.0 / 5);
        }
    }

    public void refresh() {
        filteredSet.clear();
        scoreMap.clear();
        resultList.clear();
        S1.refresh();
        S2.refresh();
        System.out.println("===============================");
    }

    public static void main(String[] args) throws Exception {
//        LshParam lsmParam;
//        if (1 > 0) {
//            return;
//        }
        Cbir cbir = new Cbir(2, 12, 400, 1936579, 10001, 10002);
        // 提取目标图像的SIFT特征
        cbir.featureExtract();
        // 建立索引
        cbir.indexBuild();
        // 更新索引上桶的IDF
        cbir.updateIDF();
        // 打印建立索引时的开销信息
//		cbir.printIndexingInfo();
        // 处理查询图像
        cbir.queryImageProcess();
        cbir.indexSearch();
        cbir.refine();
//        cbir.resultRetrieve();
//        for (int i = 1; i <= scoreArray.size() / 5; i++) {
//            cbir.k = 5 * i;
//            cbir.filter();
//            cbir.refine();
            cbir.resultRetrieve();
//            cbir.refresh();
//        }

    }
}
