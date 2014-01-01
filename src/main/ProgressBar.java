package main;

import java.awt.Color;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressBar extends JDialog{

	private static final long serialVersionUID = 1L;

	public ProgressBar(JFrame parent, String message)
	{
		JPanel mainPane = new JPanel(null);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		JLabel status = new JLabel(message);
		progressBar.setBounds(10, 10, 370, 15);
        status.setBounds(10, 25, 350, 25);
        status.setForeground(Color.white);
        mainPane.setBackground(Color.darkGray);
		mainPane.add(progressBar);
        mainPane.add(status);
        
        this.setContentPane(mainPane);
		this.setUndecorated(true);//除去title		
        this.setSize(390, 50);
        this.setLocationRelativeTo(parent); //设置此窗口相对于指定组件的位置
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // 不允许关闭
	}
	
}
