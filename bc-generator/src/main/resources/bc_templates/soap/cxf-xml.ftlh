<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" 
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:jaxws="http://cxf.apache.org/jaxws" 
       xmlns:cxf="http://cxf.apache.org/core" 
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd 
                           http://cxf.apache.org/core http://cxf.apache.org/schemas/core.xsd 
                           http://cxf.apache.org/jaxws http://cxf.apache.org/schemas/jaxws.xsd">
                           
    <jaxws:endpoint id="${bcData.servicename?cap_first}HTTP" 
			xmlns:${bcData.servicename?lower_case}="${bcData.targetNamespace}"
		    address="/${bcData.servicename}" 
		    serviceName="${bcData.servicename?lower_case}:${bcData.servicename}Service"
		    endpointName="${bcData.servicename?lower_case}:${bcData.servicename}Port" 
		    implementor="#${bcData.servicename?cap_first}PTImpl">
	</jaxws:endpoint>

    <jaxws:endpoint id="BaseServiceHTTP" 
		xmlns:baseservice="http://services.chorevolution.eu/"
	    address="/BaseService" 
	    serviceName="baseservice:BaseService"
	    endpointName="baseservice:ConfigurableServicePort" 
	    implementor="#ConfigurableServiceImpl">		    
  	</jaxws:endpoint> 
	
</beans>