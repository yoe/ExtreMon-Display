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

package be.apsu.extremon.panel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.batik.swing.svg.SVGUserAgentAdapter;

public class X3UserAgent extends SVGUserAgentAdapter {
    private Logger logger;

    public X3UserAgent() {
	super();
	this.logger = Logger.getLogger(X3UserAgent.class.getName());
    }

    public final void setLogger(Logger logger) {
	this.logger = logger;
    }

    @Override
    public final void displayError(Exception ex) {
	this.logger.log(Level.SEVERE, "Batik Error", ex);
    }

    @Override
    public final void displayError(String message) {
	this.logger.log(Level.SEVERE, message);
    }

    @Override
    public final void displayMessage(String message) {
	this.logger.log(Level.INFO, message);
    }
}
