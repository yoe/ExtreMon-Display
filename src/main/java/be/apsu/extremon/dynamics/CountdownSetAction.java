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

public class CountdownSetAction extends AbstractAction {
    private static final long MINUTES = 60;
    private static final long HOURS = MINUTES * 60;
    private static final long DAYS = HOURS * 24;
    private static final long WEEKS = DAYS * 7;
    private static final long MONTHS = WEEKS * 4;
    private static final long YEARS = MONTHS * 12;

    public CountdownSetAction(X3Panel panel, Element element,
	    String attribute, String format) {
	super(panel, element, attribute, format.replace("#", "%s"));
    }

    @Override
    public final void perform(String rawValue) {
	try {
	    final StringBuffer formatted = new StringBuffer();
	    long secondsLeft = (long) (Double.parseDouble(rawValue));
	    final boolean negative = secondsLeft < 0;
	    secondsLeft = Math.abs(secondsLeft);

	    final long years = Math.abs(secondsLeft) / YEARS;
	    if (years > 0) {
		formatted.append(years
			+ (years == 1 ? " year " : " years "));
		secondsLeft -= years * YEARS;
	    }

	    final long months = secondsLeft / MONTHS;
	    if (months > 0) {
		formatted.append(months
			+ (months == 1 ? " month " : " months "));
		secondsLeft -= months * MONTHS;
	    }

	    final long weeks = secondsLeft / WEEKS;
	    if (weeks > 0) {
		formatted.append(weeks
			+ (weeks == 1 ? " week " : " weeks "));
		secondsLeft -= weeks * WEEKS;
	    }

	    final long days = secondsLeft / DAYS;
	    if (days > 0) {
		formatted.append(days + (days == 1 ? " day " : " days "));
		secondsLeft -= days * DAYS;
	    }

	    final long hours = secondsLeft / HOURS;
	    if (hours > 0) {
		formatted.append(hours
			+ (hours == 1 ? " hour " : " hours "));
		secondsLeft -= hours * HOURS;
	    }

	    final long minutes = secondsLeft / MINUTES;
	    if (minutes > 0) {
		formatted.append(minutes
			+ (minutes == 1 ? " minute " : " minutes "));
		secondsLeft -= minutes * MINUTES;
	    }

	    if (secondsLeft > 0) {
		formatted.append(secondsLeft
			+ (secondsLeft == 1 ? " second" : " seconds"));
	    }

	    if (negative) {
		formatted.append(" ago!");
	    }

	    final String formattedValue = String.format(getFormat(),
		    formatted.toString());
	    queueAlteration(formattedValue);
	} catch (NumberFormatException nfe) {
	    queueAlteration(nfe.getLocalizedMessage());
	}

    }
}
