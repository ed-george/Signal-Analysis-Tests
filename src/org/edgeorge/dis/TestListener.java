package org.edgeorge.dis;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;


//Test Listener

public class TestListener extends PhoneStateListener {

	private int GSMsig = 0;
	private int CDMAsig = 0;
	private boolean isGSMPhone;

	//Signal strength when there is an update
	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength)
	{
		super.onSignalStrengthsChanged(signalStrength);


		if (signalStrength.isGsm()) {
			//convert to dBm
			//http://www.etsi.org/deliver/etsi_ts/127000_127099/127007/08.05.00_60/ts_127007v080500p.pdf
			//GSMsig = signalStrength.getGsmSignalStrength() * 2 - 113;
			GSMsig = signalStrength.getGsmSignalStrength();
			setGSMPhone(true); 

		} else {

			//convert to ETSI standard as dBm conversion is completed later
			CDMAsig = (signalStrength.getCdmaDbm() + 113) / 2;
			setGSMPhone(false);
		}


	}

	public int getGSMSig(){
		//Getter for GSM
		return GSMsig;
	}

	public int getCDMASig(){
		//Getter for CDMA
		return CDMAsig;
	}


	public boolean isGSMPhone() {
		//Getter for isGSMPhone
		return isGSMPhone;
	}

	public void setGSMPhone(boolean isGSMPhone) {
		//Setter for isGSMPhone
		this.isGSMPhone = isGSMPhone;
	}
}
