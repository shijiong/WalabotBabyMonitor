import paho.mqtt.client as mqtt
from enum import Enum
from time import sleep
from threading import Thread
import json


class WalabotMQTT:
    """
        MQTT client which publishes walabots data.
        import to WalabotHandler is reburied.
    """
    class Status(Enum):
        PENDING = 0
        WORKING = 1

    def __init__(self, walabotHandler):
        self.__walabot = walabotHandler()
        self.__connection_id = walabotHandler.connection_id
        self.__mqtt_client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol=mqtt.MQTTv311)
        self.__mqtt_client.on_connect = self.__on_connect
        self.__mqtt_client.on_message = self.__on_message
        self.__status = self.Status.PENDING
        self.__working_thread = 0
        self.connect()

    def connect(self):
        if self.__walabot.broker_username is not None and self.__walabot.broker_password is not None:
            self.__mqtt_client.username_pw_set(username=self.__walabot.broker_username, password=self.__walabot.broker_password)
        self.__mqtt_client.connect(self.__walabot.broker_ip_address, self.__walabot.broker_port)
        self.__mqtt_client.loop_forever()

    def __on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        self.__mqtt_client.subscribe("walabot%s/client" % self.__connection_id)

    def __on_message(self, client, userdata, msg):
        info = msg.payload.decode()
        if info == 'STOP' and self.__status is self.Status.WORKING:
            self.__status = self.Status.PENDING
            self.__working_thread.join(timeout=2)
        elif info == 'START' and self.__status is self.Status.PENDING:
            self.__status = self.Status.WORKING
            self.__working_thread = Thread(target=self.__data_loop)
            self.__working_thread.start()
        elif info == 'DISCONNECT':
            if self.__status is self.Status.WORKING:
                self.__status = self.Status.PENDING
                self.__working_thread.join(timeout=2)
            self.__mqtt_client.disconnect()
        print("%s: %s" % (msg.topic, info))

    def __data_loop(self):
        is_connected = self.__walabot.start()
        if not is_connected:
            self.__mqtt_client.publish("walabot%s/server/msg" % self.__connection_id, 'error')
            self.__status = self.Status.PENDING
        else:
            self.__mqtt_client.publish("walabot%s/server/msg" % self.__connection_id, 'start')
        error_counter = 0
        while self.__status is self.Status.WORKING:
            data = dict()
            rc = self.__walabot.get_data(data)
            if rc:
                self.__mqtt_client.publish("walabot%s/server/data" % self.__connection_id, json.dumps(data))
                error_counter = 0
            else:
                error_counter += 1
                if error_counter >= 100:
                    self.__mqtt_client.publish("walabot%s/server/msg"  % self.__connection_id, 'error')
                    self.__status = self.Status.PENDING
            sleep(0.03)
        self.__walabot.stop()