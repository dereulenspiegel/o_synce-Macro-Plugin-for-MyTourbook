package net.tourbook.device.osynce.macro;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Group	groupEraseDevice;
	private BooleanFieldEditor	eraseDevice;

	private final IPreferenceStore	preferenceStore	= Activator.getDefault().getPreferenceStore();

	@Override
	protected void createFieldEditors() {
		createUI();
		
	}

	private void createUI(){
		final Composite parent = getFieldEditorParent();
		GridLayoutFactory.fillDefaults().applyTo(parent);

		groupEraseDevice = new Group(parent, SWT.NONE);
		groupEraseDevice.setText(Messages.groupTitleEraseDevice);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupEraseDevice);
		final Label label = new Label(groupEraseDevice, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);
		label.setText(Messages.prefPageEraseDeviceNote);
		eraseDevice = new BooleanFieldEditor(
				IPreferences.ERASE_DEVICE_AFTER_IMPORT,
				Messages.prefPageEditorEraseDevice,
				groupEraseDevice);
		eraseDevice.fillIntoGrid(groupEraseDevice, 1);
		addField(eraseDevice);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(preferenceStore);
		
	}



}
