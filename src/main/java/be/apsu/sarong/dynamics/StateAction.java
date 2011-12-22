package be.apsu.sarong.dynamics;

import java.util.Map;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author frank@apsu.be
 */
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