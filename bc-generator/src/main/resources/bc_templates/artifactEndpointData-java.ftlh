package ${bcData.packagename}.business.model;

import java.util.List;

public class ArtifactEndpointData {

	private String name;
	private String role;
	private List<String> endpoints;
	
	public ArtifactEndpointData() {
	}

	public ArtifactEndpointData(String name, String role, List<String> endpoints) {
		this.name = name;
		this.role = role;
		this.endpoints = endpoints;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public List<String> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<String> endpoints) {
		this.endpoints = endpoints;
	}
	
}
