package be.apsu.sarong.dynamics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author frank@apsu.be
 */
public class Measure
{
    private Pattern         pattern;
    private String[]        captureNames;
    private Set<Action>     actions;

    public Measure(String patternStr)
    {
        this.pattern = Pattern.compile(patternStr);
        this.actions=new HashSet<Action>();
    }
    
    public boolean hasActions()
    {
        return !actions.isEmpty();
    }

    public boolean addAction(Action action)
    {
        return actions.add(action);
    }

    public void setCaptures(String capturesStr)
	{
		captureNames=capturesStr.split(",");
	}

    public void evaluate(String label, String value)
    {
        Matcher matcher=pattern.matcher(label);
        if(matcher.matches())
        {
            try
            {
                Map variables=new HashMap(matcher.groupCount());
                variables.put("id", label);
                variables.put("value", new Double(value));
                for(int i=0;i<matcher.groupCount();i++)
                    variables.put(captureNames[i],matcher.group(i+1));
                for(Action action: actions)
                action.performAction(variables);
            }
            catch(Exception ex)
            {
                System.err.println(ex);
            }
        }
    }
}
