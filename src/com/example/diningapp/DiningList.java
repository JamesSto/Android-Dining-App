package com.example.diningapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

/**
 * Creates a list of all eateries from the MasterEateryList. Separated into 2 ListView tabs - dining halls and (cafes and stores)
 * Both are created at the same time and can be switched between at will
 * 
 * A tap on any ListView element will start another activity with information about that eatery
 * 
 * @author James Stoyell
 *
 */
public class DiningList extends Activity{
	
	/**
	 * Context of main activity allows other classes (namely MasterEateryList) to get info from assets
	 */
	private static Context diningListInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dining_list);
		diningListInstance = this;
		
		//Sets up the tab system - defaults to the diningList
		TabHost tabs = (TabHost)findViewById(R.id.tabhost); 
		tabs.setup(); 
		TabHost.TabSpec spec = tabs.newTabSpec("tag1"); 
		spec.setContent(R.id.diningList); 
		spec.setIndicator("Dining Halls"); 
		tabs.addTab(spec); 
		
		spec = tabs.newTabSpec("tag2"); 
		spec.setContent(R.id.cafeList); 
		spec.setIndicator("Cafes & Stores"); 
		tabs.addTab(spec);
		
		createSchedules();
		createDiningList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dining_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(this, Settings.class);
			startActivity(i);
		}
		else if(id == R.id.refresh_action)
		{
			if(PreferenceManager.getDefaultSharedPreferences(this).getString("sortOptionsKey", "Error").equals("Location"))
			{
				Cafe.updateUserLocation();
				Hall.updateUserLocation();
			}
			MasterEateryList.getInstance().sortMasterLists();
			createDiningList();
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Returns the Context of this Activity to be used externally
	 * @return A reference to "this" instance
	 */
	public static Context getContext()
	{
		return diningListInstance;
	}
	
	/**
	 * Populates the DiningList with the Dining Hall eateries
	 */
	public void createDiningList()
	{
		//This method is public so that it can be called once again if the list needs sorting (User prefs updated)
		
		final ArrayList<Hall> masterHallList = MasterEateryList.getInstance().getHallList(); //Gets list from singleton
		final ArrayList<Cafe> masterCafeList = MasterEateryList.getInstance().getCafeList();
		
		String[] names = new String[masterCafeList.size()];
		Integer[] imageIDs = new Integer[masterCafeList.size()];
		
		for(int i = 0; i < masterCafeList.size(); i++)
		{
			names[i] = masterCafeList.get(i).commonName;
			if(masterCafeList.get(i).paymentType.equals("BRB"))
			{
				imageIDs[i] = R.drawable.cornell_logo;
			}
			else
			{
				imageIDs[i] = R.drawable.cash;
			}
		}
		
		final DiningListAdapter cafeAdapter = new DiningListAdapter(this, names, imageIDs, new Boolean[masterCafeList.size()]);
		final ListView cafeList = (ListView) findViewById(R.id.cafeList);
		cafeList.setAdapter(cafeAdapter);
		
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( final Void ... params ) {
            	for(int i = 0; i < masterCafeList.size(); i++)
            	{
            		cafeAdapter.isOpen[i] = masterCafeList.get(i).isOpen();
            	}
                return null;
            }

            @Override
            protected void onPostExecute( final Void result ) {
            	cafeAdapter.notifyDataSetChanged();
            }
        }.execute();
		
		//Anonymous class that will take the user to the appropriate hall page on a tap
		cafeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Intent i = new Intent(DiningList.diningListInstance, CafeActivity.class);
        		String name = cafeList.getItemAtPosition(position).toString();
        		i.putExtra("cafeName", name);
        		startActivity(i);
            }
        });
		
		names = new String[masterHallList.size()];
		imageIDs = new Integer[masterHallList.size()];
		
		for(int i = 0; i < masterHallList.size(); i++)
		{
			names[i] = masterHallList.get(i).commonName;
			imageIDs[i] = R.drawable.silverware;
		}
		
		//This is the adapter that will populate the diningList
		final DiningListAdapter hallAdapter = new DiningListAdapter(this, names, imageIDs, new Boolean[masterHallList.size()]);
		final ListView diningList = (ListView) findViewById(R.id.diningList);
		diningList.setAdapter(hallAdapter);
		
		//This loop will start ~10 AsyncTasks, one for each dining hall, check to see if they're open one by one and change the status bar accordingly
		for(int i = 0; i < hallAdapter.isOpen.length; i++)
    	{
			final int hallIndex = i;
			new AsyncTask<Void, Void, Void>() {
	            @Override
	            protected Void doInBackground( final Void ... params ) {
	            	hallAdapter.isOpen[hallIndex] = masterHallList.get(hallIndex).hours.isOpen();
	                return null;
	            }
	
	            @Override
	            protected void onPostExecute( final Void result ) {
	            	hallAdapter.notifyDataSetChanged();
	            }
	        }.execute();
    	}
		
		//Anonymous class that will take the user to the appropriate hall page on a tap
		diningList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Intent i = new Intent(DiningList.diningListInstance, HallActivity.class);
        		String name = diningList.getItemAtPosition(position).toString();
        		i.putExtra("hallName", name);
        		startActivity(i);
            }
        });
	}
	
	public void createSchedules()
	{
		for(final Hall h : MasterEateryList.getInstance().getHallList())
    	{
        	h.hours = new HoursRequester(h);
    	}
	}
}
