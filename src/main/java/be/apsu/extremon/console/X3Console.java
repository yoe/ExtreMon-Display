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

package be.apsu.extremon.console;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.swing.JFrame;

import be.apsu.extremon.client.X3Client;
import be.apsu.extremon.client.X3Source;
import be.apsu.extremon.panel.CredentialsDialog;
import be.apsu.extremon.panel.X3Panel;
import be.apsu.extremon.panel.X3PanelListener;
import be.fedict.commons.eid.jca.BeIDProvider;
import be.fedict.commons.eid.jca.BeIDSocketFactory;

public class X3Console implements X3PanelListener
{
	private static final int	FULL_HD_HEIGHT	=1080;
	private static final int	FULL_HD_WIDTH	=1920;
	private X3Panel				panel;
	private JFrame				frame;
	private String				chaliceURL;

	public X3Console(final String svgURL,final String chaliceURL)
	{
		Security.addProvider(new BeIDProvider());

		this.chaliceURL=chaliceURL;
		this.panel=new X3Panel("X3MonTest");
		this.panel.addListener(this);
		this.frame=new JFrame("X3MonTest");
		this.frame.setSize(FULL_HD_WIDTH,FULL_HD_HEIGHT);
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.add(this.panel.getCanvas());
		this.frame.setUndecorated(true);
		this.frame.setVisible(true);

		Authenticator.setDefault(new Authenticator()
		{
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				final PasswordAuthentication authn=CredentialsDialog.requestUserNameAndPassword();
				if(authn==null)
					System.exit(1);
				return authn;
			}
		});

		this.panel.setURI(svgURL);
	}

	@Override
	public final void panelReady(X3Panel panelThatIsReady)
	{
		final X3Client client=new X3Client();
		client.addListener(panelThatIsReady);

		try
		{
			final X3Source source=new X3Source("source",new URL(this.chaliceURL + panelThatIsReady.getSubscription()));
			
			try
			{
				source.setSocketFactory(BeIDSocketFactory.getSSLSocketFactory());
			}
			catch(KeyManagementException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(NoSuchAlgorithmException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			client.addSource(source);
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static void main(String[] args)
	{
		new X3Console(args[0],args[1]);
	}
}
