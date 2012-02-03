 /*
  * ExtreMon Project
  * Copyright (C) 2009-2012 Frank Marien
  * frank@apsu.be
  *  
  * This file is part of ExtreMon.
  *    
  * ExtreMon is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ExtreMon is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ExtreMon.  If not, see <http://www.gnu.org/licenses/>.
  */

package be.apsu.extremon.dynamics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Measure
{
	private static final Logger logger=Logger.getLogger(Measure.class.getName());
	
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

    public Map<String,String> evaluate(String label, String value)
    {
        Matcher matcher=pattern.matcher(label);
        if(matcher.matches())
        {
            System.err.println("Matched Regex For " + label);
            
            try
            {
                Map<String,String> variables=new HashMap<String,String>(matcher.groupCount());
                variables.put("label", label);
                for(int i=0;i<matcher.groupCount();i++)
                    variables.put(captureNames[i],matcher.group(i+1));
                return variables;   
            }
            catch(Exception ex)
            {
            	logger.log(Level.SEVERE,"c " + label + "=" + value,ex);
                return null;
            }
        }
        
        return null;
    }
    
    public void act(Map<String,String> variables, String value)
    {
    	String sValue=null;
    	double dValue=Double.NaN;
    
    	try
    	{
    		dValue=Double.parseDouble(value);
    	}
    	catch(NumberFormatException nfe)
    	{
    		sValue=value;
    	}

    	for(Action action: actions)
            action.performAction(variables,dValue,sValue);	
    }
}
