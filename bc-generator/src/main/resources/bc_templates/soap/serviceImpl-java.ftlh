package ${bcData.packagename}.business.impl;

<#if isOutputRootMultiple>
import java.util.ArrayList;
import java.util.List;
</#if>

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

<#list types.wsdlItems as name, item>
import ${bcData.packagename}.${item.javaName?cap_first};
</#list>
import ${bcData.packagename}.business.BusinessException;
import ${bcData.packagename}.business.${bcData.servicename?cap_first}Service;

@Service
public class ${bcData.servicename?cap_first}ServiceImpl implements ${bcData.servicename?cap_first}Service {

	private static Logger LOGGER = LoggerFactory.getLogger(${bcData.servicename?cap_first}ServiceImpl.class);

	@Override
	public <#if isOutputRootMultiple>List<</#if>${outputRootElement?cap_first}<#if isOutputRootMultiple>></#if> ${operationName?uncap_first}(${inputRootElement?cap_first} params) throws BusinessException {

		try {

			<#-- Workaround for bcPersonalWeatherStations security parameter -->
			<#if bcData.name=="bcPersonalWeatherStations">
			params.setS(SecurityHelper.getDynamicPassword("m1x3r_m0by"));
			</#if>


			// build webservice URL
			
			String baseUrl = "${url}?";

			<#list inputDataElements as inputElement>
			baseUrl = baseUrl + "${inputElement}=" + params.get${inputElement?cap_first}() + "&";
			</#list>
			baseUrl = StringUtils.removeEnd(baseUrl, "&");

			LOGGER.info("CALLING URL " + baseUrl);

			// connect to url
			URL u = new URL(baseUrl);
			HttpURLConnection c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-length", "0");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.connect();
			int status = c.getResponseCode();

			switch (status) {
			case 200:
			case 201:

				BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();

				if (c != null) {
					c.disconnect();
				}

				LOGGER.info("RECEIVED JSON " + sb.toString());

				<#if isOutputRootMultiple>
				List<${outputRootElement?cap_first}> ${outputRootElement?uncap_first}s = new ArrayList<>();
				JSONArray ${outputRootElement?uncap_first}JSONArray = new JSONArray(sb.toString());
				for (int ${outputRootElement?uncap_first}Count=0; ${outputRootElement?uncap_first}Count<${outputRootElement?uncap_first}JSONArray.length(); ${outputRootElement?uncap_first}Count++) {
					JSONObject ${outputRootElement?uncap_first}JSON = ${outputRootElement?uncap_first}JSONArray.getJSONObject(${outputRootElement?uncap_first}Count);
					${outputRootElement?cap_first} ${outputRootElement?uncap_first} = new ${outputRootElement?cap_first}();
				<#else>
				${outputRootElement?cap_first} ${outputRootElement?uncap_first} = new ${outputRootElement?cap_first}();
				JSONObject ${outputRootElement?uncap_first}JSON = new JSONObject(sb.toString());
				</#if>

				<#list types.wsdlItems[outputRootElement].innerItems as rootInnerItem>
				<@composeObject rootInnerItem outputRootElement/>
				</#list>
				
				<#macro composeObject element, previous>
				<#if element.isComplex()>
				if(!${previous?uncap_first}JSON.isNull("${element.name}")){
					<#if element.maxOccurs=="unbounded">
					JSONArray ${element.javaName?uncap_first}JSONArray = ${previous?uncap_first}JSON.getJSONArray("${element.name}");
					for (int ${element.javaName?uncap_first}Count = 0; ${element.javaName?uncap_first}Count < ${element.javaName?uncap_first}JSONArray.length(); ${element.javaName?uncap_first}Count++) {
					JSONObject ${element.javaName?uncap_first}JSON = ${element.javaName?uncap_first}JSONArray.getJSONObject(${element.javaName?uncap_first}Count);
					<#else>
					JSONObject ${element.javaName?uncap_first}JSON = ${previous?uncap_first}JSON.getJSONObject("${element.name}");
					</#if>
					${element.javaName?cap_first} ${element.javaName?uncap_first} = new ${element.javaName?cap_first}();
					<#list element.innerItems as innerItem>
					<@composeObject innerItem element.javaName/>
					</#list>
					<#if element.maxOccurs=="unbounded">
					${previous?uncap_first}.get${element.javaName?cap_first}().add(${element.javaName?uncap_first});
					}
					<#else>
					${previous?uncap_first}.set${element.javaName?cap_first}(${element.javaName?uncap_first});
					</#if>
				}
				<#else>
				<#if element.maxOccurs=="unbounded">
				if(!${previous?uncap_first}JSON.isNull("${element.name}")){
					JSONArray ${element.javaName?uncap_first}JSONArray = ${previous?uncap_first}JSON.getJSONArray("${element.name}");
					for (int ${element.javaName?uncap_first}Count = 0; ${element.javaName?uncap_first}Count < ${element.javaName?uncap_first}JSONArray.length(); ${element.javaName?uncap_first}Count++) {
						${previous?uncap_first}.get${element.javaName?cap_first}().add(${element.javaName?uncap_first}JSONArray.get<#if element.type="integer">Int<#elseif element.type="date">String<#else>${element.type?cap_first}</#if>(${element.javaName?uncap_first}Count));
					}
				}
				<#else>
				if(!${previous?uncap_first}JSON.isNull("${element.name}")) ${previous?uncap_first}.set${element.javaName?cap_first}(${previous?uncap_first}JSON.get<#if element.type="integer">Int<#elseif element.type="date">String<#else>${element.type?cap_first}</#if>("${element.name}"));
				</#if>
				</#if>
				</#macro>

				<#if isOutputRootMultiple>
					${outputRootElement?uncap_first}s.add(${outputRootElement?uncap_first});
				}
				return ${outputRootElement?uncap_first}s;
				<#else>
				return ${outputRootElement?uncap_first};
				</#if>


			default:
				if (c != null) {
					c.disconnect();
				}
				throw new Exception("Http Error: " + status);
			}
		} catch (Exception e) {
			LOGGER.error("error", e);
			throw new BusinessException(e);
		}
	}
}
