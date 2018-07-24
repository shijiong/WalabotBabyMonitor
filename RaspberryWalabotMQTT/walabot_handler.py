from __future__ import print_function  # WalabotAPI works on both Python 2 an 3.
from sys import platform
from imp import load_source
from os.path import join
import abc


class WalabotHandler:
    """
        Abstract class for Walabot app handler
        Static Params:
            connection_id: defines a unique topic with the walabot application.
            broker_ip, port, username and password: defines the broker- target connection.
            Username and password may be set to None if the communication is open.
    """

    @property
    @abc.abstractstaticmethod
    def connection_id(self):
        pass

    @property
    @abc.abstractstaticmethod
    def broker_ip_address(self):
        ...

    @property
    @abc.abstractstaticmethod
    def broker_port(self):
        ...

    @property
    @abc.abstractstaticmethod
    def broker_username(self):
        ...

    @property
    @abc.abstractstaticmethod
    def broker_password(self):
        ...

    def __init__(self):
        if self.connection_id == None or self.broker_ip_address == None or self.broker_port == None:
            assert(False)
        module_path = ''
        if platform == 'win32':
            module_path = join('C:/', 'Program Files', 'Walabot', 'WalabotSDK', 'python', 'WalabotAPI.py')
        elif platform.startswith('linux'):
            module_path = join('/usr', 'share', 'walabot', 'python', 'WalabotAPI.py')
        self._wlbt = load_source('WalabotAPI', module_path)
        self._wlbt.Init()

    @abc.abstractclassmethod
    def start(self):
        """
            Sets walabot configurations ant start recording.
            Returns: True/ False on success/ failure
        """
        ...

    @abc.abstractclassmethod
    def get_data(self, data_dict):
        """
            Fills data_dict
            Returns: True/ False on success/ failure
        """
        ...

    @abc.abstractclassmethod
    def stop(self):
        """
            Stop recording
        """
        ...