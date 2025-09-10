package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }
    
    private ResponseEntity<?> checkDate(String date, LocalDate d1, LocalDate d2) {
//      Validate date format
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest() //400
                    .body("Invalid date format, expected YYYY-MM-DD");
        }

//      Validate date range
        if (d.isBefore(d1) || d.isAfter(d2)) {
            return ResponseEntity.badRequest() //400
                    .body("Date is outside supported range");
        }
        
        return null;
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        // validate date format and handle errors consistently
    	if (!service.existsById(carId)) {
            return ResponseEntity.notFound().build(); //404
        }
    	
//      Validate date format
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest() //400
                    .body("Invalid date format, expected YYYY-MM-DD");
        }

//      Validate date range
        if (d.isBefore(LocalDate.of(2000, 1, 1)) || d.isAfter(LocalDate.of(2100, 12, 31))) {
            return ResponseEntity.badRequest() //400
                    .body("Date is outside supported range (expected range 2000 – 2100)");
        }
        
        boolean valid = service.isInsuranceValid(carId, d);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
    }
    
    
    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<?> registerClaim(@PathVariable Long carId, @RequestBody ClaimDto claimDto) {
//    	cannot use a date older than two weeks ago
    	ResponseEntity<?> cd = checkDate(claimDto.claimDate(), LocalDate.now().minusWeeks(2), LocalDate.now());
        if(cd != null)
        	return cd;
        if(claimDto.description().length() == 0)
			return ResponseEntity.badRequest() //400
                  .body("description cannot be empty");
        if(claimDto.amount() < 10)
        	return ResponseEntity.badRequest() //400
                    .body("amount cannot be lower than 10");
        
        try {
        	ClaimDto saved = service.registerClaim(carId, claimDto);
        	
        	URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()       // /cars/{carId}/claims
                    .path("/{id}")              // /cars/{carId}/claims/{claimId}
                    .buildAndExpand(carId)
                    .toUri();
        	
            return ResponseEntity.created(location).body(saved);
    	}
    	catch(EntityNotFoundException e) {
    		return ResponseEntity.notFound().build(); //404
    	}
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<List<ClaimDto>> getCarHistory(@PathVariable Long carId) {
    	if (!service.existsById(carId)) {
            return ResponseEntity.notFound().build(); //404
        }
    	
        List<ClaimDto> claims = service.getClaimsForCar(carId);
        return ResponseEntity.ok(claims);
    }
    

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}
