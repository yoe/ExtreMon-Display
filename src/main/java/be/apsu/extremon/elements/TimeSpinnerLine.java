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

package be.apsu.extremon.elements;

import org.w3c.dom.Element;

import be.apsu.extremon.dynamics.Alteration;

public class TimeSpinnerLine 
{
	private Element element;
	private double	centerX,centerY;
	
	public TimeSpinnerLine(Element element)
	{
		super();
		this.element = element;
		double y1=Double.parseDouble(element.getAttribute("y1"));
		double y2=Double.parseDouble(element.getAttribute("y2"));
		
		centerX=Double.parseDouble(element.getAttribute("x1"));
		centerY=y1+((y2-y1)/2.0);
	}
	
	public Alteration setSpinPosition(int percent)
	{
		StringBuffer buffer=new StringBuffer("rotate(");
					 buffer.append(percent * 3.6);
					 buffer.append(' ');
					 buffer.append(centerX);
					 buffer.append(' ');
					 buffer.append(centerY);
					 buffer.append(')');
					 
		return new Alteration(element,"transform",buffer.toString());
					 
	}
}
