package cn.edu.uestc.utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class SiftExt_test {


	public static void main(String[] args) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		int count = 0;
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
		MatOfKeyPoint targetMkp = new MatOfKeyPoint();
		String targetImageFolder = "C:\\Users\\pzima\\Documents\\cbir\\target_image\\";
		String targetImages[] = new File(targetImageFolder).list();
		String sql = "INSERT INTO `sift_descriptor` (`image_id`, `descriptor_id`, `descriptor`, `descriptor_v1`, `descriptor_v2`) VALUES (?, ?, ?, ?, ?)";
		Connection connection = DBUtil.getCon();
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		int i = 0;
		for (String targetImageName : targetImages) {
//			System.out.println("提取特征...");
			Mat targetImage = Highgui.imread(targetImageFolder + targetImageName);
			featureDetector.detect(targetImage, targetMkp);
			count += targetMkp.rows();
			i++;
			if (i % 50 == 0) {
				System.out.println(count);
			}

			Mat targetDesc = new Mat();
			descriptorExtractor.compute(targetImage, targetMkp, targetDesc);



			// 把这个描述符分成两部分
			for (int j = 0; j < targetDesc.rows(); j++) {
				Short[] descriptor = new Short[targetDesc.cols()];
				for (int k = 0; k < targetDesc.cols(); k++) {
					descriptor[k] = (short) targetDesc.get(j, k)[0];
				}
				System.out.println("拆分...");
				Short[][] descriptors = descriptorSplit(descriptor);
				// 把这三个向量加入到数据库中
				preparedStatement.setString(1, targetImageName);
				preparedStatement.setInt(2, j);
				preparedStatement.setString(3, shortArray2String(descriptor));
				preparedStatement.setString(4, shortArray2String(descriptors[0]));
				preparedStatement.setString(5, shortArray2String(descriptors[1]));
				preparedStatement.addBatch();
			}
			System.out.println("执行插入...");
			if (++i % 20 == 0) {
				preparedStatement.executeBatch();
			}
		}
		System.out.println(count);

		preparedStatement.executeBatch();
	}

	public static Short[][] descriptorSplit(Short[] descriptor) {
		Short[] v1 = new Short[descriptor.length];
		Short[] v2 = new Short[descriptor.length];
		Random random = new Random();
		for (int i = 0; i < descriptor.length; i++) {
			short gap = (short) (255 - descriptor[i]);
			v2[i] = (short) random.nextInt(gap);
			v1[i] = (short) (descriptor[i] + v2[i]);
		}
		return new Short[][] { v1, v2 };
	}

	public static String shortArray2String(Short[] array) {
		String result = "";
		for (Short i : array) {
			result = result + String.valueOf(i) + " ";
		}
		return result;
	}

	public static Short[] string2ShortArray(String descriptor) {
		String strArr[] = descriptor.split(" ");
		Short[] array = new Short[strArr.length];
		for (int i = 0; i < strArr.length; i++) {
			array[i] = (short) Double.valueOf(strArr[i]).intValue();
		}
		return array;
	}
}
