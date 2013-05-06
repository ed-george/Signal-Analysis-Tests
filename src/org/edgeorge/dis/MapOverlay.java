package org.edgeorge.dis;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

//MapOverlay Class
public class MapOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mapOverlay = new ArrayList<OverlayItem>();
	private Context context;
	private int draw = R.drawable.pin; //default icon (Avoiding NullExceptions)

	public MapOverlay (Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}

	public MapOverlay(Drawable arg0) {
		//default constructor
		super(boundCenterBottom(arg0));

	}


	public void addOverlay(OverlayItem overlay) {
		//Add Overlay to list
		mapOverlay.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		//return Overlay at index i
		return mapOverlay.get(i);
	}

	@Override
	public int size() {
		//Number of overlays
		return mapOverlay.size();
	}


	@Override
	protected boolean onTap(int index) {
		//Handles tap on Overlay
		OverlayItem item = mapOverlay.get(index);
		//Create new Dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		//Set title
		dialog.setTitle(item.getTitle());
		//Set text
		dialog.setMessage(item.getSnippet());
		//Set icon
		dialog.setIcon(draw);
		//Show dialog
		dialog.show();
		return true;
	}

	public void setIconOverlay(int x){
		//Set dialog icon depending on signal strength
		if (x == 99){
			draw = R.drawable.map; 
		}else{
			if (x >= 0 && x <= 10){
				draw = R.drawable.low;
			}

			if (x >= 11 && x <= 21){
				draw = R.drawable.mid;
			}

			if(x >= 22){
				draw = R.drawable.full;
			}
		}

	}
}
