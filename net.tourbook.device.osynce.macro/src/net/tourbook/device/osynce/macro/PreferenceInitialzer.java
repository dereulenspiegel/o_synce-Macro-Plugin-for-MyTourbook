package net.tourbook.device.osynce.macro;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitialzer extends AbstractPreferenceInitializer {

	public PreferenceInitialzer() {}

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

		preferenceStore.setDefault(IPreferences.ERASE_DEVICE_AFTER_IMPORT, false);

	}

}
