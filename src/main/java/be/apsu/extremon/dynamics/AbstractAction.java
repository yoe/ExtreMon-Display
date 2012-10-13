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
import be.apsu.extremon.panel.X3Panel;

public abstract class AbstractAction
{
	private String	attribute;
	private Element	element;
	private String	format;
	private X3Panel	panel;

	public AbstractAction(X3Panel panel, Element element, String attribute, String format)
	{
		this.attribute=attribute;
		this.format=format;
		this.element=element;
		this.panel=panel;
	}
	
	public abstract void perform(String value);
	
	
	public final void queueAlteration(final String value)
	{
		this.panel.queueAlteration(this.element,this.attribute,value);
	}
	
	public final String getDefinedValue(final String key)
	{
		return this.panel.getDefinedValue(key);
	}

	public final String getFormat()
	{
		return this.format;
	}
}
