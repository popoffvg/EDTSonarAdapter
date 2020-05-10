package ru.popoffvg.sonaradapter;

import org.osgi.framework.Bundle;

import com._1c.g5.wiring.AbstractGuiceAwareExecutableExtensionFactory;
import com.google.inject.Injector;

/**
 * Guice module aware executable extension factory for UI plugin.
 *
 */
public class ExecutableExtensionFactory
    extends AbstractGuiceAwareExecutableExtensionFactory
{
    @Override
    protected Bundle getBundle()
    {
        return SonarAdapterPlugin.getDefault().getBundle();
    }

    @Override
    protected Injector getInjector()
    {
        return SonarAdapterPlugin.getDefault().getInjector();
    }
}
