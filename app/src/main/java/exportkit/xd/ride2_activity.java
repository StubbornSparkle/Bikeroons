package exportkit.xd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ride2_activity extends Activity implements Serializable {

    private Button myLoc, accidentTest;

    private MapView map;
    private double myx;
    private double myy;

    private TextView temp, humidity;

    private Marker myLocTemp;

    private GeoPoint currentLocation = new GeoPoint(0.0,0.0);
    private GeoPoint startLocation;

    private View view;

    private BlurView blur;

    private Chronometer cmTimer;
    private Button btnResume, btnStop, btnPause;
    private long elapsedTime;
    //  private Button go;
    private TextView ridePaused;

    private View areyousure;
    private Button yes;
    private Button resumelight;

    private String araf="";
    private String araf2="";

    private View loading;
    private IMapController mapController;

    private Polyline temporary;

    private TextView timeToArrival, kms;
    private TextView goal;

    private ImageView gas;

    private ArrayList<GeoPoint> stations;
    private boolean showstations;

    private ArrayList stationstuff = new ArrayList();
    private Polyline stationtemp;

    private TextView kmToArrivalNum;
    private CountDownTimer timer;
    private int totalTime;
    private long milliLeft;

    private TextView kmToArrival;

    private ArrayList<double[]> history = new ArrayList<>();

    private double kiloms, totalDist;
    private boolean caloriesMode, distanceMode,  timeMode;

    private SharedPreferences sp;
    private String FetchedEmail;

    private String currentTime, currentDate;

    private ArrayList<GeoPoint> waypoints = new ArrayList();
    private String[] params = new String[6];
    private HttpResponse response;
    private boolean ended= false;


    private TextView power, speed, price, percentage, rotpermin;
    private String fetchedSpeed="0.0";
    private double weight = 60.7, height;

    private String FetchedBikeID;
    private boolean startGettingSpeed = false;
    private double wheelSize = 2.07; //26 inches in diameter to circumference in meter

    private float caloriesBurnt=0;

    private RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

    displayRoute dr = new displayRoute();

    private boolean paused;
    private getSpeed gs = new getSpeed();

    private double distanceToLoc;
    private GeoPoint dest;

    private boolean done= false;
    private boolean locationFound = false;

    private Route r = new Route();
    private Polyline tempRoute;
    //________________________________________________________________________________________________________________________
    //________________________________________________________________________________________________________________________

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        stopService();
        btnResume.setEnabled(true);
        btnStop.setEnabled(false);
        btnPause.setEnabled(true);

        chronoPause();
        showMenu();
        blur.setVisibility(View.VISIBLE);
        btnResume.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        myLoc.setVisibility(View.GONE);
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
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(isInternetAvailable()){

            setContentView(R.layout.ride);

            sp = getSharedPreferences("session", Context.MODE_PRIVATE);
            FetchedEmail = sp.getString("email","");

            dest = (GeoPoint) getIntent().getSerializableExtra("dest");
            btnResume = (Button) findViewById(R.id.resumeride);
            btnResume.setVisibility(View.GONE);

            btnStop = (Button) findViewById(R.id.endRide);
            btnPause = (Button) findViewById(R.id.pauseRide);
            myLoc = (Button) findViewById(R.id.loc);
            blur = (BlurView) findViewById(R.id.blur);
            areyousure = (View) findViewById(R.id.areyousure);
            yes = (Button) findViewById(R.id.yes);
            myLoc = (Button) findViewById(R.id.loc);
            resumelight = (Button) findViewById(R.id.resumelight);
            loading = (View) findViewById(R.id.loading);
            gas = (ImageView) findViewById(R.id.gas);
            stations = new ArrayList();
            kms = (TextView) findViewById(R.id.kms);
            kmToArrivalNum = (TextView) findViewById(R.id.kmToArrivalNum);

            cmTimer = (Chronometer) findViewById(R.id.cmTimer);

            price = (TextView) findViewById(R.id.price);
            speed = (TextView) findViewById(R.id.speed);
            rotpermin = (TextView) findViewById(R.id.rotpermin);
            percentage = (TextView) findViewById(R.id.percentage);

            temp = (TextView) findViewById(R.id.temp);
            humidity = (TextView) findViewById(R.id.humidity);
            power = (TextView) findViewById(R.id.power);

            map = (MapView) findViewById(R.id.map);

            dest = (GeoPoint) getIntent().getSerializableExtra("dest");

            getUserBike gub = new getUserBike();
            gub.doInBackground();

            currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

            callAsynchronousTask();
           // r.execute();
            dr.execute();
            getWeather2();

            mapSetup ms = new mapSetup(ride2_activity.this);
            ms.execute();

              blurBackground();
            //   blur.setVisibility(View.VISIBLE);

           // myLoc.setVisibility(View.GONE);
            myLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map.getController().animateTo(currentLocation);
                }
            });


            loading.setVisibility(View.GONE);
            blur.setVisibility(View.GONE);

            btnStop.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.VISIBLE);
            myLoc.setVisibility(View.VISIBLE);


            //chronoPause();

            ridePaused = (TextView) findViewById(R.id.ridePaused);
            ridePaused.setVisibility(View.GONE);

            //ARE YOU SURE??
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ended = true;
                    String endTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    String endDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                    String full = "[";

                    for(int i=0; i<history.size(); i++){
                        if(i == history.size()-1)
                            full+="{"+history.get(i)[0]+","+history.get(i)[1]+"}]";
                        else
                            full+="{"+history.get(i)[0]+","+history.get(i)[1]+"},";
                    }

                    params[0] = FetchedEmail;
                    params[1] = full;
                    params[2] = currentDate;
                    params[3] = endDate;
                    params[4] = currentTime;
                    params[5] = endTime;


                    YourAsyncTask yat = new YourAsyncTask(ride2_activity.this);
                    yat.execute();
//                    CreateRide cr = new CreateRide();
//                    cr.doInBackground();

                    if((boolean)getIntent().getSerializableExtra("shared")) {
                        lockBike lb = new lockBike();
                        lb.doInBackground();
                        // removeBike rb = new removeBike();
                        // rb.doInBackground();
                    }

                    dr.cancel(true);
                    gs.cancel(true);
                    stopService();
                    startActivity(new Intent(ride2_activity.this, rideSummary_activity.class));
                }
            });


            accidentTest = (Button) findViewById(R.id.accident);
            accidentTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int LAUNCH_SECOND_ACTIVITY = 1;
                    Intent i = new Intent(ride2_activity.this, accident_activity.class);
                    startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
                }
            });

            resumelight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnResume.setEnabled(false);
                    btnStop.setEnabled(true);
                    btnPause.setEnabled(true);

                    chronoStart();
                    hideMenu();
                    blur.setVisibility(View.GONE);
                    btnResume.setVisibility(View.GONE);
                    btnStop.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.VISIBLE);

                    myLoc.setVisibility(View.VISIBLE);
                }
            });
            hideMenu();
            //END

            //loc

            if(Build.VERSION.SDK_INT >= 23){
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1 );
                }else{
                    startService();
                }
            }else{
                startService();
            }

            //end loc


         //   myLoc.setVisibility(View.GONE);
            myLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(myx == 0.0 || myy == 0.0){
                        Toast.makeText(getApplicationContext(), "Finding your location...", Toast.LENGTH_SHORT).show();
                    }else{
                        map.getController().animateTo(currentLocation);
                    }
                }
            });


            gas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(showstations){
                        for(int i=0; i<stationstuff.size(); i++){
                            map.getOverlays().remove(stationstuff.get(i));
                        }
                        showstations=true;
                    }else{

                        locateStations ls = new locateStations();
                        ls.execute();
                    }
                }
            });

            goal = (TextView) findViewById(R.id.goal);

            final Handler handler = new Handler();
            Timer timer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {

                                displayRoute2 dr2 = new displayRoute2();
                                dr2.execute();
                            } catch (Exception e) {
                                // error, do something
                            }
                        }
                    });
                }
            };

            timer.schedule(task, 0, 6*1000);

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





    private class YourAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public YourAsyncTask(ride2_activity activity) {
            loading3 = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading3.setMessage("Concluding your ride, please wait...");
            loading3.show();
        }
        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... args) {

            CreateRide cr = new CreateRide();
            cr.execute();

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (loading3.isShowing()) {
                loading3.dismiss();

            }
        }
    }




    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.resumeride:
                btnResume.setEnabled(false);
                btnStop.setEnabled(true);
                btnPause.setEnabled(true);

                btnPause.setVisibility(View.VISIBLE);
                chronoStart();
                break;

            case R.id.resumelight:
                btnResume.setEnabled(false);
                btnStop.setEnabled(true);
                btnPause.setEnabled(true);

                chronoStart();
                hideMenu();
                blur.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                break;

            case R.id.endRide:
                btnResume.setEnabled(true);
                btnStop.setEnabled(false);
                btnPause.setEnabled(true);

                chronoPause();
                showMenu();
                blur.setVisibility(View.VISIBLE);
                btnResume.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                myLoc.setVisibility(View.GONE);
                break;

            case R.id.pauseRide:

                if(paused)
                    paused = false;
                else
                    paused = true;

                btnPause.setEnabled(false);
                btnResume.setEnabled(false);
                btnStop.setEnabled(false);

                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);

                blur.setVisibility(View.VISIBLE);
                ridePaused.setVisibility(View.VISIBLE);

                myLoc.setVisibility(view.GONE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        blur.setVisibility(View.GONE);
                        ridePaused.setVisibility(View.GONE);
                        myLoc.setVisibility(view.VISIBLE);
                    }
                }, 750);

                btnResume.setEnabled(true);
                btnStop.setEnabled(true);

                btnResume.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.VISIBLE);

                chronoPause();

                break;
        }
    }

    private void chronoStart(){
        // on first start
        if ( elapsedTime == 0 )
            cmTimer.setBase( SystemClock.elapsedRealtime() );
            // on resume after pause
        else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - elapsedTime);
            cmTimer.setBase( cmTimer.getBase() + intervalOnPause );
        }
        cmTimer.start();
    }

    private void chronoPause(){
        cmTimer.stop();
        elapsedTime = SystemClock.elapsedRealtime();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults );
        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startService();
                }else{
                    Toast.makeText(this, "Give me permission", Toast.LENGTH_LONG).show();
                }
        }
    }

    void startService(){
        try{
            LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
            IntentFilter filter = new IntentFilter("ACT_LOC");
            registerReceiver(receiver, filter);
            Intent intent = new Intent(ride2_activity.this, LocationService.class );
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
            Intent intent = new Intent(ride2_activity.this, LocationService.class );
            stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals( "ACT_LOC")){

                myx = intent.getDoubleExtra("latitude",0);
                myy = intent.getDoubleExtra("longitude",0);
                currentLocation.setLatitude(myx);
                currentLocation.setLongitude(myy);

                double[] latLong = {myx, myy};
                history.add(latLong);

                if(history.size()>= 2){
                    kiloms = calculateDistance(history.get(history.size()-2)[0],history.get(history.size()-2)[1], history.get(history.size()-1)[0],history.get(history.size()-1)[1]);
                }
                kiloms = kiloms/1000;
                totalDist+=kiloms;

                DecimalFormat df = new DecimalFormat("#.###");
                kms.setText(df.format(totalDist)+"");

                DecimalFormat ddf = new DecimalFormat("#.#");
                power.setText(""+ddf.format(0.004 * Double.parseDouble(fetchedSpeed) * ((weight+25)*9.80665)));

                DecimalFormat dff = new DecimalFormat("#.##");
                price.setText(dff.format((0.712*36*10/1000)*0.54)+"");
                markMyLocation();

                if(!locationFound && myx != 0.0){
                    markMyDestination();
                    routeToDest rtd = new routeToDest();
                    rtd.execute();
                    locationFound = true;
                  //  Route r = new Route();
                   // r.doInBackground();
                }

                if(distanceToLoc <= 0.01 && distanceToLoc > 0.00&& !ended){
                    ended=true;
                    stopService();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            endRide();
                        }
                    }, 5000);

                }
            }
        }
    }



    public void endRide(){

        stopService();
        ended = true;

        int LAUNCH_SECOND_ACTIVITY = 1;
        Intent intent = new Intent(ride2_activity.this, endRide_activity.class);

        String endTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String endDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        intent.putExtra("history",history.toArray());
        intent.putExtra("kiloms",totalDist);
        intent.putExtra("startDate",currentDate);
        intent.putExtra("startTime",currentTime);
        intent.putExtra("endDate",endDate);

        intent.putExtra("time", cmTimer.getText().toString());

        intent.putExtra("endTime", endTime);
        startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
    }



    private static long calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        long distanceInMeters = Math.round(6371000 * c);
        return distanceInMeters;
    }


    private void markMyLocation(){
        if(map == null) {
            return;
        }
        map.getOverlays().remove(myLocTemp);

        Marker my_marker = new Marker(map);

        my_marker.setPosition(currentLocation);
        my_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        my_marker.setTitle("Your location");
        my_marker.setPanToView(true);

        my_marker.setIcon(getResources().getDrawable(R.drawable.ic_mylocation));

        myLocTemp = my_marker;

        map.getOverlays().add(my_marker);

        map.invalidate();
    }






    public class locateStations extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {



                stations.add(new GeoPoint(currentLocation.getLatitude()+0.04501,currentLocation.getLongitude()-0.019821));
                stations.add(new GeoPoint(currentLocation.getLatitude()-0.01568,currentLocation.getLongitude()+0.061864));

                Location locationA = new Location("current");

                locationA.setLatitude(currentLocation.getLatitude());
                locationA.setLongitude(currentLocation.getLongitude());

                for(int i=0; i<stations.size(); i++){
                    Marker my_marker = new Marker(map);

                    my_marker.setPosition(stations.get(i));
                    my_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    my_marker.setPanToView(true);
                    //my_marker.setTitle("Available bike #"+i+1);

                    Location locationB = new Location("destination");
                    locationB.setLatitude(stations.get(i).getLatitude());
                    locationB.setLongitude(stations.get(i).getLongitude());


                    DecimalFormat df = new DecimalFormat("#.##");
                    double distance = locationA.distanceTo(locationB)/1000.0;

                    my_marker.setIcon(getResources().getDrawable(R.drawable.ic_gasmarker));

                    map.getOverlays().add(my_marker);

                    map.invalidate();

                    RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

                    //map.getOverlays().remove(desttemp);
                    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                    waypoints.add(currentLocation);
                    waypoints.add(stations.get(i));


                    my_marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            Toast.makeText(getApplicationContext(), "Station X\n"+df.format(distance)+" km away", Toast.LENGTH_LONG).show();

                            stationstuff.add(stationtemp);
                            map.getOverlays().remove(stationtemp);
                            Road road = roadManager.getRoad(waypoints);
                            Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getResources().getColor(R.color.red), 17);

                            map.getOverlays().add(roadOverlay);
                            stationtemp = roadOverlay;
                            Toast.makeText(getApplicationContext(), "The location is unreachable", Toast.LENGTH_SHORT);

                            stationstuff.add(roadOverlay);
                            map.invalidate();
                            return false;
                        }
                    });

                    stationstuff.add(my_marker);
                }
                showstations=true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            map.invalidate();
        }
    }








    private void locateStations(){

    }

    public static String formatSecondDateTime(int scound) {
        if(scound <= 0)return "";
        int h = scound / 3600;
        int m = scound % 3600 / 60;
        int s = scound % 60; // Less than 60 is the second, enough 60 is the minute
        return h+":"+m+":"+s;
    }

    public void timerStart(long timeLengthMilli) {
        milliLeft = timeLengthMilli;
        timer = new CountDownTimer(timeLengthMilli, 1000) {

            @Override
            public void onTick(long milliTillFinish) {
                milliLeft -= 1000;
                goal.setText(formatSecondDateTime(Integer.parseInt((milliLeft / 1000)+"")));
                //clock.setText(Long.toString(min) + ":" + Long.toString(sec));
            };
            @Override
            public void onFinish() {
            };
        };
        timer.start();
    }


    public void timerPause(){
        timer.cancel();
    };
    private void timerResume(){
        timerStart(milliLeft);
    };


//    public boolean internetIsConnected() {
//        try {
//            String command = "ping -c 1 google.com";
//            return (Runtime.getRuntime().exec(command).waitFor() == 0);
//        } catch (Exception e) {
//            return false;
//        }
//    }


    private void blurBackground(){
        float radius=21f;
        View decorview = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorview.findViewById(android.R.id.content);
        Drawable windowBackground = decorview.getBackground();
        blur.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);
    }

    private void hideMenu(){

        areyousure.setVisibility(view.GONE);
        yes.setVisibility(view.GONE);
        resumelight.setVisibility(view.GONE);
    }

    private void showMenu(){

        areyousure.setVisibility(view.VISIBLE);
        yes.setVisibility(view.VISIBLE);
        resumelight.setVisibility(view.VISIBLE);
    }


    private void getWeather2(){
        HTTPRequestTask req = new HTTPRequestTask();
        req.execute();
    }

    public class HTTPRequestTask extends AsyncTask<String, String, String> {

        // private TextView temp, humidity;
        // private String weather;

        @Override
        protected String doInBackground(String... strings) {
            try {

               /* temp = (TextView) findViewById(R.id.temp);
                humidity = (TextView) findViewById(R.id.humidity);
*/
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=cairo&appid=fc3c79a96742e5c93ac6bca034745b85");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

           /* StringBuilder sb = new StringBuilder();
            while (br.readLine() != null) {
                sb.append(br.readLine());
            }*/
                JSONObject json = new JSONObject(br.readLine());

                Iterator x = json.keys();
                JSONArray jArray = new JSONArray();

                while (x.hasNext()){
                    String key = (String) x.next();
                    jArray.put(json.get(key));
                }

                for (int i=0; i < jArray.length(); i++) {
                    try {
                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        araf = oneObject.getString("temp");
                        araf2 = oneObject.getString("humidity");

                        // Log.d("lul",weather);**
                    } catch (JSONException e) {
                        // Oops
                    }
                }

                //  RequestQueue queue = Volley.newRequestQueue();
                // queue.add(jor);

                String output;
                System.out.println("Output from Server .... \n");
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                }
                conn.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            //progressDialog.dismiss();
            //araf = result;
            humidity.setText(araf2+"%");
            temp.setText((Math.round(Float.parseFloat(araf)/10))+"℃");

        }

    }



    public class CreateRide extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try{
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/createRide");

                String email = params[0];
                String history = params[1];
                String startDate = params[2];
                String endDate = params[3];
                String startTime = params[4];
                String endTime = params[5];

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", email));
                nameValuePairs.add(new BasicNameValuePair("history", history));
                nameValuePairs.add(new BasicNameValuePair("startDate", startDate));
                nameValuePairs.add(new BasicNameValuePair("endDate", endDate));
                nameValuePairs.add(new BasicNameValuePair("startTime", startTime));
                nameValuePairs.add(new BasicNameValuePair("endTime", endTime));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);

                //Toast.makeText(register_activity.this, "lol "+response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
            } catch(ClientProtocolException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {

        }
    }

    public class displayRoute extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            if(!isCancelled()) {
                if(myx !=0.0) {
                    try {

                        //     map.getOverlays().remove(tempRoute);
                        //roadManager = new OSRMRoadManager();
                        // roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

    /*          waypoints = new ArrayList();

                waypoints.add(currentLocation);
                waypoints.add(dest);

                Log.d("malakyabni", currentLocation.getLatitude()+" maho hna aho...");

                Road road = roadManager.getRoad(waypoints);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getResources().getColor(R.color.blue), 17);

                tempRoute = roadOverlay;

                map.getOverlays().add(roadOverlay);
                map.invalidate();
*/

//                map.getOverlays().remove(temporary);

                        ArrayList<GeoPoint> tari2 = new ArrayList();

                        tari2.add(new GeoPoint(history.get(history.size() - 1)[0], history.get(history.size() - 1)[1]));
                        tari2.add(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

                /*              for (int i = 0; i < history.size(); i++) {
                    tari2.add(new GeoPoint(history.get(i)[0], history.get(i)[1]));
                }
*/

                        Road road = roadManager.getRoad(tari2);
                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, R.color.blue, 17);

                        //temporary = roadOverlay;
                        map.getOverlays().add(roadOverlay);

                        map.invalidate();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result){

        }
    }




    public class routeToDest extends AsyncTask<String, String, String> {

        RoadManager roadManager;
        ArrayList<GeoPoint> waypoints;
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try{

                map.getOverlays().remove(tempRoute);
                roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

                waypoints = new ArrayList();

                waypoints.add(currentLocation);
                waypoints.add(dest);

                Road road = roadManager.getRoad(waypoints);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getResources().getColor(R.color.blue), 17);

                tempRoute = roadOverlay;

                map.getOverlays().add(roadOverlay);
                map.invalidate();

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result){
        }
    }



    public class getUserBike extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {

            OkHttpClient client = new OkHttpClient();

            Request request = null;
            if((boolean)getIntent().getSerializableExtra("shared")){
                request = new Request.Builder().url(getResources().getString(R.string.ngrok) + "/users/getusertempbike/" + FetchedEmail).build();
            }else{
                request = new Request.Builder().url(getResources().getString(R.string.ngrok) + "/users/getuserbike/" + FetchedEmail).build();
            }

            try (Response responsee = client.newCall(request).execute()) {
                FetchedBikeID = responsee.body().string();

            } catch (Exception e) {
                e.printStackTrace();
            }

            onPostExecute("");
            return null;
        }
        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(String result) {

            startGettingSpeed = true;
            gs.execute();
        }
    }



    public class getSpeed extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            if(!isCancelled()) {
                try {

                    HttpClient httpclient = new DefaultHttpClient();

                    HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/getbike");

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("Name", FetchedBikeID.substring(1, FetchedBikeID.length() - 1)));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    response = httpclient.execute(httppost);

                    InputStream IS = response.getEntity().getContent();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(IS));
                    JSONTokener tokener = new JSONTokener(bufferedReader.readLine());
                    JSONObject json = new JSONObject(tokener);

                    Iterator x = json.keys();
                    JSONArray jsonArray = new JSONArray();

                    while (x.hasNext()) {
                        String key = (String) x.next();
                        jsonArray.put(json.get(key));
                    }

                    //name of nearest bike! a5iran bgad!

                    JSONObject jo2 = (JSONObject) jsonArray.getJSONObject(0);

                    fetchedSpeed = jo2.getString("Speed");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            double rpm = (Double.parseDouble(fetchedSpeed)*1000/60)/wheelSize;

            DecimalFormat df = new DecimalFormat("#.#");
            rotpermin.setText(df.format(rpm)+"");
            speed.setText(df.format(Double.parseDouble(fetchedSpeed)));

//            gs.execute();
            /*try {
                Thread.sleep(5000);
               // getSpeed gs = new getSpeed();
                gs.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }

    public class lockBike extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/lockBike");

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("Name", FetchedBikeID.substring(1,FetchedBikeID.length()-1) ));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);

                onPostExecute("");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(String result) {
            if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {
                Toast.makeText(ride2_activity.this, "An Error has occurred!", Toast.LENGTH_LONG).show();

            } else if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {
                removeBike rb = new removeBike();
                rb.doInBackground();
            }
        }
    }


    public class removeBike extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/nullifytempbikeid");

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
                Toast.makeText(ride2_activity.this, "An Error has occurred!", Toast.LENGTH_LONG).show();

            } else if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {
                Toast.makeText(ride2_activity.this, "Shared bike Disowned!", Toast.LENGTH_LONG).show();

                stopService();

                dr.cancel(true);
                gs.cancel(true);
                startActivity(new Intent(ride2_activity.this, start_activity.class));
            }
        }
    }



    private class mapSetup extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public mapSetup(ride2_activity activity) {
            loading3 = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading3.setMessage("Loading, please wait...");
            loading3.show();
        }
        @Override
        protected Void doInBackground(Void... args) {

            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

            map.getTileProvider().clearTileCache();
            Configuration.getInstance().setCacheMapTileCount((short) 12);
            Configuration.getInstance().setCacheMapTileOvershoot((short) 12);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

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
                }
            });
            // Create a custom tile source

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapController = map.getController();
                    mapController.setCenter(currentLocation);
                    mapController.setZoom(11.0);
                }
            });

            map.setMultiTouchControls(true);
            map.invalidate();
        //    myLoc.setVisibility(myLoc.GONE);

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (loading3.isShowing()) {

                chronoStart();

                startLocation = currentLocation;
                waypoints.add(startLocation);

                loading3.dismiss();
            }
        }
    }


    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            r.execute();
                            dr.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    }


    public class Route extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {
                map.setMultiTouchControls(true);

                //  Toast.makeText(chooseDestination3_activity.this, "myx is: "+myx+ " myy is: "+myy, Toast.LENGTH_SHORT).show();
                //location
                Location locationA = new Location("current");

                locationA.setLatitude(currentLocation.getLatitude());
                locationA.setLongitude(currentLocation.getLongitude());

                Location locationB = new Location("destination");

                locationB.setLatitude(dest.getLatitude());
                locationB.setLongitude(dest.getLongitude());

                DecimalFormat df = new DecimalFormat("#.##");
                distanceToLoc = locationA.distanceTo(locationB)/1000.00;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        kmToArrivalNum.setText(df.format(distanceToLoc));
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {

        }
    }

    private void markMyDestination(){
        if(map == null) {
            return;
        }

        Marker my_marker = new Marker(map);

        Marker temp = my_marker;

        try {
            map.getOverlays().remove(temp);

            my_marker.setPosition(dest);
            my_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            my_marker.setTitle("Your destination");
            my_marker.setPanToView(true);

            my_marker.setIcon(getResources().getDrawable(R.drawable.ic_destloc));

            map.getOverlays().add(my_marker);

            map.invalidate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public class displayRoute2 extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try{

               // map.getOverlays().remove(temporary);

                RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

                Log.d("yallihna", "here "+history.size());
                ArrayList<GeoPoint> tari2 = new ArrayList();
                for(int i=0; i<history.size(); i++){
                    tari2.add(new GeoPoint(history.get(i)[0], history.get(i)[1]));
                }

                try {
                    Road road = roadManager.getRoad(tari2);
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getResources().getColor(R.color.cool), 17);

                    temporary = roadOverlay;
                    map.getOverlays().add(roadOverlay);
                }catch(Exception e){
                    e.printStackTrace();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
        }
    }




}

