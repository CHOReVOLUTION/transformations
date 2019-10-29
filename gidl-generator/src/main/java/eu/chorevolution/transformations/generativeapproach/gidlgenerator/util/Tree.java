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
package eu.chorevolution.transformations.generativeapproach.gidlgenerator.util;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
	private TreeNode<T> rootElement;
    
    /**
	 * Default constructor.
	 */
    public Tree() {
        super();
    }
 
    /**
     * Return the root {@link TreeNode} of the tree.
     * @return the root element.
     */
    public TreeNode<T> getRootElement() {
        return this.rootElement;
    }
 
    /**
     * Set the root Element for the tree.
     * @param rootElement the root element to set.
     */
    public void setRootElement(TreeNode<T> rootElement) {
        this.rootElement = rootElement;
    }
     
    /**
     * Returns the Tree<T> as a List of {@link TreeNode}<T> objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     * @return a List<TreeNode<T>>.
     */
    public List<TreeNode<T>> toList() {
        List<TreeNode<T>> list = new ArrayList<TreeNode<T>>();
        navigate(rootElement, list);
        return list;
    }
     
    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     * @return the String representation of the Tree.
     */
    public String toString() {
        return toList().toString();
    }
     
    /**
     * navigate the Tree in pre-order style. This is a recursive method, and is
     * called from the toList() method with the root element as the first
     * argument. It appends to the second argument, which is passed by reference as it recurses down the tree.
     * 
     * @param element the starting element.
     * @param list the output of the walk.
     */
    private void navigate(TreeNode<T> element, List<TreeNode<T>> list) {
        list.add(element);
        for (TreeNode<T> data : element.getChildren()) {
        	navigate(data, list);
        }
    }

    

}
