package com.vayyar.example_app;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;

/**
 * Abstract class for creating Walabot android app using MQTT protocol
 */
abstract class WalabotApp extends AppCompatActivity{

        protected boolean isConnected;
        protected static WalabotClient walabotClient;

        /**
         * onCreate will establish the connection with the broker.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                walabotClient = new WalabotClient(this);
                isConnected = false;
        }

        /**
         * onResume() is used when an app has multiple activities.
         */
        @Override
        public void onResume(){
                super.onResume();
                this.walabotClient.changeApp(this);
        }

        /**
         * startWalabot() will send a message to start recording.
         */
        @CallSuper
        public void startWalabot(){
                if (this.isConnected)
                        walabotClient.publishMessage("START");
        }

        /**
         * stopWalabot() will send a message to stop recording.
         */
        @CallSuper
        public void stopWalabot(){
                if (this.isConnected)
                        walabotClient.publishMessage("STOP");
        }
        /**
         * Connect to the broker has succeeded.
         */
        @CallSuper
        public void onConnection(){
                isConnected = true;
        }

        /**
         * Connect to the broker failed.
         */
        @CallSuper
        public void onConnectionError(){
                isConnected = false;
        }

        /**
         * Error message from walabot publisher has been received.
         */
        public void onWalabotError(){

        }

        /**
         * receiving data from the walabot publisher
         * @param message from walabot publisher, should be parsed into a JSON object.
         */
        abstract public void onDataReceived(final String message);

        /**
         * ConnectionId
         * @return a unique topic with the walabot publisher.
         */
        abstract public String getConnectionId();

        /**
         * Getters- are needed for establishing the connection with the  broker.
         * Ip address, port, username and password
         */
        abstract public String getBrokerIpAddress();
        abstract public int getBrokerPort();
        abstract public String getBrokerUserName();
        abstract public String getBrokerPassword();



}
