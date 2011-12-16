package net.tourbook.device.osynce.macro;

import java.util.HashMap;

import net.tourbook.data.TourData;
import net.tourbook.importdata.DeviceData;
import net.tourbook.importdata.SerialParameters;
import net.tourbook.importdata.TourbookDevice;

public class MacroDeviceReader extends TourbookDevice {

	@Override
	public String buildFileNameFromRawData(String rawDataFileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkStartSequence(int byteIndex, int newByte) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDeviceModeName(int modeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SerialParameters getPortParameters(String portName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStartSequenceSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTransferDataSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean processDeviceData(String filePath, DeviceData deviceData, HashMap<Long, TourData> tourDataMap) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validateRawData(String filePath) {
		// TODO Auto-generated method stub
		return false;
	}

}
