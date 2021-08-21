package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static exportkit.xd.chooseBike_activity.getAlphaNumericString;

public class start_activity extends Activity {

	private static final int NOTIFICATION_ID = 121;
	private Button settingsBtn;
		private Button button;
		private Button sharedBtn, log, test, scanCode;
		private boolean shared;
		private View view;

		private AlertDialog.Builder dialogBuilder;
		private AlertDialog dialog, loading;


		private Button signout, cancel,go,ok;

		private SharedPreferences sp;

		private String res;
		private String url, url2, resultt;
		private ArrayList<String> numbers = new ArrayList<>();
		private String FetchedEmail,  FetchedBikeID="", random;
		private ArrayList<Bike> bikes = new ArrayList<>();

		private String QRcodeResult = "", nearestBikeName;

		private GeoPoint currentLocation = new GeoPoint(0.0,0.0);

		private HttpResponse response;
		private String[] arr = new String[3], myBike = new String[2], newBike = new String[2];

		private boolean kfaya = false;

		@Override
		public void onBackPressed() {
			//super.onBackPressed();
			signoutDialog();
		}

		public void signoutDialog() {
			dialogBuilder = new AlertDialog.Builder(this);

			final View popupView = getLayoutInflater().inflate(R.layout.signoutpopup, null);

			cancel = (Button) popupView.findViewById(R.id.cancel);
			signout = (Button) popupView.findViewById(R.id.signout);

			dialogBuilder.setView(popupView);
			dialog=dialogBuilder.create();

			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = 1000;
			lp.height = 800;
			dialog.show();
			dialog.getWindow().setAttributes(lp);
			dialog.show();

			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			signout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(start_activity.this, signin_activity.class));
				}
			});
		}

		public boolean internetIsConnected() {
			try {
				String command = "ping -c 1 google.com";
				return (Runtime.getRuntime().exec(command).waitFor() == 0);
			} catch (Exception e) {
				return false;
			}
		}

	public boolean isInternetAvailable() {
		try {
			InetAddress ipAddr = InetAddress.getByName("google.com");
			return !ipAddr.equals("");

		} catch (Exception e) {
			return false;
		}
	}

		@SuppressLint("WrongThread")
		public void onCreate(Bundle savedInstanceState) {

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			super.onCreate(savedInstanceState);
			if(isInternetAvailable()){

				startService();

				setContentView(R.layout.start);

				checkSystemWritePermission();

				sp = getSharedPreferences("session", Context.MODE_PRIVATE);
				FetchedEmail = sp.getString("email","");

				//mine getnums = new mine();
				//url = getResources().getString(R.string.ngrok)+"/users/gimmenums/"+FetchedEmail;
				//getnums.doInBackground();

				//String[] params = {fetchedEmail};

//				test = (Button) findViewById(R.id.test);
//				test.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						Toast.makeText(start_activity.this, numbers.get(0), Toast.LENGTH_LONG).show();
//
//					}
//				});

				settingsBtn = (Button) findViewById(R.id.settingsBtnn);
				button = (Button) findViewById(R.id.mybike);
				sharedBtn = (Button) findViewById(R.id.sharedBtn);
				log = (Button) findViewById(R.id.log);

				//sharedBtn.setEnabled(false);

				log.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(start_activity.this, rideSummary_activity.class);
						startActivity(intent);
					}
				});

				sharedBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(start_activity.this, choice_activity.class);
						shared = true;
						intent.putExtra("shared", shared);

						startActivity(intent);
					}
				});

				settingsBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(start_activity.this, settings_activity.class);

						intent.putExtra("back","start");
						startActivity(intent);
					}
				});

				button.setOnClickListener(new View.OnClickListener() {
					@RequiresApi(api = Build.VERSION_CODES.O)
					@SuppressLint("StaticFieldLeak")
					@Override
					public void onClick(View v) {
						//createNotificationChannel();
//						Log.d("checkmepls", "eh el el kalam");

						if (!kfaya) {
							try {
								//kfaya = true;

								//YourAsyncTask load = new YourAsyncTask(start_activity.this);
								//load.execute();

								url2 = getResources().getString(R.string.ngrok) + "/users/getuserbike/" + FetchedEmail;
								getUserBike ub = new getUserBike(start_activity.this);
								ub.execute();

								//loadingDialog();
								//executeSSHcommand();


								/*url2 = getResources().getString(R.string.ngrok) + "/users/getuserbike/" + FetchedEmail;

								getUserBike ub = new getUserBike();
								ub.doInBackground();*/

							} catch (Exception e) {
								e.printStackTrace();
							}
							//startActivity(intent);
						}
					}

				});
				//custom code goes here
			}else{

				setContentView(R.layout.nointernet);

				view = (View) findViewById(R.id.view);
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
						startActivity(getIntent());
					}
				});
				// Toast.makeText(this, "not working", Toast.LENGTH_SHORT).show();
			}
	}

		private void executeSSHcommand() {

			String user = "pi";
			String password = "raspberry";
			String host = "192.168.1.4";

			int port=22;

			try{

				JSch jsch = new JSch();
				Session session = jsch.getSession(user, host, port);
				session.setPassword(password);
				session.setConfig("StrictHostKeyChecking", "no");
				session.setTimeout(10000);
				session.connect();
				ChannelExec channel = (ChannelExec)session.openChannel("exec");
				channel.setCommand("cd /home/pi/Downloads; python3 start.py");
				channel.connect();

				Snackbar.make(this.findViewById(android.R.id.content),
						"Success!", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();

				channel.disconnect();

            /*
            URL oracle = new URL("192.168.1.4:8080");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            /*
            URL site = new URL("192.168.1.4:8080");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            site.openStream()));

            String inputLine;
            String res ="";
            while ((inputLine = in.readLine()) != null)
                res+=(inputLine);

            suc.setText(res);
            in.close();*/

			}
			catch(JSchException /*| MalformedURLException*/ e) {

				Snackbar.make(this.findViewById(android.R.id.content),
						"Check WIFI or Server! Error : " + e.getMessage(),
						Snackbar.LENGTH_LONG)
						.setDuration(20000).setAction("Action", null).show();
			} /*catch (IOException e) {
            e.printStackTrace();
        */
		}


        public void permissionDialog() {
            dialogBuilder = new AlertDialog.Builder(this);

            final View popupView = getLayoutInflater().inflate(R.layout.permissionpopup, null);

            go = (Button) popupView.findViewById(R.id.go);

            dialogBuilder.setView(popupView);
            dialog=dialogBuilder.create();

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = 1000;
            lp.height = 800;
            dialog.show();
            dialog.getWindow().setAttributes(lp);
            dialog.show();

            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAndroidPermissionsMenu();
                }
            });
        }

	private void createNotificationChannel() {

		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			CharSequence name = "lol";
			String description = "lol";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel("1", name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);


			Notification notification = new Notification.Builder(start_activity.this, "1").setSmallIcon(R.drawable.logo).setContentTitle("Cannot locate you..").setContentText("Please turn on your location").build();

			notificationManager.notify(1, notification);
		}
	}


	private boolean checkSystemWritePermission() {
            boolean retVal = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                retVal = Settings.System.canWrite(this);
                Log.d("TAG", "Can Write Settings: " + retVal);
                if(retVal){
                    ///Permission granted by the user
                   // openAndroidPermissionsMenu();
                }else{
                    permissionDialog();
                   // openAndroidPermissionsMenu();
                }
            }
            return retVal;
        }

        private void openAndroidPermissionsMenu() {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(intent);
        }

		public void recommendDialog() {
			dialogBuilder = new AlertDialog.Builder(this);

			final View popupView = getLayoutInflater().inflate(R.layout.recommend, null);

			ok = (Button) popupView.findViewById(R.id.ok);

			dialogBuilder.setView(popupView);
			dialog=dialogBuilder.create();

			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = 1000;
			lp.height = 800;
			dialog.show();
			dialog.getWindow().setAttributes(lp);
			dialog.show();

			ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}

	private class mine extends AsyncTask<String, Void, String> {

		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... strings) {
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(url).build();

			try(Response response = client.newCall(request).execute()){
				res = response.body().string();

				numbers.add("empty");
				String num = "";
				boolean isnum = false;

				for(int i=0; i<res.length(); i++){
					if(res.charAt(i)=='[')
						isnum = true;
					if(res.charAt(i)==','){
						numbers.add(num);
						num="";
					}
					if(res.charAt(i)==']'){
						isnum = false;
						numbers.add(num);
						num="";
						numbers.remove("empty");
						break;
					}
					if(isnum && res.charAt(i)!='"' && res.charAt(i)!='[' && res.charAt(i)!=']' && res.charAt(i)!=' ' &&res.charAt(i)!=',' ){
						//if(isnum){
						num+=res.charAt(i);
					}
				}

				onPostExecute("");
				Log.d("howw" ,numbers.size()+"");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			if(numbers.contains("empty")){
				recommendDialog();
			}
		}
	}




	private void createNotification(String contentTitle, String contentText,Context context) {

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		//Build the notification using Notification.Builder
		Notification.Builder builder = new Notification.Builder(start_activity.this)
				.setSmallIcon(android.R.drawable.btn_minus)
				.setAutoCancel(true)
				.setContentTitle(contentTitle)
				.setContentText(contentText);


		//Show the notification
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	private class Deg2UTM {
		double Easting;
		double Northing;
		int Zone;
		char Letter;

		public double getEasting() {
			return Easting;
		}
		public double getNorthing(){
			return Northing;
		}

		private  Deg2UTM(double Lat, double Lon)
		{
			Zone= (int) Math.floor(Lon/6+31);
			if (Lat<-72)
				Letter='C';
			else if (Lat<-64)
				Letter='D';
			else if (Lat<-56)
				Letter='E';
			else if (Lat<-48)
				Letter='F';
			else if (Lat<-40)
				Letter='G';
			else if (Lat<-32)
				Letter='H';
			else if (Lat<-24)
				Letter='J';
			else if (Lat<-16)
				Letter='K';
			else if (Lat<-8)
				Letter='L';
			else if (Lat<0)
				Letter='M';
			else if (Lat<8)
				Letter='N';
			else if (Lat<16)
				Letter='P';
			else if (Lat<24)
				Letter='Q';
			else if (Lat<32)
				Letter='R';
			else if (Lat<40)
				Letter='S';
			else if (Lat<48)
				Letter='T';
			else if (Lat<56)
				Letter='U';
			else if (Lat<64)
				Letter='V';
			else if (Lat<72)
				Letter='W';
			else
				Letter='X';
			Easting=0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180)))*0.9996*6399593.62/Math.pow((1+Math.pow(0.0820944379, 2)*Math.pow(Math.cos(Lat*Math.PI/180), 2)), 0.5)*(1+ Math.pow(0.0820944379,2)/2*Math.pow((0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2)/3)+500000;
			Easting=Math.round(Easting*100)*0.01;
			Northing = (Math.atan(Math.tan(Lat*Math.PI/180)/Math.cos((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))-Lat*Math.PI/180)*0.9996*6399593.625/Math.sqrt(1+0.006739496742*Math.pow(Math.cos(Lat*Math.PI/180),2))*(1+0.006739496742/2*Math.pow(0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))/(1-Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))+0.9996*6399593.625*(Lat*Math.PI/180-0.005054622556*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+4.258201531e-05*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4-1.674057895e-07*(5*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))/3);
			if (Letter<'M')
				Northing = Northing + 10000000;
			Northing=Math.round(Northing*100)*0.01;
		}
	}




	public double[] convert(int zone, double easting, double northing, boolean Northern){

        	if(!Northern){
				northing = 10000000 - northing;
			}

		long a = 6378137;
		double e = 0.081819191;
		double e1sq = 0.006739497;
		double k0 = 0.9996;

		double arc = northing / k0;

		double mu = arc / (a * (1 - Math.pow(e, 2) / 4.0 - 3 * Math.pow(e, 4) / 64.0 - 5 * Math.pow(e, 6) / 256.0));

		double ei = (1 - Math.pow((1 - e * e), (1 / 2.0))) / (1 + Math.pow((1 - e * e), (1 / 2.0)));

		double ca = 3 * ei / 2 - 27 * Math.pow(ei, 3) / 32.0;

		double cb = 21 * Math.pow(ei, 2) / 16 - 55 * Math.pow(ei, 4) / 32;
		double cc = 151 * Math.pow(ei, 3) / 96;
		double cd = 1097 * Math.pow(ei, 4) / 512;
		double phi1 = mu + ca * Math.sin(2 * mu) + cb * Math.sin(4 * mu) + cc * Math.sin(6 * mu) + cd * Math.sin(8 * mu);

		double n0 = a / Math.pow((1 - Math.pow((e * Math.sin(phi1)), 2)), (1 / 2.0));

		double r0 = a * (1 - e * e) / Math.pow((1 - Math.pow((e * Math.sin(phi1)), 2)), (3 / 2.0));
		double fact1 = n0 * Math.tan(phi1) / r0;

		double a1 = 500000 - easting;
		double dd0 = a1 / (n0 * k0);
		double fact2 = dd0 * dd0 / 2;

		double t0 = Math.pow(Math.tan(phi1), 2);
		double Q0 = e1sq * Math.pow(Math.cos(phi1), 2);
		double fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * e1sq) * Math.pow(dd0, 4) / 24;

		double fact4 = (61 + 90 * t0 + 298 * Q0 + 45 * t0 * t0 - 252 * e1sq - 3 * Q0 * Q0) * Math.pow(dd0, 6) / 720;

		double lof1 = a1 / (n0 * k0);
		double lof2 = (1 + 2 * t0 + Q0) * Math.pow(dd0, 3) / 6.0;
		double lof3 = (5 - 2 * Q0 + 28 * t0 - 3 * Math.pow(Q0, 2) + 8 * e1sq + 24 * Math.pow(t0, 2)) * Math.pow(dd0, 5) / 120;
		double _a2 = (lof1 - lof2 + lof3) / Math.cos(phi1);
		double _a3 = _a2 * 180 / Math.PI;

		double latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;

		if(!Northern)
			latitude = -latitude;

		double longitude;
		if(zone > 0){
			longitude = (6 * zone - 183.0) -_a3;
		}else{
			longitude = 3.0 - _a3;
		}
		double[] res = new double[2];
		res[0] = latitude;
		res[1] = longitude;

		return res;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (intentResult != null){
			if (intentResult.getContents() == null){
				Toast.makeText(start_activity.this, "Scanning failed\nTry again!", Toast.LENGTH_SHORT).show();
				//textView.setText("Cancelled");
			}else {
				QRcodeResult = intentResult.getContents();
				if(QRcodeResult.equals(myBike[1])){
					Toast.makeText(start_activity.this, "Bike linked to you!", Toast.LENGTH_SHORT).show();

					newBike[0] = FetchedEmail;
					newBike[1] = myBike[0];

					unlockBike ub = new unlockBike();
					ub.execute();

					updateBikeName ubn = new updateBikeName();
					ubn.execute();

				}else{
					Toast.makeText(start_activity.this, "QR code is incorrect!", Toast.LENGTH_SHORT).show();

					kfaya = false;
				}
				//textView.setText(intentResult.getContents());
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void scanDialog() {
		dialogBuilder = new AlertDialog.Builder(this);

		final View popupView = getLayoutInflater().inflate(R.layout.linkbikepopup, null);

		scanCode = (Button) popupView.findViewById(R.id.scanCode);

		dialogBuilder.setView(popupView);
		dialog=dialogBuilder.create();

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = 1000;
		lp.height = 800;
		dialog.show();
		dialog.getWindow().setAttributes(lp);
		dialog.show();

		scanCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator intentIntegrator = new IntentIntegrator(start_activity.this);
				intentIntegrator.setCaptureActivity(Capture.class);
				intentIntegrator.initiateScan();

				random = getAlphaNumericString(7);
				resultt = intentIntegrator.getCaptureActivity().toString();
				Toast.makeText(start_activity.this, resultt, Toast.LENGTH_LONG).show();
			}
		});
	}





	public class locateNearBikes extends AsyncTask<String, String, String> {
		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... strings) {
			try {

				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/NearestBikes");

				String East =arr[0];
				String North = arr[1];

				//Log.d("8ayar", "7agat east: "+East+" North "+North);

				//String Number = arr[2];
				String Number = "2";

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("East", East));
				nameValuePairs.add(new BasicNameValuePair("North", North));
				nameValuePairs.add(new BasicNameValuePair("Number", Number));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				response = httpclient.execute(httppost);

				InputStream IS = response.getEntity().getContent();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(IS));
				JSONTokener tokener = new JSONTokener(bufferedReader.readLine());
				JSONObject json = new JSONObject(tokener);

				Iterator x = json.keys();
				JSONArray jsonArray = new JSONArray();

				while (x.hasNext()){
					String key = (String) x.next();
					jsonArray.put(json.get(key));
				}


				JSONArray jo = new JSONArray(jsonArray.getString(0));

				//name of nearest bike! a5iran bgad!

				JSONObject jo2 = null;
				try {
					jo2 = jo.getJSONObject(0);
				}catch(Exception e){
					e.printStackTrace();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(start_activity.this, "No Bike Found!\nTry getting closer to YOUR bike", Toast.LENGTH_LONG).show();
						}
					});

				}
				if (jo2.get("Shared").equals("False") && jo2.get("Locked").equals("True")) {
					nearestBikeName = jo2.get("Name").toString();
					onPostExecute("");
				} else {
					//Toast.makeText(start_activity.this, "No Bike Found!\nTry getting closer to YOUR bike. LOL2", Toast.LENGTH_LONG).show();

					jo2 = (JSONObject) jo.get(1);
					if (jo2.get("Shared").equals("False") && jo2.get("Locked").equals("True")) {
						nearestBikeName = jo2.get("Name").toString();
						onPostExecute("");
					} else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(start_activity.this, "No Bike Found!\nTry getting EVEN closer to YOUR bike", Toast.LENGTH_LONG).show();
							}
						});
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			myBike[0] = nearestBikeName;

			random = getAlphaNumericString(7);
			myBike[1] = random;


			Log.d("bashof2",myBike[0]+" lol "+myBike[1]);
			sendCommand sc = new sendCommand();
			sc.execute();

			//kfaya = true;
			//scanDialog();
			//kfaya = false;
			//try {

				/*
				JSONArray myArray = new JSONArray(  (JSONObject) nearbikes.getLocale().toString());
				JSONArray myArray2 = new JSONArray(myArray.getJSONArray(0));

				for (int i = 0; i < myArray2.length(); i++) {
					//JSONObject x = (JSONObject) myArray.get(i);
					//bikes.add(new GeoPoint(x.getDouble("East"), x.getDouble("North")));

					JSONObject x = (JSONObject) myArray2.get(i);

					if(x.getString("Shared").equals("True") && x.getString("Locked").equals("True")){
						double[] point = convert(36, x.getDouble("East"), x.getDouble("North"),true);
						Bike bike = new Bike(x.getString("Name"), new GeoPoint(point[0], point[1]));
						bikes.add(bike);
					}
				}


				arr2[0] = bikes.get(0).getID();
				random = getAlphaNumericString(7);
				arr2[1] = random;

				updateCommand uc = new updateCommand();
				uc.doInBackground();

			} catch (JSONException e) {
				e.printStackTrace();
			}*/
		}
	}

	public class sendCommand extends AsyncTask<String, String, String> {

		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... strings) {
			try {

				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				//HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
				HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/updateCommand");

				String Name = myBike[0];
				String Command = myBike[1];

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("Name", Name));
				nameValuePairs.add(new BasicNameValuePair("Command", Command));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				response = httpclient.execute(httppost);


			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {

			scanDialog();
			//scanDialog();
		}
	}



	public class updateBikeName extends AsyncTask<String, String, String> {

		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... strings) {
			try {

				//ToDo UNCOMMENT WHEN USER SERVER IS READY!

				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				//HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
				HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/updatebikeid");

				String email = newBike[0];
				String bikeID = newBike[1];

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("email", email));
				nameValuePairs.add(new BasicNameValuePair("bikeID", bikeID));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				response = httpclient.execute(httppost);


			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			Intent intent = new Intent(start_activity.this, choice_activity.class);
			intent.putExtra("shared",false);
			startActivity(intent);
		}
	}



	public class LocationBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals( "ACT_LOC")){
				//  double lat = intent.getDoubleExtra("latitude",0);
				//  double lng = intent.getDoubleExtra("longitude",0);

				currentLocation.setLatitude(intent.getDoubleExtra("latitude",0));
				currentLocation.setLongitude(intent.getDoubleExtra("longitude",0));
			}
		}
	}

	void startService(){
		try{
			LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
			IntentFilter filter = new IntentFilter("ACT_LOC");
			registerReceiver(receiver, filter);
			Intent intent = new Intent(start_activity.this, LocationService.class );
			startService(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void stopService(){
		try{
			LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
			IntentFilter filter = new IntentFilter("ACT_LOC");
			registerReceiver(receiver, filter);
			Intent intent = new Intent(start_activity.this, LocationService.class );
			stopService(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public class Bike{
		private String ID;
		private GeoPoint initialLoc;

		public Bike(String ID, GeoPoint initialLoc){
			this.ID = ID;
			this.initialLoc = initialLoc;
		}

		public String getID() {
			return ID;
		}

		public GeoPoint getInitialLoc() {
			return initialLoc;
		}
	}


	public class unlockBike extends AsyncTask<String, String, String> {

		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... strings) {
			try {

				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/unlockBike");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("Name",newBike[1] ));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				response = httpclient.execute(httppost);

				//onPostExecute("");

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@SuppressLint("WrongThread")
		@Override
		protected void onPostExecute(String result) {
			if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {

			} else if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {

			}
		}
	}


	private class getUserBike extends AsyncTask<Void, Void, Void> {
		private ProgressDialog loading3;

		public getUserBike(start_activity activity) {
			loading3 = new ProgressDialog(activity);
		}

		@Override
		protected void onPreExecute() {
			loading3.setMessage("Loading, please wait...");
			loading3.show();
		}
		@SuppressLint("WrongThread")
		@Override
		protected Void doInBackground(Void... args) {

			if(!isCancelled()) {

				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder().url(url2).build();

				try (Response responsee = client.newCall(request).execute()) {
					FetchedBikeID = responsee.body().string();

					Log.d("checkmepls", "lpppppppppppppl");
					//Log.d("checkmepls", FetchedBikeID+" lol wala eh?");
					if (!responsee.message().equals("OK")) {

						Log.d("checkmepls", "lel");

						//cancel(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// do UI work here
			if (loading3.isShowing()) {
				loading3.dismiss();

				Log.d("checkmepls", "da5alt444");
				if(!isCancelled()){

					if(FetchedBikeID.charAt(0)=='"' && FetchedBikeID.charAt(1)=='"'){
						Log.d("checkmepls", "da5alt");

						if(currentLocation.getLatitude()==0.0){
							//		Log.d("checkmepls", "da5alt2");

							//createNotificationChannel();

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(start_activity.this, "Please turn on your location", Toast.LENGTH_SHORT).show();
								}
							});

						}else {
							Deg2UTM utm = new Deg2UTM(currentLocation.getLatitude(), currentLocation.getLongitude());

							arr[0] = utm.getEasting() + "";
							arr[1] = utm.getNorthing() + "";

							locateNearBikes lnb = new locateNearBikes();
							lnb.doInBackground();
						}
					}else{
						Intent intent = new Intent(start_activity.this, choice_activity.class);
						intent.putExtra("shared",false);
						startActivity(intent);
					}
				}


			}
		}
	}


	public class getUserBike2 extends AsyncTask<String, String, String> {

		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... strings) {

			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(url2).build();

			try (Response responsee = client.newCall(request).execute()) {
				FetchedBikeID = responsee.body().string();

				//Log.d("checkmepls", FetchedBikeID+" lol wala eh?");
				if(responsee.message().equals("OK")) {
					onPostExecute("");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
		@Override
		protected void onPostExecute(String result) {

			if(FetchedBikeID.charAt(0)=='"' && FetchedBikeID.charAt(1)=='"'){
				//	Log.d("checkmepls", "da5alt");

				if(currentLocation.getLatitude()==0.0){
					//		Log.d("checkmepls", "da5alt2");
					createNotificationChannel();
					//Toast.makeText(start_activity.this, "Please turn on your location", Toast.LENGTH_SHORT).show();

				}else {
					Log.d("checkmepls", "da5alt3");

					Deg2UTM utm = new Deg2UTM(currentLocation.getLatitude(), currentLocation.getLongitude());

					arr[0] = utm.getEasting() + "";
					arr[1] = utm.getNorthing() + "";
					//		arr[0] = "336948.15";
					//		arr[1] = "3331288.71";

					locateNearBikes lnb = new locateNearBikes();
					lnb.doInBackground();

					scanDialog();
					//if(QRcodeResult.equals());
				}
			}else{
				Intent intent = new Intent(start_activity.this, choice_activity.class);
				intent.putExtra("shared",false);
				startActivity(intent);
			}
		}
	}



}

