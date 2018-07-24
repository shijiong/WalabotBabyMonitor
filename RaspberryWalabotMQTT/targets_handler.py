import traceback
from walabot_handler import WalabotHandler
from walabot_mqtt_publisher import WalabotMQTT

arena_params = {
                'minInCm': 30,          'maxInCm': 500,        'resInCm': 3,
                'minIndegrees': -15,    'maxIndegrees': 15,    'resIndegrees': 5,
                'minPhiInDegrees': -60, 'maxPhiInDegrees': 60, 'resPhiInDegrees': 5,
                'mtiMode':True
                }


class TargetsHandler(WalabotHandler):
    connection_id = 'your unique id'
    broker_ip_address = "m12.cloudmqtt.com"
    broker_port = 17219
    broker_username = 'user name'
    broker_password = 'password'

    def __init__(self):
        super(TargetsHandler, self).__init__()
        self.__is_connected = False

    def start(self):
        if self.__is_connected:
            return True
        try:
            # Initializes walabot lib
            self._wlbt.Initialize()
            # 1) Connect : Establish communication with walabot.
            self._wlbt.ConnectAny()
            self._wlbt.SetProfile(self._wlbt.PROF_TRACKER)
            self._wlbt.SetThreshold(30)
            filter_type = self._wlbt.FILTER_TYPE_MTI if arena_params['mtiMode'] else self._wlbt.FILTER_TYPE_NONE
            self._wlbt.SetDynamicImageFilter(filter_type)
            # Setup arena - specify it by Cartesian coordinates.
            self._wlbt.SetArenaR(arena_params['minInCm'], arena_params['maxInCm'], arena_params['resInCm'])
            # Sets polar range and resolution of arena (parameters in degrees).
            self._wlbt.SetArenaTheta(arena_params['minIndegrees'], arena_params['maxIndegrees'], arena_params['resIndegrees'])
            # Sets azimuth range and resolution of arena.(parameters in degrees).
            self._wlbt.SetArenaPhi(arena_params['minPhiInDegrees'], arena_params['maxPhiInDegrees'], arena_params['resPhiInDegrees'])
            # 3) Start: Start the system in preparation for scanning.
            self._wlbt.Start()   
            # calibrates scanning to ignore or reduce the signals
            self._wlbt.StartCalibration()
            while self._wlbt.GetStatus()[0] == self._wlbt.STATUS_CALIBRATING:
                self._wlbt.Trigger()
            self.__is_connected = True
            return True
        except self._wlbt.WalabotError:
            traceback.print_exc()
        return False

    def get_data(self, data_dict):
        try:
            self._wlbt.Trigger()
            raw_targets = self._wlbt.GetTrackerTargets()
            data = [[target.xPosCm, target.yPosCm, target.zPosCm] for target in raw_targets]
            data_dict['targets'] = data
        except self._wlbt.WalabotError:
            traceback.print_exc()
            return False
        return True

    def stop(self):
        if self.__is_connected:
            self._wlbt.Stop()
            self._wlbt.Disconnect()
        print('Terminate successfully')
        self._wlbt.Clean()
        self.__is_connected = False


if __name__ == '__main__':
    mqtt = WalabotMQTT(TargetsHandler)
