package ${bcData.packagename}.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ${bcData.packagename}.business.model.ArtifactEndpointData;

public class SetInvocationAddressUtils {
	// role, ArtifactAddressData
	private static Map<String, ArtifactEndpointData> artifactsEndpointsData = new HashMap<>();
	
	public static void storeArtifactEndpointData(String role, String name, List<String> endpoints){
		
		artifactsEndpointsData.put(role, new ArtifactEndpointData(name, role, endpoints));
	}
	
	public static String getArtifactEndpointAddressFromRole(String role){
		
		if(artifactsEndpointsData.containsKey(role))
			return artifactsEndpointsData.get(role).getEndpoints().get(0);
		else
			return null;
	}

	public static String getTargetEndpointAddress() {
		if(!artifactsEndpointsData.values().isEmpty()){
			return artifactsEndpointsData.values().iterator().next().getEndpoints().get(0);
		} else {
			return null;
		}
	}
}
