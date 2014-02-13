package runtime.utils.xml;

import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.CharacterData;

public final class XmlUtil {

	private static DocumentBuilderFactory _documentBuilderFactory;

	static {

		_documentBuilderFactory = DocumentBuilderFactory.newInstance();
	}

	public static Document getXmlDocument(String xml) {

		try {

			DocumentBuilder documentBuilder = _documentBuilderFactory
					.newDocumentBuilder();
			return documentBuilder
					.parse(new InputSource(new StringReader(xml)));

		} catch (Exception exception) {

			exception.printStackTrace();
		}

		return null;
	}

	public static Document getXmlDocument(File file) {

		try {

			DocumentBuilder documentBuilder = _documentBuilderFactory
					.newDocumentBuilder();
			return documentBuilder.parse(file);

		} catch (Exception exception) {

			exception.printStackTrace();
		}

		return null;
	}

	public static String getTag(String tagText) {
		tagText = tagText.trim();
		if (tagText.equals(""))
			tagText = "block";
		return "<" + tagText.trim() + "/>";
	}

	public static String getEmptyXml(String tagText) {

		return "<" + tagText + "/>";
	}

	public static String getStartTag(String tagText) {

		return "<" + tagText + ">";
	}

	public static String getEndTag(String tagText) {

		return "</" + tagText + ">";
	}

	public static Node getNodeByAttributeValue(Document document,
			String tagText, String attributeName, String attributeValue,
			boolean ignoreCase) {

		try {

			attributeValue = attributeValue.trim();
			NodeList elements = document.getElementsByTagName(tagText.trim());

			for (int index = 0; index < elements.getLength(); index++) {

				try {

					Node currentNode = elements.item(index);

					if (ignoreCase ? attributeValue
							.equalsIgnoreCase(getAttributeValue(currentNode,
									attributeName).trim()) : attributeValue
							.equals(getAttributeValue(currentNode,
									attributeName).trim())) {

						return currentNode;
					}

				} catch (Exception exception) {
				}
			}

		} catch (Exception exception) {
		}

		return null;
	}

	public static String getAttributeValue(Node element, String attributeName) {

		try {

			if (element.hasAttributes()) {

				NamedNodeMap attributes = element.getAttributes();
				return attributes.getNamedItem(attributeName).getNodeValue();
			}

		} catch (Exception exception) {

			exception.printStackTrace();
		}

		return null;
	}

	public static void setAttributeValue(Element element, String attributeName,
			String attributeValue) {

		try {

			element.setAttribute(attributeName.trim(), attributeValue);

		} catch (Exception exception) {

			exception.printStackTrace();
		}
	}

	public static Node appendXml(Node node, Node nodeToAppend) {

		try {

			return node.appendChild(node.getOwnerDocument().importNode(
					nodeToAppend, true));

		} catch (Exception exception) {

			exception.printStackTrace();
		}

		return null;
	}

	public static Node appendXml(Node node, String xml) {

		try {

			Document newNode = getXmlDocument(xml);
			return node.appendChild(node.getOwnerDocument().importNode(
					newNode.getDocumentElement(), true));

		} catch (Exception exception) {

			exception.printStackTrace();
		}

		return null;
	}

	public static Node removeNode(Node parentNode, Node childNode) {

		try {

			return parentNode.removeChild(childNode);

		} catch (Exception exception) {

			exception.printStackTrace();
		}

		return null;
	}

	public static String removeXMLSchemaTag(String aXML) {
		try {
			aXML = aXML.indexOf("<?xml") > -1 ? aXML.substring(aXML
					.indexOf("?>") + 2) : aXML;
		} catch (Exception exp) {
			// logger.msg(Constants.LEVEL_ERROR, exp.getMessage(),
			// LogUtil.getLineNumber());
		}
		return aXML;

	}

	public static String removeInvalidXMLCharacters(String s) {
		StringBuilder out = new StringBuilder(); // Used to hold the output.
		int codePoint; // Used to reference the current character.
		int i = 0;

		while (i < s.length()) {
			codePoint = s.codePointAt(i); // This is the unicode code of the
											// character.
			if ((codePoint == 0x9)
					|| // Consider testing larger ranges first to improve speed.
					(codePoint == 0xA) || (codePoint == 0xD)
					|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
					|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
					|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
				out.append(Character.toChars(codePoint));
			}
			i += Character.charCount(codePoint); // Increment with the number of
													// code units(java chars)
													// needed to represent a
													// Unicode char.
		}
		return out.toString();
	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "";
	}

}
