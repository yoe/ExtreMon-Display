package be.apsu.sarong.dynamics;

import java.util.Calendar;
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
    public void performAction(Map variables)
    {
        long serverMillis=(long)(((Double)variables.get("value")).doubleValue()*1000);
        
        Period period=new Period(serverMillis);

        variables.put("formattedValue", formatter.print(period.normalizedStandard(PeriodType.yearWeekDayTime())));

        String on=substitutions(getOnTemplate(),variables);
        if(getElement(on)!=null)
        {
            queueSet(on,substitutions(getValueTemplate(),variables));
        }
    }
}
