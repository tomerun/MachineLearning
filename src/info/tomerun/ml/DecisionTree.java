package info.tomerun.ml;
import info.tomerun.ml.util.MultiSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class DecisionTree<T extends FeatureSet> {

	int nodeCount, featureCount;
	Node root;
	Random rand = new Random();

	DecisionTree(int nodeCount, int featureCount) {
		this.nodeCount = nodeCount;
		this.featureCount = featureCount;
	}

	void train(List<T> trainData) {
		this.root = new Node(trainData);
		PriorityQueue<Node> q = new PriorityQueue<Node>(nodeCount, new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				return Double.compare(o2.entropy(), o1.entropy());
			}
		});
		q.add(this.root);
		for (int i = 1; i <= nodeCount && !q.isEmpty(); ++i) {
			Node cur = q.poll();
			if (cur.entropy() == 0) break;
			cur.split();
			if (cur.hasChild()) {
				q.add(cur.left);
				q.add(cur.right);
			}
		}
	}

	int classify(T testData) {
		Node current = this.root;
		while (current.hasChild()) {
			current = current.next(testData);
		}
		return current.label();
	}

	private class Node {
		private Node left, right;
		private List<T> trainData;
		private MultiSet<Integer> count = new MultiSet<Integer>();
		private int label = FeatureSet.NA;
		private double entropy = Double.NaN;
		private int splitFeatureIdx = FeatureSet.NA;
		private int splitThreshold = FeatureSet.NA;

		Node(List<T> trainData) {
			this.trainData = trainData;
			for (T val : this.trainData) {
				this.count.add(val.label());
			}
		}

		void print(int depth) {
			for (int i = 0; i < depth; ++i) {
				System.out.print("  ");
			}
			System.out.println(this.count);
			if (hasChild()) {
				this.left.print(depth + 1);
				this.right.print(depth + 1);
			}
		}

		void split() {
			int[] featureIdx = selectFeature();
			determineBestSplitPoint(featureIdx);
			divideMe();
		}

		private int[] selectFeature() {
			boolean[] used = new boolean[this.trainData.get(0).featureSize()];
			int[] featureIdx = new int[DecisionTree.this.featureCount];
			for (int i = 0; i < featureIdx.length;) {
				int fi = DecisionTree.this.rand.nextInt(used.length);
				if (used[fi]) continue;
				featureIdx[i] = fi;
				used[fi] = true;
				++i;
			}
			return featureIdx;
		}

		// simple brute-force
		private void determineBestSplitPoint(int[] featureIdx) {
			double bestEntropyGain = -1;
			for (int i = 0; i < featureIdx.length; ++i) {
				final int fid = featureIdx[i];
				ArrayList<T> data = new ArrayList<T>(this.trainData);
				Collections.sort(data, new Comparator<T>() {
					public int compare(T o1, T o2) {
						int v1 = o1.feature(fid);
						int v2 = o2.feature(fid);
						if (v1 == v2) return 0;
						if (v1 == FeatureSet.NA) return -1;
						if (v2 == FeatureSet.NA) return 1;
						return v1 - v2;
					}
				});
				int start = 0;
				MultiSet<Integer> applicableCount = this.count.clone();
				while (start < data.size() && data.get(start).feature(fid) == FeatureSet.NA) {
					applicableCount.removeOne(data.get(start).label());
					++start;
				}
				if (start == data.size()) continue;
				MultiSet<Integer> leftCount = new MultiSet<Integer>();
				for (int j = start; j < data.size(); ++j) {
					int v = data.get(j).feature(fid);
					leftCount.add(data.get(j).label());
					if (j < data.size() - 1 && v != data.get(j + 1).feature(fid)) {
						MultiSet<Integer> left = leftCount.clone();
						MultiSet<Integer> right = new MultiSet<Integer>();
						for (int key : applicableCount.keySet()) {
							right.set(key, applicableCount.get(key) - left.get(key));
						}
						int leftSize = j - start + 1;
						int rightSize = data.size() - start - leftSize;
						boolean[] assignLeft = new boolean[start];
						for (int k = 0; k < start; ++k) {
							int label = data.get(k).label();
							double pLeft = 1.0 * left.get(label) / leftSize;
							double pRight = 1.0 * right.get(label) / rightSize;
							if (pLeft > pRight) {
								assignLeft[k] = true;
							} else if (pLeft == pRight && rand.nextBoolean()) {
								assignLeft[k] = true;
							}
						}
						if (left.total() + right.total() + start != this.trainData.size()) {
							System.out.println(left + " " + right + " " + start + " " + this.trainData.size());
						}
						for (int k = 0; k < start; ++k) {
							if (assignLeft[k]) {
								left.add(data.get(k).label());
							} else {
								right.add(data.get(k).label());
							}
						}
						double entropyGain = this.entropy() - left.entropy() * left.total() / this.trainData.size()
								- right.entropy() * right.total() / this.trainData.size();
						if (entropyGain > bestEntropyGain) {
							bestEntropyGain = entropyGain;
							this.splitFeatureIdx = fid;
							this.splitThreshold = v;
						}
					}
				}
			}
		}

		private void divideMe() {
			if (this.splitFeatureIdx == FeatureSet.NA) return;
			ArrayList<T> leftData = new ArrayList<T>();
			ArrayList<T> rightData = new ArrayList<T>();
			for (T val : this.trainData) {
				if (val.feature(splitFeatureIdx) <= splitThreshold) {
					leftData.add(val);
				} else {
					rightData.add(val);
				}
			}
			this.trainData.clear();
			this.left = new Node(leftData);
			this.right = new Node(rightData);
		}

		Node next(T testData) {
			int val = testData.feature(this.splitFeatureIdx);
			return val <= this.splitThreshold ? this.left : this.right;
		}

		boolean hasChild() {
			return this.left != null;
		}

		int label() {
			if (this.label != FeatureSet.NA) return this.label;
			this.label = this.count.mode();
			return this.label;
		}

		double entropy() {
			if (!Double.isNaN(this.entropy)) {
				return this.entropy;
			}
			this.entropy = this.count.entropy();
			return this.entropy;
		}

	}

}
