#!/bin/sh
set -e

echo "Aguardando DynamoDB Local ficar disponível..."
until aws dynamodb list-tables --endpoint-url "${DYNAMODB_ENDPOINT}" --region "${DYNAMODB_REGION}" > /dev/null 2>&1; do
  sleep 1
done

echo "Criando tabela ${BALANCES_TABLE_NAME}..."
aws dynamodb create-table \
  --endpoint-url "${DYNAMODB_ENDPOINT}" \
  --region "${DYNAMODB_REGION}" \
  --table-name "${BALANCES_TABLE_NAME}" \
  --attribute-definitions AttributeName=account_id,AttributeType=S \
  --key-schema AttributeName=account_id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  || echo "Tabela ${BALANCES_TABLE_NAME} já existe, seguindo em frente."