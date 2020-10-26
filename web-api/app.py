from flask import Flask, request
from threading import Lock
import json
import copy
from datetime import datetime

app = Flask(__name__)
dataWifi = []
dataBle = []
lock = Lock()


@app.route('/')
def hello_world():
    app.logger.info("root path request - OK")
    return 'OK'


@app.route('/add', methods=['POST'])
def add():
    timestamp = datetime.today().replace(microsecond=0)
    wifiJson = request.json["wifi"]
    bleJson = request.json["ble"]
    app.logger.info(wifiJson)
    app.logger.info(bleJson)
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
    return json.dumps({"wifi": dataWifi, "ble": dataBle}, default=str)


@app.route('/checkPost', methods=['POST'])
def checkPost():
    app.logger.info(request.json)
    return 'OK'


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
