package views;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.FlowLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JList;
import java.awt.GridLayout;

public class MainMenuFrame extends JFrame {
	public MainMenuFrame() {
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);
		
		Object[] columns = {"ID", "Topic", "Owners", "Users"};
		Object[][] data = {
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"3", "Topi3", "Owner3", "33"},
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
							{"1", "Topic1", "Owner1", "4"}, 
							{"2", "Topic2", "Owner2", "6"}, 
						  };
		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		JTable table = new JTable(tableModel);
		((DefaultTableModel)table.getModel()).addRow(new Object[]{"3", "Testing", "Test", "5"});
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(0, 0, 630, 503);
		panel.add(scrollPane);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(640, 0, 192, 503);
		panel.add(panel_1);
		
		
		JButton btnCreateTopic = new JButton("Create Topic");
		
		JButton btnJoinTopic = new JButton("Join Topic");
		
		JButton btnDeleteTopic = new JButton("Delete Topic");
		
		JButton btnLogout = new JButton("Logout");
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(btnCreateTopic, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
						.addComponent(btnJoinTopic, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
						.addComponent(btnDeleteTopic, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
						.addComponent(btnLogout, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnCreateTopic)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnJoinTopic)
					.addPreferredGap(ComponentPlacement.RELATED, 350, Short.MAX_VALUE)
					.addComponent(btnDeleteTopic)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLogout)
					.addContainerGap())
		);
		panel_1.setLayout(gl_panel_1);
		
		setSize(new Dimension(850, 550));
		setVisible(true);
	}
	
	public static void main(String[] args) throws InterruptedException{		
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                new MainMenuFrame();
            }
        });
	}
}
