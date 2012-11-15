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

import org.w3c.dom.Element;

import be.apsu.extremon.STATE;

public class Respondable {
    private String label;
    private Element targetElement;
    private int state;
    private boolean responding;
    private String comment;
    private String responderName;
    private Element responderNameElement;

    public Respondable(Element targetElement, String label) {
	this.targetElement = targetElement;
	this.label = label;
	this.responding = false;
	this.state = STATE.MISSING.getCode();
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public int getState() {
	return state;
    }

    public int getDisplayState() {
	if (responding)
	    return this.state | STATE.RESPONDING_BITMASK;
	else
	    return this.state;
    }

    public String getDisplayText() {
	StringBuilder builder = new StringBuilder();

	if (getResponderName() != null && !getResponderName().isEmpty())
	    builder.append(trigramFromName(getResponderName()));

	if (getComment() != null && !getComment().isEmpty()) {
	    builder.append(' ');
	    builder.append(getComment());
	}

	return builder.toString();
    }

    public void setState(int state) {
	this.state = state;
    }

    public String getComment() {
	return comment;
    }

    public void setComment(String comment) {
	this.comment = comment;
    }

    public void setResponding(boolean responding) {
	this.responding = responding;
    }

    public boolean isResponding() {
	return responding;
    }

    public String getResponderName() {
	return responderName;
    }

    public void setResponderName(String responderName) {
	this.responderName = responderName;
    }

    public Element getTargetElement() {
	return targetElement;
    }

    public void setTargetElement(Element targetElement) {
	this.targetElement = targetElement;
    }

    public Element getResponderNameElement() {
	return responderNameElement;
    }

    public void setResponderNameElement(Element responderNameElement) {
	this.responderNameElement = responderNameElement;
    }

    private final String trigramFromName(final String _name) {
	String[] nameParts = _name.toUpperCase().split(" ");
	StringBuilder builder = new StringBuilder();

	switch (nameParts.length) {
	case 1:
	    builder.append(nameParts[0].substring(0, 2));
	    break;

	case 2:
	    builder.append(nameParts[0].substring(0, 1));
	    builder.append(nameParts[1].substring(0, 2));
	    break;

	default:
	    for (int i = 0; i < nameParts.length; i++) {
		builder.append(nameParts[i].substring(0, 1));
	    }
	    break;
	}

	return builder.toString();
    }
}
