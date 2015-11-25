package views;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controllers.MainMenuController;
import models.JMSUser;
import java.awt.event.ActionListener;

public class MainMenuFrame extends JFrame {
	public static final int COLUMN_INDEX_OF_TOPIC_ID = 4;
	public static final int COLUMN_INDEX_OF_TOPIC_OWNER_ID = 3;
	
	private static final long serialVersionUID = -1262155724457779827L;
	
	private MainMenuController controller;
	private DefaultTableModel tableModel;
	private JButton btnDeleteTopic;
	private JButton btnJoinTopic;
	private JMSUser user;
	
	public MainMenuFrame(JMSUser user) {
		this.user = user;
		controller = new MainMenuController(this, user);
		
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);

		tableModel = controller.generateTopicTableModel();
		JTable table = new JTable(tableModel);
		controller.updateTopicList(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Makes cells non editable.
		for(int i = 0; i <table.getColumnCount(); i++){
			Class<?> columnClass = table.getColumnClass(i);
			table.setDefaultEditor(columnClass, null);
		}
		table.getSelectionModel().addListSelectionListener(new TopicListSelectionHandler());
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent){
				JTable table = (JTable) mouseEvent.getSource();
				Point point = mouseEvent.getPoint();
				int selectedRow = table.rowAtPoint(point);
				if(mouseEvent.getClickCount() == 2){
					UUID topicId = (UUID) table.getModel().getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
					controller.handleJoinTopicPressed(topicId);
				}
			}
		});
		createKeyBindings(table);

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

		btnJoinTopic = new JButton("Join Topic");
		btnJoinTopic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int selectedRow = table.getSelectedRow();
				if(selectedRow == -1){
					JOptionPane.showInternalMessageDialog(MainMenuFrame.this, "Failed to join topic.  " + 
							"No topic selected", "Join Failed", JOptionPane.ERROR_MESSAGE, null);
				} else {
					UUID topicIdToJoin = (UUID) tableModel.getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
					controller.handleJoinTopicPressed(topicIdToJoin);
				}
			}
		});
		btnJoinTopic.setEnabled(false);

		btnDeleteTopic = new JButton("Delete Topic");
		btnDeleteTopic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int selectedRow = table.getSelectedRow();
				if(selectedRow == -1){
					JOptionPane.showInternalMessageDialog(MainMenuFrame.this, "Failed to delete topic.  " + 
													"No topic selected", "Topic Deletion Failed", JOptionPane.ERROR_MESSAGE, null);
				} else {
					UUID topicIdToDelete = (UUID) tableModel.getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
					controller.handleDeleteTopicPressed(selectedRow, topicIdToDelete);
				}
			}
		});
		btnDeleteTopic.setEnabled(false);
		
		JButton btnLogout = new JButton("Logout");
		btnLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				controller.logout();
			}
		});
		
		JButton btnRefreshList = new JButton("Refresh List");
		btnRefreshList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.updateTopicList(table);
			}
		});
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(btnDeleteTopic, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
						.addComponent(btnLogout, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
						.addComponent(btnCreateTopic, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
						.addComponent(btnJoinTopic, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
						.addComponent(btnRefreshList, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnRefreshList)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnCreateTopic)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnJoinTopic)
					.addPreferredGap(ComponentPlacement.RELATED, 331, Short.MAX_VALUE)
					.addComponent(btnDeleteTopic)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLogout)
					.addContainerGap())
		);
		panel_1.setLayout(gl_panel_1);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(new Dimension(850, 550));
		setVisible(true);
	}

	public MainMenuController getController() {
		return controller;
	}
	
	private void createKeyBindings(JTable table) {
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		    table.getActionMap().put("Enter", new AbstractAction() {
		        @Override
		        public void actionPerformed(ActionEvent ae) {
		            int selectedRow = table.getSelectedRow();
		            UUID topicId = (UUID) table.getModel().getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
		            controller.handleJoinTopicPressed(topicId);
		        }
		    });
		}
	
    private class TopicListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) { 
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 
            if (!lsm.isSelectionEmpty()) {
                int selectedRowIndex = lsm.getMinSelectionIndex();
                btnJoinTopic.setEnabled(true);
                
                if(tableModel.getValueAt(selectedRowIndex, COLUMN_INDEX_OF_TOPIC_OWNER_ID).equals(user.getId())){
                    btnDeleteTopic.setEnabled(true);
                } else {
                	btnDeleteTopic.setEnabled(false);
                }
            } else {
            	btnDeleteTopic.setEnabled(false);
            	btnJoinTopic.setEnabled(false);
            }

		}
	}
}
