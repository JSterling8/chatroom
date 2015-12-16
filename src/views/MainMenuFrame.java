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

/**
 * The main menu window, where topics are created, joined, or deleted
 * 
 * @author Jonathan Sterling
 *
 */
public class MainMenuFrame extends JFrame {
	public static final int COLUMN_INDEX_OF_TOPIC_ID = 3;
	public static final int COLUMN_INDEX_OF_TOPIC_OWNER_ID = 2;

	private static final long serialVersionUID = -1262155724457779827L;

	private MainMenuController controller;
	private DefaultTableModel topicsTableModel;
	private JButton btnDeleteTopic;
	private JButton btnJoinTopic;
	private JMSUser user;

	public MainMenuFrame(JMSUser user) {
		this.user = user;

		// Create a controller for this frame
		controller = new MainMenuController(this, user);

		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel);
		basePanel.setLayout(null);

		topicsTableModel = controller.generateTopicTableModel();
		JTable topicsTable = new JTable(topicsTableModel);
		// We need the topic ID and topic owner ID for functionality later, but
		// we don't want to show it to the userF
		topicsTable.removeColumn(topicsTable.getColumnModel().getColumn(COLUMN_INDEX_OF_TOPIC_ID));
		topicsTable.removeColumn(topicsTable.getColumnModel().getColumn(COLUMN_INDEX_OF_TOPIC_OWNER_ID));

		// Makes it so users can only select rows, not individual cells
		topicsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Makes cells non editable.
		for (int i = 0; i < topicsTable.getColumnCount(); i++) {
			Class<?> columnClass = topicsTable.getColumnClass(i);
			topicsTable.setDefaultEditor(columnClass, null);
		}

		// Add listener and selection handler
		topicsTable.getSelectionModel().addListSelectionListener(new TopicListSelectionHandler());
		topicsTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				JTable table = (JTable) mouseEvent.getSource();
				Point point = mouseEvent.getPoint();
				int selectedRow = table.rowAtPoint(point);

				// If the user double clicks a row, open the corresponding topic
				if (mouseEvent.getClickCount() == 2) {
					UUID topicId = (UUID) table.getModel().getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
					controller.handleJoinTopicPressed(topicId);
				}
			}
		});
		// Adds listener for the enter key being pressed
		createKeyBindings(topicsTable);

		// Create a scrollpane, put the topics table in it, and add it to the
		// base panel
		JScrollPane scrollPane = new JScrollPane(topicsTable);
		scrollPane.setBounds(0, 0, 630, 503);
		basePanel.add(scrollPane);

		// Create a panel for the menu buttons and add it to the base panel
		JPanel menuButtonsPanel = new JPanel();
		menuButtonsPanel.setBounds(640, 0, 192, 503);
		basePanel.add(menuButtonsPanel);

		// Create the Create, Join, Delete, Refresh, and Logout buttons
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
				int selectedRow = topicsTable.getSelectedRow();
				if (selectedRow == -1) {
					// If Join Topic is clicked but no topic is selected, show
					// an error
					JOptionPane.showInternalMessageDialog(MainMenuFrame.this,
							"Failed to join topic.  " + "No topic selected", "Join Failed", JOptionPane.ERROR_MESSAGE,
							null);
				} else {
					// If join topic is clicked with a valid topic highlighted,
					// ask the controller to handle the button press
					UUID topicIdToJoin = (UUID) topicsTableModel.getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
					controller.handleJoinTopicPressed(topicIdToJoin);
				}
			}
		});
		btnJoinTopic.setEnabled(false);

		btnDeleteTopic = new JButton("Delete Topic");
		btnDeleteTopic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int selectedRow = topicsTable.getSelectedRow();
				if (selectedRow == -1) {
					JOptionPane.showInternalMessageDialog(MainMenuFrame.this,
							"Failed to delete topic.  " + "No topic selected", "Topic Deletion Failed",
							JOptionPane.ERROR_MESSAGE, null);
				} else {
					UUID topicIdToDelete = (UUID) topicsTableModel.getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
					controller.handleDeleteTopicPressed(selectedRow, topicIdToDelete);
				}
			}
		});
		// Defaults to disabled because no topic is selected when the window is
		// initially created
		btnDeleteTopic.setEnabled(false);

		JButton btnLogout = new JButton("Logout");
		btnLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				controller.logout();
			}
		});

		// A button to manually refresh the topic list. This is a legacy item
		// from before listeners were implemented. It's here purely to give the
		// user a feeling of control over the topic list
		JButton btnRefreshList = new JButton("Refresh List");
		btnRefreshList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.updateTopicList(topicsTable);
			}
		});

		// Some monstrosity that WindowBuilder generated
		// Makes the right menu bar layout look pretty
		GroupLayout rightMenuPanelGroupLayout = new GroupLayout(menuButtonsPanel);
		rightMenuPanelGroupLayout
				.setHorizontalGroup(
						rightMenuPanelGroupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(rightMenuPanelGroupLayout.createSequentialGroup().addContainerGap()
										.addGroup(rightMenuPanelGroupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(btnDeleteTopic, GroupLayout.DEFAULT_SIZE, 168,
														Short.MAX_VALUE)
										.addComponent(btnLogout, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
										.addComponent(btnCreateTopic, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
										.addComponent(btnJoinTopic, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
										.addComponent(btnRefreshList, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
						.addContainerGap()));
		rightMenuPanelGroupLayout.setVerticalGroup(
				rightMenuPanelGroupLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING,
						rightMenuPanelGroupLayout.createSequentialGroup().addContainerGap().addComponent(btnRefreshList)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCreateTopic)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnJoinTopic)
								.addPreferredGap(ComponentPlacement.RELATED, 331, Short.MAX_VALUE)
								.addComponent(btnDeleteTopic).addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnLogout).addContainerGap()));
		menuButtonsPanel.setLayout(rightMenuPanelGroupLayout);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(850, 550));
		setTitle("Main Menu - Logged In As: " + user.getName());
		setVisible(true);
	}

	public MainMenuController getController() {
		return controller;
	}

	/**
	 * Overrides JFrame's add controllers custom disposing before closing the
	 * window
	 */
	@Override
	public void dispose() {
		controller.handleDispose();
	}

	/**
	 * Provides a means for external classes to call this classes parent's
	 * dispose method
	 */
	public void superDispose() {
		super.dispose();
	}

	/**
	 * Effectively adds a listener for the enter key for the topics table. If
	 * enter is pressed, the same functionality as clicking the join button is
	 * implemented
	 * 
	 * @param table
	 *            The table to add the enter-key listener to
	 */
	@SuppressWarnings("serial")
	private void createKeyBindings(JTable table) {
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		table.getActionMap().put("Enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				int selectedRow = table.getSelectedRow();
				UUID topicId = (UUID) table.getModel().getValueAt(selectedRow, COLUMN_INDEX_OF_TOPIC_ID);
				controller.handleJoinTopicPressed(topicId);
			}
		});
	}

	/**
	 * A custom selection handler for the topics list. When a row is
	 * highlighted, it checks to see if the user owns that topic, and enables
	 * the delete button if they do. If they don't own the topic, it ensures the
	 * delete button is disabled. It also enables the "Join Topic" button.
	 * 
	 * @author Jonathan Sterling
	 *
	 */
	private class TopicListSelectionHandler implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();

			if (!lsm.isSelectionEmpty()) {
				int selectedRowIndex = lsm.getMinSelectionIndex();
				btnJoinTopic.setEnabled(true);

				if (topicsTableModel.getValueAt(selectedRowIndex, COLUMN_INDEX_OF_TOPIC_OWNER_ID)
						.equals(user.getId())) {
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
