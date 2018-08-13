import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Vector;

public class MainAlgorithm {

Data trainingData;
Data testData;
Tree decisionTree;

	public MainAlgorithm(String trainingDataFile, String testDataFile) throws IOException
	{
		trainingData = new Data(trainingDataFile);
		testData = new Data(testDataFile);
	}
	
	public Tree learning()
	{
		System.out.println("start of main algorithm");
		Tree tree = new Tree(trainingData.numberAttributes);
		//tree.print();
		tree.doAllTests();
		int counter = 0;
		while (trainingData.hasNext())
		{
			counter++;
			
			Sample sample = trainingData.getNextSample();
			System.out.println(counter+". sample"+sample);
			System.out.println("wrong tree:");
			tree.print();
			tree.doAllTests();
			Leaf leaf = tree.encode(sample);
			tree.doAllTests();
			System.out.println("sample encoded:");
			tree.print();
			
			Vector<AttributeValuePair> path = tree.getPath(leaf);
			System.out.println("path for resturcturing "+path);
			
			double MDLbeforeRestructuring = tree.calculateGlobalMDLScore();
			System.out.println("MDLScore befor restr"+MDLbeforeRestructuring);
			System.out.println("start restructuring");	
			System.out.println("path for restructuring="+path);
			
			// if the tree has been pruned, the path given to restructuring might be to long (but restructuring takes care of this)
			tree.doAllTests();
			if (counter == 17)
				System.out.println("seventeen");
			tree = tree.restructure(path);
			tree.doAllTests();
			double MDLafterRestructuring = tree.calculateGlobalMDLScore();
			System.out.println("MDLScore after restr"+MDLafterRestructuring);
			System.out.println("end restructuring");
			
			if (MDLbeforeRestructuring == MDLafterRestructuring)
			{			
				System.out.println("here");
				double MDLbeforePruning = tree.calculateGlobalMDLScore();
				if (!leaf.equals(tree.getRoot()) && leaf.getParent().hasOnlyLeafsAsChildren())
				{
					System.out.println("start pruning");
					tree.doAllTests();
					tree = tree.prune(path);
					tree.doAllTests();
					System.out.println("end pruning");
					tree.print();
				}
				double MDLafterpruning = tree.calculateGlobalMDLScore();
				
				if (MDLbeforePruning==MDLafterpruning && leaf.getClassification() !=sample.getClassification())
				{
						System.out.println("start extension");
						
						tree.doAllTests();
						tree = tree.extend(leaf);
						System.out.println("finished extension");
						tree.print();
						tree.doAllTests();
				}			
			}
			if (tree.root.getNegativeCount()>0)
			{
				System.out.println("imperfect classification");
			}
			tree.print();
		}	
		System.out.println("algorithm finished");
		tree.doAllTests();
		decisionTree = tree;
		return tree;
	}
	
	public Integer classify(Tree tree, Sample sample)
	{
		Node node = tree.getRoot();
		while (node instanceof InternalNode)
		{
			InternalNode nodeInt = (InternalNode) node;
		
			int attribute = nodeInt.getAttribute();
			
			String sampleValue = findValue(sample, attribute);
			try{
			node = nodeInt.getChild(new AttributeValuePair(attribute, sampleValue));
			}
			catch (RuntimeException e)
			{
				return null;
			}
		}
		
		Leaf leaf = (Leaf) node;
		
		return leaf.getClassification();
	}
		
	private String findValue(Sample sample, int attribute)
	{
		for (AttributeValuePair samplePair: sample.getAttributeValuePairs())
		{
			if (attribute==samplePair.getAttribute())
				return samplePair.getValue();
		}
		return "";
	}
	
	public void testing()
	{
		int wrongCounter = 0;
		int rightCounter = 0;
		while (testData.hasNext())
		{
			Sample sample = testData.getNextSample();
			
			Integer classification = classify(decisionTree, sample);
			if (classification==null || sample.getClassification()!=classification)
				wrongCounter++;
			else 
				rightCounter++;
		}
		System.out.println("wrong ="+wrongCounter+" right="+rightCounter);
	}
	public static void main (String[] args) throws IOException
	{
		//String datafilePath = "C:\\Dokumente und Einstellungen\\andis\\Eigene Dateien\\courses\\machine learning\\project";
		//String testDataFileName = "tic-tac-toe_test.data";
		//String trainingDataFile = "tic-tac-toe_mixed_reduced_further.";
		//String trainingDataFile = "tic-tac-toe_train.data";
		//String testDataFileName = "\\experiments\\SPECT.test";
		//String trainingDataFile = "\\experiments\\SPECT.train";
		//String datafileName = "AData.txt";
		
		if (args.length!=2)
		{
			System.out.println("usage: java MainAlgorithm <training data file> <test data file>");
		}
		String trainingDataFile = args[0];
		String testDataFile = args[1];
		MainAlgorithm algorithm = new MainAlgorithm(trainingDataFile, testDataFile);
		Tree tree = algorithm.learning();

		tree.print();
		System.out.println("final score = "+tree.calculateGlobalMDLScore());
		
		algorithm.testing();
	/*System.out.println("a priori decisionTree= ");
	decisionTree.print();
	//Sample sample = data.getNextSample();
	
	int sampleCounter = 0;
	

	//for (Sample sample :data.samples)	
	for (int i=0; i<4; i++)
	{

		Sample sample = data.getNextSample();
	}
	for (int i=0; i<5; i++)
	{
		Sample sample = data.getNextSample();
		sampleCounter++;
		System.out.println("Sample="+sample);
		decisionTree.encode(sample);				
	}
	
	double MDLScore = decisionTree.calculateGlobalMDLScore();

	System.out.println("after "+sampleCounter+" samples:");
	System.out.println("score= "+MDLScore );
	
	System.out.println("posteriori decisionTree= ");
	decisionTree.print();
	
	Tree newTree = decisionTree.extend((Leaf) decisionTree.root);
	System.out.println("after extending:");
	System.out.println("class of Root node="+newTree.root.getClass());
	newTree.print();
	MDLScore = newTree.calculateGlobalMDLScore();
	System.out.println("mdlScore="+MDLScore);
	
	Node node = ((HashSet<Edge>) ((InternalNode) newTree.getRoot()).getEdges()).iterator().next().getNode();
	Vector<AttributeValuePair> path = newTree.getPath(node);
	Node foundNode = newTree.findNode(path);
//	System.out.println(node.equals(foundNode));
	System.out.println("node="+newTree.root);
	System.out.println("pruned node="+((InternalNode) newTree.root).prune());

	
	Tree prunedTree = newTree.prune(new Vector<AttributeValuePair>());
	System.out.println("after pruning the root:");
	prunedTree.print();
	
	*/
	}
}
