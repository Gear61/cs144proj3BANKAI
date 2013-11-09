package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.SearchResult;


//justin's libraries and shiet
import java.util.ArrayList;
import org.apache.commons.lang3.StringEscapeUtils;
import java.lang.Object;

public class AuctionSearch implements IAuctionSearch
{
	// AXIS 2
	// username: admin
	// password: axis2
	
	// Convert user inputted form to MySQL form
	public static String convertDate(String time) throws ParseException
	{
		// Create a format that parses from XML date format into Java Date format
		String expectedPattern = "MMM-dd-yy HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(expectedPattern);   
        
		// Parse from passed in XML date to Java Date
        Date date = format.parse(time);
		
		// Create formatter to convert from Java Date to desired MySQL timestamp format
        String desiredPattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat format2 = new SimpleDateFormat(desiredPattern);
		
		// Format from Java Date to timestamp format. Return
		String convertedDate = format2.format(date);
		return convertedDate;
    }
	
	// Returns a WHERE clause given a search constraint
	public String convertConstraint (SearchConstraint s) throws ParseException
	{		
		if (s.getFieldName().equals(FieldName.SellerId))
		{
			return "Auction.UserID='" + s.getValue() + "'";
		}
		
		if (s.getFieldName().equals(FieldName.BuyPrice))
		{
			return "Buy_Price='" + s.getValue() + "'";
		}
		
		if (s.getFieldName().equals(FieldName.BidderId))
		{
			return "Bids.UserID='" + s.getValue() + "'";
		}
		
		if (s.getFieldName().equals(FieldName.EndTime))
		{
			return "Ends='" + convertDate(s.getValue()) + "'";
		}
		
		return "Error, invalid search constraint.";
	}
	
	public class CustomComparator implements Comparator<SearchResult>
	{
	    @Override
	    public int compare(SearchResult o1, SearchResult o2)
	    {
	        if (Integer.parseInt(o1.getItemId()) > Integer.parseInt(o2.getItemId()))
	        {
	        	return 1;
	        }
	        else if (Integer.parseInt(o1.getItemId()) < Integer.parseInt(o2.getItemId()))
	        {
	        	return -1;
	        }
	        else
	        {
	        	return 0;
	        }
	    }
	}
	
	// Finds intersection of 2 ArrayLists of SearchResults and returns it
	public ArrayList<SearchResult> mergeResultsLists (ArrayList<SearchResult> a, ArrayList<SearchResult> b)
	{		
		if (a.size() == 0 && b.size() == 0)
		{
			return new ArrayList<SearchResult>(); // Return empty list
		}
		if (a.size() == 0) // If first list is empty, just return second list because we know it has stuff
		{
			return b;
		}
		if (b.size() == 0)
		{
			return a;
		}
		
		// System.out.println("Merging Lucene and SQL results...");
		
		// Otherwise, sort, merge as new list, return
		// Sort puts this in ASCENDING order
		Collections.sort(a, new CustomComparator());
		Collections.sort(b, new CustomComparator());
		
		ArrayList<SearchResult> mergedList = new ArrayList<SearchResult>();
		
		int i = 0; // for A
		int j = 0; // for B
		while (i < a.size() && j < b.size())
		{
			// If ItemIDs are the same, we have found intersection
			if (Integer.parseInt(a.get(i).getItemId()) == Integer.parseInt(b.get(j).getItemId()))
			{
				mergedList.add(a.get(i));
				i++;
				j++;
			}
			// If they aren't the same, check for A > B. If so, advance B
			else if (Integer.parseInt(a.get(i).getItemId()) > Integer.parseInt(b.get(j).getItemId()))
			{
				j++;
			}
			else // Advance A (i)
			{
				i++;
			}
		}
		// System.out.println("Intersection has " + mergedList.size() + " results.");
		return mergedList;
	}
	
	public SearchResult[] basicSearch(String query, int numResultsToSkip, int numResultsToReturn)
	{
		ArrayList <SearchResult> results = new ArrayList <SearchResult>();
		try
		{
			// Do a search. Go HAM.
			// System.out.println("Running basic search");
			SearchEngine instance = new SearchEngine();
			Hits hits = instance.performSearch(query);

			// System.out.println("Basic search returned " + hits.length() + " results.");
			
			@SuppressWarnings("unchecked")
			Iterator<Hit> iter = hits.iterator();
			
			Hit hit;
			
			// Skip the number of results we were told to
			for (int i = 0; i < numResultsToSkip && iter.hasNext(); i++)
			{
				hit = iter.next();
			}
			
			if (numResultsToReturn == 0)
			{
				while(iter.hasNext())
				{
					hit = iter.next();
					Document doc = hit.getDocument();
					results.add(new SearchResult(doc.get("itemID"), doc.get("name")));
				}
			}
			else
			{
				for (int i = 0; i < numResultsToReturn && iter.hasNext(); i++)
				{
					hit = iter.next();
					Document doc = hit.getDocument();
					results.add(new SearchResult(doc.get("itemID"), doc.get("name")));
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception caught.\n");
		}
		SearchResult[] answerArray = new SearchResult[results.size()];
	    answerArray = results.toArray(answerArray);
		return answerArray;
	}

	public SearchResult[] advancedSearch(SearchConstraint[] constraints, int numResultsToSkip, int numResultsToReturn)
	{
		ArrayList <SearchResult> LuceneResults = new ArrayList <SearchResult>();
		ArrayList <SearchResult> SQLResults = new ArrayList <SearchResult>();
		
		Connection con = null;
		try
		{
			con = DbManager.getConnection(true);
		}
		catch (Exception e)
		{
			System.out.println("Connecting to the MySQL database failed.");
		}
		
		if (constraints.length > 0)
		{
			try
			{
				String from = "Auction";
				// Array of pseudo booleans. 0 = Buy_Price, 1 = Category, 2 = Bids
				
				int [] combo = {0,0,0};
				for (int i = 0; i < constraints.length; i++)
				{
					if (constraints[i].getFieldName().equals(FieldName.BuyPrice))
					{
						combo[0] = 1;
					}
					else if (constraints[i].getFieldName().equals(FieldName.Category))
					{
						combo[1] = 1;
					}
					else if (constraints[i].getFieldName().equals(FieldName.BidderId))
					{
						combo[2] = 1;
					}
				}
				
				// Join in Buy_Price
				if (combo[0] == 1)
				{
					from += " INNER JOIN Buy_Price on Auction.ItemID = Buy_Price.ItemID";
				}
				// Category
				if (combo[1] == 1)
				{
					from += " INNER JOIN Category on Auction.ItemID = Category.ItemID";
				}
				// Bids
				if (combo[2] == 1)
				{
					from += " INNER JOIN Bids on Auction.ItemID = Bids.ItemID";
				}

				// Feed to Lucene
				int numKeyConstraints = 0;
				ArrayList<SearchConstraint> ItemNameConstraints = new ArrayList<SearchConstraint>();
				ArrayList<SearchConstraint> CategoryConstraints = new ArrayList<SearchConstraint>();
				ArrayList<SearchConstraint> DescriptionConstraints = new ArrayList<SearchConstraint>();
				
				// Feed to SQL
				ArrayList<SearchConstraint> SQLConstraints = new ArrayList<SearchConstraint>();
				
				// Split constraints into keyword (Lucene) and non-keyword (SQL)
				for (int i = 0; i < constraints.length; i++)
				{
					if (constraints[i].getFieldName().equals("ItemName"))
					{
						ItemNameConstraints.add(constraints[i]);
						numKeyConstraints++;
					}
					else if (constraints[i].getFieldName().equals("Category"))
					{
						CategoryConstraints.add(constraints[i]);
						numKeyConstraints++;
					}
					else if (constraints[i].getFieldName().equals("Description"))
					{
						DescriptionConstraints.add(constraints[i]);
						numKeyConstraints++;
					}
					else
					{
						SQLConstraints.add(constraints[i]);
					}
				}
				
				// Grab all matching auctions for Lucene parameters
				// name:(+Mariott +Resort)
				// name:Mariott AND description:Comfortable
				if (numKeyConstraints > 0)
				{
					String query = "";
					
					if (ItemNameConstraints.size() > 0)
					{
						if (ItemNameConstraints.size() == 1)
						{
							query += "name:" + ItemNameConstraints.get(0).getValue();
						}
						else
						{
							for (int i = 0; i < ItemNameConstraints.size(); i++)
							{
								if (i == 0)
								{
									query += "name:(+" + ItemNameConstraints.get(0).getValue();
									continue;
								}
								query += " +" + ItemNameConstraints.get(i).getValue();
							}
							query += ")";
						}
					}
					
					// name:(+Mariott +Resort)
					// name:Mariott AND description:Comfortable
					if (CategoryConstraints.size() > 0)
					{
						if (CategoryConstraints.size() == 1)
						{
							if (query.equals(""))
							{
								query += "category:" + CategoryConstraints.get(0).getValue();
							}
							else
							{
								query += " AND category:" + CategoryConstraints.get(0).getValue();
							}
						}
						else
						{
							for (int i = 0; i < CategoryConstraints.size(); i++)
							{
								if (i == 0)
								{
									if (query.equals(""))
									{
										query += "category:(+" + CategoryConstraints.get(0).getValue();
									}
									else
									{
										query += " AND category:(+" + CategoryConstraints.get(0).getValue();
									}
									continue;
								}
								query += " +" + CategoryConstraints.get(i).getValue();
							}
							query += ")";
						}
					}
					
					// name:(+Mariott +Resort)
					// name:Mariott AND description:Comfortable
					if (DescriptionConstraints.size() > 0)
					{
						if (DescriptionConstraints.size() == 1)
						{
							if (query.equals(""))
							{
								query += "desc:" + DescriptionConstraints.get(0).getValue();
							}
							else
							{
								query += " AND desc:" + DescriptionConstraints.get(0).getValue();
							}
						}
						else
						{
							for (int i = 0; i < DescriptionConstraints.size(); i++)
							{
								if (i == 0)
								{
									if (query.equals(""))
									{
										query += "desc:(+" + DescriptionConstraints.get(0).getValue();
									}
									else
									{
										query += " AND desc:(+" + DescriptionConstraints.get(0).getValue();
									}
									continue;
								}
								query += " +" + DescriptionConstraints.get(i).getValue();
							}
							query += ")";
						}
					}
					
					if (query != "")
					{
						// System.out.println(query);
						LuceneResults = new ArrayList<SearchResult>(Arrays.asList(basicSearch(query, 0, 0)));
					}
				}
				
				// Grab all matching auctions for SQL parameters
				if (SQLConstraints.size() > 0)
				{
					String where = "";
					// Building the where portion

					for (int i = 0; i < SQLConstraints.size(); i++)
					{
						if (i == 0)
						{
							where += " " + convertConstraint(SQLConstraints.get(i));
						}
						else
						{
							where += " AND " + convertConstraint(SQLConstraints.get(i));
						}
					}

					Statement stmt = con.createStatement();
					String query = "SELECT DISTINCT Auction.ItemID, Name FROM " + from + " WHERE" + where;
					// System.out.println(query);
					ResultSet rs = stmt.executeQuery(query);

					while (rs.next())
					{
						SQLResults.add(new SearchResult(rs.getString(1), rs.getString(2)));
					}
					// System.out.println("SQL search returned " + SQLResults.size() + " results.");
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				System.out.println("Statement creation/execution failed");
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Date parsing failed.");
			}
			
			// Merge SQLResults and LuceneResults here
			ArrayList <SearchResult> results = mergeResultsLists(SQLResults, LuceneResults);
			
			ArrayList <SearchResult> finalResults = new ArrayList<SearchResult>();
			if (numResultsToReturn == 0) // Return ALL
			{
				for (int i = 0; (numResultsToSkip + i) < results.size(); i++)
				{
					finalResults.add(results.get(numResultsToSkip + i));
				}
			}
			else
			{
				for (int i = 0; i < numResultsToReturn && (numResultsToSkip + i) < results.size(); i++)
				{
					finalResults.add(results.get(numResultsToSkip + i));
				}
			}
			
			SearchResult[] answerArray = new SearchResult[finalResults.size()];
		    answerArray = finalResults.toArray(answerArray);
			return answerArray;
		}
		return new SearchResult[0]; // If no constraints passed in, return empty array
	}

/**************************************************************/
//THIS IS MINE (justin)
/**************************************************************/


	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!

		//connect to the database hurr
		Connection conn = null;
		XMLBean entry = new XMLBean();
		try{
			conn = DbManager.getConnection(true);

		} catch (SQLException e) {
			System.out.println("Connection to database failed!");
		}
		try {
			Statement s = conn.createStatement();
			ResultSet rs;
			String qAuction, qBids, qCategory, qUser, user, result;

			qAuction = "SELECT * FROM Auction WHERE ItemID="+itemId;
			qBids = "SELECT * FROM (SELECT * FROM Bids WHERE ItemID="+itemId+") as A INNER JOIN Ebay_Users as B WHERE A.UserID = B.UserID";
			qUser = "SELECT * FROM Ebay_Users WHERE UserID=\"";
			qCategory = "SELECT Category FROM Category WHERE ItemID="+itemId;

			rs = s.executeQuery(qAuction);
			rs.next();
			user = rs.getString("UserID");
			entry.setItemID(user);

			qUser = qUser + user + "\"";
			setXMLAuction(entry, rs);

			rs = s.executeQuery(qUser);
			setXMLUser(entry, rs);

			rs = s.executeQuery(qCategory);
			setXMLCategory(entry, rs);

			rs = s.executeQuery(qBids);
			setXMLBids(entry, rs);

			//completed XMLBean
			result = createItemXML(entry);
			return result;

		} catch (SQLException e) {
			System.out.println("ERROR: Query doesn't work in geXMLDataForItemID method!");
			System.out.println(e);
		} catch (java.text.ParseException pe) {
			System.out.println(pe);
		}

		return null;
	}

	public void setXMLCategory(XMLBean x, ResultSet rs) {
		try {
				String category;
				while(rs.next()) {
					category = rs.getString("Category");
					x.setCategory(category);
				}
		} catch(SQLException e) {
			System.out.println("ERROR: Result Set error in setXMLCategory");
			System.out.println(e);
		}
	}

	public String createItemXML(XMLBean entry) {
		String result;
		ArrayList <String> category = new ArrayList <String>();
		ArrayList <XMLBid> bid = new ArrayList <XMLBid>();
		category = entry.getCategory();
		bid = entry.getBid();

		//start of XML output
		result = 	"<Item ItemID=\""+entry.getItemID()+"\">\n";
		result +=		"\t<Name>"+entry.getName()+"</Name>\n";

		//category handler
		if (category.size() < 1) {
			result += 	"\t<Category />\n";
		}
		else {
			for (int i = 0; i < category.size(); ++i) {
				result+="\t<Category>"+category.get(i)+"</Category>\n";
			}
		}

		result +=		"\t<Currently>$"+entry.getCurrently()+"</Currently>\n";
		result +=		"\t<First_Bid>$"+entry.getFirstBid()+"</First_Bid>\n";
		result += 		"\t<Number_of_Bids>"+entry.getNumBids()+"</Number_of_Bids>\n";

		//bid handler
		if (bid.size() < 1) {	
			result +=	"\t<Bids />\n";
		}
		else {
			for (int i = 0; i < bid.size(); ++i) {
				result+= "\t<Bids>\n";
				result+= 	"\t\t<Bid>\n";
				result+= 		"\t\t\t<Bidder UserID=\""+bid.get(i).getBidderID()+"\" Rating=\""+bid.get(i).getRating()+"\">\n";
				result+= 			"\t\t\t\t<Location>"+bid.get(i).getLocation()+"</Location>\n";
				result+= 			"\t\t\t\t<Country>"+bid.get(i).getCountry()+"</Country>\n";
				result+= 		"\t\t\t</Bidder>\n";
				result+= 		"\t\t\t<Time>"+bid.get(i).getTime()+"</Time>\n";
				result+=		"\t\t\t<Amount>"+bid.get(i).getAmount()+"</Amount>\n";
				result+=	"\t\t</Bid>\n";
				result+= "\t</Bids>\n";			
			}
		}

		result +=		"\t<Location>"+entry.getLocation()+"</Location>\n";
		result +=		"\t<Country>"+entry.getCountry()+"</Country>\n";
		result +=		"\t<Started>"+entry.getStarted()+"</Started>\n";
		result +=		"\t<Ends>"+entry.getEnds()+"</Ends>\n";
		result +=		"\t<Seller UserID=\""+entry.getUserID()+"\" Rating=\""+entry.getRating()+"\" />\n";

		//Description handler
		if (entry.getDesc().equals(""))
			result +=	"\t<Description /> \n";
		else
			result +=	"\t<Description>"+entry.getDesc()+"</Description>\n";
		result +=		"</Item>";
		return result;
	}


	public void setXMLBids(XMLBean x, ResultSet rs) throws java.text.ParseException {
		try {
			XMLBid bid;
			String time;
			while(rs.next()) {
				bid = new XMLBid();
				bid.setBidderID(rs.getString("UserID"));
				bid.setRating(rs.getString("Rating"));
				bid.setLocation(StringEscapeUtils.escapeXml(rs.getString("Location")));
				bid.setCountry(StringEscapeUtils.escapeXml(rs.getString("Country")));
				time = rs.getString("BidTime");
				time = reverseDate(time);
				bid.setTime(time);
				bid.setAmount(rs.getString("Amount"));
				x.setBid(bid);
			}
		} catch (SQLException e) {
			System.out.println("ERROR: Result set error in setXMLBids");
			System.out.println(e);
		}
	}

	public void setXMLUser(XMLBean x, ResultSet rs) {
		try {
			rs.next();
			x.setUserID(rs.getString("UserID"));
			x.setRating(rs.getString("Rating"));
			x.setLocation(StringEscapeUtils.escapeXml(rs.getString("Location")));
			x.setCountry(StringEscapeUtils.escapeXml(rs.getString("Country")));
		} catch (SQLException e) {
			System.out.println("ERROR: Result set error in setXMLUser");
			System.out.println(e);
		}
	}

	public void setXMLAuction(XMLBean x, ResultSet rs) throws java.text.ParseException {
		try {
			String time;
			x.setName(rs.getString("Name"));
			x.setCurrently(rs.getString("Currently"));
			x.setFirstBid(rs.getString("First_Bid"));
			x.setNumBids(rs.getString("Number_of_Bids"));
			time = rs.getString("Started");
			time = reverseDate(time);
			x.setStarted(time);
			time = rs.getString("Ends");
			time = reverseDate(time);
			x.setEnds(time);
			x.setDesc(StringEscapeUtils.escapeXml(rs.getString("Description")));		
		} catch (SQLException e) {
			System.out.println("ERROR: Result set error in setXMLAuction method!");
		}
	}

	// Convert user inputted form to MySQL form
	public static String reverseDate(String time) throws java.text.ParseException
	{
		// Create a format that parses from XML date format into Java Date format
		String expectedPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(expectedPattern);   
        
		// Parse from passed in XML date to Java Date
        Date date = format.parse(time);
		
		// Create formatter to convert from Java Date to desired MySQL timestamp format
        String desiredPattern = "MMM-dd-yy HH:mm:ss";
		SimpleDateFormat format2 = new SimpleDateFormat(desiredPattern);
		
		// Format from Java Date to timestamp format. Return
		String convertedDate = format2.format(date);
		return convertedDate;
    }

	public String echo(String message)
	{
		return message;
	}
}