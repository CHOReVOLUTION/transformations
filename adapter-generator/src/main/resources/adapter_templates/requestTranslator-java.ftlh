package ${adapterData.packagename}.integration;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.NodeList;

import ${adapterData.packagename}.integration.util.TransformationRuleHandler;
import ${adapterData.packagename}.integration.util.TransformationUtils;


@Component
public class RequestTranslator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestTranslator.class);


	public Message<String> translate(DOMSource input) {		
		NodeList nodes = input.getNode().getChildNodes();

		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
		RequestData requestData = context.getBean(RequestData.class);
		requestData.initData(nodes);

		Map<String, Integer> occurrencesMap = new HashMap<>();

		String output = "<ns:${inputRootElement} xmlns:ns=\"${adapterData.bcTargetNamespace}\">";

		<#list translationDataRoot.innerElements as element>
		<@translate element/>
		</#list>

		<#macro translate element >
		<#if element.hasMultipleOccurrences>
		<#list element.adElements as relatedElement>
		int ${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Number = TransformationUtils.findNumberOfOccurrences(nodes, "<#if relatedElement.fromRootPath?? && relatedElement.fromRootPath!="">${relatedElement.fromRootPath}.</#if>${relatedElement.name}", occurrencesMap);
		int ${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Counter = 1;
		</#list>
		while (${element.adElements[0].fromRootPath?replace(".", "")}${element.adElements[0].name}Counter <= ${element.adElements[0].fromRootPath?replace(".", "")}${element.adElements[0].name}Number) {
			<#list element.adElements as relatedElement>
			occurrencesMap.put("<#if relatedElement.fromRootPath?? && relatedElement.fromRootPath!="">${relatedElement.fromRootPath}.</#if>${relatedElement.name}", ${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Counter);
			</#list>
		</#if>
			<#if element.hasInnerElements()>
			<@complexItemTranslate element/>
			<#else>
			<@simpleItemTranslate element/>
			</#if>
		<#if element.hasMultipleOccurrences>
			<#list element.adElements as relatedElement>
			${relatedElement.fromRootPath?replace(".", "")}${relatedElement.name}Counter++;
			</#list>
		}
		</#if>
		</#macro>

		<#macro complexItemTranslate itemTranslationData>
		output = output + "<${itemTranslationData.bcElementName}>";
		<#list itemTranslationData.innerElements as element>
		<@translate element/>
		</#list>
		output = output + "</${itemTranslationData.bcElementName}>";
		</#macro> 

		<#macro simpleItemTranslate itemTranslationData>
		<#if itemTranslationData.transformationRule??>
		<@transformationRule itemTranslationData/>
		<#else>
		<#if itemTranslationData.isEnumeration()>
		<@enumerationTranslate itemTranslationData/>
		<#else>
		<#if itemTranslationData.adElements[0]??>
		if(!TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.adElements[0].fromRootPath?? && itemTranslationData.adElements[0].fromRootPath!="">${itemTranslationData.adElements[0].fromRootPath}.</#if>${itemTranslationData.adElements[0].name}", occurrencesMap).isEmpty()) {
			output = output + "<${itemTranslationData.bcElementName}>" + StringEscapeUtils.escapeXml(TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.adElements[0].fromRootPath?? && itemTranslationData.adElements[0].fromRootPath!="">${itemTranslationData.adElements[0].fromRootPath}.</#if>${itemTranslationData.adElements[0].name}", occurrencesMap)) + "</${itemTranslationData.bcElementName}>";
		}
		</#if>
		</#if>
		</#if>
		</#macro>

		<#macro enumerationTranslate itemTranslationData>
		if(!TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.adElements[0].fromRootPath?? && itemTranslationData.adElements[0].fromRootPath!="">${itemTranslationData.adElements[0].fromRootPath}.</#if>${itemTranslationData.adElements[0].name}", occurrencesMap).isEmpty()) {
			String valueIn = TransformationUtils.findNodeValue(nodes, "<#if itemTranslationData.adElements[0].fromRootPath?? && itemTranslationData.adElements[0].fromRootPath!="">${itemTranslationData.adElements[0].fromRootPath}.</#if>${itemTranslationData.adElements[0].name}", occurrencesMap);
			String valueOut = "";
			
			switch (valueIn) {
			<#list itemTranslationData.enumerationMapping as adValue, bcValue>
			case "${adValue}":
				valueOut = "${bcValue}";
				break;
			</#list>
			}
			output = output + "<${itemTranslationData.bcElementName}>" + StringEscapeUtils.escapeXml(valueOut) + "</${itemTranslationData.bcElementName}>";
		}
		</#macro>

		<#macro transformationRule itemTranslationData>
		String ${itemTranslationData.elementPath?replace(".", "")}OutValue = TransformationRuleHandler.handleRule(StringEscapeUtils.unescapeXml("${itemTranslationData.transformationRule}"), nodes, occurrencesMap);
		output = output + "<${itemTranslationData.bcElementName}>" + StringEscapeUtils.escapeXml(${itemTranslationData.elementPath?replace(".", "")}OutValue) + "</${itemTranslationData.bcElementName}>";
		</#macro>

		output = output + "</ns:${inputRootElement}>";

		LOGGER.info("SENDING MESSAGE " + output);

		return MessageBuilder.withPayload(output).build();
	}
}
