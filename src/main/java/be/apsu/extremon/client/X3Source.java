 /*
  * ExtreMon Project
  * Copyright (C) 2009-2012 Frank Marien
  * frank@apsu.be
  *  
  * This file is part of ExtreMon.
  *    
  * ExtreMon is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ExtreMon is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ExtreMon.  If not, see <http://www.gnu.org/licenses/>.
  */

package be.apsu.extremon.client;

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

import be.apsu.extremon.test.X3DisplayTest;

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
				Logger.getLogger(X3DisplayTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			catch (IOException ex)
			{
				Logger.getLogger(X3DisplayTest.class.getName()).log(Level.SEVERE, null, ex);
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
