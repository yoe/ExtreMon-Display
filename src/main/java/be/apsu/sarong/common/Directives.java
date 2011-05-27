/*
 * Directives Copyright (c) 2008 Frank Marien
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Directives
{
	private Set<Directive> directives;

	public Directives()
	{
		super();
		directives=new HashSet<Directive>();
	}

	public Iterator<Directive> getDirectiveIterator()
	{
		return directives.iterator();
	}

	public Directives addDirective(Directive e)
	{
		directives.add(e);
		return this;
	}

	public boolean hasDirectives()
	{
		return !directives.isEmpty();
	}

	public int directiveCount()
	{
		return directives.size();
	}
}
