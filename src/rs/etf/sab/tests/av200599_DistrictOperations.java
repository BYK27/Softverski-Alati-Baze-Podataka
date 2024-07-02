/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import rs.etf.sab.operations.DistrictOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.student.DB;
/**
 *
 * @author Byk
 */
public class av200599_DistrictOperations implements DistrictOperations
{
    private boolean debug = false;
    @Override
    //first argument - Name
    //second argument - cityId
    //third argument - X
    //fourth argument - Y
    public int insertDistrict(String string, int i, int i1, int i2)
    {
        if(debug)System.out.println("Usao u insertDistrict!");
        Connection connection = DB.getInstance().getConnection();
        String insertDistrict = "insert into District (Naziv, idCity, X_Kordinata, Y_Kordinata) values (?, ?, ?, ?)";
        String findDistrict = "select idDistrict from District where Naziv = ?";
        String findCity = "select idCity from City where idCity = ?";
        
        try (PreparedStatement preparedStatementFindCity = connection.prepareStatement(findCity);)
        {
            preparedStatementFindCity.setInt(1, i);
            ResultSet resultSetFindCity = preparedStatementFindCity.executeQuery();
            if(!resultSetFindCity.next())
            {
                if(debug)System.out.println("Ne postoji grad sa zadatim ID!");
                return -1;
            }
            
            try(PreparedStatement preparedStatementFindDistrict = connection.prepareStatement(findDistrict);)
            {
                preparedStatementFindDistrict.setString(1, string);
                ResultSet resultSetFindDistrict = preparedStatementFindDistrict.executeQuery();
                if(resultSetFindDistrict.next())
                {
                    if(debug)System.out.println("Postoji distrikt sa zadatim postanskim brojem!");
                    return -1;
                }
                
                try(PreparedStatement preparedStatementInsertDistrict = connection.prepareStatement(insertDistrict, PreparedStatement.RETURN_GENERATED_KEYS);)
                {
                    preparedStatementInsertDistrict.setString(1, string);
                    preparedStatementInsertDistrict.setInt(2, i);
                    preparedStatementInsertDistrict.setInt(3, i1);
                    preparedStatementInsertDistrict.setInt(4, i2);
                    preparedStatementInsertDistrict.executeUpdate();
                    ResultSet resultSet = preparedStatementInsertDistrict.getGeneratedKeys();

                    if(resultSet.next())
                    {
                        if(debug)System.out.println("Dodao opstinu");
                        return resultSet.getInt(1);
                    }
                }
                catch (SQLException ex)
                {
                    Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            

        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return -1;
    }

    @Override
    public int deleteDistricts(String... strings)
    {
        if(debug)System.out.println("Usao u deleteDistrict ID!");
        Connection connection = DB.getInstance().getConnection();
        String deleteDistrict = "delete from District where Naziv = ?";
        int numberDeleted = 0;
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteDistrict);)
        {
           for(String naziv : strings)
           {
               preparedStatement.setString(1, naziv);
               numberDeleted += preparedStatement.executeUpdate();
           }
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numberDeleted;
    }

    //first argument - districtId
    @Override
    public boolean deleteDistrict(int i)
    {
        if(debug)System.out.println("Usao u deleteDistrict ID!");
        Connection connection = DB.getInstance().getConnection();
        String deleteDistrict = "delete from District where idDistrict = ?";
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteDistrict);)
        {
           preparedStatement.setInt(1, i);
           return preparedStatement.executeUpdate() > 0;
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int deleteAllDistrictsFromCity(String string)
    {
        Connection connection = DB.getInstance().getConnection();
        String findCity = "select idCity from City where Naziv = ?";
        String deleteDistrict = "delete from District where idCity = ?";
        
        int numberDeleted = 0;
        
        try (PreparedStatement preparedStatementFindCity = connection.prepareStatement(findCity);)
        {
            preparedStatementFindCity.setString(1, string);
            ResultSet resultSet = preparedStatementFindCity.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug)System.out.println("Ne postoji grad sa datim nazivom!");
                return numberDeleted;
            }
            
            int cityID = resultSet.getInt(1);
            
            try(PreparedStatement preparedStatementDeleteDistrict = connection.prepareStatement(deleteDistrict))
            {
                preparedStatementDeleteDistrict.setInt(1, cityID);
                numberDeleted = preparedStatementDeleteDistrict.executeUpdate();
                return numberDeleted;
            }
            catch (SQLException ex)
            {
                Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public List<Integer> getAllDistrictsFromCity(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        String getAllDistrictsFromCity = "select idDistrict from District where idCity = ?";
        List<Integer> districtList = new ArrayList<>();
        
        try(PreparedStatement preparedStatement = connection.prepareStatement(getAllDistrictsFromCity);)
        {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next())
            {
                if(debug)System.out.println("Dohvatio opstinu " + resultSet.getInt(1));
                districtList.add(resultSet.getInt(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return districtList;
    }

    @Override
    public List<Integer> getAllDistricts()
    {
        if(debug)System.out.println("Usao u getAllDistricts!");
        Connection connection = DB.getInstance().getConnection();
        List<Integer> districtList = new ArrayList<>();
        String getAllDistricts = "select idDistrict from District";
        
        try(
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getAllDistricts);
            )
        {
           while(resultSet.next())
           {
               if(debug)System.out.println("Dohvatio opstinu " + resultSet.getInt(1));
               districtList.add(resultSet.getInt(1));
           }
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return districtList;
    }
    
}
