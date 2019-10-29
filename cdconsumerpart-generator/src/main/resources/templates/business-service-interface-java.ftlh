package ${generatingJavaData.businessPackageName};

<#list generatingJavaData.receivingOperations as receivingOperation>
import ${generatingJavaData.basePackageName}.${receivingOperation.inputBusinessMessageType};
<#if receivingOperation.requestResponse>
import ${generatingJavaData.basePackageName}.${receivingOperation.outputBusinessMessageType};
</#if>
</#list>  
<#list generatingJavaData.initiatingSendOperations as initiatingSendOperation>
import ${generatingJavaData.basePackageName}.${initiatingSendOperation.inputBusinessMessageType};
</#list> 
<#list generatingJavaData.initiatingReceiveOperations as initiatingReceiveOperation>
import ${generatingJavaData.basePackageName}.${initiatingReceiveOperation.outputBusinessMessageType};
</#list>  

import ${generatingJavaData.modelPackageName}.ChoreographyLoopIndexes;

public interface ${generatingJavaData.simpleName}Service {

<#list generatingJavaData.receivingOperations as receivingOperation>
<#if receivingOperation.requestResponse>
	${receivingOperation.outputBusinessMessageType} ${receivingOperation.name}(${receivingOperation.inputBusinessMessageType} parameter, String choreographyTaskName, String senderParticipantName);
<#else>
	void ${receivingOperation.name}(${receivingOperation.inputBusinessMessageType} parameter, String choreographyTaskName, String senderParticipantName);
</#if>
	      
</#list>
<#list generatingJavaData.initiatingSendOperations as initiatingSendOperation>
<#if initiatingSendOperation.loopIndexes>
	${initiatingSendOperation.inputBusinessMessageType} ${initiatingSendOperation.name?replace("send", "create")}(ChoreographyInstanceMessages choreographyInstanceMessages, String choreographyTaskName, String receiverParticipantName, ChoreographyLoopIndexes choreographyLoopIndexes);
<#else>
	${initiatingSendOperation.inputBusinessMessageType} ${initiatingSendOperation.name?replace("send", "create")}(ChoreographyInstanceMessages choreographyInstanceMessages, String choreographyTaskName, String receiverParticipantName);
</#if>
    
</#list>
<#list generatingJavaData.initiatingReceiveOperations as initiatingReceiveOperation>
    void ${initiatingReceiveOperation.name}(${initiatingReceiveOperation.outputBusinessMessageType} parameter, String choreographyTaskName, String senderParticipantName);
    
</#list>
}
