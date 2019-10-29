<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:jaxws="http://cxf.apache.org/jaxws"
	xmlns:cxf="http://cxf.apache.org/core" xmlns:jaxrs="http://cxf.apache.org/jaxrs"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
					  http://www.springframework.org/schema/beans/spring-beans.xsd
                      http://cxf.apache.org/core 
                      http://cxf.apache.org/schemas/core.xsd 
                      http://cxf.apache.org/jaxws 
                      http://cxf.apache.org/schemas/jaxws.xsd					  
					  http://cxf.apache.org/jaxrs
                      http://cxf.apache.org/schemas/jaxrs.xsd">


	<jaxrs:server id="${bcData.name}Service" address="/services">
		<jaxrs:serviceBeans>
			<ref bean="${bcData.name}" />
		</jaxrs:serviceBeans>
	</jaxrs:server>

	<bean id="${bcData.name}" class="${bcData.packagename}.${bcData.servicename}REST" />
	
	<jaxws:endpoint id="BaseServiceHTTP" 
		xmlns:baseservice="http://services.chorevolution.eu/"
	    address="/BaseService" 
	    serviceName="baseservice:BaseService"
	    endpointName="baseservice:ConfigurableServicePort" 
	    implementor="#ConfigurableServiceImpl">		    
  	</jaxws:endpoint>
</beans>