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

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;


@SuppressWarnings({"nls","boxing"})

public class LineCapturingDOMParser extends org.apache.xerces.parsers.DOMParser {
 
	protected XMLLocator mLocator;

	protected int fLineNo = 0;

	protected int fColumnNo = 0;

	protected int fOffset = 0;

	void lastSource() {

		fLineNo = mLocator.getLineNumber();
		fColumnNo = mLocator.getColumnNumber();
		fOffset = mLocator.getCharacterOffset();

	}

	/**
	 * @see org.apache.xerces.parsers.AbstractDOMParser#startDocument(org.apache.xerces.xni.XMLLocator,
	 *      java.lang.String, org.apache.xerces.xni.NamespaceContext,
	 *      org.apache.xerces.xni.Augmentations)
	 */
	@Override
	public void startDocument(XMLLocator arg0, String arg1,
			NamespaceContext arg2, Augmentations arg3) throws XNIException {
		mLocator = arg0;
		super.startDocument(arg0, arg1, arg2, arg3);
		lastSource();
	}

	/**
	 * @see org.apache.xerces.parsers.AbstractDOMParser#characters(org.apache.xerces.xni.XMLString,
	 *      org.apache.xerces.xni.Augmentations)
	 */
	@Override
	public void characters(XMLString arg0, Augmentations arg1)
			throws XNIException {
		super.characters(arg0, arg1);
		lastSource();
	}

	/**
	 * @see org.apache.xerces.parsers.AbstractDOMParser#comment(org.apache.xerces.xni.XMLString,
	 *      org.apache.xerces.xni.Augmentations)
	 */
	@Override
	public void comment(XMLString arg0, Augmentations arg1) throws XNIException {
		super.comment(arg0, arg1);
		lastSource();

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.parsers.AbstractDOMParser#textDecl(java.lang.String,
	 *      java.lang.String, org.apache.xerces.xni.Augmentations)
	 */
	@Override
	public void textDecl(String arg0, String arg1, Augmentations arg2)
			throws XNIException {
		super.textDecl(arg0, arg1, arg2);
		lastSource();
	}

	/**
	 * @see org.apache.xerces.parsers.AbstractDOMParser#startElement(org.apache.xerces.xni.QName,
	 *      org.apache.xerces.xni.XMLAttributes,
	 *      org.apache.xerces.xni.Augmentations)
	 */
	
	
	@Override
	public void startElement(org.apache.xerces.xni.QName arg0,
			XMLAttributes arg1, Augmentations arg2) throws XNIException {
		super.startElement(arg0, arg1, arg2);
		// p("startElement: {0} {1}", arg0,arg1);

		if (fCurrentNode != null) {
			// start of element
			fCurrentNode.setUserData("location.line", fLineNo, null);
			fCurrentNode.setUserData("location.column", fColumnNo, null);
			fCurrentNode.setUserData("location.charStart", fOffset + 1, null);
			fCurrentNode.setUserData("location.charEnd", fOffset
					+ arg0.rawname.length() + 1, null);
			// end of element
			fCurrentNode.setUserData("location2.line",
					mLocator.getLineNumber(), null);
			fCurrentNode.setUserData("location2.column", mLocator
					.getColumnNumber(), null);
			fCurrentNode.setUserData("location2.charStart", mLocator
					.getCharacterOffset(), null);
			fCurrentNode.setUserData("location2.charEnd", mLocator
					.getCharacterOffset(), null);
		}
		lastSource();
	}

	/**
	 * @see org.apache.xerces.parsers.AbstractDOMParser#startCDATA(org.apache.xerces.xni.Augmentations)
	 */
	@Override
	public void startCDATA(Augmentations aug) {
		super.startCDATA(aug);
		lastSource();

	}

	/**
	 * @see org.apache.xerces.parsers.AbstractDOMParser#endCDATA(org.apache.xerces.xni.Augmentations)
	 */
	@Override
	public void endCDATA(Augmentations aug) {
		super.endCDATA(aug);
		lastSource();
	}

	/**
	 * @see org.apache.xerces.parsers.AbstractDOMParser#endElement(org.apache.xerces.xni.QName, 
	 * org.apache.xerces.xni.Augmentations)
	 */
	@Override
	public void endElement(org.apache.xerces.xni.QName element,
			Augmentations aug) {
		super.endElement(element, aug);
		lastSource();
	}

}
