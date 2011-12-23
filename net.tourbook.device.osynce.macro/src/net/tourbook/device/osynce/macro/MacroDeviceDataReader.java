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
import net.tourbook.importdata.SerialParameters;
import net.tourbook.importdata.TourbookDevice;
import de.akuz.osynce.macro.interfaces.GraphElement;
import de.akuz.osynce.macro.interfaces.Training;

public class MacroDeviceDataReader extends TourbookDevice {

	private final static String	DEVICE_ID		= "o_synce_macro";
	private final static String	VISIBLE_NAME	= "o_synce Macro";

	private List<Training>	trainings;

	private final SimpleDateFormat	dateFormat		= new SimpleDateFormat("ddMMyyyy_HH_mm_ss");

	public MacroDeviceDataReader() {
		this.visibleName = VISIBLE_NAME;
		this.fileExtension = MacroExternalDeviceReader.FILE_EXTENSION;
	}

	@Override
	public String buildFileNameFromRawData(String rawDataFileName) {
		if (trainings == null || trainings.size() == 0) {
			trainings = deserializeList(rawDataFileName);
		}
		if (trainings.size() > 0) {
			StringBuilder builder = new StringBuilder();
			builder.append(dateFormat.format(trainings.get(0).getStartDate()));
			builder.append("_");
			builder.append(dateFormat.format(trainings.get(trainings.size() - 1).getStartDate()));
			builder.append(".");
			builder.append(MacroExternalDeviceReader.FILE_EXTENSION);
			return builder.toString();
		}

		return null;
	}

	private float calculateDistance(GraphElement g){
		int time = g.getDataRate();
		float speed = g.getSpeed();
		return time * (speed * 0.36f);
	}

	@Override
	public boolean checkStartSequence(int byteIndex, int newByte) {
		//  We can't check the start Sequence so we return always true
		return true;
	}

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
	public SerialParameters getPortParameters(String portName) {
		// Parameters are hardcoded in Macro lib
		return null;
	}

	@Override
	public int getStartSequenceSize() {
		return 0;
	}

	@Override
	public int getTransferDataSize() {
		// TODO Check if we need this, but we can't know this size ind advance for this type of device
		return -1;
	}

	@Override
	public boolean processDeviceData(	String filePath,
										DeviceData deviceData,
										HashMap<Long, TourData> newlyImportedTours,
										HashMap<Long, TourData> alreadyImportedTours) {
		if (trainings == null || trainings.isEmpty()) {
			trainings = deserializeList(filePath);
		}

		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
		SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
		SimpleDateFormat secondsFormat = new SimpleDateFormat("ss");
		Date currentDate = new Date(System.currentTimeMillis());

//		deviceData = new DeviceData();
		deviceData.importId = generateUniqueIdForImport();
		deviceData.transferDay = Short.parseShort(dayFormat.format(currentDate));
		deviceData.transferMonth = Short.parseShort(monthFormat.format(currentDate));
		deviceData.transferYear = Short.parseShort(yearFormat.format(currentDate));

		for (Training t : trainings) {
			TourData tourData = new TourData();
			tourData.setDeviceTimeInterval((short) t.getGraphElements().get(0).getDataRate());
			tourData.setTourImportFilePath(filePath);
			//TODO Determine Wheel Size
//			tourData.setDeviceWheel(t.getGraphElements());
//			tourData.setDeviceTotalUp(t.getAltimeterGain());
//			tourData.setDeviceTotalDown(t.getAltimeterLoss());
			tourData.setAvgCadence(t.getAverageCadence());
			tourData.setAvgPulse(t.getAverageHeartRate());
			tourData.setCalories(t.getKCals());
			tourData.setDeviceAvgSpeed(t.getAverageSpeed());
			tourData.setIsDistanceFromSensor(true);
			tourData.setTourDistance(t.getTripDistance() / 1000);
			//FIXME At the moment we don't differentiate between pre moving time and total training time
			tourData.setTourDrivingTime(t.getTrainingDuration());
			tourData.setTourRecordingTime(t.getTrainingDuration());
			tourData.setStartDay(Short.parseShort(dayFormat.format(t.getStartDate())));
			tourData.setStartMonth(Short.parseShort(monthFormat.format(t.getStartDate())));
			tourData.setStartYear(Short.parseShort(yearFormat.format(t.getStartDate())));
			tourData.setStartHour(Short.parseShort(hourFormat.format(t.getStartDate())));
			tourData.setStartMinute(Short.parseShort(minuteFormat.format(t.getStartDate())));
			tourData.setStartSecond(Short.parseShort(secondsFormat.format(t.getStartDate())));

			final ArrayList<TimeData> timeDataList = new ArrayList<TimeData>();

			int timeCounter = 1;
			GraphElement previousGraphElement = null;

			boolean isPulsePresent = false;
			boolean isPowerPresent = false;
			boolean isCadencePresent = false;

			for (GraphElement g : t.getAllGraphElements()) {
				TimeData timeData = new TimeData();
				timeData.time = g.getDataRate();
				timeData.relativeTime = (timeCounter * g.getDataRate());
				timeData.temperature = g.getTemperature();
				timeData.cadence = g.getCadence();
				if (g.getCadence() > 0) {
					isCadencePresent = true;
				}
				timeData.pulse = g.getHeartRate();
				if (g.getHeartRate() > 0) {
					isPulsePresent = true;
				}
				timeData.distance = calculateDistance(g);

				if (previousGraphElement == null) {
					timeData.altitude = g.getAltitude();
					timeData.absoluteDistance = timeData.distance;
				} else {
					timeData.altitude = previousGraphElement.getAltitude() - g.getAltitude();
					timeData.absoluteDistance = timeDataList.get(timeDataList.size() - 1).absoluteDistance
							+ timeData.distance;
				}

				timeData.absoluteAltitude = g.getAltitude();

//				timeData.absoluteDistance TODO Calculate distance
//				also calculate relative distance timeData.distance



				timeData.power = g.getPower();
				if (g.getPower() > 0) {
					isPowerPresent = true;
				}
				timeData.speed = g.getSpeed();

				timeDataList.add(timeData);

				previousGraphElement = g;

				timeCounter++;
			}

			final Long tourId = tourData.createTourId(Integer.toString((int) (Math.abs(tourData.getStartDistance()))));

			if (!alreadyImportedTours.containsKey(tourId)
					&& timeDataList.size() > 0) {
				if (!isPowerPresent || !isCadencePresent || !isPulsePresent) {
					for (TimeData d : timeDataList) {
						if (!isPowerPresent) {
							d.power = Float.MIN_VALUE;
						}
						if (!isCadencePresent) {
							d.cadence = Float.MIN_VALUE;
						}
						if (!isPulsePresent) {
							d.pulse = Float.MIN_VALUE;
						}
					}
				}
				newlyImportedTours.put(tourId, tourData);
				tourData.createTimeSeries(timeDataList, false);
				tourData.setTourType(null);
//				tourData.computeTourDrivingTime();
				tourData.computeComputedValues();
				tourData.setDeviceId(DEVICE_ID);
				tourData.setDeviceName(VISIBLE_NAME);
				//FIXME We could run into problems here since we aren't setting modenames
//				final Short profileId = Short.valueOf(tourData.getDeviceTourType(), 16);

//				tourData.setDeviceMode(profileId);
//				tourData.setDeviceModeName(getDeviceModeName(profileId));
				tourData.setWeek(tourData.getStartYear(), tourData.getStartMonth(), tourData.getStartDay());
			}

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
