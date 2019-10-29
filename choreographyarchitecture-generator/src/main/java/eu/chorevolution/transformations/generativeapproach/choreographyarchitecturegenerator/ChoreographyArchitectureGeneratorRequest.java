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
package eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator;

import java.util.ArrayList;
import java.util.List;

import eu.chorevolution.transformations.generativeapproach.choreographyarchitecturegenerator.model.ComponentData;

public class ChoreographyArchitectureGeneratorRequest implements java.io.Serializable {
    private static final long serialVersionUID = 7046992133958679561L;

    private byte[] bpmn2Content;
    private List<ComponentData> clientParticipants;
    private List<ComponentData> prosumerParticipants;
    private List<ComponentData> providerParticipants;
   

    public ChoreographyArchitectureGeneratorRequest() {
        super();
        clientParticipants = new ArrayList<ComponentData>();
        prosumerParticipants = new ArrayList<ComponentData>();
        providerParticipants = new ArrayList<ComponentData>();
    }

   

    public ChoreographyArchitectureGeneratorRequest(byte[] bpmn2Content, List<ComponentData> clientParticipants,
            List<ComponentData> prosumerParticipants, List<ComponentData> providerParticipants) {
        super();
        this.bpmn2Content = bpmn2Content;
        this.clientParticipants = clientParticipants;
        this.prosumerParticipants = prosumerParticipants;
        this.providerParticipants = providerParticipants;
    }



    public byte[] getBpmn2Content() {
        return bpmn2Content;
    }

    public void setBpmn2Content(final byte[] bpmn2Content) {
        this.bpmn2Content = bpmn2Content;
    }

    public List<ComponentData> getClientParticipants() {
        return clientParticipants;
    }

    public void setClientParticipants(List<ComponentData> clientParticipants) {
        this.clientParticipants = clientParticipants;
    }

    public List<ComponentData> getProsumerParticipants() {
        return prosumerParticipants;
    }

    public void setProsumerParticipants(List<ComponentData> prosumerParticipants) {
        this.prosumerParticipants = prosumerParticipants;
    }

    public List<ComponentData> getProviderParticipants() {
        return providerParticipants;
    }

    public void setProviderParticipants(List<ComponentData> providerParticipants) {
        this.providerParticipants = providerParticipants;
    }

    

}
