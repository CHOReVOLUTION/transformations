package ${adapterData.packagename}.integration;

import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;

import ${adapterData.packagename}.integration.util.TransformationUtils;

@Component
public class RequestData {

	private NodeList nodes;

	public void initData(NodeList nodes) {
		this.nodes = nodes;
	}

	public String getRequestData(String nodePath) {
		return TransformationUtils.findNodeValue(nodes, nodePath);
	}
	
}
