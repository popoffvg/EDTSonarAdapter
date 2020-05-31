package ru.popoffvg.sonaradapter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SonarAdapterPlugin extends AbstractUIPlugin {

	private static BundleContext context;
	private static SonarAdapterPlugin plugin;
	public static final String ID = "ru.popoffvg.sonaradapter"; //$NON-NLS-1$
	public static final String SERVER_ID_PROPERTY = "ru.popoffvg.sonaradapter.server"; //$NON-NLS-1$
	public static final String EXCLUDES_ID_PROPERTY = "ru.popoffvg.sonaradapter.excludes"; //$NON-NLS-1$
	public static final String KEY_ID_PROPERTY = "ru.popoffvg.sonaradapter.key"; //$NON-NLS-1$
	public static final String PROJECT_ID_PROPERTY = "ru.popoffvg.sonaradapter.project"; //$NON-NLS-1$
	public static final String LOAD_SONAR_ISSUES_PROPERTY = "ru.popoffvg.sonaradapter.loadIssues"; //$NON-NLS-1$
	public static final String ASSIGNED_PROPERTY = "ru.popoffvg.sonaradapter.assign"; //$NON-NLS-1$
	
	private Injector injector;
	
	static BundleContext getContext() {
		return context;
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SonarAdapterPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns Guice injector of the plugin
	 *
	 * @return Guice injector of the plugin, never <code>null</code> if plugin is
	 *         started
	 */
	public synchronized Injector getInjector() {
		if (injector == null) 
			injector = createInjector();
		
		return injector;
	}
	
	/**
	 * Writes a status to the plugin log.
	 *
	 * @param status status to log, cannot be <code>null</code>
	 */
	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	/**
	 * Writes a throwable to the plugin log as error status.
	 *
	 * @param throwable throwable, cannot be <code>null</code>
	 */
	public static void logError(Throwable throwable) {
		log(createErrorStatus(throwable.getMessage(), throwable));
	}

	/**
	 * Creates error status by a given message and cause throwable.
	 *
	 * @param message   status message, cannot be <code>null</code>
	 * @param throwable throwable, can be <code>null</code> if not applicable
	 * @return status created error status, never <code>null</code>
	 */
	public static IStatus createErrorStatus(String message, Throwable throwable) {
		return new Status(IStatus.ERROR, ID, 0, message, throwable);
	}

	/**
	 * Creates warning status by a given message.
	 *
	 * @param message status message, cannot be <code>null</code>
	 * @return status created warning status, never <code>null</code>
	 */
	public static IStatus createWarningStatus(String message) {
		return new Status(IStatus.WARNING, ID, 0, message, null);
	}

	/**
	 * Creates warning status by a given message and cause throwable.
	 *
	 * @param message   status message, cannot be <code>null</code>
	 * @param throwable throwable, can be <code>null</code> if not applicable
	 * @return status created warning status, never <code>null</code>
	 */
	public static IStatus createWarningStatus(final String message, Exception throwable) {
		return new Status(IStatus.WARNING, ID, 0, message, throwable);
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		super.stop(bundleContext);
	}
	
	private Injector createInjector() {
		try {
			return Guice.createInjector(new ExternalDependenciesModule(this));
		} catch (Exception e) {
			log(createErrorStatus("Failed to create injector for " //$NON-NLS-1$
					+ getBundle().getSymbolicName(), e));
			throw new RuntimeException("Failed to create injector for " //$NON-NLS-1$
					+ getBundle().getSymbolicName(), e);
		}
	}

	public IPreferenceStore getPreferenceStore(IProject project) {
		ProjectScope projectScope = new ProjectScope(project);
		ScopedPreferenceStore store = new ScopedPreferenceStore(projectScope, ID);
		store.setSearchContexts(new IScopeContext[] { projectScope, InstanceScope.INSTANCE });
		return store;
	}
	
}
