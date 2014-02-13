package daemonmanager.pmutils;

import java.util.ArrayList;

public class RecDoubleTest {
	
	
	public static void main(String[] args) {
		
		int arraySize = 8;
		ArrayList<Integer> ranks = new ArrayList<Integer>();
		for ( int i=0;i< arraySize;i++)
			ranks.add(i);
		int k=0;
		int jump = 1;
		while(k<2)
		{
			System.out.println("");
			int startIndex =0;	
			int src =0;
			for( int j =startIndex; src+jump < arraySize; j++)
			{			
				
					System.out.print(ranks.get(src) + "--> "  + ranks.get(src+jump) +" " );					
					src+=jump;
				
			}
			System.out.println("");
			src = jump;
			for( int j =startIndex+jump;j< arraySize; j++)
			{		
				if(j+jump < arraySize)
				{
					System.out.print(ranks.get(src) + "--> "  + ranks.get(src+jump) +" " );
					src++;
				}
			}
			jump++;
			startIndex++;
			k++;
		}
		

	}

}
