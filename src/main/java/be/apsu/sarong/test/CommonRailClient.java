/*
 * and open the template in the editor.
 */
package be.apsu.sarong.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author frank
 */
public class CommonRailClient implements Runnable
{
	private BufferedReader				reader	= null;
	private ArrayList<String>			lines;
	private String						line;
	private List<URL>					servers;
	private CommonRailClientListener	listener;
	private boolean						running;
    private Proxy                       proxy;

	public CommonRailClient()
	{
		servers = new ArrayList<URL>();
		lines = new ArrayList<String>();
        proxy=Proxy.NO_PROXY;
	}

	public CommonRailClient addServer(URL url)
	{
		servers.add(url);
		return this;
	}

	public CommonRailClient removeServer(URL url)
	{
		servers.remove(url);
		return this;
	}
    
    public CommonRailClient setProxy(String host, int port)
    {
        if(host==null)
            proxy=Proxy.NO_PROXY;
        else
            proxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        return this;
    }

	public CommonRailClientListener getListener()
	{
		return listener;
	}

	public void setListener(CommonRailClientListener listener)
	{
		this.listener = listener;
	}
	
	public CommonRailClient start()
	{
		new Thread(this).start();
		return this;
	}
	
	public CommonRailClient stop()
	{
		running=false;
		return this;
	}

	public void run()
	{
		running=true;
		while (running)
		{
			for (URL server : servers)
			{
				if (!running)
					break;

				try
				{
                    URLConnection connection=server.openConnection(this.proxy);
					reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					listener.commonRailConnected(server);

					while (running && (line = reader.readLine()) != null)
					{
						if (line.length() == 0)
						{
							listener.commonRailShuttle(lines);
							lines.clear();
						}
						else
						{
							lines.add(line);
						}
					}
					
					reader.close();
					listener.commonRailDisconnected(server);
				}
				catch (UnknownHostException ex)
				{
					Logger.getLogger(SarongTest.class.getName()).log(Level.SEVERE, null, ex);
				}
				catch (IOException ex)
				{
					Logger.getLogger(SarongTest.class.getName()).log(Level.SEVERE, null, ex);
					listener.commonRailDisconnected(server);
				}
                
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(CommonRailClient.class.getName()).log(Level.SEVERE, null, ex);
                }
			}
		}
	}
}
