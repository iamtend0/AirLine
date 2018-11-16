import java.sql.*;
import java.util.StringTokenizer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Snooper extends JPanel implements ActionListener {
	JLabel index;
	JComboBox userTableList;
	JComboBox otherTableList;
	JComboBox otherTableSysList;
	String userTableNames[], otherTableNames[], otherTableSysNames[];
	String userTable = "";
	String otherTable = "";
	String otherTableSys = "";
	JLabel id;
	TextArea content;

	Connection link;
	String myURL = "jdbc:odbc:";
	java.sql.DatabaseMetaData patrol;

	/*	***********	*/
	/*	CONSTRUCTOR	*/
	/*	***********	*/

	public Snooper(String sourceDB) throws SQLException, ClassNotFoundException {
		super(new BorderLayout());
		connect(sourceDB);
		inspect(sourceDB);
	}

	/*	***************************	*/
	/*	CONNECTING TO THE DATA BASE	*/
	/*	***************************	*/

	public void connect(String source) throws ClassNotFoundException {
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			link = DriverManager.getConnection(myURL+source);
		} catch (SQLException e) {
			System.out.println("Connection error: " + e.getMessage());
		}
	}

	/*	***********************************	*/
	/*	INSPECTING THE DATA BASE STRUCTURE	*/
	/*	***********************************	*/

	public void inspect(String source) throws SQLException {
		patrol = link.getMetaData();
		JPanel northPanel = new JPanel(new BorderLayout());    
		JLabel titre_lien = new JLabel("Nom de la base de données : " ); 
	    JTextField champ_lien = new JTextField(10); 
		id = new JLabel("DATA BASE " + source + " (User: " + patrol.getUserName() + ")");
		northPanel.add(id,BorderLayout.NORTH);
		northPanel.add(new JLabel("TABLES : "),BorderLayout.WEST);

		JPanel southPanel = new JPanel(new BorderLayout());    
		southPanel.add(new JLabel("AUTRES TABLES : "),BorderLayout.WEST);
		
		JButton quit = new JButton("Quitter"); 
		quit.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		    	System.exit(0);
		    }
		});
		southPanel.add(quit, BorderLayout.SOUTH);
		

		System.out.println(userTable);

		ResultSet answer = patrol.getTables(null, null, null, null);
		while (answer.next()) {
			if (answer.wasNull() == false) {
				String tableName = answer.getString("TABLE_NAME");
				if (answer.getString("TABLE_TYPE").equals("SYSTEM TABLE")== true) {
					otherTableSys = otherTableSys + " " + tableName;
				} else {
					if (tableName.contains("~")) {
						otherTable = otherTable + " " + tableName;
					} else {
						userTable = userTable + " " + tableName;
					}
				}
			}
		}
		answer.close();

		StringTokenizer st2 = new StringTokenizer(otherTable," ");
		StringTokenizer st3 = new StringTokenizer(otherTableSys," ");
		StringTokenizer st = new StringTokenizer(userTable," ");
		otherTableNames = new String[st2.countTokens()];
		otherTableSysNames = new String[st3.countTokens()];
		userTableNames = new String[st.countTokens()];
		while (st2.hasMoreTokens()) {
			otherTableNames[st2.countTokens()-1] = st2.nextToken();
		}
		while (st3.hasMoreTokens()) {
			otherTableSysNames[st3.countTokens()-1] = st3.nextToken();
		}
		while (st.hasMoreTokens()) {
			userTableNames[st.countTokens()-1] = st.nextToken();
		}
		otherTableList = new JComboBox(otherTableNames);
		otherTableList.insertItemAt("Table",0);
		otherTableList.setSelectedIndex(0);
		otherTableList.addActionListener(this);
		otherTableSysList = new JComboBox(otherTableSysNames);
		otherTableSysList.insertItemAt("System Table",0);
		otherTableSysList.setSelectedIndex(0);
		otherTableSysList.addActionListener(this);
		userTableList = new JComboBox(userTableNames);
		userTableList.setSelectedIndex(0);
		userTableList.addActionListener(this);
		southPanel.add(otherTableList, BorderLayout.EAST);
		southPanel.add(otherTableSysList, BorderLayout.CENTER);
		northPanel.add(userTableList, BorderLayout.EAST);

		content = new TextArea();
		add(content, BorderLayout.CENTER);

		updateFields(otherTableNames[otherTableList.getSelectedIndex()]);
		updateFields(otherTableSysNames[otherTableSysList.getSelectedIndex()]);
		updateFields(userTableNames[userTableList.getSelectedIndex()]);

		add(northPanel, BorderLayout.NORTH);
		add(southPanel, BorderLayout.SOUTH);
	}

	/*	***************************	*/
	/*	LISTENING TO THE COMBO BOX	*/
	/*	***************************	*/

	public void actionPerformed(ActionEvent e) {
		JComboBox cb = (JComboBox)e.getSource();
		String tableName = (String)cb.getSelectedItem();
		updateFields(tableName);
	}

	/*	***********************	*/
	/*	UPDATING THE FIELD LIST	*/
	/*	***********************	*/

	protected void updateFields(String name) {
		content.setText("");	// empty the content first
		try {
			ResultSet answer = patrol.getTables(null, null, null, null);
    		ResultSet columns = patrol.getColumns(null, null, name, null); // table attributes 
    		
    		String column_name, type_name, column_size, nullable;
    		
    		while (answer.next()) {
                if (!answer.wasNull() && name.equalsIgnoreCase(answer.getString("TABLE_NAME"))) {
                		
                	content.append("CATEGORY = " + answer.getString("TABLE_CAT") + '\n');
                	content.append("TYPE = " + answer.getString("TABLE_TYPE") + '\n');
                	content.append("SCHEMA = " + answer.getString("TABLE_SCHEM") + '\n');
                	content.append("REMARKS = " + answer.getString("REMARKS") + "\n\n");

                	// Attributs de la table   
            		while (columns.next()) {
            			         		  		
            			column_name = columns.getString(4); // nom de colonne
            			type_name = columns.getString(6);   // type de donnée
            			column_size = columns.getString(7); // taille de donnée
            			nullable = columns.getString(18);   // nullable

            			if (nullable.equals("NO")) { // if NOT NULL
            				content.append(column_name + " " + type_name + "(" + column_size + ") NOT NULL\n" );
            			} else {
            				content.append(column_name + " " + type_name + "(" + column_size + ")\n" );
            			}
            			
                	}
                
                }
    		}
		}catch (SQLException e) {
			System.out.println("Meta data error");
		}
	}

	private static void createAndShowGUI(String source) throws SQLException, ClassNotFoundException {
		//Create and set up the window.
		JFrame frame = new JFrame("Snooper");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Create and set up the content pane.
		JComponent newContentPane = new Snooper(source);
		newContentPane.setOpaque(true);
		frame.setContentPane(newContentPane);
		//Display the window.
		frame.pack();
		//Make the frame half the height and width
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = screenSize.height;
		int width = screenSize.width;
		frame.setSize(width/2, height/2);

		// here's the part where i center the jframe on screen
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/*	***********	*/
	/*	LAUNCHING	*/
	/*	***********	*/

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		createAndShowGUI("AirLine");
	}
}
