package ru.popoffvg.sonaradapter.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import com.google.inject.Inject;

import ru.popoffvg.sonaradapter.SonarAdapterPlugin;

public class SonarAdapterPropertyPage extends PropertyPage {

	private ArrayList<AbstractPropertyView> propertyElements = new ArrayList<>();

	private IPreferenceStore prefs;
	@Inject
	private SonarAdapterPlugin plugin;
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		prefs = getPreferenceStore();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		newBoolProperty(composite, SonarAdapterPlugin.LOAD_SONAR_ISSUES_PROPERTY)
			.title("Load Sonar issues")
			.build();
		
		newTextProperty(composite, SonarAdapterPlugin.SERVER_ID_PROPERTY)
			.title("Server adress")
			.mandatoryField()
			.build();
		
		newTextProperty(composite, SonarAdapterPlugin.PROJECT_ID_PROPERTY)
			.title("Project")
			.mandatoryField()
			.build();
		
		newTextProperty(composite, SonarAdapterPlugin.KEY_ID_PROPERTY)
			.title("Key")
			.mandatoryField()
			.build();
		
		newTextProperty(composite, SonarAdapterPlugin.EXCLUDES_ID_PROPERTY)
			.title("Excludes")
			.build();
		
		newTextProperty(composite, SonarAdapterPlugin.ASSIGNED_PROPERTY)
			.title("Assign")
			.build();
		
		return composite;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return plugin.getPreferenceStore((IProject) getElement());
	}

	BoolPropertyView newBoolProperty(Composite composite, String id) {
		BoolPropertyView property = new BoolPropertyView(prefs, composite, id);
		propertyElements.add(property);
		return property;
	}
	
	TextPropertyView newTextProperty(Composite composite, String id) {
		TextPropertyView property = new TextPropertyView(prefs, composite, id);
		propertyElements.add(property);
		return property;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		for (AbstractPropertyView value : propertyElements) {
			value.restoreDefault();
		}
	}
	
	@Override
	public boolean performOk() {
		for (AbstractPropertyView value : propertyElements) {
			value.save();
		}
		super.performOk();
		return true;
	}
	
	

}