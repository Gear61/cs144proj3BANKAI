package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchResult;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.FieldName;

public class AuctionSearchTest
{
	public static void main(String[] args1)
	{
		AuctionSearch as = new AuctionSearch();

		String message = "Test message";
		String reply = as.echo(message);
		System.out.println("Reply: " + reply);

		/* String query = "star trek";
		SearchResult[] basicResults = as.basicSearch(query, 0, 0);
		System.out.println("Basic Search Query: " + query);
		for (SearchResult result : basicResults)
		{
			System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("Received " + basicResults.length + " results"); */
		
		/* public static final String ItemName = "ItemName"; // java.lang.String
		public static final String Category = "Category"; // java.lang.String
		public static final String SellerId = "SellerId"; // java.lang.String
		public static final String BuyPrice = "BuyPrice"; // java.lang.Double
		public static final String BidderId = "BidderId"; // java.lang.String
		public static final String EndTime = "EndTime"; // java.util.Date
		public static final String Description = "Description"; // java.lang.String*/ 

		SearchConstraint constraint = new SearchConstraint(FieldName.BuyPrice, "5.99");
		// SearchConstraint constraint1 = new SearchConstraint(FieldName.ItemName, "trek");
		// SearchConstraint constraint2 = new SearchConstraint(FieldName.SellerId, "intergalactic");
		//SearchConstraint constraint3 = new SearchConstraint(FieldName.Category, "Collectibles");
		//SearchConstraint constraint4 = new SearchConstraint(FieldName.Description, "brand");
		//SearchConstraint constraint5 = new SearchConstraint(FieldName.Description, "new");
		SearchConstraint[] constraints = {constraint};
		
		SearchResult[] advancedResults = as.advancedSearch(constraints, 0, 20);
		System.out.println("Advanced search received " + advancedResults.length + " results");
		for (SearchResult result : advancedResults)
		{
			System.out.println(result.getItemId() + ": " + result.getName());
		}

		String itemId = "1497595357";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// Add your own test here
	}
}
