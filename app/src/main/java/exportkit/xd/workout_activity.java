package exportkit.xd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;

public class workout_activity extends Activity {

    private View view;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private Button save, cancel;
    private EditText height, weight, caloriestxt, hour, minute, second, kilometer;
    private Button calories, distance, freestyle, time;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(workout_activity.this, start_activity.class));
    }

    public void caloriesDialog() {
        dialogBuilder = new AlertDialog.Builder(this);

        final View popupView = getLayoutInflater().inflate(R.layout.caloriespopup, null);
        height = (EditText) popupView.findViewById(R.id.height);
        weight = (EditText) popupView.findViewById(R.id.weight);
        caloriestxt = (EditText) popupView.findViewById(R.id.calories);

        cancel = (Button) popupView.findViewById(R.id.cancel);
        save = (Button) popupView.findViewById(R.id.save);

        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();

//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = 1000;
//        lp.height = 800;
//            lp.x=-170;
//            lp.y=100;
//        dialog.show();
//        dialog.getWindow().setAttributes(lp);
        dialog.show();

        height.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!height.getText().toString().equals("")) {

                    if (Float.parseFloat(height.getText().toString()) < 55 || Float.parseFloat(height.getText().toString()) > 272) {
                        height.setError("Enter a realistic value");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(height.getText().toString().equals("")) {
                    height.setError("If you leave this empty, we will consider the average height");
                }
            }
        });

        weight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!weight.getText().toString().equals("")) {

                    if (Float.parseFloat(weight.getText().toString()) < 27 || Float.parseFloat(weight.getText().toString()) > 300) {
                        weight.setError("Enter a realistic value");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(weight.getText().toString().equals("")) {
                    weight.setError("If you leave this empty, we will consider the average weight");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (caloriestxt.getText().toString().equals("")) {
                    caloriestxt.setError("You must add a calorie goal to continue!");
                    //Toast.makeText(popupView.getContext(), "You must add a calorie goal to continue!", Toast.LENGTH_SHORT);
                } else {

                    Intent intent = new Intent(workout_activity.this, workoutRide_activity.class);

                    if((boolean)getIntent().getSerializableExtra("shared")){
                        intent = new Intent(workout_activity.this, chooseBike_activity.class);
                        intent.putExtra("shared", true);
                    }else{
                        intent.putExtra("shared", false);
                    }

                    if(weight.getText().toString().equals(""))
                        intent.putExtra("weight", "60.7");
                    else
                        intent.putExtra("weight", weight.getText().toString());


                    if(height.getText().toString().equals(""))
                        intent.putExtra("height","165");
                    else
                        intent.putExtra("height", height.getText().toString());

                    intent.putExtra("workout", true);
                    intent.putExtra("type", "calories");
                    intent.putExtra("calories", caloriestxt.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }


    public void timeDialog() {
        dialogBuilder = new AlertDialog.Builder(this);

        final View popupView = getLayoutInflater().inflate(R.layout.timepopup, null);

        hour = (EditText) popupView.findViewById(R.id.hour);
        minute = (EditText) popupView.findViewById(R.id.minute);
        second = (EditText) popupView.findViewById(R.id.second);

        cancel = (Button) popupView.findViewById(R.id.cancel);
        save = (Button) popupView.findViewById(R.id.save);

        minute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (!minute.getText().toString().equals("")) {

                    if (Integer.parseInt(minute.getText().toString()) > 59) {
                        minute.setText("00");

                        if (!hour.getText().toString().equals("")) {
                            hour.setText(String.valueOf(Integer.parseInt(hour.getText().toString()) + 1));
                        } else {
                            hour.setText("1");
                        }
                    }
                }
            }

            ;
        });
        second.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!second.getText().toString().equals("")) {
                    if (Integer.parseInt(second.getText() + "") > 59) {
                        second.setText("00");

                        if (!minute.getText().toString().equals("")) {
                            minute.setText(String.valueOf(Integer.parseInt(minute.getText().toString()) + 1));
                        } else {
                            minute.setText("1");
                        }
                    }
                }
            };
        });

        cancel = (Button) popupView.findViewById(R.id.cancel);
        save = (Button) popupView.findViewById(R.id.save);

        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();

//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = 1000;
//        lp.height = 800;
//        lp.x=-170;
//        lp.y=100;
//        dialog.show();
//        dialog.getWindow().setAttributes(lp);
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
                if (hour.getText().toString().equals("") && minute.getText().toString().equals("") && second.getText().toString().equals("")) {
                    Toast.makeText(popupView.getContext(), "You haven't picked a time goal!", Toast.LENGTH_SHORT);
                } else {

                    Intent intent = new Intent(workout_activity.this, workoutRide_activity.class);

                    if((boolean)getIntent().getSerializableExtra("shared")){
                        intent = new Intent(workout_activity.this, chooseBike_activity.class);
                        intent.putExtra("shared", true);
                    }else{
                        intent.putExtra("shared", false);
                    }


                    if(hour.getText().toString().equals(""))
                        intent.putExtra("hour", "0");
                    else
                        intent.putExtra("hour", hour.getText().toString());

                    if(minute.getText().toString().equals(""))
                        intent.putExtra("minute", "0");
                    else
                        intent.putExtra("minute", minute.getText().toString());

                    if(second.getText().toString().equals(""))
                        intent.putExtra("second", "0");
                    else
                        intent.putExtra("second", second.getText().toString());

                    intent.putExtra("workout", true);
                    intent.putExtra("type", "time");
                    startActivity(intent);
                }
            }
        });
    }


    public void distanceDialog() {
        dialogBuilder = new AlertDialog.Builder(this);

        final View popupView = getLayoutInflater().inflate(R.layout.distancepopup, null);

        kilometer = (EditText) popupView.findViewById(R.id.kilometer);

        cancel = (Button) popupView.findViewById(R.id.cancel);
        save = (Button) popupView.findViewById(R.id.save);

        kilometer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {

            };
        });

        dialogBuilder.setView(popupView);
        dialog=dialogBuilder.create();

//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = 1000;
//        lp.height = 800;
//        dialog.show();
//        dialog.getWindow().setAttributes(lp);
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
                if (/*kilometer.getText().toString().equals("") && */kilometer.getText().toString().equals("")) {
                    Toast.makeText(workout_activity.this, "You haven't picked a time goal!", Toast.LENGTH_SHORT);
                } else {
                    Intent intent = new Intent(workout_activity.this, workoutRide_activity.class);


                    if((boolean)getIntent().getSerializableExtra("shared")){
                        intent = new Intent(workout_activity.this, chooseBike_activity.class);
                        intent.putExtra("shared", true);
                    }else{
                        intent.putExtra("shared", false);
                    }

                    intent.putExtra("type", "distance");
                    intent.putExtra("workout", true);

                    if(kilometer.getText().toString().equals(""))
                        intent.putExtra("kilometer", "0");
                    else
                        intent.putExtra("kilometer", kilometer.getText().toString());

                    startActivity(intent);
                }
            }
        });
    }


    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

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
        if(isInternetAvailable()){

            setContentView(R.layout.workout);

            calories = (Button) findViewById(R.id.calories);
            time = (Button) findViewById(R.id.time);
            freestyle = (Button) findViewById(R.id.freestyle);
            distance = (Button) findViewById(R.id.distance);

            calories.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    caloriesDialog();
                }
            });

            time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timeDialog();
                }
            });

            distance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    distanceDialog();
                }
            });
            freestyle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if((boolean)getIntent().getSerializableExtra("shared")){

                        Intent intent = new Intent(workout_activity.this, chooseBike_activity.class);
                        intent.putExtra("workout", true);
                        intent.putExtra("type", "freestyle");
                        intent.putExtra("shared", true);
                        startActivity(intent);

                    }else{
                        Intent intent = new Intent(workout_activity.this, workoutRide_activity.class);
                        intent.putExtra("workout", true);
                        intent.putExtra("type", "freestyle");
                        intent.putExtra("shared", false);
                        startActivity(intent);
                    }
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
}
