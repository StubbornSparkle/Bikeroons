package exportkit.xd;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class turnOnLed_activity extends Activity {

    private Button park;
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.turnonled);

        park = (Button) findViewById(R.id.park);
        park.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSSHcommand("192.168.43.17","Bodyflash149");
            }
        });
    }



    private void executeSSHcommand(String ip, String pw) {

        String user = "pi";
        String password = pw;
        String host = ip;

        int port=22;

        try{

            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(10000);
            session.connect();
            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand("python /home/pi/Desktop/ALL/led.py");
            channel.connect();

            Snackbar.make(this.findViewById(android.R.id.content),
                    "Success!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            channel.disconnect();

            /*
            URL oracle = new URL("192.168.1.4:8080");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            /*
            URL site = new URL("192.168.1.4:8080");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            site.openStream()));

            String inputLine;
            String res ="";
            while ((inputLine = in.readLine()) != null)
                res+=(inputLine);

            suc.setText(res);
            in.close();*/

        }
        catch(JSchException /*| MalformedURLException*/ e) {

            Snackbar.make(this.findViewById(android.R.id.content),
                    "Check WIFI or Server! Error : " + e.getMessage(),
                    Snackbar.LENGTH_LONG)
                    .setDuration(20000).setAction("Action", null).show();
        } /*catch (IOException e) {
            e.printStackTrace();
        */
    }



}
