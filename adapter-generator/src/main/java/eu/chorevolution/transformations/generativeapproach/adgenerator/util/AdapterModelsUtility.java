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
package eu.chorevolution.transformations.generativeapproach.adgenerator.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import eu.chorevolution.modelingnotations.adapter.AdapterModel;
import eu.chorevolution.modelingnotations.adapter.ChoreographyComplexItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyDataItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyEnumerationItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyMessage;
import eu.chorevolution.modelingnotations.adapter.ChoreographyMessageType;
import eu.chorevolution.modelingnotations.adapter.ChoreographySimpleItem;
import eu.chorevolution.modelingnotations.adapter.ChoreographyTask;
import eu.chorevolution.modelingnotations.adapter.ComplexItem;
import eu.chorevolution.modelingnotations.adapter.DataItem;
import eu.chorevolution.modelingnotations.adapter.DataItemRelation;
import eu.chorevolution.modelingnotations.adapter.DateEnumerationItem;
import eu.chorevolution.modelingnotations.adapter.DoubleEnumerationItem;
import eu.chorevolution.modelingnotations.adapter.EnumerationItem;
import eu.chorevolution.modelingnotations.adapter.EnumerationItemRelation;
import eu.chorevolution.modelingnotations.adapter.IntegerEnumerationItem;
import eu.chorevolution.modelingnotations.adapter.Message;
import eu.chorevolution.modelingnotations.adapter.MessageRelation;
import eu.chorevolution.modelingnotations.adapter.MessageType;
import eu.chorevolution.modelingnotations.adapter.Operation;
import eu.chorevolution.modelingnotations.adapter.OperationRelation;
import eu.chorevolution.modelingnotations.adapter.SimpleItem;
import eu.chorevolution.modelingnotations.adapter.StringEnumerationItem;
import eu.chorevolution.modelingnotations.adapter.impl.AdapterPackageImpl;
import eu.chorevolution.transformations.generativeapproach.adgenerator.ADGeneratorException;

public class AdapterModelsUtility {

	private static final String TEMPFILE_SUFFIX = "adaptergenerator";
	private static final String ADAPTER_FILE_EXTENSION = ".adapter";

	public static AdapterModel loadAdapterModel(final byte[] adapterModelContent)
			throws ADGeneratorException {
		File adapterModelFile;
		try {
			adapterModelFile = File.createTempFile(TEMPFILE_SUFFIX,
					ADAPTER_FILE_EXTENSION);
			IOUtils.write(adapterModelContent, FileUtils.openOutputStream(adapterModelFile));
		} catch (IOException e1) {
			throw new ADGeneratorException(
					"Internal Error while creating the Choreography Architecture");
		}

		URI adapterModelURI = URI.createURI(adapterModelFile.toURI().toString());

		AdapterPackageImpl.init();
		Resource resource = new XMIResourceFactoryImpl().createResource(adapterModelURI);

		try {
			resource.load(null);

		} catch (IOException e) {
			e.printStackTrace();
//			throw new ADGeneratorException(
//					"Error to load the resource: " + resource.getURI().toFileString());
		} finally {
			FileDeleteStrategy.FORCE.deleteQuietly(adapterModelFile);
		}

		return (AdapterModel) resource.getContents().get(0);
	}

	public static String getInputRootElementName(ChoreographyTask choreographyTask) {
		if (choreographyTask != null) {
			EList<ChoreographyMessage> messages = choreographyTask.getHasChoreographyMessages();
			if (messages != null) {
				for (ChoreographyMessage message : messages) {
					if (message.getType().equals(ChoreographyMessageType.INITIATING)) {
						return message.getHasChoreographyDataItem().getName();
					}
				}
			}
		}
		return null;
	}

	public static String getOutputRootElementName(ChoreographyTask choreographyTask) {
		if (choreographyTask != null) {
			EList<ChoreographyMessage> messages = choreographyTask.getHasChoreographyMessages();
			if (messages != null) {
				for (ChoreographyMessage message : messages) {
					if (message.getType().equals(ChoreographyMessageType.RETURN)) {
						return message.getHasChoreographyDataItem().getName();
					}
				}
			}
		}
		return null;
	}

	public static String getInputOperationRootElementName(Operation operation) {
		if (operation != null) {
			EList<Message> messages = operation.getHasMessages();
			if (messages != null) {
				for (Message message : messages) {
					if (message.getType().equals(MessageType.INPUT)) {
						return message.getHasMessageDataItem().getName();
					}
				}
			}
		}
		return null;
	}

	public static String getOutputOperationRootElementName(Operation operation) {
		if (operation != null) {
			EList<Message> messages = operation.getHasMessages();
			if (messages != null) {
				for (Message message : messages) {
					if (message.getType().equals(MessageType.OUTPUT)) {
						return message.getHasMessageDataItem().getName();
					}
				}
			}
		}
		return null;
	}

	public static Message getInputMessage(AdapterModel adapterModel) {
		Operation operation = adapterModel.getHasOperations().get(0);
		EList<Message> messages = operation.getHasMessages();
		if (messages != null) {
			for (Message message : messages) {
				if (message.getType().equals(MessageType.INPUT)) {
					return message;
				}
			}
		}
		return null;
	}

	public static ChoreographyMessage getChoreographyReturnMessage(AdapterModel adapterModel) {
		ChoreographyTask task = adapterModel.getHasChoreographyTasks().get(0);
		EList<ChoreographyMessage> choreographyMessages = task.getHasChoreographyMessages();
		if (choreographyMessages != null) {
			for (ChoreographyMessage message : choreographyMessages) {
				if (message.getType().equals(ChoreographyMessageType.RETURN)) {
					return message;
				}
			}
		}
		return null;
	}

	public static List<DataItem> getMessageInnerDataItems(Message message) {
		List<DataItem> result = new ArrayList<>();
		if (message != null) {
			DataItem rootItem = message.getHasMessageDataItem();
			if (rootItem != null && rootItem instanceof SimpleItem) {
				result.add(rootItem);
			} else {
				ComplexItem complexRootItem = (ComplexItem) rootItem;
				if (complexRootItem.getHasDataItems() != null) {
					for (DataItem item : complexRootItem.getHasDataItems()) {
						result.add(item);
					}
				}
			}
		}
		return result;
	}

	public static List<ChoreographyDataItem> getChoreographyMessageInnerDataItems(ChoreographyMessage message) {
		List<ChoreographyDataItem> result = new ArrayList<>();
		if (message != null) {
			ChoreographyDataItem rootItem = message.getHasChoreographyDataItem();
			if (rootItem != null && rootItem instanceof ChoreographySimpleItem) {
				result.add(rootItem);
			} else {
				ChoreographyComplexItem complexRootItem = (ChoreographyComplexItem) rootItem;
				if (complexRootItem.getHasChoreographyDataItems() != null) {
					for (ChoreographyDataItem item : complexRootItem.getHasChoreographyDataItems()) {
						result.add(item);
					}
				}
			}
		}
		return result;
	}

	public static List<String> getSimpleItemEnumerationValues(ChoreographySimpleItem item) {
		List<String> values = new ArrayList<>();
		if (!item.getHasChoreographyEnumerationItems().isEmpty()) {
			for (ChoreographyEnumerationItem enumerationItem : item.getHasChoreographyEnumerationItems()) {
				values.add(getEnumerationItemValue(enumerationItem));
			}
		}
		return values;
	}

	public static List<String> getSimpleItemEnumerationValues(SimpleItem item) {
		List<String> values = new ArrayList<>();
		if (!item.getHasEnumerationItems().isEmpty()) {
			for (EnumerationItem enumerationItem : item.getHasEnumerationItems()) {
				values.add(getEnumerationItemValue(enumerationItem));
			}
		}
		return values;
	}

	public static String getEnumerationItemValue(EnumerationItem enumerationItem) {
		String enumerationValue = "";
		switch(enumerationItem.getClass().getSimpleName()) {
			case "StringEnumerationItemImpl":
				enumerationValue = ((StringEnumerationItem) enumerationItem).getValue();
				break;
			case "IntegerEnumerationItemImpl":
				enumerationValue += ((IntegerEnumerationItem) enumerationItem).getValue();
				break;
			case "DoubleEnumerationItemImpl":
				enumerationValue += ((DoubleEnumerationItem) enumerationItem).getValue();
				break;
			case "DateEnumerationItemImpl":
				enumerationValue = ((DateEnumerationItem) enumerationItem).getValue().toString();
				break;
		}
		return enumerationValue;
	}

	public static String getEnumerationItemValue(ChoreographyEnumerationItem enumerationItem) {
		String enumerationValue = "";
		switch(enumerationItem.getClass().getSimpleName()) {
			case "StringEnumerationItemImpl":
				enumerationValue = ((StringEnumerationItem) enumerationItem).getValue();
				break;
			case "IntegerEnumerationItemImpl":
				enumerationValue += ((IntegerEnumerationItem) enumerationItem).getValue();
				break;
			case "DoubleEnumerationItemImpl":
				enumerationValue += ((DoubleEnumerationItem) enumerationItem).getValue();
				break;
			case "DateEnumerationItemImpl":
				enumerationValue = ((DateEnumerationItem) enumerationItem).getValue().toString();
				break;
		}
		return enumerationValue;
	}

	public static MessageRelation findMessageRelation(Message message, AdapterModel adapterModel) {
		if (message != null) {
			EList<OperationRelation> operationRelations = adapterModel.getHasOperationsRelations();
			if (operationRelations != null) {
				for (OperationRelation operationRelation : operationRelations) {
					EList<MessageRelation> messageRelations = operationRelation.getHasMessagesRelations();
					if (messageRelations != null) {
						for (MessageRelation messageRelation : messageRelations) {
							Message messageInRelation = messageRelation.getMessage();
							if (messageInRelation != null && messageInRelation.equals(message)) {
								return messageRelation;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static MessageRelation findChoreographyMessageRelation(ChoreographyMessage message, AdapterModel adapterModel) {
		if (message != null) {
			EList<OperationRelation> operationRelations = adapterModel.getHasOperationsRelations();
			if (operationRelations != null) {
				for (OperationRelation operationRelation : operationRelations) {
					EList<MessageRelation> messageRelations = operationRelation.getHasMessagesRelations();
					if (messageRelations != null) {
						for (MessageRelation messageRelation : messageRelations) {
							ChoreographyMessage messageInRelation = messageRelation.getChoreographyMessage();
							if (messageInRelation != null && messageInRelation.equals(message)) {
								return messageRelation;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static DataItemRelation findDataItemRelation(MessageRelation messageRelation, DataItem dataItem) {
		if (messageRelation.getHasDataItemsRelations() != null) {
			for (DataItemRelation relation : messageRelation.getHasDataItemsRelations()) {
				if (relation.getDataItem() != null) {
					for (DataItem item : relation.getDataItem()) {
						if (item.equals(dataItem)) {
							return relation;
						}
					}
				}
			}
		}
		return null;
	}

	public static DataItemRelation findDataItemRelation(MessageRelation messageRelation, ChoreographyDataItem choreographyDataItem) {
		if (messageRelation.getHasDataItemsRelations() != null) {
			for (DataItemRelation relation : messageRelation.getHasDataItemsRelations()) {
				if (relation.getDataItem() != null) {
					for (ChoreographyDataItem item : relation.getChoreographyDataItem()) {
						if (item.equals(choreographyDataItem)) {
							return relation;
						}
					}
				}
			}
		}
		return null;
	}

	public static EList<DataItem> getRelatedDataItems(ChoreographyDataItem choreographyDataItem, MessageRelation messageRelation) {
		if (messageRelation.getHasDataItemsRelations() != null) {
			for (DataItemRelation relation : messageRelation.getHasDataItemsRelations()) {
				EList<ChoreographyDataItem> choreographyDataItems = relation.getChoreographyDataItem();
				if (choreographyDataItems.contains(choreographyDataItem)) {
					return relation.getDataItem();
				}
			}
		}
		return new BasicEList<>();
	}

	public static EList<ChoreographyDataItem> getRelatedChoreographyDataItems(DataItem dataItem, MessageRelation messageRelation) {
		if (messageRelation.getHasDataItemsRelations() != null) {
			for (DataItemRelation relation : messageRelation.getHasDataItemsRelations()) {
				EList<DataItem> choreographyDataItems = relation.getDataItem();
				if (choreographyDataItems.contains(dataItem)) {
					return relation.getChoreographyDataItem();
				}
			}
		}
		return new BasicEList<>();
	}

	public static String getTransformationRule(ChoreographyDataItem choreographyDataItem, MessageRelation messageRelation) {
		if (messageRelation.getHasDataItemsRelations() != null) {
			for (DataItemRelation relation : messageRelation.getHasDataItemsRelations()) {
				EList<ChoreographyDataItem> choreographyDataItems = relation.getChoreographyDataItem();
				if (choreographyDataItems.contains(choreographyDataItem)) {
					return relation.getTransformationRule();
				}
			}
		}
		return null;
	}

	public static String getTransformationRule(DataItem dataItem, MessageRelation messageRelation) {
		if (messageRelation.getHasDataItemsRelations() != null) {
			for (DataItemRelation relation : messageRelation.getHasDataItemsRelations()) {
				EList<DataItem> dataItems = relation.getDataItem();
				if (dataItems.contains(dataItem)) {
					return relation.getTransformationRule();
				}
			}
		}
		return null;
	}

	public static EnumerationItem getRelatedEnumerationValue(ChoreographyEnumerationItem choreographyEnumerationItem, DataItemRelation dataItemRelation) {
		if(dataItemRelation.getHasEnumerationItemsRelations() != null) {
			for(EnumerationItemRelation relation : dataItemRelation.getHasEnumerationItemsRelations()) {
				if (relation.getChoreographyEnumerationItem().equals(choreographyEnumerationItem)) {
					return relation.getEnumerationItem();
				}
			}
		}
		return null;
	}

	public static ChoreographyEnumerationItem getRelatedEnumerationValue(EnumerationItem enumerationItem, DataItemRelation dataItemRelation) {
		if(dataItemRelation.getHasEnumerationItemsRelations() != null) {
			for(EnumerationItemRelation relation : dataItemRelation.getHasEnumerationItemsRelations()) {
				if (relation.getEnumerationItem().equals(enumerationItem)) {
					return relation.getChoreographyEnumerationItem();
				}
			}
		}
		return null;
	}

	public static boolean hasEnumeratedValues(ChoreographyDataItem choreographyDataItem) {
		if (choreographyDataItem instanceof SimpleItem) {
			return ((ChoreographySimpleItem) choreographyDataItem).getHasChoreographyEnumerationItems() != null;
		} else {
			return false;
		}
	}

	public static String getRootPath(ChoreographyMessage choreographyMessage, ChoreographyDataItem dataItem) {
		ChoreographyDataItem rootDataItem = choreographyMessage.getHasChoreographyDataItem();
		if (rootDataItem.equals(dataItem)) {
			return "";
		} else if (rootDataItem instanceof ChoreographyComplexItem) {
			List<ChoreographyDataItem> innerItems = ((ChoreographyComplexItem) rootDataItem).getHasChoreographyDataItems();
			return findPath(innerItems, dataItem, "");
		} else {
			return null;
		}
	}

	public static String getRootPath(Message message, DataItem dataItem) {
		DataItem rootDataItem = message.getHasMessageDataItem();
		if (rootDataItem.equals(dataItem)) {
			return "";
		} else if (rootDataItem instanceof ComplexItem) {
			List<DataItem> innerItems = ((ComplexItem) rootDataItem).getHasDataItems();
			return findPath(innerItems, dataItem, "");
		} else {
			return null;
		}
	}

	private static String findPath(List<ChoreographyDataItem> itemList, ChoreographyDataItem itemToFind, String currentPath) {
		List<ChoreographyComplexItem> innerComplexItems = new ArrayList<>();
		for (ChoreographyDataItem currentItem : itemList) {
			if (currentItem.equals(itemToFind)) {
				//return currentPath.isEmpty() ? currentItem.getName() : currentPath + "." + currentItem.getName();
				return currentPath;
			} else if (currentItem instanceof ChoreographyComplexItem) {
				innerComplexItems.add((ChoreographyComplexItem)currentItem);
			}
		}
		for (ChoreographyComplexItem innerComplexItem : innerComplexItems) {
			List<ChoreographyDataItem> nextSearchingList = innerComplexItem.getHasChoreographyDataItems();
			if (nextSearchingList != null) {
				String searchingPath = currentPath.isEmpty() ? innerComplexItem.getName() : currentPath + "." + innerComplexItem.getName();
				String guessedPath = findPath(nextSearchingList, itemToFind, searchingPath);
				if (guessedPath!=null) {
					return guessedPath;
				}
			}
 		}
		return null;
	}

	private static String findPath(List<DataItem> itemList, DataItem itemToFind, String currentPath) {
		List<ComplexItem> innerComplexItems = new ArrayList<>();
		for (DataItem currentItem : itemList) {
			if (currentItem.equals(itemToFind)) {
				//return currentPath.isEmpty() ? currentItem.getName() : currentPath + "." + currentItem.getName();
				return currentPath;
			} else if (currentItem instanceof ComplexItem) {
				innerComplexItems.add((ComplexItem)currentItem);
			}
		}
		for (ComplexItem innerComplexItem : innerComplexItems) {
			List<DataItem> nextSearchingList = innerComplexItem.getHasDataItems();
			if (nextSearchingList != null) {
				String searchingPath = currentPath.isEmpty() ? innerComplexItem.getName() : currentPath + "." + innerComplexItem.getName();
				String guessedPath = findPath(nextSearchingList, itemToFind, searchingPath);
				if (guessedPath!=null) {
					return guessedPath;
				}
			}
 		}
		return null;
	}
}
