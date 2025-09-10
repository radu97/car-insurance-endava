package com.example.carins.service;

import com.example.carins.web.dto.ClaimDto;
import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.ClaimRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;
        //optionally throw NotFound if car does not exist -> implemented in CarController.isInsuranceValid

        List<InsurancePolicy> policies = policyRepository.checkStartDate(carId, date);

        return policies.stream().anyMatch(p -> {
            LocalDate effectiveEnd = (p.getEndDate() != null) ? p.getEndDate() : p.getStartDate().plusYears(1).minusDays(1);
            return !effectiveEnd.isBefore(date);
        });
    }
    
    public boolean existsById(Long carId) {
        return carRepository.existsById(carId);
    }
    
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    
    public ClaimDto registerClaim(Long carId, ClaimDto dto) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found"));

        Claim claim = new Claim(dto.claimDate(),dto.description(),dto.amount());
        claim.setCar(car);

        Claim saved = claimRepository.save(claim);

        return toDto(saved);
    }

    public List<ClaimDto> getClaimsForCar(Long carId) {
        return claimRepository.findByCarIdOrderByClaimDateAsc(carId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ClaimDto toDto(Claim claim) {
        return new ClaimDto(
                claim.getClaimDate(),
                claim.getDescription(),
                claim.getAmount()
        );
    }
}
