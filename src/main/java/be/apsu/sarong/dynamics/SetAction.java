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
    public void performAction(Map variables)
    {
        String value=null;
        
        if(getFormat()!=null)
        {
            StringBuilder stringBuilder=new StringBuilder();
            Formatter formatter=new Formatter(stringBuilder,Locale.US);
            
            Double dValue=(Double)variables.get("value");
            formatter.format(getFormat(),dValue.doubleValue()*getMultiplier());
            value=stringBuilder.toString();
        }
        else
        {
            Double dValue=(Double)variables.get("value");
            value=String.valueOf(dValue.doubleValue()*getMultiplier());
        }

        variables.put("formattedValue", value);

        String on=substitutions(getOnTemplate(),variables);
        
        
        
        if(getElement(on)!=null)
        {
           // if(on.startsWith("be.fedict.eid.prod"))
            //    System.out.println("SETTING " + on + "=" + value);
            queueSet(on,substitutions(getValueTemplate(),variables));
        }
        else
        {
           if(on.contains("percentage"))
               System.out.println("**** FAILED " + on + "=" + value + " ; NOT FOUND"); 
        }
    }
}
