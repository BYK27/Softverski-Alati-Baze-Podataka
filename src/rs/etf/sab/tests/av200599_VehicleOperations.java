/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.VehicleOperations;
import rs.etf.sab.student.DB;
/**
 *
 * @author Byk
 */
public class av200599_VehicleOperations implements VehicleOperations
{
    private boolean debug = false;
    
    //first argument: plate
    //second argument: fuel
    //third argument: consumption
    @Override
    public boolean insertVehicle(String string, int i, BigDecimal bd)
    {
        int ID = getVehicleByPlate(string);
        if(ID != -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String queryInsert = "insert into Vehicle (RegistracioniBroj, TipGoriva, Potrosnja) values (?, ?, ?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryInsert, PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            preparedStatement.setString(1, string);
            preparedStatement.setInt(2, i);
            preparedStatement.setBigDecimal(3, bd);
            
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            
            if(resultSet.next())
            {
                if(debug) System.out.println("Ubacio vozilo" + resultSet.getInt(1));
                return true;
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return false;
    }

    @Override
    public int deleteVehicles(String... strings)
    {
        if(debug)System.out.println("Usao u deleteVehicles list!");
        Connection connection = DB.getInstance().getConnection();
        String deleteVehicle = "delete from Vehicle where RegistracioniBroj = ?";
        int numberDeleted = 0;
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteVehicle);)
        {
           for(String plate : strings)
           {
               preparedStatement.setString(1, plate);
               numberDeleted += preparedStatement.executeUpdate();
           }
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numberDeleted;
    }

    @Override
    public List<String> getAllVehichles()
    {
        if(debug) System.out.println("Usao u getAllVehichles");
        Connection connection = DB.getInstance().getConnection();
        List<String> vehicleList = new ArrayList<>();
        String getVehicle = "Select RegistracioniBroj from Vehicle";
        try 
            (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getVehicle);
            )
        {
            while (resultSet.next())
            {                
                if(debug) System.out.println("Dohvatio vozilo" + resultSet.getString(1));
                vehicleList.add(resultSet.getString(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vehicleList;
    }

    @Override
    public boolean changeFuelType(String string, int i)
    {
        if(i < 0 || i > 2) return false;
        
        int ID = getVehicleByPlate(string);
        if(ID == -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String query = "update vehicle set TipGoriva = ? where RegistracioniBroj = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
           preparedStatement.setInt(1, i);
           preparedStatement.setString(2, string);
           preparedStatement.executeUpdate();
           return true;
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean changeConsumption(String string, BigDecimal bd)
    {
        if(bd.compareTo(BigDecimal.ZERO) < 0) return false;
        
        int ID = getVehicleByPlate(string);
        if(ID == -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String query = "update vehicle set Potrosnja = ? where RegistracioniBroj = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
           preparedStatement.setBigDecimal(1, bd);
           preparedStatement.setString(2, string);
           preparedStatement.executeUpdate();
           return true;
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private int getVehicleByPlate(String plate)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select idVehicle from Vehicle where RegistracioniBroj = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setString(1, plate);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Vozilo sa zadatim tablicama ne postoji!");
                return -1;
            }
            else return  resultSet.getInt(1);
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    private boolean plateUnique(String plate)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select idVehicle from Vehicle where RegistracioniBroj = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setString(1, plate);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Vozilo sa zadatim tablicama ne postoji!");
                return true;
            }
            else return false;
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
