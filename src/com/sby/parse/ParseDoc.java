package com.sby.parse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author ShaliniJeevanigi
 * 
 *         This class parses the given test URL and creates JSON object result
 *
 */
public class ParseDoc {

	private static final String URL = "http://hiring-tests.s3-website-eu-west-1.amazonaws.com/2015_Developer_Scrape/5_products.html";
	private static final long KILO = 1024;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		ParseDoc parseDoc = new ParseDoc();
		Document doc;

		try {
			JSONArray productArray = null;
			doc = parseDoc.getDocumentByURL(URL);
			JSONObject results = new JSONObject();

			if (doc != null) {
				Elements productLister = doc.select(".listView");
				if (!productLister.isEmpty()) {
					productArray = parseDoc.getProductArray(productLister);
					if (productArray != null) {
						BigDecimal total = parseDoc.calculateTotal(productArray);

						results.put("results", productArray);
						results.put("total", total);
					}
					System.out.println(results.toJSONString());

				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets JASONArray of products
	 * 
	 * @param productLister
	 * @param productArray
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray getProductArray(Elements productLister) {
		JSONArray productArray = null;
		for (Element productList : productLister) {
			Elements products = productList.select("li h3");
			if (products != null) {
				productArray = new JSONArray();
				for (Element product : products) {
					productArray.add(createJasonObject(product));
				}
			}
		}
		return productArray;
	}

	/**
	 * Gets the document for the given URL
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Document getDocumentByURL(String url) throws IOException {
		if (url != null) {
			return Jsoup.connect(url).get();
		}
		return null;
	}

	/**
	 * Calculates sum of all unit prices in the page
	 * 
	 * @param productArray
	 * @return
	 */
	public BigDecimal calculateTotal(JSONArray productArray) {
		double price = 0.0;
		for (Object object : productArray) {
			JSONObject product = (JSONObject) object;
			Object priceObj = product.get("price");
			if (priceObj != null && priceObj.toString() != "") {
				price = price + Double.parseDouble(priceObj.toString());
			}
		}

		BigDecimal total = new BigDecimal(price);
		total = total.setScale(2, RoundingMode.HALF_UP);
		return total;
	}

	/**
	 * Creates JSON object which holds product details
	 * 
	 * @param product
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject createJasonObject(Element product) {
		Elements urls = product.select("a[href]");
		String productURL = urls.first().attr("href");
		JSONObject jObject = new JSONObject();

		try {
			Document productPage = Jsoup.connect(productURL).get();
			jObject.put("title", getValue(productPage, "h1"));
			jObject.put("size", (productPage.toString().length() / KILO) + "kb");
			jObject.put("price", getPrice(getValue(productPage, ".pricePerUnit")));
			jObject.put("description", getValue(productPage, "div.productText"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jObject;
	}

	/**
	 * Gets price from the unitprice string
	 * 
	 * @param pricePerUnit
	 * @return
	 */
	public BigDecimal getPrice(String pricePerUnit) {
		BigDecimal price = new BigDecimal(0);

		// This pattern is used to get the price value from the priceString
		// eg: £3.50/unit, we want 3.50
		final Pattern pattern = Pattern.compile("(\\d)+\\.(\\d)+");
		final Matcher matcher = pattern.matcher(pricePerUnit);

		while (matcher.find()) {
			price = new BigDecimal(matcher.group());
		}

		return price;
	}

	/**
	 * Gets the value for the given doc and the field type
	 * 
	 * @param productPage
	 * @param type
	 * @return
	 */
	public String getValue(Document productPage, String type) {
		Elements elements = productPage.select(type);
		if (null != elements) {
			return elements.first().text();
		}
		return null;
	}

}
