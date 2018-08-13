import java.util.HashSet;
import java.util.Vector;

/**
 * Represenation of the decision tree.
 * @author andis
 *
 */

public class Tree {	
	Node root;
	int numberInternalNodes;
	int numberLeaves;
	
	int globalMDLscore;
	/**
	 * Constructs a new decision tree with a new leaf at its root.  Sets the level (0) and
	 * the available attributes.
	 * @param numberAttributes Determines attibutes available at the root node 
	 * (named with the numbers from 1 to numberAttributes).
	 */
	public Tree (int numberAttributes)
	{
		HashSet<Integer> attributes = new HashSet<Integer>();
		for (int i = 1; i <= numberAttributes; i++)
		{
			attributes.add(i);
		}
	 Leaf root = new Leaf();
	 root.setAvailableAttributes(attributes);
	 this.root = root;
	 root.setLevel(0);
	}
	
	/**
	 * Constructs a new Tree with the specified node at its root.
	 * @param node The root of the new Tree.
	 */
	public Tree(Node node)
	{
	 root = node;	
	 root.setLevel(0);
	}
	

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
		root.level = 0;
	}
	
	
	public int countInternalNodes ()
	{
		TraverseTree traverseInt = new TraverseTree(this, TraverseTree.INTERNAL);
		return traverseInt.nodes.size();
	}
	
	public int countLeafNodes ()
	{
		TraverseTree traverse = new TraverseTree(this, TraverseTree.LEAVES);
		return traverse.nodes.size();
	}
	
	private int countAvailableAttributeScore ()
	{
		int numberAvailableAttributes = root.availableAttributes.size();
		

		if (root.getClass().isInstance(InternalNode.class))
		{
			HashSet<Edge> edges = ((InternalNode) root).getEdges();
			for (Edge edge: edges)
			{
				Node child = edge.getNode();
				numberAvailableAttributes += new Tree(child).countAvailableAttributeScore();
			}
			
		}
		
		return numberAvailableAttributes;
	}
	
	/**
	 * Calculates the decision tree's global MDL-score according to Quinlan and R. L. Rivest (1989).
	 * Uses crude two-part MDL, with L(H) being the coding length for the hypothesis and L(D|H) 
	 * the codinglength for the data given the hypothesis.
	 * @return L(H) + L(D|H)
	 */
	public double calculateGlobalMDLScore ()
	{		
	
		int numberInternalNodes = countInternalNodes();
		int numberNodes = numberInternalNodes + countLeafNodes();
		double b = (numberNodes + 1) /2;
		double 	 structure = calculateLScore (numberNodes, numberInternalNodes, b);
		//System.out.println("Structure Score is:: "+structure);
		 
		double attributes = calculateAttributeScore();
		//System.out.println("Attributes Score is:: "+attributes);
		
		double categories = calculateCategoryScore();
		//System.out.println("Categories Score is:: "+categories);
		
		double  hypothesisLength = structure + attributes + categories;
		//System.out.println("Structure Score is:: "+structure);
		
		//double dataLength =  calculateRootDataMDL();
		//double dataLength =  calculateDataMDL();
		//double dataLength  = calculateDataMDLBasedOnErrorEntropy();
		double dataLength = calculatDataMDLBasedOnEntropySum();
		//System.out.println("DataLength Score is:: "+dataLength);
		//System.out.println("dataLength="+dataLength);
		//System.out.println("hypothesisLenght"+ hypothesisLength);
		
		//double score= hypothesisLength + 10000*dataLength;
		double score= /*hypothesisLength + */dataLength;
		if (Double.isNaN(score))
		{
			System.out.println("dataLength = "+dataLength);
			System.out.println("hypothesis="+hypothesisLength);
			throw new RuntimeException("score="+score);
		}
		//return dataLength;
		return score;
		//return this.root.getNegativeCount();
	}
	
/**
 * The L-score from Quinlan and R. L. Rivest (1989)
 * @param n
 * @param k
 * @param b
 * @return L(n, k, b)
 */
	private double calculateLScore (int n, int k, double  b)
	{
		//System.out.println("n="+n+";k="+k+";"+Utilities.combination(n, k));
		return (Math.log(b+1) + Math.log(Utilities.combination(n, k))) / Math.log(2);		
	}
	
	private double calculateDataMDL()
	{
		TraverseTree traverseLeaves = new TraverseTree(this, TraverseTree.LEAVES);
		double score = 0;
		while (traverseLeaves.hasNext())
		{
			Leaf leaf = (Leaf) traverseLeaves.next();
			int k = leaf.getNegativeCount(); 
			int n = k + leaf.getPositiveCount(); 
			int b = n; // there is alternative measures, too :)
			//System.out.println("dataScore="+score);	
			score += calculateLScore (n, k, b);
			/*System.out.println("n="+n);
			System.out.println("k="+k);
			System.out.println("b="+b);
			System.out.println("dataScore="+score);*/
		}	
		return score;		
	}
	
	/**
	 * Calculates the entropy of the error, as it is distributed over the leaves of the tree.
	 * @return
	 */
	private double calculateDataMDLBasedOnErrorEntropy()
	{
		TraverseTree traverseLeaves = new TraverseTree(this, TraverseTree.LEAVES);
		Vector<Double> pis = new Vector<Double> ();
		while (traverseLeaves.hasNext())
		{
			Leaf leaf = (Leaf) traverseLeaves.next();
			double overallSampleNumber = leaf.getNegativeCount()+leaf.getPositiveCount();
			double errors =0;
			if (overallSampleNumber>0)
				errors = leaf.getNegativeCount() / (overallSampleNumber);
			pis.add(errors);
		}
		System.out.println("pis="+pis);
		return calculateEntropy(pis);
	}
	
	
	/**
	 * Calculates the sum of the entropies of the leaves.
	 * @return
	 */
	private double calculatDataMDLBasedOnEntropySum()
	{
		double sum = 0;
		
		TraverseTree traverseLeaves = new TraverseTree(this, TraverseTree.LEAVES);

		while (traverseLeaves.hasNext())
		{
			Vector<Double> pis = new Vector<Double> ();
			Leaf leaf = (Leaf) traverseLeaves.next();
			double overallNumer = leaf.getNegativeCount() + leaf.getPositiveCount();
			if (overallNumer==0)
				overallNumer = 1;
			pis.add(new Double(leaf.getNegativeCount()/overallNumer));
			pis.add(new Double(leaf.getPositiveCount()/overallNumer));
			sum = sum + calculateEntropy(pis);
		}
		return sum;
	}
	
	/**
	 * An alterantive way to calculate L(D|H). As a slight modification of Quinlan and R. L. Rivest(1989)
	 * this does not involve summing up the L-scores for the the tree's leaves, but rather calculates the 
	 * L-score at the root. 
	 * @return The L-score of the root node.
	 */
	private double calculateRootDataMDL()
	{
		
		int k = root.getNegativeCount(); 
		int n = k + root.getPositiveCount();
		int b = n; // there is alternative measures, too :)
		return calculateLScore (n, k, b);			
	}
	
	/**
	 * Calcualates the length for encoding the attributes (Quinlan and R. L. Rivest 1989)
	 * @return The tree's attributes' encoding length.
	 */
	private double calculateAttributeScore()
	{
		//System.out.println("entered calculateAttributeScore");
		double attributeScore = 0;
		TraverseTree internalNodesIterator = new TraverseTree(this, TraverseTree.INTERNAL);
		
		while (internalNodesIterator.hasNext())
		{
		//System.out.println("entered Loop");
		Node node = internalNodesIterator.next();

		int numberAvailableAttributes = node.getAvailableAttributes().size();
		//System.out.println("size= "+ numberAvailableAttributes);
		attributeScore += (Math.log(numberAvailableAttributes) / Math.log(2));
		}
		
		return attributeScore;
	}
	
	/**
	 * Calcualates the length for encoding the categories (Quinlan and R. L. Rivest 1989)
	 * @return The tree's categories' encoding length.
	 */
	private double calculateCategoryScore()
	{
		double categoryScore =0;
		TraverseTree traverse = new TraverseTree(this, TraverseTree.INTERNAL);
		// if the tree only consists of a single leaf (no internal nodes)
		if (!traverse.hasNext())
			return 1;
		
		while (traverse.hasNext())
		{
			Node node = traverse.next();
			if (node.getClass().equals(InternalNode.class) && ((InternalNode) node).hasAtLeastOneLeafAsDirectChild())
			{
				categoryScore += (Math.log(2) / Math.log(2));
			}
		}
		return categoryScore;
	}
	
	/**
	 * Encodes a new sample in the tree. 
	 * The sample is added at the leaf where it is classified 
	 * and the counts of positive or negative intstances, 
	 * respectively, is updated along the path from the leaf to the root.
	 * @param sample
	 */
	public Leaf encode (Sample sample)
	{
		this.testCounts();
		Leaf leaf = root.encode(sample);
		//System.out.println("after encoding but before update");
		//this.print();
		//this.testCounts();
		System.out.println("leaf ="+leaf);
		System.out.println("wronged tree");
		this.print();
		updateCounts(leaf, sample);
		System.out.println("leaf ="+leaf);
		System.out.println("corrected tree:");
		this.print();
		this.testCounts();
		//System.out.println("after uptdate");
		//this.print();
		return leaf;
	}
	
	/**
	 * Extends the decision tree at given leaf provided that this leads to an improvement of the 
	 * MDL-score. Chooses the attribute that leads to the greates improvement to "split" the leaf,
	 * i.e. turn it in an internal node.
	 * @param leaf The leaf where the tree might be extended.
	 * @return The extended tree.
	 */
	public Tree extend(Leaf leaf)
	{
		if (leaf.getNegativeCount() <= 0)
			return this;
		
		double currentScore = this.calculateGlobalMDLScore();
		Tree bestTree = this;
		//this.doAllTests();
		System.out.println("original dataScore="+this.calculateDataMDL());
		System.out.println("available attributes at leaf="+leaf.availableAttributes);
		Object[] attributes = leaf.availableAttributes.toArray();
		for (int counter = 0; counter<attributes.length; counter++)
		{
			System.out.println("entered loop");
			int attribute = (Integer) attributes[counter];
			Tree decisionTreeClone  = (Tree) this.clone();

			InternalNode node = leaf.split(attribute);	
			//decisionTreeClone.print();
			Vector<AttributeValuePair> path = this.getPath(leaf);
			//System.out.println("path="+path+ " leaf="+leaf);
			decisionTreeClone.setNode(node, path);
			
			double score = decisionTreeClone.calculateGlobalMDLScore();
			
			System.out.println("new node (proposed):");
			new Tree(node).print();
			System.out.println("score="+score+" for attribute"+attribute);
			if (score< currentScore)
			{
			
			currentScore = score;
			bestTree = decisionTreeClone;
			}				
		}
		
		return bestTree;
	}
	
	/**
	 * Prunes the tree along a specified path, if this leads to a reduction in score.
	 * After an internal node along the path has been de-expanded into a leaf
	 * this might result in several children of the same node having the same classification.
	 * In this case, that node will be pruned, too.
	 * @param path The path along which the pruning should take place.
	 * @return The pruned tree.
	 */
	public Tree prune(Vector<AttributeValuePair> path)
	{
//		System.out.println("start cloning");
		Tree treeClone = (Tree) this.clone();
//		System.out.println("find node");
		//System.out.println("treeClone:");
		//treeClone.print();
		//System.out.println("path="+path);
		Node node = treeClone.findNode(path);
		if (node instanceof Leaf)
			node = node.getParent();
	
//		System.out.println("prune internal " + node);
		Leaf leaf = ((InternalNode) node).prune();
		treeClone.setNode(leaf, path);
//		System.out.println("calculate score");
		
		//The wrong scores are being compared!! (before sibling pruning)
		if (treeClone.calculateGlobalMDLScore()< this.calculateGlobalMDLScore())
			{
			 return treeClone.pruneIfSiblingClassificationIsEqual(path);
			}
		else 
			return this;
	}
	
	
	/**
	 * Exchanges the attributes along the specified path starting at the root, if this leads to an 
	 * improvement of the score. In case of an improvement, the best attriubute is selected, and the 
	 * tree is re-expanded until this does no longer improve the score before the algorithm terminates.
	 * 
	 * @param path The path along which the restructuring takes place. 
	 * @return The tree after restructuring.
	 */
	public Tree restructure(Vector<AttributeValuePair> path)
	{		
	
		this.doAllTests();
		// go along the path (from root to leaf) searching for the first opportunity to restructure
	
		// wrong path from pruning is only followed till the last internal node :)
			
			Node node = this.root;
			int i = 0;
			boolean endOfPath = false;
			while (node instanceof InternalNode &&!endOfPath) 
			{	
				System.out.println("before exchange");
				System.out.println("path="+path);
				this.print();
				Vector<AttributeValuePair> subPath = this.getPath(node);
				System.out.println("start exchange attribute");
				Tree newTree = exchangeAttribute(subPath);	
				System.out.println("end exchange attribute");
				if (newTree.calculateGlobalMDLScore()<this.calculateGlobalMDLScore())
				{
					System.out.println("new tree through restructuring");	
					newTree.doAllTests();
					return newTree;
						
				}
				System.out.println("after exchange");	
				System.out.println("path="+path);
				this.print();
				AttributeValuePair pair;
				if (i<path.size())
				{
					pair = path.get(i);	
					node = ((InternalNode) node).getChild(pair);
					i++;
				}
				else
					endOfPath = true;
			}	
	
		return this;
	}
	

	/**
	 * Finds the best attribute for to be put at the end of the specified path.
	 * @param subPath Marks the place where the attribute exchange takes place
	 * @return The tree after the attribute exchange.
	 */
	private Tree exchangeAttribute(Vector<AttributeValuePair> subPath)
	{
		// if the node is a leaf, do nothing
		Node node = this.findNode(subPath);
		if (node instanceof Leaf) 
			return this;
		
		Node bestNode = findBestAttributeAt(subPath);
		if (bestNode instanceof InternalNode) 
		{
			//System.out.println("3");
			this.doAllTests();	
			Tree treeClone = (Tree) this.clone();
			treeClone.setNode(bestNode, subPath);			
			//treeClone.doAllTests();
			Tree newTree = treeClone.extendAsFarAsPossible(subPath);
			//newTree.doAllTests();
			//System.out.println();
			//System.out.println("new tree through restructuring:");
			newTree.print();
			return newTree;
		}			
		
		return this;
	}
	
	/**
	 * Performs an extension on the leaf at the specified path of the tree, as well as the 
	 * resulting children, as long as this leads to further improvements of the score.
	 * @param path
	 * @return The tree after extension.
	 */
	public Tree extendAsFarAsPossible(Vector<AttributeValuePair> path)
	{
		System.out.println("start extending to found");
		Tree clone = (Tree) this.clone();
		InternalNode node = (InternalNode) clone.findNode(path);
		double oldScore = clone.calculateGlobalMDLScore();
		double newScore;
			
			Object[] edges = node.getEdges().toArray();
			for (int i = 0; i < edges.length; i++)
			{
				Edge edge = (Edge) edges[i];
				Leaf child = (Leaf) node.getChild(edge.getAttributeValuePair());
				Vector<AttributeValuePair> pathToChild = clone.getPath(child);
				//Leaf child = (Leaf) edge.getNode();
				Tree extendedTree = clone.extend(child);
				
				// If the result of extend improved the clone, go deeper (recursively).
				newScore = extendedTree.calculateGlobalMDLScore();
				if (newScore < oldScore)
				{
					clone.doAllTests();
					System.out.println("unextended tree:");
					clone.print();
					clone = extendedTree;
					System.out.println("extended tree:");
					extendedTree.print();
					extendedTree.doAllTests();
					extendedTree = clone.extendAsFarAsPossible(pathToChild);
					if (extendedTree.calculateGlobalMDLScore()<newScore)
						clone = extendedTree;
					oldScore = clone.calculateGlobalMDLScore();
				}
			}
						
		
		System.out.println("end extending to fond");
		return clone;
	}
	
	
	 /**
	  * Selects the best attribute for the tree at the given node as specified by the path.
	  * @param path Specifies the node at which the best attribute should be found.
	  * @return The node at the specified location, which -- in case of a successful attribute search 
	  * -- has been turned into an internal node with the best attribute.
	  */
	private Node findBestAttributeAt (Vector<AttributeValuePair> path)
	{
		this.doAllTests();
		Node  node = findNode(path);
		Tree clone = (Tree) this.clone();

		Leaf leaf = clone.pruneSubtree(clone.getPath(node));
		
		Node bestNode = leaf;
		double bestScore = clone.calculateGlobalMDLScore();
		
		for (int attribute :leaf.getAvailableAttributes())
		{
			InternalNode newNode = leaf.split(attribute); // split creates same-classification leaves and unary extensions!

			clone.setNode(newNode, path);
			
			double score = clone.calculateGlobalMDLScore();
			System.out.println("node="+newNode+"; score="+score);
			if (score < bestScore)
			{
				bestNode = newNode;			
				bestScore = score;
			}
		}		
		//debug test:
		//Tree testTree = new Tree(bestNode);
		//testTree.doAllTests();
		
		return bestNode;
	}
	
	/**
	 * Removes the whole subtree up to the node indicated by the path, 
	 * moving up all samples from the subtrees leaves. 
	 * Does not calculate MDL-scores!
	 * @param path The path pointing to the root node of the subtree to be cloned.
	 * @return The leaf node in the tree that has been created through the pruning process.
	 */
	private Leaf pruneSubtree(Vector<AttributeValuePair> path)
	{
		System.out.println("Entered remove subtree proc");
		this.doAllTests();
		// if the node is a leaf, do nothing
		Node node = this.findNode(path);
		//System.out.println("the node to be pruned= "+node);
		if (node  instanceof Leaf)
		{
			Leaf leaf = (Leaf) node;
			return leaf;
		}
		
		// Else encode all instances to a new leaf
	
		InternalNode internalNode = (InternalNode) node;
		
		// collect all instances:
		Tree subtree = new Tree (internalNode);
		TraverseTree traversLeafsOfSubtree = new TraverseTree(subtree, TraverseTree.LEAVES);
		// copy all samples from all leaves of the subtree to be encoded in the newly created leaf (pruned node)
		Vector<Sample> samples = new Vector<Sample>();
		while (traversLeafsOfSubtree.hasNext())
		{
			Leaf nextLeaf = (Leaf) traversLeafsOfSubtree.next();
			samples.addAll(nextLeaf.getPositiveInstances());
			samples.addAll(nextLeaf.getNegativeInstances());
		}	
		
		// encode instances in a new leaf
		Leaf leaf = internalNode.createLeafFrom();
		leaf.encodeAll(samples);
		
		this.setNode(leaf, path);
		
		System.out.println("leave remove subtree");
		return leaf;
	
	}
	
	/**
	 * Updates the counts of the tree, after a new sample has been encoded.
	 * @param leaf The leaf where the new sample has been encoded.
	 * @param sample The new sample.
	 */
	private void updateCounts(Leaf leaf, Sample sample)
	{
		int classification = sample.getClassification();
		if (classification == leaf.classification)
		{
			augmentPositiveCounts(leaf, 1);
		}
		else 
			{
			
			this.augmentNegativeCounts(leaf, 1);
			System.out.println("before switch");
			this.print();
			System.out.println("leaf ="+leaf);
			switchPositiveAndNegative(leaf);
			System.out.println("leaf ="+leaf);
			}		
	
	}
	
	
	/**
	 * Switches  the classification of a leaf (from positive to negative or vice versa)
	 * if required after a new sample has been encoded. A switch is required, if the 
	 * amount of negative samples (negative count) is greater than the amount of positive 
	 * samples (positive count).
	 * @param leaf The leaf where the new sample has been encoded.
	 */
	
	private void switchPositiveAndNegative(Leaf leaf)
	{
		Leaf oldLeaf = (Leaf) leaf.clone();
		if (leaf.switchPositiveAndNegative())
		{			
			Vector<AttributeValuePair> path = this.getPath(leaf);
			this.updateCounts(path, oldLeaf);
			System.out.println("pos="+root.getPositiveCount()+"neg="+root.getNegativeCount());
		}
	}
	
	/**
	 * Updates the negative count of the tree along the path from the specified node
	 * to the root.
	 * @param node The node whose negative count has changed.
	 * @param x The amount by which the negative count has changed (positive or negative).
	 */
	private void augmentNegativeCounts(Node node, int x)
	{
		while (node.getParent()!=null)
		{
			InternalNode parent = (InternalNode) node.getParent();
			int newNegativeCount = parent.getNegativeCount() + x;
			parent.setNegativeCount(newNegativeCount);
			node = parent;
		}		
	}
	
	/**
	 * Updates the positive count of the tree along the path from the specified node
	 * to the root.
	 * @param node The node whose positive count has changed.
	 * @param x The amount by which the positive count has changed (positive or negative).
	 */
	
	private void augmentPositiveCounts(Node node, int x)
	{
		while (node.getParent()!=null)
		{
			InternalNode parent = (InternalNode) node.getParent();
			int newPositiveCount = parent.getPositiveCount() + x;
			parent.setPositiveCount(newPositiveCount);
			node = parent;
		}		
	}
	
	
	
	
	public void print()
	{
		print(0,this.root);
		System.out.println("positiveCounts ="+root.getPositiveCount()+"; "+ "negativeCounts="+root.getNegativeCount() );
		System.out.println("score = "+this.calculateGlobalMDLScore());
		System.out.println();
	}
	
	private void print(int depth, Node node)
	{
		System.out.println(minus(depth)+node);
		if (node instanceof InternalNode)
		{
			for (Edge edge: ((InternalNode) node).getEdges())
			{
				print(depth+1, edge.getNode());
			}
		}
	}
	
	private String minus(int depth)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<depth; i++)
		{
			buffer.append('-');
		}
		return buffer.toString();
	}
	
	public Object clone()
	{
		Tree tree = new Tree((Node) this.root.clone());
		return tree;
	}

	
	/**
	 * Finds the node specified by the path.
	 * @param path Specifies the node to be found.
	 * @return The node.
	 */	public Node findNode(Vector<AttributeValuePair> path)
	{
		Node currentNode = this.getRoot();
		for (AttributeValuePair address: path)
		{
			currentNode = ((InternalNode) currentNode).getChild(address);
		}
		return currentNode;
	}
	
	 /**
	  * Sets the node at a specified location in the tree.
	  * @param node Node to be set.
	  * @param path Specifies the location where the node should be set.
	  */
	public void setNode (Node node, Vector<AttributeValuePair> path)
	{
		//System.out.println("--- find path");
		boolean newNode = false;
		Node nodeInTree= findNode(path);
		
		if (nodeInTree.equals(this.getRoot()))
		{
			node.parent =null;
			this.setRoot(node);
			//this.print();
			this.testRelations();
		}
		else
		{
			AttributeValuePair pair = nodeInTree.getAboveAttributeValuePair();
			InternalNode parent = nodeInTree.getParent();
			HashSet<Edge> parentEdges = parent.getEdges();
			for (Edge edge: parentEdges)
			{
				//System.out.println("--- checking edge " + edge);
				if (edge.getAttributeValuePair().equals(pair))
				{
					edge.setNode(node);
					node.setParent(parent);
					break;
				}
			}
			//System.out.println(this.testRelations());
		}
		//System.out.println("--- update counts ");
	//	System.out.println(this.testRelations());
		this.updateCounts(this.getPath(node), nodeInTree);
	//	System.out.println(this.testRelations());
		//System.out.println("--- setNode done");
	}
	
	
	/**
	 * Constructs the path for a node of the tree.
	 * @param node The node for which the path should be constructed.
	 * @return The path.
	 */
	public Vector<AttributeValuePair> getPath(Node node)
	{
		//System.out.println("tree:");	
		//this.print();
		
		Vector<AttributeValuePair> path = new Vector<AttributeValuePair>();	
		while (node.getParent() != null)
		{
			//System.out.println("parent="+node.parent);
			//System.out.println("node="+node);
			AttributeValuePair pair = node.getAboveAttributeValuePair();
			path.add(pair);
			node =node.getParent();	
		}
		
		Vector<AttributeValuePair> reorderedpath = new Vector<AttributeValuePair>();
		for (int i =path.size()-1; i>=0; i--)	
		//for (int i =0; i<path.size(); i++)	
		{
			AttributeValuePair piece = path.get(i);
			reorderedpath.add(piece);
		}
		return reorderedpath;		 
		
		
	}
	
	

	
	private Tree pruneIfSiblingClassificationIsEqual(Vector<AttributeValuePair> path)
	{
//		 test if new leaf's siblling have same classification
		Tree treeClone = (Tree) this.clone();
		Leaf newLeaf = (Leaf) this.findNode(path);
		InternalNode parent= newLeaf.getParent();
		for (Edge edge: parent.getEdges())
		{
			Node toNode = edge.getNode();
			if (toNode instanceof Leaf) {
				Leaf leaf = (Leaf) toNode;
				if (leaf.getClassification()== newLeaf.getClassification() && !treeClone.getPath(leaf).equals(path))
				{
					Leaf extraNewLeaf = parent.prune();
					Vector<AttributeValuePair> parentPath = treeClone.getPath(parent);
					treeClone.setNode(extraNewLeaf, parentPath);
					return treeClone;
				}
				
			}
		}
		return this;
		
	}
	
	public void updateCounts(Vector<AttributeValuePair> path, Node oldNode)
	{
		Node newNode = this.findNode(path);
		int differencePositiveCounts = newNode.getPositiveCount() - oldNode.getPositiveCount();
		int differenceNegativeCounts = newNode.getNegativeCount() - oldNode.getNegativeCount();
		System.out.println("oldNode="+oldNode+" ;newNode="+newNode);
		System.out.println("differencePositiveCounts="+differencePositiveCounts+" ; differenceNegativeCounts="+differenceNegativeCounts);
		InternalNode parent = newNode.getParent();
		while (parent!=null)
		{
			parent.setPositiveCount(parent.getPositiveCount()+differencePositiveCounts);
			parent.setNegativeCount(parent.getNegativeCount()+differenceNegativeCounts);
			parent = parent.getParent();
		}
	}
	

	
	public void doAllTests()
	{
		//testForLeafsWithSameClassification();
//		testBinarity();
		testCounts();
		testRelations();
		//testForLeafsWithSameClassification();
	}
	public void  testRelations ()
	{
		boolean tester = false;
		TraverseTree travers = new TraverseTree(this, TraverseTree.ALL);
		
		while (travers.hasNext())
		{
			tester = false;
			Node node = travers.next();
			InternalNode parent = node.getParent();
			if (parent==null)
			{
				if (!root.equals(this.getRoot()))
					throw new RuntimeException("Parent is null, but node is not root!");
			}
			else
			{
				if (this.getRoot().getParent()!=null)
				{
					throw new RuntimeException("root has parents!");
				}
				for (Edge edge: parent.getEdges())
				{
					tester = false;		
					//System.out.println(edge.getNode()+" = = = "+node+" is "+(edge.getNode().equals(node)));
					if (edge.getNode().equals(node))
					{
						tester = true;
						break;
					}
				}	
				if (!tester)
				{
					//System.out.println("parent="+parent);
					throw new RuntimeException("Child refused by parent (none of parent's edges match)"); 
				}
			}
			if (node instanceof InternalNode) 
			{
				InternalNode internalNode = (InternalNode) node;
				tester = false;
				for (Edge edge: internalNode.getEdges())
				{
					Node child = edge.getNode();
					if (child.getParent().equals(internalNode))
						tester = true;
				}
				if (!tester)
					throw new RuntimeException("Parent refused by child (There are edges in parent's node where the child does not agree");
			}
		}
	}
	
	public void testCounts()
	{
		//System.out.println();
		//this.print();
		TraverseTree traverseInt = new TraverseTree(this, TraverseTree.INTERNAL);
		while(traverseInt.hasNext())
		{
			InternalNode node = (InternalNode) traverseInt.next();
			node.testCounts();
		}
	}
	public void testBinarity()
	{
		TraverseTree traverseTreeInt  = new TraverseTree(this, TraverseTree.INTERNAL);
		while (traverseTreeInt.hasNext())
		{
			InternalNode node  = (InternalNode) traverseTreeInt.next();
			if (node.getEdges().size()!=2)
				throw new RuntimeException("Tree is no longer binary!");
		}
	}
	
	public void testForLeafsWithSameClassification()
	{
		TraverseTree traverseLeafs = new TraverseTree(this, TraverseTree.LEAVES);
		while (traverseLeafs.hasNext())
		{
			Leaf leaf = (Leaf) traverseLeafs.next();
			Vector<AttributeValuePair> path = this.getPath(leaf);
			InternalNode parent = leaf.getParent();
			if (parent!=null)
			{
				for (Edge edge: parent.getEdges())
				{					
					if (edge.getNode() instanceof Leaf) 
					{
						Leaf newLeaf = (Leaf) edge.getNode();
						if (leaf.getClassification()== newLeaf.getClassification() && !this.getPath(newLeaf).equals(path))
						{
							//System.out.println("leaf="+leaf+" parent="+leaf.getParent()+ "leaf's path="+path+" ;newLeaf="+newLeaf+" parent="+newLeaf.getParent()+" newLeaf's path="+this.getPath(newLeaf));
							throw new RuntimeException("There are two child-leaves with the same classification in one node!");
						}			
					}
				}
			}
		}
	}
	
	public double calculateEntropy(Vector<Double> pis)
	{
		double e = 0;
		for (double pi: pis)
		{
			if (pi!=0)
				e = e - (pi * Math.log(pi)/Math.log(2));
		}
		return e;
	}
}
