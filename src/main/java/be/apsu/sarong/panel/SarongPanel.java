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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

public class SarongPanel
{

    private static final long serialVersionUID = -6964246820463870400L;
    private String name;
    private SarongCanvas canvas;
    private SVGDocument document;
    private Set<SarongPanelListener> listeners;
    private Map<String, Node> templates;

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

    private void generateID(Node node, String prefix, String id)
    {
        NamedNodeMap nodeAttr = node.getAttributes();
        Node nodeClass = nodeAttr.getNamedItem("class");
        if(nodeClass != null)
        {
            String nodeClassStr=nodeClass.getNodeValue();
            if(nodeClassStr.startsWith(prefix))
            {
                Node nodeID = nodeAttr.getNamedItem("id");
                String suffix = nodeClassStr.substring(prefix.length());
                System.out.println(node.getNodeName() + " class " + nodeClassStr + " TO id " + id + suffix);
                nodeID.setNodeValue(id + suffix);
                nodeAttr.removeNamedItem("class");    
            }
            else if(nodeClassStr.equalsIgnoreCase("be.apsu.sarong.template"))
            {
                Node nodeID = nodeAttr.getNamedItem("id");
                nodeID.setNodeValue(id);
                nodeAttr.removeNamedItem("class");  
            }
        }
    }

    private void generateIDs(Node node, String prefix, String id)
    {
        NodeList children = node.getChildNodes();
        generateID(node, prefix, id);
        for (int i = 0; i < children.getLength(); i++)
        {
            Node aNode = children.item(i);
            if (aNode.hasChildNodes())
            {
                generateIDs(aNode, prefix, id);
            }
            else
            {
                generateID(node, prefix, id);
            }
        }
    }
    
    void replaceTemplates()
    {
        queueUpdate(new Runnable()
        {
            @Override
            public void run()
            {
                NodeList groups = document.getElementsByTagName("g");
                for (int i = 0; i < groups.getLength(); i++)
                {
                    Node group = groups.item(i);
                    NamedNodeMap groupAttr = group.getAttributes();
                    Node groupClass = groupAttr.getNamedItem("class");
                    if (groupClass != null && groupClass.getNodeValue().equalsIgnoreCase("be.apsu.sarong.template"))
                    {
                        templates.put(groupAttr.getNamedItem("id").getNodeValue().toLowerCase(), group);
                    }
                }

                HashMap<Node, Node> replacements = new HashMap<Node, Node>();

                NodeList clones = document.getElementsByTagNameNS("http://www.w3.org/2000/svg", "use");
                for (int i = 0; i < clones.getLength(); i++)
                {
                    Node clone = clones.item(i);
                    NamedNodeMap cloneAttr = clone.getAttributes();
                    Node cloneRefNode = cloneAttr.getNamedItemNS("http://www.w3.org/1999/xlink", "href");
                    if (cloneRefNode != null)
                    {
                        String cloneRef = cloneRefNode.getNodeValue().toLowerCase();
                        if (cloneRef.startsWith("#"))
                        {
                            Node templateNode = templates.get(cloneRef.substring(1));
                            Node duplicate = templateNode.cloneNode(true);
                            
                            Node cloneTransform = cloneAttr.getNamedItem("transform").cloneNode(true);
                            NamedNodeMap duplicateAttr = duplicate.getAttributes();
                            duplicateAttr.setNamedItem(cloneTransform);

                            generateIDs(duplicate, "be.apsu.sarong.template." + cloneRef.substring(1), cloneAttr.getNamedItem("id").getNodeValue().toLowerCase());
                            
                            replacements.put(clone, duplicate);
                        }
                    }
                }


                for (Map.Entry<Node, Node> replacement : replacements.entrySet())
                {
                    Node victim = replacement.getKey();
                    Node killer = replacement.getValue();
                    Node parent = victim.getParentNode();
                    parent.replaceChild(killer, victim);       
                }
                
                FileWriter writer=null;
                
                try
                {
                    writer=new FileWriter(new File("/tmp/result.svg"));
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
        this.templates = new HashMap<String, Node>();

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
                
                //replaceTemplates();

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
        if (document == null)
        {
            return null;
        }
        return document.getElementById(id);
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
}
