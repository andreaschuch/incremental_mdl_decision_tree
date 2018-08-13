
public class Edge {
	
	private AttributeValuePair attributeValuePair;
	private Node toNode;
	
	
	public AttributeValuePair getAttributeValuePair() {
		return attributeValuePair;
	}
	public void setAttributeValuePair(AttributeValuePair attributeValuePair) {
		this.attributeValuePair = attributeValuePair;
	}
	public Node getNode() {
		return toNode;
	}
	public void setNode(Node toNode) {
		this.toNode = toNode;
	}

	public String toString()
	{
		return attributeValuePair + "-->" + toNode;
	}
	public Object clone()
	{
		Edge edge = new Edge();
		edge.toNode = (Node) this.toNode.clone();
		edge.attributeValuePair = (AttributeValuePair) this.attributeValuePair.clone();
		return edge;
	}
}
