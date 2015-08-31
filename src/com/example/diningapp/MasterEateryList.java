package com.example.diningapp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.io.*;


/**
 * A singleton that retrieves data from eatery_data.txt and stores it for use by various activities
 * @author James Stoyell
 *
 */
public class MasterEateryList {
	
	/**
	 * The list of Hall objects, scraped from the text file and used to populate the ListView in the DiningList activity
	 */
	private ArrayList<Hall> hallList = new ArrayList<Hall>();
	/**
	 * The list of Cafe objects, scraped from the text file and used to populate the ListView in the DiningList activity
	 */
	private ArrayList<Cafe> cafeList = new ArrayList<Cafe>();
	/**
	 * Singleton instance - shares information between activities
	 */
	private static MasterEateryList listInstance = new MasterEateryList();
	/**
	 * A conversion table between the common name of a hall and its corresponding object
	 */
	private static Hashtable<String, Hall> hallNames = new Hashtable<String, Hall>();
	/**
	 * A conversion table between the common name of a Cafe and its corresponding object
	 */
	private static Hashtable<String, Cafe> cafeNames = new Hashtable<String, Cafe>();
	
	static{
		try {
			BufferedReader eateryReader = new BufferedReader(new InputStreamReader(DiningList.getContext().getAssets().open("hall_data.txt")));
			String currentDatum = eateryReader.readLine();
			//When the file ends, we will have added all of the halls and the current line will be null
			while(currentDatum != null)
			{
				//Fill in variables for an eatery in proper order
				String officialName = currentDatum;
				String commonName = eateryReader.readLine();
				String description = eateryReader.readLine();
				String logoFieldName = eateryReader.readLine();
				String mapsQuery = eateryReader.readLine();
				double latitude = Double.parseDouble(eateryReader.readLine());
				double longitude = Double.parseDouble(eateryReader.readLine());
				int hallMenuCode = Integer.parseInt(eateryReader.readLine());
				String hallCalendarCode = eateryReader.readLine();
				
				//Add the eatery to the arraylist
				listInstance.hallList.add(new Hall(
									officialName, 
									commonName, 
									description, 
									(int)R.drawable.class.getDeclaredField(logoFieldName).get(Integer.class), 
									mapsQuery, 
									latitude, 
									longitude,
									hallMenuCode,
									hallCalendarCode));
				
				hallNames.put(commonName, listInstance.hallList.get(listInstance.hallList.size()-1));
				//Skip the divider line!
				currentDatum = eateryReader.readLine();
				currentDatum = eateryReader.readLine();
			}
			eateryReader.close();
			
			eateryReader = new BufferedReader(new InputStreamReader(DiningList.getContext().getAssets().open("cafe_data.txt")));
			currentDatum = eateryReader.readLine();
			while(currentDatum != null)
			{
				String paymentType = currentDatum;
				String officialName = eateryReader.readLine();
				String commonName = eateryReader.readLine();
				String description = eateryReader.readLine();
				String logoFieldName = eateryReader.readLine();
				String mapsQuery = eateryReader.readLine();
				double latitude = Double.parseDouble(eateryReader.readLine());
				double longitude = Double.parseDouble(eateryReader.readLine());
				String menu = eateryReader.readLine();
				String hours = eateryReader.readLine();

				listInstance.cafeList.add(new Cafe(paymentType,
													officialName,
													commonName,
													description,
													(int)R.drawable.class.getDeclaredField(logoFieldName).get(Integer.class), 
													mapsQuery,
													latitude,
													longitude,
													menu,
													hours));
				
				cafeNames.put(commonName, listInstance.cafeList.get(listInstance.cafeList.size()-1));
				
				//Skip the divider line
				currentDatum = eateryReader.readLine();
				currentDatum = eateryReader.readLine();
			}
			eateryReader.close();
			
		} catch (IllegalAccessException e) {
			System.err.println("ERROR COULDN'T FIND FIELD");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println("ERROR COULDN'T FIND FIELD");
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			System.err.println("ERROR COULDN'T FIND FIELD");
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			System.err.println("FILE NOT FOUND");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(listInstance.hallList);
	}
	
	/**
	 * 
	 * @return The singleton instance of this class
	 */
	public static MasterEateryList getInstance() {return listInstance;}
	
	/**
	 * This function is a constant time lookup of an eatery given a name
	 * @param name The name of the eatery to be found
	 * @return The eatery that the name corresponds to
	 */
	public static Hall findHall(String name)
	{
		return hallNames.get(name);
	}
	
	/**
	 * This function is a constant time lookup of a store given a name
	 * @param name The name of the store to be found
	 * @return The store that the name corresponds to
	 */
	public static Cafe findCafe(String name)
	{
		return cafeNames.get(name);
	}
	
	/**
	 * Sorts the master lists - may need to be done multiple times in the course of one run as the user changes the sort
	 * preferences from alphabetical to location or vice versa
	 */
	public void sortMasterLists()
	{
		Collections.sort(hallList);
		Collections.sort(cafeList);
	}
	
	/**
	 * Returns a copy of the masterHallList - does not return a reference to the list itself
	 * @return A copy of the masterHallList
	 */
	public ArrayList<Hall> getHallList()
	{
		//Do not pass by reference - no other class should modify this ArrayList
		return new ArrayList<Hall>(hallList);
	}
	
	/**
	 * Returns a copy of the masterCafeList - does not return a reference to the list itself
	 * @return A copy of the masterCafeList
	 */
	public ArrayList<Cafe> getCafeList()
	{
		//Do not pass by reference - no other class should modify this ArrayList
		return new ArrayList<Cafe>(cafeList);
	}

}
