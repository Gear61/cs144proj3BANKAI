We now have the Ebay auction data in a database. This project consisted of us creating Java functions for users to utilize to grab auctions that they're interested in. We created basic search that lets users search using keywords across auction title, description, and category along with advanced search which allowed users to search across the same fields and then some (bidder, end time, buy price, etc). We also created a function that returns the XML data for an item given its ID. Lastly, we published all of our new functions as web services using Axis2 and Apache Tomcat.

Partner:
Name: Won Kyu Lee
ID: 904083134
Email: wlee89@ucla.edu

Comments about the code is written in the source code.  In general, we are running a query to get itemID, name, description from Auctions table and using the itemID to get category per each item.  Our category is indexed by concatenating all categories for that 1 item and indexing in it as a string.  This will help keep the category column as 1 column and allow the analyzer to do work it is supposed to do.  Created another data structure to hold each item to be indexed by Lucene.
