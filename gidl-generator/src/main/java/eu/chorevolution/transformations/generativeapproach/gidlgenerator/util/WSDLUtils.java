/*
  * Copyright 2015 The CHOReVOLUTION project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package eu.chorevolution.transformations.generativeapproach.gidlgenerator.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.chorevolution.modelingnotations.gidl.ComplexType;
import eu.chorevolution.modelingnotations.gidl.DataType;
import eu.chorevolution.modelingnotations.gidl.OccurrencesTypes;
import eu.chorevolution.modelingnotations.gidl.SimpleType;
import eu.chorevolution.modelingnotations.gidl.SimpleTypes;
import eu.chorevolution.modelingnotations.gidl.impl.GidlFactoryImpl;
import eu.chorevolution.transformations.generativeapproach.gidlgenerator.GIDLGeneratorException;

public class WSDLUtils {


	private final static String STARTING_TYPES_NODE = "5";
	private final static String ONLY_INITIATING = "0";
	private final static String ONLY_RECEIVING = "1";
	private final static String BOTH_INITIATING_RECEIVING = "2";
	private final static String PORT_TYPE_SUFFIX = "PT";
	
	public static String getSTARTING_TYPES_NODE() {
		return STARTING_TYPES_NODE;
	}
	
	public static String getONLY_INITIATING() {
		return ONLY_INITIATING;
	}

	public static String getONLY_RECEIVING() {
		return ONLY_RECEIVING;
	}

	public static String getBOTH_INITIATING_RECEIVING() {
		return BOTH_INITIATING_RECEIVING;
	}

	
	public static Definition getWsdlDefinition(final byte[] wsdlContent) throws GIDLGeneratorException {
		WSDLReader wsdlReader;
		try {
			wsdlReader = WSDLFactory.newInstance().newWSDLReader();
			return wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(wsdlContent)));
		} catch (WSDLException e) {
			throw new GIDLGeneratorException("Internal Error while reading the WSDL");
		}
	}

	public static List<Operation> getOperations(Definition wsdlDefinition, String name) {
		List<Operation> operations = new ArrayList<Operation>();

		Iterator<?> portTypeIterator = wsdlDefinition.getPortTypes().entrySet().iterator();
		while (portTypeIterator.hasNext()) {
			// find portType that contains the operation
			PortType portType = ((PortType) ((Entry<?, ?>) portTypeIterator.next()).getValue());

			if(!portType.getQName().getLocalPart().equals(name+PORT_TYPE_SUFFIX))//the name must be the same
				continue;

			Iterator<?> operationIterator = portType.getOperations().iterator();
			while (operationIterator.hasNext()) {
				Operation operation = (Operation) operationIterator.next();
				if (!operations.contains(operation)) {
					operations.add(operation);
				}
			}
		}
		return operations;
	}

	
	
	
	
	
	
	public static TreeNode<TreeNodeDataType> getCompleteTypeDefinitionsFromWSDL(byte[] documentWSDL, String typeName) throws SAXException, IOException, ParserConfigurationException {

		TreeNode<TreeNodeDataType> rootElem = getTypeDefinitionsFromWSDL(documentWSDL, typeName, 0);
		
		//now i have to clean the tree, delete all nodes with getStarting_types_node

		rootElem = cleanTree(rootElem);
		
		//the first is getstarting..
				
		List<TreeNode<TreeNodeDataType>> listToReturn = new ArrayList<TreeNode<TreeNodeDataType>>();
		for(TreeNode<TreeNodeDataType> node : rootElem.getChildren()) {
			listToReturn.add(node);
		}
				
    	TreeNode<TreeNodeDataType> thisinput = new TreeNode<TreeNodeDataType>();
		for(TreeNode<TreeNodeDataType> trr : listToReturn) {
			thisinput.addChild(trr);
		}
		
		assignOccurrencesToTree(documentWSDL, thisinput);
		assignComplexOrSimpleToTree(documentWSDL, thisinput);
		
		return thisinput;
	}
	
	
	
	public static TreeNode<TreeNodeDataType> getTypeDefinitionsFromWSDL(byte[] documentWSDL, String typeName, int iterationCalled) throws SAXException, IOException, ParserConfigurationException {

		TreeNode<TreeNodeDataType> rootElem = new TreeNode<TreeNodeDataType>(new TreeNodeDataType(getSTARTING_TYPES_NODE(), getSTARTING_TYPES_NODE()));
		TreeNode<TreeNodeDataType> rootElem2;
		
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(documentWSDL));
		//complextype
		NodeList elementsComplex = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:complexType");
		for (int i = 0; i < elementsComplex.getLength(); i++) {
			if(elementsComplex.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(typeName)) {
				try {
					Node eleminside = elementsComplex.item(i).getFirstChild();
					
					//added
					if(iterationCalled == 0) {
						rootElem.addChild(new TreeNode<TreeNodeDataType>(new TreeNodeDataType(elementsComplex.item(i).getAttributes().getNamedItem("name").getNodeValue(), typeName)));
						rootElem2 = rootElem;
						rootElem = rootElem.getChildren().get(rootElem.getNumberOfChildren()-1);
					}
					else
						rootElem2 = rootElem;
					
					List<TreeNode<TreeNodeDataType>> listReturn = recursiveWSDLElement(eleminside, documentWSDL);

					for(TreeNode tree : listReturn)
						rootElem.addChild(tree);

					return rootElem2;

				}
				catch(Exception e) {
					//cannot find type
					return null;
				}
			}
		}
		
		//simpletype
		NodeList elementsSimple = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:simpleType");
		for (int i = 0; i < elementsSimple.getLength(); i++) {
			if(elementsSimple.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(typeName)) {
				try {
					Node eleminside = elementsSimple.item(i).getFirstChild();
					
					//added
					if(iterationCalled == 0) {
						rootElem.addChild(new TreeNode<TreeNodeDataType>(new TreeNodeDataType(elementsSimple.item(i).getAttributes().getNamedItem("name").getNodeValue(), typeName)));
						rootElem2 = rootElem;
						rootElem = rootElem.getChildren().get(rootElem.getNumberOfChildren()-1);
					}
					else
						rootElem2 = rootElem;

					List<TreeNode<TreeNodeDataType>> listReturn = recursiveWSDLElement(eleminside, documentWSDL);

					for(TreeNode<TreeNodeDataType> tree : listReturn) {
						rootElem.addChild(tree);
					}

					return rootElem2;

				}
				catch(Exception e) {
					//cannot find type
					return null;
				}
			}
		}

		if(rootElem.getNumberOfChildren() == 0) {//is an ending-type

			NodeList elements = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:element");
			for(int i=0; i<elements.getLength(); i++) {
				if(elements.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(typeName)) {
					String[] typeSplit = elements.item(i).getAttributes().getNamedItem("type").getNodeValue().split(":");
					return new TreeNode<TreeNodeDataType>(new TreeNodeDataType(typeSplit[typeSplit.length-1], elements.item(i).getAttributes().getNamedItem("name").getNodeValue()));

				}
			}



		}

		return rootElem;
	}

	public static List<TreeNode<TreeNodeDataType>> recursiveWSDLElement(Node nodeInside, byte[] documentWSDL) {

		List<TreeNode<TreeNodeDataType>> listTree = new ArrayList<TreeNode<TreeNodeDataType>>();
		Node eleminside = nodeInside;

		if(eleminside != null) {
			//for complextypes
			if(eleminside.getNodeName().equals("xsd:element")) {
				String[] typeSplit = eleminside.getAttributes().getNamedItem("type").getNodeValue().split(":");

				listTree.add(new TreeNode<TreeNodeDataType>(new TreeNodeDataType(typeSplit[typeSplit.length-1], eleminside.getAttributes().getNamedItem("name").getNodeValue())));

				
				//!typeSplit[0].equals("xsd") because the tns may be omitted
				if((typeSplit[0].equals("tns"))||(!typeSplit[0].equals("xsd"))) {//recursively visit all other nodes (double ricorsion :()
					try {
						listTree.get(listTree.size()-1).addChild(getTypeDefinitionsFromWSDL(documentWSDL, typeSplit[typeSplit.length-1], 1));
					} catch (SAXException | IOException | ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			//for simpletypes
			if(eleminside.getNodeName().equals("xsd:restriction")) {
				String[] typeSplit = eleminside.getAttributes().getNamedItem("base").getNodeValue().split(":");

				//FIXED last line of the method with nodeFather
				Node nodeFather = eleminside.getParentNode();
				listTree.add(new TreeNode<TreeNodeDataType>(new TreeNodeDataType(typeSplit[typeSplit.length-1], nodeFather.getAttributes().getNamedItem("name").getNodeValue())));

				if((typeSplit[0].equals("tns"))||(!typeSplit[0].equals("xsd"))) {//recursively visit all other nodes (double ricorsion :()
					try {
						listTree.get(listTree.size()-1).addChild(getTypeDefinitionsFromWSDL(documentWSDL, typeSplit[typeSplit.length-1], 1));
					} catch (SAXException | IOException | ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if(eleminside.getFirstChild() != null) {
				List<TreeNode<TreeNodeDataType>> listTreeChild = recursiveWSDLElement(eleminside.getFirstChild(), documentWSDL);
				for(TreeNode tree : listTreeChild) {
					listTree.add(tree);  
				}

			}

			if(eleminside.getNextSibling() != null) {
				List<TreeNode<TreeNodeDataType>> listTreeChild = recursiveWSDLElement(eleminside.getNextSibling(), documentWSDL);
				for(TreeNode tree : listTreeChild) {
					listTree.add(tree);  
				}

			}

		}

		return listTree;
	}


	
	
	
	public static TreeNode<TreeNodeDataType> cleanTree(TreeNode<TreeNodeDataType> treeNode) {
		
		if(treeNode == null)
			treeNode = new TreeNode<TreeNodeDataType>(new TreeNodeDataType(getSTARTING_TYPES_NODE(), getSTARTING_TYPES_NODE()));
		
		for(int i=0; i<treeNode.getNumberOfChildren(); i++) {
			TreeNode<TreeNodeDataType> tempNode = treeNode.getChildren().get(i);
			treeNode.removeChildAt(i);
			treeNode.insertChildAt(i, cleanTree(tempNode));
		}	
	
		
		
		if(treeNode.getNumberOfChildren() == 1) {//maybe is a fake children
			TreeNode<TreeNodeDataType> tempNode = treeNode.getChildren().get(0);
			if(tempNode.getData().getType().equals(getSTARTING_TYPES_NODE())) {
				treeNode.removeChildAt(0);
				for(int i=0; i<tempNode.getNumberOfChildren(); i++) {
					treeNode.addChild(tempNode.getChildren().get(i));
				}
			}
		}

		

			
		
		return treeNode;
	}

	public static List<String> getMessagesNamesFromWSDL(byte[] documentWSDL) throws SAXException, IOException, ParserConfigurationException {
		   
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(documentWSDL));

		//now i search for messages name (message name.. and message types input message e output message)
		ArrayList<String> validMessagesNames = new ArrayList<String>();
		NodeList elements2 = ((org.w3c.dom.Document) d).getElementsByTagName("message");
		
		if(elements2.getLength()==0)
			elements2 = ((org.w3c.dom.Document) d).getElementsByTagName("wsdl:message");
		
		for (int i = 0; i < elements2.getLength(); i++) {
			try {
				boolean isDuplicate=false;
				for(String duplicate : validMessagesNames) {
					if(elements2.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(duplicate)) {
						isDuplicate=true;
						break;
					}
				}
				if(!isDuplicate)
					validMessagesNames.add(elements2.item(i).getAttributes().getNamedItem("name").getNodeValue());
			}
			catch(Exception e) {
				//cannot find "name"
			}
		}
		
		return validMessagesNames;
	}

	
	public static List<String> getMessageNamesFromTaskWSDL(byte[] documentWSDL, String taskName, String whichKind) throws SAXException, IOException, ParserConfigurationException {
		   
		//with whichKind = 0 only the initiating, =1 only the receiving, with = 2 everything
		
	   	//now i search for operation name (=task name)
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(documentWSDL));

		ArrayList<String> validMessagesNames = new ArrayList<String>();
		NodeList elements = ((org.w3c.dom.Document) d).getElementsByTagName("operation");
		
		//verify if the wsdl is wsdl:operation
		if(elements.getLength() == 0)
			elements = ((org.w3c.dom.Document) d).getElementsByTagName("wsdl:operation");
		
		for (int i = 0; i < elements.getLength(); i++) {
			try {
				
				//for each operation i get the messages sender and receiver name from inside
				if(elements.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(taskName)) {
					//i have to look inside the node
					
					Node eleminside = elements.item(i).getFirstChild();
					do {
						if(eleminside==null)
							break;
						
						//is the input message
						if(whichKind.equals(getONLY_INITIATING()) || whichKind.equals(getBOTH_INITIATING_RECEIVING())) {
							if(eleminside.getNodeName().equals("input")) {
								String thisMessage = eleminside.getAttributes().getNamedItem("message").getNodeValue();
								String[] thisMessageSplit = thisMessage.split(":");
								validMessagesNames.add(thisMessageSplit[thisMessageSplit.length-1]);

							}
							else if(eleminside.getNodeName().equals("wsdl:input")) {
								String thisMessage = eleminside.getAttributes().getNamedItem("message").getNodeValue();
								String[] thisMessageSplit = thisMessage.split(":");
								validMessagesNames.add(thisMessageSplit[thisMessageSplit.length-1]);

							}
						}
						
						if(whichKind.equals(getONLY_RECEIVING()) || whichKind.equals(getBOTH_INITIATING_RECEIVING())) {
							if(eleminside.getNodeName().equals("output")) {
								String thisMessage = eleminside.getAttributes().getNamedItem("message").getNodeValue();
								String[] thisMessageSplit = thisMessage.split(":");
								validMessagesNames.add(thisMessageSplit[thisMessageSplit.length-1]);
								
							}	
							else if(eleminside.getNodeName().equals("wsdl:output")) {
								String thisMessage = eleminside.getAttributes().getNamedItem("message").getNodeValue();
								String[] thisMessageSplit = thisMessage.split(":");
								validMessagesNames.add(thisMessageSplit[thisMessageSplit.length-1]);
								
							}	
						}
						
						
					} while((eleminside = eleminside.getNextSibling()) != null);
					
				}
				
			}
			catch(Exception e) {
				//cannot find "name"	
			}
		}
		
		return validMessagesNames;
	}
	
	
	
	public static List<String> getFlattenedTreeFromTypeWSDL(byte[] documentWSDL, String typeToSearch) {
		   
		try {
	    	TreeNode<TreeNodeDataType> thisinput = getCompleteTypeDefinitionsFromWSDL(documentWSDL, typeToSearch);


			List<String> listFlattened = flatTree(thisinput, 0);
			return listFlattened;
			
	    } catch (SAXException | IOException | ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return null;
	}
	
	
	public static List<String> flatTree(TreeNode<TreeNodeDataType> treeNode, int level) {
		List<String> returnList = new ArrayList<String>();
		
		
		try {//can be empty
			returnList.add(Integer.toString(level)+":"+ treeNode.getData().getContent()+":"+treeNode.getData().getType()+":"+treeNode.getData().getMinOccurrences().toString()+":"+treeNode.getData().getMaxOccurrences().toString()+":"+treeNode.getData().getComplexOrSimpleType());
		}
		catch(Exception e) {
			//do nothing
		}
		
		for(TreeNode<TreeNodeDataType> node : treeNode.getChildren())
			returnList.addAll(flatTree(node, level+1));


		return returnList;
	}
	
	
	public static List<String> getMessagesTypesFromMessageWSDL(byte[] documentWSDL, String messageName) throws SAXException, IOException, ParserConfigurationException {
		   
	   	//now i search for operation name (=task name)
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(documentWSDL));

		ArrayList<String> validMessagesTypes = new ArrayList<String>();
		NodeList elements = ((org.w3c.dom.Document) d).getElementsByTagName("message");
		
		if(elements.getLength()==0)
			elements = ((org.w3c.dom.Document) d).getElementsByTagName("wsdl:message");
		
		for (int i = 0; i < elements.getLength(); i++) {
			try {
				
				if(elements.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(messageName)) {
					//i search for the part elements in the message
					
					Node eleminside = elements.item(i).getFirstChild();
					do {
						if(eleminside==null)
							break;
						
						//is the part where there is the type description
						if((eleminside.getNodeName().equals("part"))||(eleminside.getNodeName().equals("wsdl:part"))) {
							
							String thisTypeDefinition = eleminside.getAttributes().getNamedItem("element").getNodeValue();
							String[] thisTypeDefinitionSplit = thisTypeDefinition.split(":");
							
							//now i search for the definition on the top of the file
							Document d2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(documentWSDL));
							NodeList elements2 = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:element");

							String typeToFind = thisTypeDefinitionSplit[thisTypeDefinitionSplit.length-1];
							for(int j=0; j<elements2.getLength(); j++) {
								if(elements2.item(j).getAttributes().getNamedItem("name").getNodeValue().equals(typeToFind)) {
									
									String thisType = elements2.item(j).getAttributes().getNamedItem("type").getNodeValue();
									String[] thisTypeSplit = thisType.split(":");
									validMessagesTypes.add(thisTypeSplit[thisTypeSplit.length-1]);
									
									
								}
								
							}

						}

						
					} while((eleminside = eleminside.getNextSibling()) != null);
					
				}
				
				
			}
			catch(Exception e) {
				//cannot find "name"	
			}
		}
		
		return validMessagesTypes;
	}
	
	
	
	public static void assignOccurrencesToTree(byte[] wsdlDocument, TreeNode<TreeNodeDataType> treeNode) throws SAXException, IOException, ParserConfigurationException {
		
	   	//now i search for operation name (=task name)
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(wsdlDocument));

		//element
		NodeList elements = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:element");
		for (int i = 0; i < elements.getLength(); i++) {
			try {
				if(elements.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(treeNode.getData().getContent())) {					
					try {//for simple types its needed
						Node occurrence = elements.item(i);	

						if(occurrence.getAttributes().getNamedItem("maxOccurs").getNodeValue().equals("1"))
							treeNode.getData().setMaxOccurrences(OccurrencesTypes.ONE);
						else if(occurrence.getAttributes().getNamedItem("maxOccurs").getNodeValue().equals("0"))
							treeNode.getData().setMaxOccurrences(OccurrencesTypes.ZERO);
						else
							treeNode.getData().setMaxOccurrences(OccurrencesTypes.UNBOUNDED);

						
						if(occurrence.getAttributes().getNamedItem("minOccurs").getNodeValue().equals("1"))
							treeNode.getData().setMinOccurrences(OccurrencesTypes.ONE);
						else if(occurrence.getAttributes().getNamedItem("minOccurs").getNodeValue().equals("0"))
							treeNode.getData().setMinOccurrences(OccurrencesTypes.ZERO);
						else
							treeNode.getData().setMinOccurrences(OccurrencesTypes.UNBOUNDED);
					}
					catch(Exception e) {//here if is complex type
					
						Node occurrence = elements.item(i).getParentNode();	
			
						if(occurrence.getAttributes().getNamedItem("maxOccurs").getNodeValue().equals("1"))
							treeNode.getData().setMaxOccurrences(OccurrencesTypes.ONE);
						else if(occurrence.getAttributes().getNamedItem("maxOccurs").getNodeValue().equals("0"))
							treeNode.getData().setMaxOccurrences(OccurrencesTypes.ZERO);
						else
							treeNode.getData().setMaxOccurrences(OccurrencesTypes.UNBOUNDED);

						
						if(occurrence.getAttributes().getNamedItem("minOccurs").getNodeValue().equals("1"))
							treeNode.getData().setMinOccurrences(OccurrencesTypes.ONE);
						else if(occurrence.getAttributes().getNamedItem("minOccurs").getNodeValue().equals("0"))
							treeNode.getData().setMinOccurrences(OccurrencesTypes.ZERO);
						else
							treeNode.getData().setMinOccurrences(OccurrencesTypes.UNBOUNDED);
						
					}
				}
			}
			catch(Exception e) {}
		}		
		
		
		for(TreeNode<TreeNodeDataType> node : treeNode.getChildren())
			assignOccurrencesToTree(wsdlDocument, node);
		
	}
	
	
	public static void assignComplexOrSimpleToTree(byte[] wsdlDocument, TreeNode<TreeNodeDataType> treeNode) throws SAXException, IOException, ParserConfigurationException {
		
	   	//now i search for operation name (=task name)
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(wsdlDocument));
		
		//complextype
		NodeList elements = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:complexType");
		for (int i = 0; i < elements.getLength(); i++) {
			try {
				if(elements.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(treeNode.getData().getType())) {
					treeNode.getData().setComplexOrSimpleType("complexType");
				}
			}
			catch(Exception e) {}
		}
				
		//simpletype
		NodeList elements2 = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:simpleType");
		for (int i = 0; i < elements2.getLength(); i++) {
			try {
				if(elements2.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(treeNode.getData().getType())) {
					treeNode.getData().setComplexOrSimpleType("simpleType");		
				}
			}
			catch(Exception e) {}
		}
		

			//element not declared, search for it in elements
			NodeList elements3 = ((org.w3c.dom.Document) d).getElementsByTagName("xsd:element");
			for (int i = 0; i < elements3.getLength(); i++) {
				try {
					if(elements3.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(treeNode.getData().getContent())) {
						String[] typeSplit = elements3.item(i).getAttributes().getNamedItem("type").getNodeValue().split(":");
						if((typeSplit[0].equals("tns"))||(!typeSplit[0].equals("xsd"))) 
							treeNode.getData().setComplexOrSimpleType("complexType");		
						else
							treeNode.getData().setComplexOrSimpleType("simpleType");		
					}
				}
				catch(Exception e) {}
			}

		
		for(TreeNode<TreeNodeDataType> node : treeNode.getChildren())
			assignComplexOrSimpleToTree(wsdlDocument, node);
		
	}
	
	
	public static DataType getGIDLTreeFromTree(TreeNode<TreeNodeDataType> treeNode) {
		DataType toReturn = GidlFactoryImpl.eINSTANCE.createComplexType();
		
		try {
			if(treeNode.getData().getComplexOrSimpleType().equals("complexType"))
				toReturn = GidlFactoryImpl.eINSTANCE.createComplexType();
			else {
				toReturn = GidlFactoryImpl.eINSTANCE.createSimpleType();
				((SimpleType)toReturn).setType(getAssociatedSimpleType(treeNode.getData().getType()));
			}
				
			toReturn.setName(treeNode.getData().getContent());
			toReturn.setMinOccurs(treeNode.getData().getMaxOccurrences());
			toReturn.setMinOccurs(treeNode.getData().getMinOccurrences());
		}
		catch(Exception e) {}
		
		for(TreeNode<TreeNodeDataType> node : treeNode.getChildren()) {

			((ComplexType)toReturn).getHasDataType().add(getGIDLTreeFromTree(node));
		}
		
		
		return toReturn;
		
	}
	
	
	
	public static SimpleTypes getAssociatedSimpleType(String type) {
		
		SimpleTypes result = SimpleTypes.getByName(type);
		
		if(result == null) {

			//non-associated types
			if(type.equals("long"))
				return SimpleTypes.FLOAT;
			else if(type.equals("int"))
				return SimpleTypes.INTEGER;
			else if(type.equals("double"))
				return SimpleTypes.DOUBLE;	
			else if(type.equals("dateTime"))
				return SimpleTypes.DATE;//TODO verify this
			
		}
		
		return result;
		

	}
	
	

	
	// -----------------------------------------------------------------------

	/**
	 * <p>
	 * {@code WSDLUtils} instances should NOT be constructed in standard
	 * programming. Instead, the class should be used statically.
	 * </p>
	 *
	 * <p>
	 * This constructor is public to permit tools that require a JavaBean
	 * instance to operate.
	 * </p>
	 */
	public WSDLUtils() {
		super();
	}

}
