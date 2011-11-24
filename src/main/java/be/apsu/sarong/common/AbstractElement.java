/*
 * AbstractElement Copyright (c) 2008,2009 Frank Marien
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

package be.apsu.sarong.common;

import be.apsu.sarong.panel.SarongCanvas;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AbstractElement
{
	private String			id;
	private Element			element;
	private String			attribute;
	private SarongCanvas	canvas;

	public AbstractElement(Element element,String id)
	{
		super();
		this.id=id;
		this.element=element;
		this.attribute=null;
	}

	public AbstractElement(Element element,String id,String attribute)
	{
		super();
		this.id=id;
		this.element=element;
		this.attribute=attribute;
	}

	public String getId()
	{
		return id;
	}

	public Element getElement()
	{
		return element;
	}

	public String getAttribute()
	{
		return attribute;
	}

	public void setAttribute(String attribute)
	{
		this.attribute=attribute;
	}

	protected SarongCanvas getCanvas()
	{
		return canvas;
	}

	public void setCanvas(SarongCanvas canvas)
	{
		this.canvas=canvas;
	}

	protected void queueUpdate(Runnable updater)
	{
		if(getCanvas()==null)
			return;
		getCanvas().queueUpdate(updater);
	}

	public void animateColor(final String colors)
	{
		unanimateColor();
		
		queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				String svgNS=SVGDOMImplementation.SVG_NAMESPACE_URI;
				Element animator=getElement().getOwnerDocument().createElementNS(svgNS,"animate");
				animator.setAttributeNS(null,"id","colorAnimation."+id);
				animator.setAttributeNS(null,"attributeName",getAttribute());
				animator.setAttributeNS(null,"values",colors);
				animator.setAttributeNS(null,"dur","1s");
				animator.setAttributeNS(null,"begin","0s");
				animator.setAttributeNS(null,"repeatDur","indefinite");
				animator.setAttributeNS(null,"calcMode","linear");

				try
				{
					getElement().appendChild(animator);
				}
				catch(ClassCastException cce)
				{
					System.err.println("Applied state to wrong type:"+cce.getMessage());
				}
			}
		});
	}

	public void unanimateColor()
	{
		queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				NodeList kids=getElement().getChildNodes();
				for(int i=0;i<kids.getLength();i++)
				{
					try
					{
						Element victim=(Element)kids.item(i);
						if(victim.getAttribute("id").equals("colorAnimation."+id))
						{
							getElement().removeChild(victim);
						}
					}catch(ClassCastException cce)
					{
						System.err.println("Applied state to wrong type:"+cce.getMessage());
					}
				}
			}
		});
	}
}
