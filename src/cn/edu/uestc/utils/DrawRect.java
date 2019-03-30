package cn.edu.uestc.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DrawRect {

    /**
     * @param matrix  矩阵
     * @param filedir 文件路径。如,d:\\test.jpg
     * @throws IOException
     */
    public static void createMatrixImage(int[][] matrix, String filedir) throws IOException {
        int cx = matrix.length;
        int cy = matrix[0].length;
        //填充矩形高宽
        int cz = 10;
        //生成图的宽度
        int width = cx * cz;
        //生成图的高度
        int height = cy * cz;

        OutputStream output = new FileOutputStream(new File(filedir));
        BufferedImage bufImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D gs = bufImg.createGraphics();
        gs.setBackground(Color.WHITE);
        gs.clearRect(0, 0, width, height);
        gs.setColor(Color.BLACK);
        gs.setColor(Color.GRAY);
        for (int i = 0; i < cx; i++) {
            for (int j = 0; j < cy; j++) {
                //1绘制填充黑矩形
                if (matrix[j][i] == 1) {
                    gs.drawRect(i * cz, j * cz, cz, cz);
                    gs.fillRect(i * cz, j * cz, cz, cz);
                }
            }
        }
        gs.dispose();
        bufImg.flush();
        //输出文件
        ImageIO.write(bufImg, "jpeg", output);

    }

    public static void main(String[] args) throws Exception {
        //测试
        int[][] matrix = {
                {0, 1, 1, 0, 1, 1},
                {0, 0, 1, 0, 1, 1},
                {0, 1, 0, 0, 0, 1},
                {1, 0, 1, 1, 1, 0},
                {1, 0, 0, 1, 0, 1},
                {0, 0, 1, 0, 1, 1}};

        DrawRect.createMatrixImage(matrix, "d:\\test.jpg");
    }

}