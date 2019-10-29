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

public class TreeNode<T> {
	public T data;
	public List<TreeNode<T>> children;

	/**
	 * Default constructor.
	 */
	public TreeNode() {
		super();
	}

	/**
	 * Convenience constructor to create a TreeNode<T> with an instance of T.
	 * 
	 * @param data
	 *            an instance of T.
	 */
	public TreeNode(T data) {
		this();
		setData(data);
	}

	/**
	 * Return the children of TreeNode<T>. The {@link Tree}<T> is represented by a single root TreeNode<T> whose 
	 * children are represented by a List<TreeNode<T>>. Each of these TreeNode<T> elements in the List can have children.
	 * 
	 * The getChildren() method will return the children of a TreeNode<T>.
	 * 
	 * @return the children of Node<T>
	 */
	public List<TreeNode<T>> getChildren() {
		if (this.children == null) {
			return new ArrayList<TreeNode<T>>();
		}
		return this.children;
	}

	/**
	 * Sets the children of a TreeNode<T> object.
	 * 
	 * @param children
	 *            the List<Node<T>> to set.
	 */
	public void setChildren(List<TreeNode<T>> children) {
		this.children = children;
	}

	/**
	 * Returns the number of immediate children of this TreeNode<T>.
	 * 
	 * @return the number of immediate children.
	 */
	public int getNumberOfChildren() {
		if (children == null) {
			return 0;
		}
		return children.size();
	}

	/**
	 * Adds a child to the list of children for this TreeNode<T>. The addition of the first child will create a new List<TreeNode<T>>.
	 * 
	 * @param child
	 *            a TreeNode<T> object to set.
	 */
	public void addChild(TreeNode<T> child) {
		if (children == null) {
			children = new ArrayList<TreeNode<T>>();
		}
		children.add(child);
	}

	/**
	 * Inserts a TreeNode<T> at the specified position in the child list. 
	 * 
	 * Will throw an ArrayIndexOutOfBoundsException if the index does not exist.
	 * 
	 * @param index
	 *            the position to insert at.
	 * @param child
	 *            the TreeNode<T> object to insert.
	 * @throws IndexOutOfBoundsException
	 *             if thrown.
	 */
	public void insertChildAt(int index, TreeNode<T> child) throws IndexOutOfBoundsException {
		if (index == getNumberOfChildren()) {
			// this is really an append
			addChild(child);
			return;
		} else {
			children.get(index); // just to throw the exception, and stop here
			children.add(index, child);
		}
	}

	/**
	 * Remove the TreeNode<T> element at index index of the List<TreeNode<T>>.
	 * 
	 * @param index
	 *            the index of the element to delete.
	 * @throws IndexOutOfBoundsException
	 *             if thrown.
	 */
	public void removeChildAt(int index) throws IndexOutOfBoundsException {
		children.remove(index);
	}

	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{").append(getData().toString()).append(",[");
		int i = 0;
		for (TreeNode<T> e : getChildren()) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(e.getData().toString());
			i++;
		}
		sb.append("]").append("}");
		return sb.toString();
	}

}
