package cn.edu.uestc.cbir;

import cn.edu.uestc.utils.DBUtil;
import cn.edu.uestc.utils.LogUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPOutputStream;

public class Server {
    public long processTime;
    public long queryTime;
    public int descriptorCount = 0;
    public int fCount = 0;
    // 由Server维护的若干个哈希表
    public E2LSH e2lsh;
    public CountDownLatch countDownLatch;
    public HashMap<String, Integer> refineMap;
    public HashMap<String, Descriptor[]> encryptedTargetImageDescriptorMap; // 保存用SECURING SIFT
    // 的方法提取出来的v1或者v2
    // TODO 这个应该通过交互来使用
    public Descriptor[] queryDescriptorArray;
    public Descriptor[] encryptedQueryDescriptorArray;
    public HashMap<String, Float>[] receivedMinDistanceMapArray;
    public HashMap<String, ArrayList<Float>>[] knownDistanceMapArray;
    public HashMap<BucketInfo, HashSet<String>>[] indexInfoMapArray;
    public HashMap<BucketInfo, HashSet<String>>[] receivedIndexInfoMapArray;
    public HashMap<String, Float>[] scoreMapArray;
    public float sigma;
    public int Sigma;
    public int tCount = 0; // 处理的图像描述符的数量
    private int queueSize = 10000;
    private ServerSocket serverSocket;
    private Short serverId;
    private int corServerPort;
    private String host;

    public Server(Short serverId, int port, int corServerPort, LshParam[] lshParamArray) {
        this.serverId = serverId;
        this.corServerPort = corServerPort;
        this.host = "127.0.0.1";
        this.encryptedTargetImageDescriptorMap = new HashMap<>();
        try {
            serverSocket = new ServerSocket();
            // 关闭serverSocket时，立即释放serverSocket绑定端口以便端口重用，默认为false
            serverSocket.setReuseAddress(true);
            // accept等待连接超时时间为1000毫秒，默认为0，永不超时
            // serverSocket.setSoTimeout(20 * 000);
            // 为所有accept方法返回的socket对象设置接收缓存区大小，单位为字节，默认值和操作系统有关
            serverSocket.setReceiveBufferSize(4 * 1024 * 1024);
            // 设置性能参数，可设置任意整数，数值越大，相应的参数重要性越高（连接时间，延迟，带宽）
            serverSocket.setPerformancePreferences(3, 4, 5);
            // 服务端绑定至端口，10为服务端连接请求队列长度
            serverSocket.bind(new InetSocketAddress(port), queueSize);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.log("Server establish error!");
        }
        // 初始化， 建立索引
        this.e2lsh = new E2LSH(lshParamArray);
    }

    public void refresh() {
        fCount = 0;
        tCount = 0;
        refineMap.clear();
    }

    public void service() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Socket socket = null;
                    ObjectInputStream is = null;
                    ObjectOutputStream os = null;
                    try {
                        // 从连接请求队列中取出一个客户连接请求，创建与客户连接的socket对象
                        // 如果队列中没有请求，accept方法就会一直等待
                        socket = serverSocket.accept();
                        socket.setSoTimeout(0);
                        is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                        os = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                        Object obj = is.readObject();
                        // 获取传来的Message对象
                        Message message = (Message) obj;
                        if (message.messageType == MessageType.LSH) {
                            ImageDescriptors imageDescriptors = (ImageDescriptors) message.messageContent;
                            // 对这个descriptorId计算一部分结果，即result2们
                            // 每个index包含K个LSH函数,一共有L个index
                            HashMap<Integer, Float[][]> result = result2ArrayArrayMapCompute(imageDescriptors);
                            os.writeObject(result);
                        } else if (message.messageType == MessageType.IDF) {
                            receivedIndexInfoMapArray = (HashMap<BucketInfo, HashSet<String>>[]) message.messageContent;
                            os.writeObject(null);
                        } else if (message.messageType == MessageType.DISTANCE) {

                            HashMap<String, HashSet<Integer>>[] wantedDistance = (HashMap<String, HashSet<Integer>>[]) message.messageContent;
                            // 估计下result的大小
                            int tCount = 0;
                            for (HashMap<String, HashSet<Integer>> map : wantedDistance) {
                                for (String imageId : map.keySet()) {
                                    tCount = tCount + map.get(imageId).size();
                                }
                            }
                            System.out.println("服务器" + serverId + ", 对方发送的向量数量: " + tCount + ", 过滤比 " + (fCount * 1.0 / (fCount + tCount)) + ", 发送数据大小（如果不压缩）是" + tCount * 1.0 / 1024 / 1024 * 256 + " MB");
                            // 这里的处理是为了统计发送和接收的数据量

                            HashMap<String, HashMap<Integer, Short[]>>[] result = distanceCompute(wantedDistance);
                            // 估计下result的大小
                            int vectorCount = 0;
                            for (HashMap<String, HashMap<Integer, Short[]>> map : result) {
                                for (String imageId : map.keySet()) {
                                    vectorCount = vectorCount + map.get(imageId).size();
                                }
                            }

                            GZIPOutputStream gzipos = null;
                            gzipos = new GZIPOutputStream(socket.getOutputStream());
                            os = new ObjectOutputStream(gzipos);
                            os.writeObject(result);
                            gzipos.finish();
                            try {
                                os.close();
                            } catch (IOException e) {
                            }
                            try {
                                socket.close();
                            } catch (IOException e) {
                            }

                        } else if (message.messageType == MessageType.MIN_DISTANCE) {
                            receivedMinDistanceMapArray = (HashMap<String, Float>[]) message.messageContent;
                            os.writeObject(null);
                        }
                        os.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            is.close();
                        } catch (Exception ex) {
                        }
                        try {
                            os.close();
                        } catch (Exception ex) {
                        }
                        try {
                            socket.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        });
        thread.setDaemon(false);
        thread.start();
    }

    public void indexBuild() {
        // 到这一步时，S1和S2都已分别拿到了目标图像的加密的特征向量
        for (String imageId : encryptedTargetImageDescriptorMap.keySet()) {
            // 索引上的：这里保存SIFT特征ID和所属的图像ID
            Integer[] descriptorIdForIndexArray = null;
            if (serverId == 1) {
                descriptorIdForIndexArray = new Integer[(encryptedTargetImageDescriptorMap.get(imageId).length + 1) / 2];
                for (int i = 0; i < descriptorIdForIndexArray.length; i++) {
                    descriptorIdForIndexArray[i] = 2 * i;
                }
            } else {
                descriptorIdForIndexArray = new Integer[(encryptedTargetImageDescriptorMap.get(imageId).length) / 2];
                for (int i = 0; i < descriptorIdForIndexArray.length; i++) {
                    descriptorIdForIndexArray[i] = 2 * i + 1;
                }
            }
//			System.out.println("索引上的向量数量: " + descriptorIdForIndexArray.length);

            // 相当于对每幅目标图像都需要一次交互
            // 调用安全的计算协议，计算出SIFT特征向量落到的桶，每个特征向量都将落到L个桶
            tCount += descriptorIdForIndexArray.length;
//            HashMap<Integer, BucketInfo[]> bucketArrayMap = secureCompute(imageId, descriptorIdForIndexArray);
//            e2lsh.add(imageId, bucketArrayMap);
            secureAdd(imageId, descriptorIdForIndexArray);
        }
//        System.out.println("服务器" + serverId + "对" + tCount + "个向量建立了索引 耗时： " + (System.currentTimeMillis() - startTime));
        System.out.println("服务器" + serverId + "对" + tCount + "个向量建立了索引 计算耗时： " + this.processTime);
        System.out.println("服务器" + serverId + e2lsh);
        LogUtil.serverLog(serverId, "索引建立完成");
        this.processTime = 0;
//        System.out.println(e2lsh);
        countDownLatch.countDown();
    }

    // 安全的桶号计算协议
    // 这里传进来的descriptorIdForIndex就只有一半的descriptor
    public HashMap<Integer, BucketInfo[]> secureCompute(String imageId, Integer[] descriptorIdForIndexArray) {
        // 首先通过交互算出各个位置的result2
        HashMap<Integer, Float[][]> result2ArrayArrayMap = secureCompute1(imageId, descriptorIdForIndexArray);
        // 然后算出真实的桶号
        HashMap<Integer, BucketInfo[]> bucketArrayMap = new HashMap<>();
        for (Integer descriptorId : descriptorIdForIndexArray) {

            BucketInfo[] bucketInfos = e2lsh.compute(encryptedTargetImageDescriptorMap.get(imageId)[descriptorId].descriptor,
                    result2ArrayArrayMap.get(descriptorId), serverId);
            bucketArrayMap.put(descriptorId, bucketInfos);
        }
        return bucketArrayMap;
    }

    public void secureAdd(String imageId, Integer[] descriptorIdForIndexArray) {
        // 首先通过交互算出各个位置的result2
        HashMap<Integer, Float[][]> result2ArrayArrayMap = secureCompute1(imageId, descriptorIdForIndexArray);
        // 然后算出真实的桶号
        long startTime = System.currentTimeMillis();
        for (Integer descriptorId : descriptorIdForIndexArray) {

            BucketInfo[] bucketInfos = e2lsh.compute(encryptedTargetImageDescriptorMap.get(imageId)[descriptorId].descriptor,
                    result2ArrayArrayMap.get(descriptorId), serverId);
            e2lsh.add(imageId, descriptorId, bucketInfos);
        }
        this.processTime += (System.currentTimeMillis() - startTime);
    }

    @SuppressWarnings("unchecked")
    public HashMap<Integer, Float[][]> secureCompute1(String imageId, Integer[] descriptorIdForIndexArray) {
        // 安全的LSH计算协议
        Message message = new Message(MessageType.LSH, new ImageDescriptors(imageId, descriptorIdForIndexArray));
        HashMap<Integer, Float[][]> result2sArrayMap = (HashMap<Integer, Float[][]>) new Client(host, corServerPort).inquery(message);
        return result2sArrayMap;
    }

    public HashMap<Integer, Float[][]> result2ArrayArrayMapCompute(ImageDescriptors imageDescriptors) {
        long startTime = System.currentTimeMillis();
        HashMap<Integer, Float[][]> result2ArrayArrayMap = new HashMap<>();
        for (Integer descriptorId : imageDescriptors.descriptorIdArray) {

            Short[] encryptedDescriptor = encryptedTargetImageDescriptorMap.get(imageDescriptors.imageId)[descriptorId].descriptor;
            Float[][] result2ArrayArray = new Float[e2lsh.indexArray.length][];
            for (int i = 0; i < e2lsh.indexArray.length; i++) {
                // 在每个index上都进行计算
                Index index = e2lsh.indexArray[i];
                result2ArrayArray[i] = index.computeResult2(encryptedDescriptor);
            }
            result2ArrayArrayMap.put(descriptorId, result2ArrayArray);
        }
        this.processTime += (System.currentTimeMillis() - startTime);
        return result2ArrayArrayMap;
    }


    public void filter() {
        // 依次计算分数

        long startTimeForQuery = System.currentTimeMillis();
        scoreMapArray = new HashMap[this.e2lsh.indexArray.length];
        for (int i = 0; i < scoreMapArray.length; i++) {
            scoreMapArray[i] = new HashMap<>();
        }
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < Cbir.queryDescriptorAllocResultMapArray.length; i++) {
//            System.out.println("处理哈希表+1");
            HashMap<BucketInfo, HashSet<Integer>> queryDescriptorAllocResultMap = Cbir.queryDescriptorAllocResultMapArray[i];
            // 对匹配到的每一个桶，计算...
            for (BucketInfo bucketInfo : queryDescriptorAllocResultMap.keySet()) {
                // 找到这个bucket的权重
                if (!e2lsh.idfMapArray[i].containsKey(bucketInfo)) {
                    continue;
                }
                Float bucketIdf = e2lsh.idfMapArray[i].get(bucketInfo);

                if (bucketIdf < 0.00001f) {
                    continue;
                }
                // 查询图像在这个桶下的词频
                int queryImageTf = queryDescriptorAllocResultMap.get(bucketInfo).size();
                // 对这个桶下的其他图像，计算分数
                // 首先得到这个桶内容
                HashMap<String, HashSet<Integer>> bucketContentMap = e2lsh.getBucketContent(i, bucketInfo);
                if (bucketContentMap == null) {
                    continue;
                }
                for (String imageId : bucketContentMap.keySet()) {
                    // 分数 = 目标图像词频 * 查询图像词频 * 桶权重
                    Float score = bucketContentMap.get(imageId).size() * queryImageTf * bucketIdf;
                    if (scoreMapArray[i].containsKey(imageId)) {
                        scoreMapArray[i].replace(imageId, scoreMapArray[i].get(imageId) + score);
                    } else {
                        scoreMapArray[i].put(imageId, score);
                    }
                }
            }
        }
        this.queryTime = System.currentTimeMillis() - startTimeForQuery;
        System.out.println(this.serverId + "过滤时间: " + (System.currentTimeMillis() - startTime));
        countDownLatch.countDown();
    }

    public void featureExtract() throws Exception {
        // 本来应该S1和S2交互来提取
        // 因为工作SECURING SIFT中已经实现
        // 为了简化程序，事先提取好，存储到数据库中，用到的时候直接读取数据库。
        Connection connection = DBUtil.getCon();
        // 先检索图像id
        // todo 实验测试修改
        String sql0 = "select distinct image_id from sift_descriptor limit 1000";
        PreparedStatement preparedStatement = connection.prepareStatement(sql0);
        ResultSet resultSet0 = preparedStatement.executeQuery();
        while (resultSet0.next()) {
            String imageId = resultSet0.getString(1);
            // 再按图像id检索特征
            String sql = "select descriptor_id, descriptor_v" + serverId + " from sift_descriptor where image_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, imageId);
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Descriptor> encryptedDescriptorList = new ArrayList<>();
            while (resultSet.next()) {
                int descriptorId = resultSet.getInt(1);
                Short[] encryptedDescriptor = Cbir.string2ShortArray(resultSet.getString(2));
                encryptedDescriptorList.add(new Descriptor(descriptorId, encryptedDescriptor));
                descriptorCount++;
            }
            // 把特征描述符列表转为数组
            Descriptor[] descriptors = encryptedDescriptorList.toArray(new Descriptor[encryptedDescriptorList.size()]);
            encryptedTargetImageDescriptorMap.put(imageId, descriptors);
            // todo 实验测试修改
//            if (descriptorCount > 300000) {
//                break;
//            }
        }
    }

    public HashMap<String, HashMap<Integer, Short[]>>[] distanceCompute(HashMap<String, HashSet<Integer>>[] receivedWantedDistanceIdMapArray) {

        HashMap<String, HashMap<Integer, Short[]>>[] responseDistanceMapArray = new HashMap[receivedWantedDistanceIdMapArray.length];
        long startTime = System.currentTimeMillis();
        for (int descriptorIndex = 0; descriptorIndex < receivedWantedDistanceIdMapArray.length; descriptorIndex++) {
            HashMap<String, HashMap<Integer, Short[]>> responseDistanceMap = new HashMap();
            for (String imageId : receivedWantedDistanceIdMapArray[descriptorIndex].keySet()) {
                HashSet<Integer> descriptorSet = receivedWantedDistanceIdMapArray[descriptorIndex].get(imageId);
                HashMap<Integer, Short[]> tempResponseMap = new HashMap<>();
                for (Integer descriptorId : descriptorSet) {
                    // 计算出一个中间结果，然后包装好，响应回去
                    Short[] encryptedQueryDescriptor = encryptedQueryDescriptorArray[descriptorIndex].descriptor;
                    Short[] targetDescriptor = encryptedTargetImageDescriptorMap.get(imageId)[descriptorId].descriptor;
                    Short[] tempResult = new Short[128];
                    for (int j = 0; j < 128; j++) {
                        tempResult[j] = (short) (encryptedQueryDescriptor[j] - targetDescriptor[j]);
                    }
                    tempResponseMap.put(descriptorId, tempResult);
                }
                responseDistanceMap.put(imageId, tempResponseMap);
            }
            responseDistanceMapArray[descriptorIndex] = responseDistanceMap;
        }
        this.processTime += (System.currentTimeMillis() - startTime);
        return responseDistanceMapArray;
    }

    public void exactDistanceCompute() {
        // 1. 确定要计算哪些点之间的距离
        // 2. 询问对方
        // 3. 等待，收到对方计算的结果
        // 4. 计算真实的距离
        // 5. 把最近的距离交给对方
        // 6. 进行匹配，得到两个Map

        // 询问另外一台服务器
        // 将得到计算的距离的中间结果
        // 精确匹配是一个点一个点进行的
        // 所以数组的维数等于查询向量的数量
        fCount = 0;
        long startTimeForQuery = System.currentTimeMillis();
        HashMap<String, HashSet<Integer>>[] requiredDistanceIdMapArray = new HashMap[encryptedQueryDescriptorArray.length];
        for (int i = 0; i < requiredDistanceIdMapArray.length; i++) {
            requiredDistanceIdMapArray[i] = new HashMap<>();
        }

        // 对匹配到的每个桶，进行遍历
        for (int indexIndex = 0; indexIndex < Cbir.queryDescriptorAllocResultMapArray.length; indexIndex++) {
            for (BucketInfo bucketInfo : Cbir.queryDescriptorAllocResultMapArray[indexIndex].keySet()) {
                HashMap<String, HashSet<Integer>> bucketContent = e2lsh.getBucketContent(indexIndex, bucketInfo);
                if (bucketContent == null) {
                    continue;
                }
                for (String imageId : bucketContent.keySet()) {
                    if (Cbir.filteredSet.contains(imageId)) {
                        fCount += bucketContent.get(imageId).size();
                        continue;
                    }
                    HashSet<Integer> targetDescriptorIdSet = bucketContent.get(imageId);
                    for (Integer queryDescriptorId : Cbir.queryDescriptorAllocResultMapArray[indexIndex].get(bucketInfo)) {
                        if (requiredDistanceIdMapArray[queryDescriptorId].containsKey(imageId)) {
                            requiredDistanceIdMapArray[queryDescriptorId].get(imageId).addAll(targetDescriptorIdSet);
                        } else {
                            HashSet<Integer> set = new HashSet<>();
                            set.addAll(targetDescriptorIdSet);
                            requiredDistanceIdMapArray[queryDescriptorId].put(imageId, set);
                        }
                    }
                }
            }
        }

        HashMap<String, HashMap<Integer, Short[]>>[] receivedIntermediateDistanceMapArray = new Client(host, corServerPort)
                .inquery5(new Message(MessageType.DISTANCE, requiredDistanceIdMapArray));


        // 得把最近的两个各个位置上最近的距离告诉对方
        // 只发送最近的那1个就可以了
        // 要分享的1NN距离信息

        // 自己能知道的查询向量和图像向量的距离的map
        long startTime = System.currentTimeMillis();
        knownDistanceMapArray = new HashMap[encryptedQueryDescriptorArray.length];
        HashMap<String, Float>[] minDistanceMapArray = new HashMap[knownDistanceMapArray.length];
        // 把这个距离map计算出来
        for (int i = 0; i < receivedIntermediateDistanceMapArray.length; i++) {
            HashMap<String, HashMap<Integer, Short[]>> receivedDistanceMap = receivedIntermediateDistanceMapArray[i];
            knownDistanceMapArray[i] = new HashMap<String, ArrayList<Float>>();
            minDistanceMapArray[i] = new HashMap<String, Float>();
            for (String imageId : receivedDistanceMap.keySet()) {
                ArrayList<Float> distanceList = new ArrayList<>(receivedDistanceMap.get(imageId).size());
                for (Integer descriptorId : receivedDistanceMap.get(imageId).keySet()) {
                    // 计算真实的距离
                    // 并把距离保存到knownDistanceMapArray

                    // 计算自己这边的一半的距离
                    Short[] intermediateResult1 = Cbir.vectorSub(encryptedQueryDescriptorArray[i].descriptor, encryptedTargetImageDescriptorMap.get(imageId)[descriptorId].descriptor);

                    // 拿到收到的另外一半的距离
                    Short[] receivedIntermediateResult = receivedDistanceMap.get(imageId).get(descriptorId);

                    // 拿到两半结果后，就可以计算得到真正的向量
                    // 计算这个向量的长度，就是查询向量和目标图像的向量的距离
                    Short[] resultVector = Cbir.vectorSub(intermediateResult1, receivedIntermediateResult);
                    Float distance = Cbir.vectorLength(resultVector);
                    distanceList.add(distance);
                }
                Collections.sort(distanceList);

                minDistanceMapArray[i].put(imageId, distanceList.get(0));
                knownDistanceMapArray[i].put(imageId, distanceList);
            }
        }

        this.processTime += (System.currentTimeMillis() - startTime);
        new Client(host, corServerPort).inquery(new Message(MessageType.MIN_DISTANCE, minDistanceMapArray));
        System.out.println("服务器" + serverId + "过滤了" + fCount + "个");
        this.queryTime += (System.currentTimeMillis() - startTimeForQuery);
        countDownLatch.countDown();
    }

    public void exactSiftMatch() {
        refineMap = new HashMap<>();
        // 进行特征匹配
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < knownDistanceMapArray.length; i++) {
            HashMap<String, ArrayList<Float>> knownDistanceMap = knownDistanceMapArray[i];
            for (String imageId : knownDistanceMap.keySet()) {

                // 拿到这个图的距离们
                ArrayList<Float> distanceList = knownDistanceMap.get(imageId);
                // 如果对方不含有相关的距离或者对方的最近距离大于自己的，那么匹配就是自己完成的
                if (!receivedMinDistanceMapArray[i].containsKey(imageId) || receivedMinDistanceMapArray[i].get(imageId) > distanceList.get(0)) {
                    ArrayList<Float> compareDistanceList = new ArrayList<>();
                    compareDistanceList.add(distanceList.get(0));
                    if (distanceList.size() > 1) {
                        compareDistanceList.add(distanceList.get(1));
                    }
                    if (receivedMinDistanceMapArray[i].containsKey(imageId)) {
                        compareDistanceList.add(receivedMinDistanceMapArray[i].get(imageId));
                    }
                    Collections.sort(compareDistanceList);

                    boolean matchFlag = false;
                    if (compareDistanceList.size() == 1) {
                        if (compareDistanceList.get(0) <= Sigma) {
                            matchFlag = true;
                        }
                    } else {
                        if (compareDistanceList.get(0) / compareDistanceList.get(1) <= sigma) {
                            matchFlag = true;
                        }
                    }
                    if (matchFlag) {
                        // 匹配成功
                        if (!refineMap.containsKey(imageId)) {
                            refineMap.put(imageId, 1);
                        } else {
                            refineMap.replace(imageId, refineMap.get(imageId) + 1);
                        }
                    }
                }
            }
        }

        // 测试： refineMap 的内容
//        for (String imageId : refineMap.keySet()) {
//            System.out.println(imageId + "匹配的点数量:" + refineMap.get(imageId));
//        }
        this.processTime += (System.currentTimeMillis() - startTime);
        countDownLatch.countDown();
    }

    public void updateIDF1() {
        // 统计自己这方索引上的桶和图像的对应HashMap<BucketInfo, HashSet<String>>[]
        indexInfoMapArray = new HashMap[e2lsh.indexArray.length];
        for (int i = 0; i < indexInfoMapArray.length; i++) {
            indexInfoMapArray[i] = new HashMap<>();
        }
        for (int i = 0; i < e2lsh.indexArray.length; i++) {
            Index index = e2lsh.indexArray[i];
            HashMap<BucketInfo, HashSet<String>> indexInfoMap = indexInfoMapArray[i];
            for (BucketInfo bucketInfo : index.bucketMap.keySet()) {
                HashSet<String> tempSet = new HashSet<>();
                tempSet.addAll(index.bucketMap.get(bucketInfo).keySet());
//                System.out.println("set大小: " + tempSet.size());
                indexInfoMap.put(bucketInfo, tempSet);
            }
        }

        new Client(host, corServerPort).inquery(new Message(MessageType.IDF, indexInfoMapArray));
        countDownLatch.countDown();
    }

    public void updateIDF2() {
        // 统计自己这方索引上的桶和图像的对应HashMap<BucketInfo, HashSet<String>>[]
        for (int i = 0; i < indexInfoMapArray.length; i++) {
            HashMap<BucketInfo, HashSet<String>> indexInfoMap = indexInfoMapArray[i];
            HashMap<BucketInfo, HashSet<String>> receivedIndexInfoMap = receivedIndexInfoMapArray[i];
            for (BucketInfo bucketInfo : indexInfoMap.keySet()) {
                // 检查对方是否有这个桶
                if (!receivedIndexInfoMap.containsKey(bucketInfo)) {
                    bucketInfo.idf = (float) Math.log10(this.encryptedTargetImageDescriptorMap.size() * 1.0 / indexInfoMap.get(bucketInfo).size());
                } else {
                    // 计算这个桶下的图像数量
                    HashSet<String> tempSet = new HashSet<>();
                    tempSet.addAll(indexInfoMap.get(bucketInfo));
                    tempSet.addAll(receivedIndexInfoMap.get(bucketInfo));
                    bucketInfo.idf = (float) Math.log10(this.encryptedTargetImageDescriptorMap.size() * 1.0 / tempSet.size());
                }
                e2lsh.idfMapArray[i].put(bucketInfo, bucketInfo.idf);
            }
        }
//		for (int i=0;i< indexInfoMapArray.length;i++) {
//			HashMap<BucketInfo, HashSet<String>> indexInfoMap = indexInfoMapArray[i];
//			for (BucketInfo bucketInfo : indexInfoMap.keySet()) {
//				System.out.println(bucketInfo.id1 + "-" + bucketInfo.id2 + "-" + bucketInfo.idf);
//			}
//		}
        countDownLatch.countDown();
    }
}