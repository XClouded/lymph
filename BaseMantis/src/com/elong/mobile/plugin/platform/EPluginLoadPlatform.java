package com.elong.mobile.plugin.platform;

import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_DEX_DIR;
import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_DEX_NAME;
import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_DIR;
import static com.elong.mobile.plugin.hr.EPluginRule.PLUGIN_ODEX_NAME;
import static com.elong.mobile.plugin.hr.EPluginRule.TAG;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.elong.mobile.plugin.hr.EPluginBaseLoader;
import com.elong.mobile.plugin.hr.EPluginContextWrapper;
import com.elong.mobile.plugin.hr.EPluginException.Null;
import com.elong.mobile.plugin.hr.EPluginLoader;
import com.elong.mobile.plugin.hr.EPluginWorkerListener;
import com.elong.mobile.plugin.model.EPluginItem;
import com.elong.mobile.plugin.utils.EPluginManifestUtil;
import com.elong.mobile.plugin.utils.ReflectionUtils;

public class EPluginLoadPlatform {

	private Context initContext;

	private File pluginDepot, pluginDexDepot;

	private EPluginBaseLoader baseLoader;

	private Map<String, EPluginPlatformWorker> plugins;

	private static EPluginLoadPlatform instance = null;

	private static Object object = new Object();

	private boolean initing = false;

	//TODO modify remove singleton
	public static EPluginLoadPlatform getInstance() {
		synchronized (object) {
			if (instance == null) {
				instance = new EPluginLoadPlatform();
			}
			return instance;
		}
	}

	public EPluginLoadPlatform initPlatform(Context context) {
		Log.i(TAG, "plugin platform start create.");
		if (context == null) {
			throw new Null("context is null !");
		}
		plugins = new ConcurrentHashMap<String, EPluginLoadPlatform.EPluginPlatformWorker>();
		initContext = context;
		createDepot();
		attachClassLoader();
		return this;
	}

	private void createDepot() {
		Log.i(TAG, "plugin depot start create.");
		pluginDepot = initContext.getDir(PLUGIN_DIR, Context.MODE_PRIVATE);
		pluginDexDepot = initContext.getDir(PLUGIN_DEX_DIR,
				Context.MODE_PRIVATE);
		if (pluginDepot.exists()) {
			pluginDepot.delete();
		}
		pluginDepot.mkdirs();
		if (pluginDexDepot.exists()) {
			pluginDexDepot.delete();
		}
		pluginDexDepot.mkdirs();
//		if (!pluginDepot.exists()) {
//			pluginDepot.mkdirs();
//		}
//		if (!pluginDexDepot.exists()) {
//			pluginDexDepot.mkdirs();
//		}
		Log.i(TAG, "plugin depot finish create.");
	}

	private void attachClassLoader() {
		Log.i(TAG, "plugin start attach class loader.");
		try {
			Object mPackageInfo = ReflectionUtils.getFieldValue(initContext,
					"mBase.mPackageInfo", true);
			baseLoader = new EPluginBaseLoader(initContext.getClassLoader());
			ReflectionUtils.setFieldValue(mPackageInfo, "mClassLoader",
					baseLoader, true);
			Log.i(TAG, "plugin start attach class loader.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//TODO add extra logic
	public void onRefresh() {
		attachClassLoader();
	}
	
	public void asyncDoHires(final String dir,
			final EPluginWorkerListener listener) {
		if (initing) {
			Log.i(TAG, "plugins is loading");
			return;
		}
		Log.i(TAG, "start load plugins");
		initing = true;
		if (!plugins.isEmpty()) {
			for (String key : plugins.keySet()) {
				plugins.get(key).unloader();
			}
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				doHires(dir);
				initing = false;
				Log.i(TAG, "finish load plugins");
				if (listener != null) {
					listener.onFinish();
				}
			}
		}).start();
	}

	private void doHires(String dir) {
		File pDir = new File(dir);

		File[] ps = pDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO add extra type plugins.
				return filename.endsWith(".apk");
			}
		});

		if (ps != null) {
			for (File p : ps) {
				doHire(p.getAbsolutePath());
			}
		}
	}

	private void doHire(String path) {
		EPluginPlatformWorker worker = new EPluginPlatformWorker(this,
				initContext, path);
		worker.doWork();
		String pn = worker.getPluginItem().getPackageName();
		if (!plugins.containsKey(pn)) {
			plugins.put(pn, worker);
		}
	}

	public void doFire(String pkgName) {
		if (plugins.containsKey(pkgName)) {
			plugins.get(pkgName).unloader();
			plugins.remove(pkgName);
		}
	}

	public void startActivity(Context context, Intent intent) {
		// TODO add action...
		ComponentName cn = intent.getComponent();
		if (cn == null) {
			throw new Null("start activity name is null !");
		}
		String actName = cn.getClassName();
		String pkgName = cn.getPackageName();
		if (plugins.containsKey(pkgName)) {
			baseLoader.registerClass(pkgName, actName, 9527);
			String an = baseLoader.fetchPluginClassName(9527);
			cn = new ComponentName(initContext, an);
			intent.setComponent(cn);
			//TODO add startActivityFromResult
			context.startActivity(intent);
		}
	}

	public File getPluginDepot() {
		return pluginDepot;
	}

	public File getPluginDexDepot() {
		return pluginDexDepot;
	}

	public EPluginBaseLoader getPlugBaseLoader() {
		return baseLoader;
	}

	public EPluginPlatformWorker getWorker(String pkgName) {
		return plugins.get(pkgName);
	}

	public Map<String, EPluginPlatformWorker> getPlugins() {
		return  plugins;
	}
	
	public EPluginItem getEPluginItemByName(String name) {
		for (String key : plugins.keySet()) {
			EPluginItem item = plugins.get(key).getPluginItem();
			if (item.findActivityByClassName(name) != null) {
				return item;
			}
		}
		return null;
	}
	
	public class EPluginPlatformWorker {

		private EPluginLoadPlatform platform;
		private String path = "";

		private Context ctx;

		private EPluginItem item;

		private File dexPath;

		public EPluginPlatformWorker(EPluginLoadPlatform platform,
				Context context, String path) {
			this.platform = platform;
			this.path = path;
			this.ctx = context;
			item = new EPluginItem();
		}

		public void doWork() {
			File pluginApk = new File(path);
			if (!verifyPlugin(pluginApk)) {
				return;
			}
			try {
				item.setFilePath(path);
				pickManifest();
				pickAsset();
				pickContextWrapper();
				pickApplication();
				createLoader();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public EPluginItem getPluginItem() {
			return item;
		}

		private boolean verifyPlugin(File plugin) {
			// TODO verify file md5 for guarantee file only.
			return true;
		}

		private void pickManifest() throws Exception {
			EPluginManifestUtil.setManifestInfo(ctx, path, item);
			Log.i(TAG, "plugin pickManifest finish.");
		}

		private void pickAsset() throws Exception {
			AssetManager am = (AssetManager) AssetManager.class.newInstance();
			am.getClass().getMethod("addAssetPath", String.class)
					.invoke(am, path);
			item.setAssetManager(am);
			Resources ctxres = ctx.getResources();
			Resources res = new Resources(am, ctxres.getDisplayMetrics(),
					ctxres.getConfiguration());
			item.setResources(res);
			Log.i(TAG, "plugin pickAsset finish.");
		}

		private void pickContextWrapper() {
			EPluginContextWrapper ctxw = new EPluginContextWrapper(ctx, item);
			item.setAppWrapper(ctxw);
			Log.i(TAG, "plugin pickContextWrapper finish.");
		}

		private void pickApplication() throws Exception {
			String applicationName = item.getPackageInfo().applicationInfo.name;
			Log.i(TAG, "applicationName : " + applicationName);
			Application application = null;
			if (!TextUtils.isEmpty(applicationName)) {
				ClassLoader loader = item.getClassLoader();
				Class<?> applicationClass = loader.loadClass(applicationName);
				application = (Application) applicationClass.newInstance();
			} else {
				application = new Application();
			}

			Method attachMethod = android.app.Application.class
					.getDeclaredMethod("attach", Context.class);
			attachMethod.setAccessible(true);
			attachMethod.invoke(application, item.getAppWrapper());

			if (android.os.Build.VERSION.SDK_INT >= 14) {
				Application.class.getMethod("registerComponentCallbacks",
						Class.forName("android.content.ComponentCallbacks"))
						.invoke(ctx.getApplicationContext(), application);
			}

			item.setApplication(application);
			Log.i(TAG, "plugin pickApplication finish.");
		}

		private void createLoader() {
			String dexDir = item.getPackageName();
			dexPath = new File(platform.getPluginDexDepot(), dexDir);
			// TODO change logic
			if (dexPath.exists()) {
				dexPath.delete();
			}
			dexPath.mkdirs();
			String nativeLibPath = item.getPackageInfo().applicationInfo.nativeLibraryDir;
			EPluginLoader loader = new EPluginLoader(path,
					dexPath.getAbsolutePath(), nativeLibPath,
					platform.getPlugBaseLoader(), item);
			item.setClassLoader(loader);
			item.setDexPath(dexPath.getAbsolutePath());
			File pdp = new File(dexPath, PLUGIN_DEX_NAME);
			if (pdp.exists()) {
				pdp.delete();
			}
			pdp.mkdirs();
			item.setPluginDexPath(pdp.getAbsolutePath());
			
			File podp = new File(dexPath, PLUGIN_ODEX_NAME);
			if (podp.exists()) {
				podp.delete();
			}
			podp.mkdirs();
			item.setPluginOdexPath(podp.getAbsolutePath());
			
			Log.i(TAG, "plugin createLoader finish.");
		}

		private void unloader() {
			try {
				if (dexPath != null && dexPath.exists()) {
					dexPath.delete();
				}
				if (android.os.Build.VERSION.SDK_INT >= 14) {
					Application.class
							.getMethod(
									"unregisterComponentCallbacks",
									Class.forName("android.content.ComponentCallbacks"))
							.invoke(ctx.getApplicationContext(),
									item.getApplication());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}
