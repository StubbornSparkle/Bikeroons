package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class forgotPassword_activity extends Activity {

    private HttpResponse response;
    private EditText enteredEmail, enteredCode;
    //private TextView timer;
    private Button send, checkCode;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private Date startTime, currentTime;
    private boolean count = false;
    private boolean wait = false;
    private TextView timenstuff, texthna;

    public void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);

        enteredEmail = (EditText) findViewById(R.id.email);
        enteredCode = (EditText) findViewById(R.id.enteredCode);

        send = (Button) findViewById(R.id.send);
        checkCode = (Button) findViewById(R.id.checkCode);

        timenstuff = (TextView) findViewById(R.id.timenstuff);
        texthna = (TextView) findViewById(R.id.texthna);
        //enteredCode.setVisibility(View.GONE);
        //checkCode.setVisibility(View.GONE);

        timenstuff.setVisibility(View.GONE);
        texthna.setVisibility(View.GONE);

        sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        editor = sp.edit();

        enteredEmail.setText(sp.getString("email",""));

        checkCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(enteredCode.getText().toString().equals("")){
                    enteredCode.setError("You must enter the sent code");
                }else {
                    checkCode cc = new checkCode(forgotPassword_activity.this);
                    cc.execute();
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enteredEmail.getText().toString().equals("")) {
                    enteredEmail.setError("Please enter an email");
                }else{
                    sendCode sc = new sendCode();
                    sc.execute();
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(count){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timenstuff.setVisibility(View.VISIBLE);
                                texthna.setVisibility(View.VISIBLE);
                            }
                        });


                        SimpleDateFormat format1=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date backup = Calendar.getInstance().getTime();
                        //Log.d("timehna2", format1.format(currentTime));

                        editor.putString("timestamp",format1.format(startTime));

                        findDifference(sp.getString("timestamp",format1.format(backup)), format1.format(backup));
                    }
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        removeCode rc = new removeCode();
        rc.execute();
        startActivity(new Intent(forgotPassword_activity.this, signin_activity.class));
    }


    public class sendCode extends AsyncTask<String, String, String> {
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {

                editor.putString("email", enteredEmail.getText().toString());
                editor.putString("code", enteredCode.getText().toString());
                editor.apply();
                editor.commit();

                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/forgotpassword");

                String email = enteredEmail.getText().toString().toLowerCase();

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", email));
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
            if(response.getStatusLine().toString().equals("HTTP/1.1 200 OK")){

                Toast.makeText(forgotPassword_activity.this,"A Code has been sent to entered E-email", Toast.LENGTH_SHORT).show();
                enteredCode.setVisibility(View.VISIBLE);
                checkCode.setVisibility(View.VISIBLE);

                startTime = Calendar.getInstance().getTime();
                SimpleDateFormat format1=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                //Log.d("timehna2", format1.format(currentTime));
                editor.putString("timestamp",format1.format(startTime));
                editor.apply();
                editor.commit();

                count = true;

                // onStart();

                /*new CountDownTimer(59000, 1000) {

                    public void onTick(long millisUntilFinished) {

                        timer.setText((millisUntilFinished / 1000)+"");
                    }
                    public void onFinish() {
                        send.setText("Resend Code");
                    }
                }.start();*/
            }else{
                Toast.makeText(forgotPassword_activity.this,"An error has occurred, try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void findDifference(String start_date, String end_date){

        // SimpleDateFormat converts the
        // string format to date object
        SimpleDateFormat sdf
                = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss");

        // Try Block
        try {

            // parse method is used to parse
            // the text from a string to
            // produce the date
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);

            // Calucalte time difference
            // in milliseconds
            long difference_In_Time
                    = d2.getTime() - d1.getTime();

            // Calucalte time difference in
            // seconds, minutes, hours, years,
            // and days
            long difference_In_Seconds
                    = (difference_In_Time
                    / 1000)
                    % 60;

            long difference_In_Minutes
                    = (difference_In_Time
                    / (1000 * 60))
                    % 60;

            long difference_In_Hours
                    = (difference_In_Time
                    / (1000 * 60 * 60))
                    % 24;

            long difference_In_Years
                    = (difference_In_Time
                    / (1000l * 60 * 60 * 24 * 365));

            long difference_In_Days
                    = (difference_In_Time
                    / (1000 * 60 * 60 * 24))
                    % 365;

            // Print the date difference in
            // years, in days, in hours, in
            // minutes, and in seconds

            if(difference_In_Years == 0 && difference_In_Days == 0 && difference_In_Hours == 0 && difference_In_Minutes < 2){
                String current ="";
                if(difference_In_Seconds<10){
                    current = "0"+difference_In_Minutes+":0"+difference_In_Seconds;
                }else{
                    current = "0"+difference_In_Minutes+":"+difference_In_Seconds;
                }

                findMinuteDifference(current,"02:00");
            }
        //    Log.d("testinr", difference_In_Minutes+":"+difference_In_Seconds);
           /* System.out.print(
                    "Difference "
                            + "between two dates is: ");

            System.out.println(
                    difference_In_Years
                            + " years, "
                            + difference_In_Days
                            + " days, "
                            + difference_In_Hours
                            + " hours, "
                            + difference_In_Minutes
                            + " minutes, "
                            + difference_In_Seconds
                            + " seconds");*/
        }


        // Catch the Exception
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void findMinuteDifference(String start_date, String end_date){

        // SimpleDateFormat converts the
        // string format to date object
        SimpleDateFormat sdf
                = new SimpleDateFormat(
                "mm:ss");

        // Try Block
        try {

            // parse method is used to parse
            // the text from a string to
            // produce the date
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);

            // Calucalte time difference
            // in milliseconds
            long difference_In_Time
                    = d2.getTime() - d1.getTime();

            // Calucalte time difference in
            // seconds, minutes, hours, years,
            // and days
            long difference_In_Seconds
                    = (difference_In_Time
                    / 1000)
                    % 60;

            long difference_In_Minutes
                    = (difference_In_Time
                    / (1000 * 60))
                    % 60;

            long difference_In_Hours
                    = (difference_In_Time
                    / (1000 * 60 * 60))
                    % 24;

            long difference_In_Years
                    = (difference_In_Time
                    / (1000l * 60 * 60 * 24 * 365));

            long difference_In_Days
                    = (difference_In_Time
                    / (1000 * 60 * 60 * 24))
                    % 365;

            // Print the date difference in
            // years, in days, in hours, in
            // minutes, and in seconds

            if(difference_In_Minutes==0 && difference_In_Seconds < 2){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        texthna.setVisibility(View.GONE);
                        timenstuff.setVisibility(View.GONE);
                    }
                });

                count = false;
                editor.putString("timestamp","");
                editor.apply();
                editor.commit();

                removeCode rc = new removeCode();
                rc.execute();
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String current ="";
                        if(difference_In_Seconds<10){
                            current = "0"+difference_In_Minutes+":0"+difference_In_Seconds;
                        }else{
                            current = "0"+difference_In_Minutes+":"+difference_In_Seconds;
                        }
                        timenstuff.setText(current);
                       // timenstuff.setText(difference_In_Minutes + ":" + difference_In_Seconds);
                    }
                });
            }
          //  Log.d("testinr", difference_In_Minutes+":"+difference_In_Seconds);
           /* System.out.print(
                    "Difference "
                            + "between two dates is: ");

            System.out.println(
                    difference_In_Years
                            + " years, "
                            + difference_In_Days
                            + " days, "
                            + difference_In_Hours
                            + " hours, "
                            + difference_In_Minutes
                            + " minutes, "
                            + difference_In_Seconds
                            + " seconds");*/
        }


        // Catch the Exception
        catch (ParseException e) {
            e.printStackTrace();
        }
    }



    public class removeCode extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/removecode");

                String email = enteredEmail.getText().toString().toLowerCase();

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", email));
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(forgotPassword_activity.this,"The code has expired, please request a new one", Toast.LENGTH_LONG).show();
                }
            });
        }
    }



    private class checkCode extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading;

        public checkCode(forgotPassword_activity activity) {
            loading = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading.setMessage("Confirming your entered code, please wait...");
            loading.show();
        }
        @Override
        protected Void doInBackground(Void... args) {
            try {

                /*OkHttpClient client = new OkHttpClient();


                String email = enteredEmail.getText().toString().toLowerCase();
                String code = enteredCode.getText().toString();


                Request request = new Request.Builder().url(getResources().getString(R.string.ngrok) + "/users/checkcode/"+email+"/"+code).build();

                try (Response responsee = client.newCall(request).execute()) {
                    String lol = responsee.body().string();

                    Log.d("lell", lol);
                } catch (Exception e) {
                    e.printStackTrace();
                }
*/

                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/checkcode");

                String email = enteredEmail.getText().toString().toLowerCase();
                String code = enteredCode.getText().toString();


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", email));
                nameValuePairs.add(new BasicNameValuePair("code", code));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (loading.isShowing()) {

                removeCode rc = new removeCode();
                if(response.getStatusLine().toString().equals("HTTP/1.1 200 OK")){

                    editor.putString("email", enteredEmail.getText().toString());
                    editor.putString("code", enteredCode.getText().toString());
                    editor.apply();
                    editor.commit();

                    //rc.execute();
                    startActivity(new Intent(forgotPassword_activity.this, changePassword_activity.class));
                }else{

                    //rc.execute();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(forgotPassword_activity.this, "Code is incorrect, request a new one!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                loading.dismiss();
            }
        }
    }


    /*@Override
    protected void onUserLeaveHint()
    {
        // When user presses home page
        Log.d("mshet", "Home Button Pressed");
        left = true;

        super.onUserLeaveHint();
    }*/


}
