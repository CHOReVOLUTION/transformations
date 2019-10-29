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

import org.eclipse.bpmn2.Participant;

public class InterfaceDependency {
    private Participant initiatingParticipant;
    private Participant receivingParticipant;

    public InterfaceDependency(Participant initiatingParticipant, Participant receivingParticipant) {
        super();
        this.initiatingParticipant = initiatingParticipant;
        this.receivingParticipant = receivingParticipant;
    }

    public Participant getInitiatingParticipant() {
        return initiatingParticipant;
    }

    public Participant getReceivingParticipant() {
        return receivingParticipant;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof InterfaceDependency)) {
            return false;
        }
        InterfaceDependency interfaceDependency = (InterfaceDependency) object;
        return this.getInitiatingParticipant()
                .equals(interfaceDependency.getInitiatingParticipant())
                && this.getReceivingParticipant()
                        .equals(interfaceDependency.getReceivingParticipant());
    }

}
