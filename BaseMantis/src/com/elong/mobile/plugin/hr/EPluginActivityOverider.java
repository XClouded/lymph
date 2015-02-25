package com.elong.mobile.plugin.hr;

import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_ACTIVITY_NAME;
import static com.elong.mobile.plugin.hr.EPluginRule.TAG;

import java.io.File;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.elong.mobile.plugin.model.EPluginItem;
import com.elong.mobile.plugin.platform.EPluginLoadPlatform;
import com.elong.mobile.plugin.utils.ActivityGenerateUtil;
import com.elong.mobile.plugin.utils.EPluginDepotUtil;
import com.elong.mobile.plugin.utils.EPluginNameUtil;

/**
 * 提供公共方法供自动生成的Activity调用
 * 
 */
public class EPluginActivityOverider {

	// TODO add service logic

	// public static ComponentName overrideStartService(Activity fromAct,
	// String pluginId, Intent intent) {
	// // TODO 覆盖 StarService 方法
	// Log.i(TAG, "overrideStartService");
	// return fromAct.startService(intent);
	// }
	//
	// public static boolean overrideBindService(Activity fromAct,
	// String pluginId, Intent intent, ServiceConnection conn, int flags) {
	// // TODO overrideBindService
	// Log.i(TAG, "overrideBindService");
	// return fromAct.bindService(intent, conn, flags);
	// }
	//
	// public static void overrideUnbindService(Activity fromAct, String
	// pluginId,
	// ServiceConnection conn) {
	// // TODO overrideUnbindService
	// Log.i(TAG, "overrideUnbindService");
	// fromAct.unbindService(conn);
	// }
	//
	// public static boolean overrideStopService(Activity fromAct,
	// String pluginId, Intent intent) {
	// // TODO overrideStopService
	// Log.i(TAG, "overrideStopService");
	// return fromAct.stopService(intent);
	// }

	public static Intent overrideStartActivityForResult(Activity fromAct,
			String pluginId, Intent intent, int requestCode, Bundle options) {

		EPluginLoadPlatform platform = EPluginLoadPlatform.getInstance();

		if (intent.getComponent() != null
				&& intent.getComponent().getClassName() != null) {
			// action 为空，但是指定了包名和 activity类名
			ComponentName compname = intent.getComponent();

			String toActName = compname.getClassName();

			EPluginItem thisPlugin = platform.getEPluginItemByName(toActName);
			String pkg = thisPlugin.getPackageName();
			Log.i(TAG, "plugin is start pkg=" + pkg + " act=" + toActName);
			ActivityInfo actInThisApk = null;
			EPluginItem plug = thisPlugin;
			if (pkg != null) {
				if (pkg.equals(thisPlugin.getPackageName())) {
					actInThisApk = thisPlugin
							.findActivityByClassName(toActName);
				} else {
					EPluginItem otherPlugin = platform.getWorker(pkg)
							.getPluginItem();
					if (otherPlugin != null) {
						plug = otherPlugin;
						actInThisApk = otherPlugin
								.findActivityByClassName(toActName);
					}
				}
			} else {
				actInThisApk = thisPlugin.findActivityByClassName(toActName);
			}
			if (actInThisApk != null) {
				setPluginIntent(fromAct, intent, plug, pkg, actInThisApk.name);
			} else {
				// TODO 不支持无包名跨插件跳转，容易有同名Activity问题。
			}
		} else if (intent.getAction() != null) {
			String action = intent.getAction();
			// TODO add deal with action.
		}

		return intent;
	}

	private static void setPluginIntent(Context context, Intent intent,
			EPluginItem plugin, String pkg, String actName) {
		createProxyDex(plugin, actName);

		EPluginBaseLoader loader = EPluginLoadPlatform.getInstance()
				.getPlugBaseLoader();
		loader.registerClass(pkg, actName, 9527);
		String act = loader.fetchPluginClassName(9527);
		ComponentName compname = new ComponentName(context, act);
		intent.setComponent(compname);
	}

	static File createProxyDex(EPluginItem plugin, String activity) {
		return createProxyDex(plugin, activity, true);
	}

	static File createProxyDex(EPluginItem item, String activity, boolean lazy) {
		File savePath = EPluginDepotUtil
				.getProxyActivityDexPath(item, activity);
		createProxyDex(item, activity, savePath, lazy);
		return savePath;
	}

	private static void createProxyDex(EPluginItem item, String activity,
			File saveDir, boolean lazy) {
		Log.i(TAG + ":createProxyDex", "plugin=" + item + "\n, activity="
				+ activity);
		if (lazy && saveDir.exists()) {
			// Log.i(TAG, "dex alreay exists: " + saveDir);
			// 已经存在就不创建了，直接返回
			return;
		}
		try {
			String pkgName = item.getPackageName();
			ActivityGenerateUtil.createActivityDex(activity,
					PLUGIN_ACTIVITY_NAME, saveDir, pkgName);
		} catch (Throwable e) {
			Log.i(TAG, e.toString());
		}
	}

	public static Object[] overrideAttachBaseContext(final String pluginId,
			final Activity fromAct, Context base) {

		Log.i(TAG, "overrideAttachBaseContext: pluginId=" + pluginId
				+ ", activity=" + fromAct.getClass().getSuperclass().getName());
		EPluginItem item = EPluginLoadPlatform.getInstance().getEPluginItemByName(pluginId);
		if (item != null) {
			// TODO check application is null.
			EPluginContextWrapper actWrapper = new EPluginContextWrapper(base,
					item);
			return new Object[] { actWrapper, item.getAssetManager() };
		}
		return null;
	}

	private static void changeActivityInfo(Context activity, String pluginId) {
		final String actName = activity.getClass().getSuperclass().getName();
		Log.i(TAG, "changeActivityInfo: activity = " + activity + ", class = "
				+ actName);
		if (!activity.getClass().getName().equals(PLUGIN_ACTIVITY_NAME)) {
			Log.i(TAG, "not a Proxy Activity ,then return.");
			return;
		}
		Field field_mActivityInfo = null;
		try {
			field_mActivityInfo = Activity.class
					.getDeclaredField("mActivityInfo");
			field_mActivityInfo.setAccessible(true);
		} catch (Exception e) {
			Log.i(TAG, e.toString());
			return;
		}
		EPluginItem item = EPluginLoadPlatform.getInstance().getEPluginItemByName(pluginId);
		if (item != null) {
			ActivityInfo actInfo = item.findActivityByClassName(actName);
			actInfo.applicationInfo = item.getPackageInfo().applicationInfo;
			try {
				field_mActivityInfo.set(activity, actInfo);
			} catch (Exception e) {
				Log.i(TAG, e.toString());
			}
			Log.i(TAG,
					"changeActivityInfo->changeTheme: " + " theme = "
							+ actInfo.getThemeResource() + ", icon = "
							+ actInfo.getIconResource() + ", logo = "
							+ actInfo.logo);
		}
	}

	public static int getPlugActivityTheme(Activity fromAct, String pluginId) {

		EPluginItem item = EPluginLoadPlatform.getInstance().getEPluginItemByName(pluginId);
		if (item != null) {
			String actName = fromAct.getClass().getSuperclass().getName();
			ActivityInfo actInfo = item.findActivityByClassName(actName);
			int rs = actInfo.getThemeResource();
			Log.i(TAG, "getPlugActivityTheme: theme=" + rs + ", actName=" + actName);
			changeActivityInfo(fromAct, pluginId);
			return rs;
		}
		return View.NO_ID;
	}

	/**
	 * 按下back键的方法调用
	 * 
	 * @param pluginId
	 * @param fromAct
	 * @return 是否调用父类的onBackPressed()方法
	 */
	public static boolean overrideOnbackPressed(Activity fromAct,
			String pluginId) {
		EPluginItem item = EPluginLoadPlatform.getInstance().getEPluginItemByName(pluginId);
		if (item != null) {
			String actName = fromAct.getClass().getSuperclass().getName();
			ActivityInfo actInfo = item.findActivityByClassName(actName);
			// TODO add back logic.
			// boolean finish = item.isFinishActivityOnbackPressed(actInfo);
			// if (finish) {
			// fromAct.finish();
			// }
			// boolean ivsuper = plinfo.isInvokeSuperOnbackPressed(actInfo);
			// Log.i(TAG, "finish? " + finish + ", ivsuper? " + ivsuper);
			return true;
		}
		return false;
	}

	//
	// =================== Activity 生命周期回调方法 ==================
	//
	public static void callback_onCreate(final String pluginId, Activity fromAct) {
		Log.i(TAG, "callback_onCreate(act="
				+ fromAct.getClass().getSuperclass().getName() + ", window="
				+ fromAct.getWindow() + ")");
		EPluginItem item = EPluginLoadPlatform.getInstance().getEPluginItemByName(pluginId);
		if (item == null) {
			return;
		}
		// replace Application
		try {
			Field applicationField = Activity.class
					.getDeclaredField("mApplication");
			applicationField.setAccessible(true);
			applicationField.set(fromAct, item.getApplication());
		} catch (Exception e) {
			e.printStackTrace();
		}
		{

			String actName = fromAct.getClass().getSuperclass().getName();
			ActivityInfo actInfo = item.findActivityByClassName(actName);
			int resTheme = actInfo.getThemeResource();
			if (resTheme != 0) {
				boolean hasNotSetTheme = true;
				try {
					Field mTheme = ContextThemeWrapper.class
							.getDeclaredField("mTheme");
					mTheme.setAccessible(true);
					hasNotSetTheme = mTheme.get(fromAct) == null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (hasNotSetTheme) {
					changeActivityInfo(fromAct, pluginId);
					fromAct.setTheme(resTheme);
				}
			}
		}

		// TODO add extra logic
		// if (android.os.Build.MODEL.equals("GT-I9500")) {
		// Window window = fromAct.getWindow();// 得到 PhoneWindow 实例
		// try {
		// Object origInf = ReflectionUtils.getFieldValue(window,
		// "mLayoutInflater");
		// if (!(origInf instanceof LayoutInflaterWrapper)) {
		// ReflectionUtils.setFieldValue(
		// window,
		// "mLayoutInflater",
		// new LayoutInflaterWrapper(window
		// .getLayoutInflater()));
		// }
		// } catch (Exception e) {
		// Log.e(TAG, Log.getStackTraceString(e));
		// }
		// }
		// TODO invoke callback is nothing.
		// EPluginActivityLifeCycleCallback callback =
		// EPluginLoadPlatform.getInstance()
		// .getPluginActivityLifeCycleCallback();
		// if (callback != null) {
		// callback.onCreate(pluginId, fromAct);
		// }
	}

	// TODO ============= invoke callback is nothing.
	public static void callback_onResume(String pluginId, Activity fromAct) {
	}

	public static void callback_onStart(String pluginId, Activity fromAct) {
	}

	public static void callback_onRestart(String pluginId, Activity fromAct) {
	}

	public static void callback_onPause(String pluginId, Activity fromAct) {
	}

	public static void callback_onStop(String pluginId, Activity fromAct) {
	}

	public static void callback_onDestroy(String pluginId, Activity fromAct) {
	}
}
