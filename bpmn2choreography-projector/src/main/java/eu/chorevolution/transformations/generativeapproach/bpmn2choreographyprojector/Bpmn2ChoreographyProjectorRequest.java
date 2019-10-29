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
package eu.chorevolution.transformations.generativeapproach.bpmn2choreographyprojector;

public class Bpmn2ChoreographyProjectorRequest implements java.io.Serializable {
    private static final long serialVersionUID = 7046992133958679461L;

    private byte[] bpmn2Content;
    private String participantUsedToBpmn2Projection;

    public Bpmn2ChoreographyProjectorRequest() {
        super();
    }

    public Bpmn2ChoreographyProjectorRequest(final byte[] bpmn2Content,
            final String participantUsedToBpmn2Projection) {
        super();
        this.bpmn2Content = bpmn2Content;
        this.participantUsedToBpmn2Projection = participantUsedToBpmn2Projection;
    }

    public byte[] getBpmn2Content() {
        return bpmn2Content;
    }

    public void setBpmn2Content(final byte[] bpmn2Content) {
        this.bpmn2Content = bpmn2Content;
    }

    public String getParticipantUsedToBpmn2Projection() {
        return participantUsedToBpmn2Projection;
    }

    public void setParticipantUsedToBpmn2Projection(final String participantUsedToBpmn2Projection) {
        this.participantUsedToBpmn2Projection = participantUsedToBpmn2Projection;
    }

}
