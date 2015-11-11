package views;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controllers.ChatroomController;

public class ChatroomFrame extends JFrame {
	private DefaultTableModel messagesTableModel;
	private DefaultTableModel usersTableModel;
	private ChatroomController controller;
	
	public ChatroomFrame() {
		controller = new ChatroomController(this);
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);
		
		
		messagesTableModel = controller.generateMessagesTableModel();

		JTable messagesTable = new JTable(messagesTableModel);
		messagesTable.removeColumn(messagesTable.getColumnModel().getColumn(3));
		messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Makes cells non editable.
		for(int i = 0; i <messagesTable.getColumnCount(); i++){
			Class<?> columnClass = messagesTable.getColumnClass(i);
			messagesTable.setDefaultEditor(columnClass, null);
		}
		messagesTable.getSelectionModel().addListSelectionListener(new SharedListSelectionHandler());

		JScrollPane spMessages = new JScrollPane(messagesTable);
		spMessages.setBounds(0, 0, 630, 503);
		panel.add(spMessages);
		
		
		usersTableModel = controller.generateUsersTableModel();

		JTable usersTable = new JTable(usersTableModel);
		usersTable.removeColumn(messagesTable.getColumnModel().getColumn(1));
		usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Makes cells non editable.
		for(int i = 0; i <messagesTable.getColumnCount(); i++){
			Class<?> columnClass = usersTable.getColumnClass(i);
			usersTable.setDefaultEditor(columnClass, null);
		}
		usersTable.getSelectionModel().addListSelectionListener(new SharedListSelectionHandler());
		
		JScrollPane spUsers = new JScrollPane(usersTable);
		spUsers.setBounds(640, 0, 192, 503);
		panel.add(spUsers);
		
		setSize(new Dimension(850, 550));
	}
	
    private class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) { 
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 
/*            if (!lsm.isSelectionEmpty()) {
                int selectedRowIndex = lsm.getMinSelectionIndex();
                
                if(tableModel.getValueAt(selectedRowIndex, COLUMN_INDEX_OF_TOPIC_OWNER_ID).equals(user.getId())){
                    btnDeleteTopic.setVisible(true);
                } else {
                	btnDeleteTopic.setVisible(false);
                }
            } else {
            	btnDeleteTopic.setVisible(false);
            }*/

        }
    }
}
