package exportkit.xd;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.graphics.drawable.Drawable;
import android.location.Location;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class chooseDestination_activity extends  AppCompatActivity {

    private GeoPoint dest;

    private MapView map;
    private Marker temp;

    private Marker myLocTemp;

    private Marker mytemp;
    private Polyline desttemp;

    private Button myLoc;

    private Button start;

    private Button routeBtn;

    private boolean destChosen = false;

    private double myx;
    private double myy;
    private double distance;

    private boolean shared;

    private View view;

    private View loading;
    private BlurView blur;
    private ArrayList<GeoPoint> waypoints = new ArrayList<>();


    private IMapController mapController;

    private GeoPoint startPoint = new GeoPoint(myx, myy);

    private boolean done;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(chooseDestination_activity.this, start_activity.class));
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

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
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
    }

    void startService(){
        try{
            LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
            IntentFilter filter = new IntentFilter("ACT_LOC");
            registerReceiver(receiver, filter);
            Intent intent = new Intent(chooseDestination_activity.this, LocationService.class );
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals( "ACT_LOC")){

                myx = intent.getDoubleExtra("latitude",0);
                myy = intent.getDoubleExtra("longitude",0);

                try {
                    markMyLocation();
                    if(myx!= 0.0 && !done){
                        done = true;
                        mapController.setCenter(new GeoPoint(myx, myy));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                // Toast.makeText(chooseDestination3_activity.this, "Latitude is: "+myx + " Longitude is: "+myy, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        if(isInternetAvailable()){
            setContentView(R.layout.choosedestination);

            statusCheck();

            blur = (BlurView) findViewById(R.id.blur);
            //blurBackground();

            //blur.setVisibility(View.GONE);

            loading = (View) findViewById(R.id.loading);
            //loading.setVisibility(View.GONE);

            loading.setVisibility(View.GONE);
            blur.setVisibility(View.GONE);

            map = (MapView) findViewById(R.id.map);
            mapController = map.getController();


            if(Build.VERSION.SDK_INT >= 23){
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1 );
                }else{
                    startService();
                }
            }else{
                startService();
            }


            try {
                shared = (boolean) getIntent().getSerializableExtra("shared");
            }catch(Exception e){
                e.printStackTrace();
            }
            //GETTING THE DEVICE LOCATION


            mapSetup ms = new mapSetup(chooseDestination_activity.this);
            ms.execute();

           // createmarker();
            //GETTING THE DEVICE LOCATION

            myLoc = (Button) findViewById(R.id.loc);
           // myLoc.setVisibility(View.GONE);
            myLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(myx == 0.0 || myy == 0.0){
                        Toast.makeText(getApplicationContext(), "Finding your location...", Toast.LENGTH_SHORT).show();
                    }else{
                        map.getController().animateTo(new GeoPoint(myx, myy));

                    }
                }
            });
            //END

            //lol
            routeBtn = (Button) findViewById(R.id.route);
            //routeBtn.setVisibility(View.GONE);
            routeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!destChosen){
                        Toast.makeText(getApplicationContext(), "You haven't picked a destination!", Toast.LENGTH_SHORT).show();
                    }else{
                        try {

                            Toast.makeText(getApplicationContext(), "Showing your route, please wait...", Toast.LENGTH_SHORT).show();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    map.getOverlays().remove(desttemp);

                                    waypoints = new ArrayList<GeoPoint>();
                                    waypoints.add(new GeoPoint(myx, myy));
                                    waypoints.add(dest);

                                    displayRoute dr = new displayRoute();
                                    dr.execute();
                                }
                            }, 100);

                        } catch (Exception e) {

                            Toast.makeText(getApplicationContext(), "Destination unreachable by bike!", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
            });
            //end lol


            start = (Button) findViewById(R.id.start);
           // start.setVisibility(View.GONE);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(destChosen){
                        if(shared){
                            Intent intent = new Intent(chooseDestination_activity.this, chooseBike_activity.class);
                            intent.putExtra("workout", false);
                            intent.putExtra("distance", distance);
                            intent.putExtra("dest", (Parcelable) dest);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(chooseDestination_activity.this, ride2_activity.class);

                            intent.putExtra("shared", false);
                            //  Toast.makeText(chooseDestination3_activity.this, distance+"", Toast.LENGTH_SHORT).show();
                            intent.putExtra("distance", distance);
                            intent.putExtra("dest", (Parcelable) dest);
                            startActivity(intent);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "You haven't picked a destination!", Toast.LENGTH_SHORT).show();
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



    private void markMyLocation(){
        if(map == null) {
            return;
        }
        map.getOverlays().remove(myLocTemp);

        Marker my_marker = new Marker(map);

        my_marker.setPosition(new GeoPoint(myx,myy));
        my_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        my_marker.setTitle("Your location");
        my_marker.setPanToView(true);

        my_marker.setIcon(getResources().getDrawable(R.drawable.ic_mylocation));

        myLocTemp = my_marker;

        map.getOverlays().add(my_marker);

        map.invalidate();
    }

    public void createmarker(){
        if(map == null) {
            return;
        }

        final MapEventsReceiver mReceive = new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {

                map.getOverlays().remove(temp);
                map.getOverlays().remove(desttemp);

                Marker my_marker = new Marker(map);

                my_marker.setPosition(new GeoPoint(p.getLatitude(),p.getLongitude()));
                my_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                my_marker.setTitle("Your chosen destination");
                my_marker.setPanToView(true);

                my_marker.setIcon(getResources().getDrawable(R.drawable.ic_destloc));

                temp = my_marker;

                map.getOverlays().add(my_marker);

                destChosen = true;

                dest = new GeoPoint(p.getLatitude(),p.getLongitude());

                Location locationA = new Location("current");

                locationA.setLatitude(myx);
                locationA.setLongitude(myy);

                Location locationB = new Location("destination");

                locationB.setLatitude(dest.getLatitude());
                locationB.setLongitude(dest.getLongitude());

                DecimalFormat df = new DecimalFormat("#.##");
                distance = locationA.distanceTo(locationB)/1000.0;

                my_marker.setTitle(df.format(distance)+" Km away");

                map.invalidate();
                return false;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mReceive));
    }


    private class displayRoute extends AsyncTask< String, Void, Void > {
        @Override
        protected Void doInBackground(String... strings) {
            try{
                RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");


                Road road = roadManager.getRoad(waypoints);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, R.color.blue, 17);

                desttemp = roadOverlay;

                map.getOverlays().add(roadOverlay);
            }catch(Exception e){
                Toast.makeText(getApplicationContext(), "The location is unreachable", Toast.LENGTH_SHORT);
                e.printStackTrace();
            }

            return null;
        }
    }



    private class mapSetup extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public mapSetup(chooseDestination_activity activity) {
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
            map.setMultiTouchControls(true);
            map.invalidate();
          //  myLoc.setVisibility(myLoc.GONE);

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (loading3.isShowing()) {
                loading3.dismiss();

                map.setMultiTouchControls(true);
              //  IMapController mapController = map.getController();

                mapController.setZoom(11.0);

                //  Toast.makeText(chooseDestination3_activity.this, "myx is: "+myx+ " myy is: "+myy, Toast.LENGTH_SHORT).show();

                waypoints.add(new GeoPoint(myx, myy));
                mapController.setZoom(11.0);

                createmarker();
                map.invalidate();
            }
        }
    }

}
