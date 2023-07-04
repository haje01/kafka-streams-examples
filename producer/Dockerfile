FROM python:3.10-slim-bullseye

RUN groupadd -r python && useradd -r -g python python

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY --chown=python:python producer.py .
