/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import java.math.BigDecimal;
import java.util.List;
import rs.etf.sab.operations.CourierOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.student.DB;

/**
 *
 * @author Byk
 */
public class av200599_CourierOperations implements CourierOperations
{
    private boolean debug = false;

    //first argument - courierUserName
    //second argument - licencePlateNumber
    @Override
    public boolean insertCourier(String string, String string1)
    {
        //neko vozi auto ili auto ne postoji
        if(postojiKurirRegistracioniBroj(string1)) return false;
        int IDuser = getIdFromUsername(string);
        if(IDuser == -1) return false;
        int IDvehicle = getVehicleByPlate(string1);
        if(IDvehicle == -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String query = "insert into Courier (idUser, idVehicle, BrojIsporucenihPaketa, OstvarenProfit, Status) values (?, ?, ?, ?, ?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            preparedStatement.setInt(1, IDuser);
            preparedStatement.setInt(2, IDuser);
            preparedStatement.setInt(3, 0);
            preparedStatement.setBigDecimal(4, BigDecimal.ZERO);
            preparedStatement.setInt(5, 0);
            preparedStatement.executeUpdate(query);
            
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next())
            {
                if(debug) System.out.println("Ubacio kurira " + resultSet.getInt(0));
                return true;
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean deleteCourier(String string)
    {
        if(debug)System.out.println("Usao u deleteCourier ID!");
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "delete from Courier where idUser = ?";
        
        int ID = getIdFromUsername(string);
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, ID);
           return preparedStatement.executeUpdate() > 0;
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<String> getCouriersWithStatus(int i)
    {
        List<String> courierList = new ArrayList<>();
        if(i < 0 || i > 1) return courierList;
        Connection connection = DB.getInstance().getConnection();
        String query = "select idUser from Courier where Status = ?";
        
        try(PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next())
            {
                int ID = resultSet.getInt(1);
                courierList.add(getUsernameFromId(ID));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return courierList;
    }

    @Override
    public List<String> getAllCouriers()
    {
        if(debug) System.out.println("Usao u getAllCouriers");
        Connection connection = DB.getInstance().getConnection();
        List<String> courierList = new ArrayList<>();
        String getUser = "select idUser from Courier";
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

    //first argument - number of delivieres
    @Override
    public BigDecimal getAverageCourierProfit(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        BigDecimal bigDecimal = BigDecimal.ZERO;
        
        if(i == -1)
        {
            String query = "select avg(OstvarenProfit) from Courier";
            try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);)
            {
                if(resultSet.next())
                {
                    bigDecimal = resultSet.getBigDecimal(1);
                }
            } catch (SQLException ex)
            {
                Logger.getLogger(av200599_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }else
        {
            String query = "select avg(OstvarenProfit) from Courier where BrojIsporucenihPaketa >= ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
            {
                preparedStatement.setInt(1, i);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if(resultSet.next())
                {
                    bigDecimal = resultSet.getBigDecimal(1);
                }

            } catch (SQLException ex)
            {
                Logger.getLogger(av200599_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return  bigDecimal;
    }
    
    
    private boolean postojiKurirRegistracioniBroj(String regBroj)
    {
        Connection connection = DB.getInstance().getConnection();
        String voziloQuery = "select idVehicle from Vehicle where RegistracioniBroj = ?";
        String kurirQuery = "select idUser from Courier where idVehicle = ?";
        
        try(PreparedStatement preparedStatementVozilo = connection.prepareStatement(voziloQuery);)
        {
            preparedStatementVozilo.setString(1, regBroj);
            ResultSet resultSet = preparedStatementVozilo.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Registracioni broj ne postoji");
                return true;
            }
            
            int voziloID = resultSet.getInt(1);
            
            try(PreparedStatement preparedStatementKurir = connection.prepareStatement(kurirQuery);)
            {
                preparedStatementKurir.setInt(1, voziloID);
                resultSet = preparedStatementKurir.executeQuery();
                if(resultSet.next())
                {
                    if(debug) System.out.println("Vozilo vec vozi neki kurir");
                    return true;
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(av200599_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return false;
    }

    private int getIdFromUsernameCourier(String username)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryID = "select idUser from UserApp where KorisnickoIme = ?"; 
        String queryCourier = "select idUser from Courier where idUser = ?";
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setString(1, username);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Korisnik sa zadatim korisnickim imenom ne postoji!");
                return -1;
            }
            
            int ID = resultSet.getInt(1);
            
            try(PreparedStatement preparedStatementCourier = connection.prepareStatement(queryCourier);)
            {
                preparedStatementCourier.setInt(1, ID);
                resultSet = preparedStatementCourier.executeQuery();
                
                if(resultSet.next())
                {
                    if(debug) System.out.println("Korisnik je kurir!");
                    return ID;
                }
                else return -1;
                
            }
            catch (SQLException ex)
            {
                Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
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

    private String getUsernameFromId(int id)
    {
        if(debug) System.out.println("Usao u getUsernameFromId");
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
