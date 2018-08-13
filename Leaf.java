import java.util.HashSet;
import java.util.Vector;


public class Leaf extends Node{
	

	public Leaf()
	{
		positiveInstances = new Vector<Sample>();
		negativeInstances = new Vector<Sample>();
		//classification = null;
	}

	Integer classification;
	
	Vector<Sample> positiveInstances; //multiset; we could also have Attribute-Value-pairs?
	Vector<Sample> negativeInstances; //multiset
	
	/**
	 * Splits a leaf into an internal node base on the specified attribute.
	 * @param attribute The splitting attribute.
	 * @return The node!! :-)
	 */
	public InternalNode split (int attribute)
	{
		InternalNode node = this.createInternalNodeFrom();	
		node.setAttribute(attribute);
		
		
		Vector<Sample> allInstances= (Vector<Sample>) positiveInstances.clone();
		allInstances.addAll(negativeInstances);
		
		// collect values for splitting attribute
		HashSet<String> values = new HashSet<String>();
		for (Sample sample: allInstances)
		{			
			values.add(sample.getValue(attribute));			
		}

		HashSet<Edge> edges = new HashSet<Edge>();
		//System.out.println("values"+values);
		for (String value: values)
		{
			Leaf leaf = new Leaf();
			//System.out.println("instances="+allInstances);
			Vector<Sample> sampleVector = new Vector<Sample>();
			for (Sample sample: allInstances)		
			{	
			//	System.out.println(sample.getValue(attribute)+"=="+(value));
				if (sample.getValue(attribute).equals(value))
				{
					sampleVector.add(sample);
				}
			//	System.out.println(sampleVector);
				//sampleVector is empty
			}
			leaf.encodeAll(sampleVector); 
			//System.out.println(leaf.getPositiveCount() +"--"+leaf.getNegativeCount()); // ok
			HashSet<Integer> availableAttributes =  (HashSet<Integer>) node.getAvailableAttributes().clone();
			availableAttributes.remove(attribute);
			leaf.setAvailableAttributes(availableAttributes);
			leaf.setLevel(level+1);
			leaf.setParent(node);
			Edge edge = new Edge();
			edge.setAttributeValuePair(new AttributeValuePair(attribute, value));
			edge.setNode(leaf);
			edges.add(edge);
		}
		
		node.setEdges(edges);	
		int negativeCount = 0;
		int positiveCount = 0; 
		for (Edge edge: edges)
		{
			negativeCount += edge.getNode().getNegativeCount();
			positiveCount += edge.getNode().getPositiveCount();
		}
		//System.out.println(positiveCount + "-"+ negativeCount); //ok
		node.setNegativeCount(negativeCount);
		node.setPositiveCount(positiveCount);
		
		return node;
	}
	
	public InternalNode createInternalNodeFrom()
	{
		InternalNode node = new InternalNode();
		node.setAvailableAttributes(availableAttributes);
		node.setLevel(level);
		if (this.parent !=null)
			node.setParent((InternalNode) this.parent.clone());
		return node;		
	}
	public Leaf encode(Sample sample)
	{
		//System.out.println(((Leaf) root).getClassification());
		if (getClassification()==null)
		{
			addFirstInstance(sample); 
		}
		else
		if (sample.getClassification() == getClassification()) //maybe this must be changed to .equals?
		{
			addPositiveInstance(sample); 
		}
		else 
		{
			addNegativeInstance(sample); 
		}		
		return this;
	}
	
	public boolean switchPositiveAndNegative ()
	{
		System.out.println("swich method of class Leaf is called");
		if (negativeInstances.size()>positiveInstances.size())
		{
			Vector<Sample> store = negativeInstances;
			negativeInstances = positiveInstances;
			positiveInstances = store;
			System.out.println("classification before="+this.getClassification());
			if (this.classification==0)					
				this.classification=1;
			else
			{
				if (this.classification==1)
					this.setClassification(0);
			}
			System.out.println("classification after="+this.getClassification());
			return true;
		}
		return false;
	}
	private void addFirstInstance(Sample sample)
	{
		classification = sample.getClassification();
		addPositiveInstance(sample);
	}

	private void addPositiveInstance(Sample sample)
	{
		positiveInstances.add(sample);
	}
	
	private void addNegativeInstance(Sample sample)
	{
		negativeInstances.add(sample);
	}


	public int getNegativeCount() {
		return negativeInstances.size();
	}


	public int getPositiveCount() {
		return positiveInstances.size();
	}


	public Integer getClassification() {
		return classification;
	}


	public void setClassification(int classification) {
		this.classification = classification;
	}


	public Vector<Sample> getNegativeInstances() {
		return negativeInstances;
	}


	public void setNegativeInstances(Vector<Sample> negativeInstances) {
		this.negativeInstances = negativeInstances;
	}


	public Vector<Sample> getPositiveInstances() {
		return positiveInstances;
	}


	public void setPositiveInstances(Vector<Sample> positiveInstances) {
		this.positiveInstances = positiveInstances;
	}

	public String toString ()
	{
		
		String value="";
		String parentString = "";
		if (parent!=null)
		{
			parentString = " parent="+parent.getAttribute();
			try
			{
			value = getAboveAttributeValuePair().getValue();
			}
			catch (RuntimeException e)
			{
				value=" (removed from tree) ";
			}
		}
		if ((value == null) || (value.length() <= 0))
			value = "";
		return "leaf: c="+classification+" v="+value+" pos="+this.getPositiveCount()+" neg="+this.getNegativeCount()+this.getNegativeInstances()
		+ parentString; //"negis="+negativeInstances + "posis="+positiveInstances;
	}
	
	public Object clone()
	{
		Leaf leaf= new Leaf();
		leaf.availableAttributes = (HashSet<Integer>) this.availableAttributes.clone();
		leaf.classification = this.classification;
		leaf.level = this.level;
		leaf.negativeInstances = (Vector<Sample>) this.negativeInstances.clone();
		leaf.positiveInstances = (Vector<Sample>) this.positiveInstances.clone();
//		if (this.parent!=null)
//		{
//			leaf.parent = (InternalNode) this.parent.clone();
//		}
		
		return leaf;
	}
	
	public void encodeAll (Vector<Sample> sampleVector)
	{
		//System.out.println(sampleVector);
		sampleVector.addAll(this.negativeInstances);
		sampleVector.addAll(this.positiveInstances);
		//System.out.println(sampleVector);
		int majorityClassification = findMajorityClassification(sampleVector); // sampleVector is empty
		this.setClassification(majorityClassification);
		
		for (Sample sample: sampleVector)
		{
			encode(sample);
		}
	}
	
	
	private int findMajorityClassification(Vector<Sample> sampleVector)
	{
		//System.out.println(sampleVector);
		HashSet<Integer> classifications = new HashSet<Integer>();
		// collect classifications
		for (Sample sample: sampleVector)
		{
			classifications.add(sample.getClassification());
		}
			//System.out.println	(classifications);
		int bestClassification = Integer.MAX_VALUE;
		int maxCounter =0;
		for (int classification: classifications)
		{
			int sampleCounter = 0;
			for (Sample sample: sampleVector)
			{
				if (sample.classification == classification)
					sampleCounter++;
			}
			if (sampleCounter > maxCounter)
			{
				bestClassification = classification;
				maxCounter=sampleCounter;
			}
		}
		
		/*if (maxCounter == sampleVector.size()/ 2)
		{
			System.out.println("smartness");
			// do something smart
			Vector<Node> siblings = this.getSiblings();
			for (int classification :classifications)
			{
				for (Node sibling : siblings)
				{
					if (sibling instanceof Leaf) {
						Leaf siblingLeaf = (Leaf) sibling;
						if (siblingLeaf.getClassification() == classification)
							classifications.remove(classification);
					}
				}
			}
			Object[] bestClassificationArray = classifications.toArray();
			bestClassification = (Integer) bestClassificationArray[0];
		}*/
		
		if (bestClassification==Integer.MAX_VALUE)
			throw new RuntimeException("No best classification found!");
		return bestClassification;
	}
	

}

