package ru.popoffvg.sonaradapter.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class BoolPropertyView extends AbstractPropertyView {

	boolean flag = false;
	Button checkBox;

	public BoolPropertyView(IPreferenceStore prefs, Composite composite, String id) {
		super(prefs, composite, id);
		flag = prefs.getBoolean(id);
	}

	@Override
	AbstractPropertyView build() {

		checkBox = new Button(composite, SWT.CHECK);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		checkBox.setLayoutData(gd);
		checkBox.setSelection(flag);
		checkBox.setText(title);

		checkBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				flag = btn.getSelection();
			}

		});

		return this;
	}

	@Override
	void restoreDefault() {
		flag = false;
		checkBox.setSelection(flag);
	}

	@Override
	void save() {
		prefs.setValue(id, flag);
	}

}
