package org.async.rmi.modules;

import java.rmi.Remote;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public interface Exporter {
    Remote export(Remote impl);

    @SuppressWarnings({"UnusedDeclaration", "SpellCheckingInspection"})
    boolean unexport();

    @SuppressWarnings({"UnusedDeclaration", "SpellCheckingInspection"})
    boolean unexport(Remote obj);
}
