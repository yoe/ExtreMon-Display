package be.apsu.extremon.panel;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ResponderCommentDialog
{
	public static String requestResponderComment(String label, String existingComment)
	{
	    final JLabel 		commentLabel 		= new JLabel("Comment for " + label + ":");
	    final JTextField 	commentField 		= new JTextField(32);

	    if(existingComment!=null)
	    	commentField.setText(existingComment);
	    
	    if(JOptionPane.showConfirmDialog(null, 
			new Object[]
			{
				commentLabel,
				commentField,
			},
			"Responder Comment",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)
		    {
		    	return existingComment;
		    }

		return commentField.getText();
	}	
}
