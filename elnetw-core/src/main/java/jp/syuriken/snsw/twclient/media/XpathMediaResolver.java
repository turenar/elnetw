package jp.syuriken.snsw.twclient.media;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Media Resolver by xpathEvaluator
 */
public class XpathMediaResolver extends AbstractMediaResolver {

	private final XPath xpathEvaluator;
	private String xpath;

	public XpathMediaResolver(String xpath) {
		this.xpath = xpath;
		XPathFactory factory = XPathFactory.newInstance();
		this.xpathEvaluator = factory.newXPath();
	}

	@Override
	public UrlInfo getUrl(String urlString) throws IllegalArgumentException, InterruptedException, IOException {
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
		URL url = new URL(urlString);
		StringReader reader = new StringReader(getContentsFromUrl(url));


		try {
			Document doc = builder.parse(new InputSource(reader));
			String scrapedUrlString = xpathEvaluator.evaluate(xpath, doc);
			if (scrapedUrlString != null) {
				URI uri = url.toURI().resolve(scrapedUrlString);
				scrapedUrlString = uri.toASCIIString();
				return new UrlInfo(scrapedUrlString, false, true);
			}
			return null;
		} catch (MalformedURLException | SAXException | XPathExpressionException | URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
