package rs.etf.sab.student;

import rs.etf.sab.tests.*;
import rs.etf.sab.operations.*;
import rs.etf.sab.asserts.*;

public class StudentMain {

    public static void main(String[] args) {
        
        //mainTester();
        
        CityOperations cityOperations = new av200599_OperationsStudent(); // Change this to your implementation.
        DistrictOperations districtOperations = new av200599_DistrictOperations(); // Do it for all classes.
        CourierOperations courierOperations = new av200599_CourierOperations(); // e.g. = new MyDistrictOperations();
        CourierRequestOperation courierRequestOperation = new av200599_CourierRequestOperatios();
        GeneralOperations generalOperations = new av200599_GeneralOperations();
        UserOperations userOperations = new av200599_UserOperations();
        VehicleOperations vehicleOperations = new av200599_VehicleOperations();
        PackageOperations packageOperations = new av200599_PackageOperations();

        rs.etf.sab.tests.TestHandler.createInstance(cityOperations, courierOperations, courierRequestOperation, districtOperations, generalOperations, userOperations, vehicleOperations, packageOperations);
        rs.etf.sab.tests.TestRunner.runTests();
    }
    
    
    private static void mainTester()
    {
        av200599_OperationsStudent cityOperationsStudent = new av200599_OperationsStudent();
        cityOperationsStudent.getAllCities();
    }
}
