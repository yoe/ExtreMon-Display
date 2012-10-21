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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

import be.apsu.extremon.client.X3Client;
import be.apsu.extremon.client.X3ClientListener;
import be.apsu.extremon.client.X3Measure;
import be.apsu.extremon.dynamics.AbstractAction;
import be.apsu.extremon.dynamics.Alteration;
import be.apsu.extremon.dynamics.Alternator;
import be.apsu.extremon.dynamics.CountdownSetAction;
import be.apsu.extremon.dynamics.NumericSetAction;
import be.apsu.extremon.dynamics.Respondable;
import be.apsu.extremon.dynamics.Subscription;
import be.apsu.extremon.dynamics.TextSetAction;
import be.apsu.extremon.dynamics.TimestampSetAction;
import be.apsu.extremon.svgutils.SVGUtils;

public class X3Panel implements Runnable,X3ClientListener
{
	private static final double				MILLISECONDS							=1000.0;
	private static final int				ALTERNATOR_DELAY						=50;
	private static final int				NSET_ARGUMENTS_NAME_FORMAT_MULTIPLIER	=3;
	private static final int				NSET_ARGUMENTS_NAME_FORMAT				=2;
	private static final int				ARGUMENTS_GROUP							=3;
	private static final int				ACTION_GROUP							=2;
	private static final int				LABEL_GROUP								=1;
	private static final int				EXPECTED_ALTERATIONS_IN_SHUTTLE			=32;
	private static final int				EXPECTED_ALTERATIONS					=10;

	private static final Logger				LOGGER									=Logger.getLogger(X3Panel.class.getName());

	private static final String				SVG_ID									="id";
	private static final String				SVG_TRANSFORM							="transform";
	private static final String				X3MON_ID								="id";
	private static final String				X3MON_MAP								="map";
	private static final String				X3MON_DEFINE							="define";
	private static final String				XLINK_HREF								="href";
	private static final String				X3MON_NS								="http://extremon.org/ns/extremon";
	private static final String				X3MON_USAGE								="usage";
	private static final String				XLINK_NS_URL							="http://www.w3.org/1999/xlink";

	private String							name;
	private X3Canvas						canvas;
	private SVGDocument						document;
	private Set<X3PanelListener>			panelListeners;
	private Set<ResponderListener>			responderListeners;
	private Map<String,Element>				liveElements;
	private BlockingQueue<Set<Alteration>>	alterations;
	private Set<Alteration>					alterationsInShuttle;
	private boolean							running;
	private Map<String,String>				defines;
	private Pattern							mapConfigPattern;
	private Pattern							responderPattern;
	private Map<String,Set<AbstractAction>>	actions;
	private Map<Element,Respondable>		respondablesByElement;
	private Map<String,Respondable>			respondablesByLabel;
	private String							subscription;
	private long							lastHeartBeat;

	public X3Panel(String name)
	{
		LOGGER.setLevel(Level.ALL);
		this.name=name;
		this.canvas=new X3Canvas(new X3UserAgent(),true,true);
		this.canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		this.canvas.setAnimationLimitingNone();
		this.canvas.setBackground(Color.black);
		this.panelListeners=new HashSet<X3PanelListener>();
		this.responderListeners=new HashSet<ResponderListener>();
		this.liveElements=new HashMap<String,Element>();
		this.alterations=new ArrayBlockingQueue<Set<Alteration>>(EXPECTED_ALTERATIONS);
		this.alterationsInShuttle=new HashSet<Alteration>(EXPECTED_ALTERATIONS_IN_SHUTTLE);
		this.defines=new HashMap<String,String>();
		this.mapConfigPattern=Pattern.compile("([a-z0-9._-]*):([a-z]+)\\((.*)\\)");
		this.responderPattern=Pattern.compile("^(.*)\\.(responding|responder\\.name|responder\\.comment)$");
		this.actions=new HashMap<String,Set<AbstractAction>>();
		this.respondablesByElement=new HashMap<Element,Respondable>();
		this.respondablesByLabel=new HashMap<String,Respondable>();
		this.lastHeartBeat=0L;

		this.canvas.addSVGLoadEventDispatcherListener(new SVGLoadEventDispatcherAdapter()
		{
			@Override
			public void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e)
			{
				super.svgLoadEventDispatchCompleted(e);
				X3Panel.this.document=X3Panel.this.canvas.getSVGDocument();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e)
			{
				super.svgLoadEventDispatchCompleted(e);
				
				EventTarget t = (EventTarget)X3Panel.this.document;
				t.addEventListener("click",new EventListener()
				{
					@Override
					public void handleEvent(Event evt)
					{
						Respondable respondable=X3Panel.this.respondablesByElement.get((Element)evt.getTarget());
						if(respondable!=null)
						{
							if(!respondable.isResponding())
							{	
								for(ResponderListener listener:X3Panel.this.responderListeners)
								{
									try
									{
										listener.responding(respondable.getLabel(),true);
									}
									catch(Exception anyEx)
									{
										LOGGER.log(Level.WARNING,"Exception in ResponderListener",anyEx);
									}
								}
							}
							else
							{
								for(ResponderListener listener:X3Panel.this.responderListeners)
								{
									try
									{
										listener.responding(respondable.getLabel(),false);
									}
									catch(Exception anyEx)
									{
										LOGGER.log(Level.WARNING,"Exception in ResponderListener",anyEx);
									}
								}
							}
						}
					}
				},true);
				
//				t.addEventListener("keydown",new EventListener()
//				{
//					@Override
//					public void handleEvent(Event evt)
//					{
//						Element respondable=(Element)evt.getTarget();
//						String respondableLabel=X3Panel.this.respondables.get(respondable);
//						if(respondableLabel!=null)
//						{
//							String comment=X3Panel.this.responding.get(respondableLabel);
//							if(comment!=null)
//							{
//								DOMKeyEvent keyEvent=(DOMKeyEvent)evt;
//								comment=comment+=keyEvent.getCharCode();	
//								for(ResponderListener listener:X3Panel.this.responderListeners)
//								{
//									try
//									{
//										listener.responderComment(respondableLabel,comment);
//									}
//									catch(Exception anyEx)
//									{
//										LOGGER.log(Level.WARNING,"Exception in ResponderListener",anyEx);
//									}
//								}
//								X3Panel.this.responding.put(respondableLabel,comment);
//							}
//						}
//					}
//				},true);

				inhaleDefines(X3Panel.this.document);
				materializeTemplateUsages(X3Panel.this.document);
				removeTemplates(X3Panel.this.document);
				registerMappings(X3Panel.this.document);
				assembleSubscription();

				start();

				X3Panel.this.canvas.setEnableResetTransformInteractor(true);

				for(final X3PanelListener listener:X3Panel.this.panelListeners)
				{
					listener.panelReady(X3Panel.this);
				}

				X3Panel.this.canvas.setEnableImageZoomInteractor(true);
				X3Panel.this.canvas.setEnableResetTransformInteractor(true);
				X3Panel.this.canvas.setEnablePanInteractor(false);

				final Interactor panInteractor=new AbstractPanInteractor()
				{
					@Override
					public boolean startInteraction(InputEvent ie)
					{
						final int mods=ie.getModifiers();
						return ie.getID()==MouseEvent.MOUSE_PRESSED&&(mods&InputEvent.BUTTON1_MASK)!=0;
					}
				};

				final Interactor resetInteractor=new AbstractResetTransformInteractor()
				{

					@Override
					public boolean startInteraction(InputEvent ie)
					{
						final int mods=ie.getModifiers();
						return ie.getID()==MouseEvent.MOUSE_PRESSED&&(mods&InputEvent.BUTTON2_MASK)!=0;
					}
				};

				final Interactor zoomInteractor=new AbstractImageZoomInteractor()
				{

					@Override
					public boolean startInteraction(InputEvent ie)
					{
						final int mods=ie.getModifiers();
						return ie.getID()==MouseEvent.MOUSE_PRESSED&&(mods&InputEvent.BUTTON3_MASK)!=0;
					}	
				};

				final List<Interactor> interactors=X3Panel.this.canvas.getInteractors();
				interactors.add(panInteractor);
				interactors.add(resetInteractor);
				interactors.add(zoomInteractor);
			}

		});
	}

	protected void assembleSubscription()
	{
		Subscription 		subscription=new Subscription();
							subscription.registerMeta(".state","responding");
							subscription.registerMeta(".state","responder.name");
							subscription.registerMeta(".state","responder.comment");
							subscription.addLabels(this.actions.keySet());
		this.subscription=	subscription.getSubscription();
		
		
	}

	public final X3Panel reset()
	{
		this.canvas.resetRenderingTransform();
		return this;
	}

	final Element createDescription(String text)
	{
		final Element description=this.document.createElementNS("http://www.w3.org/2000/svg","desc");
		final Text descriptionText=this.document.createTextNode(text);
		description.appendChild(descriptionText);
		return description;
	}
	
	final Element createResponderNameLabel(Element target, String name)
	{
		final double x=Double.parseDouble(target.getAttribute("x"));
		final double y=10.0+(Double.parseDouble(target.getAttribute("y"))+(Double.parseDouble(target.getAttribute("height"))));
		final Element nameLabel=this.document.createElementNS("http://www.w3.org/2000/svg","text");
		String style=this.defines.get("respondernamehidden");
		if(style!=null)
			nameLabel.setAttribute("style",style);
		nameLabel.setAttribute("x",String.valueOf(x));
		nameLabel.setAttribute("y",String.valueOf(y));
		final Text nameText=this.document.createTextNode(name);
		nameLabel.appendChild(nameText);
		return nameLabel;
	}
	
	public final String trigramFromName(final String _name)
	{
		String[] nameParts=_name.toUpperCase().split(" ");
		StringBuilder builder=new StringBuilder();
		
		switch(nameParts.length)
		{
			case 1: 
				builder.append(nameParts[0].substring(0,2));
			break;
			
			case 2:
				builder.append(nameParts[0].substring(0,1));
				builder.append(nameParts[1].substring(0,2));
			break;
			
			default:
				for(int i=0;i<nameParts.length;i++)
				{
					builder.append(nameParts[i].substring(0,1));
				}
			break;
		}
			
		return builder.toString();
	}

	public final void start()
	{
		new Thread(this,"SarongPanelUpdater").start();
	}

	public final void stop()
	{
		this.running=false;
	}

	public final void startUpdate()
	{
		this.alterationsInShuttle.clear();
	}

	public final void endUpdate()
	{
		try
		{
			this.alterations.put(new HashSet<Alteration>(this.alterationsInShuttle));
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public final void queueAlteration(final Element on,final String attribute,final String value)
	{
		this.alterationsInShuttle.add(new Alteration(on,attribute,value));
	}

	public final String getName()
	{
		return this.name;
	}

	public final X3Panel setURI(String newURI)
	{
		this.canvas.setURI(newURI);
		return this;
	}

	public final X3Panel addPanelListener(X3PanelListener listener)
	{
		this.panelListeners.add(listener);
		return this;
	}
	
	public final X3Panel addResponderListener(ResponderListener listener)
	{
		this.responderListeners.add(listener);
		return this;
	}

	public final X3Canvas getCanvas()
	{
		return this.canvas;
	}

	public final void queueUpdate(Runnable updater)
	{
		if(this.canvas.getUpdateManager()==null)
			return;
		this.canvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(updater);
	}

	private void registerMappingsRecursor(Node node,int xmlLevel,List<String> path)
	{
		final String idSlice=SVGUtils.getX3MonId(node);
		if(idSlice!=null)
		{
			while(path.size()<(xmlLevel+1))
				path.add(null);
			path.set(xmlLevel,idSlice);
		}

		final String mappingsStr=SVGUtils.getX3MonMap(node);
		if(mappingsStr!=null)
		{
			final String[] mappings=mappingsStr.split(";");
			for(String mappingStr:mappings)
			{
				final Matcher matcher=this.mapConfigPattern.matcher(mappingStr);
				if(matcher.matches())
				{
					String label=matcher.group(LABEL_GROUP);
					final String actionStr=matcher.group(ACTION_GROUP);
					final String[] arguments=matcher.group(ARGUMENTS_GROUP).split(":");

					if(label.isEmpty())
						label=SVGUtils.njoin(path,xmlLevel+1,".");
					else if(label.startsWith("."))
						label=SVGUtils.njoin(path,xmlLevel+1,".")+label;

					if(actionStr.equals("nset")&&(arguments.length>=2))
					{
						final String attributeName=arguments[0];
						final String format=arguments[1];

						if(arguments.length==NSET_ARGUMENTS_NAME_FORMAT)
						{
							addAction(label,(Element)node,new NumericSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
						}
						else if(arguments.length==NSET_ARGUMENTS_NAME_FORMAT_MULTIPLIER)
						{
							final double scale=Double.parseDouble(arguments[2]);
							addAction(label,(Element)node,new NumericSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format,scale));
						}
					}
					else if(actionStr.equals("tset")&&arguments.length==2)
					{
						final String attributeName=arguments[0];
						final String format=arguments[1];
						LOGGER.fine("on "+label+" do TextSetAction "+(attributeName.isEmpty()?"cdata":attributeName)+" to "+format);
						addAction(label,(Element)node,new TextSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
					}
					else if(actionStr.equals("tsset")&&arguments.length==2)
					{
						final String attributeName=arguments[0];
						final String format=arguments[1];
						addAction(label,(Element)node,new TimestampSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
					}
					else if(actionStr.equals("cdset")&&arguments.length==2)
					{
						final String attributeName=arguments[0];
						final String format=arguments[1];
						addAction(label,(Element)node,new CountdownSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
					}
				}
				else
				{

					LOGGER.info("Error interpreting x3mon:map ["+mappingStr+"] Syntax Error. (id="+SVGUtils.getAttr(node,"id"));
				}
			}
		}

		final NodeList kids=node.getChildNodes();
		for(int i=0;i<kids.getLength();i+=1)
			this.registerMappingsRecursor(kids.item(i),xmlLevel+1,path);

		while(path.size()>xmlLevel)
			path.remove(path.size()-1);
	}

	private void addAction(String label,Element element, AbstractAction action)
	{
		Set<AbstractAction> actionsForThisLabel=this.actions.get(label);
		if(actionsForThisLabel==null)
		{
			actionsForThisLabel=new HashSet<AbstractAction>();
			System.err.println("Adding Actions for [" + label + "]");
			this.actions.put(label,actionsForThisLabel);
		}
			
		actionsForThisLabel.add(action);
		
		if(label.endsWith(".state"))
		{
			LOGGER.finest("Adding Respondable [" + label + "]");
			System.err.println("Adding Respondable [" + label + "]");
			addRespondable(element,label);
		}
	}
	
	private void addRespondable(Element element,String label)
	{
		if(element.getNodeName().equalsIgnoreCase("rect"))
		{
			Respondable respondable=this.respondablesByLabel.get(label);
			if(respondable==null)
			{
				respondable=new Respondable(element,label);
				Element responderNameLabel=createResponderNameLabel(element,"***");
				respondable.setResponderNameElement(responderNameLabel);
				element.getParentNode().appendChild(responderNameLabel);
				
				this.respondablesByLabel.put(label,respondable);
			}
			
			this.respondablesByElement.put(element,respondable);
		}
	}

	private void registerMappings(Node node)
	{
		this.registerMappingsRecursor(node,0,new ArrayList<String>());
		for(Entry<String,Element> liveElement:this.liveElements.entrySet())
			liveElement.getValue().setAttribute(SVG_ID,liveElement.getKey());
	}

	private void removeTemplates(Node node)
	{
		if(SVGUtils.isX3MonTemplate(node))
			node.getParentNode().removeChild(node);
		final NodeList kids=node.getChildNodes();
		for(int i=0;i<kids.getLength();i+=1)
			this.removeTemplates(kids.item(i));
	}

	private void materializeTemplateUsages(Node node)
	{
		if(node.getNodeName().equalsIgnoreCase("svg:use")||node.getNodeName().equalsIgnoreCase("use"))
		{
			final String originalId=SVGUtils.getAttrNS(node,XLINK_NS_URL,XLINK_HREF);
			if(originalId!=null)
			{
				final Node original=this.document.getElementById(originalId.substring(1));
				if(original!=null&&SVGUtils.isX3MonTemplate(original))
				{
					final Node materialNode=original.cloneNode(true);
					SVGUtils.removeAttribute(materialNode,SVG_ID);
					SVGUtils.removeAttributeNS(materialNode,X3MON_NS,X3MON_USAGE);
					SVGUtils.cloneAttribute(node,materialNode,SVG_TRANSFORM);
					SVGUtils.cloneAttributeNS(node,materialNode,X3MON_NS,X3MON_ID);
					SVGUtils.cloneAttributeNS(node,materialNode,X3MON_NS,X3MON_MAP);
					node.getParentNode().replaceChild(materialNode,node);
				}
			}
		}

		final NodeList kids=node.getChildNodes();
		for(int i=0;i<kids.getLength();i+=1)
			this.materializeTemplateUsages(kids.item(i));
	}

	private void inhaleDefines(Node node)
	{
		final String defineStr=SVGUtils.getX3MonAttr(node,X3MON_DEFINE);
		if(defineStr!=null)
		{
			final String[] defineParts=defineStr.split(":");
			if(defineParts.length==1)
			{
				final String defineLabel=defineParts[0];
				final String defineValue=node.getTextContent();
				LOGGER.fine("define "+defineLabel+" as "+defineValue);
				this.defines.put(defineLabel,defineValue);
			}
			else if(defineParts.length==2)
			{
				final String defineLabel=defineParts[0];
				final String defineAttrName=defineParts[1];
				final String defineValue=SVGUtils.getAttr(node,defineAttrName);
				LOGGER.fine("define "+defineLabel+" as "+defineValue);
				this.defines.put(defineLabel,defineValue);
			}
		}

		final NodeList kids=node.getChildNodes();
		for(int i=0;i<kids.getLength();i+=1)
			this.inhaleDefines(kids.item(i));
	}

	

	@Override
	public final void run()
	{
		Alternator alternator=null;
		final List<Set<Alteration>> popped=new ArrayList<Set<Alteration>>();

		this.running=true;
		while(this.running)
		{
			alternator=new Alternator();

			if(!this.alterations.isEmpty())
			{
				LOGGER.log(Level.FINEST,"draining "+this.alterations.size()+" shuttle"+(this.alterations.size()!=1?"s":""));
				this.alterations.drainTo(popped);
				for(Set<Alteration> shuttle:popped)
					alternator.addAll(shuttle);
				popped.clear();
			}

			this.queueUpdate(alternator);

			try
			{
				Thread.sleep(ALTERNATOR_DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public final String getDefinedValue(String key)
	{
		return this.defines.get(key);
	}

	public final String getSubscription()
	{
		return this.subscription;
	}

	@Override
	public final void clientData(X3Client client,Set<X3Measure> measures)
	{
		final double localTime=((double)System.currentTimeMillis())/MILLISECONDS;
		measures.add(new X3Measure("local.timestamp",String.valueOf(localTime)));
		
		if(System.currentTimeMillis()>(lastHeartBeat+1000))
		{
			//System.err.println("heartbeat");
			for(ResponderListener listener:X3Panel.this.responderListeners)
			{
				try
				{
					listener.heartBeat(0.0);
				}
				catch(Exception anyEx)
				{
					LOGGER.log(Level.WARNING,"Exception in ResponderListener",anyEx);
				}
			}
			lastHeartBeat=System.currentTimeMillis();
		}

		this.startUpdate();

		try
		{
			for(final X3Measure measure:measures)
			{
				final Matcher matcher=this.responderPattern.matcher(measure.getLabel());
				if(matcher.matches())
					clientRespondingMeasure(matcher.group(1),matcher.group(2),measure.getValue());
				else
					clientMeasure(measure.getLabel(),measure.getValue());
			}
		}
		finally
		{
			this.endUpdate();
		}
	}
	
	private final void clientRespondingMeasure(final String label, final String type, final String value)
	{
		System.err.println("RespondingMeasure");
		Respondable respondable=this.respondablesByLabel.get(label);
		if(respondable!=null)
		{
			if(type.equals("responding"))
			{
				boolean responding=value.equals("1");
				
				if(responding!=respondable.isResponding())
				{
					respondable.setResponding(responding);
					
					String visibleResponderNameStyle=this.getDefinedValue("respondernamevisible");
					String hiddenResponderNameStyle=this.getDefinedValue("respondernamehidden");
					if(visibleResponderNameStyle!=null && hiddenResponderNameStyle!=null)
					{
						TextSetAction tsa=new TextSetAction(this,respondable.getResponderNameElement(),"style","#");
						tsa.perform(responding?visibleResponderNameStyle:hiddenResponderNameStyle);
					}	
				}
			}
			else if(type.equals("responder.name"))
			{
				respondable.setResponderName(value);
				TextSetAction tsa=new TextSetAction(this,respondable.getResponderNameElement(),null,"#");
				tsa.perform(trigramFromName(value));

			}
			else if(type.equals("responder.comment"))
			{
				respondable.setComment(value);
			}
			
			performActions(label,String.valueOf(respondable.getDisplayState()));
		}
	}
	
	private final void clientMeasure(final String label, final String _value)
	{	
		String value=_value;
		if(label.endsWith(".state"))
		{
			Respondable respondable=this.respondablesByLabel.get(label);
			if(respondable!=null)
			{
				respondable.setState(Integer.parseInt(value));
				performActions(label,String.valueOf(respondable.getDisplayState()));
			}
		}
		else
		{
			performActions(label,value);
		}
	}

	private void performActions(final String label,String value)
	{
		final Set<AbstractAction> actionsRequired=this.actions.get(label);
		if(actionsRequired!=null)
		{
			for(AbstractAction actionRequired:actionsRequired)
			{
				actionRequired.perform(value);
			}
		}
	}
}
