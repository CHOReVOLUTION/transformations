package ${adapterData.packagename}.integration.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.support.destination.DestinationProvider;

import ${adapterData.packagename}.business.util.SetInvocationAddressUtils;

@Component
public class OutboundDestinationURIProvider implements DestinationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutboundDestinationURIProvider.class);

	@Override
	public URI getDestination() {
		try {
// 			String address = SetInvocationAddressUtils.getArtifactEndpointAddressFromRole("${adapterData.roleName}");
			String address = SetInvocationAddressUtils.getTargetEndpointAddress();
			LOGGER.info("Invocation address for ${adapterData.roleName}: " + address);			
			return new URI(address);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
}
