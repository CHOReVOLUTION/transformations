package ${adapterData.packagename}.integration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ${adapterData.packagename}.integration.util.TransformationRuleHandler;
import ${adapterData.packagename}.integration.util.TransformationUtils;

@Component
public class ResponseTranslator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseTranslator.class);

	public Message<String> translate(String input) {

		LOGGER.info("RECEIVING MESSAGE " + input);

		Document inputDoc = TransformationUtils.convertStringToDocument(input);
		NodeList nodes = inputDoc.getFirstChild().getChildNodes();

		Map<String, Integer> occurrencesMap = new HashMap<>();

		String output = "<ns:${responseRootElement} xmlns:ns=\"${adapterData.adTargetNamespace}\">";

		<#list translationDataRoot.innerElements as element>
		<@translate element/>
		</#list>

		<#macro translate element>
		<#if element.hasMultipleOccurrences>
		<#if element.bcElements[0].occurrences!="unbounded">
		<#-- THIS "IF" BRANCH INTRODUCES AN ASSUMPTION: if the related element has single occurrence, the iteration will be performed on the parent element -->
		int ${element.bcElements[0].fromRootPath?replace(".", "")}Number = TransformationUtils.findNumberOfOccurrences(nodes, "${element.bcElements[0].fromRootPath}", occurrencesMap);
		int ${element.bcElements[0].fromRootPath?replace(".", "")}Counter = 1;
		while (${element.bcElements[0].fromRootPath?replace(".", "")}Counter <= ${element.bcElements[0].fromRootPath?replace(".", "")}Number) {
			occurrencesMap.put("${element.bcElements[0].fromRootPath}", ${element.bcElements[0].fromRootPath?replace(".", "")}Counter);
		<#else>
		<#list element.bcElements as relatedElement>
		int ${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Number = TransformationUtils.findNumberOfOccurrences(nodes, "<#if relatedElement.fromRootPath?? && relatedElement.fromRootPath!="">${relatedElement.fromRootPath}.</#if>${relatedElement.name}", occurrencesMap);
		int ${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Counter = 1;
		</#list>
		while (${element.bcElements[0].fromRootPath?replace(".", "")}${element.bcElements[0].name}Counter <= ${element.bcElements[0].fromRootPath?replace(".", "")}${element.bcElements[0].name}Number) {
			<#list element.bcElements as relatedElement>
			occurrencesMap.put("<#if relatedElement.fromRootPath?? && relatedElement.fromRootPath!="">${relatedElement.fromRootPath}.</#if>${relatedElement.name}", ${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Counter);
			</#list>
		</#if>
		</#if>
			<#if element.hasInnerElements()>
			<@complexItemTranslate element/>
			<#else>
			<@simpleItemTranslate element/>
			</#if>
		<#if element.hasMultipleOccurrences>
		<#if element.bcElements[0].occurrences!="unbounded"> <#-- THIS "IF" BRANCH INTRODUCES AN ASSUMPTION! -->
			${element.bcElements[0].fromRootPath?replace(".", "")}Counter++;
		}
		<#else>
			<#list element.bcElements as relatedElement>
			${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Counter++;
			</#list>
		}
		</#if>
		</#if>
		</#macro>

		<#macro complexItemTranslate itemTranslationData>
		output = output + "<${itemTranslationData.adElementName}>";
		<#list itemTranslationData.innerElements as element>
		<@translate element/>
		</#list>
		output = output + "</${itemTranslationData.adElementName}>";
		</#macro> 

		<#macro simpleItemTranslate itemTranslationData>
		<#if itemTranslationData.transformationRule??>
		<@transformationRule itemTranslationData/>
		<#else>
		<#if itemTranslationData.isEnumeration()>
		<@enumerationTranslate itemTranslationData/>
		<#else>
		<#if itemTranslationData.bcElements[0]??>
		if(!TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.bcElements[0].fromRootPath?? && itemTranslationData.bcElements[0].fromRootPath!="">${itemTranslationData.bcElements[0].fromRootPath}.</#if>${itemTranslationData.bcElements[0].name}", occurrencesMap).isEmpty()) {
			output = output + "<${itemTranslationData.adElementName}>" + StringEscapeUtils.escapeXml(TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.bcElements[0].fromRootPath?? && itemTranslationData.bcElements[0].fromRootPath!="">${itemTranslationData.bcElements[0].fromRootPath}.</#if>${itemTranslationData.bcElements[0].name}", occurrencesMap)) + "</${itemTranslationData.adElementName}>";
		}
		</#if>
		</#if>
		</#if>
		</#macro>

		<#macro enumerationTranslate itemTranslationData>
		if(!TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.bcElements[0].fromRootPath?? && itemTranslationData.bcElements[0].fromRootPath!="">${itemTranslationData.bcElements[0].fromRootPath}.</#if>${itemTranslationData.bcElements[0].name}", occurrencesMap).isEmpty()) {
			String valueIn = TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.bcElements[0].fromRootPath?? && itemTranslationData.bcElements[0].fromRootPath!="">${itemTranslationData.bcElements[0].fromRootPath}.</#if>${itemTranslationData.bcElements[0].name}", occurrencesMap);
			String valueOut = "";
			
			switch (valueIn) {
			<#list itemTranslationData.enumerationMapping as adValue, bcValue>
			case "${bcValue}":
				valueOut = "${adValue}";
				break;
			</#list>
			}
			output = output + "<${itemTranslationData.adElementName}>" + StringEscapeUtils.escapeXml(valueOut) + "</${itemTranslationData.adElementName}>";
		}
		</#macro>

		<#macro transformationRule itemTranslationData>
		String ${itemTranslationData.elementPath?replace(".", "")}OutValue = TransformationRuleHandler.handleRule(StringEscapeUtils.unescapeXml("${itemTranslationData.transformationRule}"), nodes, occurrencesMap);
		output = output + "<${itemTranslationData.adElementName}>" + StringEscapeUtils.escapeXml(${itemTranslationData.elementPath?replace(".", "")}OutValue) + "</${itemTranslationData.adElementName}>"; 
		</#macro>

		output = output + "</ns:${responseRootElement}>";

		return MessageBuilder.withPayload(output).build();
	}
}
