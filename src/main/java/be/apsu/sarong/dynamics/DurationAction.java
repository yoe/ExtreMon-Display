package be.apsu.sarong.dynamics;

import java.util.Map;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 *
 * @author frank@apsu.be
 */
public class DurationAction extends Action
{
    private PeriodFormatter formatter;
    
    public DurationAction(String attribute, String valueTemplate, String onTemplate)
    {
        super(attribute,valueTemplate,onTemplate);
        formatter = new PeriodFormatterBuilder()
        .appendYears()
        .appendSuffix(" year ", " years ")
        .appendMonths()
        .appendSuffix(" month ", " months ")
        .appendMonths()
        .appendSuffix(" day ", " days ")
        .appendHours()
        .appendSuffix(" hour ", " hours ")
        .appendMinutes()
        .appendSuffix(" minute ", " minutes ")
        .appendSeconds()
        .appendSuffix(" second", " seconds")
        .toFormatter();
    }

    @Override
    public void performAction(Map<String,String> variables, double nValue, String sValue)
    {
    	if(sValue!=null)
    		return;
        long serverMillis=(long)(nValue*1000);
        Period period=new Period(serverMillis);
        String formattedValue=formatter.print(period);
        String on=substitutions(getOnTemplate(),variables, formattedValue);
        if(getElement(on)!=null)
            queueSet(on,substitutions(getValueTemplate(),variables, formattedValue));
    }
}
