#!/bin/bash
. ./env.sh

CN=$1

echo -e "${Yellow} - Generating ${CN} keys ${Color_Off}"
keytool -genkeypair  -keysize 2048 -genkey -alias ${CN} -keyalg RSA -keystore $KEYSTORE_DIR/${CN}.keystore\
 -storepass $PASSWORD -keypass $PASSWORD \
 -dname "CN=${CN}, OU=Async, O=RMI, L=Avigdor, S=NA, C=ISRAEL"


echo -e "${Yellow} - Generating ${CN} certificate chain ${Color_Off}"
echo -e "${Yellow}   - Adding ca certificate as trustcacerts ${Color_Off}"
keytool -keystore $KEYSTORE_DIR/ca.keystore -alias ca -storepass $PASSWORD -keypass $PASSWORD -exportcert | \
keytool -keystore $KEYSTORE_DIR/${CN}.keystore -alias ca-certificate -storepass $PASSWORD -keypass $PASSWORD\
 -v -noprompt -trustcacerts -importcert

echo -e "${Yellow} - request ${CN} certificate from CA${Color_Off}"
keytool -keystore $KEYSTORE_DIR/${CN}.keystore -alias ${CN} -storepass $PASSWORD -keypass $PASSWORD -certreq | \
keytool -keystore $KEYSTORE_DIR/ca.keystore -alias ca -storepass $PASSWORD -keypass $PASSWORD -gencert | \
keytool -keystore $KEYSTORE_DIR/${CN}.keystore -alias ${CN} -storepass $PASSWORD -keypass $PASSWORD\
 -noprompt -importcert

#echo -e "${Yellow} - List ${CN} keystore${Color_Off}"
#./ls.sh $KEYSTORE_DIR/${CN}.keystore



