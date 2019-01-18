package cn.edu.uestc.utils;

import javax.swing.*;

public class DisplayImageIcon extends JFrame {

	private JLabel lblImg;
	private Icon icon;

	public DisplayImageIcon() {

		setTitle("swing显示图片");
		setSize(200, 140);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		lblImg = new JLabel();
		add(lblImg);
		setVisible(true);

		try {
			icon = new ImageIcon("D:\\照片\\IMG_20181125_101721.jpg");
		} catch (Exception e) {
			e.printStackTrace();
		}
		lblImg.setIcon(icon);

	}

	public static void main(String[] args) {
		new DisplayImageIcon();
	}
}