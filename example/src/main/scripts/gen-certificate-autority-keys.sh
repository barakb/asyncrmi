#!/bin/bash
. ./env.sh


echo -e "${Yellow} - Generating ca keys ${Color_Off}"

rm -f $KEYSTORE_DIR/ca.keystore

keytool -genkeypair  -keysize 2048 -genkey -alias ca -keyalg RSA -keystore $KEYSTORE_DIR/ca.keystore\
 -storepass $PASSWORD -keypass $PASSWORD \
 -dname "CN=Certificate Authority, OU=Async, O=RMI, L=Avigdor, S=NA, C=ISRAEL"
