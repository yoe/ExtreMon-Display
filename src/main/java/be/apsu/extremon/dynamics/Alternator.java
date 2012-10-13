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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Alternator implements Runnable
{
	private Set<Alteration> alterations;

	public Alternator()
	{
		this.alterations=new HashSet<Alteration>();
	}

	public final Alternator addAll(Collection<Alteration> moreAlterations)
	{
		this.alterations.addAll(moreAlterations);
		return this;
	}
	
	public final Alternator add(final Alteration anotherAlteration)
	{
		this.alterations.add(anotherAlteration);
		return this;
	}

	@Override
	public final void run()
	{
		for(Alteration alteration : this.alterations)
			alteration.alter();
	}
}
