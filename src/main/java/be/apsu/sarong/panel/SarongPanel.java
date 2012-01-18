/*
 * SarongPanel Copyright (c) 2008,2009 Frank Marien
 * 
 *  This file is part of Sarong.

Sarong is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Sarong is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Sarong.  If not, see <http://www.gnu.org/licenses/>.

 */
package be.apsu.sarong.panel;

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

import be.apsu.sarong.dynamics.Alteration;
import be.apsu.sarong.elements.TimeSpinnerLine;
import be.apsu.sarong.svgutils.SVGUtils;

public class SarongPanel implements Runnable
{

    private static final String SVG_ID = "id";
    private static final String SVG_TRANSFORM = "transform";
    private static final String SVG_USE = "use";
    private static final String X3MON_ID = "id";
    private static final String XLINK_HREF = "href";
    private static final String X3MON_NS = "http://extremon.org/ns/extremon";
    private static final String X3MON_USAGE = "usage";
    private static final String XLINK_NS_URL = "http://www.w3.org/1999/xlink";
    
    private String 								name;
    private SarongCanvas 						canvas;
    private SVGDocument 						document;
    private Set<SarongPanelListener> 			listeners;
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
 
    
    public SarongPanel addKeyListener(KeyListener keyListener)
    {
        canvas.addKeyListener(keyListener);
        return this;
    }

    public SarongPanel reset()
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
                            System.err.println("creating description for " + id);
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

    public SarongPanel(String name)
    {
        this.name = name;
        this.canvas = new SarongCanvas();
        this.canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        this.canvas.setAnimationLimitingNone();
        this.canvas.setBackground(Color.black);
        this.listeners = new HashSet<SarongPanelListener>();
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
                    Logger.getLogger(SarongPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally
                {
                    try
                    {
                        writer.close();
                    }
                    catch (IOException ex)
                    {
                        Logger.getLogger(SarongPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                canvas.setEnableResetTransformInteractor(true);

                for (Iterator<SarongPanelListener> i = listeners.iterator(); i.hasNext();)
                {
                    i.next().panelReady(SarongPanel.this);
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

    public SarongPanel setURI(String newURI)
    {
        canvas.setURI(newURI);
        return this;
    }

    public SarongPanel addListener(SarongPanelListener listener)
    {
        listeners.add(listener);
        return this;
    }

    public SarongCanvas getCanvas()
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
        if (canvas.getUpdateManager() == null)
        {
            return;
        }

        canvas.getUpdateManager().
                getUpdateRunnableQueue().
                invokeLater(updater);
    }

    public int suspendRedraw()
    {
    	return document.getRootElement().suspendRedraw(2000);
    }
    
    public void unsuspendRedraw()
    {
    	document.getRootElement().unsuspendRedrawAll();
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
            		System.out.println("liveSpinner [" + node.getNodeName() + "] " + SVGUtils.njoin(path, xmlLevel + 1, ".") + " (" + SVGUtils.getAttr(node, SVG_ID) + ")");
            	}
            	else
            	{
            		liveElements.put(SVGUtils.njoin(path, xmlLevel + 1, "."), (Element) node);
            		System.out.println("liveElement [" + node.getNodeName() + "] " + SVGUtils.njoin(path, xmlLevel + 1, ".") + " (" + SVGUtils.getAttr(node, SVG_ID) + ")");
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
		
		
		this.running=true;
		while(running)
		{
			long lastLocalTimestamp=System.currentTimeMillis();
			
			List<Set<Alteration>> popped=new ArrayList<Set<Alteration>>();
			final Set<Alteration> consolidated=new HashSet<Alteration>();
			
			if(!alterations.isEmpty())
			{
				//System.out.println("draining " + alterations.size() + " shuttle" + (alterations.size()!=1?"s":""));
				alterations.drainTo(popped);
				for(Set<Alteration> shuttle : popped)
					consolidated.addAll(shuttle);
				popped.clear();
			}
			
			timestampCalendar.setTimeInMillis(lastRemoteTimestamp);
	        consolidated.add(new Alteration(tsRemoteElem,null,timestampFormat.format(timestampCalendar.getTime()) + (timestampCalendar.get(Calendar.MILLISECOND)/100)));
	        consolidated.add(tsRemoteProgressElem.setSpinPosition(timestampCalendar.get(Calendar.MILLISECOND)/10));
	        
	        timestampCalendar.setTimeInMillis(lastLocalTimestamp);
	        consolidated.add(new Alteration(tsLocalElem,null,timestampFormat.format(timestampCalendar.getTime()) + (timestampCalendar.get(Calendar.MILLISECOND)/100)));
	        consolidated.add(tsLocalProgressElem.setSpinPosition(timestampCalendar.get(Calendar.MILLISECOND)/10));
	        
	        long lag=lastRemoteTimestamp-lastLocalTimestamp;
	        consolidated.add(new Alteration(tsDifferenceElem,null,lagFormat.format(lag)));
	        
	        lag=(Math.abs(lag)/50);
	        if(lag>20) lag=20;		// limit lag for graphical representation
	        consolidated.add(new Alteration(tsRemoteElem,"transform","translate(0 " + (-lag) + ")"));
	        consolidated.add(new Alteration(tsLocalElem,"transform","translate(0 " + (lag) + ")"));
				
			/*System.out.println("\n\nSHUTTLE");
			for(Alteration alteration : consolidated)
				System.out.println("Alteration On [" + alteration + "]");
			System.out.println("/SHUTTLE\n\n"); */

			queueUpdate(new Runnable()
			{
				@Override
				public void run()
				{
					for(Alteration alteration : consolidated)
						alteration.alter();
				}
			});
			
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
