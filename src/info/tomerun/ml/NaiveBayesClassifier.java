package info.tomerun.ml;

import info.tomerun.ml.util.MultiSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaiveBayesClassifier<T extends FeatureSet> {

	private double pseudocount;
	private ArrayList<Feature> prob = new ArrayList<Feature>();
	private int[] histLabel;
	private int trainDataSize;
	private int featureSize;
	private int labelSize;
	private double[] weight;

	public NaiveBayesClassifier() {
		pseudocount = 1;
	}

	public NaiveBayesClassifier(double pseudocount) {
		this.pseudocount = pseudocount;
	}

	public void train(List<T> dataSet) {
		this.trainDataSize = dataSet.size();
		this.featureSize = dataSet.get(0).featureSize();
		if (this.weight == null) {
			this.weight = new double[this.featureSize];
			Arrays.fill(this.weight, 1);
		}
		this.labelSize = dataSet.get(0).labelSize();
		this.histLabel = new int[this.labelSize];
		for (T data : dataSet) {
			++this.histLabel[data.label()];
		}
		for (int i = 0; i < this.featureSize; ++i) {
			Feature f = new Feature(i);
			f.train(dataSet);
			this.prob.add(f);
		}
	}

	public void setWeight(double[] w) {
		this.weight = w.clone();
	}

	public int classify(T testData) {
		double maxP = 0;
		int maxC = 0;
		for (int i = 0; i < this.labelSize; ++i) {
			double p = 1.0 * this.histLabel[i] / this.trainDataSize;
			for (int j = 0; j < this.featureSize; ++j) {
				p *= (this.prob.get(j).getCount(testData.feature(j), i) + this.pseudocount) / this.histLabel[i] * weight[j];
			}
			if (p > maxP) {
				maxP = p;
				maxC = i;
			}
		}
		return maxC;
	}

	private class Feature {

		private int featureId;
		private ArrayList<MultiSet<Integer>> count = new ArrayList<MultiSet<Integer>>();

		Feature(int fid) {
			this.featureId = fid;
			for (int i = 0; i < labelSize; ++i) {
				count.add(new MultiSet<Integer>());
			}
		}

		void train(List<T> dataSet) {
			for (T data : dataSet) {
				int fv = data.feature(this.featureId);
				this.count.get(data.label()).add(fv);
			}
		}

		int getCount(int featureValue, int label) {
			return count.get(label).get(featureValue);
		}

	}

}
