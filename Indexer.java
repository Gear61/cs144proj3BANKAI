package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

public class Indexer
{

	/** Creates a new instance of Indexer */
	public Indexer()
	{
	}

	// JOOSTIN wrote this (reference the lucene tutorial)
	private IndexWriter indexWriter = null;

	/*
	 * JOOSTIN also wrote this IndexWriter constructor/retreiver same
	 * constructor used in the tutorial IndexWriter(String directory, analyzer,
	 * bool create); return if indexWriter has bee initialized
	 */
	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		String directory = System.getenv("LUCENE_INDEX") + "/basic-index";
		if (indexWriter == null)
		{
			indexWriter = new IndexWriter(directory, new StandardAnalyzer(), create);
		}
		return indexWriter;
	}

	/*
	 * can't forget to close our indexWriter!
	 */
	public void closeIndexWriter() throws IOException
	{
		if (indexWriter != null)
		{
			indexWriter.close();
		}
	}

	// this method iterates through the result set, rs, passed and indexes it
	public void getAuction(ResultSet rs, Connection conn) throws IOException
	{
		try
		{
			// made a bean to hold basic search values we need (itemID, name,
			// desc, category, in some order)
			// that needs to be indexed by lucene
			BasicBean toIndex;
			String itemID, category;
			// this IndexWriter is used in the indexAuction method
			// retreive the IndexWriter that's already initialized
			IndexWriter writer = getIndexWriter(false);
			// second query to run to get categories from database
			String innerQuery = "SELECT Category FROM Category WHERE ItemID = ";
			ResultSet rsInner;
			Statement sInner = conn.createStatement();
			// iterate through the query results till null
			while (rs.next())
			{
				toIndex = new BasicBean();
				toIndex.setName(rs.getString("Name"));
				toIndex.setDesc(rs.getString("Description"));
				itemID = rs.getString("ItemID");
				toIndex.setItemID(itemID);
				// complete the inner query to
				// SELECT Category FROM Category WHERE ItemID = (current itemID)
				innerQuery += itemID;
				rsInner = sInner.executeQuery(innerQuery);
				// get the category string from the result set
				category = getAllCat(rsInner);
				toIndex.setCategory(category);
				// reset the query string for next result
				innerQuery = "SELECT Category FROM Category WHERE ItemID = ";
				// pass basicbean to index method
				indexAuction(toIndex, writer);
			}
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
		catch (IOException ie)
		{
			System.out.println(ie);
		}
	}

	/**********************************/
	// this method grabs all categories for a given item and concatenates
	// then returns the reult string
	/**********************************/
	public String getAllCat(ResultSet rs)
	{
		try
		{
			String category;
			String categorySum = "";
			// iterate through given categories
			while (rs.next())
			{
				// grab current category
				category = rs.getString("Category");
				// attach to sum
				categorySum = categorySum + " " + category;
			}
			return categorySum;
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
		// in case there is no categories for a given result set, return "NULL"
		// string
		return "NULL";
	}

	/*******************************************/
	// this method indexes the given BasicBean for basic search function
	/*******************************************/
	public void indexAuction(BasicBean b, IndexWriter writer) throws IOException
	{
		Document doc = new Document();
		// add fields and values that are needed for the search
		doc.add(new Field("name", b.getName(), Field.Store.YES, Field.Index.TOKENIZED));
		doc.add(new Field("category", b.getCategory(), Field.Store.YES, Field.Index.TOKENIZED));
		doc.add(new Field("desc", b.getDesc(), Field.Store.YES, Field.Index.TOKENIZED));
		String fullSearchableText = b.getName() + " " + b.getCategory() + " " + b.getDesc();
		doc.add(new Field("content", fullSearchableText, Field.Store.NO, Field.Index.TOKENIZED));
		// write tuple to the writer
		writer.addDocument(doc);
	}

	public void rebuildIndexes()
	{

		Connection conn = null;

		// create a connection to the database to retrieve Items from MySQL
		try
		{
			conn = DbManager.getConnection(true);
		}
		catch (SQLException ex)
		{
			System.out.println(ex);
		}
		/********************************/
		// my code starts here
		/********************************/
		try
		{
			// open indexWriter
			getIndexWriter(true);
			// Query to retreive Name and Description for the basic search
			// ItemID is to do another query to get all the categories
			// because Auction does not hold categories
			String outerQuery = "SELECT ItemID, Name, Description FROM Auction";
			ResultSet rsOuter;
			Statement sOuter = conn.createStatement();
			// run the query!
			rsOuter = sOuter.executeQuery(outerQuery);
			// pass the resultset of the query to parse
			// have to pass the db connection too
			getAuction(rsOuter, conn);
			// close indexWriter
			closeIndexWriter();
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
		catch (IOException ie)
		{
			System.out.println(ie);
		}
		/********************************/
		// my code ends here
		/********************************/

		// close the database connection
		try
		{
			conn.close();
		}
		catch (SQLException ex)
		{
			System.out.println(ex);
		}
	}

	public static void main(String args[])
	{
		Indexer idx = new Indexer();
		idx.rebuildIndexes();
	}
}
