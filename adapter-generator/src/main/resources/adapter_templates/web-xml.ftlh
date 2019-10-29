<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xmlns="http://xmlns.jcp.org/xml/ns/javaee" 
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd" 
         id="WebApp_ID" 
         version="3.1">
	<display-name>${adapterData.name}</display-name>
    
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>
            /WEB-INF/spring/root-context.xml
        </param-value>
    </context-param>
    
	<servlet>
		<display-name>${adapterData.name}</display-name>
		<servlet-name>${adapterData.name}</servlet-name>
		<servlet-class>org.springframework.ws.transport.http.MessageDispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>/WEB-INF/spring/integration/si-components.xml</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>${adapterData.name}</servlet-name>
		<url-pattern>/${adapterData.name}</url-pattern>
	</servlet-mapping>

	<servlet>
		<display-name>invocationAddressServlet</display-name>
		<servlet-name>invocationAddressServlet</servlet-name>
		<servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
		<init-param>
			<param-name>config-location</param-name>
			<param-value>/WEB-INF/spring/webmvc-config.xml</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>invocationAddressServlet</servlet-name>
		<url-pattern>/BaseService</url-pattern>
	</servlet-mapping>
	<session-config>
		<session-timeout>60</session-timeout>
	</session-config>

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

</web-app>