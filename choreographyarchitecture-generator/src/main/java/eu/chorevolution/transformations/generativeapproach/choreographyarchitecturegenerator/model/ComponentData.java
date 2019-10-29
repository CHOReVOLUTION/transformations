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
package eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model;

public class ComponentData implements java.io.Serializable {
    private static final long serialVersionUID = 7046992133958679563L;

    private String participantName;
    private String name;
    private String location;

    private ConsumerComponentData consumerComponentData;
    private SecurityComponentData securityComponentData;
    private BindingComponentData bindingComponentData;
    private AdapterComponentData adapterComponentData;
    
    public ComponentData(String participantName, String name, String location) {
        super();
        this.participantName = participantName;
        this.name = name;
        this.location = location;
    }
    
    public ComponentData(String participantName, String name, String location,
            ConsumerComponentData consumerComponentData,
            SecurityComponentData securityComponentData,
            BindingComponentData bindingComponentData,
            AdapterComponentData adapterComponentData) {
        super();
        this.participantName = participantName;
        this.name = name;
        this.location = location;
        this.consumerComponentData = consumerComponentData;
        this.securityComponentData = securityComponentData;
        this.bindingComponentData = bindingComponentData;
        this.adapterComponentData = adapterComponentData;
    }
    
    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ConsumerComponentData getConsumerComponentData() {
        return consumerComponentData;
    }

    public void setConsumerComponentData(ConsumerComponentData consumerComponentData) {
        this.consumerComponentData = consumerComponentData;
    }

	public SecurityComponentData getSecurityComponentData() {
		return securityComponentData;
	}

	public void setSecurityComponentData(SecurityComponentData securityComponentData) {
		this.securityComponentData = securityComponentData;
	}

	public BindingComponentData getBindingComponentData() {
		return bindingComponentData;
	}

	public void setBindingComponentData(BindingComponentData bindingComponentData) {
		this.bindingComponentData = bindingComponentData;
	}
	
	public AdapterComponentData getAdapterComponentData() {
		return adapterComponentData;
	}

	public void setAdapterComponentData(AdapterComponentData adapterComponentData) {
		this.adapterComponentData = adapterComponentData;
	}

	@Override
    public boolean equals(Object obj){
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ComponentData))return false;
        ComponentData componentData = (ComponentData)obj;
        return this.getParticipantName().equals(componentData.getParticipantName());
    }

}
