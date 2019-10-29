/*
* Copyright 2017 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.adgenerator;

import java.io.File;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.EList;
import org.junit.Test;

import eu.chorevolution.modelingnotations.adapter.AdapterModel;
import eu.chorevolution.modelingnotations.adapter.ChoreographyComplexItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyDataItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyMessage;
import eu.chorevolution.modelingnotations.adapter.ChoreographySimpleItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyTask;
import eu.chorevolution.modelingnotations.adapter.ComplexItem;
import eu.chorevolution.modelingnotations.adapter.DataItem;
import eu.chorevolution.modelingnotations.adapter.DataItemRelation;
import eu.chorevolution.modelingnotations.adapter.EnumerationItemRelation;
import eu.chorevolution.modelingnotations.adapter.Message;
import eu.chorevolution.modelingnotations.adapter.MessageRelation;
import eu.chorevolution.modelingnotations.adapter.Operation;
import eu.chorevolution.modelingnotations.adapter.OperationRelation;
import eu.chorevolution.modelingnotations.adapter.SimpleItem;
import eu.chorevolution.transformations.generativeapproach.adgenerator.model.WSDLInfo;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.AdapterModelsUtility;
import eu.chorevolution.transformations.generativeapproach.adgenerator.util.WSDLUtility;

public class InputReadingTest {

	private static final String TEST_RESOURCES = "." + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar + "resources";

	private static final String ADAPTER_MODEL_FILE_NAME = "WP5/journeyplanner.adapter";
	private static final String WSDL_IN_FILE_NAME = "WP5/bcJourneyPlanner.wsdl";

	@Test
	public void wsdlReadingTest() {
		try {
			byte[] wsdl = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + WSDL_IN_FILE_NAME));
			WSDLInfo wsdlInfo = WSDLUtility.readWSDLInfo(wsdl);
			String serviceName = wsdlInfo.getServiceName();
			String targetNamespace = wsdlInfo.getTargetNS();
			Definition definition = wsdlInfo.getDefinition();
			Service service = (Service) definition.getAllServices().get(definition.getAllServices().keySet().toArray()[0]);
			String serviceLocationURI = ((SOAPAddress)service.getPort((String)service.getPorts().keySet().toArray()[0]).getExtensibilityElements().get(0)).getLocationURI();

			System.out.println("SERVICE NAME: " + serviceName + "\nTARGET NAMESPACE: " + targetNamespace + "\nLOCATION URI: " + serviceLocationURI);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	public void adapterModelReadingTest() {
		try {
			byte[] adapterModelByteStream = FileUtils.readFileToByteArray(new File(TEST_RESOURCES + File.separatorChar + ADAPTER_MODEL_FILE_NAME));
			AdapterModel model = AdapterModelsUtility.loadAdapterModel(adapterModelByteStream);
			EList<ChoreographyTask> tasks = model.getHasChoreographyTasks();
			for (ChoreographyTask task : tasks) {
				System.out.println("- Choreography Task: " + task.getName());
				EList<ChoreographyMessage> messages = task.getHasChoreographyMessages();
				for (ChoreographyMessage message : messages) {
					System.out.println("   - Message: " + message.getName() + " (" + message.getType() + ")");
					if (message.getHasChoreographyDataItem() != null) {
						ChoreographyDataItem rootDataItem = message.getHasChoreographyDataItem();
						recursiveChoreographyDataItemPrint(rootDataItem, 2);
					}
				}
			}
			System.out.println();
			EList<Operation> operations = model.getHasOperations();
			for (Operation operation : operations) {
				System.out.println("- Operation: " + operation.getName());
				EList<Message> messages = operation.getHasMessages();
				for (Message message : messages) {
					System.out.println("   - Message: " + message.getName() + " (" + message.getType() + ")");
					if (message.getHasMessageDataItem() != null) {
						DataItem rootDataItem = message.getHasMessageDataItem();
						recursiveDataItemPrint(rootDataItem, 2);
					}
				}
			}
			System.out.println();
			EList<OperationRelation> relations = model.getHasOperationsRelations();
			for (OperationRelation relation : relations) {
				// ChoreographyDataItem - DataItem
				System.out.print("- Operation Relation: task ");
				for (ChoreographyTask task : relation.getChoreographyTasks()) {
					System.out.print(task.getName() + " ");
				}
				System.out.print("-> operation ");
				for (Operation operation : relation.getOperations()) {
					System.out.print(operation.getName() + " ");
				}
				System.out.println();
				for (MessageRelation messageRelation : relation.getHasMessagesRelations()) {
					printIndentation(1);
					System.out.println("- Message Relation: choreographyMessage " + messageRelation.getChoreographyMessage().getName() + " -> message " + messageRelation.getMessage().getName());
					for (DataItemRelation dataItemRelation : messageRelation.getHasDataItemsRelations()) {
						printIndentation(2);
						System.out.print("- Data Item Relation: choreographyItem ");
						for (ChoreographyDataItem item : dataItemRelation.getChoreographyDataItem()) {
							System.out.print(item.getName() + " ");
						}
						System.out.print("-> item ");
						for (DataItem item : dataItemRelation.getDataItem()) {
							System.out.print(item.getName() + " ");
						}
						System.out.println();
						if (dataItemRelation.getHasEnumerationItemsRelations() != null) {
							for (EnumerationItemRelation enumerationRelation : dataItemRelation.getHasEnumerationItemsRelations()) {
								printIndentation(3);
								System.out.println(
										AdapterModelsUtility.getEnumerationItemValue(enumerationRelation.getChoreographyEnumerationItem()) +
										" -> " +
										AdapterModelsUtility.getEnumerationItemValue(enumerationRelation.getEnumerationItem()));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void recursiveChoreographyDataItemPrint(ChoreographyDataItem choreographyDataItem, int level) {
		if (! (choreographyDataItem instanceof ChoreographyComplexItem)) {
			ChoreographySimpleItem simpleItem = (ChoreographySimpleItem) choreographyDataItem;
			printIndentation(level);
			System.out.println("- SimpleItem: " + simpleItem.getName() + " (" + simpleItem.getType().getName() + ")");
			for (String enumerationValue : AdapterModelsUtility.getSimpleItemEnumerationValues(simpleItem)) {
				printIndentation(level+1);
				System.out.println("- EnumerationItem: " + enumerationValue);
			}
		} else {
			printIndentation(level);
			System.out.println("- ComplexItem: " + choreographyDataItem.getName() + " (" + ((ChoreographyComplexItem) choreographyDataItem).getTypeName() + ")");
			ChoreographyComplexItem complexItem = (ChoreographyComplexItem) choreographyDataItem;
			for (ChoreographyDataItem item : complexItem.getHasChoreographyDataItems()) {
				recursiveChoreographyDataItemPrint(item, level+1);
			}
		}
	}

	private static void recursiveDataItemPrint(DataItem dataItem, int level) {
		if (! (dataItem instanceof ComplexItem)) {
			SimpleItem simpleItem = (SimpleItem) dataItem;
			printIndentation(level);
			System.out.println("- SimpleItem: " + simpleItem.getName() + " (" + simpleItem.getType().getName() + ")");
			for (String enumerationValue : AdapterModelsUtility.getSimpleItemEnumerationValues(simpleItem)) {
				printIndentation(level+1);
				System.out.println("- EnumerationItem: " + enumerationValue);
			}
		} else {
			printIndentation(level);
			System.out.println("- ComplexItem: " + dataItem.getName());
			ComplexItem complexItem = (ComplexItem) dataItem;
			for (DataItem item : complexItem.getHasDataItems()) {
				recursiveDataItemPrint(item, level+1);
			}
		}
	}

	private static void printIndentation(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("   ");
		}
	}

}
