import os
import json 
import time
import random
from datetime import datetime

from faker import Faker 
from confluent_kafka import Producer
from confluent_kafka.admin import AdminClient

KAFKA_BROKER = os.environ['KAFKA_BROKER']
KAFKA_TOPIC = os.environ['KAFKA_TOPIC']
PROD_TYPE = os.environ['PROD_TYPE']
print(f"KAFKA_BROKER: {KAFKA_BROKER}")
print(f"KAFKA_TOPIC: {KAFKA_BROKER}")
print(f"PROD_TYPE: {PROD_TYPE}")
if PROD_TYPE == 'multistr':
    KAFKA_TOPIC2 = KAFKA_TOPIC + '2'

NUM_BATCH=1
BATCH_ITEMS=100
NUM_USERS=10

# Wait for kafka topic ready
while True:
    cluster_metadata = AdminClient({'bootstrap.servers': KAFKA_BROKER}).list_topics()
    if KAFKA_TOPIC in cluster_metadata.topics:
        print(f"Topic {KAFKA_TOPIC} is ready.")
        break
    else:
        print(f"Topic {KAFKA_TOPIC} is not ready. waiting...")
        time.sleep(3)
        continue

conf = {
    'bootstrap.servers': KAFKA_BROKER,
}

fake = Faker('en_US')
users = ['ID' + str(fake.unique.random_int(min=11111, max=99999)) for _ in range(NUM_USERS)]
p = Producer(**conf)


def send_message(fake, users, p, numerr):
    """타입별 메시지 생성 및 전송."""
    user_id = fake.random_element(elements=users)
    msg = fake.sentence(nb_words=10)

    if PROD_TYPE == 'multistr':
        body = msg
    else:
        log_level = fake.random_element(elements=('INFO', 'WARN', 'ERROR'))
        log_entry = {
                'user_id': user_id,
                'timestamp': time.time(),
                'datetime': str(datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]),
                'log_level': log_level,
                'message': msg,
            }
        if log_level == 'ERROR':
            numerr += 1
        body = json.dumps(log_entry)
    p.produce(KAFKA_TOPIC, key=user_id, value=body)

    # 멀티 스트림 예제를 위한 메시지 복제 
    if PROD_TYPE == 'multistr':
        p.produce(KAFKA_TOPIC2, key=user_id, value=body)
    return numerr


for b in range(NUM_BATCH):
    start = time.time()
    numerr = 0
    
    # 배치 아이템 수만큼 메시지 생성 및 전송
    print(f"[ ] Generate {BATCH_ITEMS} logs.")
    for _ in range(BATCH_ITEMS):
        numerr = send_message(fake, users, p, numerr)
    # 남은 메시지 전달
    p.flush()

    # 잠시 쉬었다 재개 
    time.sleep(1)
    elapsed = time.time() - start
    velocity = BATCH_ITEMS / elapsed
    print(f"[v] Generate {BATCH_ITEMS} logs. Elapsed {elapsed:.2f}, Velocity {velocity:.2f}, Errors {numerr}")

time.sleep(1e6)
