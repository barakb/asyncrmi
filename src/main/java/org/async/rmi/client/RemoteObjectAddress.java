package org.async.rmi.client;

import java.io.*;

/**
 * Created by Barak Bar Orion
 * 08/10/14.
 */
public class RemoteObjectAddress implements Serializable, Externalizable{
    private String url;
    private long objectId;
    private long classLoaderId;

    public RemoteObjectAddress() {
    }

    public RemoteObjectAddress(String url, long objectId, long classLoaderId) {
        this.url = url;
        this.objectId = objectId;
        this.classLoaderId = classLoaderId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getUrl() {
        return url;
    }

    @SuppressWarnings("UnusedDeclaration")
    public long getObjectId() {
        return objectId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public long getClassLoaderId() {
        return classLoaderId;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(url);
        out.writeLong(objectId);
        out.writeLong(classLoaderId);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        url = in.readUTF();
        objectId = in.readLong();
        classLoaderId = in.readLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteObjectAddress that = (RemoteObjectAddress) o;

        if (classLoaderId != that.classLoaderId) return false;
        if (objectId != that.objectId) return false;
        //noinspection RedundantIfStatement
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (int) (objectId ^ (objectId >>> 32));
        result = 31 * result + (int) (classLoaderId ^ (classLoaderId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "RemoteObjectAddress{" +
                "url='" + url + '\'' +
                ", objectId=" + objectId +
                ", classLoaderId=" + classLoaderId +
                '}';
    }
}
