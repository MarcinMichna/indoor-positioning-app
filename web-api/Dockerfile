FROM python:3.8-slim-buster

ADD app.py /
ADD requirements.txt /

RUN pip install -r requirements.txt

CMD [ "python", "./app.py" ]
