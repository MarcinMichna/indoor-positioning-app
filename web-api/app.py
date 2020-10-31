from flask import Flask, request
from threading import Lock
import json
import copy
import pytz
import time
from datetime import datetime

app = Flask(__name__)
dataWifi = []
dataBle = []
lock = Lock()


@app.route('/')
def hello_world():
    app.logger.info("root path request - OK")
    timestamp = datetime.now().replace(microsecond=0)
    app.logger.info(timestamp)
    return str(timestamp)


@app.route('/add', methods=['POST'])
def add():
    timestamp = datetime.now().replace(microsecond=0)
    wifiJson = request.json["wifi"]
    bleJson = request.json["ble"]
    for i in wifiJson:
        if len(dataWifi) > 1000:
            dataWifi.pop(0)
        i["timestamp"] = timestamp
        dataWifi.append(i)
    for i in bleJson:
        if len(dataWifi) > 1000:
            dataBle.pop(0)
        i["timestamp"] = timestamp
        dataBle.append(i)
    app.logger.info("Request '/add', added {} + {} objects".format(len(wifiJson), len(bleJson)))
    return 'OK'


@app.route('/get', methods=['GET'])
def get():
    with lock:
        resWifi = copy.deepcopy(dataWifi)
        resBle = copy.deepcopy(dataBle)
        dataWifi.clear()
        dataBle.clear()
    app.logger.info("Request '/get', returned {} + {} objects".format(len(resWifi), len(resBle)))
    return json.dumps({"wifi": resWifi, "ble": resBle}, default=str)


@app.route('/checkGet', methods=['GET'])
def checkGet():
    app.logger.info(dataWifi)
    app.logger.info(dataBle)
    return json.dumps({"wifi": dataWifi, "ble": dataBle, "time": datetime.now().replace(microsecond=0)}, default=str)


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
