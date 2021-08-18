package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class register_activity extends Activity {

    private Button regBtn;
    private Button iHaveAnAcc;
    private View view;

    private EditText firstNameInput, lastNameInput, emailAddress, password;

    private HttpResponse response, regResponse, valResponse;

    SharedPreferences sp,sp2;
    SharedPreferences.Editor editor;


    private String[] params = new String[5];

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(register_activity.this, signin_activity.class));
    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(internetIsConnected()){

            setContentView(R.layout.register);

            regBtn = (Button) findViewById(R.id.registerBtn);
            iHaveAnAcc = (Button) findViewById(R.id.iHaveAnAcc);

            firstNameInput = (EditText) findViewById(R.id.firstNameInput);
            lastNameInput = (EditText) findViewById(R.id.lastNameInput);
            emailAddress = (EditText) findViewById(R.id.emailAddress);
            password = (EditText) findViewById(R.id.password);

            firstNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(firstNameInput.getText().toString().equals("")){
                        firstNameInput.setError("Must not be empty!");
                    }
                }
            });

            lastNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    if(lastNameInput.getText().toString().equals("")){
                        lastNameInput.setError("Must not be empty!");
                    }
                }
            });

            password.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    if(password.getText().toString().length()<=8 || password.getText().toString().length()>=20){
                        password.setError("Password must be at least 8 characters long and at most 20 characters long!");
                    }
                }
            });

            emailAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            regBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   // RegData pd = new RegData();

                    params[0] = "register";
                    params[1] = firstNameInput.getText().toString();
                    params[2] = lastNameInput.getText().toString();
                    params[3] = emailAddress.getText().toString();
                    params[4] = password.getText().toString();

                    Register r = new Register(register_activity.this);
                    r.execute();
                 //   pd.doInBackground();

                }
            });

            iHaveAnAcc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(register_activity.this, signin_activity.class));
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


    private class Register extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading3;

        public Register(register_activity activity) {
            loading3 = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading3.setMessage("Registering you, please wait...");
            loading3.show();
        }
        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... args) {

            //1
            RegData rd = new RegData();
            rd.execute();
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


    public class RegData extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
            HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/"+params[0]);


            if(params[0].equals("register")){
                try {

                    // Add your data
                    String fname = params[1];
                    String lname = params[2];
                    String email = params[3].toLowerCase();
                    String password = params[4];

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("fname", fname));
                    nameValuePairs.add(new BasicNameValuePair("lname", lname));
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("password", password));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    regResponse = httpclient.execute(httppost);

                    //Toast.makeText(register_activity.this, "lol "+response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(String result){
            if(regResponse != null && regResponse.getStatusLine().toString().equals("HTTP/1.1 200 OK")){
                sp = getSharedPreferences("session", Context.MODE_PRIVATE);
                editor = sp.edit();

                editor.putString("email",emailAddress.getText().toString());
                editor.apply();
                editor.commit();

                startActivity(new Intent(register_activity.this, start_activity.class));
            }else{
                ValData vd = new ValData();
                params[0]= "emailVal";
                vd.execute();
            }

        }
    }

    public class ValData extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
            HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/"+params[0]);

            if(params[0].equals("emailVal")){
                try{
                    String email = emailAddress.getText().toString();

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    valResponse = httpclient.execute(httppost);

                    //    Toast.makeText(register_activity.this, "lol "+response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(String result) {

            if (valResponse != null && valResponse.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        emailAddress.setError("Your e-mail does not look like an e-mail...");
                    }
                });

            }else if(valResponse != null && valResponse.getStatusLine().toString().equals("HTTP/1.1 200 OK")){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(register_activity.this, valResponse.getStatusLine().toString(), Toast.LENGTH_SHORT);
                    }
                });
                TakenData td = new TakenData();
                params[0] = "checkemail";
                td.execute();
            }
        }
    }


    public class TakenData extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
            HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/"+params[0]);

                try{
                    String email = emailAddress.getText().toString();

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    response = httpclient.execute(httppost);

                    //    Toast.makeText(register_activity.this, "lol "+response.getStatusLine().toString(), Toast.LENGTH_LONG).show();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (response != null && response.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        emailAddress.setError("This e-mail is taken!");
                    }
                });
            }

        }
    }


}
