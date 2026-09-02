package com.ride.goeasy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.AvailableVehicleDTO;
import com.ride.goeasy.dto.LocationResponse;
import com.ride.goeasy.dto.MatrixResponse;
import com.ride.goeasy.dto.ReverseGeoResponse;
import com.ride.goeasy.dto.VehicleDetailDTO;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.exception.VehicleNotFoundException;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class VehicleService {

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private VehicleRepo vehicleRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${locationiq.api.key}")
    private String apiKey;

    private static final String LOCATION_API = "https://us1.locationiq.com/v1/search";
    private static final String MATRIX_API   = "https://us1.locationiq.com/v1/matrix/driving/";

    public AvailableVehicleDTO getAvailableVehicles(
            Long mobile,
            String destinationLocation,
            Boolean preferFemaleDriver,
            Boolean greenOnly,
            String ridePreference) {

        // STEP 1: Destination coordinates
        double destLat = 12.9716;
        double destLon = 77.5946;
        try {
            String url = LOCATION_API + "?key=" + apiKey + "&q=" + destinationLocation + "&format=json";
            LocationResponse[] locationRes = restTemplate.getForObject(url, LocationResponse[].class);
            if (locationRes != null && locationRes.length > 0) {
                destLat = Double.parseDouble(locationRes[0].getLat());
                destLon = Double.parseDouble(locationRes[0].getLon());
            }
        } catch (Exception e) {
            System.err.println("Location API Timeout (VehicleService): " + e.getMessage());
        }

        // STEP 2: Fetch customer
        Customer customer = customerRepo.findByMobno(mobile)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobile));

        String[] src = customer.getCurrentLocation().split(",");
        double srcLat = Double.parseDouble(src[0]);
        double srcLon = Double.parseDouble(src[1]);

        // STEP 3: Distance via Matrix API
        double distance = 5.0;
        try {
            String finalURL = MATRIX_API + srcLon + "," + srcLat + ";" + destLon + "," + destLat
                    + "?key=" + apiKey + "&annotations=distance,duration";
            MatrixResponse matrixResponse = restTemplate.getForObject(finalURL, MatrixResponse.class);
            if (matrixResponse != null && matrixResponse.getDistances() != null) {
                distance = matrixResponse.getDistances().get(0).get(1) / 1000.0;
            }
        } catch (Exception e) {
            System.err.println("Matrix API Timeout: " + e.getMessage());
        }

        // STEP 4: Reverse-geocode customer city
        String city = "Bangalore";
        try {
            String reverseUrl = "https://us1.locationiq.com/v1/reverse?key=" + apiKey
                    + "&lat=" + srcLat + "&lon=" + srcLon + "&format=json";
            ReverseGeoResponse reverseResponse = restTemplate.getForObject(reverseUrl, ReverseGeoResponse.class);
            if (reverseResponse != null && reverseResponse.getAddress() != null) {
                String apiCity = reverseResponse.getAddress().getCity();
                if (apiCity != null) city = apiCity;
            }
        } catch (Exception e) {
            System.err.println("Reverse Geocoding Failed: " + e.getMessage());
        }

        // STEP 5: Fetch vehicles applying female + green filters via DB queries
        boolean femaleOnly = Boolean.TRUE.equals(preferFemaleDriver);
        boolean green      = Boolean.TRUE.equals(greenOnly);

        List<Vehicle> availableVehicles;
        if (femaleOnly && green) {
            availableVehicles = vehicleRepo.findAvailableFemaleDriverGreenVehiclesInCity(city);
        } else if (femaleOnly) {
            availableVehicles = vehicleRepo.findAvailableFemaleDriverVehiclesInCity(city);
        } else if (green) {
            availableVehicles = vehicleRepo.findAvailableGreenVehiclesInCity(city);
        } else {
            availableVehicles = vehicleRepo.findAvailableVehiclesInCity(city);
        }

        // STEP 6: Apply ride-preference filter in Java (small data set)
        final String pref = (ridePreference != null && !ridePreference.isBlank()) ? ridePreference.toUpperCase() : null;
        if (pref != null && !pref.equals("ANY")) {
            availableVehicles = availableVehicles.stream()
                    .filter(v -> v.getDriver() != null &&
                                 pref.equalsIgnoreCase(v.getDriver().getRidePreference()))
                    .collect(java.util.stream.Collectors.toList());
        }

        System.out.println("DEBUG: city=" + city + ", vehicles=" + availableVehicles.size()
                + ", femaleOnly=" + femaleOnly + ", green=" + green + ", pref=" + pref);

        Double penaltyAmount = customer.getPenaltyAmount();
        if (penaltyAmount == null) penaltyAmount = 0.0;

        List<VehicleDetailDTO> vehicleDetails = new ArrayList<>();

        for (Vehicle v : availableVehicles) {
            VehicleDetailDTO dto = new VehicleDetailDTO();
            dto.setModel(v.getVehicleModel());
            dto.setVehicleModel(v.getVehicleModel());
            dto.setVehicleNumber(v.getVehicleNumber());
            dto.setVehicleCapacity(v.getVehicleCapacity());
            dto.setFuelType(v.getFuelType() != null ? v.getFuelType() : "PETROL");

            Double dbSpeed = v.getAvgspeed();
            dto.setAverageSpeed(dbSpeed != null ? dbSpeed : 45.0);
            dto.setPricePerKm(v.getPricePerKm() != null ? v.getPricePerKm() : 12.0);
            dto.setDistance(distance);

            // Transparent fare breakdown
            double adjustedRate = (v.getPricePerKm() != null && v.getPricePerKm() < 25) ? v.getPricePerKm() : 12.0;
            double baseFare     = 100.0;
            double distanceFare = distance * adjustedRate;
            double fare         = baseFare + distanceFare;
            double totalAmount  = fare + penaltyAmount;

            dto.setBaseFare(baseFare);
            dto.setDistanceFare(distanceFare);
            dto.setEstimatedFare(fare);
            dto.setPenalty(penaltyAmount);
            dto.setTotalAmout(totalAmount);

            Double avgSpeed = v.getAvgspeed();
            if (avgSpeed == null || avgSpeed <= 0) avgSpeed = 45.0;
            dto.setEstimatedTime((distance / avgSpeed) * 60);

            // Driver info
            Driver d = v.getDriver();
            if (d != null) {
                dto.setDriverName(d.getDname());
                dto.setDriverGender(d.getGender());
                dto.setDriverRidePreference(d.getRidePreference());
                dto.setDriverVerificationTier(d.getVerificationTier());
                dto.setDriverRating(d.getDriverRating() != null ? d.getDriverRating() : 0.0);
                dto.setDriverTotalRatings(d.getTotalRatings() != null ? d.getTotalRatings() : 0);
                dto.setDriverReliabilityScore(d.getReliabilityScore() != null ? d.getReliabilityScore() : 100.0);
            }

            vehicleDetails.add(dto);
        }

        AvailableVehicleDTO response = new AvailableVehicleDTO();
        response.setCustomer(customer);
        response.setDistance(distance);
        response.setSource(customer.getCurrentLocation());
        response.setDestination(destinationLocation);
        response.setVehicles(vehicleDetails);

        return response;
    }

    public ResponseStructure<Vehicle> findByVno(String vehicleNumber) {
        Vehicle vehicle = vehicleRepo.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle Not Found: " + vehicleNumber));

        ResponseStructure<Vehicle> rs = new ResponseStructure<>();
        rs.setData(vehicle);
        rs.setMessage("Vehicle found successfully");
        rs.setStatusCode(HttpStatus.OK.value());
        return rs;
    }
}
