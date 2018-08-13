import java.util.HashSet;
import java.util.Vector;


public abstract class Node {

	
	InternalNode parent;
	
	HashSet<Integer> availableAttributes;
	
	int level;
	
	abstract public int getNegativeCount();

	abstract public int getPositiveCount();
	
	abstract public Leaf encode(Sample sample); //returns the leaf where the sample has been encoded (handy for uptdating)

	public InternalNode getParent() {
		return parent;
	}

	public void setParent(InternalNode parent) {
		this.parent = parent;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public HashSet<Integer> getAvailableAttributes() {
		return availableAttributes;
	}

	public void setAvailableAttributes(HashSet<Integer> availableAttributes) {
		this.availableAttributes = availableAttributes;
	}
	
	abstract public Object clone();
	
	/**
	 * Finds the attribute value pair of the node's parent.
	 * @return The parent's attribute and the value that leads to the node itself.
	 */
	public AttributeValuePair getAboveAttributeValuePair()
	{
		//System.out.println();
		//System.out.println("this node="+this);
		if (parent!=null)
		{
			for (Edge edge: parent.getEdges())
			{
				//System.out.println(edge);
				Node toNode = edge.getNode();
				//System.out.println(toNode+" = = = "+this+" is "+toNode.equals(this));
				if (toNode.equals(this))
				{
					return edge.getAttributeValuePair(); 
				}
					
			}
		}
		throw new RuntimeException("Must be root node...");
	}
	
	public Vector<Node> getSiblings ()
	{
	
		Vector<Node> siblings = new Vector<Node>();
		if (this.parent == null)
			return siblings;
		
		AttributeValuePair myPair = this.getAboveAttributeValuePair();
		
		for (Edge edge: this.getParent().getEdges())
		{
			if (!edge.getAttributeValuePair().equals(myPair))
				siblings.add(edge.getNode());
		}
		return siblings;
	}
	
}
