
package runtime.utils.xml;

import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XML {
	private String sxml;
	Node node = null;

	private Element elem() {
		if (node instanceof Element)
			return (Element) node;
		return null;
	}

	public XML(String xml) {

		if (xml == "")
			throw new NullPointerException();
		this.sxml = stripNonValidXMLCharacters(xml).trim();

		parseXML();
	}

	public String stripNonValidXMLCharacters(String in) {
		StringBuffer out = new StringBuffer(); // Used to hold the output.
		char current; // Used to reference the current character.

		if (in == null || ("".equals(in)))
			return ""; // vacancy test.
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught
									// here; it should not happen.
			if ((current == 0x9) || (current == 0xA) || (current == 0xD)
					|| ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}

	private XML(Node textNode) {
		this.node = textNode;

	}

	private XML(Element node) {
		this.node = node;
	}

	public XML GetRoot() {
		return new XML(node.getOwnerDocument().getDocumentElement());
	}

	public Boolean hasComplexContent() {
		return getChildren().size() > 0;
	}

	public Node getNode() {
		return node;
	}

	private String localName() {
		if (elem() != null)
			return elem().getTagName();
		return "";
	}

	public String getTagName() {
		return localName();
	}

	public String getAttrib(String arg) {
		if (elem() != null) {
			if (elem().hasAttribute(arg)) {
				return elem().getAttribute(arg);
			}
		}
		return "";
	}

	public void setAttrib(String attribName, Boolean attribVal) {

		setAttrib(attribName, attribVal.toString());
	}

	public void setAttrib(String attribName, double attribVal) {

		setAttrib(attribName, ((Double) attribVal).toString());
	}

	public void setAttrib(String attribName, int attribVal) {
		setAttrib(attribName, Integer.toString(attribVal));
	}

	public void setAttrib(String attribName, String attribVal) {
		if (elem() != null)
			elem().setAttribute(attribName, attribVal);
	}

	public XML getChild(String tagName) {
		XMLList xl = getChildren(tagName);
		if (xl.size() > 0) {
			return xl.get(0);
		}
		return null;

	}

	public XML getFirstChild() {
		XMLList xl = getAllChildren();
		if (xl.size() > 0) {
			return xl.get(0);
		}
		return null;

	}

	public XML getLastChild() {
		XMLList xl = getAllChildren();
		if (xl.size() > 0) {
			return xl.get(xl.size() - 1);
		}
		return null;

	}

	public XMLList getAllChildren() {
		XMLList xl = new XMLList();
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node instanceof Element) {
				xl.add(new XML((Element) node));
			} else if (node.getNodeType() == Node.TEXT_NODE) {
				xl.add(new XML(node));
			}
		}
		return xl;
	}

	public XMLList getChildren() {
		XMLList xl = new XMLList();
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				xl.add(new XML((Element) nodes.item(i)));
			}
		}
		return xl;
	}

	public XMLList getChildren(String tagName) {
		XMLList taggedChildren = new XMLList();
		XMLList xl = getChildren();
		for (int i = 0; i < xl.size(); i++) {
			if (xl.get(i).getTagName().equals(tagName)) {
				taggedChildren.add(xl.get(i));
			}
		}
		return taggedChildren;
	}

	public XML appendChildList(XMLList xl) {
		for (XML xml : xl) {
			appendChild(xml);
		}
		return this;
	}

	public XML appendChild(XML xml) {
		try {
			org.w3c.dom.Document domYouAreAddingTheNodeTo = node
					.getOwnerDocument();
			Node tempNode = domYouAreAddingTheNodeTo.importNode(xml.node, true); // true
																					// if
																					// you
																					// want
																					// a
																					// deep
																					// copy

			this.node.appendChild(tempNode);

		} catch (Exception ex) {
		}
		return this;
	}

	/*
	 * public void setValue(String text) { //this.node.setNodeValue(text);
	 * this.node.setTextContent(text); }
	 * 
	 * public String getValue() { return this.node.getNodeValue();
	 * 
	 * }
	 */
	public int getChildCount() {
		return getChildren().size();

	}

	public int getChildCount(String tagName) {
		int count = 0;
		try {
			count = getChildren(tagName).size();
		} catch (Exception ex) {

		}
		return count;

	}

	public String getText() {
		return node.getTextContent();
	}

	public void setText(String text) {
		node.setTextContent(text);
	}

	public XML getParent() {
		if (node.getParentNode() != null
				&& node.getParentNode() instanceof Element) {
			Element el = (Element) node.getParentNode();
			if (el != null)
				return new XML(el);
		}
		return null;
	}

	public XML getDescendantByName(String tagName, String attribName,
			String attribVal) {
		for (XML xml : this.descendants(tagName)) {
			if (xml.getAttrib(attribName).equals(attribVal))
				return xml;
		}
		return null;
	}

	public XMLList descendants(String tagName) {
		XMLList xl = new XMLList();
		if (elem() != null) {
			NodeList nodes = elem().getElementsByTagName(tagName);
			for (int i = 0; i < nodes.getLength(); i++) {
				xl.add(new XML((Element) nodes.item(i)));
			}
		}

		return xl;

	}

	public String getChildNodeText(String tagName) {
		XML x = getChild(tagName);
		if (x != null)
			return x.getText();
		return "";
	}

	private Boolean parseXML() {
		DocumentBuilder docBuilder;
		org.w3c.dom.Document dom = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {

			System.out.println("Wrong parser configuration: " + e.getMessage());
			return null;
		}
		try {

			dom = docBuilder.newDocument(); // initialize
			node = dom.getDocumentElement(); // initialize

			InputSource in = new InputSource(new StringReader(sxml));
			in.setEncoding("UTF-8");

			dom = docBuilder.parse(in);
			node = dom.getDocumentElement();

			return true;

		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.out.println("Could not read source file: " + e.getMessage());
		}

		return false;
	}

	public void RemoveChild(XML x) {
		if (x != null && x.node != null)
			node.removeChild(x.node);

	}

	public String toString() {
		return getAllNodes(this.node);
	}

	public String toXmlString() {
		// set up a transformer
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans;
		try {
			trans = transfac.newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
			return "";
		}
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// create string from xml tree
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(node.getOwnerDocument());
		try {
			trans.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String xmlString = sw.toString();

		return xmlString;
	}

	private String getNode(Node node) {
		String Node = "<" + node.getNodeName().toString();
		if (node.hasAttributes()) {
			for (int m = 0; m < node.getAttributes().getLength(); m++) {
				Node += " " + node.getAttributes().item(m).getNodeName();
				Node += "='" + node.getAttributes().item(m).getNodeValue()
						+ "'";
			}
		}
		Node += ">";
		return Node;
	}

	private String getAllNodes(Node node) {
		String document = "";
		document += getNode(node);
		document += "\r\n\t";
		if (node.hasChildNodes()) {
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				document += getAllNodes(node.getChildNodes().item(i));
				if (i == node.getChildNodes().getLength() - 1) {
					document += "</" + node.getNodeName() + ">";
					document += "\r\n\t";
				}
			}
		} else {
			document += "</" + node.getNodeName() + ">";
		}
		return document;
	}

}
