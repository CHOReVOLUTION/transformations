package ${adapterData.packagename}.business.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ${adapterData.packagename}.business.model.ArtifactEndpointData;

public class SetInvocationAddressUtils {

	private static final String DEFAULT_ADDRESS = "${adapterData.serviceLocationURI}";

	private static final Logger LOGGER = LoggerFactory.getLogger(SetInvocationAddressUtils.class);

	// role, ArtifactAddressData
	private static Map<String, ArtifactEndpointData> artifactsEndpointsData = new HashMap<>();
	
	public static void storeArtifactEndpointData(String role, String name, List<String> endpoints){
		
		artifactsEndpointsData.put(role, new ArtifactEndpointData(name, role, endpoints));
		LOGGER.info("Adapter ${adapterData.name} set invocation address for artifact " + name + " with role " + role + " to " + endpoints.get(0));
	}
	
	public static String getArtifactEndpointAddressFromRole(String role){
		
		if(artifactsEndpointsData.containsKey(role)) {
			String address = artifactsEndpointsData.get(role).getEndpoints().get(0);
			LOGGER.info("Adapter ${adapterData.name} found invocation address to call artifact with role " + role + ": " + address);
			return address;
		} else {
			LOGGER.info("Adapter ${adapterData.name} has NOT found invocation address to call artifact with role " + role);
			return DEFAULT_ADDRESS;
		}
	}

	public static String getTargetEndpointAddress() {
		if(!artifactsEndpointsData.values().isEmpty()){
			String address = artifactsEndpointsData.values().iterator().next().getEndpoints().get(0);
			LOGGER.info("Adapter ${adapterData.name} found invocation addres to call target artifact: " + address);
			return address;
		} else {
			LOGGER.info("Adapter ${adapterData.name} is using default address to call target artifact: " + DEFAULT_ADDRESS);
			return DEFAULT_ADDRESS;
		}
	}
}
