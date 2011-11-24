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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private static final long           serialVersionUID = -6964246820463870400L;
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
//    void replaceTemplate(Element clone, Element template)
//    {
//        Element duplicate = (Element) template.cloneNode(true);
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
//                        if (templateElement != null && hasClass(templateElement, "be.apsu.sarong.template")) {
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
//                        if (hasClass(templateNode,"be.apsu.sarong.template"))
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
    
    void _setFQids(Node node, int xmlLevel, List<String> path)
    {
        NamedNodeMap attr=node.getAttributes();
        if(attr!=null && attr.getLength()>0)
        {
            Node idSliceN = node.getAttributes().getNamedItemNS("http://extremon.org/ns/extremon","idslice");
            if(idSliceN!=null)
            {
                String idslice=idSliceN.getNodeValue();
                if(path.size()<=xmlLevel)
                    for(int i=-2;i<=(xmlLevel-path.size());i++)
                        path.add(null);
                path.set(xmlLevel, idslice);
                liveElements.put(njoin(path,xmlLevel+1,"."),(Element)node); 
                System.out.println(njoin(path,xmlLevel+1,"."));
            }
        }

        NodeList kids=node.getChildNodes();
        if(kids.getLength()>0)
        {
            for(int i=0;i<kids.getLength();i++)
            {
                Node kid=kids.item(i); 
                _setFQids(kid,xmlLevel+1,path);
            }
        }
    }
     
    void setFQids()
    {
        _setFQids(document,0,new ArrayList<String>());       
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
                
                setFQids();

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
}
