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

public class X3Measure
{
	public String label;
	public String value;
	
	public X3Measure(String label, String value)
	{
		this.label = label;
		this.value = value;	
	}

	@Override
	public boolean equals(Object _that)
	{
		if(!(_that instanceof X3Measure))
			return false;
		X3Measure that=(X3Measure)_that;
		return that.label.equals(this.label);
	}

	@Override
	public int hashCode()
	{
		return this.label.hashCode();
	}
}
