package info.tomerun.ml;
import info.tomerun.ml.util.MultiSet;

import java.util.ArrayList;
import java.util.Random;

public class RandomForest<T extends FeatureSet> {

	ArrayList<DecisionTree<T>> trees;
	int treeCount;
	int featureCount;
	int nMin;
	Random rand = new Random();

	RandomForest() {
		this.treeCount = 100;
		this.trees = new ArrayList<DecisionTree<T>>(100);
		this.featureCount = 3;
		this.nMin = 1;
	}

	RandomForest(int treeC, int featureC, int nMin) {
		this.treeCount = treeC;
		this.trees = new ArrayList<DecisionTree<T>>(treeC);
		this.featureCount = featureC;
		this.nMin = nMin;
	}

	void train(ArrayList<T> ps) {
		for (int i = 0; i < this.treeCount; ++i) {
			DecisionTree<T> tree = new DecisionTree<T>(nMin, this.featureCount);
			ArrayList<T> ensemble = new ArrayList<T>(ps.size());
			for (int j = 0; j < ps.size(); ++j) {
				ensemble.add(ps.get(rand.nextInt(ps.size())));
			}
			tree.train(ensemble);
			this.trees.add(tree);
		}
	}

	int classify(T testVal) {

		MultiSet<Integer> count = new MultiSet<Integer>();
		for (DecisionTree<T> tree : this.trees) {
			count.add(tree.classify(testVal));
		}
		int best = 0;
		int max = 0;
		for (int key : count.keySet()) {
			if (count.get(key) > max) {
				max = count.get(key);
				best = key;
			}
		}
		return best;
	}

}
