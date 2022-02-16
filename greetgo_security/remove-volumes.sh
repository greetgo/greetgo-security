#!/bin/sh

cd "$(dirname "$0")"

docker run --rm -i \
  -v "$HOME/volumes/greetgo-security:/volumes" \
  busybox:1.31.0 \
  find /volumes/ -maxdepth 1 -mindepth 1 -exec rm -rf {} \;

