package be.apsu.sarong.dynamics;

import be.apsu.sarong.panel.SarongCanvas;
import be.apsu.sarong.panel.SarongPanel;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Element;

/**
 *
 * @author frank@apsu.be
 */
public abstract class Action
{
    private String          attribute;
    private String          valueTemplate;
    private String          onTemplate;
    private String          format;
    private double          multiplier;
    private SarongPanel     panel;

    public Action(String attribute, String valueTemplate, String onTemplate)
    {
        this.attribute      = attribute;
        this.valueTemplate  = valueTemplate;
        this.onTemplate     = onTemplate;
        this.multiplier     = 1.0;
    }
    
    public String getAttribute()
    {
        return attribute;
    }

    public void setAttribute(String attribute)
    {
        this.attribute = attribute;
    }

    public String getOnTemplate()
    {
        return onTemplate;
    }

    public void setOnTemplate(String onTemplate)
    {
        this.onTemplate = onTemplate;
    }

    public String getValueTemplate()
    {
        return valueTemplate;
    }

    public void setValueTemplate(String valueTemplate)
    {
        this.valueTemplate = valueTemplate;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public double getMultiplier()
    {
        return multiplier;
    }

    public void setMultiplier(double multiplier)
    {
        this.multiplier = multiplier;
    }

    public SarongPanel getPanel()
    {
        return panel;
    }

    public void setPanel(SarongPanel panel)
    {
        this.panel = panel;
    }

    public SarongCanvas getCanvas()
    {
        return panel.getCanvas();
    }

    public Element getElement(String id)
    {
        return getPanel().getElementById(id);
    }
    
    protected String substitutions(String template,Map vars)
	{
		for(Iterator<Entry<String,String>> iter=vars.entrySet().iterator();iter.hasNext();)
		{
			Entry var=iter.next();
			try
			{
				template=template.replace("${"+var.getKey()+"}",(String)var.getValue());
			}
			catch(ClassCastException cce)
			{
				Double value=(Double)var.getValue();
				template=template.replace("${"+var.getKey()+"}",String.valueOf(value));
			}

		}
		return template;
	}

    public abstract void performAction(Map variables);

    protected void queueSet(final String on, final String value)
    {
        if(getCanvas()==null)
			return;
        
        getCanvas().queueUpdate(new Runnable()
        {
            @Override
            public void run()
            {
                if(getAttribute().equals("cdata"))
                    getElement(on).setTextContent(value);
                else
                    getElement(on).setAttribute(getAttribute(),value);
            }
        });
    }
}
