package cn.edu.uestc.cbir;

public class Similarity implements Comparable<Similarity> {
	public String imageId;
	public Float similarity;

	public Similarity(String imagePath, Float similarity) {
		super();
		this.imageId = imagePath;
		this.similarity = similarity;
	}

	@Override
	public int compareTo(Similarity o) {
		Float result = this.similarity - o.similarity;
		if (Math.abs(result) <= 0.0000000001) {
			return 0;
		}
		return result < 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "图像ID：" + imageId + " " + similarity;
	}
}
