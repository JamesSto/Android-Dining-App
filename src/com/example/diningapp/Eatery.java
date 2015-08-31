package com.example.diningapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * This abstract class is a superclass of both Hall and Cafe, and represents a place to eat on Cornell campus
 * Fields are used to populate both the listView and the Eatery's corresponding activity.
 * @author James Stoyell
 *
 */
public abstract class Eatery implements Comparable<Eatery>{
	
	/**
	 * The time to wait before refreshing the user's location if requested, in milliseconds
	 */
	private final int LOCATION_REFRESH_DELAY = 30000; //30 seconds
	/**
	 * Only sort of an official name - actually just a unique string that will be present in any given name that might show up in an HTML doc or other external input
	 */
	String officialName;
	/**
	 * The common name of the Eatery that will be shown to the user
	 */
	String commonName;
	/**
	 * The HTML formatted string description of the Eatery that will be shown to the user, generally received from the Cornell site
	 */
	String description;
	/**
	 * The integer reference to the logo of this Eatery, stored in the res folder
	 */
	int logo;	
	/**
	 * String query used when opening google maps to get directions
	 */
	String mapsQuery;
	/**
	 * Latitude coordinate as float
	 */
	double latitude;
	/**
	 * Longitude coordinate as float
	 */
	double longitude;
	
	/**
	 * A reference to the user preferences
	 */
	private static SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DiningList.getContext());
	
	/**
	 * The time that the user location was last checked for. Unix timestamp of milliseconds since some date in 1970. Defaults to 0 to show that location has not been checked during this execution
	 */
	private static long timeOfLastLocationCheck = 0;
	/**
	 * A location representing the last Location received from the Android OS. Just gets most recent when updated, doesn't force a location update
	 */
	private static Location userLocation = null;
	
	public Eatery(String officialName, String commonName, String description, int logo, String mapsQuery, double latitude, double longitude)
	{
		this.officialName = officialName;
		this.commonName = commonName;
		this.description = description;
		this.logo = logo;
		this.mapsQuery = mapsQuery;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Compares Eateries for sorting purposes based on current user preferences
	 * Either alphabetical or location based
	 */
	@Override
	public int compareTo(Eatery another) {

		if(prefs.getString("sortOptionsKey", "Error").equals("Alphabetical"))
		{
			return commonName.compareToIgnoreCase(another.commonName);
		}
		else
		{
			if(System.currentTimeMillis() - timeOfLastLocationCheck > LOCATION_REFRESH_DELAY)
			{
				updateUserLocation();
				return compareTo(another);
			}
			else
			{
				return Double.compare(distance(), another.distance());
			}
		}
	}
	
	/**
	 * returns the distance between this and the user's current location - a simple distance formula calculation
	 * using GPS coordinates.
	 * @return Distance between the user and this Eatery
	 */
	private double distance()
	{
		double userLongitude = userLocation.getLongitude();
		double userLatitude = userLocation.getLatitude();
		//Distance formula
		return Math.sqrt(
				Math.pow(userLongitude - longitude, 2) +
				Math.pow(userLatitude - latitude, 2));
	}
	
	@Override
	public String toString()
	{
		return commonName;
	}
	
	/**
	 * Updates the location of the user using the most recently received provider. This does not force a new retrieval
	 * of the information, just grabs whichever one was retrieved most recently.
	 * 
	 * If the user location cannot be found, the sort options is reset to alphabetical and a Toast is displayed to the user
	 */
	public static void updateUserLocation()
	{
		LocationManager lm = (LocationManager)DiningList.getContext().getSystemService(Context.LOCATION_SERVICE);
		Location locOne = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location locTwo = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		userLocation = null;
		if(locOne == null)
		{
			if(locTwo != null)
			{
				userLocation = locTwo;
			}
		}
		else if(locTwo == null)
		{
			if(locOne != null)
			{
				userLocation = locOne;
			}
		}
		timeOfLastLocationCheck = System.currentTimeMillis();
		
		if(userLocation == null)
		{
			SharedPreferences.Editor prefEditor = prefs.edit();
			prefEditor.putString("sortOptionsKey", "Alphabetical");
			prefEditor.apply();
			Toast.makeText(DiningList.getContext(), "Couldn't find your location", Toast.LENGTH_SHORT).show();
		}
	}
}
