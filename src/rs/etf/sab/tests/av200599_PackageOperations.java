/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.PackageOperations;
import rs.etf.sab.student.DB;
/**
 *
 * @author Byk
 */
public class av200599_PackageOperations implements PackageOperations
{
    
    private boolean debug = false;
    private List<BigDecimal> pocetnaCenaList = new ArrayList<>(Arrays.asList(new BigDecimal(10), new BigDecimal(25), new BigDecimal(75)));
    private List<BigDecimal> tezinskiFaktorList = new ArrayList<>(Arrays.asList(new BigDecimal(0), new BigDecimal(1), new BigDecimal(2)));
    private List<BigDecimal> cenaPoKilogramuList = new ArrayList<>(Arrays.asList(new BigDecimal(0), new BigDecimal(100), new BigDecimal(300)));
    private List<BigDecimal> gorivoList = new ArrayList<>(Arrays.asList(new BigDecimal(15), new BigDecimal(32), new BigDecimal(36)));

    //first argument districtFrom
    //second argument districtTo
    //third argument userName
    //fourth argument packageType
    //fift argument weight
    
    @Override
    public int insertPackage(int i, int i1, String string, int i2, BigDecimal bd)
    {
        if(i2 < 0 || i2 > 2) return -1;
        int IDuser = getIdFromUsername(string);
        
        Connection connection = DB.getInstance().getConnection();
        String query = "insert into Package (Tip, OpstinaDostavlja, OpstinaPreuzima, TezinaPaketa, StatusIsporuke, idUser, Cena, VremePrihvatanja, VremeStvaranja) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTimestamp = sdf.format(new java.util.Date());
        
        BigDecimal distance = this.euclideanDistance(i, i1);
        BigDecimal cena = this.getPackagePrice(i2, bd.doubleValue(), distance, 1);
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            preparedStatement.setInt(1, i2);
            preparedStatement.setInt(2, i);
            preparedStatement.setInt(3, i1);
            preparedStatement.setBigDecimal(4, bd);
            preparedStatement.setInt(5, 0);
            preparedStatement.setInt(6, IDuser);
            
            preparedStatement.setBigDecimal(7, cena); 
            
            preparedStatement.setString(8, null); 
            preparedStatement.setString(9, currentTimestamp); 
            
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            
            if(resultSet.next())
            {
                if(debug) System.out.println("Ubacio paket sa ID = " + resultSet.getInt(1));
                return resultSet.getInt(1);
            }

            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        return -1;
    }

    
    //first argument userName
    //second argument packageId
    //third argument percent
    @Override
    public int insertTransportOffer(String string, int i, BigDecimal bd)
    {
        int IDuser = this.getIdFromUsername(string);
        if(IDuser == -1) return -1;
        
        //kurir vec vozi
        if(this.getCourierStatus(string) == 1)return -1;
        
        if(!this.packageExists(i)) return -1;
        
        
        Connection connection = DB.getInstance().getConnection();
        String query = "insert into Offer (idPackage, idUser, ProcenatCeneIsporuke, StatusPonude) values (?, ?, ?, 0)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            preparedStatement.setInt(1, i);
            preparedStatement.setInt(2, IDuser);
            preparedStatement.setBigDecimal(3, bd);
            preparedStatement.executeUpdate();
            
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            
            if(resultSet.next())
            {
                if(debug) System.out.println("Ubacio ponudu sa ID = " + resultSet.getInt(1));
                return resultSet.getInt(1);
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return -1;
    }

    //first argument offerId
    @Override
    public boolean acceptAnOffer(int i)
    {
        int IDpackage = this.getPackageFromOffer(i);
        if(IDpackage == -1) return false;
        
        BigDecimal popust = this.getPopust(i);
        
        this.updateStatusPonude(i, 1);
        if(!this.setPackageStatusAndTimeNow(i, 1, popust)) return false;                   //package accepted
        List<Integer> offersDeleteList = this.getNotAcceptedOffersForPackage(IDpackage, i);
        
        //manuelno brisanje
//        for(int IDoffer : offersDeleteList)
//        {
//            if(!this.deleteOffer(IDoffer)) return false;
//        }
        
        return true;
    }

    @Override
    public List<Integer> getAllOffers()
    {
        if(debug) System.out.println("Usao u getAllOffers");
        Connection connection = DB.getInstance().getConnection();
        List<Integer> offerListt = new ArrayList<>();
        String getUser = "Select idOffer from Offer";
        try 
            (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getUser);
            )
        {
            while (resultSet.next())
            {                
                if(debug) System.out.println("Dohvatio ponudu " + resultSet.getInt(1));
                offerListt.add(resultSet.getInt(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_OperationsStudent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return offerListt;
    }

    @Override
    public List<Pair<Integer, BigDecimal>> getAllOffersForPackage(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idOffer,ProcenatCeneIsporuke from Offer where idPackage = ?";
        List<Pair<Integer, BigDecimal>> offerList = new ArrayList<>();
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next())
            {
                offerList.add(new av200599_Pair(resultSet.getInt(1), resultSet.getBigDecimal(2)));
            }
           
            return offerList;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return offerList;
    }

    @Override
    public boolean deletePackage(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "delete from Package where idPackage = ?";
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, i);
            return  preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeWeight(int i, BigDecimal bd)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "update Package set TezinaPaketa = ? where idPackage = ?";
     
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setBigDecimal(1, bd);
            preparedStatement.setInt(2, i);
            return  preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    //first argument id package
    //second argument type
    @Override
    public boolean changeType(int i, int i1)
    {
        if(i1 < 0 || i1 > 2) return false;
        Connection connection = DB.getInstance().getConnection();
        String query = "update Package set Tip = ? where idPackage = ?";
     
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, i1);
            preparedStatement.setInt(2, i);
            return  preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    //first argument id package
    @Override
    public Integer getDeliveryStatus(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select StatusIsporuke from Package where idPackage = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getInt(1);
            else return null;
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getPriceOfDelivery(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select Cena from Package where idPackage = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getBigDecimal(1);
            else return null;
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public Date getAcceptanceTime(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select VremePrihvatanja from Package where idPackage = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getDate(1);
            else return null;
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public List<Integer> getAllPackagesWithSpecificType(int i)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idPackage from Package where Tip = ?";
        List<Integer> packageList = new ArrayList<>();
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                packageList.add(resultSet.getInt(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packageList;
    }

    @Override
    public List<Integer> getAllPackages()
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idPackage from Package";
        List<Integer> packageList = new ArrayList<>();
        
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
            )
        {
            while(resultSet.next())
            {
                packageList.add(resultSet.getInt(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packageList;
    }

    @Override
    public List<Integer> getDrive(String string)
    {
        
       List<Integer> packageList = new ArrayList<>();
       int ID = this.getIdFromUsername(string);
       if(ID == -1) return packageList;
       
       Connection connection = DB.getInstance().getConnection();
       String query = "select idPackage from Package where idUser = ? and StatusIsporuke = 2";
       
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next())
            {
                packageList.add(resultSet.getInt(1));
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
       return packageList;
    }

    @Override
    public int driveNextPackage(String string)
    {
        int ID = this.getIdFromUsername(string);
        if(ID == -1) return -2;
        
        //courier isnt driving
        if(this.getCourierStatus(string) == 0) 
        {
            getPackagesForCourierToDrive(string);
            int IDpack = this.getFirsPackage(ID);
            int IDdistrict = geDistrictPreuzimaFromPackage(IDpack);
            this.insertDrive(ID, IDdistrict);
        }
        
        
        //nothing to drive
        if(this.getDrive(string).isEmpty()) return -1;

        String query = "select top 1 idPackage from Package where idUser = ? and StatusIsporuke = 2 order by VremePrihvatanja asc"; 
        
        Connection connection = DB.getInstance().getConnection();
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(!resultSet.next()) return -1;
            
            int IDpackage = resultSet.getInt(1);
            this.setPackageStatus(IDpackage, 3);
            
            
            int IDdrive = this.getDriveByCourier(ID);
            
            int IDfrom = this.geDistrictPreuzimaFromPackage(IDpackage);
            int IDto = this.geDistrictDostavljaFromPackage(IDpackage);

           
            
            BigDecimal distanca = this.euclideanDistance(IDfrom, IDto);
            
            IDfrom = this.getDriveCurrentDistrict(IDdrive);
            IDto = this.geDistrictPreuzimaFromPackage(IDpackage);
            setDriveCurrentDistrict(IDdrive, IDto);
            
            distanca =  this.euclideanDistance(IDfrom, IDto).add(distanca);
            
            
            BigDecimal cenaPaketa = this.getPriceFromPackage(IDpackage);
            
            
            this.updateDrivingDistance(IDdrive, distanca, cenaPaketa);
            
            //last package
            if(this.getCourierStatus(string) == 1 && !hasMoreToDrive(ID))
            {
                
                this.setCourierStatus(string, 0);
                BigDecimal totalDistane = this.getDrivingDistance(IDdrive);
                
                int IDvehicle = this.getVehicleFromCourier(ID);
                int tipGoriva = this.getTipGorivaFromVehicle(IDvehicle);
                BigDecimal potrosnja = this.getPotrosnjaVehicle(IDvehicle);
                
                BigDecimal profitVoznje = this.getProfitVoznje(IDdrive);
                BigDecimal totalProfit = this.calculateProfit(totalDistane, profitVoznje, tipGoriva, potrosnja);
                this.updateCourierEndDrive(ID, totalProfit);
                this.deleteDrive(IDdrive);
            }
            
            return IDpackage;
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }
    
    
    private boolean getPackagesForCourierToDrive(String string)
    {
        int ID = this.getIdFromUsername(string);
        
        Connection connection = DB.getInstance().getConnection();
        String query = "update Package set StatusIsporuke = 2 where idUser = ? and StatusIsporuke = 1";
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, ID);
            
            this.setCourierStatus(string, 1);
            
            
            return preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private int getFirsPackage(int IDuser)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select top 1 idPackage from Package where idUser = ? and StatusIsporuke = 2 order by VremePrihvatanja asc"; 
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, IDuser);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getInt(1);
        }catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }
    
    
    private int findDistrict(String naziv)
    {
        Connection connection = DB.getInstance().getConnection();
        String getAllDistrictsFromCity = "select idDistrict from District where Naziv = ?";
        
        try(PreparedStatement preparedStatement = connection.prepareStatement(getAllDistrictsFromCity);)
        {
            preparedStatement.setString(1, naziv);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next())
            {
                if(debug)System.out.println("Dohvatio opstinu " + naziv);
                return resultSet.getInt(1);
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
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
    
    private BigDecimal euclideanDistance(int x1, int y1, int x2, int y2) 
    {
       BigDecimal distance = BigDecimal.valueOf(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
       return distance;
    }
    
    private BigDecimal euclideanDistance(int idOd, int idDo) 
    {
       int x1, x2, y1, y2;
       BigDecimal distance = BigDecimal.ZERO;
       
       Connection connection = DB.getInstance().getConnection();
       String query = "select X_Kordinata, Y_Kordinata from District where idDistrict = ?";
       
        try (PreparedStatement preparedStatementOd = connection.prepareStatement(query);)
        {
            preparedStatementOd.setInt(1, idOd);
            ResultSet resultSet = preparedStatementOd.executeQuery();
            
            if(!resultSet.next())
            {
                if(debug) System.out.println("Ne postoji opstina sa ID " + idOd);
                return BigDecimal.ZERO;
            }
            
            x1 = resultSet.getInt(1);
            y1 = resultSet.getInt(2);
            
            try (PreparedStatement preparedStatementDo = connection.prepareStatement(query);)
            {
                preparedStatementDo.setInt(1, idDo);
                resultSet = preparedStatementDo.executeQuery();
                if(!resultSet.next())
                {
                    if(debug) System.out.println("Ne postoji opstina sa ID " + idOd);
                    return BigDecimal.ZERO;
                }

                x2 = resultSet.getInt(1);
                y2 = resultSet.getInt(2);
                
                distance = this.euclideanDistance(x1, y1, x2, y2);
                return distance;
                
            }  
            catch (SQLException ex)
            {
                Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return distance;
    }
    
    private BigDecimal getPackagePrice(int tip, double tezina, BigDecimal euklidskaDistanca, double procenat) 
    {
        double decimalniProcenat = procenat / 100;
        decimalniProcenat += 1;
        
        
        BigDecimal ukupnaTezina = this.tezinskiFaktorList.get(tip).multiply(BigDecimal.valueOf(tezina));
        BigDecimal ukupnaTezinaPoKg = ukupnaTezina.multiply(this.cenaPoKilogramuList.get(tip));
        BigDecimal saOsnovnomCenom = ukupnaTezinaPoKg.add(this.pocetnaCenaList.get(tip));
        BigDecimal ret = saOsnovnomCenom.multiply(euklidskaDistanca);
        
        BigDecimal retSaPopustom = ret.multiply(BigDecimal.valueOf(decimalniProcenat));
        
        return ret;
   }
    
    private boolean packageExists(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idPackage from Package";
        
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
            )
        {
            if(resultSet.next()) return true;
            else return false;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private int getPackageFromOffer(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idPackage from Offer where idOffer = ?";
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) return resultSet.getInt(1);
            else return -1;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    private boolean setPackageStatus(int ID, int status)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "update Package set StatusIsporuke = ? where idPackage = ?";
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, status);
            preparedStatement.setInt(2, ID);
            return  preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private boolean setPackageStatusAndTimeNow(int ID, int status, BigDecimal popust)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "update Package set StatusIsporuke = ?, VremePrihvatanja = CURRENT_TIMESTAMP, Cena = Cena * ? where idPackage = ?";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTimestamp = sdf.format(new java.util.Date());
        
        popust = popust.divide(BigDecimal.valueOf(100));
        popust = popust.add(BigDecimal.ONE);
        
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, status);
            //preparedStatement.setString(2, currentTimestamp);
            preparedStatement.setBigDecimal(2, popust);
            preparedStatement.setInt(3, ID);
            return  preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private List<Integer> getNotAcceptedOffersForPackage(int idPackage, int idOffer)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idOffer from Offer where idPackage = ? and idOffer <> ?";
        List<Integer> offerList = new ArrayList<>();
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, idPackage);
            preparedStatement.setInt(2, idOffer);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next())
            {
                offerList.add(resultSet.getInt(1));
            }
           
            return offerList;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return offerList;
    }

    private boolean deleteOffer(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "delete from Offer where idOffer = ?";
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, ID);
            return  preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private BigDecimal getPopust(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select ProcenatCeneIsporuke from Offer where idOffer = ?";
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getBigDecimal(1);
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO;
    }

    private boolean setCourierStatus (String string, int status)
    {
        int ID = this.getIdFromUsername(string);
        if(ID == -1) return false;
        
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "update Courier set Status = ? where idUser = ?";
        
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, status);
           preparedStatement.setInt(2, ID);
           return preparedStatement.executeUpdate() > 0;
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private int getCourierStatus (String string)
    {
        int ID = this.getIdFromUsername(string);
        if(ID == -1) return -1;
        
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "select Status from Courier where idUser = ?";
        
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, ID);
           ResultSet resultSet = preparedStatement.executeQuery();
           
           if(resultSet.next()) return  resultSet.getInt(1);
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private int insertDrive(int ID, int idDistrict)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "insert into Drive (idUser, Distanca, BrojPaketa, ProfitVoznje, idDistrict) values (?, ?, ?, ?, ?)";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            preparedStatement.setInt(1, ID);
            preparedStatement.setInt(2, 0);
            preparedStatement.setInt(3, 0);
            preparedStatement.setBigDecimal(4, BigDecimal.ZERO);
            preparedStatement.setInt(5, idDistrict);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next()) return resultSet.getInt(1);
            else return -1;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    private boolean deleteDrive(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "delete from Drive where idDrive = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            return preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private int getDriveByCourier(int ID)
    {
        
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "select idDrive from Drive where idUser = ?";
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, ID);
           ResultSet resultSet = preparedStatement.executeQuery();
           
           if(resultSet.next()) return  resultSet.getInt(1);
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    private boolean updateDrivingDistance(int ID, BigDecimal distance, BigDecimal profitVoznje)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "update Drive set Distanca = Distanca + ?, BrojPaketa = BrojPaketa + 1, ProfitVoznje = ProfitVoznje + ?  where idDrive = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setBigDecimal(1, distance);
            preparedStatement.setBigDecimal(2, profitVoznje);
            preparedStatement.setInt(3, ID);
            
            return preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private BigDecimal getDrivingDistance(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select Distanca from Drive where idDrive = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getBigDecimal(1);
            else return BigDecimal.ZERO;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return BigDecimal.ZERO;
    }
    
    private int getDriveCurrentDistrict(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idDistrict from Drive where idDrive = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getInt(1);
            else return -1;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }
    
    private boolean setDriveCurrentDistrict(int ID, int district)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "update Drive set idDistrict = ? where idDrive = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, district);
            preparedStatement.setInt(2, ID);
            return preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private int getBrojPaketaDrive(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select BrojPaketa from Drive where idDrive = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getInt(1);
            else return -1;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }
    
     private BigDecimal getProfitVoznje(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select ProfitVoznje from Drive where idDrive = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) return resultSet.getBigDecimal(1);
            else return BigDecimal.ZERO;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return BigDecimal.ZERO;
    }

    private int geDistrictDostavljaFromPackage(int ID)
    {
        
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "select OpstinaDostavlja from Package where idPackage = ?";
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, ID);
           ResultSet resultSet = preparedStatement.executeQuery();
           
           if(resultSet.next()) return  resultSet.getInt(1);
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    private int geDistrictPreuzimaFromPackage(int ID)
    {
        
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "select OpstinaPreuzima from Package where idPackage = ?";
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, ID);
           ResultSet resultSet = preparedStatement.executeQuery();
           
           if(resultSet.next()) return  resultSet.getInt(1);
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private boolean hasMoreToDrive(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idPackage from Package where idUser = ? and StatusIsporuke = 2";
        
        try(PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) return true;
            else return false;
            
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private boolean updateCourierEndDrive(int ID, BigDecimal profit)
    {
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "update Courier set OstvarenProfit = OstvarenProfit + ? where idUser = ?";
        
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setBigDecimal(1, profit);
           preparedStatement.setInt(2, ID);
           return preparedStatement.executeUpdate() > 0;
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private BigDecimal calculateProfit(BigDecimal distanca, BigDecimal profitVoznje, int tipGoriva, BigDecimal potrosnja)
    {
        return profitVoznje.subtract(distanca.multiply(this.gorivoList.get(tipGoriva)).multiply(potrosnja));
    }
    
    private int getVehicleFromCourier(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "select idVehicle from Courier where idUser = ?";
        
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, ID);
           ResultSet resultSet = preparedStatement.executeQuery();
           
           if(resultSet.next()) return  resultSet.getInt(1);
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    private int getTipGorivaFromVehicle(int IDVehicle)
    {
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "select tipGoriva from Vehicle where idVehicle = ?";
        
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, IDVehicle);
           ResultSet resultSet = preparedStatement.executeQuery();
           
           if(resultSet.next()) return  resultSet.getInt(1);
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
   
    private BigDecimal getPriceFromPackage(int ID)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "select Cena from Package where idPackage = ?";
        
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) return resultSet.getBigDecimal(1);
            else return BigDecimal.ZERO;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal getPotrosnjaVehicle(int IDVehicle)
    {
        Connection connection = DB.getInstance().getConnection();
        String deleteCourier = "select Potrosnja from Vehicle where idVehicle = ?";
        
        
        try( PreparedStatement preparedStatement = connection.prepareStatement(deleteCourier);)
        {
           preparedStatement.setInt(1, IDVehicle);
           ResultSet resultSet = preparedStatement.executeQuery();
           
           if(resultSet.next()) return  resultSet.getBigDecimal(1);
           
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_DistrictOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO;
    }
   
    private boolean updateStatusPonude(int ID, int status)
    {
        Connection connection = DB.getInstance().getConnection();
        String query = "update Offer set StatusPonude = ? where idOffer = ?";
        
        try (PreparedStatement preparedStatement= connection.prepareStatement(query);)
        {
            preparedStatement.setInt(1, status);
            preparedStatement.setInt(2, ID);
            
            return preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException ex)
        {
            Logger.getLogger(av200599_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
}
