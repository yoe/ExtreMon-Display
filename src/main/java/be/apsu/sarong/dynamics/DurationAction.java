package be.apsu.sarong.dynamics;

import java.util.Map;
import org.joda.time.Period;
import org.joda.time.PeriodType;
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
            .appendWeeks()
            .appendSuffix(" week ", " weeks ")
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
        String formattedValue=formatter.print(period.normalizedStandard(PeriodType.yearWeekDayTime()));
        String on=substitutions(getOnTemplate(),variables, formattedValue);
        if(getElement(on)!=null)
            queueSet(on,substitutions(getValueTemplate(),variables, formattedValue));
    }
}
