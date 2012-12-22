package info.tomerun.ml.util;
import java.util.ArrayList;

public class EasyCsv {

	public static ArrayList<String> parse(String line) {
		ArrayList<String> ret = new ArrayList<String>();
		int pos = 0;
		while (true) {
			while (pos < line.length() && Character.isWhitespace(line.charAt(pos))) {
				++pos;
			}
			if (pos == line.length()) {
				ret.add("");
				break;
			}
			StringBuilder sb = new StringBuilder();
			boolean quote = line.charAt(pos) == '"';
			if (quote) ++pos;
			while (pos < line.length()) {
				char c = line.charAt(pos);
				if (!quote && c == ',') {
					break;
				}
				++pos;
				if (quote && c == '"') {
					quote = false;
					continue;
				}
				sb.append(c);
			}
			ret.add(sb.toString().trim());
			if (pos == line.length()) break;
			++pos;
		}
		return ret;
	}
}
