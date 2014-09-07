/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.media;

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
