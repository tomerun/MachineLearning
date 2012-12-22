package info.tomerun.ml.util;

import java.util.HashMap;
import java.util.Set;

public class MultiSet<T> {

	private static final double log_2_e = Math.log(2);
	private HashMap<T, Integer> map = new HashMap<T, Integer>();

	public MultiSet<T> clone() {
		MultiSet<T> ret = new MultiSet<T>();
		ret.map = new HashMap<T, Integer>(this.map);
		return ret;
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MultiSet)) return false;
		return ((MultiSet) o).map.equals(this.map);
	}

	@Override
	public int hashCode() {
		return this.map.hashCode();
	}

	public void add(T v) {
		if (!map.containsKey(v)) {
			map.put(v, 1);
		} else {
			map.put(v, map.get(v) + 1);
		}
	}

	public void removeOne(T v) {
		map.put(v, map.get(v) - 1);
	}

	public void set(T v, int value) {
		map.put(v, value);
	}

	public int get(T v) {
		if (!map.containsKey(v)) return 0;
		return map.get(v);
	}

	public int total() {
		int ret = 0;
		for (T key : keySet()) {
			ret += get(key);
		}
		return ret;
	}

	public T mode() {
		T best = null;
		int max = 0;
		for (T key : keySet()) {
			if (get(key) > max) {
				max = get(key);
				best = key;
			}
		}
		return best;
	}

	public Set<T> keySet() {
		return this.map.keySet();
	}

	public double entropy() {
		double ret = 0;
		int total = total();
		for (T key : keySet()) {
			int count = get(key);
			if (count != 0) {
				double p = 1.0 * count / total;
				ret -= p * Math.log(p) / log_2_e;
			}
		}
		return ret;
	}
}
