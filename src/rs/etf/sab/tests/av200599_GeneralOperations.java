/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import rs.etf.sab.operations.GeneralOperations;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.student.DB;
/**
 *
 * @author Byk
 */
public class av200599_GeneralOperations implements GeneralOperations
{
    private boolean debug = false;
    @Override
    public void eraseAll()
    {
        Connection connection = DB.getInstance().getConnection();
        if(debug) System.out.println("Usao u eraseAll");
        String deleteQuery = "delete from District;\n" +
        "delete from Admin;\n" +
        "delete from City;\n" +
        "delete from Courier;\n" +
        "delete from CourierRequest;\n" +
        "delete from Drive;\n" +
        "delete from Offer;\n" +
        "delete from Package;\n" +
        "delete from UserApp;\n" +
        "delete from Vehicle;";
        
        try (Statement statement = connection.createStatement();)
        {
            int ret = statement.executeUpdate(deleteQuery);
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
