package com.example.carins.repo;

import com.example.carins.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    @Query("""
    	    select p
    	    from InsurancePolicy p
    	    where p.car.id = :carId
    	      and p.startDate <= :date
    	    """)
    List<InsurancePolicy> checkStartDate(@Param("carId") Long carId, @Param("date") LocalDate date);

    List<InsurancePolicy> findByCarId(Long carId);
    List<InsurancePolicy> findByEndDate(LocalDate endDate);
}