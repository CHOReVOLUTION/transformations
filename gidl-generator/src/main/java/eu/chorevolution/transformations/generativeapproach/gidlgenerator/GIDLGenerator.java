/*
  * Copyright 2015 The CHOReVOLUTION project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package eu.chorevolution.transformations.generativeapproach.gidlgenerator;

import java.io.IOException;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import eu.chorevolution.modelingnotations.gidl.ComplexType;
import eu.chorevolution.modelingnotations.gidl.ContextTypes;
import eu.chorevolution.modelingnotations.gidl.Data;
import eu.chorevolution.modelingnotations.gidl.DataType;
import eu.chorevolution.modelingnotations.gidl.GIDLModel;
import eu.chorevolution.modelingnotations.gidl.InterfaceDescription;
import eu.chorevolution.modelingnotations.gidl.OccurrencesTypes;
import eu.chorevolution.modelingnotations.gidl.OperationTypes;
import eu.chorevolution.modelingnotations.gidl.ProtocolTypes;
import eu.chorevolution.modelingnotations.gidl.QosTypes;
import eu.chorevolution.modelingnotations.gidl.RoleTypes;
import eu.chorevolution.modelingnotations.gidl.Scope;
import eu.chorevolution.modelingnotations.gidl.impl.GidlFactoryImpl;
import eu.chorevolution.transformations.generativeapproach.gidlgenerator.util.GIDLGeneratorUtils;
import eu.chorevolution.transformations.generativeapproach.gidlgenerator.util.TreeNode;
import eu.chorevolution.transformations.generativeapproach.gidlgenerator.util.TreeNodeDataType;
import eu.chorevolution.transformations.generativeapproach.gidlgenerator.util.WSDLUtils;

public class GIDLGenerator {
	private GIDLGeneratorRequest gidlGeneratorRequest;

	public GIDLGeneratorResponse generate(final GIDLGeneratorRequest gidlGeneratorRequest)
			throws GIDLGeneratorException {
		this.gidlGeneratorRequest = gidlGeneratorRequest;
		
		GIDLModel gidlModel = GidlFactoryImpl.eINSTANCE.createGIDLModel();
		// START FIX to use just only one interface provider
		InterfaceDescription providerInterfaceDescription = GidlFactoryImpl.eINSTANCE.createInterfaceDescription();
		providerInterfaceDescription.setRole(RoleTypes.PROVIDER);
		gidlModel.getHasInterfaces().add(providerInterfaceDescription);
		// END FIX

		//START FIX set protocol to SOAP
		gidlModel.setProtocol(ProtocolTypes.SOAP);
		//END FIX
		
		Definition wsdlDefinition = WSDLUtils.getWsdlDefinition(gidlGeneratorRequest.getWsdlContent());
		List<Operation> wsdlOperations = WSDLUtils.getOperations(wsdlDefinition, gidlGeneratorRequest.getName());

		for (Operation wsdlOperation : wsdlOperations) {
			eu.chorevolution.modelingnotations.gidl.Operation gidlOperation = GidlFactoryImpl.eINSTANCE
					.createOperation();
			
			gidlOperation.setName(wsdlOperation.getName());
			// START FIX to use OperationTypes.TWO_WAY_SYNC for each operation
			gidlOperation.setType(OperationTypes.TWO_WAY_SYNC);
			// END FIX
			
			Scope gidlScope = GIDLGeneratorUtils.createScope(wsdlOperation.getName());
			// START FIX GET is fixed
			gidlScope.setVerb("GET");
			// END FIX
			gidlOperation.setHasScope(gidlScope);
			
			// START FIX to use QosTypes.RELIABLE for each operation
			gidlOperation.setQos(QosTypes.RELIABLE);
			// END FIX
			
					
			try {
				//the first is the input message(s)
				for(int i=0; i<2; i++) {
					String msgKind = "0";
					if(i==0)
						msgKind = WSDLUtils.getONLY_INITIATING();
					else
						msgKind = WSDLUtils.getONLY_RECEIVING();
					
					List<String> opMessagesNames = WSDLUtils.getMessageNamesFromTaskWSDL(gidlGeneratorRequest.getWsdlContent(), wsdlOperation.getName(), msgKind);
					for(String opMessagesName : opMessagesNames) {
						Data opData = GidlFactoryImpl.eINSTANCE.createData();
						
						//TODO the example 1 will never work, is not correct WSDL: in the types, it misses tns:dfdfdf and there is only dfdfdf
						
						//TODO capire perche con il test case 3 non viene rappresentato correttamente transportMode
						//viene considerato simpletype, e viene messo unbounded.
						
						if(i==0) {
							opData.setName("request");
						}
						else{
							opData.setName("response");
						}
						opData.setContext(ContextTypes.BODY);

						List<String> msgTypes = WSDLUtils.getMessagesTypesFromMessageWSDL(gidlGeneratorRequest.getWsdlContent(), opMessagesName);					
						TreeNode<TreeNodeDataType> treeOfTypes = WSDLUtils.getCompleteTypeDefinitionsFromWSDL(gidlGeneratorRequest.getWsdlContent(), msgTypes.get(0));

						//List<String> hh = WSDLUtils.getFlattenedTreeFromTypeWSDL(gidlGeneratorRequest.getWsdlContent(), msgTypes.get(0));
						//for(String gg : hh)
						//	System.out.println(gg);
						
						DataType dt = WSDLUtils.getGIDLTreeFromTree(treeOfTypes);
						
						try {
							dt.getName().equals(null);
						}
						catch(Exception e) {
							if(i==0)
								dt.setName("RequestRoot");
							else
								dt.setName("ResponseRoot");
						}
						
						for(int z=0; z<((ComplexType)dt).getHasDataType().size(); z++)
							opData.getHasDataType().add(((ComplexType)dt).getHasDataType().get(z));
						
						//opData.getHasDataType().add(dt);
						
						if(i==0)
							gidlOperation.getInputData().add(opData);
						else
							gidlOperation.getOutputData().add(opData);
					}

				
				}
			} catch (SAXException | IOException | ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			providerInterfaceDescription.getHasOperations().add(gidlOperation);
			
		}

		return new GIDLGeneratorResponse(GIDLGeneratorUtils.geGIDLContent(gidlModel));
	}

}
