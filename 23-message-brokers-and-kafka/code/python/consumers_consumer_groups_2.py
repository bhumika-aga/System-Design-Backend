from confluent_kafka import Consumer

c = Consumer({
    "bootstrap.servers": "localhost:9092",
    # the consumer GROUP, scale by adding instances
    "group.id": "billing",
    # where to start if no committed offset (sec 8)
    "auto.offset.reset": "earliest",
    # commit manually for at-least-once (sec 9)
    "enable.auto.commit": False,
})
# Kafka assigns this instance some partitions
c.subscribe(["orders"])

try:
    while True:
        msg = c.poll(1.0)  # wait up to 1s for the next record
        if msg is None:
            continue
        if msg.error():
            print("error:", msg.error());
            continue

        process(msg.value())  # do the work FIRST...
        # ...THEN commit the offset (at-least-once, sec 9)
        c.commit(msg)
finally:
    # leave the group cleanly -> triggers a rebalance
    c.close()
