package com.example.diningapp;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is an eatery, an instance representation of a store, cafe, or other non-dining hall eatery on Cornell campus. 
 * A master list of these is created and stored in the MasterEateryList class, and can be used by the 
 * main program to access information about Cafes.
 * 
 * This class extends Eatery - see Eatery class for more info
 * 
 * @author James Stoyell
 *
 */
public class Cafe extends Eatery{
	
	private final int MINUTES_PER_HOUR = 60;
	private final int HOURS_PER_DAY = 24;
	private final int MORNING_OVERFLOW_HOURS = 4;

	/**
	 * The payment type for the Cafe, either accepting BRBs with the string "BRB" or only taking cash with the string "Cash"
	 */
	public String paymentType;
	/**
	 * An HTML string representing this cafe's menu
	 */
	public String menu;
	/**
	 * An HTML string representing this Cafe's operating hours - DOES NOT update for holidays and the like (static)
	 */
	public String hours;
	/**
	 *  A hashtable for converting from a short week length string to a corresponding code (1 for Sunday, 2 for Monday, etc)
	 */
	private static Hashtable<String, Integer> dayCodes = new Hashtable<String, Integer>();
	
	//HELP I STARTED USING REGULAR EXPRESSIONS AND NOW I CAN'T STOP
	/**
	 * The pattern to match a single day's times in the hours string
	 * 
	 * <p>Group 0: The entire string
	 * <p>Group 1: The day of week string
	 * <p>Group 2: The start block hours
	 * <p>Group 3: The start block minutes
	 * <p>Group 4: THe start block AM/PM
	 * <p>Group 5: The end block hours
	 * <p>Group 6: The end block minutes
	 * <p>Group 7: The end block AM/PM
	 */
	private static final Pattern SINGLE_DAY_PATTERN = Pattern.compile("<b>(\\w+)</b> ?(?:&nbsp )*(\\d\\d?):(\\d\\d)(AM|PM) - (\\d\\d?):(\\d\\d)(AM|PM)");
	
	/**
	 * The pattern to match a range of days in the hours string
	 * 
	 * <p>Group 0: The entire string
	 * <p>Group 1: Start day string
	 * <p>Group 2: End day string
	 * <p>Group 3: The start block hours
	 * <p>Group 4: The start block minutes
	 * <p>Group 5: THe start block AM/PM
	 * <p>Group 6: The end block hours
	 * <p>Group 7: The end block minutes
	 * <p>Group 8: The end block AM/PM
	 */
	private static final Pattern DAY_RANGE_PATTERN = Pattern.compile("<b>(\\w+)-(\\w+)</b> ?(?:&nbsp ?)*(\\d\\d?):(\\d\\d)(AM|PM) - (\\d\\d?):(\\d\\d)(AM|PM)");
	
	static //Initializes the dayCodes hashtable
	{
		dayCodes.put("Sun", Calendar.SUNDAY);
		dayCodes.put("M", Calendar.MONDAY);
		dayCodes.put("T", Calendar.TUESDAY);
		dayCodes.put("W", Calendar.WEDNESDAY);
		dayCodes.put("Th", Calendar.THURSDAY);
		dayCodes.put("F", Calendar.FRIDAY);
		dayCodes.put("Sat", Calendar.SATURDAY);
	}
	
	public Cafe(String paymentType, String officialName, String commonName, String description, int logo, String mapsQuery, double latitude, double longitude, String menu, String hours)
	{
		super(officialName, commonName, description, logo, mapsQuery, latitude, longitude);
		this.paymentType = paymentType;
		this.menu = menu;
		this.hours = hours;
	}
	
	/**
	 * Checks the hours within the hours string and compares to the current time to see if this cafe is open
	 * @return Whether the cafe is open
	 */
	public boolean isOpen()
	{
		Calendar now = Calendar.getInstance();
		int currentDay = now.get(Calendar.DAY_OF_WEEK);
		int currentTime = (now.get(Calendar.HOUR_OF_DAY) + (TimeZone.getTimeZone("EST").getDSTSavings() == 0 ? 1 : 0)) * MINUTES_PER_HOUR + now.get(Calendar.MINUTE);
		//Some times will spill over into the next day, so we need to account for this
		//For instance, a valid time block might be "Fri      9:00AM - 3:00AM", so we count the extra hours that are
		//technically part of Saturday as more hours within Friday
		if(currentTime < MORNING_OVERFLOW_HOURS * MINUTES_PER_HOUR)
		{
			currentTime += HOURS_PER_DAY * MINUTES_PER_HOUR;
			currentDay -= 1;
			//Route back to Saturday if the day was Sunday, otherwise just go to the day before
			currentDay = currentDay != Calendar.SUNDAY ? currentDay - 1 : Calendar.SATURDAY;
		}
		
		Matcher m = SINGLE_DAY_PATTERN.matcher(hours);
		while(m.find())
		{
			if(dayCodes.get(m.group(1)) == currentDay)
			{
				int start = getTimeInMinutes(m.group(2), m.group(3), m.group(4));
				int end = getTimeInMinutes(m.group(5), m.group(6), m.group(7));
				if(end < MORNING_OVERFLOW_HOURS * MINUTES_PER_HOUR)
				{
					end += HOURS_PER_DAY * MINUTES_PER_HOUR;
				}
				return currentTime >= start && currentTime <= end;
			}
		}
		m = DAY_RANGE_PATTERN.matcher(hours);
		while(m.find())
		{
			int startDay = dayCodes.get(m.group(1));
			int endDay = dayCodes.get(m.group(2));
			
			if(currentDay >= startDay && currentDay <= endDay)
			{
				int start = getTimeInMinutes(m.group(3), m.group(4), m.group(5));
				int end = getTimeInMinutes(m.group(6), m.group(7), m.group(8));
				if(end < MORNING_OVERFLOW_HOURS * MINUTES_PER_HOUR)
				{
					end += HOURS_PER_DAY * MINUTES_PER_HOUR;
				}
				return currentTime >= start && currentTime <= end;
			}
		}
		return false;
	}
	
	/**
	 * Calculates the number of minutes since the start of the day given the parameters passed in
	 * @param hours The hours digits as if on a digital clock - Must be 12 or less
	 * @param minutes The minutes digits as if on a digital clock - Must be less that 60
	 * @param period AM or PM, the period of the day
	 * @return Number of minutes since the start of the day
	 */
	private int getTimeInMinutes(String hours, String minutes, String period)
	{
		int time = Integer.parseInt(hours);
		if(period.equals("PM"))
		{
			time += 12;
		}
		time *= MINUTES_PER_HOUR;
		return time + Integer.parseInt(minutes);
	}
}
