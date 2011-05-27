/*
 * MeasureElement Copyright (c) 2008,2009 Frank Marien
 * 
 *  This file is part of Sarong.

    Sarong is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sarong is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Sarong.  If not, see <http://www.gnu.org/licenses/>.

 */

package be.apsu.sarong.common;

import java.util.Formatter;
import java.util.Locale;

import org.w3c.dom.Element;

public class MeasureElement extends AbstractElement
{	
	private double	multiplier;
	private String	format;

	public MeasureElement(Element element,String id,String attribute,double multiplier)
	{
		super(element,id,attribute);
		this.multiplier=multiplier;
	}

	public MeasureElement(Element element,String id,String attribute,double multiplier,String format)
	{
		super(element,id,attribute);
		this.multiplier=multiplier;
		this.format=format;
	}

	public double getMultiplier()
	{
		return multiplier;
	}

	public void setValue(double value)
	{
		final double nValue=value;
		queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				if(getAttribute().equals("cdata"))
				{
					if(format!=null)
					{
						StringBuilder stringBuilder=new StringBuilder();
						Formatter formatter=new Formatter(stringBuilder,Locale.US);
						formatter.format(format,new Double(nValue*multiplier));
						getElement().setTextContent(stringBuilder.toString());
					}
					else
					{
						getElement().setTextContent(""+(nValue*getMultiplier()));
					}
				}
				else
				{
					getElement().setAttribute(getAttribute(),""+(nValue*getMultiplier()));
				}
			}
		});
	}
}
