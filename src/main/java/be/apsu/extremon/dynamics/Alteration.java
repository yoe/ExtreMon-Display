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
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.svg.SVGStylable;

public class Alteration {
    private static final int MAGIC_PRIME = 31;
    private Element on;
    private String attribute;
    private String value;
    private boolean isStyleElement;

    public Alteration(Element on, String attribute, String value) {
	super();
	this.on = on;
	this.attribute = attribute;
	this.isStyleElement = false;

	if (attribute != null && value != null
		&& attribute.equals("width")
		&& (value.equals("0.0") || value.startsWith("-")))
	    this.value = "0.00001";
	else
	    this.value = value;
    }

    public Alteration(Element on, String attribute, String subAttribute,
	    String value) {
	super();
	this.on = on;
	this.attribute = subAttribute;
	this.value = value;
	this.isStyleElement = true;
    }

    public Alteration(Element on, String value) {
	super();
	this.on = on;
	this.attribute = null;
	this.value = value;
	this.isStyleElement = false;
    }

    public final void alter() {
	if (this.attribute == null) {
	    this.on.setTextContent(this.value);
	} else {
	    if (this.isStyleElement) {
		CSSStyleDeclaration style = ((SVGStylable) this.on)
			.getStyle();
		style.setProperty(this.attribute, this.value, "");
	    } else {
		this.on.setAttribute(this.attribute, this.value);
	    }
	}
    }

    // our identity in collections depends on the element:attribute tuple we act
    // upon
    // so we get replaced by the last instance acting on the same element

    @Override
    public final boolean equals(Object thatObject) {
	if (this == thatObject)
	    return true;
	if (!(thatObject instanceof Alteration))
	    return false;
	final Alteration that = (Alteration) thatObject;
	return (this.on.equals(that.on))
		&& ((this.attribute == null) ? that.attribute == null
			: this.attribute.equals(that.attribute));
    }

    @Override
    public final int hashCode() {
	int hash = 1;
	hash = hash * MAGIC_PRIME + this.on.hashCode();
	hash = hash
		* MAGIC_PRIME
		+ (this.attribute == null ? 0 : this.attribute.hashCode());
	return hash;
    }

    @Override
    public final String toString() {
	return this.on.getAttribute("id") + ":"
		+ (this.attribute != null ? this.attribute : "cdata")
		+ " ALTER TO " + this.value;
    }
}
