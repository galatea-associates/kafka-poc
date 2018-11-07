import atexit
import csv
import time

from kafka import KafkaConsumer
from Counter import Counter
from multiprocessing import Manager, Process

from DataConfiguration import configuration


def close_consumer(consumer):
    consumer.close()

def recieve(counter, topic):
    consumer = get_consumer(topic)
    atexit.register(close_consumer, consumer)
    for msg in consumer:
        counter.increment()

def get_consumer(topic):
    return KafkaConsumer(topic, bootstrap_servers=['ec2-3-8-1-159.eu-west-2.compute.amazonaws.com:9092'])

def count_msgs_every_second(counter, topic, time_interval, prev_time, shared_dict):
    while True:
        if time.time() - prev_time >= time_interval:
            counter_size = counter.value()
            counter.reset()
            #print("Topic " + topic + " recieved " + str(counter_size) + " messages!")
            shared_dict[topic].append(int(counter_size))
            prev_time = time.time()

def start_recieving(topic, time_interval, numb_procs):
    counter = Counter(0)
    shared_dict[topic] = manager.list()
    procs = [Process(target=recieve, args=(counter, topic)) for i in range(numb_procs)]
    for p in procs: p.start()
    timer_proc = Process(target=count_msgs_every_second, args=(counter, topic, time_interval, time.time(), shared_dict))
    timer_proc.start()
    procs.append(timer_proc)
    return procs

def cleanup_processes(procs):
    for p in procs: p.terminate()

def produce_output(dict_key, output_time):
    if len(shared_dict[dict_key]) == 0:
        return
    print(dict_key + " - Mean: " + str(sum(shared_dict[dict_key]) / len(shared_dict[dict_key])) + " Max: " + str(max(shared_dict[dict_key])) + " Min: " + str(min(shared_dict[dict_key])) ) 
    with open("output-recieve-" + str(int(output_time)) + ".csv", 'a', newline='') as output_file:
        wr = csv.writer(output_file, quoting=csv.QUOTE_ALL)
        wr.writerow(shared_dict[dict_key])

def cleanup(topics_procs):
    for procs in topics_procs:
        cleanup_processes(procs)
    output_time = time.time()
    produce_output(dict_key="prices", output_time=output_time)
    produce_output(dict_key="positions", output_time=output_time)
    produce_output(dict_key="instrument_reference_data", output_time=output_time)


def process_data_config(config):

    topics_procs = []

    for topic in config:
        procs = start_recieving(topic=topic, numb_procs=config[topic]["Number of Processes"], time_interval=config[topic]["Time Interval"])
        topics_procs.append(procs)

    return topics_procs

if __name__ == '__main__':
    global manager, shared_dict

    manager =  Manager()
    shared_dict = manager.dict()    

    topics_procs = process_data_config(configuration)

    atexit.register(cleanup, topics_procs=topics_procs)
    input("Press Enter to exit...")