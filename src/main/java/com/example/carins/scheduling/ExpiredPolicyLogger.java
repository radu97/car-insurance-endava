package com.example.carins.scheduling;

import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.model.InsurancePolicy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ExpiredPolicyLogger {
	private static final Logger logger = Logger.getLogger(ExpiredPolicyLogger.class.getName());
    private final InsurancePolicyRepository policyRepository;

    public ExpiredPolicyLogger(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    
    @Scheduled(cron = "0 01 00 * * *") //0s 1m 0h
    public void logExpiredPolicies() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Find policies that expired at midnight today, but not yet logged
        List<InsurancePolicy> expiredPolicies = policyRepository
                .findByEndDate(yesterday);
        
        for (InsurancePolicy policy : expiredPolicies) {
            logger.info(String.format(
                "Policy %s for car %s expired on %s",
                policy.getId(),
                policy.getCar().getId(),
                policy.getEndDate()
            ));

            policyRepository.save(policy);
        }
    }
}
