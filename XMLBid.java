package edu.ucla.cs.cs144;

public class XMLBid {
	private String m_bidderID;
	private String m_rating;
	private String m_location;
	private String m_country;
	private String m_time;	
	private String m_amount;
	//bid.bidderID
	public void setBidderID(String new_bidderID) {
		m_bidderID = new_bidderID;
	}
	public String getBidderID() {
		return m_bidderID;
	}

	//bid.rating
	public void setRating(String new_rating) {
		m_rating = new_rating;
	}
	public String getRating() {
		return m_rating;
	}

	//bid.location
	public void setLocation(String new_location) {
		m_location = new_location;
	}
	public String getLocation() {
		return m_location;
	}

	//bid.country
	public void setCountry(String new_country) {
		m_country = new_country;
	}
	public String getCountry() {
		return m_country;
	}
	
	public void setTime(String new_time) {
		m_time = new_time;
	}	
	public String getTime() {
		return m_time;
	}
	
	public void setAmount(String new_amount) {
		m_amount = new_amount;
	}	
	public String getAmount() {
		return m_amount;
	}	

}