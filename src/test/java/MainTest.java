import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class MainTest {

	@Test
	public void testSort() throws JsonParseException, JsonProcessingException,
			IOException {

		String actual = Main
				.sort("{'d':[[7,2],[8,10,1]],'b':'1','c':['2','3','1'],'a':[{'b':1,'a':'2' },{'c':1,'d':4 },{'b':1,'a':'0' }]}");

		String expected = "{\r\n  \"a\" : [\r\n    {\r\n      \"a\" : \"0\",\r\n      \"b\" : 1\r\n    },\r\n    {\r\n      \"a\" : \"2\",\r\n   "
				+ "   \"b\" : 1\r\n    },\r\n    {\r\n      \"c\" : 1,\r\n      \"d\" : 4\r\n  "
				+ "  }\r\n  ],\r\n  \"b\" : \"1\",\r\n  \"c\" : [\r\n    \"1\",\r\n    \"2\",\r\n    \"3\"\r\n  ],\r\n  "
				+ "\"d\" : [\r\n    [\r\n      2,\r\n      7\r\n    ],\r\n    [\r\n      1,\r\n      8,\r\n      10\r\n    ]\r\n  ]\r\n}";

		org.junit.Assert.assertEquals(expected, actual);
	}
}
