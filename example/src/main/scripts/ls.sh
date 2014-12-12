#!/bin/bash
. ./env.sh
if [ $# -eq 0 ]
then
    for i in $( ls -v $KEYSTORE_DIR/*.keystore ); do
        echo
        echo "listing entries in: $i"
        echo "------------------------------------------"
        keytool -list -v -storepass $PASSWORD -keypass $PASSWORD -keystore $i;
    done
else
        echo
        echo "listing entries in: $1"
        echo "------------------------------------------"
        keytool -list -v -storepass $PASSWORD -keypass $PASSWORD -keystore $1;
fi


#


#D:\ssl-article\examples>keytool -import -v -noprompt -trustcacerts -alias verisigndemocert -file verisign-demo-root-cert.pem -keystore server_key
#store.jks -storepass weblogic1234
#Certificate was added to keystore
#[Saving server_keystore.jks]