import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Provides an Iterator to travers the tree's nodes, internal nodes or leaves.
 * @author andis
 *
 */
public class TraverseTree implements Iterator{
	
	public static final int ALL = 1;
	public static final int INTERNAL = 2;
	public static final int LEAVES = 3;
	
	public Vector<Node> nodes;
	private int position;
	
	
	/**
	 * Constructs an interator to travers the tree according to the constraint.
	 * @param tree The tree to be traversed.
	 * @param constraint Determines which of the nodes will be traversed (Traverse.ALL, 
	 * Traverse.INTERNAL or Traverse.LEAVES).
	 */	
	public TraverseTree(Tree tree, int constraint)
	{
		nodes = new Vector<Node>();	
		position = 0;
		Node node = tree.getRoot();

		traverse(node, constraint);	
	}
	
	public void remove ()
	{
		nodes.remove(position-1);
	}


	private void traverse(Node node, int constraint)
	{
		if (node instanceof InternalNode)
		{
			if (constraint == ALL|| constraint == INTERNAL)
			{
				nodes.add(node);
			}
			for (Edge edge :((InternalNode) node).getEdges())
			{
				traverse(edge.getNode(), constraint);
			}		
		}
		else
		{
			if (constraint == ALL|| constraint == LEAVES)
			{
				nodes.add(node);
			}
		}
		
	}
	

	public Node next()
	{
		if (!hasNext()) {
			throw new NoSuchElementException("No more nodes");
		}
		Node nextElement = nodes.get(position);
		position++;
		return nextElement;
	}
	
	public boolean hasNext()
	{
		return position < nodes.size();
	}
}
