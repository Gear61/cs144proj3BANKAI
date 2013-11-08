package edu.ucla.cs.cs144;

public class BasicBean {
	/*
	* This is the bean used to store ItemID, Name, Category, and Descriptions
	* of the indexer
	* The category will be a concatenation of all the categories the item is a part of
	* ex: Item #1 is in 4 categories cat1, cat2, cat3, and cat4
	* category will hold "cat1 cat2 cat3 cat4" where each category will only be separated by spaces
	* (category will be tokenized by lucene)
	*/

	//constructor to initialize all strings as "NULL"
	public String itemID, name, category, desc;
	public BasicBean() {
		itemID = "NULL";
		name = "NULL";
		category = "NULL";
		desc = "NULL";
	}
	//now let's set some getters and setters
	/*****************************************/
	/*itemID*/
	/*****************************************/
	public void setItemID(String new_itemID) {
		itemID = new_itemID;
	}
	public String getItemID() {
		return itemID;
	}
	/*****************************************/
	/*name*/
	/*****************************************/
	public void setName(String new_name) {
		name = new_name;
	}
	public String getName() {
		return name;
	}
	/*****************************************/
	/*category*/
	/*****************************************/
	public void setCategory(String new_category) {
		category = new_category;
	}
	public String getCategory() {
		return category;
	}
	/*****************************************/
	/*description*/
	/*****************************************/
	public void setDesc(String new_desc) {
		desc = new_desc;
	}
	public String getDesc() {
		return desc;
	}
	/*****************************************/
	/*end of setters and getters*/
	/*****************************************/
}