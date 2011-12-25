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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import de.akuz.osynce.macro.AbstractMacroSerialPortDevice;
import de.akuz.osynce.macro.CommunicationException;
import de.akuz.osynce.macro.MacroRXTXDevice;
import de.akuz.osynce.macro.interfaces.Macro;
import de.akuz.osynce.macro.interfaces.Training;

/**
 * This class handles the communication with the macro. Essentially an existing library is used.
 * Also all data will be parsed and interpreted by this class and then serialized to disk to be read
 * by the device data reader class
 * 
 * @author Till Klocke
 */
public class MacroExternalDeviceReader extends ExternalDevice {

	public final static String	FILE_EXTENSION	= "mac";
	
	private List<File>			_receivedFiles;

	private boolean				isCancelImport	= false;

	private List<Training>		trainings;

	private Macro				macro;

	private int					trainingsCount;

	private Map<String, String>		macroProperties;

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

				final String msg = NLS.bind(Messages.Import_Wizard_Monitor_task_msg, new Object[] {
						visibleName,
						portName,
						9600 });

				macro = new MacroRXTXDevice();
				macroProperties = new HashMap<String, String>();
				macroProperties.put(AbstractMacroSerialPortDevice.PROPERTY_PORTNAME, portName);
				macro.init(macroProperties);

				try {
					trainingsCount = macro.getTrainingsStartDates().size();
					monitor.beginTask(msg, trainingsCount);
					readDeviceData(monitor, portName);
					saveReceivedData();
				} catch (CommunicationException e) {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openError(
									Display.getCurrent().getActiveShell(),
									"Error",
									net.tourbook.device.osynce.macro.Messages.errorMessageReadingDevice);
						}
					});
				}

			}

		};
	}
	
	@Override
	public boolean isImportCanceled() {
		return isCancelImport;
	}

	/**
	 * Here we read the data from the device. We are each stored training seperately, so we are able
	 * to cancel the transfer and can show the user meaningful progress.
	 * 
	 * @param monitor
	 * @param portName
	 */
	private void readDeviceData(final IProgressMonitor monitor, final String portName) throws CommunicationException {
		trainings = new ArrayList<Training>(trainingsCount);
		if (!isCancelImport) {
			int i = 0;
			while (i < trainingsCount && !isCancelImport) {
				trainings.add(macro.getTraining(i));
				monitor.worked(1);
				i++;
			}
			if (prefStore.getBoolean(IPreferences.ERASE_DEVICE_AFTER_IMPORT)) {
				macro.erase();
			}
		}
	}

	/**
	 * This method serializes the read data to disk to be processed later
	 */
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
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(
							Display.getCurrent().getActiveShell(),
							"Error",
							net.tourbook.device.osynce.macro.Messages.errorMessageTempFileNotFound);
				}
			});
			e.printStackTrace();
		} catch (final IOException e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(
							Display.getCurrent().getActiveShell(),
							"Error",
							net.tourbook.device.osynce.macro.Messages.errorMessageIOException);
				}
			});
			e.printStackTrace();
		}
	}
}
