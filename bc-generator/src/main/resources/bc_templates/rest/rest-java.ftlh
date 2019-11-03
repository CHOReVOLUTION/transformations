package ${bcData.packagename};

import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import ${bcData.packagename}.util.SetInvocationAddressUtils;

@Controller
public class ${bcData.servicename?cap_first}REST {

	private static Logger logger = LoggerFactory.getLogger(${bcData.servicename?cap_first}REST.class);

	@GET
	@Produces("text/plain")
	@Path("/${operationName}")
	public String ${operationName}(
			<#list requestValues.requestElements as name, element>
			<#if element.enumeration>
			@QueryParam("${element.name}") String ${element.name}<#sep>, </#sep>
			<#else>
			@QueryParam("${element.name}") ${element.type} ${element.name}<#sep>, </#sep>
			</#if>
			</#list>
			) {

		logger.info("CALLED ${operationName} ON ${bcData.servicename?upper_case}");

		try {
			URL WSDL_URL = new URL(SetInvocationAddressUtils.getTargetEndpointAddress());
			${bcData.roleName?cap_first}Service cd${bcData.roleName?cap_first}Service = null;
			if (WSDL_URL == null) {
				 cd${bcData.roleName?cap_first}Service = new ${bcData.roleName?cap_first}Service();
			} else {
				 cd${bcData.roleName?cap_first}Service = new ${bcData.roleName?cap_first}Service(WSDL_URL);
			}
			
			${bcData.roleName?cap_first}PT cd${bcData.roleName?cap_first}PT = cd${bcData.roleName?cap_first}Service.get${bcData.roleName?cap_first}Port();

			${requestValues.requestWrapperTypeName?cap_first} request = new ${requestValues.requestWrapperTypeName?cap_first}();
			<#list requestValues.requestElements as name, element>
			<#if element.enumeration>
			request.set${element.name?cap_first}(${element.type?cap_first}.fromValue(${element.name}));
			<#else>
			request.set${element.name?cap_first}(${element.name});
			</#if>
			</#list>

			${inputRootItem?cap_first} requestType = new ${inputRootItem?cap_first}();
			requestType.set${requestValues.requestWrapperParameterName?cap_first}(request);

			${outputRootItem?cap_first} response = cd${bcData.roleName?cap_first}PT.${operationName}(requestType);

			// convert the planResponse to json
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(response);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
}