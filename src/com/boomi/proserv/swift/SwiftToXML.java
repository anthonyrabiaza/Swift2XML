package com.boomi.proserv.swift;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.prowidesoftware.swift.io.ConversionService;
import com.prowidesoftware.swift.model.SwiftMessage;

/**
 * SwiftToXML Class, this class is an utility which will allow conversion from
 * Swift to XML massage and vice-versa. <br/> Additionally some utilities will allow
 * the transformation of String to InputStream and vice-versa for reading and
 * writing Documents in Boomi process, conversion of XML Document to String, etc.
 * 
 * @author anthony.rabiaza@gmail.com
 *
 */
public class SwiftToXML {

	/**
	 * Get the File content to String
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static String readFile(String fileName) throws Exception {

		System.out.println("Reading " + fileName + "...");
		StringBuffer buffer = new StringBuffer();
		String line = null;
		FileReader fileReader = new FileReader(fileName);

		BufferedReader bufferedReader = new BufferedReader(fileReader);

		while ((line = bufferedReader.readLine()) != null) {
			buffer.append(line);
			buffer.append("\n");
		}
		bufferedReader.close();

		return buffer.toString();
	}

	/**
	 * Parse the content of Swift Message and return XML document
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static String parseSwift(String content) throws Exception {
		SwiftMessage msg = SwiftMessage.parse(content);
		ConversionService conv = new ConversionService();
		String xmlContent = conv.getXml(msg);
		return xmlContent;
	}

	/**
	 * Parse the content of Swift Message and return XML document
	 * @param content
	 * @param recursive
	 * @return
	 * @throws Exception
	 */
	public static List<String> parseMultipleSwift(String content) throws Exception {
		List<String> listString 	= new ArrayList<String>();
		List<String> listSubString 	= new ArrayList<String>();
		SwiftMessage msg = SwiftMessage.parse(content);
		ConversionService conv = new ConversionService();
		String xmlContent = conv.getXml(msg);
		
		// Get element inside unparsedTexts
		Document doc = getXMLDocument(xmlContent);
		NodeList nodes = getNodes(doc, "//unparsedTexts/text");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node 						= nodes.item(i);
			List<String> xmlSubContent 		= parseMultipleSwift(node.getTextContent());
			for (Iterator<String> iterator 	= xmlSubContent.iterator(); iterator.hasNext();) {
				String currentxmlSubContent = iterator.next();
				Document docSubContent 		= getXMLDocument(currentxmlSubContent);
				//Node newNode 				= docSubContent.getFirstChild();
				//Node importedNode 		= doc.importNode(newNode, true);
				//node.getParentNode().appendChild(importedNode);
				listSubString.add(toString(docSubContent));
			}
			node.getParentNode().removeChild(node);
		}
		listString.add(toString(doc));
		listString.addAll(listSubString);
		
		return listString;
	}

	/**
	 * Parse the content of a XML Document and return Swift message
	 * @param xmlContent
	 * @return
	 * @throws Exception
	 */
	public static String parseXML(String xmlContent) throws Exception {
		// Remove element with name=REMOVE
		Document doc = getXMLDocument(xmlContent);
		NodeList nodes = getNodes(doc, "//tag[value[text() = 'REMOVE']]");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			node.getParentNode().removeChild(node);
		}
		xmlContent = toString(doc);
		// End Remove element with name=REMOVE
		ConversionService conv = new ConversionService();
		return conv.getFIN(xmlContent);
	}

	private static NodeList getNodes(Document doc, String xpathQuery) throws XPathExpressionException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		Object result = (xpath.compile(xpathQuery)).evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		return nodes;
	}

	private static Document getXMLDocument(String xmlContent)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(stringToInputStream(xmlContent));
		return doc;
	}

	/**
	 * Wrapper to Parse the content of Swift Message and return XML document as InputStream
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static InputStream parseSwift(InputStream is) throws Exception {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
			String swift = buffer.lines().collect(Collectors.joining("\n"));
			String xml = parseSwift(swift);
			return stringToInputStream(xml);
		}
	}
	
	/**
	 * Wrapper to Parse the content of Swift Message and return XML document as InputStream
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static List<InputStream> parseMultipleSwift(InputStream is) throws Exception {
		List<InputStream> streams = new ArrayList<InputStream>();
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
			String swift = buffer.lines().collect(Collectors.joining("\n"));
			List<String> xmls = parseMultipleSwift(swift);
			for (Iterator<String> iterator = xmls.iterator(); iterator.hasNext();) {
				String xml = (String) iterator.next();
				streams.add(new ByteArrayInputStream(xml.getBytes()));
			}
			return streams;
		}
	}

	/**
	 * Wrapper to parse the content of a XML Document and return Swift message
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static InputStream parseXML(InputStream is) throws Exception {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
			String xmlContent = buffer.lines().collect(Collectors.joining("\n"));
			String swift = parseXML(xmlContent);
			return stringToInputStream(swift);
		}
	}
	
	/**
	 * Wrapper to parse the content of a XML Documents and return Swift message with separator
	 * @param iss
	 * @param separator to add after each block, put null if no separator is required
	 * @param ending String that need to end the Swift message block to put the separator, put null to always add the separator
	 * @return
	 * @throws Exception
	 */
	public static InputStream parseMultipleXML(List<InputStream> iss, String separator, String ending) throws Exception {
		String swiftOut = "";
		for (Iterator<InputStream> iterator = iss.iterator(); iterator.hasNext();) {
			InputStream is = iterator.next();
			if(!swiftOut.equals("") && separator!=null && ((ending!=null && swiftOut.toString().endsWith(ending)) || ending==null)) {
				swiftOut += separator;  
			}
			swiftOut += inputStreamToString(SwiftToXML.parseXML(is));
		}
		return stringToInputStream(swiftOut);
	}

	/**
	 * Utility to convert InputStream to String
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}

	/**
	 * Utility to convert String to InputStream
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static InputStream stringToInputStream(String str) throws IOException {
		return new ByteArrayInputStream(str.getBytes());
	}

	/**
	 * Utility to convert XML Document to String
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static String toString(Document doc) throws Exception {

		StringWriter sw = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();

	}

}
