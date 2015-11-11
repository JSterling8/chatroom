package views;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controllers.MainMenuController;
import models.JMSUser;

public class MainMenuFrame extends JFrame {
	private static final int COLUMN_INDEX_OF_TOPIC_ID = 4;

	private static final int COLUMN_INDEX_OF_TOPIC_OWNER_ID = 3;

	private static final long serialVersionUID = -1262155724457779827L;

	private MainMenuController controller;
	private DefaultTableModel tableModel;
	private JButton btnDeleteTopic;
	private JMSUser user;
	
	public MainMenuFrame(JMSUser user) {
		this.user = user;
		controller = new MainMenuController(this, user);
		
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);

		tableModel = controller.generateTableModelWithTestData();

		JTable table = new JTable(tableModel);
		table.removeColumn(table.getColumnModel().getColumn(COLUMN_INDEX_OF_TOPIC_OWNER_ID));
		table.removeColumn(table.getColumnModel().getColumn(COLUMN_INDEX_OF_TOPIC_ID - 1));  // -1 is because index 4 becomes index 3 after the column in the line above is removed
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Makes cells non editable.
		for(int i = 0; i <table.getColumnCount(); i++){
			Class<?> columnClass = table.getColumnClass(i);
			table.setDefaultEditor(columnClass, null);
		}
		table.getSelectionModel().addListSelectionListener(new SharedListSelectionHandler());

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(0, 0, 630, 503);
		panel.add(scrollPane);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(640, 0, 192, 503);
		panel.add(panel_1);

		JButton btnCreateTopic = new JButton("Create Topic");
		btnCreateTopic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				controller.handleCreateButtonPressed();
			}
		});

		JButton btnJoinTopic = new JButton("Join Topic");

		btnDeleteTopic = new JButton("Delete Topic");
		btnDeleteTopic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int selectedRow = table.getSelectedRow();
				if(selectedRow == -1){
					//TODO Throw error dialogue saying nothing is selected to delete
				} else {
					UUID topicIdToDelete = (UUID) tableModel.getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
					controller.deleteTopic(selectedRow, topicIdToDelete);
				}
			}
		});
		btnDeleteTopic.setVisible(false);
		
		JButton btnLogout = new JButton("Logout");
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1
				.setHorizontalGroup(
						gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_1.createSequentialGroup().addContainerGap()
										.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
												.addComponent(btnCreateTopic, GroupLayout.DEFAULT_SIZE, 172,
														Short.MAX_VALUE)
										.addComponent(btnJoinTopic, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
										.addComponent(btnDeleteTopic, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
										.addComponent(btnLogout, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
						.addContainerGap()));
		gl_panel_1.setVerticalGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup().addContainerGap().addComponent(btnCreateTopic)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnJoinTopic)
						.addPreferredGap(ComponentPlacement.RELATED, 350, Short.MAX_VALUE).addComponent(btnDeleteTopic)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnLogout).addContainerGap()));
		panel_1.setLayout(gl_panel_1);

		setSize(new Dimension(850, 550));
		setVisible(true);
	}

	public MainMenuController getController() {
		return controller;
	}
	
    private class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) { 
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 
            if (!lsm.isSelectionEmpty()) {
                int selectedRowIndex = lsm.getMinSelectionIndex();
                
                if(tableModel.getValueAt(selectedRowIndex, COLUMN_INDEX_OF_TOPIC_OWNER_ID).equals(user.getId())){
                    btnDeleteTopic.setVisible(true);
                } else {
                	btnDeleteTopic.setVisible(false);
                }
            } else {
            	btnDeleteTopic.setVisible(false);
            }

        }
    }
}
