package com.vayyar.example_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import android.media.MediaPlayer;

public class MainActivity extends  WalabotApp {
    /*
        WalabotApp connection params
     */
    private String connectionId = "mywalabotapp";
    private String brokerIpAddress = "m12.cloudmqtt.com";
    private int brokerPort = 17219;
    private String brokerUserName = "dtylglsa";
    private String brokerPassword = "TvtjqBqHXfj2";

    /*
        my app params
     */
    private TextView messageBox;
    private boolean last_clicked = false;

    private TextView distanceXTxt;
    private TextView distanceYTxt;
    private TextView distanceZTxt;
    private TextView distanceCalTxt;
    private TextView warningTxt;
    private TextView safeDistanceTxt;
    private ImageView imageView;

    MediaPlayer mPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startBtn = (Button) findViewById(R.id.start);
        Button stopBtn = (Button) findViewById(R.id.stop);
        messageBox = (TextView) findViewById(R.id.output);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWalabot();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopWalabot();
            }
        });

        distanceXTxt=(TextView)findViewById(R.id.distanceX);
        distanceYTxt=(TextView)findViewById(R.id.distanceY);
        distanceZTxt=(TextView)findViewById(R.id.distanceZ);
        distanceCalTxt=(TextView)findViewById(R.id.distanceCal);
        warningTxt=(TextView)findViewById(R.id.warning);
        safeDistanceTxt=(TextView)findViewById(R.id.safeDistance);
        imageView=(ImageView)findViewById(R.id.imageView);

        warningTxt.setText("No Waring");

        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mPlayer.setLooping(true);
    }

    @Override
    public void startWalabot(){
        super.startWalabot();
        if (this.isConnected){
            last_clicked = true;
            print("starting...");
        }
        else{
            last_clicked = false;
            print("waiting for\nconnection");
        }

    }

    @Override
    public void stopWalabot(){
        super.stopWalabot();
        last_clicked = false;
        print("stopped");
    }

    public void print(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageBox.setText(message);
            }
        });
    }


    @Override
    public void onDataReceived(final String message) {
        if (last_clicked){
            try {
                JSONObject data = new JSONObject(message);
                //this.print(data.getString("energy"));
                String Rxdmessage=data.getString("targets");
                this.print(Rxdmessage);
                if(Rxdmessage=="[]")
                {
                    this.print("Baby is not detected!");
                }
                else
                {
                    String str1=Rxdmessage.replace("[",""); //Eliminate "["
                    String str2=str1.replace("]",""); //Eliminate "]"
                    String[] distance=str2.split(",");
                    distanceXTxt.setText(distance[0]);
                    distanceYTxt.setText(distance[1]);
                    distanceZTxt.setText(distance[2]);
                    Double distancex=Double.parseDouble(distance[0]);
                    Double distancey=Double.parseDouble(distance[1]);
                    Double distancez=Double.parseDouble(distance[2]);
                    Double distanceCal= Math.sqrt(Math.pow(distancex,2)+Math.pow(distancey,2)+Math.pow(distancez,2));
                    distanceCalTxt.setText(String.valueOf(distanceCal));
                    String distanceset=safeDistanceTxt.getText().toString();
                    Double distanceSet=Double.parseDouble(distanceset);
                    if(distanceCal>distanceSet) {
                        warningTxt.setText("Baby is out of safety distance");
                        warningTxt.setTextColor(Color.RED);
                        mPlayer.start();
                        imageView.setImageResource(R.drawable.cry);
                    }
                    else {
                        warningTxt.setText("No Waring");
                        warningTxt.setTextColor(Color.GRAY);
                        mPlayer.pause();
                        mPlayer.seekTo(0);
                        imageView.setImageResource(R.drawable.smile);
                    }

                }
            } catch (JSONException e) {

            }
        }
    }

    @Override
    public void onWalabotError() {
        print("walabot error");
    }


    @Override
    public String getConnectionId(){
        return this.connectionId;
    }

    @Override
    public String getBrokerIpAddress(){
        return this.brokerIpAddress;
    }

    @Override
    public int getBrokerPort(){
        return this.brokerPort;
    }

    @Override
    public String getBrokerUserName(){
        return this.brokerUserName;
    }

    @Override
    public String getBrokerPassword(){
        return this.brokerPassword;
    }

}
