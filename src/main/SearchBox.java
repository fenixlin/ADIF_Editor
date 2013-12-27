package main;

import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SearchBox extends JDialog implements PropertyChangeListener{

	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JOptionPane optionPane;
	private String btnString1 = "Find";
    private String btnString2 = "Cancel";
    private String result;
    
    public String getResult() {return result;}

	public SearchBox(Frame frame)
	{
		//set modality to true to block other windows
		
		//refer to http://docs.oracle.com/javase/tutorial/uiswing/examples/components/DialogDemoProject/src/components/DialogDemo.java
		//http://docs.oracle.com/javase/tutorial/uiswing/examples/components/DialogDemoProject/src/components/CustomDialog.java
		super(frame, "Search", true);
		textField = new JTextField(30);
		String msg = "Find:";
		
		Object[] array = {msg, textField};
		Object[] options = {btnString1, btnString2};
		optionPane = new JOptionPane(array,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                options,
                options[0]);
		setContentPane(optionPane);
		
		addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });
		
	    //Register an event handler that puts the text into the option pane.
        //textField.addActionListener(this);
        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //Register an event handler that reacts to option pane state changes.
        //optionPane.addPropertyChangeListener(this);
        
		//Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);        
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /*change the JOptionPane's value property. */
                    optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });
        
        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
	}

	/* This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
 
        if (isVisible()
         && (e.getSource() == optionPane)
         && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();
 
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }
 
            //Reset the JOptionPane's value to guarantee property change event will be fired when pressing the same button next time.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);          
            
            if (value.equals("OK")) {            	
                result = textField.getText();             
            }
            textField.setText(null);
            setVisible(false);
        }        
    }		

}
