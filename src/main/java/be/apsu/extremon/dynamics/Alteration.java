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

import org.w3c.dom.Element;

public class Alteration
{
	private Element	on;
	private String	attribute;
	private String	value;

	public Alteration(Element on, String attribute, String value)
	{
		super();
		this.on = on;
		this.attribute = attribute;
		
		if(attribute!=null && attribute.equals("width") && value.equals("0.0"))
			this.value="0.00001";
		else
			this.value = value;
	}

	public void alter()
	{
		if (attribute != null)
			on.setAttribute(attribute, value);
		else
			on.setTextContent(value);
	}

	// our identity in collections depends on the element:attribute tuple we act upon
	// so we get replaced by the last instance acting on the same element

	@Override
	public boolean equals(Object _that)
	{
		if (this == _that)
			return true;
		if(!(_that instanceof Alteration))
			return false;
		Alteration that = (Alteration)_that;
		return (this.on.equals(that.on)) && ((this.attribute == null) ? that.attribute == null : this.attribute.equals(that.attribute));
	}

	@Override
	public int hashCode()
	{
		int hash = 1;
		hash = hash * 31 + on.hashCode();
		hash = hash * 31 + (attribute == null ? 0 : attribute.hashCode());
		return hash;
	}

	@Override
	public String toString()
	{
		return on.getAttribute("id") + ":" + (attribute!=null?attribute:"cdata") + " ALTER TO " + value;
	}
}
