package info.tomerun.ml;

/**
 * 各素性が整数値を持つテストデータ1つを表す。
 */
public interface FeatureSet {
	
	/**
	 * @return count of feature
	 */
	public int featureSize();

	/**
	 * @param id index of feature, must be 0 ~ featureSize-1 (inclusive)
	 */
	public int feature(int id);

	/**
	 * @return count of label
	 */
	public int labelSize();

	/**
	 * @return label of this instance, 0 ~ labelSize-1 (inclusive)
	 */
	public int label();

	public static final int NA = Integer.MIN_VALUE;
}
