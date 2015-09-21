package com.example.diningapp;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Scrapes the cornell webserver using a POST request to get menu data
 * @author James Stoyell
 *
 */
public class MenuRequester extends AsyncTask<Void, Void, String>{
	
	/**
	 * The baseURL for the POST request, formed in the constructor
	 */
	private URL baseURL;
	//TODO: Change USER_AGENT to something more official
	/**
	 * The User Agent used for this class's POST request
	 */
	private final String USER_AGENT = "JAMES S : jms852@Cornell.edu";
	/**
	 * The current date, formatted such that it can be used for the Cornell Dining's POST request
	 */
	private String date;
	/**
	 * The meal to be requested - Breakfast, Lunch, Brunch, or Dinner
	 */
	private String meal;
	/**
	 * The Hall to retrieve information about
	 */
	private Hall eats;
	/**
	 * The String to be returned - defaults to error message so that if it remains unchanged the user is displayed a message
	 */
	public String returnMenuString = "There has been an error";
	/**
	 * The menu TextView that this information should be added to
	 */
	private TextView menu;
	/**
	 * The menu regex pattern that matches an item on the menu
	 * <p>Group 0: The entire string
	 * <p>Group 1: The item string, either an item or header, e.g. "Turkey Burgers" or "Grill Station"
	 */
	private static final Pattern MENU_PATTERN = Pattern.compile("<(?:h4|p) class=\"(?:menuCatHeader|menuItem)\">([^<>]+)<\\/(?:h4|p)>");
	
	/**
	 * Precondition: All parameters are formatted as needed for a POST request by http://living.sas.cornell.edu/dine/whattoeat/menus.cfm
	 * <p>Scrapes information from the aforementioned URL about specific meal at date and location specified
	 * @param date The date to request menu information from
	 * @param meal The meal, one of three possible strings "Breakfast", "Lunch", or "Dinner"
	 * @param hallName The hall name, needed by the group request
	 */
	public MenuRequester(String date, String meal, Hall eats, TextView menu)
	{
		super();
		this.date = date;
		this.meal  = meal;
		this.eats = eats;
		this.menu = menu;
		 try {
			baseURL = new URL("http://living.sas.cornell.edu/dine/whattoeat/menus.cfm");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.err.println("ERROR: URL WAS MALFORMED CANNOT REQUEST MENUS");
		}
	}
	
	/**
	 * Grabs the menu for the given parameters with a GET request to Cornell servers
	 * @return the parsed menu string formatted as HTML
	 */
	public String getMenu()
	{
		String response = "";
		try{
			HttpURLConnection connection = (HttpURLConnection) baseURL.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setDoOutput(true);
			
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			String queryParams = "menudates=" + date + "&menuperiod=" + meal + "&menulocations=" + eats.hallMenuCode;
		    writer.write(queryParams);
		    writer.close();
		    
		    BufferedReader menuHTMLResponse = new BufferedReader(
			        new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer responseBuffer = new StringBuffer();
	 
			while ((inputLine = menuHTMLResponse.readLine()) != null) {
				responseBuffer.append(inputLine);
			}
			menuHTMLResponse.close();
			response = new String(responseBuffer);
		}
		catch(IOException i)
		{
			i.printStackTrace();
			System.err.println("ERROR: IOEXCEPTION DURING POST REQUEST FOR MENU");
			return "Error retrieving menu data";
		}
		response = parseHTMLForMenu(response);
	    
		return response;
	}
	
	/**
	 * Returns a neat menu string from raw HTML
	 * @param HTML
	 * HTML code from http://living.sas.cornell.edu/dine/whattoeat/menus.cfm retrieved from getMenu()
	 * @return neat menu string formatted with nice HTML
	 */
	private String parseHTMLForMenu(String HTML)
	{

		ArrayList<String> menuLines = new ArrayList<String>();
		if(!HTML.contains(eats.commonName) && !HTML.contains(eats.officialName))
		{
			return "Closed";
		}
		HTML = HTML.replaceAll("<[^<>]*img src[^<>]*>", "");
		Matcher menuMatcher = MENU_PATTERN.matcher(HTML);
		while(menuMatcher.find())
		{
			if(menuMatcher.group(0).contains("menuItem"))
			{
				menuLines.add("\t\t\t" + menuMatcher.group(1));
			}
			else
			{
				menuLines.add("<b>" + menuMatcher.group(1) + "</b>");
			}
		}
		String parsedMenu = "";
		for(String s : menuLines)
		{
			s = s.replace("&nbsp;", "");
			s += "<br/>";
			parsedMenu += s;
		}
		return !parsedMenu.isEmpty() ? parsedMenu : "Closed";
	}
	

	@Override
	/**
	 * Checks to see if brunch is an option - returns that if it is, otherwise, returns the menu from the getMenu() call
	 * for the appropriate meal
	 */
	protected String doInBackground(Void... params) {
		if(meal.equals("Breakfast"))
		{
			meal = "Brunch";
			returnMenuString = getMenu();
			if(!returnMenuString.equals("Closed"))
			{
				return "BRUNCHNOTIFIER" + returnMenuString;
			}
			else
			{
				meal = "Breakfast";
			}
		}
		
		return getMenu();
	}
	
	@SuppressLint("CutPasteId")
	@Override
	/**
	 * Posts the result to the appropriate TextView
	 * Changes headers to remove Breakfast and Lunch and add Brunch if necessary
	 * If no menu was given, but hours show that the hall is open for that meal, shows an error
	 */
	protected void onPostExecute(String result)
	{
		HallActivity c = (HallActivity) HallActivity.getContext();
		if(result.contains("BRUNCHNOTIFIER"))
		{
			result = result.replace("BRUNCHNOTIFIER", "");
			final TextView b = (TextView)c.findViewById(R.id.breakfastMenuLabel);

			b.setText("Brunch");
			final LinearLayout l = (LinearLayout)c.findViewById(R.id.lunchLayout);
			final TextView lt = (TextView)c.findViewById(R.id.lunchTime);
			if(lt.getText().equals("CLOSED"))
			{
				l.setVisibility(View.GONE);
				final TextView h = (TextView)c.findViewById(R.id.lunchBotHr);
				h.setVisibility(View.GONE);
			}
		}
		if(result.equals("Closed"))
		{
			//Meal will never be brunch in this case, as it would not have assigned BRUNCHNOTIFIER as above
			if(meal.equals("Breakfast"))
			{
				final TextView h = (TextView) c.findViewById(R.id.breakfastTime);
				if(!h.getText().equals("CLOSED"))
				{
					result = "<b>Error:</b> A menu is not available at this time";
				}
			}
			if(meal.equals("Lunch"))
			{
				final TextView h = (TextView) c.findViewById(R.id.lunchTime);
				if(!h.getText().equals("CLOSED"))
				{
					result = "<b>Error:</b> A menu is not available at this time";
				}
			}
			if(meal.equals("Dinner"))
			{
				final TextView h = (TextView) c.findViewById(R.id.dinnerTime);
				if(!h.getText().equals("CLOSED"))
				{
					result = "<b>Error:</b> A menu is not available at this time";
				}
			}
		}
		menu.setText(Html.fromHtml(result));
	}
}