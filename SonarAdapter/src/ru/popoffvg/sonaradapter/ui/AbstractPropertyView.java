package ru.popoffvg.sonaradapter.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

abstract class AbstractPropertyView{

	protected final Composite composite;
	protected final String id;
	protected boolean isMandatoryFiled = false;
	protected String title = "";
	protected final IPreferenceStore prefs;
	
	AbstractPropertyView(IPreferenceStore prefs, Composite composite, String id){
		this.prefs = prefs;
		this.composite = composite;
		this.id = id;
	}
	
	abstract AbstractPropertyView build();
	
	AbstractPropertyView mandatoryField() {
		isMandatoryFiled = true;
		return this;
	}
	
	abstract void restoreDefault();

	abstract void save();
	
	AbstractPropertyView title(String title) {
		this.title = title;
		return this;
	}
	
}