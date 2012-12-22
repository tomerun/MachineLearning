package info.tomerun.ml.encoding;

import info.tomerun.ml.FeatureSet;

public class Sentence implements FeatureSet {

	private int label;
	private int[] ratio;

	Sentence(byte[] bytes, int encodingLabel) {
		this.label = encodingLabel;
		this.ratio = new int[256];
		for (int i = 0; i < bytes.length; ++i) {
			this.ratio[bytes[i] & 255] += 1;
		}
		for (int i = 0; i < 256; ++i) {
			this.ratio[i] = (int) Math.ceil(1.0 * this.ratio[i] / bytes.length * 10);
		}
	}

	@Override
	public int featureSize() {
		return 256;
	}

	@Override
	public int feature(int id) {
		return this.ratio[id];
	}

	@Override
	public int labelSize() {
		return 4;
	}

	@Override
	public int label() {
		return this.label;
	}

}
