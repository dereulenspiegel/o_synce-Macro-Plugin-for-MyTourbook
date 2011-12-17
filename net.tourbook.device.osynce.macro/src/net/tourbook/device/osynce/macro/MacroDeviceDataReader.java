package net.tourbook.device.osynce.macro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.tourbook.data.TimeData;
import net.tourbook.data.TourData;
import net.tourbook.importdata.DeviceData;
import net.tourbook.importdata.IRawDataReader;
import de.akuz.osynce.macro.interfaces.GraphElement;
import de.akuz.osynce.macro.interfaces.Training;

public class MacroDeviceDataReader implements IRawDataReader {

	private final static String	DEVICE_ID		= "osynce_macro";
	private final static String	VISIBLE_NAME	= "osynce Macro";

	private List<Training>	trainings;

	@SuppressWarnings("unchecked")
	private List<Training> deserializeList(String filePath){
		ObjectInputStream ois = null;
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			ois = new ObjectInputStream(fis);
			Object in = ois.readObject();
			List<Training> list = (List<Training>) in;
			return list;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private long generateUniqueIdForImport() {
		return getLongFromDate(new Date(System.currentTimeMillis()));
	}

	@Override
	public String getDeviceModeName(int modeId) {
		// TODO Check if this is relevant
		return null;
	}

	private long getLongFromDate(Date date){
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateString = format.format(date);
		return Long.parseLong(dateString);
	}

	@Override
	public int getTransferDataSize() {
		// TODO Check if we need this, but we can't know this size ind advance for this type of device
		return 0;
	}

	@Override
	public boolean processDeviceData(	String filePath,
										DeviceData deviceData,
										HashMap<Long, TourData> newlyImportedTours,
										HashMap<Long, TourData> alreadyImportedTours) {
		if (trainings == null || trainings.isEmpty()) {
			trainings = deserializeList(filePath);
		}
		if(deviceData != null){
			System.out.println("DeviceData is NOT NULL!!!!");
		}
		if (newlyImportedTours != null) {
			System.out.println("TourDataMap is NOT NULL!!!");
		}
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
		Date currentDate = new Date(System.currentTimeMillis());

		deviceData = new DeviceData();
		deviceData.importId = generateUniqueIdForImport();
		deviceData.transferDay = Short.parseShort(dayFormat.format(currentDate));
		deviceData.transferMonth = Short.parseShort(monthFormat.format(currentDate));
		deviceData.transferYear = Short.parseShort(yearFormat.format(currentDate));

		for (Training t : trainings) {
			TourData tourData = new TourData();
			tourData.setDeviceTimeInterval((short) t.getGraphElements().get(0).getDataRate());
			tourData.importRawDataFile = filePath;
			tourData.setTourImportFilePath(filePath);
			//TODO Determine Wheel Size
//			tourData.setDeviceWheel(t.getGraphElements());
//			tourData.setDeviceTotalUp(t.getAltimeterGain());
//			tourData.setDeviceTotalDown(t.getAltimeterLoss());
			//TODO set startYear

			final ArrayList<TimeData> timeDataList = new ArrayList<TimeData>();
			
			long timeCounter = 1;
			GraphElement previousGraphElement = null;

			for (GraphElement g : t.getAllGraphElements()) {
				TimeData timeData = new TimeData();
				timeData.absoluteAltitude = g.getAltitude();
				timeData.absoluteDistance = t.getTripDistance();
				timeData.absoluteTime = g.getDataRate() * timeCounter;
				timeData.absoluteTime = t.getStartDate().getTime() + g.getDataRate() * timeCounter;
				if (previousGraphElement != null) {
					timeData.altitude = previousGraphElement.getAltitude() - g.getAltitude();
				} else {
					timeData.altitude = g.getAltitude();
				}
				timeData.cadence = g.getCadence();
				//TODO calculate Distance
				timeData.distance = Integer.MIN_VALUE;
				timeData.gpxDistance = Float.MIN_VALUE;
				timeData.latitude = Double.MIN_VALUE;
				timeData.longitude = Double.MIN_VALUE;
				timeData.power = g.getPower();
				timeData.pulse = g.getHeartRate();
				timeData.relativeTime = (int) (g.getDataRate() * timeCounter);
				timeData.speed = (int) (g.getSpeed() * 10);
				timeData.temperature = (int) (g.getTemperature());
				timeData.time = g.getDataRate();

				timeDataList.add(timeData);

				previousGraphElement = g;

				timeCounter++;
			}

			final Long tourId = tourData.createTourId(Integer.toString((int) (Math.abs(tourData.getStartDistance()))));

			if (!alreadyImportedTours.containsKey(tourId)
					&& newlyImportedTours.containsKey(tourId)
					&& timeDataList.size() > 0) {
				newlyImportedTours.put(tourId, tourData);
				tourData.createTimeSeries(timeDataList, false);
				tourData.setTourType(null);
				tourData.computeTourDrivingTime();
				tourData.computeComputedValues();
				tourData.setDeviceId(DEVICE_ID);
				tourData.setDeviceName(VISIBLE_NAME);
				//FIXME We could run into problems here since we aren't setting modenames
				final Short profileId = Short.valueOf(tourData.getDeviceTourType(), 16);

				tourData.setDeviceMode(profileId);
				tourData.setDeviceModeName(getDeviceModeName(profileId));
				tourData.setWeek(tourData.getStartYear(), tourData.getStartMonth(), tourData.getStartDay());
			}

		}

		if (newlyImportedTours == null) {
			newlyImportedTours = new HashMap<Long, TourData>();
		}

		return true;
	}

	@Override
	public boolean validateRawData(String filePath) {
		File tempFile = new File(filePath);
		boolean validated = true;
		if (!tempFile.exists()) {
			validated = false;
		}
		if (tempFile.length() == 0) {
			validated = false;
		}
		return validated;
	}

}
