import java.util.HashSet;


public class Sample {
	
	int classification;
	
	HashSet<AttributeValuePair> attributeValuePairs;

	public HashSet<AttributeValuePair> getAttributeValuePairs() {
		return attributeValuePairs;
	}

	public void setAttributeValuePairs(
			HashSet<AttributeValuePair> attributeValuePairs) {
		this.attributeValuePairs = attributeValuePairs;
	}

	public int getClassification() {
		return classification;
	}

	public void setClassification(int classification) {
		this.classification = classification;
	}
	

	public String toString()
	{
		String string = " classification = "+classification + ";" ;
		
		for (AttributeValuePair attributeValuePair: attributeValuePairs)
		{
			string +=  attributeValuePair;
		}
		return string;
	}

	
	public String getValue (int attribute)
	{
		for (AttributeValuePair pair: attributeValuePairs)
		{
			if (pair.getAttribute() == attribute)
			{
				return pair.getValue();
			}
		}
		throw new RuntimeException("Attribute not existent.");
	}
}
