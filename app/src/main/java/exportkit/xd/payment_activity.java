package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.google.zxing.integration.android.IntentIntegrator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class payment_activity extends Activity {



    private AlertDialog.Builder alertBuilder;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private HttpResponse response;
    private Button scanCode;
    private String random, resultt;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.payment);

        CardForm cardForm = (CardForm) findViewById(R.id.card_form);
        Button buy = (Button)findViewById(R.id.btnBuy);

        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .setup(payment_activity.this);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cardForm.isValid()) {
                    alertBuilder = new AlertDialog.Builder(payment_activity.this);
                    alertBuilder.setTitle("Confirm before starting your ride");
                    alertBuilder.setMessage("Card number: " + cardForm.getCardNumber() + "\n" +
                            "Card expiry date: " + cardForm.getExpirationDateEditText().getText().toString() + "\n" +
                            "Card CVV: " + cardForm.getCvv());
                    alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(payment_activity.this, "Please scan the QR code on the chosen bike", Toast.LENGTH_LONG).show();

                            sendCommand sc = new sendCommand();
                            sc.execute();
                        }
                    });
                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();

                } else {
                    Toast.makeText(payment_activity.this, "Please complete the form", Toast.LENGTH_LONG).show();
                }
            }
        });

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

                String Name = (String)getIntent().getSerializableExtra("bikeName");
                String Command = (String)getIntent().getSerializableExtra("random");


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("Name", Name));
                nameValuePairs.add(new BasicNameValuePair("Command", Command));
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
            scanDialog();
        }
    }

    public void scanDialog() {
        dialogBuilder = new AlertDialog.Builder(this);

        final View popupView = getLayoutInflater().inflate(R.layout.linkbikepopup, null);

        scanCode = popupView.findViewById(R.id.scanCode);

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(payment_activity.this);
                intentIntegrator.setCaptureActivity(Capture.class);
                intentIntegrator.initiateScan();

                resultt = intentIntegrator.getCaptureActivity().toString();

                Toast.makeText(payment_activity.this, resultt, Toast.LENGTH_LONG).show();
            }
        });
    }



}
