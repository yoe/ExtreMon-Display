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

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ResponderCommentDialog {
    public static String requestResponderComment(String label,
	    String existingComment) {
	final JLabel commentLabel = new JLabel("Comment for " + label
		+ ":");
	final JTextField commentField = new JTextField(32);

	if (existingComment != null)
	    commentField.setText(existingComment);

	if (JOptionPane.showConfirmDialog(null, new Object[] {
		commentLabel, commentField, }, "Responder Comment",
		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) 
		!= JOptionPane.OK_OPTION) {
	    return existingComment;
	}

	return commentField.getText();
    }
}
