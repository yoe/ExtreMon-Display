/*   ExtreMon Project                                                      
 *   Copyright (C) 2012 Frank Marien                                  
 *   frank@apsu.be                                                         
 *                                                                         
 *   This file is part of ExtreMon.                                        
 *                                                                         
 *   ExtreMon is free software: you can redistribute it and/or modify      
 *   it under the terms of the GNU General Public License as published by  
 *   the Free Software Foundation, either version 3 of the License, or     
 *   (at your option) any later version.                                   
 *                                                                         
 *   ExtreMon is distributed in the hope that it will be useful,           
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         
 *   GNU General Public License for more details.                          
 *                                                                         
 *   You should have received a copy of the GNU General Public License     
 *   along with ExtreMon.  If not, see <http://www.gnu.org/licenses/>.     
 */

package be.apsu.extremon.panel;

import java.net.PasswordAuthentication;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class CredentialsDialog {
    private CredentialsDialog() {
	super();
    }

    public static PasswordAuthentication requestUserNameAndPassword() {
	final JLabel loginLabel = new JLabel("ExtreMon Login:");
	final JTextField loginField = new JTextField(15);
	final JLabel passwordLabel = new JLabel("ExtreMon Password:");
	final JPasswordField passwordField = new JPasswordField();

	if (JOptionPane.showConfirmDialog(null, new Object[] { loginLabel,
		loginField, passwordLabel, passwordField, },
		"ExtreMon Credentials", JOptionPane.OK_CANCEL_OPTION,
		JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
	    return null;
	}

	return new PasswordAuthentication(loginField.getText(),
		passwordField.getPassword());
    }
}
