package info.tomerun.ml.encoding;

import info.tomerun.ml.NaiveBayesClassifier;
import info.tomerun.ml.RandomForest;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

/**
 * 文字コードの推定を機械学習で行う。
 */
public class EncodingClassifier {

	private static final String fullWidthPunctuation = "！”＃＄％＆’（）＝〜｜−＾￥＠＋＊；：＜＞，．／・？＿［］｛｝";
	private static final String halfWidthPunctuation = "!\"#$%&'()=~|-^¥@+*;:<>,./･?_[]{}";
	private static final HashMap<Character, Character> fullHalfMapping = new HashMap<Character, Character>();
	private static final String[] encodingName = { "UTF-8", "UTF-16LE", "Shift_JIS", "EUC-JP" };
	private static Random rand = new Random(42);
	private static final int SIZE = 1000;

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < fullWidthPunctuation.length(); ++i) {
			fullHalfMapping.put(fullWidthPunctuation.charAt(i), halfWidthPunctuation.charAt(i));
		}
		String[] source = input();
		byte[][][] bytes = encode(source);

		//		drawImage(bytes);
		byte[][][] trainingBytes = new byte[SIZE / 2][encodingName.length][];
		byte[][][] testBytes = new byte[SIZE / 2][encodingName.length][];
		select(bytes, trainingBytes, testBytes);
		Sentence[] trainingSet = new Sentence[encodingName.length * SIZE / 2];
		Sentence[] testSet = new Sentence[encodingName.length * SIZE / 2];
		for (int i = 0; i < SIZE / 2; ++i) {
			for (int j = 0; j < encodingName.length; ++j) {
				trainingSet[i * encodingName.length + j] = new Sentence(trainingBytes[i][j], j);
				testSet[i * encodingName.length + j] = new Sentence(testBytes[i][j], j);
			}
		}
		//		classifyNaiveBayes(trainingSet, testSet);
		classifyRandomForest(trainingSet, testSet);
	}

	private static String[] input() throws Exception {
		URL url = EncodingClassifier.class.getResource("common-sentences.txt");
		Scanner sc = new Scanner(new File(url.toURI()), "utf-8");
		String[] source = new String[SIZE];
		for (int i = 0; i < SIZE; ++i) {
			source[i] = sc.nextLine().replaceAll("[ 　\u00a0]", "").replaceAll("^＾", ""); // u00a0 is &nbsp; 
			if (rand.nextBoolean()) {
				source[i] = convertFullToHalf(source[i]);
			}
		}
		return source;
	}

	private static String convertFullToHalf(String str) {
		char[] from = str.toCharArray();
		char[] to = new char[str.length()];
		for (int i = 0; i < from.length; ++i) {
			char c = from[i];
			if ('Ａ' <= c && c <= 'Ｚ') c -= 'Ａ' - 'A';
			if ('ａ' <= c && c <= 'ｚ') c -= 'ａ' - 'a';
			if ('０' <= c && c <= '９') c -= '０' - '0';
			if (fullHalfMapping.containsKey(c)) c = fullHalfMapping.get(c);
			to[i] = c;
		}
		return String.valueOf(to);
	}

	private static byte[][][] encode(String[] source) throws CharacterCodingException {
		byte[][][] bytes = new byte[SIZE][encodingName.length][]; // 
		for (int i = 0; i < SIZE; ++i) {
			for (int j = 0; j < encodingName.length; ++j) {
				bytes[i][j] = encode(source[i], encodingName[j]);
			}
		}
		return bytes;
	}

	private static byte[] encode(String source, String encoding) throws CharacterCodingException {
		CharBuffer cb = CharBuffer.wrap(source.toCharArray());
		Charset charset = Charset.forName(encoding);
		CharsetEncoder encoder = charset.newEncoder();
		ByteBuffer buf = encoder.encode(cb);
		byte[] ret = new byte[buf.limit()];
		buf.get(ret);
		return ret;
	}

	private static void drawImage(byte[][][] bytes) throws IOException {
		double[][] distribution = count(bytes);
		for (int i = 0; i < encodingName.length; ++i) {
			drawImage(distribution[i], encodingName[i]);
		}
	}

	private static double[][] count(byte[][][] bytes) {
		int[] all = new int[4];
		int[][] hist = new int[encodingName.length][256];
		for (int i = 0; i < SIZE; ++i) {
			for (int j = 0; j < encodingName.length; ++j) {
				for (int k = 0; k < bytes[i][j].length; ++k) {
					hist[j][bytes[i][j][k] & 255]++;
				}
				all[j] += bytes[i][j].length;
			}
		}
		double[][] ret = new double[encodingName.length][256];
		for (int i = 0; i < encodingName.length; ++i) {
			for (int j = 0; j < 256; ++j) {
				ret[i][j] = 1.0 * hist[i][j] / all[i];
			}
		}
		return ret;
	}

	private static void drawImage(double[] distribution, String encoding) throws IOException {
		final int CELL = 20;
		final int MARGIN = 20;
		final int LEFT = 70;
		final int TOP = 50;
		final int WIDTH = CELL * 16 + MARGIN * 2 + LEFT;
		final int HEIGHT = CELL * 16 + MARGIN * 2 + TOP;
		final Color[] colors = { new Color(0xffffff), new Color(0xffccff), new Color(0xff99ff), new Color(0xff66ff),
				new Color(0xff33ff), new Color(0xff00ff), new Color(0xff00cc), new Color(0xff0099), new Color(0xff0066),
				new Color(0xff0033), new Color(0xff0000), };

		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.BLACK);

		// draw Strings
		FontMetrics metrics = g.getFontMetrics();

		g.drawString(encoding, (int) (WIDTH / 2 - metrics.getStringBounds(encoding, g).getWidth() / 2), 20);
		drawStringCenter(g, "low 4bit", LEFT + MARGIN + CELL * 16 / 2.0, TOP - 10);
		drawStringCenter(g, "high 4bit", LEFT / 2.0, TOP + MARGIN + CELL * 16 / 2.0);
		for (int i = 0; i < 16; ++i) {
			drawStringCenter(g, "" + i, LEFT + MARGIN + CELL * (2 * i + 1) / 2.0, TOP + MARGIN / 2.0);
			drawStringCenter(g, "" + i, LEFT + MARGIN / 2.0, TOP + MARGIN + CELL * (2 * i + 1) / 2.0);
		}

		// draw HeatMap
		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				int v = (int) (distribution[i * 16 + j] * 1000);
				int idx = 0;
				while (v > 0) {
					++idx;
					v /= 2;
				}
				g.setColor(colors[idx]);
				g.fillRect(LEFT + MARGIN + CELL * j, TOP + MARGIN + CELL * i, CELL, CELL);
			}
		}

		// draw Lines
		g.setColor(Color.LIGHT_GRAY);
		for (int i = 1; i < 16; ++i) {
			g.drawLine(LEFT + MARGIN, TOP + MARGIN + CELL * i, WIDTH - MARGIN, TOP + MARGIN + CELL * i); // horz
			g.drawLine(LEFT + MARGIN + CELL * i, TOP + MARGIN, LEFT + MARGIN + CELL * i, HEIGHT - MARGIN); // vert
		}
		g.setColor(Color.BLACK);
		g.drawLine(LEFT + MARGIN, TOP + MARGIN, WIDTH - MARGIN, TOP + MARGIN);
		g.drawLine(LEFT + MARGIN, TOP + MARGIN, LEFT + MARGIN, HEIGHT - MARGIN);
		g.drawLine(LEFT + MARGIN, TOP + MARGIN + CELL * 16, WIDTH - MARGIN, TOP + MARGIN + CELL * 16);
		g.drawLine(LEFT + MARGIN + CELL * 16, TOP + MARGIN, LEFT + MARGIN + CELL * 16, HEIGHT - MARGIN);

		ImageIO.write(image, "png", new File(encoding + ".png"));
	}

	private static void drawStringCenter(Graphics2D g, String str, double x, double y) {
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D rect = metrics.getStringBounds(str, g);
		g.drawString(str, (int) (x - rect.getWidth() / 2), (int) (y + rect.getHeight() / 2));
	}

	// bytesを訓練データとテストデータに振り分ける
	private static void select(byte[][][] bytes, byte[][][] train, byte[][][] test) {
		ArrayList<Integer> index = new ArrayList<Integer>();
		for (int i = 0; i < SIZE; ++i) {
			index.add(i);
		}
		Collections.shuffle(index);
		for (int i = 0; i < SIZE / 2; ++i) {
			for (int j = 0; j < encodingName.length; ++j) {
				train[i][j] = bytes[index.get(i)][j];
			}
		}
		for (int i = SIZE / 2; i < SIZE; ++i) {
			for (int j = 0; j < encodingName.length; ++j) {
				test[i - SIZE / 2][j] = bytes[index.get(i)][j];
			}
		}
	}

	private static void classifyNaiveBayes(Sentence[] trainingSet, Sentence[] testSet) {
		NaiveBayesClassifier<Sentence> naiveBayes = new NaiveBayesClassifier<Sentence>(0.1);
		naiveBayes.train(Arrays.asList(trainingSet));
		int correct = 0;
		for (Sentence testData : testSet) {
			int expect = testData.label();
			int actual = naiveBayes.classify(testData);
			if (expect == actual) {
				++correct;
			}
		}
		System.out.println("correct:" + correct);
		System.out.println("wrong:" + (testSet.length - correct));
	}

	private static void classifyRandomForest(Sentence[] trainingSet, Sentence[] testSet) {
		RandomForest<Sentence> randomForest = new RandomForest<Sentence>(500, 6, 300);
		randomForest.train(Arrays.asList(trainingSet));
		int correct = 0;
		for (Sentence testData : testSet) {
			int expect = testData.label();
			int actual = randomForest.classify(testData);
			if (expect == actual) {
				++correct;
			}
		}
		System.out.println("correct:" + correct);
		System.out.println("wrong:" + (testSet.length - correct));
	}

}
