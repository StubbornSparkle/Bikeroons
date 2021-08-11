package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class forgotPassword_activity extends Activity {

    private HttpResponse response;
    private EditText editText;
    private TextView timer;
    private Button send;


    public void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);

        editText = (EditText) findViewById(R.id.email);
        timer = (TextView) findViewById(R.id.timer);
        send = (Button) findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editText.getText().toString().equals("")) {
                    editText.setError("Please enter an email");
                }
                else{
                    sendCode sc = new sendCode();
                    sc.execute();
                }
            }
        });



    }


    public class sendCode extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {

                // Create a new HttpClient and Post Header


                HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok) + "/users/forgotpassword");

                String email = editText.getText().toString();

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

                new CountDownTimer(59000, 1000) {

                    public void onTick(long millisUntilFinished) {

                        timer.setText((millisUntilFinished / 1000)+"");
                    }
                    public void onFinish() {
                        send.setText("Resend Code");
                    }
                }.start();
            }
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

}
