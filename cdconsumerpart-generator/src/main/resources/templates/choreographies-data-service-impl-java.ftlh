package ${generatingJavaData.businessPackageName}.impl.dataservice;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import ${generatingJavaData.businessPackageName}.ChoreographyDataService;
import ${generatingJavaData.businessPackageName}.ChoreographyInstanceMessagesStore;
import ${generatingJavaData.modelPackageName}.MessagesData;

@Service
public class ChoreographiesDataServiceImpl<T> implements ChoreographyDataService {
	// choregraphyID, ChoreographyInstanceMessages
	private HashMap<String, ChoreographyInstanceMessagesStore<T>> choreographyInstancesMessages = new HashMap<>();

	public ChoreographyInstanceMessagesStore<T> getChoreographyInstanceMessages(String choreographyID) {
		
		if(!choreographyInstancesMessages.containsKey(choreographyID)){
			HashMap<String, List<MessagesData<T>>> map = new HashMap<String, List<MessagesData<T>>>();
			ChoreographyInstanceMessagesStore<T> choreographyInstanceMessages = new ChoreographyInstanceMessagesStoreImpl<T>(map);
			choreographyInstancesMessages.put(choreographyID, choreographyInstanceMessages);
		}			
		return choreographyInstancesMessages.get(choreographyID);
	}
	
}
