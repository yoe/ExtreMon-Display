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

package be.apsu.extremon.client;

public class X3TimeValue
{
	private double	 time;
	private String	 value;
	
	public X3TimeValue(double time, String value)
	{
		super();
		this.time = time;
		this.value = value;
	}
	
	public double getTime()
	{
		return time;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public X3TimeValue setTime(long time)
	{
		this.time = time;
		return this;
	}
	
	public X3TimeValue setValue(String value)
	{
		this.value = value;
		return this;
	}	
}
