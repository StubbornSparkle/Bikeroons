package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class rideSummary_activity extends Activity {

    private Button done;

    private SharedPreferences sp;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private Button close;

    private MapView map;
    private IMapController mapController;
    private LinearLayout l;

    private String url;
    private String FetchedEmail;

    private ArrayList<Ride> rides = new ArrayList<>();

    private String fullRes;

    private int getThis;
    private HttpResponse remresponse;

    private String[] params = new String[2];
    private TextView time, start, endD, number;
    private RoadManager roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

    private int thisone;
    private Ride garabRide;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongThread")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ridesummary);

        l = (LinearLayout) findViewById(R.id.linearLayout);
        done = (Button) findViewById(R.id.done);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(rideSummary_activity.this, start_activity.class));
            }
        });

        sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        FetchedEmail = sp.getString("email","");
        params[0] = FetchedEmail;

        url = getResources().getString(R.string.ngrok)+"/users/getrides/"+FetchedEmail;

      //  YourAsyncTask yat = new YourAsyncTask(rideSummary_activity.this);
      //  yat.execute();

        getRides gr = new getRides();
        gr.execute();
    }




    private class mapSetup extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public mapSetup(rideSummary_activity activity) {
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
                }
            });
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




    public void rideDialog(int a) {
        dialogBuilder = new AlertDialog.Builder(this);

        final View popupView = getLayoutInflater().inflate(R.layout.ridepopup, null);

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
        mapController = map.getController();

        mapSetup ms = new mapSetup(rideSummary_activity.this);
        ms.execute();

        try{

           // Ride temp = null;

            //int i=0;
            for(; thisone<rides.size(); thisone++){
                if(rides.get(thisone).getRideNo() == a){
                    //temp = rides.get(thisone);
                    garabRide = rides.get(thisone);
                    break;
                }
            }

            time = (TextView) popupView.findViewById(R.id.time);
            start = (TextView) popupView.findViewById(R.id.start);
            endD = (TextView) popupView.findViewById(R.id.endD);
            number = (TextView) popupView.findViewById(R.id.number);

            String startStuff = garabRide.getStartDate() +" "+ garabRide.getStartTime();
            String endStuff = garabRide.getEndDate() +" "+ garabRide.getEndTime();
            time.setText(findDifference(startStuff, endStuff));
            start.setText(startStuff);
            endD.setText(endStuff);
            number.setText((a)+"");
            /*
            waypoints = new ArrayList();

            for (int j = 0; j < temp.getHistory().size(); j++) {
                waypoints.add(new GeoPoint(temp.getHistory().get(j)[0], temp.getHistory().get(j)[1]));
            }*/

            /*if(!temp.getHistory().isEmpty()) {
                mapController.setCenter(new GeoPoint(temp.getHistory().get(0)[0], temp.getHistory().get(0)[1]));
            }*/

            getThis = thisone;

            YourAsyncTask2 yat2 = new YourAsyncTask2(rideSummary_activity.this);
            yat2.execute();

         //   getRoutes gr = new getRoutes();
         //   gr.doInBackground();

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        map.invalidate();

        close = (Button) popupView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    static String findDifference(String start_date,String end_date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        try {
            java.util.Date d1 = sdf.parse(start_date);
            java.util.Date d2 = sdf.parse(end_date);
            long difference_In_Time
                    = d2.getTime() - d1.getTime();
            long difference_In_Minutes
                    = (difference_In_Time
                    / (1000 * 60))%60;

            long difference_In_Hours
                    = (difference_In_Time
                    / (1000 * 60 *60))%60;

            return(difference_In_Hours +":"+ difference_In_Minutes+ "");
        }
        // Catch the Exception
        catch (ParseException e) {
            e.printStackTrace();
        }
        return"";
    }

    private class getRoutes extends AsyncTask<String, Void, String> {
        //JSONArray msg;
        @Override
        protected String doInBackground(String... strings) {
            RoadManager roadManager2 = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");

            ArrayList<GeoPoint> newest = new ArrayList<>();

            Log.d("getthis", "ekfowekfo lol "+garabRide.getRideNo());
            //Ride temp = rides.get(thisone);

            for (int i = 0; i < garabRide.getHistory().size(); i++) {

                newest.add(new GeoPoint(garabRide.getHistory().get(i)[0], garabRide.getHistory().get(i)[1]));
            }

            if(!garabRide.getHistory().isEmpty()) {
                mapController.setCenter(new GeoPoint(garabRide.getHistory().get(0)[0], garabRide.getHistory().get(0)[1]));
            }
            mapController.setZoom(15.0);


            try{
                Road road = roadManager2.getRoad(newest);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getResources().getColor(R.color.purple), 17);

                map.getOverlays().add(roadOverlay);
                map.invalidate();
            }catch(Exception e){
                e.printStackTrace();
            }

            thisone=0;
           // onPostExecute("");

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(rideSummary_activity.this, start_activity.class));
    }



    private class getRides extends AsyncTask<String, Void, String> {

        //JSONArray msg;
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {

            rides.clear();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try(Response responsee = client.newCall(request).execute()){

                fullRes = responsee.body().string();

                try {
                    JSONObject myObject = new JSONObject(fullRes);
                    Iterator y = myObject.keys();
                    JSONArray myArray = new JSONArray();

                    while (y.hasNext()) {
                        String key = (String) y.next();
                        myArray.put(myObject.get(key));
                    }

                    for (int i = 0; i < myArray.length(); i++) {

                        JSONArray x = myArray.getJSONArray(i);
                        for (int j = 0; j < x.length(); j++) {

                            JSONArray fArr = x.getJSONArray(j);
                            Ride ride = new Ride();
                            ArrayList<double[]> fetchedHistory = new ArrayList<>();

                            ride.setRideNo(Integer.parseInt(fArr.getJSONObject(0).get("rideNo").toString()));

                            String hist = fArr.getJSONObject(1).get("history").toString();

                            ArrayList<Double> lol = new ArrayList();

                            String[] test = hist.split(",");

                            for(int k=0; k<test.length; k++) {
                                if(k%2==0) {
                                    if(test[k].charAt(0)=='[') {
                                        lol.add(Double.parseDouble(test[k].substring(2)));
                                    }else {
                                        lol.add(Double.parseDouble(test[k].substring(1)));
                                    }
                                }else {
                                    if(test[k].charAt(test[k].length()-1)=='}') {
                                        lol.add(Double.parseDouble(test[k].substring(0,test[k].length()-1)));
                                    }else if(test[k].charAt(test[k].length()-1)==']'){
                                        lol.add(Double.parseDouble(test[k].substring(0,test[k].length()-2)));
                                    }
                                }
                            }
                            for(int k=0; k<(lol.size());k=k+2){
                                fetchedHistory.add(new double[] {lol.get(k), lol.get(k+1)});
                            }

                            // Log.d("finally",fetchedHistory.size()+"\n");
                            ride.setHistory(fetchedHistory);
                            ride.setStartDate(fArr.getJSONObject(2).get("startDate").toString());
                            ride.setEndDate(fArr.getJSONObject(3).get("endDate").toString());
                            ride.setStartTime(fArr.getJSONObject(4).get("startTime").toString());
                            ride.setEndTime(fArr.getJSONObject(5).get("endTime").toString());

                            rides.add(ride);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String result) {
        }
    }


    private class YourAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public YourAsyncTask(rideSummary_activity activity) {
            loading3 = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading3.setMessage("Deleting ride, please wait...");
            loading3.show();
        }
        @Override
        protected Void doInBackground(Void... args) {

            removeRide rr = new removeRide();
            rr.doInBackground();
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

    private class YourAsyncTask2 extends AsyncTask<Integer, Void, Void> {
        private ProgressDialog loading3;

        public YourAsyncTask2(rideSummary_activity activity) {
            loading3 = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            getRoutes gr = new getRoutes();
            gr.doInBackground();

            return null;
        }

        @Override
        protected void onPreExecute() {
            loading3.setMessage("Fetching your ride, please wait...");
            loading3.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (loading3.isShowing()) {
                loading3.dismiss();

            }
        }
    }


    public class removeRide extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try{

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/removeride");

                String email = params[0];
                String rideNo = params[1];

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", email));
                nameValuePairs.add(new BasicNameValuePair("rideNo", rideNo));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                remresponse = httpclient.execute(httppost);

                //Toast.makeText(register_activity.this, "lol "+response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
            } catch(ClientProtocolException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String result) {
            if (remresponse != null && remresponse.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {
                Toast.makeText(rideSummary_activity.this, "Ride could not be removed!", Toast.LENGTH_SHORT).show();

            } else if (remresponse != null && remresponse.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {
                Toast.makeText(rideSummary_activity.this, "Ride removed successfully!", Toast.LENGTH_SHORT).show();

                l.removeAllViews();
                getRides gr = new getRides();
                gr.execute();

            }
        }
    }

    public class Ride {
        private int RideNo;
        private ArrayList<double[]> history = new ArrayList<>();
        private String startDate;
        private String endDate;
        private String startTime;
        private String endTime;

        private Button show;
        private Button remove;

        private Ride(){
        }

        public void setRideNo(int rideNo){
            this.RideNo = rideNo;
        }
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        public void setEndDate(String endDate){
            this.endDate = endDate;
        }
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        public void setEndTime(String endTime){
            this.endTime = endTime;

            show = new Button(getApplicationContext());
            remove = new Button(getApplicationContext());

            show.setText("Ride "+(getRideNo())+":     at "+getStartTime()+"     on "+getStartDate());
            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    rideDialog(getRideNo());
                }
            });

            remove.setText("Remove ^");
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    params[1] = getRideNo()+"";
                    YourAsyncTask yat = new YourAsyncTask(rideSummary_activity.this);
                    yat.execute();
                   // removeRide rr = new removeRide();
                  //  rr.doInBackground();
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    l.addView(getShow());
                    l.addView(getRemove());
                }
            });
        }

        public void setHistory(ArrayList history){
            this.history = history;
        }

        //_________________________________________________
        public ArrayList<double[]> getHistory(){
            return this.history;
        }

        public int getRideNo(){
            return this.RideNo;
        }
        public String getStartDate(){
            return this.startDate;
        }
        public String getEndDate(){
            return this.endDate;
        }
        public String getStartTime(){
            return this.startTime;
        }
        public String getEndTime(){
            return this.endTime;
        }
        public Button getShow(){
            return this.show;
        }
        public Button getRemove(){
            return this.remove;
        }
//        public ArrayList getHistory(ArrayList history){
  //          return this.history;
    //    }


    }

}