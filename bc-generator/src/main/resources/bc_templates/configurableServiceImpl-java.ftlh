package ${bcData.packagename}.webservices;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ${bcData.packagename}.baseservice.ConfigurableService;
import ${bcData.packagename}.util.SetInvocationAddressUtils;

@Component(value = "ConfigurableServiceImpl")
public class ConfigurableServiceImpl implements ConfigurableService{

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableServiceImpl.class);
	
	@Override
	public void setInvocationAddress(String arg0, String arg1, List<String> arg2) {
		
		LOGGER.info("CALLED setInvocationAddress ON ${bcData.name}");
		SetInvocationAddressUtils.storeArtifactEndpointData(arg0, arg1, arg2);
	}

}
