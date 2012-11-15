/* ExtreMon Project
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.w3c.dom.Element;
import be.apsu.extremon.panel.X3Panel;

public class TimestampSetAction extends AbstractAction {
    private static final int DECISECONDS = 100;
    private SimpleDateFormat timestampFormat;
    private Calendar timestampCalendar;

    public TimestampSetAction(X3Panel panel, Element element,
	    String attribute, String format) {
	super(panel, element, attribute, format.replace("#", "%s"));
	this.timestampCalendar = Calendar.getInstance();
	this.timestampFormat = new SimpleDateFormat(
		"yyyy.MM.dd.HH.mm.ss.");
    }

    @Override
    public final void perform(String rawValue) {
	try {
	    final long timeStampInMs = (long) (Double
		    .parseDouble(rawValue) * 1000.0);
	    this.timestampCalendar.setTimeInMillis(timeStampInMs);
	    final String formattedDate = this.timestampFormat
		    .format(this.timestampCalendar.getTime())
		    + (this.timestampCalendar.get(Calendar.MILLISECOND) / DECISECONDS);
	    final String formattedValue = String.format(getFormat(),
		    formattedDate);
	    queueAlteration(formattedValue);
	} catch (NumberFormatException nfe) {
	    queueAlteration(nfe.getLocalizedMessage());
	}
    }
}
