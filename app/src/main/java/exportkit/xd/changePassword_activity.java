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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class changePassword_activity extends Activity {

    private EditText newPassword, newPasswordAgain;
    private HttpResponse response;

    private Button changePW;

    private SharedPreferences sp;
    private String FetchedEmail, FetchedCode;

    private boolean canChange = false;

    @SuppressLint("WrongThread")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        newPassword = (EditText) findViewById(R.id.newPassword);
        newPasswordAgain = (EditText) findViewById(R.id.newPasswordAgain);
        changePW = (Button) findViewById(R.id.changePW);

        sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        FetchedEmail = sp.getString("email","").toLowerCase();
        FetchedCode = sp.getString("code","");

        changePW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(newPassword.getText().toString().length()<8 || newPassword.getText().toString().length()>20){
                    newPassword.setError("Your password must be 8-20 characters long");
                    canChange = false;
                }
                else if(newPasswordAgain.getText().toString().length()<8 || newPasswordAgain.getText().toString().length()>20){
                    newPasswordAgain.setError("Your password must be 8-20 characters long");
                    canChange = false;
                }
                else if (!newPasswordAgain.getText().toString().equals(newPassword.getText().toString())) {
                    newPasswordAgain.setError("Your password does not match the one above");
                    canChange = false;
                }else{
                    canChange = true;
                }
                if(canChange) {
                    changePW cpw = new changePW(changePassword_activity.this);
                    cpw.execute();
                }
            }
        });

    }



    private class changePW extends AsyncTask<Void, Void, Void> {
        private ProgressDialog loading;

        public changePW(changePassword_activity activity) {
            loading = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            loading.setMessage("Changing your password, please wait...");
            loading.show();
        }
        @Override
        protected Void doInBackground(Void... args) {

            try {

                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/changepwez");

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", FetchedEmail));
                nameValuePairs.add(new BasicNameValuePair("newpassword", newPassword.getText().toString()));
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

                if(response.getStatusLine().toString().equals("HTTP/1.1 200 OK")){

                    startActivity(new Intent(changePassword_activity.this, signin_activity.class));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(changePassword_activity.this, "Your password has been successfully changed!", Toast.LENGTH_LONG).show();
                        }
                    });

                }else{

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(changePassword_activity.this, "An error has occurred with changing your password!", Toast.LENGTH_LONG).show();
                        }
                    });

                }

                loading.dismiss();
            }
        }
    }



}
