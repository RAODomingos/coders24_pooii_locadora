package service;

import dto.CreateVehicleDTO;
import model.vehicle.Vehicle;

import java.util.List;

public interface VehicleService {

    Vehicle createVehicle(CreateVehicleDTO vehicleDTO);

    Vehicle updateVehicle(Vehicle vehicle);

    boolean deleteVehicle(Vehicle vehicle);

    Vehicle findVehicleById(String id);

    List<Vehicle> findAllVehicles();

    Vehicle findVehicleByPlate(String plate);

    List<Vehicle> findVehicleByModel(String model);

    List<Vehicle> findVehicleByBrand(String brand);

    List<Vehicle> findVehicleByAgencyId(String agencyId);

}
