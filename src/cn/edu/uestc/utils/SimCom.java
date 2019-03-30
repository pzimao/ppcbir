package cn.edu.uestc.utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class SimCom {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIFT);
        DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

        //读取目标图像
        File targetImages = new File("C:\\Users\\pzima\\Documents\\cbir\\target_image\\");

        String[] targetImageNames = targetImages.list();
        ArrayList<Mat> descriptorList = new ArrayList<>();

        // 结果矩阵
        int[][] resultArray = new int[targetImageNames.length][targetImageNames.length];
        for (int i = 0; i < targetImageNames.length; i++) {
            System.out.println("i=" + i);
            String xImageName = targetImages.getAbsolutePath() + "\\" + targetImageNames[i];
            Mat xImage = Highgui.imread(xImageName);

            // 2.得到查询图像的关键点位置
            MatOfKeyPoint xMkp = new MatOfKeyPoint();
            fd.detect(xImage, xMkp);

            // 3.得到查询图像的特征描述符
            Mat xDesc = new Mat();
            de.compute(xImage, xMkp, xDesc);
            descriptorList.add(xDesc);
        }

        // 进行match 操作
        for (int i = 0; i < descriptorList.size(); i++) {
            System.out.println("匹配:" + i);
            for (int j = 0; j < i; j++) {
                // 进行匹配

                try {


                    List<MatOfDMatch> matches = new LinkedList();
                    descriptorMatcher.knnMatch(descriptorList.get(i), descriptorList.get(j), matches, 2);

                    LinkedList<DMatch> goodMatchesList = new LinkedList();

                    // 对匹配结果进行筛选，依据distance进行筛选
                    matches.forEach(match -> {
                        DMatch[] dmatcharray = match.toArray();
                        DMatch m1 = dmatcharray[0];
                        DMatch m2 = dmatcharray[1];

                        if (m1.distance / m2.distance <= 0.8) {
                            //System.out.println(m1.distance + "\t" + m2.distance);
                            goodMatchesList.addLast(m1);
                        }
                    });
                    int flag = descriptorList.get(i).rows();
                    if (descriptorList.get(j).rows() > flag) {
                        flag = descriptorList.get(j).rows();
                    }


                    resultArray[j][i] = resultArray[i][j] = (int) (goodMatchesList.size() * 1.0 / flag * 255);
                } catch (Exception e) {
                    resultArray[j][i] = resultArray[i][j] = 0;
                    continue;
                }
            }
            resultArray[i][i] = 255;
        }
        // 打印结果矩阵
        System.out.println("[");
        for (int i = 0; i < resultArray.length; i++) {
            System.out.print("[");
            for (int j = 0; j < resultArray.length; j++) {
                System.out.print(resultArray[i][j]);
                if (j != resultArray.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("],");
        }
        System.out.println("]");
    }
}
