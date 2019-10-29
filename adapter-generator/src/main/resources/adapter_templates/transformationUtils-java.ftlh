package ${adapterData.packagename}.integration.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class TransformationUtils {

	public static String getXMLString(Node node) {
		if (!node.hasChildNodes()) return "";
		String element = "<" + node.getLocalName() + ">";
		NodeList children = node.getChildNodes();
		if(children.getLength() > 1) {
			for (int i = 0; i < children.getLength(); i++) {
				Node currentNode = children.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					element = element + getXMLString(currentNode);
				}
			}
		} else {
			element = element + node.getTextContent();
		}
		return element + "</" + node.getLocalName() + ">";
	}

	public static Node findNode(NodeList nodes, String nodePath) {
		String[] nodePathArray = nodePath.split("\\.");
		String elementName = nodePathArray[0];
		if (nodePathArray.length == 1) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n.getNodeName()!=null && n.getNodeName().equals(elementName)){
					return n;
				}
			}
		} else {
			Node first = findNode(nodes, elementName); // Called into the base case
			if (first!=null) {
				String remainingPath = "";
				for (int i=1; i < nodePathArray.length; i++) {
					remainingPath += nodePathArray[i] + ".";
				}
				if (!remainingPath.isEmpty()) {
					remainingPath = remainingPath.substring(0, remainingPath.length()-1);
					return findNode(first.getChildNodes(), remainingPath);
					
				}
			}
		}
		return null;
	}

	public static Node findNode(NodeList nodes, String nodeSearchingPath, Map<String, Integer> occurrencesMap) {
		return findNode(nodes, nodeSearchingPath, "", occurrencesMap);
	}

	public static Node findNode(NodeList nodes, String nodeSearchingPath, String nodeCurrentPath, Map<String, Integer> occurrencesMap) {
		String[] nodePathArray = nodeSearchingPath.split("\\.");
		String elementName = nodePathArray[0];
		String searching = nodeCurrentPath.isEmpty() ? elementName : nodeCurrentPath + "." + elementName;
		if (nodePathArray.length == 1) {
			List<Node> returningList = new ArrayList<>();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n.getNodeName()!=null && n.getNodeName().equals(elementName)){
					returningList.add(n);
				}
			}
			if (occurrencesMap.containsKey(searching)) {
				return returningList.get(occurrencesMap.get(searching)-1);
			} else {
				return returningList.isEmpty() ? null : returningList.get(0);
			}
		} else {
			Node node = findNode(nodes, elementName, nodeCurrentPath, occurrencesMap); // Called into the base case
			if (node!=null) {
				String remainingPath = "";
				for (int i=1; i < nodePathArray.length; i++) {
					remainingPath += nodePathArray[i] + ".";
				}
				if (!remainingPath.isEmpty()) {
					remainingPath = remainingPath.substring(0, remainingPath.length()-1);
					return findNode(node.getChildNodes(), remainingPath, searching, occurrencesMap);
					
				}
			}
		}
		return null;
	}

	public static List<Node> findNodes(NodeList nodes, String elementName) {
		List<Node> returnList = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
		    Node n = nodes.item(i);
		    if (n.getNodeName()!=null && n.getNodeName().equals(elementName)){
		    	returnList.add(n);
		    }
		}
		return returnList;
	}

	public static String nodeValue(Node node){
		if (node!=null) {
			return node.getTextContent();
		} else {
			return "";
		}
	}

	public static int findNumberOfOccurrences(NodeList nodes, String nodePath, Map<String, Integer> occurrencesMap) {
		String[] nodePathArray = nodePath.split("\\.");
		String elementName = nodePathArray[0];
		if (nodePathArray.length == 1) {
			List<Node> returningList = new ArrayList<>();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n.getNodeName()!=null && n.getNodeName().equals(elementName)){
					returningList.add(n);
				}
			}
			return returningList.size();
		} else {
			Node node = findNode(nodes, elementName, occurrencesMap); // Called into the base case
			if (node!=null) {
				String remainingPath = "";
				for (int i=1; i < nodePathArray.length; i++) {
					remainingPath += nodePathArray[i] + ".";
				}
				if (!remainingPath.isEmpty()) {
					remainingPath = remainingPath.substring(0, remainingPath.length()-1);
					return findNumberOfOccurrences(node.getChildNodes(), remainingPath, occurrencesMap);
				}
			}
		}
		return 0;
	}

	public static String findNodeValue(NodeList nodes, String nodePath) {
		return nodeValue(findNode(nodes, nodePath));
	}

	public static String findNodeValue(NodeList nodes, String nodePath, Map<String, Integer> occurrencesMap) {
		return nodeValue(findNode(nodes, nodePath, occurrencesMap));
	}

	public static NodeList findChildNodes(NodeList nodes, String elementName) {
		return findNode(nodes, elementName).getChildNodes();
	}

	public static String transformValueOf(String transformationRule, String elementValue) {
		String[] associations = transformationRule.split(",");
		for (int i=0; i<associations.length; i++) {
			String association = associations[i];
			String[] valueMapping = association.split(":");
			if(valueMapping.length==2) {
				if (valueMapping[0].equals(elementValue)) {
					return valueMapping[1];
				}
			}
		}
		return elementValue;
	}

    public static String convertDocumentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }
}
