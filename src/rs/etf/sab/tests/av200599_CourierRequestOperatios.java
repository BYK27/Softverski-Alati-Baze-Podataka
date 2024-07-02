/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CourierRequestOperation;
import rs.etf.sab.student.DB;

/**
 *
 * @author Byk
 */
public class av200599_CourierRequestOperatios implements CourierRequestOperation
{
    
    private boolean debug = false;

    //first argument - username
    //second argument - licence plate
    @Override
    public boolean insertCourierRequest(String string, String string1)
    {
        if(debug) System.out.println("Usao u insertCourierRequest");
        int IDuser = getIdFromUsername(string);
        int IDVehicle = getVehicleByPlate(string1);
        
        if(IDuser == -1 || IDVehicle == -1) return false;
        
        
        if(voziloVecVozi(IDuser)) return false;
        
        //kurir vec ima zahtev
        if(getCourierFromRequest(IDuser) != -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String query = "insert into CourierRequest (idUser, idVehicle) values (?, ?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            preparedStatement.setInt(1, IDuser);
            preparedStatement.setInt(2, IDVehicle);
            preparedStatement.executeUpdate();
            
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            
            if(resultSet.next())
            {
                if(debug) System.out.println("Ubacio zahtev za korisnika " + string + " za vozilo " + string1);
                return true;
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_CourierRequestOperatios.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    //first argument - username
    @Override
    public boolean deleteCourierRequest(String string)
    {
        int ID = getIdFromUsername(string);
        if(ID == -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String query = "delete from CourierRequest where idUser = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_CourierRequestOperatios.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        return false;
    }

    //first argument - username
    //second argument - licence plate
    @Override
    public boolean changeVehicleInCourierRequest(String string, String string1)
    {
        int IDuser = getIdFromUsername(string);
        int IDVehicle = getVehicleByPlate(string1);
        
        if(IDuser == -1 || IDVehicle == -1) return false;
        if(voziloVecVozi(IDuser)) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String query = "update CourierRequest set idVehicle = ? where idUser = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, IDVehicle);
            preparedStatement.setInt(2, IDuser);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_CourierRequestOperatios.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public List<String> getAllCourierRequests()
    {
        if(debug) System.out.println("Usao u getAllCourierRequests");
        Connection connection = DB.getInstance().getConnection();
        List<String> courierList = new ArrayList<>();
        String getUser = "Select idUser from CourierRequest";
        try 
            (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getUser);
            )
        {
            while (resultSet.next())
            {                
                if(debug) System.out.println("Dohvatio kurira " + resultSet.getInt(1));
                courierList.add(getUsernameFromId(resultSet.getInt(1)));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return courierList;
    }

    //first argument - username
    @Override
    public boolean grantRequest(String string)
    {
        int IDuser = getIdFromUsername(string);
        if(IDuser == -1) return false;
        
        //kurir nema zahtev
        if(getCourierFromRequest(IDuser) == -1) return false;
        
        int IDvehicle = getVehicleByCourierInRequest(IDuser);
        if(IDvehicle == -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String query = "insert into Courier (idUser, idVehicle, BrojIsporucenihPaketa, OstvarenProfit, Status) values (?, ?, ?, ?, ?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, IDuser);
            preparedStatement.setInt(2, IDvehicle);
            preparedStatement.setInt(3, 0);
            preparedStatement.setBigDecimal(4, BigDecimal.ZERO);
            preparedStatement.setInt(5, 0);

            return preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_CourierRequestOperatios.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private int getIdFromUsername(String username)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select idUser from UserApp where KorisnickoIme = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setString(1, username);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Korisnik sa zadatim korisnickim imenom ne postoji!");
                return -1;
            }
            else return  resultSet.getInt(1);
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
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

    //first argument - courier ID
    private int getCourierFromRequest(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select idUser from CourierRequest where idUser = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setInt(1, ID);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Korisnik nije dodao nijedan zahtev!");
                return -1;
            }
            else return  resultSet.getInt(1);
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    } 
      
    //first argument - courier ID
    private int getVehicleByCourierInRequest(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select idVehicle from CourierRequest where idUser = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setInt(1, ID);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Korisnik nije dodao nijedan zahtev!");
                return -1;
            }
            else return  resultSet.getInt(1);
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    } 
    
    
    //first argument - vehicle ID
    private boolean voziloVecVozi(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select idUser from Courier where idVehicle = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setInt(1, ID);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Niko ne vozi to vozilo!");
                return false;
            }
            else return true;
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private String getUsernameFromId(int id)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select KorisnickoIme from UserApp where idUser = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setInt(1, id);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Korisnik sa zadatim ID ne postoji!");
                return "";
            }
            else return  resultSet.getString(1);
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }  

}
