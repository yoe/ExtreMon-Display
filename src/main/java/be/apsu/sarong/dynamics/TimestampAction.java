package be.apsu.sarong.dynamics;

import java.text.DecimalFormat;
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
    private Format      format,lagFormat;
    
    public TimestampAction(String attribute, String valueTemplate, String onTemplate)
    {
        super(attribute,valueTemplate,onTemplate);
        calendar=Calendar.getInstance();
        format=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.");
        lagFormat = new DecimalFormat("0000");
    }

    @Override
    public void performAction(Map variables)
    {
        long serverMillis=(long)(((Double)variables.get("value")).doubleValue()*1000);
        long clientMillis=System.currentTimeMillis();
        
        calendar.setTimeInMillis(serverMillis);

        variables.put("formattedValue", format.format(calendar.getTime()) + (calendar.get(Calendar.MILLISECOND)/100) + " (" + lagFormat.format(serverMillis-clientMillis) + "ms)");

        String on=substitutions(getOnTemplate(),variables);
        if(getElement(on)!=null)
        {
            queueSet(on,substitutions(getValueTemplate(),variables));
        }
    }
}
