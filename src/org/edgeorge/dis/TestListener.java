package org.edgeorge.dis;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;


//Test Listener

public class TestListener extends PhoneStateListener {

	private int GSMsig = 0;
	private int CDMAsig = 0;
	private boolean isGSMPhone;

	/* Get the Signal strength from the provider, each time there is an update */
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

			CDMAsig = signalStrength.getCdmaDbm();
			setGSMPhone(false);
		}


	}

	public int getGSMSig(){
		return GSMsig;
	}

	public int getCDMASig(){
		return CDMAsig;
	}


	public boolean isGSMPhone() {
		return isGSMPhone;
	}

	public void setGSMPhone(boolean isGSMPhone) {
		this.isGSMPhone = isGSMPhone;
	}
}
