package runtime.utils.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

public class XMLParser {

	private DocumentBuilderFactory domFactory;
	private DocumentBuilder builder;
	private Document doc;
	private XPathFactory factory;
	private XPath xpath;
	private XPathExpression expr;

	public XMLParser() {
		try {
			domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			builder = domFactory.newDocumentBuilder();
			factory = XPathFactory.newInstance();
			xpath = factory.newXPath();

		} catch (ParserConfigurationException pce) {
			System.out
					.println("ParserConfigurationException:" + pce.toString());
		}

	}

	public void setDocument(String xml) throws IOException, SAXException {
		InputSource is = new InputSource(new StringReader(xml));
		doc = builder.parse(is);
	}

	public void setDocument(InputStream is) throws IOException, SAXException {
		String xml = convertStreamToString(is);
		setDocument(xml);
	}

	public String getParsedXMLString() {
		String xmlString = null;
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			xmlString = result.getWriter().toString();

		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		return xmlString;
	}

	public String getParsedXMLString(Node node) {
		String xmlString = null;
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(node);
			transformer.transform(source, result);

			xmlString = result.getWriter().toString();

		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		return xmlString;
	}

	private String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public Object search(String xpathExpression)
			throws XPathExpressionException {
		expr = xpath.compile(xpathExpression);// "//book[author='Neal Stephenson']/title/text()"
		return expr.evaluate(doc, XPathConstants.NODESET);
	}
}
