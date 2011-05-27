/*
 * Directive Copyright (c) 2008,2009 Frank Marien
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * directive [;directive]..
 * directive:
 * 	attribute:value [,attribute:value]..
 */

public class Directive
{
	private String				type;
	private Map<String,String>	attributes;

	public Directive(String type)
	{
		super();
		this.type=type;
		this.attributes=new HashMap<String,String>();
	}

	public String getType()
	{
		return type;
	}

	public Iterator<Map.Entry<String,String>> attributesIterator()
	{
		return attributes.entrySet().iterator();
	}

	public String getStringAttribute(String key,String deflt)
	{
		String result=attributes.get(key);
		if(result==null)
			result=deflt;
		return result;
	}

	public double getDoubleAttribute(String key,double deflt)
	{
		String doubleStr=attributes.get(key);
		double result=deflt;

		if(doubleStr!=null)
		{
			try
			{
				result=Double.parseDouble(doubleStr);
			}catch(NumberFormatException nfe)
			{
				result=deflt;
			}
		}
		return result;
	}

	public Object setAttribute(String key,String value)
	{
		return attributes.put(key,value);
	}

	public int attributeCount()
	{
		return attributes.size();
	}

	public boolean hasAttribute(String key)
	{
		return attributes.containsKey(key);
	}
}
