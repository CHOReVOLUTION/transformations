package ${adapterData.packagename}.integration.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.NodeList;

import ${adapterData.packagename}.integration.RequestData;

public class TransformationRuleHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransformationRuleHandler.class);

	private static final String[] FUNCTIONS = {"valueof", "parseDate", "replace", "json", "inputMessage"};

	public static String handleRule(String rule, NodeList nodes, Map<String, Integer> occurrencesMap) {

		LOGGER.info("Handling rule " + rule);
		
		String output = "";
		String[] ruleElements = rule.split("(?<!\\\\)\\+");

		for (int i=0; i<ruleElements.length; i++) {
			String ruleElement = ruleElements[i].trim();

			if (ruleElement.matches("\"([^\"]*)\"")) {
				output = output + ruleElement.substring(1, ruleElement.length()-1);
				
			} else if (isRuleFunction(ruleElement)) {
				ruleElement = unescapeRule(ruleElement);
				output = output + stepInsideRule(ruleElement, nodes, occurrencesMap);
			} else {
				output = output + TransformationUtils.findNodeValue(nodes, ruleElement, occurrencesMap);
			}
		}

		return output;
	}

	private static String stepInsideRule(String rule, NodeList nodes, Map<String, Integer> occurrencesMap) {
		String[] ruleElements = rule.split("\\(", 2);

		String ruleName = ruleElements[0];
		String ruleBody = ruleElements[1].substring(0, ruleElements[1].length()-1).trim();

		switch (ruleName) {
		case "valueof":
			return transformValueof(ruleBody, nodes, occurrencesMap);

		case "parseDate":
			return transformParseDate(ruleBody, nodes, occurrencesMap);

		case "replace":
			return transformReplace(ruleBody, nodes, occurrencesMap);

		case "json":
			return transformJson(ruleBody, nodes, occurrencesMap);

		case "inputMessage":
			return transformInputMessage(ruleBody);
		}

		return "";
	}


	private static String transformValueof(String ruleBody, NodeList nodes, Map<String, Integer> occurrencesMap) {

		LOGGER.info("Transforming rule valueof");

		ruleBody = ruleBody.replace("\"", "");
		String[] ruleElements = ruleBody.split(",");

		if (ruleElements.length>1) {
			String translatingElement = ruleElements[0].trim();

			String elementValue = TransformationUtils.findNodeValue(nodes, translatingElement, occurrencesMap);

			for (int i=1; i<ruleElements.length; i++) {
				String association = ruleElements[i].trim();
				String[] valueMapping = association.split(":");
				if(valueMapping.length==2) {
					if (valueMapping[0].equals(elementValue)) {
						return valueMapping[1];
					}
				}
			}
		}
		LOGGER.info("Valueof rule parameters are incorrect: returning empty value");
		return "";
	}

	private static String transformParseDate(String ruleBody, NodeList nodes, Map<String, Integer> occurrencesMap) {

		LOGGER.info("Transforming rule parseDate");
		
		String[] ruleElements = ruleBody.split(",", 2);

		if (ruleElements.length>1) {
			String translatingElement = ruleElements[0].trim();

			String dateRegex = ruleElements[1].trim();
			dateRegex = dateRegex.substring(1, dateRegex.length()-1); // removing double quotes

			String elementValue = TransformationUtils.findNodeValue(nodes, translatingElement, occurrencesMap);

			if (elementValue!=null && !elementValue.isEmpty()) {

				String output = "";

				try {

					Date date = null;

					String regexComponents = dateRegex.replaceAll("\'.*?\'", " ").trim(); // Extract only the component needed for parsing the date

					// If the date is printed only as milliseconds, the simple date parser will produce a wrong output
					if (regexComponents.matches("([S]+)[ ]*(z|Z|X)*")) {

						String[] removeValues = StringUtils.substringsBetween(dateRegex, "\'", "\'");
						for (String removeValue : removeValues) {
							elementValue = elementValue.replace(removeValue, "");
						}

						String[] dateComponents = elementValue.split("[^0-9]", 1);

						if (dateComponents.length>0) {

							long milliseconds = Long.parseLong(dateComponents[0]);
							Timestamp t = new Timestamp(milliseconds);
							date = new Date(t.getTime());

						} else {
							LOGGER.info("Adapter it is not able to parse correctly the date: it may contain errors");

							SimpleDateFormat inputDateFormat = new SimpleDateFormat(dateRegex);
							date = inputDateFormat.parse(elementValue);
						}

					} else {

						SimpleDateFormat inputDateFormat = new SimpleDateFormat(dateRegex);
						date = inputDateFormat.parse(elementValue);
					}
	
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
					output = dateFormat.format(date);
				} catch (ParseException e) {
					LOGGER.error(e.getMessage());
				}

				return output;
			} else {
				LOGGER.info("Element value is empty: skipping parsing");
			}

		}
		LOGGER.info("ParseDate rule parameters are incorrect: returning empty value");
		return "";
	}

	private static String transformReplace(String ruleBody, NodeList nodes, Map<String, Integer> occurrencesMap) {

		LOGGER.info("Transforming rule replace");

		String[] ruleElements = ruleBody.split(",", 3);

		if (ruleElements.length>3) {
			String translatingElement = ruleElements[0].trim();

			String regex = ruleElements[1].trim();
			regex = regex.substring(1, regex.length()-1);

			String replace = ruleElements[2].trim();
			replace = replace.substring(1, replace.length()-1);

			String elementValue = TransformationUtils.findNodeValue(nodes, translatingElement, occurrencesMap);

			String output = "";
			try {
				output = elementValue.replaceAll(regex, replace);
			} catch (PatternSyntaxException e) {
				LOGGER.error(e.getMessage());
			}

			return output;
		}

		LOGGER.info("Replace rule parameters are incorrect: returning empty value");
		return "";
	}

	private static String transformJson(String ruleBody, NodeList nodes, Map<String, Integer> occurrencesMap) {

		LOGGER.info("Transforming rule replace");

		String[] ruleElements = ruleBody.split(",", 2);

		if (ruleElements.length>1) {
			String translatingElmement = ruleElements[0].trim();
			String objectField = ruleElements[1].trim();
			objectField = objectField.substring(1, objectField.length()-1);

			String translatingJSON = TransformationUtils.findNodeValue(nodes, translatingElmement, occurrencesMap);
			JSONObject jsonObject = new JSONObject(translatingJSON);
			return jsonObject.get(objectField).toString();
		}
		LOGGER.info("Json rule parameters are incorrect: returning empty value");
		return "";

	}

	private static String transformInputMessage(String ruleBody) {

		LOGGER.info("Transforming rule inputMessage");

		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
		RequestData requestData = context.getBean(RequestData.class);

		String output = requestData.getRequestData(ruleBody);

		return output!=null ? output : "";
	}
	

	private static String unescapeRule(String rule) {
		return rule.replaceAll("\\\\", "");
	}

	private static boolean isRuleFunction(String rule) {
		for (int i=0; i<FUNCTIONS.length; i++) {
			if (rule.startsWith(FUNCTIONS[i])) {
				return true;
			}
		}
		return false;
	}
}
