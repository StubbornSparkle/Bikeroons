 package exportkit.xd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


 public class settings_activity  extends Activity {

    private Button backBtn;
    private View view;
    private String back;

     private HttpResponse response,addNumResponse, removeNumResponse, editNumResponse;

    private Button add;
    private Button remove, edit, permission, pwchange, done;

     private AlertDialog.Builder dialogBuilder;
     private AlertDialog dialog;

     private Button save, cancel;
     private EditText numtext, newpw, oldpw;

     private  LinearLayout l;

     private SharedPreferences sp;

     private ArrayList<String> numbers = new ArrayList<>();
     private String url;

     private String FetchedEmail,res;

     private String[] editParams= new String[3];
     private String[] removeParams = new String[2];
     private String[] addParams = new String[2];
     private String[] pwParams = new String[3];

    @SuppressLint("WrongThread")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(isInternetAvailable()){

            setContentView(R.layout.settings);

            back = (String)getIntent().getSerializableExtra("back");
            l = (LinearLayout) findViewById(R.id.linearLayout);

            permission = (Button) findViewById(R.id.permission);

            sp = getSharedPreferences("session", Context.MODE_PRIVATE);
            FetchedEmail = sp.getString("email","");
            url =getResources().getString(R.string.ngrok)+"/users/gimmenums/"+FetchedEmail;

            getNumbers pd = new getNumbers(settings_activity.this);
            pd.execute();


            pwchange = (Button) findViewById(R.id.pwchange);
            pwchange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwChange();
                }
            });

            permission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            });

            add = (Button) findViewById(R.id.add);
           // remove = (Button) findViewById(R.id.remove);
            backBtn = (Button) findViewById(R.id.backBtn);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(back.equals("choice")){

                        Intent intent = new Intent(settings_activity.this, choice_activity.class);
                        intent.putExtra("shared", (boolean)getIntent().getSerializableExtra("shared"));
                        startActivity(intent);

                    }else if(back.equals("start")){
                        Intent intent = new Intent(settings_activity.this, start_activity.class);
                        startActivity(intent);
                    }
                }
            });


            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNumDialog();
                }
            });

            //addButtons();

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

     @Override
     public void onBackPressed() {
         //super.onBackPressed();
         if(back.equals("choice")){
             Intent intent = new Intent(settings_activity.this, choice_activity.class);

             intent.putExtra("shared", (boolean)getIntent().getSerializableExtra("shared"));
             startActivity(intent);

         }else if(back.equals("start")){
             startActivity(new Intent(settings_activity.this, start_activity.class));
         }
     }

//     public boolean internetIsConnected() {
//         try {
//             String command = "ping -c 1 google.com";
//             return (Runtime.getRuntime().exec(command).waitFor() == 0);
//         } catch (Exception e) {
//             return false;
//         }
//     }

     public boolean isInternetAvailable() {
         try {
             InetAddress ipAddr = InetAddress.getByName("google.com");
             return !ipAddr.equals("");

         } catch (Exception e) {
             return false;
         }
     }




    public void addButtons(){
        if(numbers!=null) {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    l.removeAllViews();
                    for (int i = 0; i < numbers.size(); i++) {
                        Button button = new Button(getApplicationContext());
                        button.setText(numbers.get(i));

                        int finalI = i;
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NumDialog(numbers.get(finalI));
                            }
                        });
                        l.addView(button);
                    }

                }
            });
        }
    }


     public void pwChange() {
         dialogBuilder = new AlertDialog.Builder(this);

         final View popupView = getLayoutInflater().inflate(R.layout.pwchange, null);

         cancel = (Button) popupView.findViewById(R.id.cancel);
         done = (Button) popupView.findViewById(R.id.done);
         newpw = (EditText) popupView.findViewById(R.id.newpw);
         oldpw = (EditText) popupView.findViewById(R.id.oldpw);

         dialogBuilder.setView(popupView);
         dialog=dialogBuilder.create();

//         WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//         lp.copyFrom(dialog.getWindow().getAttributes());
//         lp.width = 1000;
//         lp.height = 800;
//         dialog.show();
//         dialog.getWindow().setAttributes(lp);
         dialog.show();

         cancel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 dialog.dismiss();
             }
         });

         done.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {


                 sp = getSharedPreferences("session", Context.MODE_PRIVATE);
                 FetchedEmail = sp.getString("email","");

                 ChangePassword cpw = new ChangePassword();

                 pwParams[0] = FetchedEmail;
                 pwParams[1] = oldpw.getText().toString();
                 pwParams[2] = newpw.getText().toString();

                 cpw.doInBackground();
             }
         });
     }

     public class ChangePassword extends AsyncTask<String, String, String> {

         @SuppressLint("WrongThread")
         @Override
         protected String doInBackground(String... strings) {

             try{
                 // Create a new HttpClient and Post Header
                 HttpClient httpclient = new DefaultHttpClient();
                 //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                 HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/changepw");

                 String email = pwParams[0];
                 String password = pwParams[1];
                 String newpassword = pwParams[2];

                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                 nameValuePairs.add(new BasicNameValuePair("email", email));
                 nameValuePairs.add(new BasicNameValuePair("password", password));
                 nameValuePairs.add(new BasicNameValuePair("newpassword", newpassword));
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
             if(response != null && response.getStatusLine().toString().equals("HTTP/1.1 500 Internal Server Error")){
                 newpw.setError("Invalid new password or wrong old password!");
             }else if(response.getStatusLine().toString().equals("HTTP/1.1 200 OK")){
                 Toast.makeText(settings_activity.this, "Password changed!", Toast.LENGTH_SHORT).show();
                 dialog.dismiss();
             }
         }
     }


     public void addNumDialog() {
         dialogBuilder = new AlertDialog.Builder(this);

         final View popupView = getLayoutInflater().inflate(R.layout.addnumpopup, null);

         cancel = (Button) popupView.findViewById(R.id.cancel);
         save = (Button) popupView.findViewById(R.id.save);
         numtext = (EditText) popupView.findViewById(R.id.numtext);

         dialogBuilder.setView(popupView);
         dialog=dialogBuilder.create();

//         WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//         lp.copyFrom(dialog.getWindow().getAttributes());
//         lp.width = 1000;
//         lp.height = 800;
//         dialog.show();
//         dialog.getWindow().setAttributes(lp);
         dialog.show();

         cancel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 dialog.dismiss();
             }
         });

         save.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(numtext.getText().toString().isEmpty()){
                     numtext.setError("Enter a phone number!");
                 }else {

                     addNumber addnum = new addNumber();

                     addParams[0] = FetchedEmail ;
                     addParams[1] = numtext.getText().toString();

                     addnum.doInBackground();

                     //ToDo: ADD NUMBER DONE

                 }
             }
         });
     }

     public void NumDialog(String num) {
         dialogBuilder = new AlertDialog.Builder(this);

         final View popupView = getLayoutInflater().inflate(R.layout.numpopup, null);

         remove = (Button) popupView.findViewById(R.id.remove);
         edit = (Button) popupView.findViewById(R.id.edit);
         numtext = (EditText) popupView.findViewById(R.id.numtext);

         numtext.setText(num);

         dialogBuilder.setView(popupView);
         dialog=dialogBuilder.create();

//         WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//         lp.copyFrom(dialog.getWindow().getAttributes());
//         lp.width = 1000;
//         lp.height = 800;
//         dialog.show();
//         dialog.getWindow().setAttributes(lp);
         dialog.show();

         edit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 //ToDo: EDIT NUMBER
                editNumber edit = new editNumber();

                editParams[0] = FetchedEmail ;
                editParams[1] = numtext.getText().toString();
                editParams[2] = num;

                edit.doInBackground();

             }
         });
         remove.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 //ToDo: Remove Number

                 removeNumber remove = new removeNumber();

                 removeParams[0] = FetchedEmail ;
                 removeParams[1] = numtext.getText().toString();

                 remove.doInBackground();


                 //Toast.makeText(settings_activity.this, "Number removed!", Toast.LENGTH_SHORT).show();

             }
         });
     }


     public class addNumber extends AsyncTask<String, String, String> {

         @SuppressLint("WrongThread")
         @Override
         protected String doInBackground(String... strings) {

             try{
                 // Create a new HttpClient and Post Header
                 HttpClient httpclient = new DefaultHttpClient();
                 //HttpPost httppost = new HttpPost("http://localhost:9090/users/register");
                 HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/addnumber");

                 String email = addParams[0];
                 String numbers = addParams[1];

                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                 nameValuePairs.add(new BasicNameValuePair("email", email));
                 nameValuePairs.add(new BasicNameValuePair("numbers", numbers));
                 httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                 // Execute HTTP Post Request
                 addNumResponse = httpclient.execute(httppost);

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
             if (numtext.getText().toString().length() == 11 && addNumResponse.getStatusLine().toString().equals("HTTP/1.1 200 OK")){
                 Toast.makeText(settings_activity.this, "Number added!", Toast.LENGTH_SHORT).show();
                 addButtons();
                 dialog.dismiss();

             }else{
                 Toast.makeText(settings_activity.this, "This does not look like a valid number!\nTry not including the country code", Toast.LENGTH_SHORT).show();
             }
         }
     }

     private class getNumbers extends AsyncTask<Void, Void, Void> {
         private ProgressDialog loading;

         public getNumbers(settings_activity activity) {
             loading = new ProgressDialog(activity);
         }

         @Override
         protected void onPreExecute() {
             loading.setMessage("Fetching your emergency numbers, please wait...");
             loading.show();
         }
         @SuppressLint("WrongThread")
         @Override
         protected Void doInBackground(Void... args) {

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
                 addButtons();
                 //return response.body().string();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return null;
         }
         @Override
         protected void onPostExecute(Void result) {
             // do UI work here
             if (loading.isShowing()) {

                 loading.dismiss();
             }
         }
     }


     public class editNumber extends AsyncTask<String, String, String> {

         @SuppressLint("WrongThread")
         @Override
         protected String doInBackground(String... strings) {

             try{
                 // Create a new HttpClient and Post Header
                 HttpClient httpclient = new DefaultHttpClient();
                 //HttpPost httppost = new HttpPost("http://localhost:9090/users/editnumber");
                 HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/editnumber");

                 String email = editParams[0];
                 String numbers = editParams[1];
                 String numbers2 = editParams[2];

                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                 nameValuePairs.add(new BasicNameValuePair("email", email));
                 nameValuePairs.add(new BasicNameValuePair("numbers", numbers));
                 nameValuePairs.add(new BasicNameValuePair("numbers2", numbers2));
                 httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                 // Execute HTTP Post Request
                 editNumResponse = httpclient.execute(httppost);

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
             if(editNumResponse.getStatusLine().toString().equals("HTTP/1.1 200 OK")){
                 Toast.makeText(settings_activity.this, "Number edited!", Toast.LENGTH_SHORT).show();
                 addButtons();
                 dialog.dismiss();
             }else {
                 Toast.makeText(settings_activity.this, "There was an error editing the number!", Toast.LENGTH_SHORT).show();
             }
         }
     }




     public class removeNumber extends AsyncTask<String, String, String> {

         @SuppressLint("WrongThread")
         @Override
         protected String doInBackground(String... strings) {

             try{
                 // Create a new HttpClient and Post Header
                 HttpClient httpclient = new DefaultHttpClient();
                 //HttpPost httppost = new HttpPost("http://localhost:9090/users/editnumber");
                 HttpPost httppost = new HttpPost(getResources().getString(R.string.ngrok)+"/users/removenumber");

                 String email = removeParams[0];
                 String numbers = removeParams[1];

                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                 nameValuePairs.add(new BasicNameValuePair("email", email));
                 nameValuePairs.add(new BasicNameValuePair("numbers", numbers));
                 httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                 // Execute HTTP Post Request
                 removeNumResponse = httpclient.execute(httppost);
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
             if(removeNumResponse.getStatusLine().toString().equals("HTTP/1.1 200 OK")){
                 Toast.makeText(settings_activity.this, "Number removed!", Toast.LENGTH_SHORT).show();
                 addButtons();
                 dialog.dismiss();
             }else{
                 Toast.makeText(settings_activity.this, "There was an error removing the number!", Toast.LENGTH_SHORT).show();
             }
         }

     }


 }
