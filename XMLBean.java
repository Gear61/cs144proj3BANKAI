package edu.ucla.cs.cs144;

import java.util.ArrayList;

public class XMLBean {
	private String m_itemID;
	private String m_name;
	private ArrayList <String> m_category = new ArrayList <String>();
	private String m_currently;
	private String m_firstBid;
	private String m_numBids;
	private ArrayList <XMLBid> m_bid = new ArrayList <XMLBid>();
	private String m_location;
	private String m_country;
	private String m_started;
	private String m_ends;
	private String m_userID;
	private String m_rating;
	private String m_desc;

	//itemID
	public void setItemID(String new_itemID) {
		m_itemID = new_itemID;
	}
	public String getItemID() {
		return m_itemID;
	}

	//name
	public void setName(String new_name) {
		m_name = new_name;
	}
	public String getName() {
		return m_name;
	}

	//category
	public void setCategory(String new_category) {
		m_category.add(new_category);
	}
	public ArrayList <String> getCategory() {
		return m_category;
	}

	//currently
	public void setCurrently(String new_currently) {
		m_currently = new_currently;
	}
	public String getCurrently() {
		return m_currently;
	}

	//first bid
	public void setFirstBid(String new_firstBid) {
		m_firstBid = new_firstBid;
	}
	public String getFirstBid() {
		return m_firstBid;
	}

	//number of bids
	public void setNumBids(String new_numBids) {
		m_numBids = new_numBids;
	}
	public String getNumBids() {
		return m_numBids;
	}

	//bids
	public void setBid(XMLBid new_bid) {
		m_bid.add(new_bid);
	}
	public ArrayList <XMLBid> getBid() {
		return m_bid;
	}

	//seller location
	public void setLocation(String new_location) {
		m_location = new_location;
	}
	public String getLocation() {
		return m_location;
	}

	//seller country
	public void setCountry(String new_country) {
		m_country = new_country;
	}
	public String getCountry() {
		return m_country;
	}

	//started
	public void setStarted(String new_started) {
		m_started = new_started;
	}
	public String getStarted() {
		return m_started;
	}

	//ends
	public void setEnds(String new_ends) {
		m_ends = new_ends;
	}
	public String getEnds() {
		return m_ends;
	}

	//userID
	public void setUserID(String new_userID) {
		m_userID = new_userID;
	}
	public String getUserID() {
		return m_userID;
	}

	//rating
	public void setRating(String new_rating) {
		m_rating = new_rating;
	}
	public String getRating() {
		return m_rating;
	}

	//desc
	public void setDesc(String new_desc) {
		m_desc = new_desc;
	}
	public String getDesc() {
		return m_desc;
	}

}