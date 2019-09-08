# Review Streamer

## Start up Postgres
```shell
docker-compose up -d
```

## Create Topics
```shell
./kafka_2.12-2.3.0/bin/kafka-topics.sh --create --topic udemy-reviews-valid --partitions 3 --replication-factor 1 --zookeeper localhost:2181
./kafka_2.12-2.3.0/bin/kafka-topics.sh --create --topic udemy-reviews-fraud --partitions 3 --replication-factor 1 --zookeeper localhost:2181
./kafka_2.12-2.3.0/bin/kafka-topics.sh --create --topic long-term-stats --partitions 3 --replication-factor 1 --zookeeper localhost:2181
./kafka_2.12-2.3.0/bin/kafka-topics.sh --create --topic recent-stats --partitions 3 --replication-factor 1 --zookeeper localhost:2181
```

## Reset Application Consumer Group
```shell
./kafka_2.12-2.3.0/bin/kafka-streams-application-reset.sh --application-id fraud-detector --input-topics udemy-reviews --zookeeper localhost:2181
```

## Designed from 
https://medium.com/@stephane.maarek/how-to-use-apache-kafka-to-transform-a-batch-pipeline-into-a-real-time-one-831b48a6ad85