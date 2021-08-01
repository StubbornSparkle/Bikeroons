package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class endRide_activity extends Activity {

    private MapView map;
    private IMapController mapController;

    private int numofrides;

    private Object[] historyArr;

    private ArrayList<double[]> history = new ArrayList<>();

    private Button end, resume;

    private TextView start,endD, time, kms;

    private GeoPoint currentLocation = new GeoPoint(0.0,0.0);

    private String[] params = new String[6];
    private SharedPreferences sp;
    private String FetchedEmail;

    private HttpResponse response;
    //private Array history;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        FetchedEmail = sp.getString("email","");
        setContentView(R.layout.endride);

        end = (Button) findViewById(R.id.end);
        resume = (Button) findViewById(R.id.resume);
        map = (MapView) findViewById(R.id.map);
        kms = (TextView) findViewById(R.id.kms);
        time = (TextView) findViewById(R.id.time);
        start = (TextView) findViewById(R.id.start);
        endD = (TextView) findViewById(R.id.endD);

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
        mapController.setZoom(11.0);

        //time.setText();
        start.setText(getIntent().getSerializableExtra("startDate")+"\n"+getIntent().getSerializableExtra("startTime"));
        endD.setText(getIntent().getSerializableExtra("endDate")+"\n"+getIntent().getSerializableExtra("endTime"));

        DecimalFormat df = new DecimalFormat("#.###");


        kms.setText(df.format(getIntent().getSerializableExtra("kiloms"))+"");

        historyArr = (Object[]) getIntent().getSerializableExtra("history");
        for(int i=0; i<historyArr.length;i++){
            double[] x ={((double[])historyArr[i])[0], ((double[])historyArr[i])[1]};
            history.add(x);
        }


        //time.setText(":3gh"+findDifference(getIntent().getSerializableExtra("startDate")+" "+getIntent().getSerializableExtra("startTime"), getIntent().getSerializableExtra("endDate")+" "+getIntent().getSerializableExtra("endTime")));
        time.setText(""+getIntent().getSerializableExtra("time"));

        //dest = (GeoPoint) getIntent().getSerializableExtra("dest");

        currentLocation.setLatitude(history.get(0)[0]);
        currentLocation.setLongitude(history.get(0)[1]);


        YourAsyncTask yat = new YourAsyncTask(endRide_activity.this);
        yat.execute();

        //displayRoute dr = new displayRoute();
        //dr.doInBackground();


        try{
        }catch(Exception e){
            e.printStackTrace();
        }

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String full = "[";
                for(int i=0; i<history.size(); i++){
                    if(i == history.size()-1)
                        full+="{"+history.get(i)[0]+","+history.get(i)[1]+"}]";
                    else
                        full+="{"+history.get(i)[0]+","+history.get(i)[1]+"},";
                }

                params[0] = FetchedEmail;
                params[1] = full;
                params[2] = getIntent().getSerializableExtra("startDate")+"";
                params[3] = getIntent().getSerializableExtra("endDate")+"";
                params[4] = getIntent().getSerializableExtra("startTime")+"";
                params[5] = getIntent().getSerializableExtra("endTime")+"";


                CreateRide cr = new CreateRide();
                cr.doInBackground();

            }
        });
    }

    static String findDifference(String start_date,String end_date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy\nhh:mm");
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
        catch (ParseException e) {
            e.printStackTrace();
        }
        return"";
    }


    public class displayRoute extends AsyncTask<String, String, String> {

        RoadManager roadManager;
        ArrayList<GeoPoint> waypoints;
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try{
                roadManager = new MapQuestRoadManager("HGszeMYhLeGPs4wvGGtgl8ARKifFAjDX");
                waypoints = new ArrayList();

                for(int i=0; i<history.size(); i++){
                    waypoints.add(new GeoPoint(history.get(i)[0], history.get(i)[1]));
                }

                Road road = roadManager.getRoad(waypoints);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getResources().getColor(R.color.blue), 17);

                map.getOverlays().add(roadOverlay);
                mapController.setCenter(currentLocation);
                map.invalidate();

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {

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

                onPostExecute("");
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
            startActivity(new Intent(endRide_activity.this, rideSummary_activity.class));

        }
    }


    private class YourAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public YourAsyncTask(endRide_activity activity) {
            loading3 = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading3.setMessage("Loading ride, please wait...");
            loading3.show();
        }
        @Override
        protected Void doInBackground(Void... args) {

            displayRoute dr = new displayRoute();
            dr.doInBackground();


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

}
