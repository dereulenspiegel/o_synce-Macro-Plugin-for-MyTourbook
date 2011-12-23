package net.tourbook.device.osynce.macro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tourbook.Messages;
import net.tourbook.importdata.ExternalDevice;
import net.tourbook.importdata.RawDataManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;

import de.akuz.osynce.macro.AbstractMacroSerialPortDevice;
import de.akuz.osynce.macro.CommunicationException;
import de.akuz.osynce.macro.MacroRXTXDevice;
import de.akuz.osynce.macro.interfaces.Macro;
import de.akuz.osynce.macro.interfaces.Training;

public class MacroExternalDeviceReader extends ExternalDevice {

	public final static String	FILE_EXTENSION	= "mac";
	
	private List<File>			_receivedFiles;

	private boolean				isCancelImport	= false;

	public final static int		WORK_TO_DO		= 1;

	private List<Training>		trainings;

	private Macro				macro;

	private int					trainingsCount;

	private final IPreferenceStore	prefStore		= Activator.getDefault().getPreferenceStore();

	public void cancelImport() {
		isCancelImport = true;
	}
	
	@Override
	public IRunnableWithProgress createImportRunnable(final String portName, final List<File> receivedFiles) {
		_receivedFiles = receivedFiles;

		return new IRunnableWithProgress() {
			@Override
			public void run(final IProgressMonitor monitor) {

//				final SerialParameters portParameters = tourbookDevice.getPortParameters(portName);

//				if (portParameters == null) {
//					return;
//				}

				final String msg = NLS.bind(Messages.Import_Wizard_Monitor_task_msg, new Object[] {
						visibleName,
						portName,
						9600 });

				macro = new MacroRXTXDevice();
				Map<String, String> properties = new HashMap<String, String>();
				properties.put(AbstractMacroSerialPortDevice.PROPERTY_PORTNAME, portName);
				macro.init(properties);


				try {
					trainingsCount = macro.getTrainingsStartDates().size();
					monitor.beginTask(msg, trainingsCount);
					readDeviceData(monitor, portName);
					saveReceivedData();
				} catch (CommunicationException e) {
					e.printStackTrace();
				}

			}

		};
	}
	
	@Override
	public boolean isImportCanceled() {
		return isCancelImport;
	}

	private void readDeviceData(final IProgressMonitor monitor, final String portName) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(AbstractMacroSerialPortDevice.PROPERTY_PORTNAME, portName);
//		macro.init(properties);
		trainings = new ArrayList<Training>(trainingsCount);
		if (!isCancelImport) {
			try {
				for (int i = 0; i < trainingsCount; i++) {
//					macro.init(properties);
					trainings.add(macro.getTraining(i));
					monitor.worked(1);
				}

				if (prefStore.getBoolean(IPreferences.ERASE_DEVICE_AFTER_IMPORT)) {
					macro.erase();
				}
			} catch (CommunicationException e) {
				e.printStackTrace();

			} finally {
				//monitor.worked(WORK_TO_DO);
			}
		}
	}

	private void saveReceivedData() {
		try {

			final File tempFile = File.createTempFile("trainings", "." + FILE_EXTENSION, //$NON-NLS-1$ //$NON-NLS-2$
					new File(RawDataManager.getTempDir()));

			final FileOutputStream fileStream = new FileOutputStream(tempFile);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileStream);
			objectOut.writeObject(trainings);
			fileStream.close();

			_receivedFiles.add(tempFile);

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
