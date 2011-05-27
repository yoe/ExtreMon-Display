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
import org.w3c.dom.svg.SVGDocument;

public class SarongPanel
{

    private static final long serialVersionUID = -6964246820463870400L;
    private String name;
    private SarongCanvas canvas;
    private SVGDocument document;
    private Set<SarongPanelListener> listeners = new HashSet<SarongPanelListener>();

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

    public SarongPanel(String name)
    {
        this.name = name;
        this.canvas = new SarongCanvas();
        this.canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        this.canvas.setAnimationLimitingNone();
        this.canvas.setBackground(Color.black);

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
        if(document==null)
            return null;
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
