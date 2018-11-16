import java.sql.*;
import java.util.StringTokenizer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Snooper extends JPanel implements ActionListener {
	JLabel index;
	JComboBox tableList;
	String tableNames[], tableNamesAsOne;
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
        id = new JLabel("DATA BASE " + source + " (User: " + patrol.getUserName() + ")");
        add(id,BorderLayout.NORTH);
        add(new JLabel("TABLES: "),BorderLayout.WEST);
        ResultSet answer = patrol.getTables(null, null, null, null);
        while (answer.next()) {
                if (answer.wasNull() == false) {
                	tableNamesAsOne = tableNamesAsOne + answer.getString("TABLE_NAME") + " ";
                }
        }
        answer.close();
        StringTokenizer st = new StringTokenizer(tableNamesAsOne," ");
        tableNames = new String[st.countTokens()];
    	while (st.hasMoreTokens()) {
    		tableNames[st.countTokens()-1] = st.nextToken();
    	}
    	tableList = new JComboBox(tableNames);
        tableList.setSelectedIndex(0);
        tableList.addActionListener(this);
    	add(tableList, BorderLayout.EAST);
    	content = new TextArea();
    	add(content, BorderLayout.SOUTH);
    	updateFields(tableNames[tableList.getSelectedIndex()]);
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
    		ResultSetMetaData rsmd = columns.getMetaData();
    		int columnsNumber = rsmd.getColumnCount();
    		
    		String column_name, type_name, column_size;
    		
    		while (answer.next()) {
                if (!answer.wasNull() && name.equalsIgnoreCase(answer.getString("TABLE_NAME"))) {
                		
                	content.append("CATEGORY = " + answer.getString("TABLE_CAT") + '\n');
                	content.append("TYPE = " + answer.getString("TABLE_TYPE") + '\n');
                	content.append("SCHEMA = " + answer.getString("TABLE_SCHEM") + '\n');
                	content.append("REMARKS = " + answer.getString("REMARKS") + "\n\n");

            		while (columns.next()) {
            			// Attributs de la table            		
            		    for (int i = 0; i <= columnsNumber; i+=16) {
            		    	//if(name.equalsIgnoreCase(rsmd.getTableName(i))) {
            		    		
            		    		column_name = columns.getString(i+4);
                    			type_name = columns.getString(i+6); 
                    			column_size = columns.getString(i+7);
//
                    			content.append(column_name + " " + type_name + "(" + column_size + ") \n" );
//                		        String columnValue = columns.getString(i);
//                 		        content.append(columnValue + " " + rsmd.getColumnName(i) + "\n");
            		    	//}     
            		    }

                	}
                
                }
    		}
    	} catch (SQLException e) {
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
    	frame.setVisible(true);
    }
    
	/*	***********	*/
	/*	LAUNCHING	*/
	/*	***********	*/
    
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
		createAndShowGUI("AirLine");
    }
}
