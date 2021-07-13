#!/bin/sh

cd "$(dirname "$0")"

echo "%%%"
echo "%%% Останавливаем докеровские образы : docker-compose down"
echo "%%%"

docker-compose down

echo "%%%"
echo "%%% Удаляем вольюмы : sudo rm -rf ./volumes/"
echo "%%%"

docker run --rm -i \
  -v "$PWD/volumes:/volumes" \
  busybox:1.31.0 \
  find /volumes/ -maxdepth 1 -mindepth 1 -exec rm -rf {} \;

rm -rf ./volumes/

