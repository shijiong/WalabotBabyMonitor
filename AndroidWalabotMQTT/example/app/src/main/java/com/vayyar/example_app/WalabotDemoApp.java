package com.vayyar.example_app;

/*
public class WalabotDemoApp extends AppCompatActivity implements WalabotApp{
    private TextView messageBox;
    boolean last_clicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walabot_demo_app);
        messageBox = (TextView) findViewById(R.id.output);
        MainActivity.walabotClient.changeApp(this);
        Button startBtn = (Button) findViewById(R.id.start);
        Button stopBtn = (Button) findViewById(R.id.stop);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                last_clicked = true;
                print("starting...");
                MainActivity.walabotClient.publishMessage("START");
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                last_clicked = false;
                print("stoped");
                MainActivity.walabotClient.publishMessage("STOP");
            }
        });
    }

    @Override
    public void onReceive(final String topic, final String message) {
        if (last_clicked) {
            if (topic.equals("msg")){
                this.print(message);
            }
            else if (topic.equals("data")){
                try{
                    JSONObject data = new JSONObject(message);
                    this.print(data.getString("energy"));
                }
                catch (JSONException e){

                }
            }
        }
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
    public void onResume(){
        super.onResume();
        MainActivity.walabotClient.changeApp(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        MainActivity.walabotClient.publishMessage("STOP");
        MainActivity.walabotClient.disconnect();
    }

    @Override
    public void enterApp() {
    }

}*/
