package com.vayyar.example_app;

import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;


public class WalabotClient implements Serializable {
    public MqttAndroidClient mqttAndroidClient;
    private WalabotApp app;
    private String clientId = "ExampleAndroidClient";

    public WalabotClient(WalabotApp app){
        this.app = app;
    }

    public void changeApp(WalabotApp newapp){
        this.app = newapp;
        this.connect();
    }

    public void connect(){

        String serverUri = "tcp://" + this.app.getBrokerIpAddress() + ":" + this.app.getBrokerPort();
        mqttAndroidClient = new MqttAndroidClient(this.app.getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                app.onConnection();
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                app.onConnectionError();
                Log.w("Mqtt", "lost connection");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String topic_s = topic.split("/")[2];


                if (topic_s.equals("msg") && mqttMessage.toString().equals("error")){
                    app.onWalabotError();
                }
                else if (topic_s.equals("data")){
                    app.onDataReceived(mqttMessage.toString());
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        _connect();
    }


    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void _connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(this.app.getBrokerUserName());
        mqttConnectOptions.setPassword(this.app.getBrokerPassword().toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                    app.onConnection();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + app.getBrokerIpAddress() + exception.toString());
                    app.onConnectionError();
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe("walabot" + this.app.getConnectionId() + "/server/+", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                    app.onConnectionError();
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(@NonNull String msg){
        try{
            byte[] encodedPayload = new byte[0];
            encodedPayload = msg.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setId(320);
            message.setRetained(true);
            message.setQos(2);
            mqttAndroidClient.publish("walabot" + this.app.getConnectionId() + "/client", message);
        }
        catch (MqttException err){
        }
        catch (UnsupportedEncodingException err){
        }

    }


public void disconnect(){
    try {
        this.mqttAndroidClient.disconnect();
    }
    catch (MqttException ex){

    }
}

}