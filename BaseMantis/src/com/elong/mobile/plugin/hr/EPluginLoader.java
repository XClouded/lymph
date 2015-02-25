package com.elong.mobile.plugin.hr;

import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_ACTIVITY_NAME;
import static com.elong.mobile.plugin.hr.EPluginRule.TAG;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

import com.elong.mobile.plugin.model.EPluginItem;
import com.elong.mobile.plugin.utils.ActivityGenerateUtil;
import com.elong.mobile.plugin.utils.EPluginDepotUtil;

import dalvik.system.DexClassLoader;

public class EPluginLoader extends DexClassLoader {

	private Map<String, EPluginActivityLoader> activities;

	private EPluginItem item;

	public EPluginLoader(String dexPath, String optimizedDirectory,
			String libraryPath, ClassLoader parent, EPluginItem item) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		activities = new ConcurrentHashMap<String, EPluginActivityLoader>();
		this.item = item;
	}

	public Class<?> loadActivityClass(String name)
			throws ClassNotFoundException {
		Log.i(TAG, "load activity class is " + name);
		DexClassLoader clsLoader = activities.get(name);
		if (clsLoader == null) {
			File saveTo = EPluginDepotUtil.getProxyActivityDexPath(item, name);
			try {
				ActivityGenerateUtil.createActivityDex(name,
						PLUGIN_ACTIVITY_NAME, saveTo, item.getPackageName());
				clsLoader = new EPluginActivityLoader(saveTo.getAbsolutePath(),
						EPluginDepotUtil.getProxyActivityODexPath(item)
								.getAbsolutePath(),
						item.getPackageInfo().applicationInfo.nativeLibraryDir,
						this, item);
				activities.put(name, (EPluginActivityLoader) clsLoader);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return clsLoader.loadClass(PLUGIN_ACTIVITY_NAME);
	}

	public Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			Class<?> c = findLoadedClass(name);
			if (c == null) {
				if (name.startsWith("android.support.")) {
					try {
						c = findClass(name);
					} catch (ClassNotFoundException e) {
					}
					if (c == null) {
						c = findByParent(name, true);
					}
				} else {
					c = findByParent(name, false);
					if (c == null) {
						c = findClass(name);
					}
				}
			}
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
	}

	private Class<?> findByParent(String name, boolean throwEx)
			throws ClassNotFoundException {
		Class<?> c = null;
		try {
			ClassLoader parent = getParent();
			if (parent != null) {
				if (parent.getClass().isInstance(EPluginBaseLoader.class)) {
					parent = parent.getParent();
				}
				if (parent != null) {
					c = parent.loadClass(name);
				}
			}
		} catch (ClassNotFoundException e) {
			if (throwEx) {
				throw e;
			}
		}
		return c;
	}

	public Object getClassLoadingLock(String name) {
		return name.hashCode();
	}
}
