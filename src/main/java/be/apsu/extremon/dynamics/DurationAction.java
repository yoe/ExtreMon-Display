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

import java.util.Map;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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
