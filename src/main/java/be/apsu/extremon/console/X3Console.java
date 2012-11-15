/* ExtreMon Project
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JFrame;

import be.apsu.extremon.client.X3Client;
import be.apsu.extremon.client.X3Drain;
import be.apsu.extremon.client.X3Source;
import be.apsu.extremon.panel.CredentialsDialog;
import be.apsu.extremon.panel.ResponderListener;
import be.apsu.extremon.panel.X3Panel;
import be.apsu.extremon.panel.X3PanelListener;
import be.fedict.commons.eid.client.BeIDCards;
import be.fedict.commons.eid.jca.BeIDProvider;
import be.fedict.commons.eid.jca.BeIDSocketFactory;

public class X3Console implements X3PanelListener, ResponderListener {
    private static final Logger LOGGER = Logger.getLogger(X3Console.class
	    .getName());
    private static final int FULL_HD_HEIGHT = 1080;
    private static final int FULL_HD_WIDTH = 1920;
    private X3Panel panel;
    private JFrame frame;
    private String chaliceURL, responderURL;
    private X3Source source;
    private X3Drain drain;
    private BeIDCards beID;

    public X3Console(final String svgURL, final String chaliceURL,
	    final String responderURL) {
	Security.addProvider(new BeIDProvider());

	this.chaliceURL = chaliceURL;
	this.responderURL = responderURL;
	this.panel = new X3Panel("X3MonTest");
	this.panel.addPanelListener(this);
	this.beID = new BeIDCards();
	this.frame = new JFrame("X3MonTest");
	this.frame.setSize(FULL_HD_WIDTH, FULL_HD_HEIGHT);
	this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	this.frame.add(this.panel.getCanvas());
	this.frame.setUndecorated(true);
	this.frame.setVisible(true);

	Authenticator.setDefault(new Authenticator() {
	    @Override
	    protected PasswordAuthentication getPasswordAuthentication() {
		final PasswordAuthentication authn = CredentialsDialog
			.requestUserNameAndPassword();
		if (authn == null)
		    System.exit(1);
		return authn;
	    }
	});

	if (beID.hasBeIDCards()) {
	    try {
		HttpsURLConnection.setDefaultSSLSocketFactory(BeIDSocketFactory
			.getSSLSocketFactory());
	    } catch (KeyManagementException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	this.panel.setURI(svgURL);
    }

    @Override
    public final void panelReady(X3Panel panelThatIsReady) {
	final X3Client client = new X3Client();
	client.addListener(panelThatIsReady);

	try {
	    source = new X3Source("source", new URL(this.chaliceURL
		    + panelThatIsReady.getSubscription()));

	    if (beID.hasBeIDCards()) {
		try {
		    source.setSocketFactory(BeIDSocketFactory
			    .getSSLSocketFactory());
		} catch (KeyManagementException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	    client.addSource(source);
	} catch (MalformedURLException e) {
	    LOGGER.log(Level.SEVERE, "Chalice URL is Unusable", e);
	}

	if (beID.hasBeIDCards()) {
	    try {
		drain = new X3Drain("drain", new URL(this.responderURL));
		this.panel.addResponderListener(this);

		try {
		    drain.setSocketFactory(BeIDSocketFactory
			    .getSSLSocketFactory());
		} catch (KeyManagementException e) {
		    LOGGER.log(Level.SEVERE, "BeID Key Management Failed", e);
		} catch (NoSuchAlgorithmException e) {
		    LOGGER.log(
			    Level.SEVERE,
			    "ExtreMon BeID Identification requires missing algorithm",
			    e);
		}

		drain.start();
	    } catch (MalformedURLException e) {
		LOGGER.log(Level.SEVERE, "Libation URL is Unusable", e);
	    }
	}

    }

    @Override
    public void responding(String label, boolean responding) {
	System.err.println("sending [" + label + ".responding=" + responding
		+ "]");
	drain.put(label + ".responding", responding);
    }

    @Override
    public void responderComment(String label, String comment) {
	drain.put(label + ".responder.comment", comment);
	System.err.println("sending [" + label + ".responder.comment="
		+ comment + "]");
    }

    @Override
    public void heartBeat(double timeDiff) {
	drain.put("user.lag", timeDiff);
    }

    public static void main(String[] args) {
	new X3Console(args[0], args[1], args[2]);
    }
}
