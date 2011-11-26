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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;

public class SarongPanel
{
    private static final String SVG_ID = "id";
    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private static final String SVG_TRANSFORM = "transform";
    private static final String SVG_USE = "use";
    private static final String X3MON_ID = "id";
    private static final String XLINK_HREF      = "href";
    private static final String X3MON_TEMPLATE  = "template";
    private static final String X3MON_NS    = "http://extremon.org/ns/extremon";
    private static final String X3MON_USAGE     = "usage";
    private static final String XLINK_NS_URL    = "http://www.w3.org/1999/xlink";
    
    private String                      name;
    private SarongCanvas                canvas;
    private SVGDocument                 document;
    private Set<SarongPanelListener>    listeners;
    private HashMap<String,Element>     liveElements;

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

//    private void generateIDs(Element element, String prefix)
//    {
//        NodeList descendants = element.getElementsByTagName("*");
//        for (int i = 0; i < descendants.getLength(); i++)
//        {
//            if (descendants.item(i).getNodeType() == Node.ELEMENT_NODE) {
//                // should be true for all descendants
//                Element descendant = (Element)descendants.item(i);
//                descendant.setAttribute("id", prefix + "." + descendant.getAttribute("id"));
//            }
//        }
//        element.setAttribute("id", prefix);
//    }
//    void replaceTemplate(Element clone, Element original)
//    {
//        Element duplicate = (Element) original.cloneNode(true);
//        NamedNodeMap cloneAttr = clone.getAttributes();
//        if (cloneAttr.getNamedItem("transform") != null) {
//            Node duplicateTransformAttr = cloneAttr.getNamedItem("transform").cloneNode(true);
//            NamedNodeMap duplicateAttr = duplicate.getAttributes();
//            duplicateAttr.setNamedItem(duplicateTransformAttr);
//        }
//        generateIDs(duplicate, cloneAttr.getNamedItem("id").getNodeValue().toLowerCase());
//        Node parent = clone.getParentNode();
//        parent.replaceChild(duplicate, clone);
//    }
//    
//    boolean replaceTemplates(Element element)
//    {
//        return replaceTemplates(element.getElementsByTagNameNS("http://www.w3.org/2000/svg", "use"));
//    }
//    
//    boolean replaceTemplates(org.w3c.dom.Document document)
//    {
//        return replaceTemplates(document.getElementsByTagNameNS("http://www.w3.org/2000/svg", "use"));
//    }
//    
//    boolean replaceTemplates(NodeList clones)
//    {
//        boolean templateReplaced = false;
//        for (int i = clones.getLength() - 1; i >= 0; i--) {
//            Element clone = (Element)clones.item(i);
//            if (clone != null) {
//                NamedNodeMap cloneAttr = clone.getAttributes();
//                Node cloneRefNode = cloneAttr.getNamedItemNS("http://www.w3.org/1999/xlink", "href");
//                if (cloneRefNode != null) {
//                    String cloneRef = cloneRefNode.getNodeValue().toLowerCase();
//                    if (cloneRef.startsWith("#")) {
//                        Element templateElement = document.getElementById(cloneRef.substring(1));
//                        if (templateElement != null && hasClass(templateElement, "be.apsu.sarong.original")) {
//                            replaceTemplate(clone,templateElement);
//                            templateReplaced = true;
//                        }
//                    }
//                }
//            }
//        }
//        return templateReplaced;
//    }
//    
//    void replaceTemplates()
//    {
//        queueUpdate(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                // replace nested templates
//                boolean templateReplaced = true;
//                while(templateReplaced)
//                {
//                    templateReplaced = false;
//                    NodeList templateNodes = document.getElementsByTagName("*");
//                    for (int i = 0; i < templateNodes.getLength(); i++)
//                    {
//                        Node templateNode = templateNodes.item(i);
//                        if (hasClass(templateNode,"be.apsu.sarong.original"))
//                        {
//                            templateReplaced = templateReplaced || replaceTemplates((Element)templateNode);
//                        }
//                    }
//                }
//
//                // replace clones
//                replaceTemplates(document);
//                
//                FileWriter writer=null;
//                
//                try
//                {
//                    writer=new FileWriter(new File("/tmp/new.svg"));
//                    DOMUtilities.writeDocument(document, writer);
//                }
//                catch (IOException ex)
//                {
//                    Logger.getLogger(SarongPanel.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                finally
//                {
//                    try
//                    {
//                        writer.close();
//                    }
//                    catch (IOException ex)
//                    {
//                        Logger.getLogger(SarongPanel.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }  
//            }
//        });
//    }
//    
//    private boolean hasClass(Node templateNode, String className)
//    {
//        try
//        {
//            return templateNode.getAttributes().getNamedItem("class").getNodeValue().equalsIgnoreCase(className);
//        }
//        catch (NullPointerException ex)
//        {
//            return false;
//        }
//    }
    
    
    Element createDescription(String text)
    {
        Element description=document.createElementNS("http://www.w3.org/2000/svg","desc");
        Text    descriptionText=document.createTextNode(text);
        description.appendChild(descriptionText);
        return description;
    }
    
    public static String join( Iterable< ? extends Object > pColl, String separator )
    {
        Iterator< ? extends Object > oIter;
        if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) )
            return "";
        StringBuilder oBuilder = new StringBuilder( String.valueOf( oIter.next() ) );
        while ( oIter.hasNext() )
            oBuilder.append( separator ).append( oIter.next() );
        return oBuilder.toString();
    }
    
    public static String njoin(List<String> list, int count, String separator )
    {
        StringBuilder builder=new StringBuilder();
        
        if(count>list.size())
            count=list.size();
        
        for(int i=0;i<count;i++)
        {
            String slice=list.get(i);
            if(slice!=null)
            {
                builder.append('.');
                builder.append(slice);  
            }
        }
        
        return builder.toString().substring(1);
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
                    Node node=allNodes.item(i);
                    Node    idNode = node.getAttributes().getNamedItem("id");
                    if(idNode!=null)
                    {
                        String id=idNode.getNodeValue();
                        String[] idParts=id.split("\\.");
                        
                        if(idParts.length>4)
                        {
                            System.err.println("creating description for " + id);
                            Element desc=createDescription(id.substring(0,id.lastIndexOf('.')));
                            
                            Element element=(Element)node;
                            NodeList descriptions=element.getElementsByTagName("desc");
                            if(descriptions.getLength()>0)
                                element.removeChild(descriptions.item(0));
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
        this.listeners      = new HashSet<SarongPanelListener>();
        this.liveElements   = new HashMap<String, Element>();

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
                
                materializeTemplateUsages();
                removeTemplates();
                registerLiveElements();
                
                FileWriter writer=null;
                
                try
                {
                    writer=new FileWriter(new File("/tmp/new.svg"));
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
        if(document==null || liveElements==null)
            return null;
        Element element=liveElements.get(id);
        if(element==null)
            System.out.println(id);
        return element;
    }

    public void queueUpdate(Runnable updater)
    {
        if (canvas.getUpdateManager()==null)
            return;
        
        canvas.getUpdateManager().
                getUpdateRunnableQueue().
                invokeLater(updater);
    }
    
    private void _registerLiveElements(Node node, int xmlLevel, List<String> path)
    {
        String idSlice=getX3MonId(node);
        if(idSlice!=null)
        {       
            while(path.size()<(xmlLevel+1))
                path.add(null);

            path.set(xmlLevel, idSlice);

            if(!node.getNodeName().equalsIgnoreCase("g"))
            {
                liveElements.put(njoin(path,xmlLevel+1,"."),(Element)node); 
                System.out.println("[" + node.getNodeName() + "] " + njoin(path,xmlLevel+1,".") + " (" + getAttr(node, SVG_ID) + ")");
            }
        }

        NodeList kids=node.getChildNodes();
        for(int i=0;i<kids.getLength();i++)
            _registerLiveElements(kids.item(i),xmlLevel+1,path);
        
         while(path.size()>(xmlLevel))
            path.remove(path.size()-1);
    }
    
    private void registerLiveElements()
    {
        _registerLiveElements(document,0,new ArrayList<String>());
        for(Entry<String,Element> liveElement:liveElements.entrySet())
            liveElement.getValue().setAttribute(SVG_ID,liveElement.getKey());
    }

    private void _removeTemplates(Node node)
    {
        if(isX3MonTemplate(node))
            node.getParentNode().removeChild(node);
        NodeList kids=node.getChildNodes();
        for(int i=0;i<kids.getLength();i++)
            _removeTemplates(kids.item(i));
    }
    
    private void removeTemplates()
    {
        _removeTemplates(document);               
    }
    
    private void _materializeTemplateUsages(Node node)
    {
        String idSlice=getX3MonId(node);
        if(idSlice!=null)
        {
            if(node.getNodeName().equals(SVG_USE))
            {
                String originalId=getAttrNS(node, XLINK_NS_URL, XLINK_HREF);
                if(originalId!=null)
                {
                    Node original=document.getElementById(originalId.substring(1));
                    if(original!=null && isX3MonTemplate(original))
                    {
                        Node materialNode=original.cloneNode(true);
                        removeAttribute(materialNode,SVG_ID);
                        removeAttributeNS(materialNode,X3MON_NS,X3MON_USAGE);
                        cloneAttribute(node,materialNode,SVG_TRANSFORM);
                        cloneAttributeNS(node,materialNode,X3MON_NS,X3MON_ID);
                        node.getParentNode().replaceChild(materialNode,node);
                    }
                }
            }
        }

        NodeList kids=node.getChildNodes();
        for(int i=0;i<kids.getLength();i++)
            _materializeTemplateUsages(kids.item(i));
    }
     
    private void materializeTemplateUsages()
    {
        _materializeTemplateUsages(document);               
    }
    
    private String getAttr(Node node, String attrName)
    { 
        NamedNodeMap attrNodeMap=node.getAttributes();
        if(attrNodeMap==null)
            return null;
        Node valueNode = attrNodeMap.getNamedItem(attrName);
        if(valueNode==null)
            return null;
        return valueNode.getNodeValue();
    }
    
    private boolean setAttr(Node node, String attrName, String attrValue)
    { 
        NamedNodeMap attrNodeMap=node.getAttributes();
        if(attrNodeMap==null)
            return false;
        
        Node valueNode = attrNodeMap.getNamedItem(attrName);
        if(valueNode==null)
            return false;
        
        valueNode.setNodeValue(attrValue);

        return true;
    }
    
    private String getAttrNS(Node node, String nameSpace, String attrName)
    { 
        NamedNodeMap attrNode=node.getAttributes();
        if(attrNode==null)
            return null;
        Node valueNode = attrNode.getNamedItemNS(nameSpace,attrName);
        if(valueNode==null)
            return null;
        return valueNode.getNodeValue();
    }
    
    private String getX3MonAttr(Node node, String attrName)
    { 
        return getAttrNS(node,X3MON_NS,attrName);
    }
    
    private String getX3MonId(Node node)
    {
        return getX3MonAttr(node, X3MON_ID);
    }
    
    private boolean isX3MonUsage(Node node, String usage)
    {
        String x3MonUsage=getX3MonAttr(node,X3MON_USAGE);
        if(x3MonUsage==null)
            return false;
        return x3MonUsage.contains(usage);
    }
    
    private boolean isX3MonTemplate(Node node)
    {
        boolean is=isX3MonUsage(node, X3MON_TEMPLATE);
        return is;
    }
    
    private void cloneAttribute(Node source, Node destination, String attrName)
    {
        Node attrNode = source.getAttributes().getNamedItem(attrName);
        if(attrNode!=null)
            destination.getAttributes().setNamedItem(attrNode.cloneNode(true));
    }
    
    private void cloneAttributeNS(Node source, Node destination, String nameSpace, String attrName)
    {
        Node attrNode = source.getAttributes().getNamedItemNS(nameSpace, attrName);
        if(attrNode!=null)
            destination.getAttributes().setNamedItemNS(attrNode.cloneNode(true));
    }
    
    private void removeAttribute(Node node, String attrName)
    {
        NamedNodeMap attrNode=node.getAttributes();
        if(attrNode==null)
            return;
        attrNode.removeNamedItem(attrName);
    }
    
    private void removeAttributeNS(Node node, String nameSpace, String attrName)
    {
        NamedNodeMap attrNode=node.getAttributes();
        if(attrNode==null)
            return;
        attrNode.removeNamedItemNS(nameSpace, attrName);
    }
}
