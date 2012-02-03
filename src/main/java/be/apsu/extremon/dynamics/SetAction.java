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

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

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
