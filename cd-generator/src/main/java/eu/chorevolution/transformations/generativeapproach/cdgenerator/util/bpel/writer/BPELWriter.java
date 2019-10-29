/*
* Copyright 2016 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xerces.util.DOMUtil;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.Branches;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Compensate;
import org.eclipse.bpel.model.CompensateScope;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.CompletionCondition;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Correlation;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.CorrelationSets;
import org.eclipse.bpel.model.Correlations;
import org.eclipse.bpel.model.Documentation;
import org.eclipse.bpel.model.Else;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.Exit;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.Extension;
import org.eclipse.bpel.model.ExtensionActivity;
import org.eclipse.bpel.model.Extensions;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.FromPart;
import org.eclipse.bpel.model.FromParts;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Links;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.MessageExchanges;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.OpaqueActivity;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.PartnerLinks;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Query;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Rethrow;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.ServiceRef;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Sources;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.bpel.model.TerminationHandler;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.ToPart;
import org.eclipse.bpel.model.ToParts;
import org.eclipse.bpel.model.Validate;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.adapters.INamespaceMap;
import org.eclipse.bpel.model.extensions.BPELActivitySerializer;
import org.eclipse.bpel.model.extensions.BPELExtensionRegistry;
import org.eclipse.bpel.model.extensions.BPELExtensionSerializer;
import org.eclipse.bpel.model.extensions.ServiceReferenceSerializer;
import org.eclipse.bpel.model.messageproperties.Property;
import org.eclipse.bpel.model.partnerlinktype.PartnerLinkType;
import org.eclipse.bpel.model.partnerlinktype.Role;
import org.eclipse.bpel.model.proxy.IBPELServicesProxy;
import org.eclipse.bpel.model.util.BPELConstants;
import org.eclipse.bpel.model.util.BPELServicesUtility;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.Message;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import eu.chorevolution.transformations.generativeapproach.cdgenerator.model.BPELData;


/**
 * BPELWriter is responsible for serializing the BPEL EMF model to an output stream.
 */
@SuppressWarnings("nls")
public class BPELWriter {

	/**
	 * Prevents the BPEL writer from searching and adding automatically import declarations.
	 * <p>
	 * This option was added because the BPEL writer performs some actions that are
	 * not natural with a pure EMF approach. Using this option in the EMF serialization
	 * allows to by-pass these actions for the automatic resolution of imports.
	 * </p>
	 */
	private static final String XMLNS_PREFIX = "xmlns:";
	public static final String SKIP_AUTO_IMPORT = "bpel.skip.auto.import";
	static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private Document document = null;
	private BPELResource fBPELResource = null;
	protected BPELPackage bpelPackage = null;
	private final BPELExtensionRegistry extensionRegistry = BPELExtensionRegistry.getInstance();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BPELWriter.class);

	public BPELWriter() {
	}

	public BPELWriter(BPELResource resource, Document document) {
		this();
		this.fBPELResource = resource;
		this.document = document;
	}

	/**
	 * Return the resource used in this writer.
	 *
	 * @return the resource used in this writer.
	 */

	public BPELResource getResource() {
		return this.fBPELResource;
	}

	/**
	 * Convert the BPEL model to an XML DOM model and then write the DOM model
	 * to the output stream.
	 *
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceImpl#doSave(OutputStream,
	 *      Map)
	 */
	public void write(BPELData bpelData,File out)
			throws IOException {

		try {
			// Create a DOM document.
			final DocumentBuilderFactory documentBuilderFactory = 
					new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setValidating(false);
			DocumentBuilder builder = documentBuilderFactory
					.newDocumentBuilder();
			this.document = builder.newDocument();
			// Transform the EMF model to the DOM document.
			this.bpelPackage = BPELPackage.eINSTANCE;
			this.document = resource2XML(bpelData);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); 
            
            DOMSource source = new DOMSource(this.document);
            StreamResult sr = new StreamResult(out);
            transformer.transform(source, sr);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			ex.printStackTrace();
		}
	}

	protected Document resource2XML(BPELData bpelData) {

		Element procElement = process2XML(bpelData);
		this.document.appendChild(procElement);
		return this.document;
	}

	protected Element process2XML(BPELData bpelData) {
		
		Process process = bpelData.getProcess();
		Element processElement = createBPELElement("process");
		if (process.getName() != null)
			processElement.setAttribute("name", process.getName());
		if (process.getTargetNamespace() != null)
			processElement.setAttribute("targetNamespace", process
					.getTargetNamespace());
		if (process.isSetSuppressJoinFailure())
			processElement.setAttribute("suppressJoinFailure", BPELUtils
					.boolean2XML(process.getSuppressJoinFailure()));
		if (process.getExitOnStandardFault() != null)
			processElement.setAttribute("exitOnStandardFault", BPELUtils
					.boolean2XML(process.getExitOnStandardFault()));
		if (process.isSetVariableAccessSerializable())
			processElement.setAttribute("variableAccessSerializable", BPELUtils
					.boolean2XML(process.getVariableAccessSerializable()));
		if (process.isSetQueryLanguage())
			processElement.setAttribute("queryLanguage", process
					.getQueryLanguage());
		if (process.isSetExpressionLanguage())
			processElement.setAttribute("expressionLanguage", process
					.getExpressionLanguage());
		if (process.isSetAbstractProcessProfile())
			processElement.setAttribute("abstractProcessProfile", process
					.getAbstractProcessProfile());

		for (Entry<String, String> importedNamespaceData : bpelData.getImportedNamespacesData().entrySet()) {
			processElement.setAttribute(XMLNS_PREFIX+importedNamespaceData.getKey(),importedNamespaceData
					.getValue());
		}

		for (Object next : process.getImports()) {
			processElement
					.appendChild(import2XML((org.eclipse.bpel.model.Import) next));
		}

		if (process.getPartnerLinks() != null
				&& !process.getPartnerLinks().getChildren().isEmpty())
			processElement.appendChild(partnerLinks2XML(process
					.getPartnerLinks()));

		if (process.getVariables() != null
				&& !process.getVariables().getChildren().isEmpty())
			processElement.appendChild(variables2XML(process.getVariables()));

		if (process.getCorrelationSets() != null
				&& !process.getCorrelationSets().getChildren().isEmpty())
			processElement.appendChild(correlationSets2XML(process
					.getCorrelationSets()));

		if (process.getExtensions() != null)
			processElement.appendChild(extensions2XML(process.getExtensions()));

		if (process.getFaultHandlers() != null)
			processElement.appendChild(faultHandlers2XML(process
					.getFaultHandlers()));

		if (process.getEventHandlers() != null)
			processElement.appendChild(eventHandler2XML(process
					.getEventHandlers()));

		if (process.getMessageExchanges() != null
				&& !process.getMessageExchanges().getChildren().isEmpty())
			processElement.appendChild(messageExchanges2XML(process
					.getMessageExchanges()));

		if (process.getActivity() != null)
			processElement.appendChild(activity2XML(process.getActivity()));

		extensibleElement2XML(process, processElement);

		return processElement;
	}

	protected QName getQName(EObject object) {
		QName qname = null;

		if (object.eIsProxy() && object instanceof IBPELServicesProxy) {
			qname = ((IBPELServicesProxy) object).getQName();
		} else if (object instanceof PartnerLinkType) {
			qname = BPELServicesUtility.getQName((PartnerLinkType) object);
		} else if (object instanceof Property) {
			qname = BPELServicesUtility.getQName((Property) object);
		}

		return qname;
	}

	protected String getOperationSignature(Operation op) {
		String signature = "";
		if (op != null) {
			signature = op.getName();
		}
		return signature;
	}

	protected Element import2XML(org.eclipse.bpel.model.Import imp) {
		
		Element importElement = createBPELElement("import");
		if (imp.getNamespace() != null) {
			importElement.setAttribute("namespace", imp.getNamespace());
		}
		if (imp.getLocation() != null) {
			importElement.setAttribute("location", imp.getLocation());
		}
		if (imp.getImportType() != null) {
			importElement.setAttribute("importType", imp.getImportType());
		}
		return importElement;
	}

	protected Element partnerLinks2XML(PartnerLinks partnerLinks) {
		
		// If there are no partner links then skip creating Element
		if (partnerLinks.getChildren().isEmpty())
			return null;
		Element partnerLinksElement = createBPELElement("partnerLinks");
		for (Object next : partnerLinks.getChildren()) {
			partnerLinksElement
					.appendChild(partnerLink2XML((PartnerLink) next));
		}
		extensibleElement2XML(partnerLinks, partnerLinksElement);
		return partnerLinksElement;
	}

	protected Element partnerLink2XML(PartnerLink partnerLink) {
		
		Element partnerLinkElement = createBPELElement("partnerLink");
		if (partnerLink.getName() != null) {
			partnerLinkElement.setAttribute("name", partnerLink.getName());
		}
		if (partnerLink.isSetInitializePartnerRole())
			partnerLinkElement.setAttribute("initializePartnerRole", BPELUtils
					.boolean2XML(partnerLink.getInitializePartnerRole()));
		PartnerLinkType plt = partnerLink.getPartnerLinkType();
		if (plt != null) {
			String qnameStr = qNameToString(partnerLink, getQName(plt));
			partnerLinkElement.setAttribute("partnerLinkType", qnameStr);
			Role myRole = partnerLink.getMyRole();
			if (myRole != null) {
				partnerLinkElement.setAttribute("myRole", myRole.getName());
			}
			Role partnerRole = partnerLink.getPartnerRole();
			if (partnerRole != null) {
				partnerLinkElement.setAttribute("partnerRole", partnerRole
						.getName());
			}
		}
		extensibleElement2XML(partnerLink, partnerLinkElement);
		return partnerLinkElement;
	}

	protected Element variables2XML(Variables variables) {
		
		// If there are no variables then skip creating Element
		if (variables.getChildren().isEmpty())
			return null;
		Element variablesElement = createBPELElement("variables");
		for (Object next : variables.getChildren()) {
			Variable variable = (Variable) next;
			variablesElement.appendChild(variable2XML(variable));
		}
		extensibleElement2XML(variables, variablesElement);
		return variablesElement;
	}

	protected Element variable2XML(Variable variable) {
		
		Element variableElement = createBPELElement("variable");
		if (variable.getName() != null) {
			variableElement.setAttribute("name", variable.getName());
		}
		Message msg = variable.getMessageType();
		if (msg != null) {
			variableElement.setAttribute("messageType", msg.getQName()
					.getNamespaceURI()+":"+msg.getQName().getLocalPart());			
		}
		if (variable.getType() != null) {
			XSDTypeDefinition type = variable.getType();
			QName qname = new QName(type.getTargetNamespace(), type.getName());
			variableElement.setAttribute("type", qNameToString(variable, qname));
		}
		if (variable.getXSDElement() != null) {
			XSDElementDeclaration element = variable.getXSDElement();
			QName qname = new QName(element.getTargetNamespace(), element.getName());
			variableElement.setAttribute("element", qNameToString(variable, qname));
		}
		// from-spec
		From from = variable.getFrom();
		if (from != null) {
			Element fromElement = createBPELElement("from");
			from2XML(from, fromElement);
			variableElement.appendChild(fromElement);
		}
		extensibleElement2XML(variable, variableElement);
		return variableElement;
	}

	protected Element fromPart2XML(FromPart fromPart) {
		Element fromPartElement = createBPELElement("fromPart");

		if (fromPart.getPart() != null) {
			fromPartElement.setAttribute("part", fromPart.getPart().getName());
		}
		if (fromPart.getToVariable() != null) {
			fromPartElement.setAttribute("toVariable", fromPart.getToVariable()
					.getName());
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(fromPart, fromPartElement);
		return fromPartElement;
	}

	protected Element toPart2XML(ToPart toPart) {
		
		Element toPartElement = createBPELElement("toPart");
		if (toPart.getPart() != null) {
			toPartElement.setAttribute("part", toPart.getPart().getName());
		}
		if (toPart.getFromVariable() != null) {
			toPartElement.setAttribute("fromVariable", toPart.getFromVariable()
					.getName());
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(toPart, toPartElement);
		return toPartElement;
	}

	protected Element extensions2XML(Extensions extensions) {
		
		Element extensionsElement = createBPELElement("extensions");
		Iterator<?> it = extensions.getChildren().iterator();
		while (it.hasNext()) {
			Extension extension = (Extension) it.next();
			extensionsElement.appendChild(extension2XML(extension));
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(extensions, extensionsElement);
		extensibleElement2XML(extensions, extensionsElement);
		return extensionsElement;
	}

	protected Element extension2XML(Extension extension) {
		
		Element extensionElement = createBPELElement("extension");
		if (extension.getNamespace() != null) {
			extensionElement
					.setAttribute("namespace", extension.getNamespace());
		}
		if (extension.isSetMustUnderstand()) {
			extensionElement.setAttribute("mustUnderstand", BPELUtils
					.boolean2XML(extension.getMustUnderstand()));
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(extension, extensionElement);
		extensibleElement2XML(extension, extensionElement);
		return extensionElement;
	}

	protected Element correlationSets2XML(CorrelationSets correlationSets) {
		
		// If there are no correlation sets then skip creating Element
		if (correlationSets.getChildren().isEmpty())
			return null;
		Element correlationSetsElement = createBPELElement("correlationSets");
		Iterator<?> it = correlationSets.getChildren().iterator();
		while (it.hasNext()) {
			CorrelationSet correlationSet = (CorrelationSet) it.next();
			correlationSetsElement
					.appendChild(correlationSet2XML(correlationSet));
		}
		extensibleElement2XML(correlationSets, correlationSetsElement);
		return correlationSetsElement;
	}

	protected Element correlationSet2XML(CorrelationSet correlationSet) {
		
		Element correlationSetElement = createBPELElement("correlationSet");
		if (correlationSet.getName() != null) {
			correlationSetElement
					.setAttribute("name", correlationSet.getName());
		}
		String propertiesList = properties2XML(correlationSet);
		if (propertiesList.length() > 0) {
			correlationSetElement.setAttribute("properties", propertiesList);
		}
		extensibleElement2XML(correlationSet, correlationSetElement);
		return correlationSetElement;
	}

	protected String properties2XML(CorrelationSet correlationSet) {
		
		StringBuffer propertiesList = new StringBuffer();
		Iterator<?> properties = correlationSet.getProperties().iterator();
		while (properties.hasNext()) {
			Property property = (Property) properties.next();
			String qnameStr = qNameToString(correlationSet, getQName(property));
			propertiesList.append(qnameStr);
			if (properties.hasNext())
				propertiesList.append(" ");
		}
		return propertiesList.toString();
	}

	protected Element messageExchanges2XML(MessageExchanges messageExchanges) {
		
		// If there are no messageExchanges then skip creating Element
		if (messageExchanges.getChildren().isEmpty())
			return null;
		Element messageExchangesElement = createBPELElement("messageExchanges");
		for (Object next : messageExchanges.getChildren()) {
			messageExchangesElement
					.appendChild(messageExchange2XML((MessageExchange) next));
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(messageExchanges, messageExchangesElement);
		extensibleElement2XML(messageExchanges, messageExchangesElement);
		return messageExchangesElement;
	}

	protected Element messageExchange2XML(MessageExchange messageExchange) {
		Element messageExchangeElement = createBPELElement("messageExchange");

		if (messageExchange.getName() != null)
			messageExchangeElement.setAttribute("name", messageExchange.getName());
		// serialize local namespace prefixes to XML
		serializePrefixes(messageExchange, messageExchangeElement);
		extensibleElement2XML(messageExchange, messageExchangeElement);
		return messageExchangeElement;
	}

	protected Element fromParts2XML(FromParts fromParts) {
		Element fromPartsElement = createBPELElement("fromParts");

		for (Object next : fromParts.getChildren()) {
			FromPart fromPart = (FromPart) next;
			fromPartsElement.appendChild(fromPart2XML(fromPart));
		}
		extensibleElement2XML(fromParts, fromPartsElement);
		// serialize local namespace prefixes to XML
		serializePrefixes(fromParts, fromPartsElement);
		extensibleElement2XML(fromParts, fromPartsElement);
		return fromPartsElement;
	}

	protected Element toParts2XML(ToParts toParts) {
		
		Element toPartsElement = createBPELElement("toParts");
		for (Object next : toParts.getChildren()) {
			ToPart toPart = (ToPart) next;
			toPartsElement.appendChild(toPart2XML(toPart));
		}
		extensibleElement2XML(toParts, toPartsElement);
		// serialize local namespace prefixes to XML
		serializePrefixes(toParts, toPartsElement);
		extensibleElement2XML(toParts, toPartsElement);
		return toPartsElement;
	}

	protected Element correlations2XML(Correlations correlations) {
		
		Element correlationsElement = createBPELElement("correlations");
		Iterator<?> it = correlations.getChildren().iterator();
		while (it.hasNext()) {
			Correlation correlation = (Correlation) it.next();
			correlationsElement.appendChild(correlation2XML(correlation));
		}
		extensibleElement2XML(correlations, correlationsElement);
		return correlationsElement;
	}

	protected Element correlation2XML(Correlation correlation) {
		
		Element correlationElement = createBPELElement("correlation");
		if (correlation.getSet() != null && correlation.getSet().getName() != null)
			correlationElement.setAttribute("set", correlation.getSet().getName());
		if (correlation.isSetInitiate())
			correlationElement.setAttribute("initiate", correlation.getInitiate());
		if (correlation.isSetPattern())
			// Bugzilla 340654
			correlationElement.setAttribute("pattern", correlation.getPattern().getLiteral());
		extensibleElement2XML(correlation, correlationElement);
		return correlationElement;
	}

	protected Element faultHandlers2XML(FaultHandler faultHandler) {
		
		Element faultHandlersElement = createBPELElement("faultHandlers");
		faultHandler2XML(faultHandlersElement, faultHandler);
		// serialize local namespace prefixes to XML
		serializePrefixes(faultHandler, faultHandlersElement);
		extensibleElement2XML(faultHandler, faultHandlersElement);
		return faultHandlersElement;
	}

	protected void faultHandler2XML(Element parentElement,
			FaultHandler faultHandler) {
		
		Iterator<?> catches = faultHandler.getCatch().iterator();
		while (catches.hasNext()) {
			Catch _catch = (Catch) catches.next();
			parentElement.appendChild(catch2XML(_catch));
		}
		if (faultHandler.getCatchAll() != null) {
			parentElement.appendChild(catchAll2XML(faultHandler.getCatchAll()));
		}
	}

	protected Element compensationHandler2XML(CompensationHandler compensationHandler) {
		
		Element compensationHandlerElement = createBPELElement("compensationHandler");
		if (compensationHandler.getActivity() != null) {
			Element activityElement = activity2XML(compensationHandler.getActivity());
			compensationHandlerElement.appendChild(activityElement);
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(compensationHandler, compensationHandlerElement);
		extensibleElement2XML(compensationHandler, compensationHandlerElement);
		return compensationHandlerElement;
	}

	protected Element terminationHandler2XML(TerminationHandler terminationHandler) {
		
		Element terminationHandlerElement = createBPELElement("terminationHandler");
		if (terminationHandler.getActivity() != null) {
			Element activityElement = activity2XML(terminationHandler.getActivity());
			terminationHandlerElement.appendChild(activityElement);
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(terminationHandler, terminationHandlerElement);
		extensibleElement2XML(terminationHandler, terminationHandlerElement);
		return terminationHandlerElement;
	}

	protected Element eventHandler2XML(EventHandler eventHandler) {
		
		Element eventHandlerElement = createBPELElement("eventHandlers");
		// For backwards compatibility with 1.1 we should serialize
		// OnMessages here.
		for (Object name : eventHandler.getEvents()) {
			OnEvent onEvent = (OnEvent) name;
			eventHandlerElement.appendChild(onEvent2XML(onEvent));
		}
		for (Object name : eventHandler.getAlarm()) {
			OnAlarm onAlarm = (OnAlarm) name;
			eventHandlerElement.appendChild(onAlarm2XML(onAlarm));
		}
		// serialize local namespace prefixes to XML
		serializePrefixes(eventHandler, eventHandlerElement);
		extensibleElement2XML(eventHandler, eventHandlerElement);
		return eventHandlerElement;
	}

	public Element activity2XML(Activity activity) {

		Element activityElement = null;
		
		if (activity instanceof ExtensionActivity)
			activityElement = extensionActivity2XML((ExtensionActivity) activity);
		else if (activity instanceof Empty)
			activityElement = empty2XML((Empty) activity);
		else if (activity instanceof Invoke)
			activityElement = invoke2XML((Invoke) activity);
		else if (activity instanceof Assign)
			activityElement = assign2XML((Assign) activity);
		else if (activity instanceof Reply)
			activityElement = reply2XML((Reply) activity);
		else if (activity instanceof Receive)
			activityElement = receive2XML((Receive) activity);
		else if (activity instanceof Wait)
			activityElement = wait2XML((Wait) activity);
		else if (activity instanceof Throw)
			activityElement = throw2XML((Throw) activity);
		else if (activity instanceof Exit)
			activityElement = exit2XML((Exit) activity);
		else if (activity instanceof Flow)
			activityElement = flow2XML((Flow) activity);
		else if (activity instanceof If)
			activityElement = if2XML((If) activity);
		else if (activity instanceof While)
			activityElement = while2XML((While) activity);
		else if (activity instanceof Sequence)
			activityElement = sequence2XML((Sequence) activity);
		else if (activity instanceof Pick)
			activityElement = pick2XML((Pick) activity);
		else if (activity instanceof Scope)
			activityElement = scope2XML((Scope) activity);
		else if (activity instanceof Compensate)
			activityElement = compensate2XML((Compensate) activity);
		else if (activity instanceof CompensateScope)
			activityElement = compensateScope2XML((CompensateScope) activity);
		else if (activity instanceof Rethrow)
			activityElement = rethrow2XML((Rethrow) activity);
		else if (activity instanceof OpaqueActivity)
			activityElement = opaqueActivity2XML((OpaqueActivity) activity);
		else if (activity instanceof ForEach)
			activityElement = forEach2XML((ForEach) activity);
		else if (activity instanceof RepeatUntil)
			activityElement = repeatUntil2XML((RepeatUntil) activity);
		else if (activity instanceof Validate)
			activityElement = validate2XML((Validate) activity);

		return activityElement;
	}

	protected Element addCommonActivityItems(Element activityElement,
			Activity activity) {

		addStandardAttributes(activityElement, activity);
		addStandardElements(activityElement, activity);
		extensibleElement2XML(activity, activityElement);
		return activityElement;
	}

	protected void addStandardAttributes(Element activityElement, Activity activity) {
		
		if (activity.getName() != null)
			activityElement.setAttribute("name", activity.getName());
		if (activity.isSetSuppressJoinFailure()) {
			activityElement.setAttribute("suppressJoinFailure", BPELUtils
					.boolean2XML(activity.getSuppressJoinFailure()));
		}
	}

	protected void addStandardElements(Element activityElement, Activity activity) {
		
		// NOTE: Mind the order of these elements.
		Node firstChild = activityElement.getFirstChild();
		Targets targets = activity.getTargets();
		if (targets != null) {
			activityElement.insertBefore(targets2XML(targets), firstChild);
		}
		Sources sources = activity.getSources();
		if (sources != null) {
			activityElement.insertBefore(sources2XML(sources), firstChild);
		}
	}

	protected Element catch2XML(Catch _catch) {
		
		Element catchElement = createBPELElement("catch");
		if (_catch.getFaultName() != null) {
			catchElement.setAttribute("faultName", qNameToString(_catch, _catch
					.getFaultName()));
		}
		if (_catch.getFaultVariable() != null) {
			catchElement.setAttribute("faultVariable", _catch
					.getFaultVariable().getName());
		}
		if (_catch.getFaultMessageType() != null) {
			catchElement.setAttribute("faultMessageType", qNameToString(_catch,
					_catch.getFaultMessageType().getQName()));
		}
		if (_catch.getFaultElement() != null) {
			XSDElementDeclaration element = _catch.getFaultElement();
			QName qname = new QName(element.getTargetNamespace(), element
					.getName());
			catchElement.setAttribute("faultElement", qNameToString(_catch,
					qname));
		}
		if (_catch.getActivity() != null) {
			catchElement.appendChild(activity2XML(_catch.getActivity())); 
		}
		extensibleElement2XML(_catch, catchElement);
		return catchElement;
	}

	protected Element catchAll2XML(CatchAll catchAll) {
		
		Element catchAllElement = createBPELElement("catchAll");
		Activity activity = catchAll.getActivity();
		if (activity != null)
			catchAllElement.appendChild(activity2XML(activity));
		extensibleElement2XML(catchAll, catchAllElement);
		return catchAllElement;
	}

	protected Element empty2XML(Empty empty) {
		
		Element activityElement = createBPELElement("empty");
		addCommonActivityItems(activityElement, empty);
		return activityElement;
	}

	protected Element opaqueActivity2XML(OpaqueActivity activity) {
		
		Element activityElement = createBPELElement("opaqueActivity");
		//Set Namespace to Abstract Process
		INamespaceMap<String, String> nsMap = BPELUtils.getNamespaceMap(this.fBPELResource.getProcess());
		if (this.fBPELResource.getOptionUseNSPrefix()) {
        	nsMap.remove("");
            List<String> prefix = nsMap.getReverse(this.fBPELResource.getNamespaceURI());
            if (prefix.isEmpty()){
            	nsMap.put(BPELConstants.PREFIX, BPELConstants.NAMESPACE_ABSTRACT_2007);
            } else {
            	// Which prefix?
            	nsMap.put(prefix.get(0), BPELConstants.NAMESPACE_ABSTRACT_2007);
            }
    	} else {
            nsMap.put("", BPELConstants.NAMESPACE_ABSTRACT_2007);
        }

		this.fBPELResource.setNamespaceURI(BPELConstants.NAMESPACE_ABSTRACT_2007);

		Process process = this.fBPELResource.getProcess();
		//Set Default Abstract Process Profile
			if (!process.isSetAbstractProcessProfile()){
				process.setAbstractProcessProfile(BPELConstants.NAMESPACE_ABSTRACT_PROFILE_T);
			}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element forEach2XML(ForEach forEach) {

		Element activityElement = createBPELElement("forEach");
		if (forEach.getParallel() != null)
			activityElement.setAttribute("parallel", BPELUtils.boolean2XML(forEach.getParallel()));
		if (forEach.getCounterName() != null) {
			activityElement.setAttribute("counterName", forEach.getCounterName().getName());
		}
		if (forEach.getStartCounterValue() != null) {
			activityElement.appendChild(expression2XML(forEach.getStartCounterValue(), "startCounterValue"));
		}
		if (forEach.getFinalCounterValue() != null) {
			activityElement.appendChild(expression2XML(forEach.getFinalCounterValue(), "finalCounterValue"));
		}
		CompletionCondition completionCondition = forEach.getCompletionCondition();
		if (completionCondition != null) {
			Element completionConditionElement = completionCondition2XML(completionCondition);
			activityElement.appendChild(completionConditionElement);
		}
		if (forEach.getActivity() != null) {
			activityElement.appendChild(activity2XML(forEach.getActivity()));
		}
		addCommonActivityItems(activityElement, forEach);
		return activityElement;
	}

	protected Element completionCondition2XML(CompletionCondition completionCondition) {
		
		Element completionConditionElement = createBPELElement("completionCondition");
		if (completionCondition.getBranches() != null) {
			Element branchesElement = branches2XML(completionCondition.getBranches());
			completionConditionElement.appendChild(branchesElement);
		}
		return completionConditionElement;
	}

	protected Element branches2XML(Branches branches) {
		
		Element branchesElement = expression2XML(branches, "branches");
		if (branches.isSetCountCompletedBranchesOnly())
			branchesElement.setAttribute("successfulBranchesOnly", BPELUtils
					.boolean2XML(branches.getCountCompletedBranchesOnly()));
		return branchesElement;
	}

	protected Element rethrow2XML(Rethrow activity) {
		
		Element activityElement = createBPELElement("rethrow");
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element validate2XML(Validate activity) {
		
		Element activityElement = createBPELElement("validate");
		StringBuilder variablesList = new StringBuilder();
		Iterator<?> variables = activity.getVariables().iterator();
		while (variables.hasNext()) {
			Variable variable = (Variable) variables.next();
			variablesList.append(variable.getName());
			if (variables.hasNext())
				variablesList.append(" ");
		}
		if (variablesList.length() > 0) {
			activityElement.setAttribute("variables", variablesList.toString());
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element extensionActivity2XML(ExtensionActivity activity) {

		Element activityElement = createBPELElement("extensionActivity");
		String localName = activity.eClass().getName();
		String namespace = activity.eClass().getEPackage().getNsURI();
		QName qName = new QName(namespace, localName);
		BPELActivitySerializer serializer = this.extensionRegistry.getActivitySerializer(qName);
		DocumentFragment fragment = null;
		if (serializer != null) {
			fragment = this.document.createDocumentFragment();
			Element child = getFirstChildElement(fragment);
			if (child!=null) {
				activityElement.appendChild(child);
			}
		}
		return activityElement;
	}

	protected Element invoke2XML(Invoke activity) {

		Element activityElement = createBPELElement("invoke");

		if (activity.getPartnerLink() != null)
			activityElement.setAttribute("partnerLink", activity.getPartnerLink().getName());
		if (activity.getOperation() != null)
			activityElement.setAttribute("operation", getOperationSignature(activity.getOperation()));
		if (activity.getInputVariable() != null)
			activityElement.setAttribute("inputVariable", activity.getInputVariable().getName());
		if (activity.getOutputVariable() != null)
			activityElement.setAttribute("outputVariable", activity.getOutputVariable().getName());
		if (activity.getCorrelations() != null)
			activityElement.appendChild(correlations2XML(activity.getCorrelations()));
		FaultHandler faultHandler = activity.getFaultHandler();
		if (faultHandler != null) {
			faultHandler2XML(activityElement, faultHandler);
		}
		if (activity.getCompensationHandler() != null)
			activityElement.appendChild(compensationHandler2XML(activity.getCompensationHandler()));
		if (activity.getFromParts() != null)
			activityElement.appendChild(fromParts2XML(activity.getFromParts()));
		if (activity.getToParts() != null)
			activityElement.appendChild(toParts2XML(activity.getToParts()));
		addCommonActivityItems(activityElement, activity);
		
		return activityElement;
	}

	protected Element receive2XML(Receive activity) {

		Element activityElement = createBPELElement("receive");

		if (activity.getPartnerLink() != null)
			activityElement.setAttribute("partnerLink", activity.getPartnerLink().getName());
		if (activity.getPortType() != null)
			activityElement.setAttribute("portType", qNameToString(activity, activity.getPortType()
					.getQName()));
		if (activity.getOperation() != null)
			activityElement.setAttribute("operation",
					getOperationSignature(activity.getOperation()));
		if (activity.getVariable() != null)
			activityElement.setAttribute("variable", activity.getVariable().getName());
		if (activity.isSetCreateInstance())
			activityElement.setAttribute("createInstance", BPELUtils.boolean2XML(activity
					.getCreateInstance()));
		if (activity.getMessageExchange() != null)
			activityElement.setAttribute("messageExchange", activity.getMessageExchange().getName());
		if (activity.getCorrelations() != null)
			activityElement.appendChild(correlations2XML(activity.getCorrelations()));
		if (activity.getFromParts() != null)
			activityElement.appendChild(fromParts2XML(activity.getFromParts()));

		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element reply2XML(Reply activity) {

		Element activityElement = createBPELElement("reply");
		
		if (activity.getPartnerLink() != null)
			activityElement.setAttribute("partnerLink", activity.getPartnerLink().getName());
		if (activity.getPortType() != null)
			activityElement.setAttribute("portType", qNameToString(activity, activity.getPortType()
					.getQName()));
		if (activity.getOperation() != null)
			activityElement.setAttribute("operation", getOperationSignature(activity.getOperation()));
		if (activity.getVariable() != null)
			activityElement.setAttribute("variable", activity.getVariable().getName());
		if (activity.getFaultName() != null) {
			activityElement.setAttribute("faultName", qNameToString(activity, activity.getFaultName()));
		}
		if (activity.getMessageExchange() != null)
			activityElement.setAttribute("messageExchange", activity.getMessageExchange().getName());
		if (activity.getCorrelations() != null)
			activityElement.appendChild(correlations2XML(activity.getCorrelations()));
		if (activity.getToParts() != null)
			activityElement.appendChild(toParts2XML(activity.getToParts()));

		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element assign2XML(Assign activity) {

		Element activityElement = createBPELElement("assign");
		if (activity.getValidate() != null)
			activityElement.setAttribute("validate", BPELUtils.boolean2XML(activity.getValidate()));
		List<?> copies = activity.getCopy();
		if (!copies.isEmpty()) {
			for (Object name : copies) {
				Copy copy = (Copy) name;
				activityElement.appendChild(copy2XML(copy));
			}
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element copy2XML(Copy copy) {
		
		Element copyElement = createBPELElement("copy");
		if (copy.isSetKeepSrcElementName())
			copyElement.setAttribute("keepSrcElementName", BPELUtils.boolean2XML(copy
					.getKeepSrcElementName()));
		if (copy.isSetIgnoreMissingFromData())
			copyElement.setAttribute("ignoreMissingFromData", BPELUtils.boolean2XML(copy
					.getIgnoreMissingFromData()));
		From from = copy.getFrom();
		if (from != null) {
			Element fromElement = createBPELElement("from");
			from2XML(from, fromElement);
			copyElement.appendChild(fromElement);
		}
		To to = copy.getTo();
		if (to != null) {
			Element toElement = createBPELElement("to");
			to2XML(to, toElement);
			copyElement.appendChild(toElement);
		}

		if(copy.getElement() != null){
			for (int i = 0; i < copy.getElement().getAttributes().getLength(); i++) {
				Node attributeNode = copy.getElement().getAttributes().item(i);
				copyElement.setAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
			}
		}
		
		extensibleElement2XML(copy, copyElement);

		return copyElement;
	}

	
	public static Node convertStringToNode(EObject parent, String s, BPELResource bpelResource) {
		
		// Create DOM document
		DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		StringBuilder namespaces = new StringBuilder();
		String namespaceURI = bpelResource.getNamespaceURI();
		if (bpelResource.getOptionUseNSPrefix()) {
			String prefix = "bpel";
			s = "<" + prefix + ":from xmlns:" + prefix + "=\"" + namespaceURI + "\" "
					+ namespaces.toString() + ">" + s + "</" + prefix + ":from>";
		} else {
			s = "<from xmlns=\"" + namespaceURI + "\" " + namespaces.toString() + ">" + s + "</from>";
		}	
		try {
			StringReader sr = new StringReader(s);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource source = new InputSource(sr);
			source.setEncoding("UTF8");
			Document document = builder.parse(source);
			return document.getDocumentElement();
		} catch (Exception e) {
			return null;
		}

	}

		
	protected void from2XML(From from, Element fromElement) {

		if (from.getVariable() != null) {
			fromElement.setAttribute("variable", from.getVariable().getName());
		}
		if (from.getPart() != null) {
			fromElement.setAttribute("part", from.getPart().getName());
		}
		if (from.getPartnerLink() != null) {
			fromElement.setAttribute("partnerLink", from.getPartnerLink().getName());
		}
		Property property = from.getProperty();
		if (property != null) {
			String qnameStr = qNameToString(from, getQName(property));
			fromElement.setAttribute("property", qnameStr);
		}

		if (from.getQuery() != null) {
			Query query = from.getQuery();
			Element queryElement = query2XML(query);
			fromElement.appendChild(queryElement);
		}

		if (from.isSetEndpointReference()) {
			fromElement.setAttribute("endpointReference", from
					.getEndpointReference().toString());
		}

		if (from.isSetOpaque()) {
			fromElement.setAttribute("opaque", BPELUtils.boolean2XML(from
					.getOpaque()));
		}

		if (from.isSetLiteral() && from.getLiteral() != null
				&& !from.getLiteral().equals("")) {

			Node node = null;
			Element literal = createBPELElement("literal");

			fromElement.appendChild(literal);

			if (Boolean.TRUE.equals(from.getUnsafeLiteral())) {
				node = BPELWriter.convertStringToNode(from, from.getLiteral(),
				(eu.chorevolution.transformations.generativeapproach.cdgenerator.util
						.bpel.writer.BPELResource) getResource());
			}

			if (node != null) {
				for (Node child = node.getFirstChild(); child != null; child = child
						.getNextSibling()) {					
					DOMUtil.copyInto(child, literal);
				}
			} else {
				CDATASection cdata = BPELUtils.createCDATASection(this.document,
						from.getLiteral());
				fromElement.appendChild(cdata);

			}

		}

		if (from.getServiceRef() != null) {
			fromElement.appendChild(serviceRef2XML(from.getServiceRef()));
		}

		if (from.getExpression() != null) {
			Expression expression = from.getExpression();

			if (expression.getExpressionLanguage() != null) {
				fromElement.setAttribute("expressionLanguage", expression
						.getExpressionLanguage());
			}
			if (expression.getBody() != null) {
				CDATASection cdata = BPELUtils.createCDATASection(this.document,
						(String) expression.getBody());
				fromElement.appendChild(cdata);
			}
		}

		if (from.getType() != null) {
			XSDTypeDefinition type = from.getType();
			QName qname = new QName(type.getTargetNamespace(), type.getName());
			fromElement.setAttribute("xsi:type", qNameToString(from, qname));
		}

		extensibleElement2XML(from, fromElement);

	}

	protected Element serviceRef2XML(ServiceRef serviceRef) {
		
		Element serviceRefElement = createBPELElement("service-ref");
		String referenceScheme = serviceRef.getReferenceScheme();
		if (referenceScheme != null) {
			serviceRefElement.setAttribute("reference-scheme", referenceScheme);
		}
		if (serviceRef.getValue() != null) {
			Node valueNode = serviceRefValue2XML(serviceRef);
			if (valueNode != null) {
				serviceRefElement.appendChild(valueNode);
			}
		}
		return serviceRefElement;
	}

	protected Node serviceRefValue2XML(ServiceRef serviceRef) {
		
		Object value = serviceRef.getValue();
		if (value instanceof ExtensibilityElement) {
			ExtensibilityElement extensibilityElement = (ExtensibilityElement) value;
			BPELExtensionSerializer serializer = null;
			QName qname = extensibilityElement.getElementType();
			try {
				serializer = (BPELExtensionSerializer) this.extensionRegistry
						.querySerializer(BPELExtensibleElement.class, qname);
			} catch (WSDLException e) {
				LOGGER.error(e.getMessage(), e);
			}

			if (serializer != null) {
				DocumentFragment fragment = this.document.createDocumentFragment();
				Element child = getFirstChildElement(fragment);
				return child;
			}
		} else if (serviceRef.getValue() != null) {
			ServiceReferenceSerializer serializer = this.extensionRegistry
					.getServiceReferenceSerializer(serviceRef.getReferenceScheme());
			if (serializer != null) {
				DocumentFragment fragment = this.document.createDocumentFragment();
				Element child = getFirstChildElement(fragment);
				return child;
			} else {
				CDATASection cdata = BPELUtils.createCDATASection(this.document,
						serviceRef.getValue().toString());
				return cdata;
			}
		}
		return null;
	}

	protected Element query2XML(Query query) {
		
		Element queryElement = createBPELElement("query");
		if (query.getQueryLanguage() != null) {
			queryElement.setAttribute("queryLanguage", query.getQueryLanguage());
		}
		if (query.getValue() != null) {
			CDATASection cdata = BPELUtils.createCDATASection(this.document, query.getValue());
			queryElement.appendChild(cdata);
		}
		return queryElement;
	}

	protected void to2XML(To to, Element toElement) {
		
		if (to.getVariable() != null) {
			toElement.setAttribute("variable", to.getVariable().getName());
		}
		if (to.getPart() != null) {
			toElement.setAttribute("part", to.getPart().getName());
		}
		if (to.getPartnerLink() != null) {
			toElement
					.setAttribute("partnerLink", to.getPartnerLink().getName());
		}
		Property property = to.getProperty();
		if (property != null) {
			String qnameStr = qNameToString(to, getQName(property));
			toElement.setAttribute("property", qnameStr);
		}

		if (to.getQuery() != null) {
			Query query = to.getQuery();
			Element queryElement = query2XML(query);
			toElement.appendChild(queryElement);
		}
		if (to.getExpression() != null) {
			Expression expression = to.getExpression();

			if (expression.getExpressionLanguage() != null) {
				toElement.setAttribute("expressionLanguage", expression
						.getExpressionLanguage());
			}
			if (expression.getBody() != null) {
				CDATASection cdata = BPELUtils.createCDATASection(this.document,
						(String) expression.getBody());
				toElement.appendChild(cdata);
			}
		}

		extensibleElement2XML(to, toElement);
	}

	protected Element wait2XML(Wait activity) {
		
		Element activityElement = createBPELElement("wait");
		if (activity.getFor() != null) {
			activityElement.appendChild(expression2XML(activity.getFor(), "for"));
		}
		if (activity.getUntil() != null) {
			activityElement.appendChild(expression2XML(activity.getUntil(), "until"));
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element throw2XML(Throw activity) {
		
		Element activityElement = createBPELElement("throw");
		if (activity.getFaultVariable() != null && activity.getFaultVariable().getName() != null) {
			activityElement.setAttribute("faultVariable", activity.getFaultVariable().getName());
		}
		if (activity.getFaultName() != null) {
			activityElement.setAttribute("faultName", qNameToString(activity, activity.getFaultName()));
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element exit2XML(Exit activity) {
		
		Element activityElement = createBPELElement("exit");
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	void addActivities(Element activityElement, List<?> listOfActivities) {
		
		for (Object next : listOfActivities) {
			activityElement.appendChild(activity2XML((Activity) next));
		}
	}

	protected Element flow2XML(Flow activity) {

		Element activityElement = createBPELElement("flow");
		Links links = activity.getLinks();
		if (links != null) {
			Element linksElement = links2XML(links);
			activityElement.appendChild(linksElement);
		}

		CompletionCondition completionCondition = activity
				.getCompletionCondition();
		if (completionCondition != null) {
			Element completionConditionElement = completionCondition2XML(completionCondition);
			activityElement.appendChild(completionConditionElement);
		}

		addActivities(activityElement, activity.getActivities());

		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element documentation2XML(Documentation documentation) {
		
		Element documentationElement = createBPELElement("documentation");
		if (documentation.getSource() != null) {
			documentationElement.setAttribute("source", documentation
					.getSource());
		}
		if (documentation.getLang() != null) {
			documentationElement.setAttribute("xml:lang", documentation
					.getLang());
		}
		if (documentation.getValue() != null
				&& documentation.getValue().length() > 0) {
			Text textNode = documentationElement.getOwnerDocument()
					.createTextNode(documentation.getValue());
			documentationElement.appendChild(textNode);
		}

		return documentationElement;
	}

	protected Element links2XML(Links links) {
		
		Element linksElement = createBPELElement("links");
		for (Object next : links.getChildren()) {
			linksElement.appendChild(link2XML((Link) next));
		}
		extensibleElement2XML(links, linksElement);
		return linksElement;
	}

	protected Element link2XML(Link link) {
		
		Element linkElement = createBPELElement("link");
		if (link.getName() != null)
			linkElement.setAttribute("name", link.getName());
		extensibleElement2XML(link, linkElement);
		return linkElement;
	}

	protected Element if2XML(If activity) {

		Element ifElement = createBPELElement("if");
		if (activity.getCondition() != null) {
			ifElement.appendChild(expression2XML(activity.getCondition(),
					"condition"));
		}
		if (activity.getActivity() != null) {
			ifElement.appendChild(activity2XML(activity.getActivity()));
		}
		List<?> elseIfs = activity.getElseIf();
		if (!elseIfs.isEmpty()) {
			for (Object next : elseIfs) {
				ElseIf elseIf = (ElseIf) next;
				ifElement.appendChild(elseIf2XML(elseIf));
			}
		}
		Else _else = activity.getElse();
		if (_else != null) {
			Element elseElement = else2XML(_else);
			ifElement.appendChild(elseElement);
		}
		addCommonActivityItems(ifElement, activity);
		return ifElement;
	}

	protected Element elseIf2XML(ElseIf elseIf) {
		
		Element elseIfElement = createBPELElement("elseif");
		if (elseIf.getCondition() != null) {
			elseIfElement.appendChild(expression2XML(elseIf.getCondition(), "condition"));
		}
		if (elseIf.getActivity() != null) {
			elseIfElement.appendChild(activity2XML(elseIf.getActivity()));
		}
		extensibleElement2XML(elseIf, elseIfElement);

		return elseIfElement;
	}

	protected Element else2XML(Else _else) {
		
		Element elseElement = createBPELElement("else");
		if (_else.getActivity() != null) {
			Element activityElement = activity2XML(_else.getActivity());
			elseElement.appendChild(activityElement);
		}
		extensibleElement2XML(_else, elseElement);

		return elseElement;
	}

	protected Element while2XML(While activity) {
		
		Element activityElement = createBPELElement("while");
		if (activity.getCondition() != null) {
			activityElement.appendChild(expression2XML(activity.getCondition(),
					"condition"));
		}
		if (activity.getActivity() != null) {
			activityElement.appendChild(activity2XML(activity.getActivity()));
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element repeatUntil2XML(RepeatUntil activity) {

		Element activityElement = createBPELElement("repeatUntil");
		if (activity.getActivity() != null) {
			activityElement.appendChild(activity2XML(activity.getActivity()));
		}
		if (activity.getCondition() != null) {
			activityElement.appendChild(expression2XML(activity.getCondition(), "condition"));
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element expression2XML(Expression expression, String elementName) {
		
		Element expressionElement = createBPELElement(elementName);

		if (expression.getExpressionLanguage() != null) {
			expressionElement.setAttribute("expressionLanguage", expression
					.getExpressionLanguage());
		}
		if (expression.getOpaque() != null) {
			expressionElement.setAttribute("opaque", BPELUtils
					.boolean2XML(expression.getOpaque()));
		}
		if (expression.getBody() != null) {
			Object body = expression.getBody();
			if (body instanceof ExtensibilityElement) {
				ExtensibilityElement extensibilityElement = (ExtensibilityElement) body;
				Element child = extensibilityElement2XML(extensibilityElement);
				if (child != null) {
					expressionElement.appendChild(child);
				}
			} else {
				CDATASection cdata = BPELUtils.createCDATASection(this.document,
						expression.getBody().toString());
				expressionElement.appendChild(cdata);
			}
		}

		return expressionElement;
	}

	protected Element sequence2XML(Sequence activity) {
		
		Element activityElement = createBPELElement("sequence");
		addActivities(activityElement, activity.getActivities());
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element sources2XML(Sources sources) {
		
		Element sourcesElement = createBPELElement("sources");
		for (Object next : sources.getChildren()) {
			sourcesElement.appendChild(source2XML((Source) next));
		}
		extensibleElement2XML(sources, sourcesElement);
		return sourcesElement;
	}

	protected Element source2XML(Source source) {

		Element sourceElement = createBPELElement("source");
		sourceElement.setAttribute("linkName", source.getLink().getName());
		Condition transitionCondition = source.getTransitionCondition();
		if (transitionCondition != null) {
			sourceElement.appendChild(expression2XML(transitionCondition,
					"transitionCondition"));
		}
		extensibleElement2XML(source, sourceElement);
		return sourceElement;
	}

	protected Element targets2XML(Targets targets) {
		
		Element targetsElement = createBPELElement("targets");
		// Write out the join condition
		Condition joinCondition = targets.getJoinCondition();
		if (joinCondition != null) {
			targetsElement.appendChild(expression2XML(joinCondition,
					"joinCondition"));
		}
		for (Object next : targets.getChildren()) {
			targetsElement.appendChild(target2XML((Target) next));
		}
		extensibleElement2XML(targets, targetsElement);
		return targetsElement;
	}

	protected Element target2XML(Target target) {
		
		Element targetElement = createBPELElement("target");
		targetElement.setAttribute("linkName", target.getLink().getName());
		extensibleElement2XML(target, targetElement);
		return targetElement;
	}

	protected Element onMessage2XML(OnMessage onMsg) {
		
		Element onMessageElement = createBPELElement("onMessage");
		if (onMsg.getPartnerLink() != null
				&& onMsg.getPartnerLink().getName() != null) {
			onMessageElement.setAttribute("partnerLink", onMsg.getPartnerLink()
					.getName());
		}
		if (onMsg.getPortType() != null
				&& onMsg.getPortType().getQName() != null) {
			onMessageElement.setAttribute("portType", qNameToString(onMsg,
					onMsg.getPortType().getQName()));
		}
		if (onMsg.getOperation() != null) {
			onMessageElement.setAttribute("operation",
					getOperationSignature(onMsg.getOperation()));
		}
		if (onMsg.getVariable() != null
				&& onMsg.getVariable().getName() != null) {
			onMessageElement.setAttribute("variable", onMsg.getVariable()
					.getName());
		}
		if (onMsg.getMessageExchange() != null)
			onMessageElement.setAttribute("messageExchange", onMsg
					.getMessageExchange().getName());
		if (onMsg.getCorrelations() != null) {
			onMessageElement.appendChild(correlations2XML(onMsg
					.getCorrelations()));
		}
		if (onMsg.getActivity() != null) {
			onMessageElement.appendChild(activity2XML(onMsg.getActivity()));
		}
		if (onMsg.getFromParts() != null) {
			onMessageElement.appendChild(fromParts2XML(onMsg.getFromParts()));
		}
		// TODO: Why do we have this? I don't think OnMessage is extensible.
		extensibleElement2XML(onMsg, onMessageElement);
		return onMessageElement;
	}

	protected Element onEvent2XML(OnEvent onEvent) {
		
		Element onEventElement = createBPELElement("onEvent");
		if (onEvent.getPartnerLink() != null && onEvent.getPartnerLink().getName() != null) {
			onEventElement.setAttribute("partnerLink", onEvent.getPartnerLink().getName());
		}
		if (onEvent.getPortType() != null && onEvent.getPortType().getQName() != null) {
			onEventElement.setAttribute("portType", qNameToString(onEvent, onEvent.getPortType()
					.getQName()));
		}
		if (onEvent.getOperation() != null) {
			onEventElement.setAttribute("operation", getOperationSignature(onEvent.getOperation()));
		}
		if (onEvent.getVariable() != null && onEvent.getVariable().getName() != null) {
			onEventElement.setAttribute("variable", onEvent.getVariable().getName());
		}
		if (onEvent.getMessageExchange() != null)
			onEventElement.setAttribute("messageExchange", onEvent.getMessageExchange().getName());
		if (onEvent.getMessageType() != null) {
			onEventElement.setAttribute("messageType", qNameToString(onEvent, onEvent.getMessageType()
					.getQName()));
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336003
		// "element" attribute was missing from original model
		if (onEvent.getXSDElement() != null) {
			onEventElement.setAttribute("element", onEvent.getXSDElement().getQName());
		}
		if (onEvent.getCorrelationSets() != null) {
			onEventElement.appendChild(correlationSets2XML(onEvent.getCorrelationSets()));
		}
		if (onEvent.getCorrelations() != null) {
			onEventElement.appendChild(correlations2XML(onEvent.getCorrelations()));
		}
		if (onEvent.getActivity() != null) {
			onEventElement.appendChild(activity2XML(onEvent.getActivity()));
		}
		if (onEvent.getFromParts() != null) {
			onEventElement.appendChild(fromParts2XML(onEvent.getFromParts()));
		}

		//Why do we have this? I don't think OnEvent is extensible.
		extensibleElement2XML(onEvent, onEventElement);
		return onEventElement;
	}

	protected Element onAlarm2XML(OnAlarm onAlarm) {
		
		Element onAlarmElement = createBPELElement("onAlarm");
		if (onAlarm.getFor() != null) {
			onAlarmElement.appendChild(expression2XML(onAlarm.getFor(), "for"));
		}
		if (onAlarm.getUntil() != null) {
			onAlarmElement.appendChild(expression2XML(onAlarm.getUntil(),
					"until"));
		}
		if (onAlarm.getRepeatEvery() != null) {
			onAlarmElement.appendChild(expression2XML(onAlarm.getRepeatEvery(),
					"repeatEvery"));
		}
		if (onAlarm.getActivity() != null) {
			onAlarmElement.appendChild(activity2XML(onAlarm.getActivity()));
		}

		//Why do we have this? I don't think OnAlarm is extensible.
		extensibleElement2XML(onAlarm, onAlarmElement);
		return onAlarmElement;
	}

	protected Element pick2XML(Pick activity) {
		
		Element activityElement = createBPELElement("pick");
		if (activity.isSetCreateInstance()) {
			activityElement.setAttribute("createInstance", BPELUtils.boolean2XML(activity
					.getCreateInstance()));
		}
		for (Object next : activity.getMessages()) {
			activityElement.appendChild(onMessage2XML((OnMessage) next));
		}
		for (Object next : activity.getAlarm()) {
			activityElement.appendChild(onAlarm2XML((OnAlarm) next));
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element scope2XML(Scope activity) {

		Element activityElement = createBPELElement("scope");
		
		if (activity.isSetIsolated())
			activityElement.setAttribute("isolated", BPELUtils.boolean2XML(activity.getIsolated()));
		if (activity.isSetExitOnStandardFault())
			activityElement.setAttribute("exitOnStandardFault", BPELUtils.boolean2XML(activity
					.getExitOnStandardFault()));
		if (activity.getVariables() != null && !activity.getVariables().getChildren().isEmpty())
			activityElement.appendChild(variables2XML(activity.getVariables()));
		if (activity.getCorrelationSets() != null && !activity.getCorrelationSets().getChildren().isEmpty())
			activityElement.appendChild(correlationSets2XML(activity.getCorrelationSets()));
		if (activity.getPartnerLinks() != null && !activity.getPartnerLinks().getChildren().isEmpty())
			activityElement.appendChild(partnerLinks2XML(activity.getPartnerLinks()));
		if (activity.getFaultHandlers() != null)
			activityElement.appendChild(faultHandlers2XML(activity.getFaultHandlers()));
		if (activity.getCompensationHandler() != null)
			activityElement.appendChild(compensationHandler2XML(activity.getCompensationHandler()));
		if (activity.getTerminationHandler() != null)
			activityElement.appendChild(terminationHandler2XML(activity.getTerminationHandler()));
		if (activity.getEventHandlers() != null)
			activityElement.appendChild(eventHandler2XML(activity.getEventHandlers()));
		if (activity.getMessageExchanges() != null && !activity.getMessageExchanges().getChildren()
				.isEmpty())
			activityElement.appendChild(messageExchanges2XML(activity.getMessageExchanges()));
		if (activity.getActivity() != null)
			activityElement.appendChild(activity2XML(activity.getActivity()));

		addCommonActivityItems(activityElement, activity);

		return activityElement;
	}

	protected Element compensateScope2XML(CompensateScope activity) {
		
		Element activityElement = createBPELElement("compensateScope");
		Activity scopeOrInvoke = activity.getTarget();
		if (scopeOrInvoke != null) {
			activityElement.setAttribute("scope", scopeOrInvoke.getName());
		}
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected Element compensate2XML(Compensate activity) {
		
		Element activityElement = createBPELElement("compensate");
		addCommonActivityItems(activityElement, activity);
		return activityElement;
	}

	protected QName getQName(org.eclipse.wst.wsdl.ExtensibilityElement element, String localName) {
		
		EObject container = null;
		for (container = element.eContainer(); container != null && !(container instanceof Definition);) {
			container = container.eContainer();
		}
		if (container == null) {
			return null;
		}
		return new QName(((Definition) container).getTargetNamespace(), localName);
	}

	/**
	 * Convert a BPEL ExtensibileElement to XML
	 */
	protected void extensibleElement2XML(BPELExtensibleElement extensibleElement, Element element) {

		if (extensibleElement.getDocumentation() != null) {
			Node firstChild = element.getFirstChild();
			Element documentationElement = documentation2XML(extensibleElement
					.getDocumentation());
			if (firstChild == null) {
				element.appendChild(documentationElement);
			} else {
				element.insertBefore(documentationElement, firstChild);
			}
		}

		// Get the extensibility elements and if the platform is running try to
		// order them.
		// If the platform is not running just serialize the elements in the
		// order they appear.
		List<org.eclipse.wst.wsdl.ExtensibleElement> extensibilityElements = extensibleElement
				.getExtensibilityElements();

		// Loop through the extensibility elements
		for (Object name : extensibilityElements) {
			ExtensibilityElement extensibilityElement = (ExtensibilityElement) name;
			// Lookup a serializer for the extensibility element
			BPELExtensionSerializer serializer = null;
			QName qname = extensibilityElement.getElementType();
			try {
				serializer = (BPELExtensionSerializer) this.extensionRegistry
						.querySerializer(BPELExtensibleElement.class, qname);
			} catch (WSDLException e) {
				LOGGER.error(e.getMessage(), e);
			}
			if (serializer != null) {
				// Create a temp document fragment for the serializer
				DocumentFragment fragment = this.document.createDocumentFragment();
				Node tempElement = fragment.getFirstChild();
				if (tempElement == null) {
					return;
				}
				String nodeName = tempElement.getNodeName();
				nodeName = nodeName.substring(nodeName.lastIndexOf(':') + 1);
				if (nodeName.equals("extensibilityAttributes")) {
					// Add the attributes to the parent DOM element
					String elementName = tempElement.getNodeName();
					String prefix = elementName.lastIndexOf(':') != -1 ? elementName
							.substring(0, elementName.indexOf(':')): null;
					NamedNodeMap attributes = tempElement.getAttributes();
					for (int a = 0, n = attributes.getLength(); a < n; a++) {
						Attr attr = (Attr) attributes.item(a);
						String attrName = attr.getNodeName();
						if (attrName.indexOf(':') == -1 && prefix != null)
							attrName = prefix + ':' + attrName;
						if (attrName.startsWith("xmlns:")) {
							String localName = attrName.substring("xmlns:".length());
							Map<String, String> nsMap = BPELUtils.getNamespaceMap(extensibleElement);
							if (!nsMap.containsKey(localName) || !attr.getNodeValue().equals(nsMap
									.get(localName))) {
								nsMap.put(localName, attr.getNodeValue());
							}
						} else {
							element.setAttribute(attrName, attr.getNodeValue());
						}
					}
				} else {
					// The extensibility element was serialized into a DOM
					// element, simply
					// add it to the parent DOM element
					// always append the extension element to the
					// begining of the children list
					if (element.getFirstChild() == null)
						element.appendChild(tempElement);
					else
						element.insertBefore(tempElement, element
								.getFirstChild());
				}
			}
		}
	}

	/**
	 * Get process from the resource
	 * @return the Process
	 */
	private Process getProcess() {
		return getResource().getProcess();
	}

	/**
	 * Convert a BPEL ExtensibilityElement to XML
	 */
	protected Element extensibilityElement2XML(ExtensibilityElement extensibilityElement) {

		BPELExtensionSerializer serializer = null;
		QName qname = extensibilityElement.getElementType();
		try {
			serializer = (BPELExtensionSerializer) this.extensionRegistry
					.querySerializer(BPELExtensibleElement.class, qname);
		} catch (WSDLException e) {
			return null;
		}

		// Deserialize the DOM element and add the new Extensibility element to
		// the parent
		// BPELExtensibleElement
		DocumentFragment fragment = this.document.createDocumentFragment();
		Element child = getFirstChildElement(fragment);
		return child;
	}

	protected Element createBPELElement(String tagName) {

		return this.document.createElement(BPELConstants.PREFIX+":"+tagName);
	}

	private void serializePrefixes(EObject eObject, Element context) {
		
		INamespaceMap<String, String> nsMap = BPELUtils.getNamespaceMap(eObject);
		if (!nsMap.isEmpty()) {
			for( Map.Entry<String,String> entry : nsMap.entrySet()) {
				String prefix = entry.getKey();
				String namespace = entry.getValue();
				if (prefix.length() == 0)
					context.setAttributeNS(XSDConstants.XMLNS_URI_2000, "xmlns", namespace);
				else
					context.setAttributeNS(XSDConstants.XMLNS_URI_2000, "xmlns:" + prefix, namespace);
			}
		}
	}

	private String qNameToString(EObject eObject, QName qname) {
		
		String namespace = qname.getNamespaceURI();
		if (namespace == null || namespace.length() == 0) {
			return qname.getLocalPart();
		}
		// Transform BPEL namespaces to the latest version so that
		// references to the old namespace are not serialized.
		if (BPELConstants.isBPELNamespace(namespace)) {
			namespace = BPELConstants.NAMESPACE;
		}
		// if a prefix is not found for the namespaceURI, create a new
		// prefix
		return namespace + ":" + qname.getLocalPart();
	}


	private Element getFirstChildElement(DocumentFragment fragment) {
		
		// first child may be a TEXT Node (e.g. whitespace)
		Node child = fragment.getFirstChild();
		while (child != null && !(child instanceof Element)) {
			child = child.getNextSibling();
		}
		// We are out of the loop: either child is null, or it is an element
		if( child != null )
			return (Element) child;
		throw new IllegalArgumentException("Document Fragment does not contain any Elements");
	}
}
