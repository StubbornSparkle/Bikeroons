package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class signin_activity extends Activity {

    private Button signinBtn;
    private Button registerBtn;
    private View view;
    private String url,res;

    private HttpResponse response;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private EditText email, password;

    private String[] arr = new String[2];

    private TextView forgot;

   // private String emaill, passwordd;

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        //finish();
        //System.exit(0);
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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if( isInternetAvailable()){

            setContentView(R.layout.signin);

            /*
            Button google = (Button) findViewById(R.id.google);
            google.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(signin_activity.this, googleMaps_activity.class));
                }
            });*/

            signinBtn = (Button) findViewById(R.id.signinBtn);
            registerBtn = (Button) findViewById(R.id.registerBtn);

            email = (EditText) findViewById(R.id.email);
            password = (EditText) findViewById(R.id.password);

            forgot = (TextView) findViewById(R.id.forgot);

//            forgot.setVisibility(View.GONE);
            forgot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(signin_activity.this, forgotPassword_activity.class));
                }
            });


            signinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        arr[0] = email.getText().toString();
                        arr[1] = password.getText().toString();

                       YourAsyncTask yat = new YourAsyncTask(signin_activity.this);
                       yat.execute();

                        //Route r = new Route();
                        //r.doInBackground();

//                        startActivity(new Intent(signin_activity.this, start_activity.class));
                        // PostData pd = new PostData();

                        //pd.doInBackground(inputs);

                        //Toast.makeText(signin_activity.this,  response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
                    }
                });

            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(signin_activity.this, register_activity.class));

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


//    public class Route extends AsyncTask<String, String, String> {
//
//        @SuppressLint("WrongThread")
//        @Override
//        protected String doInBackground(String... strings) {
//            try {
//
//                // Create a new HttpClient and Post Header
//                HttpClient httpclient = new DefaultHttpClient();
//                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
//                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/login");
//
//                String email = arr[0];
//                String password = arr[1];
//
//                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//                nameValuePairs.add(new BasicNameValuePair("email", email));
//                nameValuePairs.add(new BasicNameValuePair("password", password));
//                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//                // Execute HTTP Post Request
//                response = httpclient.execute(httppost);
//
//                onPostExecute("");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//
//            if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(signin_activity.this, "Wrong e-mail or password! ", Toast.LENGTH_LONG).show();
//                    }
//                });
//
//            } else if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {
//
//                sp = getSharedPreferences("session", Context.MODE_PRIVATE);
//                editor = sp.edit();
//
//
//                editor.putString("email", email.getText().toString());
//                editor.apply();
//                editor.commit();
//                url = getResources().getString(R.string.ngrok) + "/users/getname/" + email.getText().toString();
//                startActivity(new Intent(signin_activity.this, start_activity.class));
//
//                startActivity(new Intent(signin_activity.this, start_activity.class));
//            }else if(response != null && response.getStatusLine().toString().equals("HTTP/1.1 502 Bad Gateway")){
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(signin_activity.this, "Bad connection!", Toast.LENGTH_LONG).show();
//                    }
//                });
//            }else if(response != null && response.getStatusLine().toString().equals("HTTP/1.1 404 Not Found")){
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(signin_activity.this, "404 Not Found!", Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//
//        }
//    }


    private class getName extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try (Response responsee = client.newCall(request).execute()) {
                res = responsee.body().string();

                JSONObject names = new JSONObject(res);


                editor.putString("firstName",names.getString("firstName"));
                editor.putString("lastName",names.getString("lastName"));
                editor.apply();
                editor.commit();
                System.out.println(names.getString("firstName") + " " + names.getString("lastName"));

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private class YourAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public YourAsyncTask(signin_activity activity) {
            loading3 = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading3.setMessage("Signing you in, please wait...");
            loading3.show();
        }
        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... args) {

//            Route r = new Route();
//            r.execute();

            try {

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/login");

                String email = arr[0];
                String password = arr[1];

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", email));
                nameValuePairs.add(new BasicNameValuePair("password", password));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(signin_activity.this, "Wrong e-mail or password! ", Toast.LENGTH_LONG).show();
                    }
                });

            } else if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {

                sp = getSharedPreferences("session", Context.MODE_PRIVATE);
                editor = sp.edit();

                url = getResources().getString(R.string.ngrok) + "/users/getname/" + email.getText().toString();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();


                try (Response responsee = client.newCall(request).execute()) {
                    res = responsee.body().string();
                    JSONObject names = new JSONObject(res);
                    editor.putString("firstName",names.getString("firstName"));
                    editor.putString("lastName",names.getString("lastName"));

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                editor.putString("email", email.getText().toString());
                editor.apply();
                editor.commit();

                startActivity(new Intent(signin_activity.this, start_activity.class));

            }else if(response != null && response.getStatusLine().toString().equals("HTTP/1.1 502 Bad Gateway")){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(signin_activity.this, "Bad connection!", Toast.LENGTH_LONG).show();
                    }
                });
            }else if(response != null && response.getStatusLine().toString().equals("HTTP/1.1 404 Not Found")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(signin_activity.this, "404 Not Found!", Toast.LENGTH_LONG).show();
                    }
                });
            }



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
