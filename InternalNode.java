import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Vector;


public class InternalNode extends Node{

	private int attribute;
	//	int score;
	
	private HashSet <Edge> edges;
	
	private int positiveCount;
	private int negativeCount;
	
	public Leaf encode(Sample sample)
	{
		int attribute = getAttribute();
		
		String sampleValue = sample.getValue(attribute);
		//System.out.println("attribute="+attribute+"at node "+this);
		
		//System.out.println(sample);
		try
		{
			Node nextNode = getChild(new AttributeValuePair(attribute, sampleValue));
			return nextNode.encode(sample);
		}
		catch (RuntimeException e)
		{
			return this.growNewBranch(sample);			
		}
		
	}
	
	/**
	 * Grows a new branch in case the new sample cannot be encoded as its 
	 * value is missing from the tree.
	 * @param sample The new sample.
	 * @return The newly created leaf (where the new branch is ending).
	 */
	private Leaf growNewBranch(Sample sample)
	{
		String sampleValue = sample.getValue(attribute);
		Edge newEdge = new Edge();
		newEdge.setAttributeValuePair(new AttributeValuePair(attribute, sampleValue));
		Leaf newLeaf = new Leaf();
		newLeaf.encode(sample);
		newLeaf.setParent(this);
		newLeaf.setLevel(this.level+1);
		HashSet<Integer> leafAvailableAttribues = (HashSet<Integer>) availableAttributes.clone();
		leafAvailableAttribues.remove(attribute);
		newLeaf.setAvailableAttributes(leafAvailableAttribues);
		newEdge.setNode(newLeaf);
		edges.add(newEdge);
		return newLeaf;
	}
	
	/**
	 * @return String representation containing more information than that of "toString()".
	 */
	public String toLargeString()
	{
		String value = "";
		if (parent!=null)
			value = "v="+getAboveAttributeValuePair().value;
		
		StringBuffer buffer = new StringBuffer();
		for (Edge edge: this.edges)
		{
			buffer.append(edge.toString());
		}
		return value+" "+ attribute+buffer.toString();
	}
	
	/**
	 * Selects the child from the node's children which is obtained by following the edge
	 * with the specified attribute-value pair.
	 * @param pair Specifies the node's edge (especially by specifiying the value).
	 * @return The node's child at the specified edge.
	 */
	public Node getChild(AttributeValuePair pair)
	{
		if (this.edges.size()==0)
			throw new RuntimeException("edges are empty!");
		for (Edge edge: edges)
		{			
			//System.out.println(edge.getAttributeValuePair()+" == "+pair+"is "+edge.getAttributeValuePair().equals(pair));
			if ((edge.getAttributeValuePair()).equals(pair))
				return edge.getNode();
		}
		throw new RuntimeException("No edge for this attribute value pair ("+pair+")starts at this node.");
	}
	
	/**
	 * Creates a new leaf from this internal node.
	 * @return The leaf.
	 */
	public Leaf createLeafFrom()
	{
		Leaf leaf = new Leaf();
		leaf.availableAttributes = (HashSet<Integer>) this.availableAttributes.clone();
		leaf.level = this.level;
		if (parent != null)
			leaf.parent = (InternalNode) this.parent.clone();
		return leaf;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public HashSet<Edge> getEdges() {
		return edges;
	}

	public void setEdges(HashSet<Edge> edges) {
		this.edges = edges;
	}

	public int getNegativeCount() {
		return negativeCount;
	}

	public void setNegativeCount(int negativeCount) {
		this.negativeCount = negativeCount;
	}

	public int getPositiveCount() {
		return positiveCount;
	}

	public void setPositiveCount(int positiveCount) {
		this.positiveCount = positiveCount;
	}

	public void printWithAllChildren()
	{
		System.out.println(this);
		for (Edge edge: edges)
		{
			System.out.println(edge);
		}
	}
	/**
	 * Tests if the at least one of the node's children is a leaf.
	 * @return True, if there is at least one leaf among the node's children.
	 */
	public boolean hasAtLeastOneLeafAsDirectChild()
	{
		for (Edge edge: edges)
		{
			if (edge.getNode().getClass().equals(Leaf.class))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public Object clone()
	{
		InternalNode node = new InternalNode();
		node.attribute = this.attribute;
		node.availableAttributes = new HashSet<Integer>();
		for (Integer availableAttribute: this.availableAttributes)
		{
			node.availableAttributes.add(new Integer(availableAttribute.intValue()));
		}
		node.edges = new HashSet<Edge>();
		for (Edge edge: this.edges)
		{
			Edge clonedEdge = (Edge) edge.clone();
			clonedEdge.getNode().setParent(node);
			node.edges.add(clonedEdge);
		}
		node.level = this.level;
		node.negativeCount = this.negativeCount;
//		if (node.parent !=null)
//			node.parent = (InternalNode) this.parent.clone();
		node.positiveCount = this.positiveCount;
		return node;
	}

	
	public Leaf prune() //only applicable for internal nodes whose direct children are leaves
	{
//		System.out.println("- create leaf");
		Leaf leaf = this.createLeafFrom();
		Vector<Sample> allSamples = new Vector<Sample>();
		for (Edge edge: this.getEdges())
		{
//			System.out.println("- get instances for edge " + edge);
			allSamples.addAll(((Leaf)edge.getNode()).getNegativeInstances());
			allSamples.addAll(((Leaf)edge.getNode()).getPositiveInstances());
		}
		
//		System.out.println("- encode all samples");
		leaf.encodeAll(allSamples);
//		System.out.println("- return leaf " + leaf);
		return leaf;
	}
	
	public String toString()
	{
		String parentStr = "";
		if (parent!=null)
		{
			try
			{
				parentStr = "parent="+getAboveAttributeValuePair().getAttribute();
			}
			catch (RuntimeException e)
			{
				parentStr = " (removed from tree)";
			}
		}
		return attribute+parentStr;
	}
	
	public void testCounts()
	{
		int negativeCount = 0;
		int positiveCount = 0;
		for (Edge edge: this.getEdges())
		{
			Node child  = edge.getNode();
			negativeCount += child.getNegativeCount();
			positiveCount += child.getPositiveCount();			
		}
		System.out.println();
		if (negativeCount!= this.negativeCount)
			
			throw new RuntimeException("negative count is wrong: "+  "should be "+negativeCount+ " but is "+this.negativeCount);
		if (positiveCount != this.getPositiveCount())
			throw new RuntimeException("postive count is wrong: " +"should be "+positiveCount+" but is " +this.positiveCount);
	}
	
	public boolean hasOnlyLeafsAsChildren()
	{
		for (Edge edge: this.getEdges())
		{
			if (edge.getNode() instanceof InternalNode)
				return false;			
		}
	return true;	
	}
}
