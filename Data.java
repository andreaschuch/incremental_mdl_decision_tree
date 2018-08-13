import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;


public class Data {
	
	//HashMap<Attribute, HashSet<Double>> dataStructure;
	int numberAttributes; 
	Vector<Sample> samples = new Vector<Sample>();
	
	private Iterator<Sample> dataIterator;
	
	public Data (String filename) throws IOException
	{
		readData(filename);
		dataIterator = samples.iterator();
	}
	
	void readData(String filename) throws IOException
	{
		// determine the number of Attributes
		BufferedReader reader = new BufferedReader(new FileReader(filename)); 
		String line = reader.readLine();
		//System.out.println("first line="+line);
		StringTokenizer tokenizer = new StringTokenizer(line, ",");
		//System.out.println("tokenizer="+tokenizer+" ::"+tokenizer.countTokens());
		numberAttributes = tokenizer.countTokens()-1;
		//line = reader.readLine();
		
		// read in Samples
		while (line != null)
		{
			Sample sample = new Sample();
			StringTokenizer tokenizer1 = new StringTokenizer(line, ",");
			int attributeName = 1;
			int classification = Integer.parseInt(tokenizer1.nextToken());
			sample.setClassification(classification);
			HashSet<AttributeValuePair> attributeValuePairs = new HashSet<AttributeValuePair>();
			while(tokenizer1.hasMoreElements())
			{
				String value = tokenizer1.nextToken().trim();
				attributeValuePairs.add(new AttributeValuePair(attributeName, value));
				attributeName++;
			}
			sample.setAttributeValuePairs(attributeValuePairs);
			samples.add(sample);
			line = reader.readLine();
		}		
	}
	
	public void print()
	{
		int counter =1; 
		for (Sample sample: samples)
		{
			System.out.println("Sample "+counter+ sample.toString());
							
			counter++;
					
		}
	}
	
	public Sample getNextSample()
	{
		return dataIterator.next();
	}
	
	public boolean hasNext()
	{
		return dataIterator.hasNext();
	}

}
