<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:int="http://www.springframework.org/schema/integration"
	xmlns:int-ws="http://www.springframework.org/schema/integration/ws"
	xmlns:sws="http://www.springframework.org/schema/web-services"
	xsi:schemaLocation="
	http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
	http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd
	http://www.springframework.org/schema/web-services http://www.springframework.org/schema/web-services/web-services-2.0.xsd
	http://www.springframework.org/schema/integration/ws http://www.springframework.org/schema/integration/ws/spring-integration-ws.xsd" >


	<bean class="org.springframework.ws.server.endpoint.mapping.UriEndpointMapping">
		<property name="defaultEndpoint" ref="ws-inbound-gateway" />
	</bean>

	<sws:static-wsdl id="${adapterData.name}" location="/WEB-INF/wsdl/${adapterData.wsdlname}" />

    <int-ws:inbound-gateway id="ws-inbound-gateway"
    	request-channel="inboundChannel"
    	reply-channel="outboundChannel"/>

	<int:channel id="inboundChannel" />

	<int:channel id="translatedRequestChannel" />

	<int:channel id="serviceResponseChannel" />

	<int:channel id="outboundChannel" />

	<int:service-activator input-channel="inboundChannel" output-channel="translatedRequestChannel" ref="requestTranslator" />
	<bean id="requestTranslator" class="${adapterData.packagename}.integration.RequestTranslator"/>

	<int:chain input-channel="translatedRequestChannel" output-channel="serviceResponseChannel">
		<int:header-enricher>
			<int:header name="SOAPAction" value="action" />
		</int:header-enricher>
		<int-ws:outbound-gateway destination-provider="uriProvider" />
	</int:chain>

	<bean id="uriProvider" class="${adapterData.packagename}.integration.util.OutboundDestinationURIProvider"/>

	<int:service-activator input-channel="serviceResponseChannel" output-channel="outboundChannel" ref="responseTranslator" />
	<bean id="responseTranslator" class="${adapterData.packagename}.integration.ResponseTranslator"/>

</beans>
