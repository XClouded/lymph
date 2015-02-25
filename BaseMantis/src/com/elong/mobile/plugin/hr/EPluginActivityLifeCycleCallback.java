package com.elong.mobile.plugin.hr;

import android.app.Activity;

/**
 * 插件Activity的生命周期方法回调
 * 
 */
public interface EPluginActivityLifeCycleCallback {

	public void onCreate(String pluginId, Activity pluginAct);

	public void onResume(String pluginId, Activity pluginAct);

	public void onPause(String pluginId, Activity pluginAct);

	public void onStart(String pluginId, Activity pluginAct);

	public void onRestart(String pluginId, Activity pluginAct);

	public void onStop(String pluginId, Activity pluginAct);

	public void onDestroy(String pluginId, Activity pluginAct);

	// public boolean onBackPressed(String pluginId, Activity pluginAct);
}
