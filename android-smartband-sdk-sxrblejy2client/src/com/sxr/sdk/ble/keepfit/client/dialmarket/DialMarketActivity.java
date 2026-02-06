package com.sxr.sdk.ble.keepfit.client.dialmarket;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.sxr.sdk.ble.keepfit.client.CheckUtil;
import com.sxr.sdk.ble.keepfit.client.CommonAttributes;
import com.sxr.sdk.ble.keepfit.client.ImageUtil;
import com.sxr.sdk.ble.keepfit.client.R;
import com.sxr.sdk.ble.keepfit.client.SampleBleService;
import com.sxr.sdk.ble.keepfit.client.SysUtils;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class DialMarketActivity extends FragmentActivity {
	public String TAG = this.getClass().getSimpleName();
	private static final String DIALMARKET_PREFIX = "keepfit";
	public static final String dial_upgrade_json = "http://download.keeprapid.com/apps/smartband/" + DIALMARKET_PREFIX + "/dialinfo/%1$s/dail.json";

	public static final String P_FIRMWARE_PATH = "/JYouPro/firmware";
	public static final String P_IMAGE_DIAL_PATH = "/JYouPro/dailimgs";
	public static final String P_IMAGE_DIAL_EXT = ".pmg";

	public static final String ACTION_PREFIX = "keepfit";
	public static final String ACTION_NOTIFY_DEVICE_DIALINFO_CHANGED = ACTION_PREFIX + "DEVICE_DIALINFO_CHANGED";
	public static final String ACTION_NOTIFY_DEVICE_DIALINFO_CHANGED_TEST = ACTION_PREFIX + "ACTION_NOTIFY_DEVICE_DIALINFO_CHANGED_TEST";

	public static final String SYNC_PROCESSING = ACTION_PREFIX + ".SYNC_PROCESSING";

	// create a preference accessor. This is for global app preferences.
	public SharedPreferences sharedPreferences;

	class DownloadTaskInfo{
		ImageView imgDial;
		String fileName;
		String url;

		public DownloadTaskInfo(ImageView iv, String filename, String url) {
			this.imgDial = iv;
			this.fileName = filename;
			this.url = url;
		}
	}

	public class DialInfo  {

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		private String name;
		private String filename;
		private String image;
		private String md5String;

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		private String domain;

		public String getMd5String() {
			return md5String;
		}

		public void setMd5String(String md5String) {
			this.md5String = md5String;
		}
	}

	public class ChannelAdapter extends BaseAdapter {

		private ArrayList<DialInfo> dialList;
		private LayoutInflater layoutInflater;
		private Context activityContext;

		public ChannelAdapter(ArrayList<DialInfo> list, Context context){
			dialList = list;
			activityContext = context;
			layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return dialList.size();
		}

		@Override
		public Object getItem(int position) {
			return dialList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				//加载布局
				convertView =layoutInflater.inflate(R.layout.item_gridview_dial,null);

				holder = new ViewHolder();
				holder.imgDial = (ImageView)convertView.findViewById(R.id.dial_img);
				holder.decDial = (TextView)convertView.findViewById(R.id.dial_dec);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}

			//设置图标和文字
			DialInfo dialInfo = dialList.get(position);
			if(dialInfo != null){
				String name = dialInfo.getName();
				String md5String = dialInfo.getMd5String();
				String filename = dialInfo.getFilename();
				String imagefilename = dialInfo.getImage();
				String domain = dialInfo.getDomain();
				holder.decDial.setText(dialInfo.getName());

				String binPath = SysUtils.createSDCardDir(activityContext, P_FIRMWARE_PATH)
						+ "/" + filename;
				java.io.File file = new java.io.File(binPath);

				String imagePath = SysUtils.createSDCardDir(activityContext, P_IMAGE_DIAL_PATH)
						+ "/" + imagefilename + P_IMAGE_DIAL_EXT;
				java.io.File fileImg  = new java.io.File(imagePath);

				if (!fileImg.exists()) {
					holder.imgDial.setImageResource(R.mipmap.dial_default);
					new DownLoadImageTask(holder.imgDial, imagefilename + P_IMAGE_DIAL_EXT).execute(domain + imagefilename);
				}
				else {
					Bitmap bmpDial = BitmapFactory.decodeFile(imagePath);
					if (bmpDial != null)
						holder.imgDial.setImageBitmap(bmpDial);
				}
			}
			return convertView;
		}

		class ViewHolder{
			ImageView imgDial;
			TextView decDial;
		}
	}

	private class DownLoadImageTask extends AsyncTask<String,Void, Bitmap> {
		ImageView imageView;
		String filename;

		public DownLoadImageTask(ImageView imageView, String filename){
			this.imageView = imageView;
			this.filename = filename;
		}

		/*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
		protected Bitmap doInBackground(String...urls){
			Date now = new Date();
			String urlOfImage = urls[0] + "?t=" + String.valueOf(now.getTime());
			Bitmap logo = null;
			try{
				InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
				logo = BitmapFactory.decodeStream(is);
			}catch(Exception e){ // Catch the download exception
				e.printStackTrace();
			}
			return logo;
		}

		/*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
		protected void onPostExecute(Bitmap result){
			if(imageView != null) {
				imageView.setImageBitmap(result);
				String imagePath = SysUtils.createSDCardDir(getApplicationContext(), P_IMAGE_DIAL_PATH)
						+ "/" + filename;
				ImageUtil.saveBitmap(result, imagePath);
			}
		}
	}

	private GridView mGridView;  //九宫格
	private ArrayList<DialInfo> dialInfoList;
	private ChannelAdapter dailAdapter;
	private DialogLoading dl;
	private ArrayList<DownloadTaskInfo> downloadInfoList;
	protected String macid;
	protected String fwVer;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_market);

		initData();
		initView();
	}

	@Override
	protected void onDestroy() {
		saveLog("onDestroy");
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}

	private void initData() {
		sharedPreferences = getSharedPreferences("jy_sdk_prefs", Context.MODE_PRIVATE);
		// TODO:load current device product info in the fw
		// TODO:read local storage json file of dial market infomation
		// TODO:check server if expire in local storage file
		dl = new DialogLoading(this);
		downloadInfoList = new ArrayList<>();
		macid = sharedPreferences.getString(CommonAttributes.P_MAC_ID, CommonAttributes.P_MAC_ID_DEFAULT);
		fwVer = sharedPreferences.getString(CommonAttributes.FIRMWARE_VERSION + "-" + macid, CommonAttributes.FIRMWARE_VERSION_DEFAULT);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CommonAttributes.ACTION_NOTIFY_DIAL_JSON_CONTENT);
		registerReceiver(broadcastReceiver, intentFilter);

		String productType = sharedPreferences.getString(CommonAttributes.FUNCTION_DIAL_PRODUCTTYPE + "-" + macid, CommonAttributes.FUNCTION_DIAL_PRODUCTTYPE_DEFAULT);
		String productId = sharedPreferences.getString(CommonAttributes.FUNCTION_DIAL_PRODUCTID + "-" + macid, CommonAttributes.FUNCTION_DIAL_PRODUCTID_DEFAULT);
		checkDialServerInfo(productType + productId);
	}

    private void initView() {
		mGridView = (GridView)findViewById(R.id.gvDialMarket);
		dl.show();

//		GridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//				Intent intent = new Intent(DialMarketActivity.this, AlarmClockInfoActivity.class);
//				int id = (int) dialList.get(i).getAlarm_id();
//				intent.putExtra("id", id);
//				startActivity(intent);
//			}
//		});
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null)
				return;
			if (action.equalsIgnoreCase(CommonAttributes.ACTION_NOTIFY_DIAL_JSON_CONTENT)) {
				String content = intent.getStringExtra("content");
				Message msg = new Message();
				Bundle data = new Bundle();
				data.putString("result",content);
				msg.setData(data);
				checkDialServerInfoHandler.sendMessage(msg);
			}
		}
	};

	private Handler checkDialServerInfoHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Bundle data = msg.getData();
			String result = data.getString("result");
			saveLog("upgrade result:" + result);

			JSONObject resultObj;
			try {
				String hwCode = "";
				if(fwVer != null && fwVer.length() > 8) {
					hwCode = fwVer.substring(0, 8);
				}

				resultObj = new JSONObject(result);
				if(hwCode != null && hwCode.length() == 8) {
					try {
						if (resultObj.has(hwCode)) {
							JSONObject selfObj = resultObj.getJSONObject(hwCode);
							if (selfObj != null) {
								resultObj = selfObj;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				String domain = resultObj.getString("domain");
				JSONArray daillist = resultObj.getJSONArray("daillist");

				dialInfoList = new ArrayList<>();
				for(int i=0;i<daillist.length();i++){
					DialInfo dialInfo = new DialInfo();
					JSONObject dialInfoObj = (JSONObject) daillist.get(i);
					dialInfo.setName(dialInfoObj.getString("name"));
					dialInfo.setFilename(dialInfoObj.getString("filename"));
					dialInfo.setImage(dialInfoObj.getString("image"));
					dialInfo.setMd5String("" + dialInfoObj.getString("md5"));
					dialInfo.setDomain("" + domain);
					dialInfoList.add(dialInfo);
				}

				// save the server info

				dailAdapter = new ChannelAdapter(dialInfoList,DialMarketActivity.this);

				mGridView.setAdapter(new ChannelAdapter(dialInfoList,DialMarketActivity.this));
				//给九宫格设置监听器
				mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						int isSyncing = Integer.valueOf(sharedPreferences.getString(SYNC_PROCESSING, "0"));
						if (isSyncing == 1) {
							Toast.makeText(DialMarketActivity.this, getString(R.string.Device_Busy), Toast.LENGTH_SHORT).show();
							return;
						}

						Intent intent = new Intent();
						String filename = dialInfoList.get(position).getFilename();
						String imagefilename = dialInfoList.get(position).getImage();
						String dialname = dialInfoList.get(position).getName();
						String md5String = dialInfoList.get(position).getMd5String();
						String domain = dialInfoList.get(position).getDomain();

						saveLog("filename: " + filename);
						saveLog("md5String: " + md5String);
						saveLog("imagefilename: " + imagefilename);
						saveLog("domain: " + domain);

						intent.putExtra("filename", filename);
						intent.putExtra("md5String", md5String);
						intent.putExtra("imagefilename", imagefilename);
						intent.putExtra("dialname", dialname);
						intent.putExtra("domain", domain);
//						startActivity(intent);
						setResult(RESULT_OK, intent);
						finish();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				dl.dismiss();
			}

			return true;
		}
	});

	private void checkDialServerInfo(String fw) {
		try {
			Intent intent = new Intent(CommonAttributes.ACTION_REQUEST_DIAL_JSON_CONTENT);
			intent.putExtra("fw", fw);
			sendBroadcast(intent);
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return ;
	}

	public void saveLog(String content){
		Log.d(TAG, content);
		if(CheckUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
			SysUtils.writeTxtToFile(this, content, CommonAttributes.P_LOG_PATH, SysUtils.getDateString(new Date(), 0, 2) + "-demo.log");
	}

}