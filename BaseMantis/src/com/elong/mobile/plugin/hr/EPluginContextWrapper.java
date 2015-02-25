package com.elong.mobile.plugin.hr;

import java.io.File;

import com.elong.mobile.plugin.model.EPluginItem;
import com.elong.mobile.plugin.utils.EPluginDepotUtil;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import static com.elong.mobile.plugin.hr.EPluginRule.TAG;

;

public class EPluginContextWrapper extends ContextWrapper {

	private EPluginItem plugin;
	private ApplicationInfo applicationInfo;
	private File fileDir;

	public EPluginContextWrapper(Context base, EPluginItem plugin) {
		super(base);
		this.plugin = plugin;
		applicationInfo = new ApplicationInfo(super.getApplicationInfo());
		applicationInfo.sourceDir = plugin.getFilePath();
		applicationInfo.dataDir = plugin.getPluginDexPath();
		fileDir = EPluginDepotUtil.getPluginFileDir(plugin);
	}

	@Override
	public File getFilesDir() {
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		return fileDir;
	}

	@Override
	public String getPackageResourcePath() {
		Log.i(TAG, "getPackageResourcePath()");
		return super.getPackageResourcePath();
	}

	@Override
	public String getPackageCodePath() {
		Log.i(TAG, "getPackageCodePath()");
		return super.getPackageCodePath();
	}

	@Override
	public File getCacheDir() {
		Log.i(TAG, "getCacheDir()");
		return super.getCacheDir();
	}

	@Override
	public PackageManager getPackageManager() {
		Log.i(TAG, "PackageManager()");
		return super.getPackageManager();
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	@Override
	public Context getApplicationContext() {
		Log.i(TAG, "getApplicationContext()");
		return plugin.getApplication();
	}

	@Override
	public String getPackageName() {
		Log.i(TAG, "getPackageName()");
		return plugin.getPackageName();
	}

	@Override
	public Resources getResources() {
		Log.i(TAG, "getResources()");
		return plugin.getResources();
	}

	@Override
	public AssetManager getAssets() {
		Log.i(TAG, "getAssets()");
		return plugin.getAssetManager();
	}
	// @Override
	// public Object getSystemService(String name) {
	// if (name.equals(Context.ACTIVITY_SERVICE)) {
	// if (plugin.getApplicationInfo().process != null) {
	// return plugin.activityManager;
	// }
	// }
	// return super.getSystemService(name);
	// }
}
