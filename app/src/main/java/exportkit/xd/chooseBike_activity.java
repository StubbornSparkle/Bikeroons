package exportkit.xd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
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
import org.json.JSONException;
import org.json.JSONObject;
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

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class chooseBike_activity extends AppCompatActivity {

    private MapView map;

    private Button myLoc;

    private Button start;

    double myx, myy;

    private ArrayList<Bike> bikes = new ArrayList<>();

    private Marker myLocTemp;
    private boolean destChosen = false;

    private GeoPoint currentLocation;

    private View view;

    private BlurView blur;
    private View loading;

    private Polyline biketemp;
    private Bike chosenBike;
    private boolean canStart=true;


    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private ImageView qr;
    private EditText text;

    private String random;
    private String resultt;

    private String url, fullRes="";
    private boolean bikesLocated;


    private HttpResponse response;
    private String[] arr = new String[2];
    private Button scanCode;


    private String QRcodeResult = "";

    private SharedPreferences sp;
    private String FetchedEmail;
    private String[] newBike = new String[2];
    /*  public void qrPopup() {

        dialogBuilder = new AlertDialog.Builder(this);

        final View popupView = getLayoutInflater().inflate(R.layout.qrpopup, null);

        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 1200;
        lp.height = 1200;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        dialog.show();


        qr = (ImageView) findViewById(R.id.qr);
        text = (EditText) findViewById(R.id.text);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(text.getText().toString(), BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qr.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } */


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(chooseBike_activity.this, start_activity.class));
    }

//    public boolean internetIsConnected() {
//        try {
//            String command = "ping -c 1 google.com";
//            return (Runtime.getRuntime().exec(command).waitFor() == 0);
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(isInternetAvailable()){

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            setContentView(R.layout.choosebike);

            sp = getSharedPreferences("session", Context.MODE_PRIVATE);
            FetchedEmail = sp.getString("email","");

            //loading
            blur = (BlurView) findViewById(R.id.blur);

            blurBackground();
            blur.setVisibility(View.VISIBLE);

            Log.d("nshof", "heehee");

            loading = (View) findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            blur.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loading.setVisibility(View.GONE);
                    blur.setVisibility(View.GONE);

                    myLoc.setVisibility(myLoc.VISIBLE);
                    start.setVisibility(View.VISIBLE);
                }
            }, 4000);

            currentLocation = new GeoPoint(myx,myy);

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

            //END

            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
            map = (MapView) findViewById(R.id.map);
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


            locateRealBikes lrb = new locateRealBikes();
            lrb.execute();

            Log.d("garab kda","lolli");
            final Handler handlerr = new Handler();
            handlerr.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if(currentLocation.getLongitude()!=0.0 && currentLocation.getLatitude()!=0.0) {

                        map.setMultiTouchControls(true);
                        IMapController mapController = map.getController();

                        mapController.setZoom(11.0);

                        //locateBikes();

                        //  Toast.makeText(chooseDestination3_activity.this, "myx is: "+myx+ " myy is: "+myy, Toast.LENGTH_SHORT).show();
                        mapController.setCenter(currentLocation);
                    }
                }
            }, 3000);

            map.invalidate();

            //GETTING THE DEVICE LOCATION

            myLoc = (Button) findViewById(R.id.loc);
            myLoc.setVisibility(View.GONE);
            myLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)  {
                    map.getController().animateTo(currentLocation);
                }
            });
            //END

            start = (Button) findViewById(R.id.start);
            start.setVisibility(View.GONE);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(destChosen){
                        if(canStart){

                            /*startActivity( new Intent(chooseBike_activity.this, qr_activity.class));
                            qrPopup();

                            IntentIntegrator intentIntegrator = new IntentIntegrator(chooseBike_activity.this);
                            intentIntegrator.setCaptureActivity(Capture.class);
                            intentIntegrator.initiateScan();
                            //executeSSHcommand();
                            result = intentIntegrator.getCaptureActivity().toString();*/


                            //if(result.equals(random)){
                              //  Toast.makeText(chooseBike_activity.this, "they're equal",Toast.LENGTH_SHORT);
                            //}

                            random = getAlphaNumericString(7);
                            arr[0] = chosenBike.getID();
                            //arr[1] ="cd /home/pi/Desktop; python3 Trial_6.py "+ random+"; cd home/pi/lib_oled96; python3 display.py";
                            arr[1] =random;

                            sendCommand sc = new sendCommand();
                            sc.doInBackground();


                           // Toast.makeText(chooseBike_activity.this, result,Toast.LENGTH_SHORT).show();


                            /*
                            if((boolean)getIntent().getSerializableExtra("workout")){

                                Intent intent = new Intent(chooseBike_activity.this, workoutRide2_activity.class);
                                switch((String)getIntent().getSerializableExtra("type")){

                                    case "calories":
                                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                                        intent.putExtra("weight", (String) getIntent().getSerializableExtra("weight"));
                                        intent.putExtra("height", (String) getIntent().getSerializableExtra("height"));
                                        intent.putExtra("calories", (String) getIntent().getSerializableExtra("calories"));
                                        break;

                                    case "distance":
                                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                                        intent.putExtra("meter", (String) getIntent().getSerializableExtra("meter"));
                                        break;

                                    case "time":
                                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                                        intent.putExtra("hour", (String) getIntent().getSerializableExtra("hour"));
                                        intent.putExtra("minute", (String) getIntent().getSerializableExtra("minute"));
                                        intent.putExtra("second", (String) getIntent().getSerializableExtra("second"));
                                        break;

                                    case "freestyle":
                                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                                        break;
                                }
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(chooseBike_activity.this, ride_activity.class);
                                intent.putExtra("dest", (Parcelable) getIntent().getSerializableExtra("dest"));
                                startActivity(intent);
                            }*/
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "You haven't picked a bike!", Toast.LENGTH_SHORT).show();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(chooseBike_activity.this);
                intentIntegrator.setCaptureActivity(Capture.class);
                intentIntegrator.initiateScan();

                random = getAlphaNumericString(7);

                resultt = intentIntegrator.getCaptureActivity().toString();

                Toast.makeText(chooseBike_activity.this, resultt, Toast.LENGTH_LONG).show();
            }
        });
    }



    public void qrPopup() {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(text.getText().toString(), BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qr.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //bikes positions
    /*private void locateBikes(){
        bikes.add(new GeoPoint(currentLocation.getLatitude()-0.00021,currentLocation.getLongitude()-0.00041));
        bikes.add(new GeoPoint(currentLocation.getLatitude()-0.10937,currentLocation.getLongitude()-0.092864));

        Location locationA = new Location("current");

        locationA.setLatitude(currentLocation.getLatitude());
        locationA.setLongitude(currentLocation.getLongitude());

        for(int i=0; i<bikes.size(); i++){
            Marker my_marker = new Marker(map);

            my_marker.setPosition(bikes.get(i));
            my_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            my_marker.setPanToView(true);
            //my_marker.setTitle("Available bike #"+i+1);

            Location locationB = new Location("destination");

            locationB.setLatitude(bikes.get(i).getLatitude());
            locationB.setLongitude(bikes.get(i).getLongitude());

            DecimalFormat df = new DecimalFormat("#.##");
            double distance = locationA.distanceTo(locationB)/1000.0;
            my_marker.setTitle("Available bike #"+i+1+"\n"+df.format(distance)+" Km away");

            my_marker.setIcon(getResources().getDrawable(R.drawable.ic_bikeloc));


            map.getOverlays().add(my_marker);

            map.invalidate();

            int finalI = i+1;

            RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

            //map.getOverlays().remove(desttemp);
            ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
            waypoints.add(currentLocation);
            waypoints.add(bikes.get(i));
            Road road = roadManager.getRoad(waypoints);
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road, R.color.blue, 17);

            my_marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    Toast.makeText(getApplicationContext(), "You have picked bike #"+ finalI+"\nIt is "+df.format(distance)+" km away", Toast.LENGTH_LONG).show();
                    map.getOverlays().remove(biketemp);
                    destChosen = true;
                    chosenBike = new GeoPoint(bikes.get(finalI-1).getLatitude(),bikes.get(finalI-1).getLongitude());

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            map.getOverlays().add(roadOverlay);
                            biketemp = roadOverlay;

                            map.invalidate();
                        }
                    }, 100);
                    return false;
                }
            });
        }
        map.invalidate();
    }*/


    public class locateRealBikes extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(getResources().getString(R.string.ngrok2) + "/bikes").build();

            try (Response responsee = client.newCall(request).execute()) {

                fullRes = responsee.body().string();

            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                bikes.clear();

                JSONArray myArray = new JSONArray(fullRes);

                for (int i = 0; i < myArray.length(); i++) {
                    //JSONObject x = (JSONObject) myArray.get(i);
                    //bikes.add(new GeoPoint(x.getDouble("East"), x.getDouble("North")));

                    JSONObject x = (JSONObject) myArray.get(i);

                    if(x.getString("Shared").equals("True") && x.getString("Locked").equals("True")){

                        double[] utm = convert(36, x.getDouble("East"), x.getDouble("North"),true);

                        Bike bike = new Bike(x.getString("Name"), new GeoPoint(utm[0], utm[1]));
                        bikes.add(bike);
                    }
                }

                Location locationA = new Location("current");

                locationA.setLatitude(currentLocation.getLatitude());
                locationA.setLongitude(currentLocation.getLongitude());

                for(int i=0; i<bikes.size(); i++){
                    Marker my_marker = new Marker(map);

                    my_marker.setPosition(bikes.get(i).getInitialLoc());
                    my_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    my_marker.setPanToView(true);
                    //my_marker.setTitle("Available bike #"+i+1);

                   // my_marker.setTitle("Available bike #"+(i+1)+"\n"+df.format(distance)+" Km away");
                    my_marker.setIcon(getResources().getDrawable(R.drawable.ic_bikeloc));
                    map.getOverlays().add(my_marker);
                    map.invalidate();

                    int finalI = i+1;

                    //ToDo ADDD THE CLICK NAME THEN

                    my_marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {

                            try {
                                ///RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

                                //map.getOverlays().remove(desttemp);
                               /// ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                               /// waypoints.add(currentLocation);

                                //ToDo fix this

                               /// waypoints.add(bikes.get(finalI - 1).getInitialLoc());
                               /// Road road = roadManager.getRoad(waypoints);
                               /// Polyline roadOverlay = RoadManager.buildRoadOverlay(road, R.color.blue, 17);

                               /// map.getOverlays().remove(biketemp);


                                Location locationA = new Location("current");

                                locationA.setLatitude(currentLocation.getLatitude());
                                locationA.setLongitude(currentLocation.getLongitude());

                                Location locationB = new Location("destination");

                                locationB.setLatitude(bikes.get(finalI-1).getInitialLoc().getLatitude());
                                locationB.setLongitude(bikes.get(finalI-1).getInitialLoc().getLongitude());

                                DecimalFormat df = new DecimalFormat("#.##");
                                double distance = locationA.distanceTo(locationB)/1000.0;


                                Log.d("locations",distance+"");
                                Toast.makeText(getApplicationContext(), "You have picked bike #"+ finalI+"\nIt is "+df.format(distance)+" km away", Toast.LENGTH_LONG).show();

                                destChosen = true;
                                //ToDo fix this
                               chosenBike =bikes.get(finalI-1);

                               /// map.getOverlays().add(roadOverlay);
                               /// biketemp = roadOverlay;

                                map.invalidate();

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            return false;
                        }
                    });
                }
                map.invalidate();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null){
            if (intentResult.getContents() == null){
                Toast.makeText(chooseBike_activity.this, "Scanning failed\nTry again!", Toast.LENGTH_SHORT).show();
                //textView.setText("Cancelled");
            }else {
                QRcodeResult = intentResult.getContents();
                if(QRcodeResult.equals(arr[1])){
                  //  Toast.makeText(chooseBike_activity.this, "true "+arr[1], Toast.LENGTH_SHORT).show();

                    Toast.makeText(chooseBike_activity.this, "Bike is temporarily linked to you", Toast.LENGTH_SHORT).show();

                    newBike[0] = FetchedEmail;
                    newBike[1] = arr[0];

                    unlockBike ub = new unlockBike();
                    ub.doInBackground();

                    updateBikeName ubn = new updateBikeName();
                    ubn.doInBackground();


                }else{
                    Toast.makeText(chooseBike_activity.this, "false "+arr[1], Toast.LENGTH_SHORT).show();
                }
                //textView.setText(intentResult.getContents());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
				HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/updatetempbikeid");

				String email = newBike[0];
				String tempBikeID = newBike[1];

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("email", email));
				nameValuePairs.add(new BasicNameValuePair("tempBikeID", tempBikeID));
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

            if((boolean)getIntent().getSerializableExtra("workout")){
                Intent intent = new Intent(chooseBike_activity.this, workoutRide2_activity.class);

                switch((String)getIntent().getSerializableExtra("type")){

                    case "calories":
                        intent.putExtra("shared",getIntent().getSerializableExtra("shared"));

                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                        intent.putExtra("weight", (String) getIntent().getSerializableExtra("weight"));
                        intent.putExtra("height", (String) getIntent().getSerializableExtra("height"));
                        intent.putExtra("calories", (String) getIntent().getSerializableExtra("calories"));
                        break;

                    case "distance":
                        intent.putExtra("shared",getIntent().getSerializableExtra("shared"));

                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                        intent.putExtra("meter", (String) getIntent().getSerializableExtra("meter"));
                        break;

                    case "time":
                        intent.putExtra("shared",getIntent().getSerializableExtra("shared"));

                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                        intent.putExtra("hour", (String) getIntent().getSerializableExtra("hour"));
                        intent.putExtra("minute", (String) getIntent().getSerializableExtra("minute"));
                        intent.putExtra("second", (String) getIntent().getSerializableExtra("second"));
                        break;

                    case "freestyle":
                        intent.putExtra("shared",getIntent().getSerializableExtra("shared"));

                        intent.putExtra("type", (String) getIntent().getSerializableExtra("type"));
                        break;
                }
                startActivity(intent);
            }else {
                Intent intent = new Intent(chooseBike_activity.this, ride_activity.class);

                intent.putExtra("shared",true);

                intent.putExtra("dest", (Parcelable) getIntent().getSerializableExtra("dest"));
                startActivity(intent);
            }

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


    //end

    private void displayRoute(){
        try{

            RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

            ArrayList<GeoPoint> waypoints = new ArrayList();
            waypoints.add(currentLocation);
            waypoints.add(new GeoPoint(31.98908,31.08986));

            Road road = roadManager.getRoad(waypoints);
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road, R.color.blue, 17);

            map.getOverlays().add(roadOverlay);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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
        chooseBike_activity.LocationBroadcastReceiver receiver = new chooseBike_activity.LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACT_LOC");
        registerReceiver(receiver, filter);
        Intent intent = new Intent(chooseBike_activity.this, LocationService.class );
        startService(intent);
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals( "ACT_LOC")){
                //  double lat = intent.getDoubleExtra("latitude",0);
                //  double lng = intent.getDoubleExtra("longitude",0);

                myx = intent.getDoubleExtra("latitude",0);
                myy = intent.getDoubleExtra("longitude",0);
                currentLocation.setLatitude(myx);
                currentLocation.setLongitude(myy);

                if(!bikesLocated){

                    map.setMultiTouchControls(true);
                    IMapController mapController = map.getController();

                    mapController.setZoom(11.0);

                    url = getResources().getString(R.string.ngrok2) + "/bikes";

                    locateRealBikes lrb = new locateRealBikes();
                    lrb.doInBackground();
                    //locateBikes();

                    //  Toast.makeText(chooseDestination3_activity.this, "myx is: "+myx+ " myy is: "+myy, Toast.LENGTH_SHORT).show();
                    mapController.setCenter(currentLocation);

                    bikesLocated = true;
                }

                markMyLocation();

                //  Toast.makeText(ride_activity.this, "Latitude is: "+myx + " Longitude is: "+myy, Toast.LENGTH_LONG).show();
            }
        }
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


    public class BarcodeEncoder {
        private static final int WHITE = 0xFFFFFFFF;
        private static final int BLACK = 0xFF000000;

        public BarcodeEncoder() {
        }

        public Bitmap createBitmap(BitMatrix matrix) {
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        }

        public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
            try {
                return new MultiFormatWriter().encode(contents, format, width, height);
            } catch (WriterException e) {
                throw e;
            } catch (Exception e) {
                // ZXing sometimes throws an IllegalArgumentException
                throw new WriterException(e);
            }
        }

        public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
            try {
                return new MultiFormatWriter().encode(contents, format, width, height, hints);
            } catch (WriterException e) {
                throw e;
            } catch (Exception e) {
                throw new WriterException(e);
            }
        }

        public Bitmap encodeBitmap(String contents, BarcodeFormat format, int width, int height) throws WriterException {
            return createBitmap(encode(contents, format, width, height));
        }

        public Bitmap encodeBitmap(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
            return createBitmap(encode(contents, format, width, height, hints));
        }
    }

    static String getAlphaNumericString(int n) {

        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
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
            channel.setCommand("cd /home/pi/.local/bin; python3 qr.py "+random+"; fim -a myQR.png");
            channel.connect();

            Snackbar.make(this.findViewById(android.R.id.content),
                    "Success!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            channel.disconnect();
        }
        catch(JSchException e) {

            Snackbar.make(this.findViewById(android.R.id.content),
                    "Check WIFI or Server! Error : " + e.getMessage(),
                    Snackbar.LENGTH_LONG)
                    .setDuration(20000).setAction("Action", null).show();
        } /*catch (IOException e) {
            e.printStackTrace();
        */
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


    public class sendCommand extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/updateCommand");

                String Name = arr[0];
                String Command = arr[1];

                Log.d("y3nieh", arr[0]+" lol "+arr[1]);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("Name", Name));
                nameValuePairs.add(new BasicNameValuePair("Command", Command));
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
            scanDialog();
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
                nameValuePairs.add(new BasicNameValuePair("Name",arr[0] ));
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


}
