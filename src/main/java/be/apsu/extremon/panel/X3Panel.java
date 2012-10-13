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
import org.w3c.dom.svg.SVGDocument;

import be.apsu.extremon.client.X3Client;
import be.apsu.extremon.client.X3ClientListener;
import be.apsu.extremon.client.X3Measure;
import be.apsu.extremon.dynamics.AbstractAction;
import be.apsu.extremon.dynamics.Alteration;
import be.apsu.extremon.dynamics.Alternator;
import be.apsu.extremon.dynamics.CountdownSetAction;
import be.apsu.extremon.dynamics.NumericSetAction;
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
	private Set<X3PanelListener>			listeners;
	private Map<String,Element>				liveElements;
	private BlockingQueue<Set<Alteration>>	alterations;
	private Set<Alteration>					alterationsInShuttle;
	private boolean							running;
	private Map<String,String>				defines;
	private Pattern							mapConfigPattern;
	private Map<String,Set<AbstractAction>>	actions;
	private String							subscription;

	public X3Panel(String name)
	{
		LOGGER.setLevel(Level.ALL);
		this.name=name;
		this.canvas=new X3Canvas(new X3UserAgent(),true,true);
		this.canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		this.canvas.setAnimationLimitingNone();
		this.canvas.setBackground(Color.black);
		this.listeners=new HashSet<X3PanelListener>();
		this.liveElements=new HashMap<String,Element>();
		this.alterations=new ArrayBlockingQueue<Set<Alteration>>(EXPECTED_ALTERATIONS);
		this.alterationsInShuttle=new HashSet<Alteration>(EXPECTED_ALTERATIONS_IN_SHUTTLE);
		this.defines=new HashMap<String,String>();
		this.mapConfigPattern=Pattern.compile("([a-z0-9._-]*):([a-z]+)\\((.*)\\)");
		this.actions=new HashMap<String,Set<AbstractAction>>();

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

				inhaleDefines(X3Panel.this.document);
				materializeTemplateUsages(X3Panel.this.document);
				removeTemplates(X3Panel.this.document);
				registerMappings(X3Panel.this.document);
				assembleSubscription();

				start();

				X3Panel.this.canvas.setEnableResetTransformInteractor(true);

				for(final X3PanelListener listener:X3Panel.this.listeners)
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

	public final X3Panel addKeyListener(KeyListener keyListener)
	{
		this.canvas.addKeyListener(keyListener);
		return this;
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

	public final X3Panel addListener(X3PanelListener listener)
	{
		this.listeners.add(listener);
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
			System.err.println(node.getTextContent());
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
							addAction(label,new NumericSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
						}
						else if(arguments.length==NSET_ARGUMENTS_NAME_FORMAT_MULTIPLIER)
						{
							final double scale=Double.parseDouble(arguments[2]);
							addAction(label,new NumericSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format,scale));
						}
					}
					else if(actionStr.equals("tset")&&arguments.length==2)
					{
						final String attributeName=arguments[0];
						final String format=arguments[1];
						LOGGER.fine("on "+label+" do TextSetAction "+(attributeName.isEmpty()?"cdata":attributeName)+" to "+format);
						addAction(label,new TextSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
					}
					else if(actionStr.equals("tsset")&&arguments.length==2)
					{
						final String attributeName=arguments[0];
						final String format=arguments[1];
						addAction(label,new TimestampSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
					}
					else if(actionStr.equals("cdset")&&arguments.length==2)
					{
						final String attributeName=arguments[0];
						final String format=arguments[1];
						addAction(label,new CountdownSetAction(this,(Element)node,attributeName.isEmpty()?null:attributeName,format));
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

	private void addAction(String label,AbstractAction action)
	{
		Set<AbstractAction> actionsForThisLabel=this.actions.get(label);
		if(actionsForThisLabel==null)
		{
			actionsForThisLabel=new HashSet<AbstractAction>();
			this.actions.put(label,actionsForThisLabel);
		}

		actionsForThisLabel.add(action);
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

	private void assembleSubscription()
	{
		int shortestPath=Integer.MAX_VALUE;
		final List<Set<String>> groups=new ArrayList<Set<String>>();

		System.err.println(this.actions.keySet().size()+" labels");

		for(String label:this.actions.keySet())
		{
			if(label.startsWith("local."))
				continue;
			
			final String[] labelParts=label.split("\\.");
			for(int i=0;i<labelParts.length;i+=1)
			{
				if(i>groups.size()-1)
					groups.add(new HashSet<String>());
				groups.get(i).add(labelParts[i]);
			}

			if(labelParts.length<shortestPath)
				shortestPath=labelParts.length;
		}

		final List<String> regexParts=new ArrayList<String>();

		int position=0;
		for(Set<String> groupItems:groups)
		{
			final StringBuilder builder=new StringBuilder();

			if(position<shortestPath)
			{
				builder.append('/');
				if(groupItems.size()>1)
					builder.append('(');
				builder.append(SVGUtils.join(groupItems,"|"));
				if(groupItems.size()>1)
					builder.append(')');
			}
			else
			{
				builder.append("(/");
				if(groupItems.size()>1)
					builder.append('(');
				builder.append(SVGUtils.join(groupItems,"|"));
				if(groupItems.size()>1)
					builder.append(')');
				builder.append("|$)");
			}
			regexParts.add(builder.toString());
			position+=1;
		}

		this.subscription=SVGUtils.join(regexParts,"");
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

		this.startUpdate();

		try
		{
			for(X3Measure measure:measures)
			{
				final Set<AbstractAction> actionsRequired=this.actions.get(measure.getLabel());
				if(actionsRequired!=null)
				{
					for(AbstractAction actionRequired:actionsRequired)
					{
						actionRequired.perform(measure.getValue());
					}
				}
			}
		}
		finally
		{
			this.endUpdate();
		}
	}
}
