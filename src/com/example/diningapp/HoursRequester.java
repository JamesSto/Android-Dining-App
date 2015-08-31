package com.example.diningapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Pair;
import android.widget.ProgressBar;

/**
 * An AsyncTask that grabs schedule information from the Cornell servers then returns that that the Hall class
 * Information is grabbed from a Google Calendar using JSoup and put into clean times. Some guessing is required to find
 * out which meal each time range represents
 * @author James Stoyell
 *
 */
public class HoursRequester extends AsyncTask<Void, Void, String[]>{
	/**
	 * The base of the URL for the GET request to google calendars - need to append the calendar src specific to the Eatery
	 */
	private final String URL_BASE = "https://www.google.com/calendar/htmlembed?mode=WEEK&src=";
	/**
	 * Regex pattern for retreiving schedule from input
	 * <p>Group 0: The entire string, usually "[time] [meal] until [time]"
	 * <p>Group 1: The starting time
	 * <p>Group 2: The hour of the starting time
	 * <p>Group 3: The ending time
	 */
	private static final Pattern HOUR_PATTERN = Pattern.compile("((\\d+):?\\d*(?:am|pm)?)[a-zA-Z /]+until (\\d+:?\\d*(?:am|pm)?)");
	
	/**
	 * The time pattern for extracting real time data from the time string
	 * 
	 * <p>Group 0: The entire string, some sequence of "[time] - [time]", where time is formatted in many different possible ways
	 * <p>Group 1: The hours block of the start time
	 * <p>Group 2: The minutes block of the start time
	 * <p>Group 3: am or pm of start time, if present
	 * <p>Group 4: The hours block of the end time
	 * <p>Group 5: The minutes block of the end time
	 * <p>Group 6: am or pm of the start time, if present
	 */
	private static final Pattern TIME_PATTERN = Pattern.compile("(\\d?\\d)(?::(\\d\\d))?(am|pm)? - (\\d?\\d)(?::(\\d\\d))?(am|pm)?");
	/**
	 * The URL instance of the calendar
	 */
	private URL calendarURL;
	/**
	 * The breakfast hours for this instance
	 */
	public String breakfastHours= "";
	/**
	 * The lunch hours for this instance
	 */
	public String lunchHours = "";
	/**
	 * The dinner hours for this instance
	 */
	public String dinnerHours = "";
	/**
	 * The hallActivity to relay the hours information back to
	 */
	public HallActivity hall;
	/**
	 * Variable to make sure that hours are only checked as often as necessary
	 */
	private long timeSinceLastHoursCheck = 0;
	
	public HoursRequester(Hall eats)
	{
		try {
			calendarURL = new URL(URL_BASE + eats.hallCalendarCode);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public HoursRequester(HoursRequester r, HallActivity h)
	{
		this.calendarURL = r.calendarURL;
		this.breakfastHours = r.breakfastHours;
		this.lunchHours = r.lunchHours;
		this.dinnerHours = r.dinnerHours;
		this.timeSinceLastHoursCheck = r.timeSinceLastHoursCheck;
		this.hall = h;
	}
	
	/**
	 * Gets the HTML calendar from the Google Calendar
	 */
	private void getHours()
	{
		if(System.currentTimeMillis() - timeSinceLastHoursCheck > 120000) //2 minutes have passed
		{
			timeSinceLastHoursCheck = System.currentTimeMillis();
			BufferedReader calendarReader = null;
			String response = "Network Error";
			try {
				URLConnection c = calendarURL.openConnection();
				c.setRequestProperty("User-Agent", "JAMES S : jms852@Cornell.edu");
				calendarReader = new BufferedReader(new InputStreamReader(c.getInputStream()));
				String inputLine;
				StringBuffer responseBuffer = new StringBuffer();
				while ((inputLine = calendarReader.readLine()) != null) {
					responseBuffer.append(inputLine);
				}
				calendarReader.close();
				response = new String(responseBuffer);
			}
			catch (IOException e) {
				System.out.println(hall.place.commonName);
				System.out.println("Something went wrong");
				System.out.println(calendarURL);
				e.printStackTrace();
			}
			
			parseHTMLForCalendar(response);
		}
	}
	
	/**
	 * Takes in the raw HTML from the calendar page and parses it down into individual hours for each meal
	 * @param HTML
	 */
	private void parseHTMLForCalendar(String HTML)
	{
		breakfastHours = "";
		lunchHours = "";
		dinnerHours = "";
		Document cal = Jsoup.parse(HTML);
		Elements day = cal.select(".cell-today");
		//Compiles all text from inside tags with "cell-today" class into a single string
		String times = day.text();

		Matcher m = HOUR_PATTERN.matcher(times);
		ArrayList<String> hourList = new ArrayList<String>();
		//A lot of guesswork goes on in this loop - goes through each string that matches the hourPattern
		//And tries to find the meal it belongs to
		while(m.find())
		{
			hourList.add(m.group(1) + " - " + m.group(2));
			if(m.group(0).contains("Breakfast") || m.group(0).contains("Brunch"))
			{
				breakfastHours += (breakfastHours.isEmpty() ? "" : "\n") + m.group(1) + " - " + m.group(3);
			}
			else if(m.group(0).contains("Lunch"))
			{
				lunchHours = lunchHours.isEmpty() ? m.group(1) + " - " + m.group(3) : lunchHours.substring(0,lunchHours.indexOf("-")+1) + " " + m.group(3);
			}
			else if(m.group(0).contains("Dinner") || m.group(0).contains("Supper"))
			{
				dinnerHours += (dinnerHours.isEmpty() ? "" : "\n") + m.group(1) + " - " + m.group(3);
			}
			else //And so the guessing begins
			{
				if(m.group(1).contains("am") && Integer.parseInt(m.group(2)) < 10)
				{
					breakfastHours += (breakfastHours.isEmpty() ? "" : "\n") + m.group(1) + " - " + m.group(3);
				}
				else if(m.group(0).contains("pm") && Integer.parseInt(m.group(2)) >= 3 && Integer.parseInt(m.group(2)) < 9)
				{
					dinnerHours += (dinnerHours.isEmpty() ? "" : "\n") + m.group(1) + " - " + m.group(3);
				}
				else //Well.... I guess it's lunch?
				{
					lunchHours = lunchHours.isEmpty() ? m.group(1) + " - " + m.group(3) : lunchHours.substring(0,lunchHours.indexOf("-")+1) + " " + m.group(3);
				}
			}
		}
	}
	
	public boolean isOpen()
	{
		getHours();
		String[] times = {breakfastHours, lunchHours, dinnerHours};

		ArrayList<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer, Integer>>();
		
		for(int i = 0; i < times.length; i++)
		{
			Matcher m = TIME_PATTERN.matcher(times[i]);
			while(m.find())
			{
				int start = Integer.parseInt(m.group(1)) * 60;
				
				if(m.group(2) != null)
				{
					start += Integer.parseInt(m.group(2));
				}
				if((m.group(3) != null && m.group(3).equals("pm")) || i == 2
						|| (Integer.parseInt(m.group(1)) < 5 && i == 1))
				{
					start += 12 * 60;
				}
				int end = Integer.parseInt(m.group(4)) * 60;
				if(m.group(5) != null)
				{
					end += Integer.parseInt(m.group(5));
				}
				if((m.group(6) != null  && m.group(6).equals("pm")) || i == 2
						|| (Integer.parseInt(m.group(4)) < 5 && i == 1))
				{
					end += 60 * 12;
				}
				ranges.add(new Pair<Integer, Integer>(start, end));
			}
		}
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		int currentTime = (c.get(Calendar.HOUR_OF_DAY) + (TimeZone.getTimeZone("EST").getDSTSavings() == 0 ? 1 : 0)) * 60 + c.get(Calendar.MINUTE);

		for(Pair<Integer, Integer> range : ranges)
		{
			if(currentTime > range.first && currentTime < range.second)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks to see if they should be Closed instead, then returns them in the form of a String[]
	 */
	@Override
	protected String[] doInBackground(Void... params) {
		getHours();
		if(breakfastHours.isEmpty())
		{
			breakfastHours = "CLOSED";
		}
		if(lunchHours.isEmpty())
		{
			lunchHours = "CLOSED";
		}
		if(dinnerHours.isEmpty())
		{
			dinnerHours = "CLOSED";
		}
		
		return new String[]{breakfastHours, lunchHours, dinnerHours};
	}
	
	/**
	 * Sets the hours in their appropriate TextViews and removes the loading bars
	 */
	@Override
	protected void onPostExecute(String[] hours)
	{
		hall.breakfastTime.setText(hours[0]);
    	hall.bLoad.setVisibility(ProgressBar.GONE);
    		
    	hall.lunchTime.setText(hours[1]);
    	hall.lLoad.setVisibility(ProgressBar.GONE);
    	
    	hall.dinnerTime.setText(hours[2]);
    	hall.dLoad.setVisibility(ProgressBar.GONE);
	}

}
