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

public class SecurityComponentData implements java.io.Serializable {
    private static final long serialVersionUID = 7046992133958679565L;

    private String name;
    private String location;

    public SecurityComponentData(String name, String location) {
        super();
        this.name = name;
        this.location = location;
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
    
    @Override
    public boolean equals(Object obj){
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SecurityComponentData))return false;
        SecurityComponentData securityComponentData = (SecurityComponentData)obj;
        return this.getName().equals(securityComponentData.getName());
    }

}
