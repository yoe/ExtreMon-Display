package be.apsu.sarong.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class X3Client implements X3SourceListener
{
	private static final Logger logger=Logger.getLogger(X3Client.class.getName());
	
	private Set<X3Source> 							sources;
	private Set<X3ClientListener>					listeners;
	private Map<String,Map<X3Source,TimeValue>>		cache;
	private Set<X3Measure>							shuttle;
	
	public X3Client()
	{
		super();
		sources		=new HashSet<X3Source>();
		listeners	=new HashSet<X3ClientListener>();
		cache		=new HashMap<String,Map<X3Source,TimeValue>>();
		shuttle		=new HashSet<X3Measure>();
	}

	public void addSource(X3Source source)
	{
		this.sources.add(source);
		source.start(this);
	}
	
	public void removeSource(X3Source source)
	{
		source.stop();
		this.sources.add(source);
	}
	
	public void addListener(X3ClientListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(X3ClientListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void sourceConnected(X3Source source)
	{
	}

	@Override
	public void sourceDisconnected(X3Source source)
	{
	}

	@Override
	public synchronized void sourceData(X3Source source, double timeStamp, List<X3Measure> measures)
	{
		for(X3Measure measure: measures)
		{
			logger.log(Level.FINEST,"SOURCEDATA " + source.getName() + " [" + measure.label + "=" + measure.value + "]");
			
        /*	Map<X3Source,TimeValue> sourceTimeValue=cache.get(measure.label);
        
        	if(sourceTimeValue==null)
        	{
        		logger.finest("\tADDLBL " + measure.label + "(" + source.getName() + ")");
        		sourceTimeValue=new HashMap<X3Source,TimeValue>();
        		sourceTimeValue.put(source,new TimeValue(timeStamp,measure.value));
        		cache.put(measure.label, sourceTimeValue);
        	}
        	else
        	{
        		TimeValue timeValue=sourceTimeValue.get(source);
        		if(timeValue==null)
        		{
        			logger.finest("\tADDSRC " + measure.label + "(" + source.getName() + ")");
        			timeValue=new TimeValue(timeStamp,measure.value);
        			sourceTimeValue.put(source,timeValue);
        		}
        		else
        		{
        			logger.finest("\tUPDATE " + measure.label + "(" + source.getName() + ")");
        			timeValue.setValue(measure.value);
        		}
        	} */
        	
        	shuttle.add(measure);
        }
		
		for(X3ClientListener listener: listeners)
			listener.clientData(this, shuttle);
		shuttle.clear();
	}
}
