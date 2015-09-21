package com.example.diningapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Creates a Hall, an All You Care To Eat eatery from data in the MasterEateryList
 * 
 * This class populates static items (logo, description, containers) in the same thread, but uses AsyncTasks to
 * get and set items that need to be grabbed online (menus and schedules)
 * @author James Stoyell
 *
 */
public class HallActivity extends Activity {
	
	/**
	 * The All You Care To Eat dining hall that this activity represents
	 */
	public Hall place;
	/**
	 * The view containg the breakfast menu
	 */
	public TextView breakfastMenuView;
	/**
	 * The view containing the lunch menu
	 */
	public TextView lunchMenuView;
	/**
	 * The view containing the dinner menu
	 */
	public TextView dinnerMenuView;
	/**
	 * The view containing the breakfast hours
	 */
	public TextView breakfastTime;
	/**
	 * The view containing the lunch hours
	 */
	public TextView lunchTime;
	/**
	 * THe view containing the dinner hours
	 */
	public TextView dinnerTime;
	
	/**
	 * The loading animation for the breakfast hours
	 */
	public ProgressBar bLoad;
	/**
	 * The loading animation for the lunch hours
	 */
	public ProgressBar lLoad;
	/**
	 * The loading bar for the dinner hours
	 */
	public ProgressBar dLoad;
	/**
	 * The context of this activity, used to make changes to views in this activity from other classes
	 */
	private static Context c;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hall);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		c = this;
		
		//The menu for each meal time is hidden, to be revealed if clicked on
		hideExpandos();
		
		String hallName = getIntent().getStringExtra("hallName");
		place = MasterEateryList.findHall(hallName);
		setTitle(place.commonName);
		
		((ImageView) findViewById(R.id.logo)).setImageResource(place.logo);
		((TextView) findViewById(R.id.description)).setText(place.description);
		
		breakfastTime = (TextView) findViewById(R.id.breakfastTime);
		lunchTime = (TextView) findViewById(R.id.lunchTime);
		dinnerTime = (TextView) findViewById(R.id.dinnerTime);
		
		bLoad = (ProgressBar) findViewById(R.id.bLoad);
		lLoad = (ProgressBar) findViewById(R.id.lLoad);
		dLoad = (ProgressBar) findViewById(R.id.dLoad);
		
		place.hours.hall = this;
		//Any given AsyncTask can only be executed once, so a new class must be created each time a new activity is opened
		place.hours = new HoursRequester(place.hours, this);
		place.hours.execute();
		
		getAndSetMenu("Breakfast", breakfastMenuView);
		getAndSetMenu("Lunch", lunchMenuView);
		getAndSetMenu("Dinner", dinnerMenuView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hall_one, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(this, Settings.class);
			startActivity(i);
		}
		else if (id == android.R.id.home)
		{
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Listens for a click on the "Take me there!" button and opens Google Maps to guide the user
	 * @param v - Not really needed, but required as a parameter for an onClick listener
	 */
	public void openMaps(View v)
	{
		Uri mapsUri = Uri.parse(place.mapsQuery);
		Intent intent = new Intent(Intent.ACTION_VIEW, mapsUri);
		startActivity(intent);
	}
	
	/**
	 * Hides the menu TextViews initially, to be revealed when the user clicks on the appropriate menu
	 */
	private void hideExpandos()
	{
		breakfastMenuView = (TextView) findViewById(R.id.breakfastMenu);
		breakfastMenuView.setVisibility(View.GONE);

		lunchMenuView = (TextView) findViewById(R.id.lunchMenu);
		lunchMenuView.setVisibility(View.GONE);

		dinnerMenuView = (TextView) findViewById(R.id.dinnerMenu);
		dinnerMenuView.setVisibility(View.GONE);
	}
	
	/**
	 * An onClick listener that either expands or hides a menu when a header is tapped.
	 *  Also rotates the arrow next to each header
	 * @param v The View that was clicked on, to be used to determine what menu to show
	 */
	public void toggle_contents(View v)
	{
		if(v == findViewById(R.id.breakfastLayout))
		{
			((ImageView)findViewById(R.id.bExpandArrow)).setRotation(breakfastMenuView.isShown() ? 0 : 90);
			breakfastMenuView.setVisibility(breakfastMenuView.isShown() ? View.GONE : View.VISIBLE );
		}
		else if(v == findViewById(R.id.lunchLayout))
		{
			((ImageView)findViewById(R.id.lExpandArrow)).setRotation(lunchMenuView.isShown() ? 0 : 90);
			lunchMenuView.setVisibility(lunchMenuView.isShown() ? View.GONE : View.VISIBLE );
		}
		else if(v == findViewById(R.id.dinnerLayout))
		{
			((ImageView)findViewById(R.id.dExpandArrow)).setRotation(dinnerMenuView.isShown() ? 0 : 90);
			dinnerMenuView.setVisibility(dinnerMenuView.isShown() ? View.GONE : View.VISIBLE );
		}
	}
	
	/**
	 * Uses an AsyncTask to get menu information and put it into the appropriate menu TextViews
	 * @param meal The meal to get menu information for
	 * @param menu The menu to put that information into
	 */
	@SuppressLint("SimpleDateFormat")
	private void getAndSetMenu(String meal, TextView menu)
	{
		//This is the required date format for the POST request to Cornell servers
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d");
		String dateString = dateFormat.format(new Date());
		
		MenuRequester menuRequest = new MenuRequester(dateString, meal, place, menu);
		menuRequest.execute();
	}
	
	/**
	 * Gives this Activity's instance for use in other classes
	 * @return the currently open instance of this activity
	 */
	public static Context getContext()
	{
		return c;
	}
}
