package com.example.diningapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Settings class - stores, changes, manages, and listens for settings changes in the app
 * Takes appropriate action when settings are changed as well
 * @author James Stoyell
 *
 */

public class Settings extends Activity implements OnSharedPreferenceChangeListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Don't allow user to access the settings menu from the settings menu
		//So nothing in the action bar
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Back button
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * The settings fragment for this app
	 */
	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}
	
	/**
	 * Updates dining list when sort preference is changed
	 * Either updates the user location and resorts the list, or simply resorts the list alphabetically
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
		DiningList diningList = (DiningList) DiningList.getContext();
		Cafe.updateUserLocation();
		Hall.updateUserLocation();
		MasterEateryList.getInstance().sortMasterLists();
		diningList.createDiningList();
		
	}
}
