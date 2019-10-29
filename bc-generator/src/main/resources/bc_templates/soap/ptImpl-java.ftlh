package ${bcData.packagename}.webservices;

<#if isOutputRootMultiple>
import java.util.List;
</#if>

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ${bcData.packagename}.${bcData.servicename?cap_first}PT;
import ${bcData.packagename}.${inputRootElement?cap_first};
import ${bcData.packagename}.${outputRootElement?cap_first};
import ${bcData.packagename}.business.${bcData.servicename?cap_first}Service;

@Component(value = "${bcData.servicename?cap_first}PTImpl")
public class ${bcData.servicename?cap_first}PTImpl implements ${bcData.servicename?cap_first}PT {

	private static Logger logger = LoggerFactory.getLogger(${bcData.servicename?cap_first}PTImpl.class);
	
	@Autowired
	private ${bcData.servicename?cap_first}Service service;

	@Override
	public <#if isOutputRootMultiple>List<</#if>${outputRootElement?cap_first}<#if isOutputRootMultiple>></#if> ${operationName?uncap_first}(${inputRootElement?cap_first} params) {
		logger.info("CALLED ${operationName} ON ${bcData.servicename?upper_case}");
		try{
			<#if isOutputRootMultiple>List<</#if>${outputRootElement?cap_first}<#if isOutputRootMultiple>></#if> response = service.${operationName?uncap_first}(params);
			return response;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
}
