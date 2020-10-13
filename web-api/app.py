from flask import Flask, request
from threading import Lock
import json
import copy

app = Flask(__name__)
dataList = []
lock = Lock()


@app.route('/')
def hello_world():
    return 'OK'


@app.route('/add', methods=['POST'])
def add():
    content = request.json
    for i in content:
        if len(dataList) > 10000:
            dataList.pop(0)
        dataList.append(i)
    print("Request '/add', added {} objects".format(len(content)))
    return 'OK'


@app.route('/get', methods=['GET'])
def get():
    with lock:
        toReturn = copy.deepcopy(dataList)
        dataList.clear()
    print("Request '/get', returned {} objects".format(len(toReturn)))
    return json.dumps(toReturn)

@app.route('/check', methods=['POST'])
def check():
    content = request.json
    print(content)
    return 'Done'


if __name__ == '__main__':
    app.run(host='0.0.0.0')
