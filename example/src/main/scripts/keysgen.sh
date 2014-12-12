#!/bin/bash
. ./env.sh

# main entry to create server and client keys signed by ca.

rm -rf $KEYSTORE_DIR
mkdir $KEYSTORE_DIR

./gen-certificate-autority-keys.sh
./gen-key-and-certificate.sh server
./gen-key-and-certificate.sh client


