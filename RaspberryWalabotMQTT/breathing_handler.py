import traceback
from walabot_handler import WalabotHandler
from walabot_mqtt_publisher import WalabotMQTT


arena_params = {
               'min_in_cm': 30,       'max_in_cm': 150,     'res_in_cm': 1,
               'min_in_degrees': -4,  'max_in_degrees': 4,  'res_in_degrees': 2,
               'min_phi_degrees': -4, 'max_phi_degrees': 4, 'res_phi_degrees': 2
                }


class BreathingHandler(WalabotHandler):

    connection_id = 'your unique id'
    broker_ip_address = "m12.cloudmqtt.com"
    broker_port = 17219
    broker_username = 'user name'
    broker_password = 'password'

    def __init__(self):
        super(BreathingHandler, self).__init__()
        self._is_connected = False

    def start(self):
        if self._is_connected:
            return True
        try:
            # Initializes walabot lib
            self._wlbt.Initialize()
            # 1) Connect : Establish communication with walabot.
            self._wlbt.ConnectAny()
            # Set Profile - to Sensor-Narrow.
            self._wlbt.SetProfile(self._wlbt.PROF_SENSOR_NARROW)
            # Dynamic-imaging filter for the specific frequencies typical of breathing
            self._wlbt.SetDynamicImageFilter(self._wlbt.FILTER_TYPE_DERIVATIVE)
            # Setup arena - specify it by Cartesian coordinates.
            self._wlbt.SetArenaR(arena_params['min_in_cm'], arena_params['max_in_cm'], arena_params['res_in_cm'])
            # Sets polar range and resolution of arena (parameters in degrees).
            self._wlbt.SetArenaTheta(arena_params['min_in_degrees'], arena_params['max_in_degrees'],
                                      arena_params['res_in_degrees'])
            # Sets azimuth range and resolution of arena.(parameters in degrees).
            self._wlbt.SetArenaPhi(arena_params['min_phi_degrees'], arena_params['max_phi_degrees'],
                                    arena_params['res_phi_degrees'])
            # 3) Start: Start the system in preparation for scanning.
            self._wlbt.Start()
            self._is_connected = True
            return True
        except self._wlbt.WalabotError:
            traceback.print_exc()
        return False
    
    def get_data(self, data_dict):
        try:
            self._wlbt.Trigger()
            data = self._wlbt.GetImageEnergy() * 1e7
            data_dict['energy'] = data
        except self._wlbt.WalabotError:
            traceback.print_exc()
            return False
        return True

    def stop(self):
        if self._is_connected:
            self._wlbt.Stop()
            self._wlbt.Disconnect()
        print('Terminate successfully')
        self._wlbt.Clean()
        self._is_connected = False


if __name__ == '__main__':
    mqtt = WalabotMQTT(BreathingHandler)
