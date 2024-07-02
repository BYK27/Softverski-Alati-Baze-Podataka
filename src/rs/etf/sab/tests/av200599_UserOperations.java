/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import java.util.List;
import rs.etf.sab.operations.UserOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.student.DB;
/**
 *
 * @author Byk
 */
public class av200599_UserOperations implements UserOperations
{
    private boolean debug = false;
    @Override
    //first argument - username
    //second argument - firstname
    //third argument - lastname
    //fourth argument - password
    public boolean insertUser(String string, String string1, String string2, String string3)
    {
        if(debug) System.out.println("Usao u insertUser");
        if(!checkValidity(string, string1, string2, string3)) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String insertUser = "insert into UserApp (KorisnickoIme, Ime, Prezime, Sifra, BrojPoslatihPaketa) values (?,?,?,?,?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            preparedStatement.setString(1, string);
            preparedStatement.setString(2, string1);
            preparedStatement.setString(3, string2);
            preparedStatement.setString(4, string3);
            preparedStatement.setInt(5, 0);
            
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            
            if(resultSet.next())
            {
                if(debug) System.out.println("Ubacio korisnika " + string);
                return true;
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    //first argument - username
    @Override
    public int declareAdmin(String string)
    {
        if(!postojiKorisnik(string)) return 2;
        if(vecAdmin(string)) return 1;
        
        Connection connection = DB.getInstance().getConnection();
        String queryInsert = "insert into Admin (idUser) values (?)";
        
        int ID = getIdFromUsername(string);
        if(ID == -1) return 2;
        
        try(PreparedStatement preparedStatement = connection.prepareStatement(queryInsert);)
        {
            preparedStatement.setInt(1, ID);
            preparedStatement.executeUpdate();
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 0;
    }

    @Override
    public Integer getSentPackages(String... strings)
    {
        Connection connection = DB.getInstance().getConnection();
        Integer sentPackages = 0;
        int lastSent = -1;
        String query = "select count(idPackage) from Package where idUser = ? and StatusIsporuke > 1";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            for(String username : strings)
            {
                int ID = getIdFromUsername(username);
                if(ID == -1) continue;

                preparedStatement.setInt(1, ID);
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.next()) 
                {
                    lastSent = resultSet.getInt(1);
                    sentPackages += lastSent;
                }
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(lastSent == -1) return null;
        return sentPackages;
    }

    @Override
    public int deleteUsers(String... strings)
    {
        if(debug) System.out.println("Usao u deleteUsers sa nazivom");
        Connection connection = DB.getInstance().getConnection();
        String deleteUser = "delete from UserApp where KorisnickoIme = ?";
        int numberDeleted = 0;
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteUser);)
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

    @Override
    public List<String> getAllUsers()
    {
        if(debug) System.out.println("Usao u getAllUsers");
        Connection connection = DB.getInstance().getConnection();
        List<String> userList = new ArrayList<>();
        String getUser = "Select KorisnickoIme from UserApp";
        try 
            (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getUser);
            )
        {
            while (resultSet.next())
            {                
                if(debug) System.out.println("Dohvatio korisnika" + resultSet.getString(1));
                userList.add(resultSet.getString(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userList;
    }
    
    private boolean checkValidity(String username, String firstname, String lastname, String password)
    {
        if(firstname == null || firstname.isEmpty() || !Character.isUpperCase(firstname.charAt(0))) return false;
        if(lastname == null || lastname.isEmpty() || !Character.isUpperCase(lastname.charAt(0))) return false;
        if(password == null || password.isEmpty() || password.length() < 8) return false;
        
        boolean containsLetter = false;
        boolean containsDigit = false;
        
        for(char c : password.toCharArray())
        {
            if(Character.isLetter(c)) containsLetter = true;
            else if(Character.isDigit(c)) containsDigit = true;
            
            if(containsDigit && containsLetter) return true;
        }
        
        Connection connection = DB.getInstance().getConnection();
        String query = "select idUser from UserApp where KorisnickoIme = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next())
            {
                if(debug) System.out.println("Vec postoji korisnik sa tim imenom.");
                return false;
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        return true;
    }

    private boolean postojiKorisnik(String username)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idUser from UserApp where KorisnickoIme = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next())
            {
                if(debug) System.out.println("Vec postoji korisnik sa tim imenom.");
                return true;
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private boolean vecAdmin(String username)
    {
        Connection connection = DB.getInstance().getConnection();
        String queryCheck = "select idUser from Admin where idUser = ?";
        String queryID = "select idUser from UserApp where KorisnickoIme = ?"; 
        
        try (PreparedStatement preparedStatementID = connection.prepareStatement(queryID);)
        {
            preparedStatementID.setString(1, username);
            ResultSet resultSet = preparedStatementID.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Korisnik sa zadatim korisnickim imenom ne postoji!");
                return true;
            }
            
            int ID = resultSet.getInt(1);
            
            
            try(PreparedStatement preparedStatementCheck = connection.prepareStatement(queryCheck);)
            {
                preparedStatementCheck.setInt(1, ID);
                resultSet = preparedStatementCheck.executeQuery();
                
                if(resultSet.next())
                {
                    if(debug) System.out.println("Korisnik je vec admin!");
                    return true;
                }

            }catch (SQLException ex)
            {
                Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
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
}
