#!/bin/sh
set -e

echo "Criando tópico ${TRANSACTIONS_TOPIC}..."
rpk topic create "${TRANSACTIONS_TOPIC}" --brokers redpanda:9092 \
  || echo "Tópico ${TRANSACTIONS_TOPIC} já existe, seguindo em frente."

echo "Criando tópico ${TRANSACTIONS_DLT_TOPIC}..."
rpk topic create "${TRANSACTIONS_DLT_TOPIC}" --brokers redpanda:9092 \
  || echo "Tópico ${TRANSACTIONS_DLT_TOPIC} já existe, seguindo em frente."

echo "Publicando eventos de teste em ${TRANSACTIONS_TOPIC}..."
rpk topic produce "${TRANSACTIONS_TOPIC}" --brokers redpanda:9092 < /infra/redpanda/transactions-events-seed.jsonl

echo "Seed do Redpanda concluído."