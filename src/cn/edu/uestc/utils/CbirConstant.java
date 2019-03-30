package cn.edu.uestc.utils;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;

public class CbirConstant {
    public static String targetImagePath = "C:\\Users\\pzima\\Documents\\cbir\\target_image\\";
    public static String queryImagePath = "C:\\Users\\pzima\\Documents\\cbir\\query_image\\0.jpg";
    public static String resultImagePath = "C:\\Users\\pzima\\Documents\\cbir\\result_image\\";

    public static int descriptorType = FeatureDetector.SIFT;
    public static int extractorType = DescriptorExtractor.SIFT;
    public static int d = 128;
    public static int K = 1; // 哈希函数数量
    public static int L = 1; // LSH哈希表数量
    public static int w = 60; // LSH桶宽
}
