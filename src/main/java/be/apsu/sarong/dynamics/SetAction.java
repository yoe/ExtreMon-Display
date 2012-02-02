package be.apsu.sarong.dynamics;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author frank@apsu.be
 */
public class SetAction extends Action
{
    public SetAction(String attribute, String valueTemplate, String onTemplate)
    {
        super(attribute,valueTemplate,onTemplate);
    }

    @Override
    public void performAction(Map<String,String> variables, double nValue, String sValue)
    {    	
    	String value=null;
    	
    	if(sValue==null)
    	{
	        try
	        {
		        if(getFormat()!=null)
		        {
		            StringBuilder stringBuilder=new StringBuilder();
		            Formatter formatter=new Formatter(stringBuilder,Locale.US);
		            formatter.format(getFormat(),nValue*getMultiplier());
		            value=stringBuilder.toString();
		        }
		        else
		        {
		        	value=String.valueOf(nValue*getMultiplier());
		        }
		        
		        
	        }
	        catch(NumberFormatException nfe)
	        {
	        	value=nfe.getLocalizedMessage();
	        }
    	}
    	else
    	{
    		value=sValue;
    	}

        String on=substitutions(getOnTemplate(),variables,value);
        
        if(getElement(on)!=null)
            queueSet(on,substitutions(getValueTemplate(),variables,value));
    }
}
