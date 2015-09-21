package com.example.diningapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This ArrayAdapter is used to populate the ListViews in the DiningList.
 * @author James Stoyell
 *
 */
public class DiningListAdapter extends ArrayAdapter<String> {
	
	public String[] names;
	public Integer[] imageIDs;
	public Boolean[] isOpen;
	private Activity context;
	
	public DiningListAdapter(Activity context, String[] names, Integer[] imageIDs, Boolean[] isOpen)
	{
		super(context, R.layout.li, names);
		this.names = names;
		this.imageIDs = imageIDs;
		this.context = context;
		this.isOpen = isOpen;
	}
	
	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.li, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.nameView);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logoView);
		View statusBar = (View) rowView.findViewById(R.id.openBar);
		txtTitle.setText(names[position]);
		imageView.setImageResource(imageIDs[position]);
		if(isOpen[position] != null)
		{
			statusBar.setBackgroundColor(isOpen[position] ? context.getResources().getColor(R.color.green) : context.getResources().getColor(R.color.red));
		}
		else
		{
			statusBar.setBackgroundColor(context.getResources().getColor(R.color.gray));
		}
		return rowView;
	}

}