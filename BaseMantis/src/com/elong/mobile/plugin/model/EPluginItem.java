package com.elong.mobile.plugin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elong.mobile.plugin.hr.EPluginContextWrapper;
import com.elong.mobile.plugin.hr.EPluginLoader;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * 
 */
public class EPluginItem {

	private String id;
	private String filePath;
	private String dexPath;
	private String pluginDexPath, pluginOdexPath;
	private PackageInfo packageInfo;
	private Map<String, ResolveInfo> activities;
	private ResolveInfo mainActivity;
	private List<ResolveInfo> services;
	private List<ResolveInfo> receivers;
	private List<ResolveInfo> providers;
	//
	private transient EPluginLoader classLoader;
	private transient Application application;
	private transient AssetManager assetManager;
	private transient Resources resources;

	private EPluginContextWrapper appWrapper;

	public String getPackageName() {
		return packageInfo.packageName;
	}

	public ActivityInfo findActivityByClassNameFromPkg(String actName) {
		if (packageInfo.activities == null) {
			return null;
		}
		for (ActivityInfo act : packageInfo.activities) {
			if (act.name.equals(actName)) {
				return act;
			}
		}
		return null;
	}

	public ActivityInfo findActivityByClassName(String actName) {
		if (packageInfo.activities == null) {
			return null;
		}
		ResolveInfo act = activities.get(actName);
		if (act == null) {
			return null;
		}
		return act.activityInfo;
	}

	public ActivityInfo findActivityByAction(String action) {
		if (activities == null || activities.isEmpty()) {
			return null;
		}
		for (ResolveInfo act : activities.values()) {
			if (act.filter != null && act.filter.hasAction(action)) {
				return act.activityInfo;
			}
		}
		return null;
	}

	public ActivityInfo findReceiverByClassName(String className) {
		if (packageInfo.receivers == null) {
			return null;
		}
		for (ActivityInfo receiver : packageInfo.receivers) {
			if (receiver.name.equals(className)) {
				return receiver;
			}
		}
		return null;

	}

	public ServiceInfo findServiceByClassName(String className) {
		if (packageInfo.services == null) {
			return null;
		}
		for (ServiceInfo service : packageInfo.services) {
			if (service.name.equals(className)) {
				return service;
			}
		}
		return null;

	}

	public ServiceInfo findServiceByAction(String action) {
		if (services == null || services.isEmpty()) {
			return null;
		}
		for (ResolveInfo ser : services) {
			if (ser.filter != null && ser.filter.hasAction(action)) {
				return ser.serviceInfo;
			}
		}
		return null;
	}

	public void addActivity(ResolveInfo activity) {
		if (activities == null) {
			activities = new HashMap<String, ResolveInfo>(20);
		}
		activities.put(activity.activityInfo.name, activity);
		if (mainActivity == null
				&& activity.filter != null
				&& activity.filter.hasAction("android.intent.action.MAIN")
				&& activity.filter
						.hasCategory("android.intent.category.LAUNCHER")) {
			mainActivity = activity;
		}
	}

	public void addReceiver(ResolveInfo receiver) {
		if (receivers == null) {
			receivers = new ArrayList<ResolveInfo>();
		}
		receivers.add(receiver);
	}

	public void addService(ResolveInfo service) {
		if (services == null) {
			services = new ArrayList<ResolveInfo>();
		}
		services.add(service);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public PackageInfo getPackageInfo() {
		return packageInfo;
	}

	public void setPackageInfo(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
		activities = new HashMap<String, ResolveInfo>(
				packageInfo.activities.length);
	}

	public EPluginLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(EPluginLoader classLoader) {
		this.classLoader = classLoader;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

	public Collection<ResolveInfo> getActivities() {
		if (activities == null) {
			return null;
		}
		return activities.values();
	}

	public List<ResolveInfo> getServices() {
		return services;
	}

	public void setServices(List<ResolveInfo> services) {
		this.services = services;
	}

	public List<ResolveInfo> getProviders() {
		return providers;
	}

	public void setProviders(List<ResolveInfo> providers) {
		this.providers = providers;
	}

	public ResolveInfo getMainActivity() {
		return mainActivity;
	}

	public List<ResolveInfo> getReceivers() {
		return receivers;
	}

	public String getDexPath() {
		return dexPath;
	}

	public void setDexPath(String dexPath) {
		this.dexPath = dexPath;
	}

	public String getPluginDexPath() {
		return pluginDexPath;
	}

	public void setPluginDexPath(String pluginDexPath) {
		this.pluginDexPath = pluginDexPath;
	}

	public String getPluginOdexPath() {
		return pluginOdexPath;
	}

	public void setPluginOdexPath(String pluginOdexPath) {
		this.pluginOdexPath = pluginOdexPath;
	}

	public EPluginContextWrapper getAppWrapper() {
		return appWrapper;
	}

	public void setAppWrapper(EPluginContextWrapper appWrapper) {
		this.appWrapper = appWrapper;
	}

	@Override
	public int hashCode() {
		return (id == null) ? super.hashCode() : id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EPluginItem other = (EPluginItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + "[ id=" + id + ", pkg=" + getPackageName()
				+ " ]";
	}
}
