/*
 * Copyright 2012 Alliance Global Services, Inc. All rights reserved.
 * 
 * Licensed under the General Public License, Version 3.0 (the "License") you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Class: LinkStack
 * 
 * Purpose: This class contains utility methods for push and pop methods for
 * stacking
 */

package com.ags.aft.util;
/**
 * The class LinkStack
 */
public class LinkStack {
	/** top variable */
	private Node top;

	/**
	 * Creates a new instance of LinkStack
	 */
	public LinkStack() {
		top = null;
	}
	/**
	 * returns top value either true/false
	 * @return either true/false
	 */
	public boolean empty() {
		return top == null;
	}
	/**
	 * returns either true/false
	 * @return either true/false
	 */
	public boolean full() {
		return false;
	}
	/**
	 * push method implementation
	 * @param e
	 *        object
	 */
	public void push(Object e) {
		Node tmp = new Node(e);
		tmp.next = top;
		top = tmp;
	}
	/**
	 * pop method implementation
	 * @return object
	 */
	public Object pop() {
		Object obj = top.data;
		top = top.next;
		return obj;
	}
	/**
	 * peek method implementation
	 * @return object
	 */
	public Object peek() {
		return top.data;
	}
	/**
	 * postfix method implementation
	 * @param x
	 *         x
	 * @return string
	 */
	public String postfix(String x) {
		String resultExpression = "";
		LinkStack stack = new LinkStack();
		for (int i = 0; i < x.length(); i++) {
			char c = x.charAt(i);
			if (c == '(') {
				stack.push(c);
			} else if (c == ')') {
				while (!stack.peek().equals('(')) {
					resultExpression += stack.pop();
					stack.pop();
				}
			} else {
				resultExpression += c;
			}
		}
		// written if condition for trimming of the "(" being appended to the
		// right operand (21-Feb-2013).
		if (!stack.peek().equals('(')) {
			while (!stack.empty()) {
				resultExpression += stack.pop();
			}
		}
		return resultExpression;
	}
	/**
	 * inner class node implementation
	 */
	class Node {
		/**
		 * data variable declaration
		 */
		private Object data;
		/**
		 * next variable declaration
		 */
		private Node next;
		
		/**
		 * Creates a new instance of node
		 */
		public Node() {
			data = ' ';
			next = null;
		}
		/**
		 * Creates a new instance of node
		 * @param val
		 *          val
		 */
		public Node(Object val) {
			data = val;
			next = null;
		}
	}
}
