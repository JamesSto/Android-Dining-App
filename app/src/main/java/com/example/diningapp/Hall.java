package com.example.diningapp;

/**
 * This class is an Dining Hall, an instance representation of an All You Care to Eat dining hall on Cornell Campus. 
 * A master list of these is created and stored in the MasterEateryList class, and can be used by the 
 * main program to access information about Halls
 * 
 * This class extends Eatery - see Eatery class for more info
 * 
 * @author James Stoyell
 *
 */
public class Hall extends Eatery{
	
	/**
	 * The code necessary for a POST request to get the menu from Cornell
	 */
	public int hallMenuCode;
	/**
	 * The calendar code src necessary for a GET request to Google Calendars for this eatery
	 */
	public String hallCalendarCode;
	
	/**
	 * An HoursRequester object that is specific to this hall containg information about the schedule of the hall
	 */
	public HoursRequester hours;
	
	public Hall(String officialName, String commonName, String description, int logo, String mapsQuery, double latitude, double longitude, int hallMenuCode, String hallCalendarCode)
	{
		super(officialName, commonName, description, logo, mapsQuery, latitude, longitude);
		this.hallMenuCode = hallMenuCode;
		this.hallCalendarCode = hallCalendarCode;
	}
}
