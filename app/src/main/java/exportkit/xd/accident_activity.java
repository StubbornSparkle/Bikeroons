package exportkit.xd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class accident_activity extends Activity {

    private TextView cd;

    private Set<String> getNumbers;
    SharedPreferences spget;

    private GeoPoint currentLocation = new GeoPoint(0.0,0.0);

    final int SEND_SMS_PERMISSION_REQUEST_CODE=1;

    private boolean send = true;
    private View view;

    private ArrayList<String> numbers = new ArrayList<>();
    private String url, FetchedEmail,res;

    private SharedPreferences sp;

    @SuppressLint("WrongThread")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.accident);
        cd = (TextView) findViewById(R.id.cd);


        sp = getSharedPreferences("session", Context.MODE_PRIVATE);
        FetchedEmail = sp.getString("email","");
        url =getResources().getString(R.string.ngrok)+"/users/gimmenums/"+FetchedEmail;

        getNumbers getNumbers = new getNumbers();
        getNumbers.doInBackground();


        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {

                startService();
            }
        } else {
            startService();
        }


        view = (View) findViewById(R.id.view);
//        checkSystemWritePermission();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send = false;

                try {
                    playRingtone3();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        new CountDownTimer(6000, 1000) {

            public void onTick(long millisUntilFinished) {

                cd.setText((millisUntilFinished / 1000)+"");
            }
            public void onFinish() {
                //startActivity(new Intent(accident_activity.this, ride_activity.class));

                if(send){
                    sendSMS();
                }

                try {
                    playRingtone3();
                } catch (IOException e) {
                    e.printStackTrace();
                }


              /*  view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
                });*/

            }
        }.start();
    }

    protected void sendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

            } else {
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
            }
        }else{

            if(numbers!=null) {
                SmsManager smsManager = SmsManager.getDefault();
                for (int i = 0; i < numbers.size(); i++)
                    smsManager.sendTextMessage(numbers.get(i), null, "Help! I had an accident with my smart E-bike at the location:\n"
                            +currentLocation.getLatitude()+", "+currentLocation.getLongitude()+"\n\nI urgently need your help!\n\nSMS sent from my awesome Bikeroons app.", null, null);
            }

        }
    }


    public void playRingtone3() throws IOException {
        final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        MediaPlayer mp = MediaPlayer.create(this, R.raw.emergency);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp=MediaPlayer.create(this, R.raw.emergency);
            }else if(send){
                mp=MediaPlayer.create(this, R.raw.emergency);
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public abstract class CountUpTimer extends CountDownTimer {
        private static final long INTERVAL_MS = 1000;
        private final long duration;

        protected CountUpTimer(long durationMs) {
            super(durationMs, INTERVAL_MS);
            this.duration = durationMs;
        }

        public abstract void onTick(int second);

        @Override
        public void onTick(long msUntilFinished) {
            int second = (int) ((duration - msUntilFinished) / 1000);
            onTick(second);
        }

        @Override
        public void onFinish() {
            onTick(duration / 1000);
        }
    }




    void startService(){
        try{
            accident_activity.LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
            IntentFilter filter = new IntentFilter("ACT_LOC");
            registerReceiver(receiver, filter);
            Intent intent = new Intent(accident_activity.this, LocationService.class );
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACT_LOC")) {

                currentLocation.setLatitude(intent.getDoubleExtra("latitude", 0));
                currentLocation.setLongitude(intent.getDoubleExtra("longitude", 0));
            }
        }
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





    private class getNumbers extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try(Response responsee = client.newCall(request).execute()){
                res = responsee.body().string();

                String num = "";

                boolean isnum = false;

                for(int i=0; i<res.length(); i++){
                    if(res.charAt(i)=='[')
                        isnum = true;
                    if(res.charAt(i)==','){

                        if(!num.equals(""))
                            numbers.add(num);
                        num="";
                    }
                    if(res.charAt(i)==']'){
                        isnum = false;
                        if(!num.equals(""))
                            numbers.add(num);
                        num="";
                        break;
                    }
                    if(isnum && res.charAt(i)!='"' && res.charAt(i)!='[' && res.charAt(i)!=']' && res.charAt(i)!=' ' &&res.charAt(i)!=',' ){
                        //if(isnum){
                        num+=res.charAt(i);
                    }
                }
                //return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            Toast.makeText(accident_activity.this, numbers.size()+"", Toast.LENGTH_SHORT).show();
        }
    }


}
