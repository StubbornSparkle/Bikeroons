package exportkit.xd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//TODO:
// Display whether the bike is in use or not
// View the live bike route
// Notify the user that their bike is being rented
// End the bike renting
// Optional: send message to the user renting the bike

public class rentBike_activity extends Activity {

    private MapView map;
    private IMapController mapController;
    private GeoPoint myBikeLoc = new GeoPoint(0.0,0.0), currentLocation = new GeoPoint(0.0,0.0);
    private String myBike;
    private String fetchedBikeID="";

    private SharedPreferences sp;
    private String FetchedEmail;

    private HttpResponse response;
    private Button test;
    private boolean locateMyBike= false;
    private int mapSetupDone = 0;
    private Marker tempMarker;

    public void onBackPressed() {
        mapSetupDone = 0;
        stopRenting sr = new stopRenting(rentBike_activity.this);
        sr.execute();

        startActivity(new Intent(rentBike_activity.this, choice_activity.class));
        //super.onBackPressed();
        //stopService();
    }


    public void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rentbike);

       /* if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {

                startService();
            }
        } else {
            startService();
        }*/

        sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        FetchedEmail = sp.getString("email","");

        map = (MapView) findViewById(R.id.map);

        getUserBike gub = new getUserBike();
        gub.execute();


        final Handler ha=new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                //call function
                if(locateMyBike){
                    locateBike lb = new locateBike();
                    lb.execute();
                }
                ha.postDelayed(this, 3000);
            }
        }, 1000);
    }



    /*private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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
    }*/


    public class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACT_LOC")) {

                currentLocation.setLatitude(intent.getDoubleExtra("latitude", 0));
                currentLocation.setLongitude(intent.getDoubleExtra("longitude", 0));
            }
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
                myBike = responsee.body().string();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
           // locateBike lb = new locateBike();
            //lb.execute();

            locateMyBike = true;

            //locate.setVisibility(View.VISIBLE);
            //locateBike lb = new locateBike();
            //lb.doInBackground();
        }
    }


    private class mapSetup extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public mapSetup(rentBike_activity activity) {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mapController = map.getController();
                    mapController.setZoom(15.0);

                    Marker spot = new Marker(map);

                    tempMarker = spot;

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
                }
            });
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (loading3.isShowing()) {

                loading3.dismiss();
                locateMyBike = true;
            }
        }
    }


    public class locateBike extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {

                locateMyBike = false;

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/getbike");

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                JSONObject obj  = new JSONObject(myBike);
                String bikeName = obj.getString("message");

                nameValuePairs.add(new BasicNameValuePair("Name", bikeName));
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

                if (jsonArray.getJSONObject(0) != null) {

                    JSONObject jo2 = (JSONObject) jsonArray.getJSONObject(0);

                    double[] location = convert(36, Double.parseDouble(jo2.get("East") + ""), Double.parseDouble(jo2.get("North") + ""), true);


                    Log.d("ngarabyalla","THIS SHOULD CHANGE "+location[0]+", "+location[1]);
                    myBikeLoc = new GeoPoint(location[0], location[1]);

                } else {
                    Toast.makeText(rentBike_activity.this, "An error has occurred!\nTry again", Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(mapSetupDone < 1){
                mapSetupDone++;
                mapSetup ms = new mapSetup(rentBike_activity.this);
                ms.execute();
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        map.getOverlays().remove(tempMarker);

                        Marker spot = new Marker(map);

                        tempMarker = spot;

                        spot.setPosition(myBikeLoc);
                        spot.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        spot.setTitle("Your bike is here");
                        spot.setPanToView(true);

                        spot.setIcon(getResources().getDrawable(R.drawable.ic_bikeloc));

                        map.getOverlays().add(spot);
                        map.invalidate();
                    }
                });

                locateMyBike = true;
            }
            //HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
        }

    }


 /*   void startService(){
        try{
            LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
            IntentFilter filter = new IntentFilter("ACT_LOC");
            registerReceiver(receiver, filter);
            Intent intent = new Intent(rentBike_activity.this, LocationService.class );
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
            Intent intent = new Intent(rentBike_activity.this, LocationService.class );
            stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/




    private class stopRenting extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading;

        public stopRenting(rentBike_activity activity) {
            loading = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading.setMessage("Loading, please wait...");
            loading.show();
        }
        @Override
        protected Void doInBackground(Void... args) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok2) + "/bikes/stopShareBike");


                JSONObject obj = new JSONObject(myBike);
                String bikeName = obj.getString("message");


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("Name", bikeName));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);

            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (loading.isShowing()) {

                //locateMyBike = true;
                loading.dismiss();
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


}


//back = (String)getIntent().getSerializableExtra("back");