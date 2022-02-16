#!/bin/sh

cd "$(dirname "$0")"

echo "%%%"
echo "%%% Останавливаем докеровские образы : docker-compose down"
echo "%%%"

docker-compose down

echo "%%%"
echo "%%% Удаляем вольюмы"
echo "%%%"

sh remove-volumes.sh

echo "%%%"
echo "%%% Запускаем докеровские образы : docker-compose up -d"
echo "%%%"

docker-compose up -d
