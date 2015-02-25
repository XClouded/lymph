package com.elong.mobile.plugin.hr;

import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_ACTIVITY_NAME;

import com.elong.mobile.plugin.model.EPluginItem;

import dalvik.system.DexClassLoader;


public class EPluginActivityLoader extends DexClassLoader {

	public EPluginActivityLoader(String dexPath, String optimizedDirectory,
			String libraryPath, ClassLoader parent, EPluginItem item) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
	}

	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (name.equals(PLUGIN_ACTIVITY_NAME)) {
			Class<?> c = findLoadedClass(name);
			if (c == null) {
				c = findClass(name);
			}
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
		return super.loadClass(name, resolve);
	}
}
