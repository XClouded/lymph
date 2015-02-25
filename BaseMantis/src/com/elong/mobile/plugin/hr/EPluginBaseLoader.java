package com.elong.mobile.plugin.hr;

import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_ACTIVITY_NAME;
import static com.elong.mobile.plugin.hr.EPluginRule.TAG;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;
import android.util.Log;

import com.elong.mobile.plugin.platform.EPluginLoadPlatform;
import com.elong.mobile.plugin.platform.EPluginLoadPlatform.EPluginPlatformWorker;

public class EPluginBaseLoader extends ClassLoader {

	// TODO may be use Vector better thread safe
	private Map<Integer, String[]> classIndex;

	public EPluginBaseLoader(ClassLoader parent) {
		super(parent);
		classIndex = new ConcurrentHashMap<Integer, String[]>();
	}

	public void registerClass(String pkg, String actName, int type) {
		if (classIndex.containsKey(type)) {
			classIndex.remove(type);
		}
		classIndex.put(type, new String[] { pkg, actName });
	}

	public String fetchPluginClassName(int type) {
		String name = "";
		switch (type) {
		case 9527:
			name = PLUGIN_ACTIVITY_NAME;
			break;

		default:
			break;
		}
		return name;
	}
	
	public int fetchPluginClassType(String className) {
		int index = -1;
		if (className.equals(PLUGIN_ACTIVITY_NAME)) {
			index = 9527;
		}
		return index;
	}
	
	public Class<?> loadClass(String className, boolean resolv)
			throws ClassNotFoundException {
		Log.i(TAG, "base loadClass: " + className);
		int type = fetchPluginClassType(className);
		if (type != -1) {
			String[] info = classIndex.get(type);
			String pkgName = info[0];
			String actName = info[1];
			String pluginName = fetchPluginClassName(type);
			EPluginPlatformWorker worker = EPluginLoadPlatform.getInstance()
					.getWorker(pkgName);

			if (worker != null) {
				try {
					// TODO add service application... class loader.
					if (PLUGIN_ACTIVITY_NAME.equals(pluginName)) {
						return worker.getPluginItem().getClassLoader()
								.loadActivityClass(actName);
					} else {
						return worker.getPluginItem().getClassLoader()
								.loadClass(actName);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return super.loadClass(className, resolv);
	}
}
