#!/bin/sh

cd "$(dirname "$0")"

echo "%%%"
echo "%%% Останавливаем докеровские образы : docker-compose down"
echo "%%%"

docker-compose down

echo "%%%"
echo "%%% Удаляем вольюмы"
echo "%%%"

mkdir -p volumes

docker run --rm -i \
  -v "$PWD/volumes:/volumes" \
  busybox:1.31.0 \
  find /volumes/ -maxdepth 1 -mindepth 1 -exec rm -rf {} \;

rm -rvf volumes/

echo "%%%"
echo "%%% Запускаем докеровские образы : docker-compose up -d"
echo "%%%"

mkdir -p volumes
docker-compose up -d