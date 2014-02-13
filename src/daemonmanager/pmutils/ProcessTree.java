/*
 The MIT License

 Copyright (c) 2013 - 2013
   1. High Performance Computing Group, 
   School of Electrical Engineering and Computer Science (SEECS), 
   National University of Sciences and Technology (NUST)
   2. Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter (2013 - 2013)
   

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/*
 * File         : ProcessTree.java 
 * Author       : Khurram Shahzad, Mohsan Jameel, Aamir Shafi, Bryan Carpenter
 * Created      : January 30, 2013 6:00:57 PM 2013
 * Revision     : $
 * Updated      : $
 */

package daemonmanager.pmutils;

import java.util.ArrayList;

import daemonmanager.pmutils.PNode;
import daemonmanager.pmutils.ProcessTree;

public class ProcessTree<T> {
	
	private PNode<T> root;

	public ProcessTree(PNode<T> root) {
		this.root = root;
	}

	public boolean isEmpty() {
		return (root == null) ? true : false;
	}

	public PNode<T> getRoot() {
		return root;
	}

	public void setRoot(PNode<T> root) {
		this.root = root;
	}

	public boolean exists(T key) {
		return find(root, key);
	}

	public int getNumberOfPNodes() {
		return getNumberOfDescendants(root) + 1;
	}

	public int getNumberOfDescendants(PNode<T> PNode) {
		int n = PNode.getChildren().size();
		for (PNode<T> child : PNode.getChildren())
			n += child.getChildren().size();

		return n;

	}

	private boolean find(PNode<T> PNode, T keyPNode) {
		boolean res = false;
		if (PNode.getData().equals(keyPNode))
			return true;

		else {
			for (PNode<T> child : PNode.getChildren())
				if (find(child, keyPNode))
					res = true;
		}

		return res;
	}

	public ArrayList<PNode<T>> getPreOrderTraversal() {
		ArrayList<PNode<T>> preOrder = new ArrayList<PNode<T>>();
		buildPreOrder(root, preOrder);
		return preOrder;
	}

	public ArrayList<PNode<T>> getPostOrderTraversal() {
		ArrayList<PNode<T>> postOrder = new ArrayList<PNode<T>>();
		buildPostOrder(root, postOrder);
		return postOrder;
	}

	private void buildPreOrder(PNode<T> PNode, ArrayList<PNode<T>> preOrder) {
		preOrder.add(PNode);
		for (PNode<T> child : PNode.getChildren()) {
			buildPreOrder(child, preOrder);
		}
	}

	private void buildPostOrder(PNode<T> PNode, ArrayList<PNode<T>> preOrder) {
		for (PNode<T> child : PNode.getChildren()) {
			buildPreOrder(child, preOrder);
		}
		preOrder.add(PNode);
	}

	public ArrayList<PNode<T>> getLongestPathFromRootToAnyLeaf() {
		ArrayList<PNode<T>> longestPath = null;
		int max = 0;
		for (ArrayList<PNode<T>> path : getPathsFromRootToAnyLeaf()) {
			if (path.size() > max) {
				max = path.size();
				longestPath = path;
			}
		}
		return longestPath;
	}

	public ArrayList<ArrayList<PNode<T>>> getPathsFromRootToAnyLeaf() {
		ArrayList<ArrayList<PNode<T>>> paths = new ArrayList<ArrayList<PNode<T>>>();
		ArrayList<PNode<T>> currentPath = new ArrayList<PNode<T>>();
		getPath(root, currentPath, paths);

		return paths;
	}

	private void getPath(PNode<T> PNode, ArrayList<PNode<T>> currentPath,
			ArrayList<ArrayList<PNode<T>>> paths) {
		if (currentPath == null)
			return;

		currentPath.add(PNode);

		if (PNode.getChildren().size() == 0) {
			// This is a leaf
			paths.add(clone(currentPath));
		}
		for (PNode<T> child : PNode.getChildren())
			getPath(child, currentPath, paths);

		int index = currentPath.indexOf(PNode);
		for (int i = index; i < currentPath.size(); i++)
			currentPath.remove(index);
	}

	private ArrayList<PNode<T>> clone(ArrayList<PNode<T>> list) {
		ArrayList<PNode<T>> newList = new ArrayList<PNode<T>>();
		for (PNode<T> PNode : list)
			newList.add(new PNode<T>(PNode));

		return newList;
	}
	
	public static void main(String[] args) {
		
		
		int nArr = Integer.parseInt(args[0]);	
		ArrayList<Integer> valArray  = new ArrayList<Integer>();
	
		
		PNode<Integer> rootNode = new PNode<Integer>(Integer.parseInt(args[1]));
		
		for( int i =2; i< args.length;i++)
		{		
			valArray.add(Integer.parseInt(args[i]));
		}
		
		ProcessTree<Integer> tree = new ProcessTree<Integer>(rootNode);
		ProcessTree.BuildTree(nArr,rootNode,valArray,0);
		ArrayList<PNode<Integer>> preOrder = tree.getPreOrderTraversal();

		for(PNode<Integer> node : preOrder)
		{
			if(node.getParent() != null)
				System.out.println("Node: "+  node.getData() +"  -- " + node.getParent().getData());
			else
				System.out.println("Node: "+  node.getData() +"  --  root" );
		}
	}
	
	
	public static void BuildTree(int nArr,PNode<Integer> parentNode,ArrayList<Integer> valArray,int index)
	{
		for(int i=index; i<index + nArr ;i++)
		{
			if(i >= valArray.size() )
				return;
			PNode<Integer> childNode = new PNode<Integer>(valArray.get(i));
			parentNode.addChild(childNode);			
			BuildTree(nArr, childNode, valArray,(i+1)*nArr);	
		}
	}
	
	


}

