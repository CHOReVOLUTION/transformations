<?xml version="1.0" encoding="UTF-8"?>
<definitions name="${bcData.servicename}"
	targetNamespace="${bcData.targetNamespace}"
	xmlns="http://schemas.xmlsoap.org/wsdl/" xmlns:wsp="http://www.w3.org/ns/ws-policy"
	xmlns:tns="${bcData.targetNamespace}"
	xmlns:wsp1_2="http://schemas.xmlsoap.org/ws/2004/09/policy"
	xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
	xmlns:wsam="http://www.w3.org/2007/05/addressing/metadata">

	<types>
		<xsd:schema targetNamespace="${bcData.targetNamespace}">
			<xsd:element name="${inputMessageName}" type="tns:${inputMessageName}" />
			<xsd:element name="${outputMessageName}" type="tns:${outputMessageName}" />

			<#list types.wsdlItems as name, item>
			<#if item.isComplex()>
			<@complexType item />
			<#elseif item.isEnumeration()>
			<@enumerationType item />
			<#else>
			<@elment item />
			</#if>
			</#list>

			<#macro complexType item>
			<xsd:complexType name="${item.type}">
				<xsd:sequence>
					<#list item.innerItems as item>
					<@element item />
					</#list>
				</xsd:sequence>
			</xsd:complexType>
			</#macro>

			<#macro enumerationType item>
			<xsd:simpleType name="${item.name}">
				<xsd:restriction base="xsd:${item.type}">
				<#list item.enumerationValues as value>
					<xsd:enumeration value="${value}" />
				</#list>
				</xsd:restriction>
			</xsd:simpleType>
			</#macro>

					<#macro element item>
					<#if item.isComplex()>
					<xsd:element name="${item.name}" type="tns:${item.type}" <#if item.minOccurs!="1">minOccurs="${item.minOccurs}"</#if> <#if item.maxOccurs!="1">maxOccurs="${item.maxOccurs}"</#if>/>
					<#elseif item.isEnumeration()>
					<xsd:element name="${item.name}" type="tns:${item.name}" <#if item.minOccurs!="1">minOccurs="${item.minOccurs}"</#if> <#if item.maxOccurs!="1">maxOccurs="${item.maxOccurs}"</#if>/>
					<#else>
					<xsd:element name="${item.name}" type="xsd:<#if item.type="integer">int<#else>${item.type}</#if>" <#if item.minOccurs!="1">minOccurs="${item.minOccurs}"</#if> <#if item.maxOccurs!="1">maxOccurs="${item.maxOccurs}"</#if>/>
					</#if>
					</#macro>
		</xsd:schema>
	</types>
	<message name="${inputMessageName}">
		<part name="parameters" element="tns:${inputMessageName}">
		</part>
	</message>
	<message name="${outputMessageName}">
		<part name="parameters" element="tns:${outputMessageName}">
		</part>
	</message>
	<portType name="${bcData.servicename}PT">
		<operation name="${operationName}">
			<input message="tns:${inputMessageName}">
			</input>
			<output message="tns:${outputMessageName}">
			</output>
		</operation>
	</portType>
	<binding name="${bcData.servicename}Binding" type="tns:${bcData.servicename}PT">
		<soap:binding style="document"
			transport="http://schemas.xmlsoap.org/soap/http" />
		<operation name="${operationName}">
			<soap:operation soapAction="" />
			<input>
				<soap:body use="literal" />
			</input>
			<output>
				<soap:body use="literal" />
			</output>
		</operation>
	</binding>
	<service name="${bcData.servicename}Service">
		<port name="${bcData.servicename}Port" binding="tns:${bcData.servicename}Binding">
			<soap:address location="http://localhost:8080/ode/processes/${bcData.name}.${bcData.servicename}Port/" />
		</port>
	</service>
</definitions>
