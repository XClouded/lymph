package com.elong.mobile.plugin.utils;

import java.io.File;

import com.elong.mobile.plugin.model.EPluginItem;

public class EPluginDepotUtil {

	public static File getProxyActivityDexPath(EPluginItem item, String activity) {
		File folder = new File(item.getPluginDexPath(), "activities");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String suffix = ".dex";
		if (android.os.Build.VERSION.SDK_INT < 11) {
			suffix = ".jar";
		}
		File savePath = new File(folder, activity + suffix);
		return savePath;
	}

	public static File getProxyActivityODexPath(EPluginItem item) {
		File folder = new File(item.getPluginOdexPath(), "activities");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	public static File getPluginLibDir(EPluginItem item) {
		File folder = new File(item.getPluginDexPath(), "libs");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	public static File getPluginFileDir(EPluginItem item) {
		File folder = new File(item.getPluginDexPath(), "files");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
}
