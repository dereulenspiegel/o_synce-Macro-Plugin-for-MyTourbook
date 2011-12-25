package net.tourbook.device.osynce.macro;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String		groupTitleEraseDevice;

	public static String		prefPageEraseDeviceNote;

	public static String		prefPageEditorEraseDevice;

	public static String		errorMessageReadingDevice;

	public static String		errorMessageTempFileNotFound;

	public static String		errorMessageIOException;

	public static String		errorMessageFailedToReadRawData;

	private static final String	BUNDLE_NAME	= "net.tourbook.device.osynce.macro.messages";	//$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {}

}
