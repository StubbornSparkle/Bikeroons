package exportkit.xd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import exportkit.xd.R;

public class BaseActivity extends AppCompatActivity{

    public DrawerLayout drawerLayout;
    public Button drawerButton, drawerSignOut,drawerHome, drawerMyBike,drawerFindBike, drawerSettings;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button signout, cancel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawerLayout = findViewById(R.id.my_drawer_layout);

        drawerButton = findViewById(R.id.opendrawer);
        drawerSignOut = findViewById(R.id.signoutBtnDrawer);
        drawerHome = findViewById(R.id.homeDrawer);
        drawerMyBike = findViewById(R.id.myBikeButtonDrawer);
        drawerFindBike = findViewById(R.id.findBikeButtonDrawer);
        drawerSettings = findViewById(R.id.settingsButtonDrawer);


        if(drawerButton!=null) {
            drawerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });

            drawerSignOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signoutDialog();
                }
            });

            drawerHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(BaseActivity.this, start_activity.class));
                }
            });

            drawerMyBike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            drawerFindBike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   Intent intent = new Intent(BaseActivity.this, choice_activity.class);
                    intent.putExtra("shared", true);
                    startActivity(intent);
                }
            });

            drawerSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(BaseActivity.this, settings_activity.class));
                }
            });

        }




    }


    public void signoutDialog() {
        dialogBuilder = new AlertDialog.Builder(this);

        final View popupView = getLayoutInflater().inflate(R.layout.signoutpopup, null);

        cancel = (Button) popupView.findViewById(R.id.cancel);
        signout = (Button) popupView.findViewById(R.id.signout);

        dialogBuilder.setView(popupView);
        dialog=dialogBuilder.create();


        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseActivity.this, signin_activity.class));
            }
        });
    }


}