/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import java.util.List;
import rs.etf.sab.operations.CityOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.student.DB;


/**
 *
 * @author Byk
 */
public class av200599_OperationsStudent implements CityOperations{

    private boolean debug = false;

    @Override
    //first argument - City name
    //second argument - Postal code
    public int insertCity(String string, String string1) 
    {
        if(debug) System.out.println("Usao u insertCity");
        String findCity = "select idCity from City where PostanskiBroj = ? OR Naziv = ?";
        String insertCity = "insert into City (Naziv, PostanskiBroj) values (?, ?)";
        Connection connection = DB.getInstance().getConnection();
        try(PreparedStatement preparedStatementFindCity = connection.prepareStatement(findCity);) 
        {
            preparedStatementFindCity.setString(1, string1);
            preparedStatementFindCity.setString(2, string);
            ResultSet resultSetFindCity = preparedStatementFindCity.executeQuery();
            if(resultSetFindCity.next())
            {
                if(debug)System.out.println("Postoji grad sa zadatim postanskim brojem!");
                return -1;
            }
            
            try(PreparedStatement preparedStatementInsertCity = connection.prepareStatement(insertCity, PreparedStatement.RETURN_GENERATED_KEYS);)
            {
                preparedStatementInsertCity.setString(1, string);
                preparedStatementInsertCity.setString(2, string1);
                preparedStatementInsertCity.executeUpdate();
                ResultSet resultSetInsertCity = preparedStatementInsertCity.getGeneratedKeys();
                if(resultSetInsertCity.next()) 
                {
                    if(debug) System.out.println("Ubacio grad " + resultSetInsertCity.getInt(1));
                    return resultSetInsertCity.getInt(1);
                }
            }
            catch (SQLException ex) {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
            }      
        } catch (SQLException ex) {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }            
        return -1;

    }

    //first argument - list od city names 
    @Override
    public int deleteCity(String... strings) 
    {
        if(debug) System.out.println("Usao u deleteCity sa nazivom");
        Connection connection = DB.getInstance().getConnection();
        String deleteCity = "delete from City where Naziv = ?";
        int numberDeleted = 0;
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteCity);)
        {
            for(String naziv : strings)
            {
                preparedStatement.setString(1, naziv);
                if(preparedStatement.executeUpdate() > 0) numberDeleted += 1;
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return numberDeleted;
    }

    //first argument - idCity 
    @Override
    public boolean deleteCity(int i) 
    {
        if(debug) System.out.println("Usao u deleteCity sa ID");
        Connection connection = DB.getInstance().getConnection();
        String findCity = "select idCity from City where idCity = ?";
        String deleteCity = "delete from City where idCity = ?";
        try (PreparedStatement preparedStatementFindCity = connection.prepareStatement(findCity);)
        {
            
            preparedStatementFindCity.setInt(1, i);
            ResultSet resultSet = preparedStatementFindCity.executeQuery();
            
            if(!resultSet.next()) return false;
            
            if(debug) System.out.println("City not found " + i);
            
            try(PreparedStatement preparedStatementDeleteCity = connection.prepareStatement(deleteCity);)
            {
                preparedStatementDeleteCity.setInt(1, i);
                if(debug) System.out.println("City deleted: " + i);
                return preparedStatementDeleteCity.executeUpdate() > 0;
            }
            catch (SQLException ex)
            {
                Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<Integer> getAllCities() 
    {
        if(debug) System.out.println("Usao u getAllCities");
        Connection connection = DB.getInstance().getConnection();
        List<Integer> cityList = new ArrayList<>();
        String getCities = "Select IdCity from City";
        try 
            (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getCities);
            )
        {
            while (resultSet.next())
            {                
                if(debug) System.out.println("Dohvatio grad" + resultSet.getInt(1));
                cityList.add(resultSet.getInt(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cityList;

    }
    
}
