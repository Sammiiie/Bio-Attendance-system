/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package biosys;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Ejiga Samuel
 */
public class DBUpdater {
    
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet result = null;
    
    
    //RETRIEVE DATA
    public DefaultTableModel getData() {
        //ADD COLUMNS TO TABLE MODEL
        DefaultTableModel dm = new DefaultTableModel();
        dm.addColumn("idStaff");
        dm.addColumn("name");
        dm.addColumn("type");
        dm.addColumn("phone");
        dm.addColumn("bio");
        

        //SQL STATEMENT
        String sql = "SELECT * FROM staff";

        try {
            con = Connector.connectDB();

            //PREPARED STMT
            pst = con.prepareStatement(sql);
            result = pst.executeQuery(sql);

            //LOOP THRU GETTING ALL VALUES
            while (result.next()) {
                //GET VALUES
                String id = result.getString(1);
                String name = result.getString(2);
                String type = result.getString(3);
                String phone = result.getString(4);
                

                dm.addRow(new String[]{id, name,type,phone});
            }

            return dm;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }
    
    //DELETE DATA
    public Boolean delete(String id)
    {
        //SQL STMT
        String sql="DELETE FROM staff  WHERE idstaff ='"+id+"'";
        String gh ="DELETE FROM staff  WHERE idstaff ='"+id+"'";

        try
        {
            //GET COONECTION
            con = Connector.connectDB();

            //STATEMENT
            pst =con.prepareStatement(sql);
            pst =con.prepareStatement(gh);

            //EXECUTE
            pst.execute(sql);
            pst.execute(gh);

            return true;

        }catch(SQLException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
    
    
}
