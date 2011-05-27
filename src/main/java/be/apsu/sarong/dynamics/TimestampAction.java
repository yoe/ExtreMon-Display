package be.apsu.sarong.dynamics;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/**
 *
 * @author frank@apsu.be
 */
public class TimestampAction extends Action
{
    private Calendar    calendar;
    private Format      format;
    
    public TimestampAction(String attribute, String valueTemplate, String onTemplate)
    {
        super(attribute,valueTemplate,onTemplate);
        calendar=Calendar.getInstance();
        format=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.");
    }

    @Override
    public void performAction(Map variables)
    {
        calendar.setTimeInMillis((long)(((Double)variables.get("value")).doubleValue()*1000));
        
        variables.put("formattedValue", format.format(calendar.getTime()) + (calendar.get(Calendar.MILLISECOND)/100));

        String on=substitutions(getOnTemplate(),variables);
        if(getElement(on)!=null)
        {
            queueSet(on,substitutions(getValueTemplate(),variables));
        }
    }
}
