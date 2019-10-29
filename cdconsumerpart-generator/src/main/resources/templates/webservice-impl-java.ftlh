package ${generatingJavaData.webServicePackageName};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ${generatingJavaData.basePackageName}.${generatingJavaData.portName};
<#list generatingJavaData.receivingOperations as receivingOperation>
import ${generatingJavaData.basePackageName}.${receivingOperation.inputMessageType};
import ${generatingJavaData.basePackageName}.${receivingOperation.outputMessageType};
<#if receivingOperation.requestResponse>
import ${generatingJavaData.basePackageName}.${receivingOperation.outputBusinessMessageType};
</#if>
</#list>  
<#list generatingJavaData.initiatingSendOperations as initiatingSendOperation>
import ${generatingJavaData.basePackageName}.${initiatingSendOperation.inputMessageType};
import ${generatingJavaData.basePackageName}.${initiatingSendOperation.outputMessageType};
import ${generatingJavaData.basePackageName}.${initiatingSendOperation.inputBusinessMessageType};
</#list>  
<#list generatingJavaData.initiatingReceiveOperations as initiatingReceiveOperation>
import ${generatingJavaData.basePackageName}.${initiatingReceiveOperation.inputMessageType};
import ${generatingJavaData.basePackageName}.${initiatingReceiveOperation.outputMessageType};
</#list> 

import ${generatingJavaData.businessPackageName}.ChoreographyDataService;
import ${generatingJavaData.businessPackageName}.ChoreographyInstanceMessagesStore;
import ${generatingJavaData.businessPackageName}.${generatingJavaData.simpleName}Service;

import ${generatingJavaData.modelPackageName}.ChoreographyLoopIndexes;

@Component(value="${generatingJavaData.javaName}")
public class ${generatingJavaData.javaName} implements ${generatingJavaData.portName} {
	
	private static Logger logger = LoggerFactory.getLogger(${generatingJavaData.javaName}.class);

	@Autowired
	private ${generatingJavaData.simpleName}Service service;
	
	@Autowired
	private ChoreographyDataService choreographyDataService;
	
<#list generatingJavaData.receivingOperations as receivingOperation>
    @Override
    public ${receivingOperation.outputMessageType} ${receivingOperation.name}(${receivingOperation.inputMessageType} parameters) {
    	logger.info("CALLED ${receivingOperation.name} ON ${generatingJavaData.cdName}");	
    	${receivingOperation.outputMessageType} result = new ${receivingOperation.outputMessageType}();
    	ChoreographyInstanceMessagesStore store = choreographyDataService.getChoreographyInstanceMessages(parameters.getChoreographyId().getChoreographyId());
		store.storeMessage(parameters.getSenderParticipantName(), parameters.getReceiverParticipantName(), parameters.getInputMessageName(), parameters.getChoreographyTaskName(), parameters.getInputMessageData());
	<#if receivingOperation.requestResponse>
  		${receivingOperation.outputBusinessMessageType} outputBusinessMessage = service.${receivingOperation.name}(parameters.getInputMessageData(), parameters.getChoreographyTaskName(), parameters.getSenderParticipantName());
  		result.setOutputMessageData(outputBusinessMessage);
  		store.storeMessage(parameters.getSenderParticipantName(), parameters.getReceiverParticipantName(), parameters.getOutputMessageName(), parameters.getChoreographyTaskName(), outputBusinessMessage);
	<#else>
  		service.${receivingOperation.name}(parameters.getInputMessageData(), parameters.getChoreographyTaskName(), parameters.getSenderParticipantName());
	</#if>
		
		return result;
    }
     
</#list>    	
<#list generatingJavaData.initiatingSendOperations as initiatingSendOperation>
    @Override
    public ${initiatingSendOperation.outputMessageType} ${initiatingSendOperation.name}(${initiatingSendOperation.inputMessageType} parameters) {
    	logger.info("CALLED ${initiatingSendOperation.name} ON ${generatingJavaData.cdName}");	
<#if initiatingSendOperation.loopIndexes>
		logger.info("${initiatingSendOperation.name} - loopIndexes= " + parameters.getLoopIndexes());
    	ChoreographyLoopIndexes choreographyLoopIndexes = new ChoreographyLoopIndexes(parameters.getLoopIndexes());
    	ChoreographyInstanceMessagesStore store = choreographyDataService.getChoreographyInstanceMessages(parameters.getChoreographyId().getChoreographyId());
		${initiatingSendOperation.inputBusinessMessageType} businessResult = service.${initiatingSendOperation.name?replace("send", "create")}(store, parameters.getChoreographyTaskName(), parameters.getReceiverParticipantName(), choreographyLoopIndexes);
<#else>
	    ChoreographyInstanceMessagesStore store = choreographyDataService.getChoreographyInstanceMessages(parameters.getChoreographyId().getChoreographyId());
		${initiatingSendOperation.inputBusinessMessageType} businessResult = service.${initiatingSendOperation.name?replace("send", "create")}(store, parameters.getChoreographyTaskName(), parameters.getReceiverParticipantName());
</#if>
    	store.storeMessage(parameters.getSenderParticipantName(), parameters.getReceiverParticipantName(), parameters.getInputMessageName(), parameters.getChoreographyTaskName(), businessResult);
		${initiatingSendOperation.outputMessageType} result = new ${initiatingSendOperation.outputMessageType}();
		result.setInputMessageData(businessResult);
	 
		return result;
    }
     
</#list>
<#list generatingJavaData.initiatingReceiveOperations as initiatingReceiveOperation>
	@Override
	public ${initiatingReceiveOperation.outputMessageType} ${initiatingReceiveOperation.name}(${initiatingReceiveOperation.inputMessageType} parameters) {
		logger.info("CALLED ${initiatingReceiveOperation.name} ON ${generatingJavaData.cdName}");
		ChoreographyInstanceMessagesStore store = choreographyDataService.getChoreographyInstanceMessages(parameters.getChoreographyId().getChoreographyId());
		store.storeMessage(parameters.getSenderParticipantName(), parameters.getReceiverParticipantName(), parameters.getOutputMessageName(), parameters.getChoreographyTaskName(), parameters.getOutputMessageData());
		service.${initiatingReceiveOperation.name}(parameters.getOutputMessageData(), parameters.getChoreographyTaskName(), parameters.getSenderParticipantName());
		return new ${initiatingReceiveOperation.outputMessageType}();
	}
     
</#list>

	
}
