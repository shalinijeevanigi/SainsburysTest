package com.sby.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.math.BigDecimal;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sby.parse.ParseDoc;

/**
 * @author admin
 *
 */
public class ParseDocTest {
	ParseDoc parseDoc;
	private static String URL;
	Document doc;

	@Before
	public void setUp() throws Exception {
		parseDoc = new ParseDoc();
		URL = "http://hiring-tests.s3-website-eu-west-1.amazonaws.com/2015_Developer_Scrape/5_products.html";

	}

	@After
	public void tearDown() throws Exception {
		parseDoc = null;
	}

	/**
	 * test to check document returned is not null
	 */
	@Test
	public void testGetDocumentByUrl() {

		try {
			doc = parseDoc.getDocumentByURL(URL);
		} catch (IOException e) {
		}
		assertNotNull(doc);
	}

	/**
	 * test to check productArray returned is not null
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetProductArray() throws IOException {
		doc = parseDoc.getDocumentByURL(URL);
		JSONArray productArray = parseDoc.getProductArray(doc.select(".listView"));
		assertNotNull(productArray);
	}

	/**
	 * if no products are available then null should be returned
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetProductArrayFail() throws IOException {
		doc = parseDoc.getDocumentByURL(URL);
		JSONArray productArray = parseDoc.getProductArray(doc.select(".list"));
		assertNull(productArray);
	}

	/**
	 * test to check sum of product unit prices
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCalculateTotal() {

		JSONArray productArray = new JSONArray();
		JSONObject jObject1 = new JSONObject();
		JSONObject jObject2 = new JSONObject();
		JSONObject jObject3 = new JSONObject();

		jObject1.put("title", "abc");
		jObject1.put("size", "34kb");
		jObject1.put("price", "3.40");
		jObject1.put("description", "xyz");
		productArray.add(jObject1);

		jObject2.put("title", "ABC");
		jObject2.put("size", "35kb");
		jObject2.put("price", "1.40");
		jObject2.put("description", "XYZ");
		productArray.add(jObject2);

		jObject3.put("title", "ABC12");
		jObject3.put("size", "35kb");
		jObject3.put("price", "");
		jObject3.put("description", "XYZ12");
		productArray.add(jObject3);

		BigDecimal bd = parseDoc.calculateTotal(productArray);
		assertEquals(4.8, bd.doubleValue(), 2);

	}

	/**
	 * test to get price value from the given price string per unit
	 */
	@Test
	public void testGetPrice() {
		String priceStr = "£3.50/unit";
		BigDecimal bd = parseDoc.getPrice(priceStr);
		assertEquals(3.5, bd.doubleValue(), 0);
	}

}
