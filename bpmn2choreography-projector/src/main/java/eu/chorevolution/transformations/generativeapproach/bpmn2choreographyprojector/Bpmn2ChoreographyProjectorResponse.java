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

public class Bpmn2ChoreographyProjectorResponse implements java.io.Serializable {
    private static final long serialVersionUID = 7046992133958679462L;

    private byte[] bpmn2Content;

    public Bpmn2ChoreographyProjectorResponse() {
        super();
    }

    public Bpmn2ChoreographyProjectorResponse(final byte[] bpmn2Content, final byte[] xsdTypesContent) {
        super();
        this.bpmn2Content = bpmn2Content;
    }

    public byte[] getBpmn2Content() {
        return bpmn2Content;
    }

    public void setBpmn2Content(final byte[] bpmn2Content) {
        this.bpmn2Content = bpmn2Content;
    }

}
