/*
 * TimeStampElement Copyright (c) 2008,2009 Frank Marien
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;

public class TimeStampElement extends AbstractElement
{
	private SimpleDateFormat	format;
	private DecimalFormat		decimals = new DecimalFormat("0");

	public TimeStampElement(Element element,String id,String formatStr)
	{
		super(element,id);
		this.format=new SimpleDateFormat(formatStr);
	}

	public String formatTimeStamp(double timeStamp)
	{
		return format.format(new Date((long)timeStamp*1000)) + ":" + decimals.format(10*(timeStamp-(long)timeStamp));
	}

	public void setTimeStamp(double timeStamp)
	{
		final String formattedStr=formatTimeStamp(timeStamp);
		queueUpdate(new Runnable()
		{
            @Override
			public void run()
			{
				getElement().setTextContent(formattedStr);
			}
		});
	}
}
