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
import org.w3c.dom.Element;
import be.apsu.extremon.panel.X3Panel;

public class NumericSetAction extends AbstractAction
{
	private double scale;
	
	public NumericSetAction(X3Panel panel,Element element,String attribute,String format)
	{
		this(panel,element,attribute,format,1.0);
	}
	
	public NumericSetAction(X3Panel panel,Element element,String attribute,String format, double scale)
	{
		super(panel,element,attribute,format.replace("#","%.0f"));	
		this.scale=scale;
	}

	@Override
	public final void perform(String rawValue)
	{
		double doubleValue;
		
		try
		{
			doubleValue=this.scale * Double.parseDouble(rawValue);
		}
		catch(NumberFormatException nfe)
		{
			doubleValue=Double.NaN;
		}
		
		String formattedValue=null;
		final StringBuilder stringBuilder=new StringBuilder();
		final Formatter formatter=new Formatter(stringBuilder,Locale.US);

		try
		{
			formatter.format(getFormat(),doubleValue);
			formattedValue=stringBuilder.toString();
			if(formattedValue.startsWith("$"))
			{
				final String lookedUpValue=getDefinedValue(formattedValue.substring(1));
				if(lookedUpValue==null)
					formattedValue="Can't Substitute ["+formattedValue+"]. Undefined..";
				else
					formattedValue=lookedUpValue;
			}
		}
		catch(NumberFormatException nfe)
		{
			formattedValue=nfe.getLocalizedMessage();
		}
		finally
		{
			formatter.close();
		}

		queueAlteration(formattedValue);
	}
}
