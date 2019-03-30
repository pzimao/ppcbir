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
    public static HashMap<BucketInfo, HashSet<Integer>>[] queryDescriptorAllocResultMapArray; // 这个东西用来记录查询图像向量在L个哈希表上的分配情况
    public LshParam[] lshParamArray;
    public Descriptor[] queryDescriptors;
    public int k; // The top-k images are returned.
    public HashSet<String> imageSet = new HashSet<>();
    public ArrayList<Similarity> resultList; // 图像检索的最终排序

    public String printInfo = "";

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
            lshParams[i].K = K;
        }
        lshParamArray = lshParams;
        S1 = new Server((short) 1, curPort1, curPort2, lshParamArray);
        S2 = new Server((short) 2, curPort2, curPort1, lshParamArray);
        S1.service();
        S2.service();
        // 加载OPENCV库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        imageSet.add("all_souls_000000.jpg");
        imageSet.add("all_souls_000001.jpg");
        imageSet.add("all_souls_000002.jpg");
        imageSet.add("all_souls_000005.jpg");
        imageSet.add("all_souls_000006.jpg");
        imageSet.add("all_souls_000008.jpg");
        imageSet.add("all_souls_000013.jpg");
        imageSet.add("all_souls_000014.jpg");
        imageSet.add("all_souls_000015.jpg");
        imageSet.add("all_souls_000019.jpg");
        imageSet.add("all_souls_000021.jpg");
        imageSet.add("all_souls_000022.jpg");
        imageSet.add("all_souls_000026.jpg");
        imageSet.add("all_souls_000035.jpg");
        imageSet.add("all_souls_000040.jpg");
        imageSet.add("all_souls_000041.jpg");
        imageSet.add("all_souls_000045.jpg");
        imageSet.add("all_souls_000048.jpg");
        imageSet.add("all_souls_000051.jpg");
        imageSet.add("all_souls_000053.jpg");
        imageSet.add("all_souls_000054.jpg");
        imageSet.add("all_souls_000055.jpg");
        imageSet.add("all_souls_000059.jpg");
        imageSet.add("all_souls_000063.jpg");
        imageSet.add("all_souls_000064.jpg");
        imageSet.add("all_souls_000065.jpg");
        imageSet.add("all_souls_000066.jpg");
        imageSet.add("all_souls_000068.jpg");
        imageSet.add("all_souls_000072.jpg");
        imageSet.add("all_souls_000073.jpg");
        imageSet.add("all_souls_000085.jpg");
        imageSet.add("all_souls_000087.jpg");
        imageSet.add("all_souls_000090.jpg");
        imageSet.add("all_souls_000091.jpg");
        imageSet.add("all_souls_000093.jpg");
        imageSet.add("all_souls_000103.jpg");
        imageSet.add("all_souls_000105.jpg");
        imageSet.add("all_souls_000107.jpg");
        imageSet.add("all_souls_000110.jpg");
        imageSet.add("all_souls_000119.jpg");
        imageSet.add("all_souls_000126.jpg");
        imageSet.add("all_souls_000132.jpg");
        imageSet.add("all_souls_000133.jpg");
        imageSet.add("all_souls_000134.jpg");
        imageSet.add("all_souls_000136.jpg");
        imageSet.add("all_souls_000140.jpg");
        imageSet.add("all_souls_000143.jpg");
        imageSet.add("all_souls_000145.jpg");
        imageSet.add("all_souls_000146.jpg");
        imageSet.add("all_souls_000148.jpg");
        imageSet.add("all_souls_000150.jpg");
        imageSet.add("all_souls_000152.jpg");
        imageSet.add("all_souls_000153.jpg");
        imageSet.add("all_souls_000157.jpg");
        imageSet.add("all_souls_000159.jpg");
        imageSet.add("all_souls_000161.jpg");
        imageSet.add("all_souls_000167.jpg");
        imageSet.add("all_souls_000174.jpg");
        imageSet.add("all_souls_000175.jpg");
        imageSet.add("all_souls_000183.jpg");
        imageSet.add("all_souls_000184.jpg");
        imageSet.add("all_souls_000186.jpg");
        imageSet.add("all_souls_000188.jpg");
        imageSet.add("all_souls_000197.jpg");
        imageSet.add("all_souls_000205.jpg");
        imageSet.add("all_souls_000206.jpg");
        imageSet.add("all_souls_000209.jpg");
        imageSet.add("all_souls_000210.jpg");
        imageSet.add("all_souls_000214.jpg");
        imageSet.add("all_souls_000220.jpg");
    }

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

    public static Short[] string2ShortArray(String descriptor) {
        String strArr[] = descriptor.split(" ");
        Short[] array = new Short[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            array[i] = (short) Float.valueOf(strArr[i]).intValue();
        }
        return array;
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
//        cbir.k = 5;
        cbir.queryImageProcess();
        cbir.indexSearch();
//        cbir.filter();
        cbir.refineForPr();
//        cbir.resultRetrieve();
//        for (int i = 1; i <= scoreArray.size() / 5; i++) {
//            cbir.k = 5 * i;
//            cbir.filter();
//            cbir.refine();
//            cbir.resultRetrieve();
//            cbir.refresh();
//        }

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
//		System.out.print("输入小阈值（浮点数）：");
//		float sigma = scanner.nextFloat();
//		System.out.print("输入大阈值（整数）：");
//		int Sigma = scanner.nextInt();

        String queryImagePath = "C:\\Users\\pzima\\Desktop\\460.jpg";
        float sigma = 0.6f;
        int Sigma = 100;
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
        S1.queryDescriptorArray = queryDescriptors;
//        S2.queryDescriptorArray = queryDescriptors;

        S1.encryptedQueryDescriptorArray = encryptedQueryDescriptor1;
        S2.encryptedQueryDescriptorArray = encryptedQueryDescriptor2;
        System.out.println("查询图像的特征数量: " + queryDescriptors.length);
    }

    public void queryDescriptorBucketAlloc() {
        // 这里的bucketInfo不带权重
        // L个E2LSH hash table
        queryDescriptorAllocResultMapArray = new HashMap[S1.e2lsh.indexArray.length];

        for (int i = 0; i < queryDescriptorAllocResultMapArray.length; i++) {
            queryDescriptorAllocResultMapArray[i] = new HashMap<>();
        }
        long startTime = System.currentTimeMillis();
        S1.e2lsh.descriptorsAlloc(queryDescriptorAllocResultMapArray, S1.queryDescriptorArray);
        S1.processTime = (System.currentTimeMillis() - startTime);
        S2.processTime = (System.currentTimeMillis() - startTime);
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

    public void indexSearch() throws Exception {
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
        for (int i = 20 * k; i < Cbir.scoreArray.size(); i++) {
            System.out.println(Cbir.scoreArray.get(i).similarity);
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
            System.out.println(k / 5 + "k时相似图片比 " + similarImageCount * 1.0 / 5);
        } else {
            for (int i = 0; i < resultList.size(); i++) {
                if (imageSet.contains(resultList.get(i).imageId)) {
                    similarImageCount++;
                }
            }
            System.out.println(k / 5 + "k时相似图片比 " + similarImageCount * 1.0 / 5);
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

    public void refineForPr() throws Exception {
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
        for (int i = 0; i < resultList.size(); i++) {
            Similarity similarity = resultList.get(i);
            if (imageSet.contains(similarity.imageId)) {
                similarImageCount++;
            }
            System.out.println(similarImageCount * 1.0 / imageSet.size() + "\t" + similarImageCount * 1.0 / (i + 1)); // 查全率 + 查准率
        }
        System.out.println("S1 查询时间: " + S1.queryTime);
        System.out.println("S2 查询时间: " + S2.queryTime);
        System.out.println("S1 查询计算时间: " + S1.processTime);
        System.out.println("S2 查询计算时间: " + S2.processTime);
    }
}
