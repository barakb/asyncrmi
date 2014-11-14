package org.async.rmi.modules;

import java.net.UnknownHostException;
import java.rmi.Remote;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public interface Exporter {
    <T extends Remote> T export(Remote impl) throws InterruptedException, UnknownHostException;

    @SuppressWarnings({"UnusedDeclaration", "SpellCheckingInspection"})
    boolean unexport();

    @SuppressWarnings({"UnusedDeclaration", "SpellCheckingInspection"})
    boolean unexport(Remote obj);
}
