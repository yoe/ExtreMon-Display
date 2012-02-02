package be.apsu.sarong.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.apsu.sarong.test.SarongTest;

public class X3Source implements Runnable
{
	private String						name;
	private URL 						url;
	private Proxy						proxy;
	private X3SourceListener			listener;
	private boolean						running;
	
	public X3Source(String name, URL url)
	{
		super();
		this.name=name;
		this.url=url;
		setProxy(null,0);
	}
	
	public final X3Source setProxy(String host, int port)
    {
        if(host==null)
            proxy=Proxy.NO_PROXY;
        else
            proxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        return this;
    }
	
	public X3Source start(X3SourceListener listener)
	{
		this.listener=listener;
		new Thread(this,"X3Source [" + url.toString() + "]").start();
		return this;
	}
	
	public X3Source stop()
	{
		running=false;
		return this;
	}
	
	@Override
	public void run()
	{
		running=true;
		while (running)
		{
			try
			{
                URLConnection 			connection	=url.openConnection(this.proxy);
				BufferedReader 			reader 		=new BufferedReader(new InputStreamReader(connection.getInputStream()));
				ArrayList<X3Measure>	lines		=new ArrayList<X3Measure>();
				String					line;
				double					timeStamp	=0;
				
				listener.sourceConnected(this);

				while(running && (line=reader.readLine())!=null)
				{
					if(line.length()==0)
					{
						listener.sourceData(this,timeStamp,lines);
						lines.clear();
						timeStamp=0;
					}
					else
					{
						String[] labelValue=line.split("=");
			            if(labelValue.length==2)
			            {
			            	lines.add(new X3Measure(labelValue[0],labelValue[1]));
			            	
			            	if(labelValue[0].endsWith("timestamp"))
			            	{
			            		try
			            		{
			            			timeStamp=Double.parseDouble(labelValue[1]);
			            		}
			            		catch(NumberFormatException nfe)
			            		{
			            			Logger.getLogger(X3Source.class.getName()).log(Level.SEVERE, "failed to parse timestamp", nfe);
			            		}
			            	}
			            }
					}
				}
				
				reader.close();
				listener.sourceDisconnected(this);
			}
			catch (UnknownHostException ex)
			{
				Logger.getLogger(SarongTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			catch (IOException ex)
			{
				Logger.getLogger(SarongTest.class.getName()).log(Level.SEVERE, null, ex);
				listener.sourceDisconnected(this);
			}
            
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(X3Source.class.getName()).log(Level.SEVERE, null, ex);
            }
		}
	}

	public String getName()
	{
		return name;
	}
}
