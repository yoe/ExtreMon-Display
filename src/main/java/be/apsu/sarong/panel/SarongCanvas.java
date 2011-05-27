/*
 * SarongCanvas Copyright (c) 2008,2009 Frank Marien
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

package be.apsu.sarong.panel;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgent;

public class SarongCanvas extends JSVGCanvas
{
	private static final long	serialVersionUID	=-5879264608991434474L;

	public SarongCanvas()
	{
		super();
	}

	public SarongCanvas(SVGUserAgent ua,boolean eventsEnabled,boolean selectableText)
	{
		super(ua,eventsEnabled,selectableText);
	}
	
	public void queueUpdate(Runnable updater)
	{
		getUpdateManager().getUpdateRunnableQueue().invokeLater(updater);
	}

}
