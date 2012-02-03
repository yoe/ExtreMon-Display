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

package be.apsu.extremon.test;

import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import be.apsu.extremon.client.X3Client;
import be.apsu.extremon.client.X3ClientListener;
import be.apsu.extremon.client.X3Measure;
import be.apsu.extremon.client.X3Source;
import be.apsu.extremon.dynamics.Action;
import be.apsu.extremon.dynamics.DurationAction;
import be.apsu.extremon.dynamics.Measure;
import be.apsu.extremon.dynamics.MeasureCacheElement;
import be.apsu.extremon.dynamics.SetAction;
import be.apsu.extremon.dynamics.StateAction;
import be.apsu.extremon.dynamics.TimestampAction;
import be.apsu.extremon.panel.X3Panel;
import be.apsu.extremon.panel.X3PanelListener;

public class X3DisplayTest implements X3PanelListener, X3ClientListener
{
	private static final Logger logger=Logger.getLogger(X3DisplayTest.class.getName());
	
    private X3Panel     				panel;
    private JFrame          				frame;
    private List<Measure>   				measures;
    private Map<String,MeasureCacheElement>	labelMeasureAssignments;
    private Properties 						configuration;
    
    public X3DisplayTest()
    {
        Measure     measure;
        Action      action;
        
        panel=new X3Panel("X3MonTest");
        panel.addListener(this);
        frame=new JFrame("X3MonTest");
        frame.setSize(1920,1200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(panel.getCanvas());
        frame.setUndecorated(true);
        frame.setVisible(true);
        
        measures=new ArrayList<Measure>();
        labelMeasureAssignments=new HashMap<String,MeasureCacheElement>();
        
        configuration = new Properties();
        try
        {
        	configuration.load(this.getClass().getResourceAsStream("/x3mon.conf"));
            final String username=configuration.getProperty("username");
            final String password=configuration.getProperty("password");
            
            Authenticator.setDefault(new Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});
        }
        catch (IOException ex)
        {
        	logger.log(Level.SEVERE,"Can't read configuration",ex);
        	System.exit(1);
        }
        

//        measure=new Measure("^be\\.apsu\\.([0-9a-z]+)\\.cpu\\.([0-9]+)\\.cpu\\.(user|nice|system|idle|wait|interrupt|softirq|steal)\\.value$");
//        measure.setCaptures("host,cpu,type");
//        action=new SetAction("width","${value}","cpu.core.${cpu}.${type}.bar");
//        action.setPanel(panel);
//        measure.addAction(action);
//        action=new SetAction("cdata","${type} ${formattedValue} %","cpu.core.${cpu}.${type}.span");
//        action.setFormat("%.0f");
//        action.setPanel(panel);
//        measure.addAction(action);
//        measures.add(measure);
        
        String[] stateColors=new String[]{"fill:#79ffb3;fill-opacity:1",         // OK, green
                "fill:#ffffab;fill-opacity:1",         // WARNING, yellow
                "fill:#ec7c87;fill-opacity:1",         // ALERT,  red
                "fill:#7a70ff;fill-opacity:1",         // MISSING, blue
                "fill:#e154f4;fill-opacity:1"};       // TACKLED, magenta
        
        measure=new Measure("^(be.fedict.eid.mon.(eridu|badtibira|tristan|isolde)).dispatcher.timestamp$");
        measure.setCaptures(",fqhn,hostname");
        action=new TimestampAction();
        measure.addAction(action);
        action.setPanel(panel);

        measures.add(measure);
        
        measure=new Measure("^be.fedict.eid.([a-z]+).trust.(validauthcertchain|revokedauthcertchain).xkms2probe.responsetime$");
        measure.setCaptures("environment,test");
        action=new SetAction("width","${formattedValue}","be.fedict.eid.${environment}.trust.${test}.xkms2probe.responsetime.bar");
        action.setMultiplier(.2);
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.${environment}.trust.${test}.xkms2probe.responsetime.text");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        //be.fedict.eid.ta.trust.validrrncertchain.xkms2probe.responsetime.state
        measure=new Measure("^be.fedict.eid.([a-z]+).trust.(validauthcertchain|revokedauthcertchain).state$");
        measure.setCaptures("environment,test");
        action=new StateAction("style","be.fedict.eid.${environment}.trust.${test}.xkms2probe.responsetime.bar", stateColors);
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        measure=new Measure("^be\\.fedict\\.eid\\.(.+)\\.(http|ocsp|tsl)probe\\.state$");
        measure.setCaptures("prefix,probe");
        action=new StateAction("style","be.fedict.eid.${prefix}.${probe}probe.responsetime.bar", stateColors);
        action.setPanel(panel);
        measure.addAction(action);
        action=new StateAction("style","be.fedict.eid.${prefix}.${probe}probe.state.blob", stateColors);
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
 
        measure=new Measure("^be\\.fedict\\.eid\\.(.+)\\.(http|ocsp|tsl)probe\\.responsetime$");
        measure.setCaptures("prefix,probe");
        action=new SetAction("width","${formattedValue}","be.fedict.eid.${prefix}.${probe}probe.responsetime.bar");
        action.setMultiplier(.2);
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.${prefix}.${probe}probe.responsetime.text");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
       
        
        /*
        
        //be.fedict.eid.pki-ra-devws.responsetime.bar
        
        measure=new Measure("^be.fedict.eid.(pr|ta|dev).trust.(app[12]).disk.(vd[a-z]).disk_(ops|time|octets).(read|write)");
        measure.setCaptures("env,app,dev,op,rw");
        action=new SetAction("width","${formattedValue}","be.fedict.eid.${env}.trust.${app}.disk.${dev}.disk_${op}.${rw}.bar");
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.${env}.trust.${app}.disk.${dev}.disk_${op}.${rw}.span");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
       // be.fedict.eid.dev.trust.app1.cpu.0.cpu.wait.value
       // be.fedict.eid.dev.trust.app1.cpu.0.cpu.wait
         
        measure=new Measure("^be.fedict.eid.(pr|ta|dev).trust.(app[12]).cpu.([0-9]+).cpu.([a-z]+).value");
        measure.setCaptures("env,app,core,type");
        action=new SetAction("width","${formattedValue}","be.fedict.eid.${env}.trust.${app}.cpu.${core}.cpu.${type}.bar");
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.${env}.trust.${app}.cpu.${core}.cpu.${type}.span");
        action.setFormat("%.0f %%");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        measure=new Measure("^be.fedict.eid.(pr|ta|dev).trust.(app[12]).snmp.gauge.cachehits.value");
        measure.setCaptures("env,app,cnt");
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.${env}.trust.${app}.snmp.gauge.cachehits.span");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        measure=new Measure("^be.fedict.eid.(pr|ta|dev).trust.(app[12]).snmp.(counter|gauge).([a-z]+).value");
        measure.setCaptures("env,app,type,cnt");
        action=new SetAction("width","${formattedValue}","be.fedict.eid.${env}.trust.${app}.snmp.${type}.${cnt}.bar");
        action.setPanel(panel);
        action.setMultiplier(.5);
        measure.addAction(action);
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.${env}.trust.${app}.snmp.${type}.${cnt}.span");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        //be.fedict.eid.dev.trust.app1.genericjmx.memorypool-edenspace.memory.max.value
        // be.fedict.eid.dev.trust.app1.memorypool-cmspermgen.memory.used.span
        
        measure=new Measure("^be.fedict.eid.(pr|ta|dev).trust.(app[12]).genericjmx.memorypool-(cmspermgen|edenspace|survivorspace).memory.used.value");
        measure.setCaptures("env,app,type");
        action=new SetAction("width","${formattedValue}","be.fedict.eid.${env}.trust.${app}.memorypool-${type}.memory.used.bar");
        action.setPanel(panel);
        action.setMultiplier(.0000005);
        measure.addAction(action);
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.${env}.trust.${app}.memorypool-${type}.memory.used.span");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); */
        
     
        
        /*
        //be.fedict.eid.eridu.curl.ocsp.eid.belgium.be.httpprobe.responsetime.state
        measure=new Measure("^be.fedict.eid.([a-z]+).curl.([a-z0-9._]+).httpprobe.responsetime.state$");
        measure.setCaptures("host,env");
        action=new StateAction("style","be.fedict.eid.${env}.response_time.bar", stateColors);
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        // */
        
        //be.fedict.eid.tsl.belgium.be.tslprobe.responsetime
        measure=new Measure("^be.fedict.eid.ext.tsl.tslprobe.responsetime.state$");
        action=new StateAction("style","be.fedict.eid.tsl.belgium.be.tslprobe.responsetime.bar", stateColors);
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        // be.fedict.eid.tsl.belgium.be.tslprobe.responsetime
        measure=new Measure("^be.fedict.eid.ext.tsl.tslprobe.responsetime$");
        action=new SetAction("width","${formattedValue}","be.fedict.eid.tsl.belgium.be.tslprobe.responsetime.bar");
        action.setMultiplier(.3);
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("cdata","${formattedValue}","be.fedict.eid.tsl.belgium.be.tslprobe.responsetime.text");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
       
        
        //be.fedict.eid.tsl.belgium.be.tslprobe.validityleft.blob
        measure=new Measure("^be.fedict.eid.ext.tsl.tslprobe.validityleft.state$");
        action=new StateAction("style","be.fedict.eid.ext.tsl.tslprobe.validityleft.blob", stateColors);
        action.setPanel(panel);
        measure.addAction(action);
        measures.add(measure); 
        
        measure=new Measure("^be.fedict.eid.ext.tsl.tslprobe.validityleft$");
        action=new DurationAction("cdata","${formattedValue}","be.fedict.eid.ext.tsl.tslprobe.validityleft.span");
        measure.addAction(action);
        action.setPanel(panel);
        measures.add(measure);
        
        //                    be.fedict.eid. prod.                                pki-ra-por.                       app1.     df.opt-eid-pki-ra-app.df_complex.reserved.value
        measure=new Measure("^be.fedict.eid.(prod|ta|int|mon).(dss|pki-ra-mod|idp|pki-ra-por|trust|tristan|isolde).(app[0-9]).df.([a-z-]+).df_complex.(free|used|reserved).percentage$");
        measure.setCaptures("env,app,host,mountpoint,metric");
        action=new SetAction("cdata","${formattedValue} %","be.fedict.eid.${env}.${app}.${host}.df.${mountpoint}.${metric}.percentage.span");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("width","${formattedValue}","be.fedict.eid.${env}.${app}.${host}.df.${mountpoint}.${metric}.percentage.bar");
        action.setPanel(panel);
        measure.addAction(action);  
        measures.add(measure);
        
        //                     be.fedict.eid.             mon.                                          isolde.cpu.2.cpu.user.value
        measure=new Measure("^(be.fedict.eid.(prod|ta|int|mon)\\.(dss|pki-ra-mod|idp|pki-ra-por|trust|tristan|isolde)\\.?(app[0-9]))?\\.cpu.([0-9]+)\\.cpu\\.(idle|interrupt|nice|softirq|steal|system|user|wait)\\.value$");
        measure.setCaptures("prefix,env,app,host,core,metric");
        action=new SetAction("cdata","${formattedValue} % ${metric}","${prefix}.cpu.${core}.cpu.${metric}.value.text");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("width","${formattedValue}","${prefix}.cpu.${core}.cpu.${metric}.value.bar");
        action.setPanel(panel);
        measure.addAction(action);  
        measures.add(measure);
        
        //                    be.fedict.eid.mon.                          isolde.cpu.2       .cpu.user.value
       /* measure=new Measure("^be.fedict.eid.mon.(eridu|badtibira|tristan|isolde).cpu.([0-9]+).cpu.(idle|interrupt|nice|softirq|steal|system|user|wait).value$");
        measure.setCaptures("host,core,metric");
        action=new SetAction("cdata","${formattedValue} % ${metric}","be.fedict.eid.${host}.cpu.${core}.cpu.${metric}.value.text");
        action.setFormat("%.0f");
        action.setPanel(panel);
        measure.addAction(action);
        action=new SetAction("width","${formattedValue}","be.fedict.eid.mon.${host}.cpu.${core}.cpu.${metric}.value.bar");
        action.setPanel(panel);
        measure.addAction(action);  
        measures.add(measure); */
        
        
        panel.setURI("file:///data/eid_prod_2012_02_01.svg");
    }

    public static void main(String[] args)
    {
        new X3DisplayTest();
    }

    @Override
    public void panelReady(X3Panel panel)
    {
    	X3Client client=new X3Client();
    		     client.addListener(this);
    	
		try
		{
			client.addSource(new X3Source(configuration.getProperty("host0_name"),new URL(configuration.getProperty("host0_url"))));
			client.addSource(new X3Source(configuration.getProperty("host1_name"), new URL(configuration.getProperty("host1_url"))));
		} 
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
    }

	public void clientData(X3Client client, Set<X3Measure> changes)
	{
		panel.startUpdate();
		
		try
		{
			for(X3Measure change:changes)
			{
				logger.log(Level.FINEST,"CLIENTDATA [" + change.label + "=" + change.value + "]");
				
				if(labelMeasureAssignments.containsKey(change.label))
	        	{
	        		MeasureCacheElement mceFound=labelMeasureAssignments.get(change.label);
	            	if(mceFound!=null)
	            	{
	            		try
		                {
	            			mceFound.getMeasure().act(mceFound.getVariables(),change.value);
		                }
		                catch(Exception ex)
		                {
		                    System.err.println(ex.toString());
		                }
	            	}
	        	}
	        	else
	        	{
	        		boolean found=false;
		            for(Measure measure: measures)
		            {
		                try
		                {
		                	Map<String,String> variables=measure.evaluate(change.label, change.value);
		                    if(variables!=null)
		                    {
		                    	logger.finest("Caching " + change.label);
		                    	labelMeasureAssignments.put(change.label, new MeasureCacheElement(measure, variables));
		                    	measure.act(variables, change.value);
		                    	found=true;
		                        break;
		                    }
		                }
		                catch(Exception ex)
		                {
		                	logger.log(Level.INFO,"client data interpretation error",ex);
		                }
		            }
		            
		            if(!found)
		            	labelMeasureAssignments.put(change.label,null);	// negative cache entry
	        	}
			}
		}
		finally
		{
			panel.endUpdate();
		}	
	}
}

