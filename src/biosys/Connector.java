/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package biosys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Ejiga Samuel
 */
public class Connector {
    private static Connection con = null;
    public static Connection connectDB() {
        
        try {
            
        // register MYSQL Driver
            
            Class.forName("com.mysql.jdbc.Driver");
            
            // establishing connection to my sql database
            
        con = DriverManager.getConnection
        ("jdbc:mysql://localhost:3806/biotest","root",null);
        
 // root refers to your workbench username while mysql refers to your workbench password
        JOptionPane.showMessageDialog(null, "connection successfully established");//Note Remove pane for smooth transition in production mode
        return con;
        }
        catch(ClassNotFoundException | SQLException ex){
            JOptionPane.showMessageDialog(null, ex);
            return null;
        }
   
    
    }
     public static void main(String[] args) {
    connectDB();
    
    }
     
     
}
