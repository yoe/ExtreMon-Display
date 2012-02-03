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

package be.apsu.extremon.dynamics;

import java.util.Map;
import org.w3c.dom.Element;

public class StateAction extends Action
{
    private String[] stateValues;
    
    public StateAction(String attribute, String onTemplate, String[] stateValues)
    {
        super(attribute,"",onTemplate);
        this.stateValues=stateValues;
    }
    
  /*  public void animatedColor(final Element el, final String colors)
	{
		unanimateColor(el);
		
		queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				String svgNS=SVGDOMImplementation.SVG_NAMESPACE_URI;
				Element animator=el.getOwnerDocument().createElementNS(svgNS,"animate");
				animator.setAttributeNS(null,"id","colorAnimation."+el.getAttribute("id"));
				animator.setAttributeNS(null,"attributeName",getAttribute());
				animator.setAttributeNS(null,"values",colors);
				animator.setAttributeNS(null,"dur","1s");
				animator.setAttributeNS(null,"begin","0s");
				animator.setAttributeNS(null,"repeatDur","indefinite");
				animator.setAttributeNS(null,"calcMode","linear");

				try
				{
					el.appendChild(animator);
				}
				catch(ClassCastException cce)
				{
					//System.err.println("Applied state to wrong type:"+cce.getMessage());
				}
			}
		});
	}

	public void unanimateColor(final Element el)
	{
		queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				NodeList kids=el.getChildNodes();
				for(int i=0;i<kids.getLength();i++)
				{
					try
					{
						Element victim=(Element)kids.item(i);
						if(victim.getAttribute("id").equals("colorAnimation."+el.getAttribute("id")))
						{
							el.removeChild(victim);
						}
					}
                    catch(ClassCastException cce)
					{
						//System.err.println("Applied state to wrong type:"+cce.getMessage());
					}
				}
			}
		});
	} */

    @Override
    public void performAction(Map<String,String> variables, double nValue, String sValue)
    {
    	if(sValue!=null)
    		return;
    	
        String on=substitutions(getOnTemplate(),variables,null);
        String value=stateValues[(int)nValue];
        
        //System.out.println("WANT TO SET " + on + "=" + value + " for state " + code);
        
        Element onElem=getElement(on);
        
        if(onElem!=null)
        {
        	queueSet(on,value);
        }
        else
        {
            //System.out.println("FAILED " + on + "=" + value + " ; NOT FOUND");
        }
    }
}