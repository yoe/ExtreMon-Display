package be.apsu.sarong.client;

import java.util.List;

/**
 *
 * @author frank@apsu.be
 */
public interface X3SourceListener
{
    public void sourceConnected		(X3Source source);
    public void sourceDisconnected	(X3Source source);
    public void sourceData			(X3Source source, double timeStamp, List<X3Measure> measures);
}
