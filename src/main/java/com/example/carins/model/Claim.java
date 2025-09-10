package com.example.carins.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
public class Claim {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne(optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
	
    @NotNull(message = "Claim date cannot be null")
	private String claimDate;
	
	@NotBlank @Size(min = 5, max = 255)
	private String description;
	
	@NotNull(message = "Amount cannot be null")
	@Positive(message = "Amount must be positive")
	private Double amount;
	
	public Claim() {}
	public Claim(String claimDate, String description, double amount) {
		this.car = car;
		this.claimDate = claimDate;
		this.description = description;
		this.amount = amount;
	}
	
	public Long getId() {return id;}
	
	public String getClaimDate() {return claimDate;}
	public void String(String d) {claimDate = d;}
	
	public String getDescription() {return description;}
	public void setDescription(String d) {description = d;}
	
	public Double getAmount() {return amount;}
	public void setAmount(Double a) {amount = a;}
	
	public void setCar(Car car) {
        this.car = car;
    }
}