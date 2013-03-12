package org.edgeorge.dis;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MapOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mapOverlay = new ArrayList<OverlayItem>();
	private Context mContext;
	private int draw = R.drawable.pin; //default icon (Avoiding NullExceptions)

	public MapOverlay (Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	public MapOverlay(Drawable arg0) {
		super(boundCenterBottom(arg0));

	}


	public void addOverlay(OverlayItem overlay) {
		mapOverlay.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mapOverlay.get(i);
	}

	@Override
	public int size() {
		return mapOverlay.size();
	}


	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mapOverlay.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());

		dialog.setMessage(item.getSnippet());

		dialog.setIcon(draw);
		dialog.show();
		return true;
	}

	public void setIconOverlay(int x){

		if (x == 99){
			draw = R.drawable.map; //This will need to change
		}

		if (x >= 0 && x <= 10){
			draw = R.drawable.low;
		}

		if (x >= 11 && x <= 21){
			draw = R.drawable.mid;
		}

		if(x >= 22 && x <= 33){
			draw = R.drawable.full;
		}
	}
}
