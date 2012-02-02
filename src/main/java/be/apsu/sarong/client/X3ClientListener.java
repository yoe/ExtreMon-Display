package be.apsu.sarong.client;

import java.util.Set;

public interface X3ClientListener
{
    public void clientData(X3Client client, Set<X3Measure> changes);
}
