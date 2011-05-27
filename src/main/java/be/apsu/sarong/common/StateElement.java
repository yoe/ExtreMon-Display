/*
 * StateElement Copyright (c) 2008 Frank Marien
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
import java.util.Map;

import org.w3c.dom.Element;

public class StateElement extends AbstractElement
{
	private Map<String,String>	stateMap;

	public StateElement(Element element,String id,String attribute)
	{
		super(element,id,attribute);
		this.stateMap=new HashMap<String,String>(8);
	}

	public void setState(String state)
	{
		final String nState=state;
		queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				
				String mappedValue=getMappedValue(nState);
				if(mappedValue!=null)
					getElement().setAttribute(getAttribute(),mappedValue);
			}
		});
	}

	public boolean hasStates()
	{
		return !stateMap.isEmpty();
	}

	public String setValueForState(String key,String value)
	{
		return stateMap.put(key,value);
	}

	private String getMappedValue(String state)
	{
		return stateMap.get(state);
	}
}
