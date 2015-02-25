package com.elong.mobile.test.main;

import static com.elong.mobile.plugin.hr.EPluginRule.TAG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.elong.mobile.basemantis.R;
import com.elong.mobile.plugin.hr.EPluginWorkerListener;
import com.elong.mobile.plugin.model.EPluginItem;
import com.elong.mobile.plugin.platform.EPluginLoadPlatform;
import com.elong.mobile.plugin.utils.FileUtil;

public class TestMainActivity extends Activity {

	EPluginLoadPlatform platform;

	Handler handler = new Handler();
	ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		platform = EPluginLoadPlatform.getInstance().initPlatform(this);
		initCache();
	}

	

	@Override
	public void onBackPressed() {
		System.exit(0);
	}



	@Override
	protected void onResume() {
		super.onResume();
		if (platform != null) {
			platform.onRefresh();
		}
	}



	private void initCache() {
		File f = getDir("plugin_cache_file", Context.MODE_PRIVATE);
		if (!f.exists()) {
			f.mkdirs();
		}
		try {
			String[] dirs = getAssets().list("");
			if (dirs != null) {
				pd = ProgressDialog.show(this, "", "logding...");
				for (String n : dirs) {
					Log.i(TAG, n);
					if (n.endsWith(".apk")) {
						InputStream is = getAssets().open(n);
						File tar = new File(f, n);
						if (!tar.exists()) {
							FileUtil.writeToFile1(is, tar);
						}
						is.close();
					}
					
				}
				pd.dismiss();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		initContent(f);
	}
	
	private void initContent(File dir) {
		pd = ProgressDialog.show(this, "", "logding...");
		platform.asyncDoHires(dir.getAbsolutePath(),
				new EPluginWorkerListener() {

					@Override
					public void onFinish() {
						handler.post(new Runnable() {

							@Override
							public void run() {
								pd.dismiss();
								initList();
							}
						});
					}
				});

	}

	private void initList() {
		ListView lv = (ListView) findViewById(R.id.main_list);
		int s = platform.getPlugins().size();
		final ArrayList<EPluginItem> list = new ArrayList<EPluginItem>(s);
		for (String key : platform.getPlugins().keySet()) {
			list.add(platform.getPlugins().get(key).getPluginItem());
		}
		PluginAdapter pa = new PluginAdapter(list);
		lv.setAdapter(pa);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				EPluginItem item = list.get(position);
				String pkg = item.getPackageName();
				String act = item.getMainActivity().activityInfo.name;
				intent.setComponent(new ComponentName(pkg, act));
				platform.startActivity(TestMainActivity.this, intent);
			}
		});
		lv.performItemClick(lv.getAdapter().getView(0, null, null), 0, 0);
	}

	private class PluginAdapter extends BaseAdapter {
		private ArrayList<EPluginItem> list_;

		public PluginAdapter(ArrayList<EPluginItem> list) {
			this.list_ = list;
		}

		@Override
		public int getCount() {
			return list_.size();
		}

		@Override
		public Object getItem(int position) {
			return list_.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View cv, ViewGroup parent) {
			if (cv == null) {
				TextView tv = new TextView(TestMainActivity.this);
				EPluginItem item = list_.get(position);
				Drawable drawable = item.getResources().getDrawable(
						item.getPackageInfo().applicationInfo.icon);
				tv.setTextSize(26.0f);
				tv.setTextColor(Color.BLACK);
				tv.setText("fuxk");
				drawable.setBounds(0, 0, 100, 100);
				tv.setGravity(Gravity.CENTER);
				tv.setCompoundDrawables(null, drawable, null, null);
				cv = tv;
			}
			return cv;
		}
	}
}
