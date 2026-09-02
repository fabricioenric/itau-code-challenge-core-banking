#!/bin/sh
set -e

echo "Criando tópico ${TRANSACTIONS_TOPIC}..."
rpk topic create "${TRANSACTIONS_TOPIC}" --brokers redpanda:9092 \
  || echo "Tópico ${TRANSACTIONS_TOPIC} já existe, seguindo em frente."

echo "Criando tópico ${TRANSACTIONS_DLT_TOPIC}..."
rpk topic create "${TRANSACTIONS_DLT_TOPIC}" --brokers redpanda:9092 \
  || echo "Tópico ${TRANSACTIONS_DLT_TOPIC} já existe, seguindo em frente."

echo "Publicando eventos de teste em ${TRANSACTIONS_TOPIC}..."
# Chama o script gerador de eventos, passando o tópico e a quantidade de eventos (ex: 20)
# O script 'produce-transactions-events.sh' é um script Bash, então o chamamos com 'bash'.
bash /infra/redpanda/produce-transactions-events.sh "${TRANSACTIONS_TOPIC}" 20

echo "Seed do Redpanda concluído."