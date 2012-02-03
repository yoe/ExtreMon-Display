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

package be.apsu.extremon.panel;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.AbstractImageZoomInteractor;
import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.swing.gvt.AbstractResetTransformInteractor;
import org.apache.batik.swing.gvt.Interactor;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherAdapter;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;

import be.apsu.extremon.dynamics.Alteration;
import be.apsu.extremon.dynamics.Alternator;
import be.apsu.extremon.elements.TimeSpinnerLine;
import be.apsu.extremon.svgutils.SVGUtils;

public class X3Panel implements Runnable
{
	private static final Logger logger=Logger.getLogger(X3Panel.class.getName());
				
	
    private static final String SVG_ID = "id";
    private static final String SVG_TRANSFORM = "transform";
    private static final String SVG_USE = "use";
    private static final String X3MON_ID = "id";
    private static final String XLINK_HREF = "href";
    private static final String X3MON_NS = "http://extremon.org/ns/extremon";
    private static final String X3MON_USAGE = "usage";
    private static final String XLINK_NS_URL = "http://www.w3.org/1999/xlink";
    
    private String 								name;
    private X3Canvas 						canvas;
    private SVGDocument 						document;
    private Set<X3PanelListener> 			listeners;
    private HashMap<String, Element> 			liveElements;
    private HashMap<String, TimeSpinnerLine>	liveSpinnerElements;
    private BlockingQueue<Set<Alteration>>  	alterations;
    private Set<Alteration>						alterationsInShuttle;
	private boolean								running;
	private long								lastRemoteTimestamp;
    private Calendar    						timestampCalendar;
    private Format      						timestampFormat,lagFormat;
    private Element								tsRemoteElem,tsLocalElem;
    private Element								tsDifferenceElem;
    private TimeSpinnerLine						tsRemoteProgressElem,tsLocalProgressElem;
 
    
    public X3Panel addKeyListener(KeyListener keyListener)
    {
        canvas.addKeyListener(keyListener);
        return this;
    }

    public X3Panel reset()
    {
        canvas.resetRenderingTransform();
        return this;
    }

    Element createDescription(String text)
    {
        Element description = document.createElementNS("http://www.w3.org/2000/svg", "desc");
        Text descriptionText = document.createTextNode(text);
        description.appendChild(descriptionText);
        return description;
    }

    void generateTooltips()
    {
        queueUpdate(new Runnable()
        {
            @Override
            public void run()
            {
                NodeList allNodes = document.getElementsByTagName("*");
                for (int i = 0; i < allNodes.getLength(); i++)
                {
                    Node node = allNodes.item(i);
                    Node idNode = node.getAttributes().getNamedItem("id");
                    if (idNode != null)
                    {
                        String id = idNode.getNodeValue();
                        String[] idParts = id.split("\\.");

                        if (idParts.length > 4)
                        {
                            logger.finest("creating description for " + id);
                            Element desc = createDescription(id.substring(0, id.lastIndexOf('.')));

                            Element element = (Element) node;
                            NodeList descriptions = element.getElementsByTagName("desc");
                            if (descriptions.getLength() > 0)
                            {
                                element.removeChild(descriptions.item(0));
                            }
                            node.appendChild(desc);
                        }
                    }
                }
            }
        });
    }

    public X3Panel(String name)
    {
    	logger.setLevel(Level.ALL);
        this.name = name;
        //this.canvas = new SarongCanvas();
        this.canvas = new X3Canvas(new X3UserAgent(),true,true);
        this.canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        this.canvas.setAnimationLimitingNone();
        this.canvas.setBackground(Color.black);
        this.listeners = new HashSet<X3PanelListener>();
        this.liveElements = new HashMap<String, Element>();
        this.liveSpinnerElements = new HashMap<String, TimeSpinnerLine>();
        this.alterations = new ArrayBlockingQueue<Set<Alteration>>(10);
        this.alterationsInShuttle=new HashSet<Alteration>(32);
        this.timestampCalendar=Calendar.getInstance();
        this.timestampFormat=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.");
        this.lagFormat = new DecimalFormat("+0000 ms;-0000 ms");

        this.canvas.addSVGLoadEventDispatcherListener(new SVGLoadEventDispatcherAdapter()
        {

            @Override
            public void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e)
            {
                super.svgLoadEventDispatchCompleted(e);
                document = canvas.getSVGDocument();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e)
            {
                super.svgLoadEventDispatchCompleted(e);

                materializeTemplateUsages(document);
                removeTemplates(document);
                registerLiveElements(document);
                
                tsRemoteElem 			= getElementById("timestamp-remote");
                tsLocalElem 			= getElementById("timestamp-local");
                tsDifferenceElem 		= getElementById("timestamp-difference");
                
                tsRemoteProgressElem 	= getSpinnerById("timestamp-remote-progress");
                tsLocalProgressElem 	= getSpinnerById("timestamp-local-progress");
                
                start();
                
                FileWriter writer = null;

                try
                {
                    writer = new FileWriter(new File("/tmp/new.svg"));
                    DOMUtilities.writeDocument(document, writer);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(X3Panel.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally
                {
                    try
                    {
                        writer.close();
                    }
                    catch (IOException ex)
                    {
                        Logger.getLogger(X3Panel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                canvas.setEnableResetTransformInteractor(true);

                for (Iterator<X3PanelListener> i = listeners.iterator(); i.hasNext();)
                {
                    i.next().panelReady(X3Panel.this);
                }

                canvas.setEnableImageZoomInteractor(true);
                canvas.setEnableResetTransformInteractor(true);
                canvas.setEnablePanInteractor(false);

                Interactor panInteractor = new AbstractPanInteractor()
                {

                    @Override
                    public boolean startInteraction(InputEvent ie)
                    {
                        int mods = ie.getModifiers();
                        return ie.getID() == MouseEvent.MOUSE_PRESSED && (mods & InputEvent.BUTTON1_MASK) != 0;
                    }
                };

                Interactor resetInteractor = new AbstractResetTransformInteractor()
                {

                    @Override
                    public boolean startInteraction(InputEvent ie)
                    {
                        int mods = ie.getModifiers();
                        return ie.getID() == MouseEvent.MOUSE_PRESSED && (mods & InputEvent.BUTTON2_MASK) != 0;
                    }
                };

                Interactor zoomInteractor = new AbstractImageZoomInteractor()
                {

                    @Override
                    public boolean startInteraction(InputEvent ie)
                    {
                        int mods = ie.getModifiers();
                        return ie.getID() == MouseEvent.MOUSE_PRESSED && (mods & InputEvent.BUTTON3_MASK) != 0;
                    }
                };

                List<Interactor> interactors = canvas.getInteractors();
                interactors.add(panInteractor);
                interactors.add(resetInteractor);
                interactors.add(zoomInteractor);
            }
        });
    }
    
    public void start()
    {
    	new Thread(this,"SarongPanelUpdater").start();
    }
    
    public void stop()
    {
    	this.running=false;
    }
    
    public void startUpdate()
    {
    	alterationsInShuttle.clear();
    }
    
    public void endUpdate()
    {
    	try
		{
			alterations.put(new HashSet<Alteration>(alterationsInShuttle));
		} 
    	catch (InterruptedException e)
		{
			e.printStackTrace();
		}	
    }
    
    public void queueAlteration(Element on,String attribute, String value)
    {
    	alterationsInShuttle.add(new Alteration(on, attribute, value));
    }
    
    public String getName()
    {
        return name;
    }

    public X3Panel setURI(String newURI)
    {
        canvas.setURI(newURI);
        return this;
    }

    public X3Panel addListener(X3PanelListener listener)
    {
        listeners.add(listener);
        return this;
    }

    public X3Canvas getCanvas()
    {
        return canvas;
    }

    public Element getElementById(String id)
    {
        if (document == null || liveElements == null)
            return null;
        return liveElements.get(id);
    }
    
    public TimeSpinnerLine getSpinnerById(String id)
    {
        if (document == null || liveSpinnerElements == null)
            return null;
        return liveSpinnerElements.get(id);
    }

    public void queueUpdate(Runnable updater)
    {
        if (canvas.getUpdateManager()==null)
            return;
        canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(updater);
    }

    private void _registerLiveElements(Node node, int xmlLevel, List<String> path)
    {
        String idSlice = SVGUtils.getX3MonId(node);
        if (idSlice != null)
        {
            while(path.size()<(xmlLevel + 1))
                path.add(null);

            path.set(xmlLevel, idSlice);

            if(!node.getNodeName().equalsIgnoreCase("g"))
            {
            	if(SVGUtils.isX3MonTimeSpinner(node))
            	{
            		liveSpinnerElements.put(SVGUtils.njoin(path, xmlLevel + 1, "."),new TimeSpinnerLine((Element)node));
            		logger.finest("liveSpinner [" + node.getNodeName() + "] " + SVGUtils.njoin(path, xmlLevel + 1, ".") + " (" + SVGUtils.getAttr(node, SVG_ID) + ")");
            	}
            	else
            	{
            		liveElements.put(SVGUtils.njoin(path, xmlLevel + 1, "."), (Element) node);
            		logger.finest("liveElement [" + node.getNodeName() + "] " + SVGUtils.njoin(path, xmlLevel + 1, ".") + " (" + SVGUtils.getAttr(node, SVG_ID) + ")");
            	}
            }
        }

        NodeList kids = node.getChildNodes();
        for(int i=0;i<kids.getLength();i++)
            _registerLiveElements(kids.item(i),xmlLevel+1,path);

        while(path.size()>(xmlLevel))
            path.remove(path.size()-1);
    }

    private void registerLiveElements(Node node)
    {
        _registerLiveElements(node, 0, new ArrayList<String>());
        for (Entry<String, Element> liveElement : liveElements.entrySet())
            liveElement.getValue().setAttribute(SVG_ID, liveElement.getKey());
    }

    private void removeTemplates(Node node)
    {
        if (SVGUtils.isX3MonTemplate(node))
            node.getParentNode().removeChild(node);
        NodeList kids = node.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++)
            removeTemplates(kids.item(i));
    }

    private void materializeTemplateUsages(Node node)
    {
        if(node.getNodeName().equals(SVG_USE))
        {
            String originalId = SVGUtils.getAttrNS(node, XLINK_NS_URL, XLINK_HREF);
            if (originalId != null)
            {
                Node original = document.getElementById(originalId.substring(1));
                if (original != null && SVGUtils.isX3MonTemplate(original))
                {
                    Node materialNode = original.cloneNode(true);
                    SVGUtils.removeAttribute(materialNode, SVG_ID);
                    SVGUtils.removeAttributeNS(materialNode, X3MON_NS, X3MON_USAGE);
                    SVGUtils.cloneAttribute(node, materialNode, SVG_TRANSFORM);
                    SVGUtils.cloneAttributeNS(node, materialNode, X3MON_NS, X3MON_ID);
                    node.getParentNode().replaceChild(materialNode, node);
                }
            }
        }

        NodeList kids = node.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++)
            materializeTemplateUsages(kids.item(i));
    }

	@Override
	public void run()
	{
		Alternator alternator=null;
		List<Set<Alteration>> popped=new ArrayList<Set<Alteration>>();
		
		this.running=true;
		while(running)
		{
			long lastLocalTimestamp	=System.currentTimeMillis();
				 alternator			=new Alternator();
			
			if(!alterations.isEmpty())
			{
				logger.log(Level.FINEST,"draining " + alterations.size() + " shuttle" + (alterations.size()!=1?"s":""));
				alterations.drainTo(popped);
				for(Set<Alteration> shuttle : popped)
					alternator.addAll(shuttle);
				popped.clear();
			}
			
			timestampCalendar.setTimeInMillis(lastRemoteTimestamp);
			alternator.add(new Alteration(tsRemoteElem,null,timestampFormat.format(timestampCalendar.getTime()) + (timestampCalendar.get(Calendar.MILLISECOND)/100)));
			alternator.add(tsRemoteProgressElem.setSpinPosition(timestampCalendar.get(Calendar.MILLISECOND)/10));
	        
	        timestampCalendar.setTimeInMillis(lastLocalTimestamp);
	        alternator.add(new Alteration(tsLocalElem,null,timestampFormat.format(timestampCalendar.getTime()) + (timestampCalendar.get(Calendar.MILLISECOND)/100)));
	        alternator.add(tsLocalProgressElem.setSpinPosition(timestampCalendar.get(Calendar.MILLISECOND)/10));
	        
	        long lag=lastRemoteTimestamp-lastLocalTimestamp;
	        alternator.add(new Alteration(tsDifferenceElem,null,lagFormat.format(lag)));
	        
	        lag=(Math.abs(lag)/50);
	        if(lag>20) lag=20;		// limit lag for graphical representation to avoid eye-watering jitter
	        if(lag<10) lag=0;
	        alternator.add(new Alteration(tsRemoteElem,"transform","translate(0 " + (-lag) + ")"));
	        alternator.add(new Alteration(tsLocalElem,"transform","translate(0 " + (lag) + ")"));

			queueUpdate(alternator);
			
			try
			{
				Thread.sleep(50);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public long getLastRemoteTimestamp()
	{
		return lastRemoteTimestamp;
	}

	public void setLastRemoteTimestamp(long lastRemoteTimestamp)
	{
		this.lastRemoteTimestamp = lastRemoteTimestamp;
	}

	
}
