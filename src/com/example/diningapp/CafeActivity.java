package com.example.diningapp;

import android.app.Activity;
import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * A cafe, not accepting meal swipes, has a static menu. This class formats an activity for a given cafe
 * with data from the MasterEateryList
 * @author James Stoyell
 *
 */
public class CafeActivity extends Activity {
	
	public Cafe place;
	public TextView menuView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cafe);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		String cafeName = getIntent().getStringExtra("cafeName");
		place = MasterEateryList.findCafe(cafeName);
		setTitle(place.commonName);
		((ImageView) findViewById(R.id.logo)).setImageResource(place.logo);
		((TextView) findViewById(R.id.description)).setText(place.description);
		((TextView) findViewById(R.id.cafeHours)).setText(Html.fromHtml(place.hours));
		((TextView) findViewById(R.id.cafeMenu)).setText(Html.fromHtml(place.menu));
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
	 * Opens Google Maps with a request for directions to this Cafe
	 * @param v The button that was clicked on
	 */
	public void openMaps(View v)
	{
		Uri mapsUri = Uri.parse(place.mapsQuery);
		Intent intent = new Intent(Intent.ACTION_VIEW, mapsUri);
		startActivity(intent);
	}
}
