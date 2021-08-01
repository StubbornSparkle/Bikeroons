
package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

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
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

	public class choice_activity extends Activity {

	private Button settingsBtn, transportation, exercise, park, locate, remove;

	private SharedPreferences sp;
	private String FetchedEmail;

	private String url="", fetchedSpeed="", fetchedBikeID="";

	private boolean shared, parkclicked;

	private View view;

	private HttpResponse response;
	private String myBike;
	private GeoPoint myBikeLoc;

	private AlertDialog.Builder dialogBuilder;
	private AlertDialog dialog;
	private Button close;

	private MapView map;
	private IMapController mapController;
	private GeoPoint currentLocation = new GeoPoint(0.0,0.0);

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		stopService();
		startActivity(new Intent(choice_activity.this, start_activity.class));
	}
	public boolean internetIsConnected() {
		try {
			String command = "ping -c 1 google.com";
			return (Runtime.getRuntime().exec(command).waitFor() == 0);
		} catch (Exception e) {
			return false;
		}
	}


	@SuppressLint("WrongThread")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		if(internetIsConnected()){

			setContentView(R.layout.choice);

			startService();

			transportation = (Button) findViewById(R.id.transportationBtn);
			settingsBtn = (Button) findViewById(R.id.settingsBtn);
			exercise = (Button) findViewById(R.id.exerciseBtn);

			park = (Button) findViewById(R.id.park);
			locate = (Button) findViewById(R.id.locate);
			remove = (Button) findViewById(R.id.remove);

			sp = getSharedPreferences("session", Context.MODE_PRIVATE);
			FetchedEmail = sp.getString("email","");

			//locate.setVisibility(View.GONE);

			park.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					parkclicked=true;
					locateBike lb = new locateBike();
					lb.execute();
					//createNotificationChannel();
				}
			});
			try{
				shared = (boolean)getIntent().getSerializableExtra("shared");
				if(shared){
					park.setVisibility(View.GONE);

					url =getResources().getString(R.string.ngrok)+"/users/getuserbike/"+FetchedEmail;
					//park.
					locate.setVisibility(View.GONE);
					remove.setVisibility(View.GONE);
				}else{
					getUserBike gub = new getUserBike();
					gub.doInBackground();

					remove.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							removeBike rb = new removeBike();
							rb.doInBackground();
						}
					});

					locate.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							//getUserBike gub = new getUserBike();
							//gub.doInBackground();
							locateBike lb = new locateBike();
							lb.execute();
						}
					});
				}
			}catch (Exception e){
				e.printStackTrace();
			}



			settingsBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					stopService();
					Intent intent = new Intent(choice_activity.this, settings_activity.class);
					intent.putExtra("shared", shared);
					intent.putExtra("back","choice");

					startActivity(intent);
				}
			});

			exercise.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//startActivity(new Intent(choice_activity.this, workout_activity.class));

					if(shared){
						Intent intent = new Intent(choice_activity.this, workout_activity.class);
						shared = true;
						intent.putExtra("shared", shared);

						stopService();
						startActivity(intent);
					}else{
						Intent intent = new Intent(choice_activity.this, workout_activity.class);
						shared = false;
						intent.putExtra("shared", shared);

						stopService();
						startActivity(intent);
					}

				}
			});

			transportation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//startActivity(new Intent(choice_activity.this, chooseDestination3_activity.class));

					if(shared){
						Intent intent = new Intent(choice_activity.this, chooseDestination_activity.class);
						shared = true;
						intent.putExtra("shared", shared);

						stopService();
						startActivity(intent);
					}else{
						Intent intent = new Intent(choice_activity.this, chooseDestination_activity.class);
						shared = false;
						intent.putExtra("shared", shared);

						stopService();
						startActivity(intent);
					}

				}
			});
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


		private void createNotificationChannel() {
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				//CharSequence name = getString(R.string.channel);
				//String description = getString(R.string.channelD);
				CharSequence name = "1";
				String description = "1";

				int importance = NotificationManager.IMPORTANCE_DEFAULT;
				NotificationChannel channel = new NotificationChannel("1", name, importance);
				channel.setDescription(description);
				// Register the channel with the system; you can't change the importance
				// or other notification behaviors after this
				NotificationManager notificationManager = getSystemService(NotificationManager.class);
				notificationManager.createNotificationChannel(channel);

				NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
						.setSmallIcon(R.drawable.logo)
						.setContentTitle("Your bike's parking location")
						//.setContentText("Your bike is parked at: "+myBikeLoc.getLatitude()+", "+myBikeLoc.getLongitude())
						.setPriority(NotificationCompat.PRIORITY_MAX).setStyle(new NotificationCompat.InboxStyle().addLine("Your bike is parked at: ").addLine(myBikeLoc.getLatitude()+", "+myBikeLoc.getLongitude()));

				Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				builder.setSound(alarmSound);

				//builder.setDefaults(-1);

				Log.d("heehee","heehee4");
				notificationManager.notify(1,builder.build());
				//Notification notification = new Notification();

				//notificationManager.notify("1", Notification);
			}
		}


		public class getUserBike extends AsyncTask<String, String, String> {

			@SuppressLint("WrongThread")
			@Override
			protected String doInBackground(String... strings) {

				OkHttpClient client = new OkHttpClient();

				Request request = new Request.Builder().url(getResources().getString(R.string.ngrok) + "/users/getuserbike/" + FetchedEmail).build();

				try (Response responsee = client.newCall(request).execute()) {

					//TODO  zabatti el fetchedbike ID deh pls lma el user server yshta8al
					fetchedBikeID = responsee.body().string();

					onPostExecute("");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(String result) {
				myBike = fetchedBikeID;
				//locate.setVisibility(View.VISIBLE);
				//locateBike lb = new locateBike();
				//lb.doInBackground();
			}
		}

		public class removeBike extends AsyncTask<String, String, String> {

			@SuppressLint("WrongThread")
			@Override
			protected String doInBackground(String... strings) {
				try {

					// Create a new HttpClient and Post Header
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
					HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/nullifybikeid");

					Log.d("fenak", FetchedEmail);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("email", FetchedEmail));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					// Execute HTTP Post Request
					response = httpclient.execute(httppost);

					onPostExecute("");

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {
					Toast.makeText(choice_activity.this, "An Error has occurred!", Toast.LENGTH_LONG).show();

				} else if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {
					Toast.makeText(choice_activity.this, "Bike Disowned!", Toast.LENGTH_LONG).show();

					stopService();
					startActivity(new Intent(choice_activity.this, start_activity.class));
				}
			}

		}


		public class locateBike extends AsyncTask<String, String, String> {

			@SuppressLint("WrongThread")
			@Override
			protected String doInBackground(String... strings) {
				try {

					// Create a new HttpClient and Post Header
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
					HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/getbike");

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("Name", myBike.substring(1,myBike.length()-1)));
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

					//name of nearest bike! a5iran bgad!


					if(jsonArray.getJSONObject(0)!=null) {

						JSONObject jo2 = (JSONObject) jsonArray.getJSONObject(0);

						double[] location = convert(36, Double.parseDouble(jo2.get("East") + ""), Double.parseDouble(jo2.get("North") + ""), true);

						myBikeLoc = new GeoPoint(location[0], location[1]);

						//onPostExecute("");
					}else{
						Toast.makeText(choice_activity.this, "An error has occurred!\nTry again", Toast.LENGTH_SHORT).show();

					}


				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				if(!parkclicked){
					locationDialog();
				}else{
					createNotificationChannel();
					parkclicked = false;
				}
			}
		}


		public void locationDialog() {
			dialogBuilder = new AlertDialog.Builder(this);

			final View popupView = getLayoutInflater().inflate(R.layout.mybikelocationpopup, null);

			dialogBuilder.setView(popupView);
			dialog=dialogBuilder.create();

			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width =  WindowManager.LayoutParams.MATCH_PARENT;
			lp.height =  WindowManager.LayoutParams.MATCH_PARENT;
			dialog.show();
			dialog.getWindow().setAttributes(lp);
			dialog.show();

			map = (MapView) popupView.findViewById(R.id.map);
			map.getTileProvider().clearTileCache();
			Configuration.getInstance().setCacheMapTileCount((short) 12);
			Configuration.getInstance().setCacheMapTileOvershoot((short) 12);
			// Create a custom tile source
			map.setTileSource(new OnlineTileSourceBase("", 1, 20, 512, ".png",
					new String[]{"https://a.tile.openstreetmap.org/"}) {
				@Override
				public String getTileURLString(long pMapTileIndex) {
					return getBaseUrl()
							+ MapTileIndex.getZoom(pMapTileIndex)
							+ "/" + MapTileIndex.getX(pMapTileIndex)
							+ "/" + MapTileIndex.getY(pMapTileIndex)
							+ mImageFilenameEnding;
				}
			});

			mapController = map.getController();
			mapController.setZoom(15.0);

			Marker spot = new Marker(map);
			spot.setPosition(myBikeLoc);
			spot.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			spot.setTitle("Your bike is here");
			spot.setPanToView(true);

			spot.setIcon(getResources().getDrawable(R.drawable.ic_bikeloc));
			mapController.setCenter(myBikeLoc);

			map.getOverlays().add(spot);

			Marker me = new Marker(map);
			me.setPosition(currentLocation);
			me.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			me.setTitle("You are here");
			me.setPanToView(true);

			me.setIcon(getResources().getDrawable(R.drawable.ic_mylocation));

			map.getOverlays().add(me);

			map.invalidate();

			close = (Button) popupView.findViewById(R.id.close);
			close.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
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


		void startService(){
			try{
				LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
				IntentFilter filter = new IntentFilter("ACT_LOC");
				registerReceiver(receiver, filter);
				Intent intent = new Intent(choice_activity.this, LocationService.class );
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
				Intent intent = new Intent(choice_activity.this, LocationService.class );
				stopService(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public class LocationBroadcastReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("ACT_LOC")) {

					currentLocation.setLatitude(intent.getDoubleExtra("latitude", 0));
					currentLocation.setLongitude(intent.getDoubleExtra("longitude", 0));
				}
			}

		}
	}