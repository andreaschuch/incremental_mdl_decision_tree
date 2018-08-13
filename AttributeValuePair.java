
public class AttributeValuePair {
	
	int attribute;
	String value;

	public boolean equals(Object pair)
	{
		return attribute == ((AttributeValuePair) pair).getAttribute() && value.equals(((AttributeValuePair) pair).getValue());
	}
	
	public AttributeValuePair (int attribute, String value)
	{
		this.attribute = attribute;
		this.value = value;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString() {
		return 
		"Attribute '"+attribute+"'"+
		"Value '"+value+"'";
	}
	
	public Object clone()
	{
		AttributeValuePair pair = new AttributeValuePair(this.attribute, new String(this.value));
		return pair;
	}
}
