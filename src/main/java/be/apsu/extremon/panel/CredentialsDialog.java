package be.apsu.extremon.panel;

import java.net.PasswordAuthentication;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class CredentialsDialog
{
	private CredentialsDialog()
	{
		super();
	}
	
	public static PasswordAuthentication requestUserNameAndPassword()
	{
	    final JLabel loginLabel 			= new JLabel("ExtreMon Login:");
	    final JTextField loginField 		= new JTextField(15);
	    final JLabel passwordLabel 			= new JLabel("ExtreMon Password:");
	    final JPasswordField passwordField 	= new JPasswordField();
	    
	    
	    
	    if(JOptionPane.showConfirmDialog(null, 
			new Object[]
			{
				loginLabel,
				loginField,
				passwordLabel,
				passwordField,
			},
			"ExtreMon Credentials",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)
		    {
		    	return null;
		    }

		return new PasswordAuthentication(loginField.getText(), passwordField.getPassword());
	}	
}


