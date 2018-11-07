import atexit
import csv
import time

from argparse import ArgumentParser
from Counter import Counter
from kafka import KafkaProducer
from multiprocessing import Manager, Process

def process_val(val):
    if isinstance(val, bytes):
        return val
    elif isinstance(val, str):
        return val.encode()
    elif callable(val):
        return val()
    else:
        return b''

def send(server_args, counter, topic, val, wait_for_response):
    producer = KafkaProducer(bootstrap_servers=[str(server_args.ip) +":"+ str(server_args.port)])
    atexit.register(cleanup_producer, producer=producer)
    while True:
        while counter.check_value_and_increment():
            if wait_for_response:
                future = producer.send(topic, process_val(val))
                result = future.get(timeout=60)
            else:
                producer.send(topic, val)


def reset_every_second(counter, topic, time_interval, prev_time, shared_dict):
    while True:
        if time.time() - prev_time >= time_interval:
            counter_size = counter.value()
            counter.reset()
            #print("Topic " + topic + " sent " + str(counter_size) + " messages!")
            shared_dict[topic].append(int(counter_size))
            prev_time = time.time()

def start_sending(server_args, counter, topic, val, numb_procs, time_interval, wait_for_response=True):
    shared_dict[topic] = manager.list() 
    procs = [Process(target=send, args=(server_args, counter, topic, val, wait_for_response)) for i in range(numb_procs)]
    for p in procs: p.start()
    timer_proc = Process(target=reset_every_second, args=(counter, topic, time_interval, time.time(), shared_dict))
    timer_proc.start()
    procs.append(timer_proc)
    return procs

def cleanup_processes(procs):
    for p in procs: p.terminate()

def cleanup_producer(producer):
    producer.close()

def produce_output(dict_key, output_time):
    if len(shared_dict[dict_key]) == 0:
        return
    print(dict_key + " - Mean: " + str(sum(shared_dict[dict_key]) / len(shared_dict[dict_key])) + " Max: " + str(max(shared_dict[dict_key])) + " Min: " + str(min(shared_dict[dict_key])) ) 
    with open("output-send-" + str(int(output_time)) + ".csv", 'a', newline='') as output_file:
        wr = csv.writer(output_file, quoting=csv.QUOTE_ALL)
        wr.writerow(shared_dict[dict_key])

def cleanup(topics_procs):
    for procs in topics_procs:
        cleanup_processes(procs)
    output_time = time.time()
    #TODO: Automate the key selection.
    produce_output(dict_key="prices", output_time=output_time)
    produce_output(dict_key="positions", output_time=output_time)
    produce_output(dict_key="instrument_reference_data", output_time=output_time)

def parse_args():
    parser = ArgumentParser()
    parser.add_argument("-i", "--serverIP", dest="ip",
                        help="Kafka server address", required=True)
    parser.add_argument("-p", "--serverPort", dest="port",
                        help="Kafka server port", default=9092 )

    args = parser.parse_args()
    return args


if __name__ == '__main__':
    global manager, shared_dict

    server_args = parse_args()

    manager =  Manager()
    shared_dict = manager.dict()    

    topics_procs = []
    #TODO: Read configuration externally (requires no more hard coding data)
    counter1 = Counter(init_val=0, limit_val=40000)
    procs = start_sending(server_args=server_args, counter=counter1, topic='prices', val=b'1.0', numb_procs=11, time_interval=1.0)
    topics_procs.append(procs)

    counter2 = Counter(init_val=0, limit_val=20000)
    procs = start_sending(server_args=server_args, counter=counter2, topic='positions', val=b'This is the position data', numb_procs=4, time_interval=1.0)
    topics_procs.append(procs)

    counter3 = Counter(init_val=0, limit_val=100)
    procs = start_sending(server_args=server_args, counter=counter3, topic='instrument_reference_data', val=b'InstRef', numb_procs=1, time_interval=60.0)
    topics_procs.append(procs)

    atexit.register(cleanup, topics_procs=topics_procs)
    input("Press Enter to exit...")

