/*
* Copyright 2016 The CHOReVOLUTION project
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
package eu.chorevolution.transformations.generativeapproach.cdgenerator.util.bpel.writer;

import java.util.Iterator;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Links;
import org.eclipse.emf.ecore.EObject;

/**
 * Base implementation of LinkResolver. This resolves all link
 * as defined in the BPEL specifications.
 * 
 */
public class BPELLinkResolver implements LinkResolver {

	public Link getLink(Activity activity, String linkName) {
		if (activity != null) {
			EObject container = activity.eContainer();
			while (container != null) {
				if (container instanceof Flow) {
					Links links =((Flow)container).getLinks();
					if (links != null) {
						for (Iterator<Link> it = links.getChildren().iterator(); it.hasNext(); ) {
							Link candidate = it.next(); 		
							if (candidate.getName().equals(linkName)) 
								return candidate;
						}
					}
				}
				container = container.eContainer();
			}
		}
		return null;		
	}
}
