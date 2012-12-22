package info.tomerun.ml.util;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

public class EasyCsvTest {

	@Test
	public void test() {
		ArrayList<String> result = EasyCsv.parse("abc, def  ,\"123\",\"456,789\",0");
		Assert.assertEquals(5, result.size());
		Assert.assertEquals("abc", result.get(0));
		Assert.assertEquals("def", result.get(1));
		Assert.assertEquals("123", result.get(2));
		Assert.assertEquals("456,789", result.get(3));
		Assert.assertEquals("0", result.get(4));
		result = EasyCsv.parse(",,\"\",");
		Assert.assertEquals(4, result.size());
		Assert.assertEquals("", result.get(0));
		Assert.assertEquals("", result.get(1));
		Assert.assertEquals("", result.get(2));
		Assert.assertEquals("", result.get(3));
		result = EasyCsv.parse(",");
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("", result.get(0));
		Assert.assertEquals("", result.get(1));
		result = EasyCsv.parse("\"a\",\"b\"");
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("a", result.get(0));
		Assert.assertEquals("b", result.get(1));
		result = EasyCsv.parse("hoge");
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("hoge", result.get(0));
		result = EasyCsv.parse("");
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("", result.get(0));
	}

}
