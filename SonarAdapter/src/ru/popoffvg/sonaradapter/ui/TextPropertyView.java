package ru.popoffvg.sonaradapter.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TextPropertyView extends AbstractPropertyView {

	private Text textFiled;

	TextPropertyView(IPreferenceStore prefs, Composite composite, String id) {
		super(prefs, composite, id);
	}

	@Override
	public AbstractPropertyView build() {
		Label valueLabel = new Label(composite, SWT.NONE);
		String additionalSymbols = isMandatoryFiled ? "*:" : ":";
		valueLabel.setText(title + additionalSymbols);
		GridData gdLabel = new GridData();
		gdLabel.grabExcessHorizontalSpace = false;
		valueLabel.setLayoutData(gdLabel);

		textFiled = new Text(composite, SWT.BORDER);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		textFiled.setLayoutData(gd);

		textFiled.setText(prefs.getString(id));

		return this;
	}

	@Override
	public void restoreDefault() {
		textFiled.setText("");
	}

	@Override
	public void save() {
		prefs.setValue(id, textFiled.getText());
	}

}
