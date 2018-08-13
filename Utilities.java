
public class Utilities {

	
	static public double factorial(int n)
	{
		double factorial = 1;
		for (int i = 1; i<=n; i++)
		{
			factorial *= i;
		}
		
		return factorial;
	}
	
	static public double combination (int n, int k)
	{			
		return factorial(n) / (factorial(k) * factorial(n-k));
	}
}
