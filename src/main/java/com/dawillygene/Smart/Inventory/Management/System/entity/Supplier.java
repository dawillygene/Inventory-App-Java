package com.dawillygene.Smart.Inventory.Management.System.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Supplier entity representing product suppliers
 */
@Entity
@Table(name = "suppliers")
@EntityListeners(AuditingEntityListener.class)
public class Supplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name must not exceed 100 characters")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Contact person is required")
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    @Column(name = "contact_person", nullable = false)
    private String contactPerson;
    
    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "tax_number")
    private String taxNumber;
    
    @Column(name = "payment_terms")
    private String paymentTerms;
    
    @Column(name = "delivery_time_days")
    private Integer deliveryTimeDays;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    // Default constructor
    public Supplier() {}
    
    // All args constructor
    public Supplier(Long id, String name, String contactPerson, String email, String phoneNumber, 
                   String address, String website, String taxNumber, String paymentTerms, 
                   Integer deliveryTimeDays, Boolean isActive, String notes, 
                   LocalDateTime createdAt, LocalDateTime updatedAt, List<Product> products) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.website = website;
        this.taxNumber = taxNumber;
        this.paymentTerms = paymentTerms;
        this.deliveryTimeDays = deliveryTimeDays;
        this.isActive = isActive;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.products = products != null ? products : new ArrayList<>();
    }

    // Constructor for creating supplier with basic info
    public Supplier(String name, String contactPerson, String email) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.isActive = true;
        this.products = new ArrayList<>();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public Integer getDeliveryTimeDays() { return deliveryTimeDays; }
    public void setDeliveryTimeDays(Integer deliveryTimeDays) { this.deliveryTimeDays = deliveryTimeDays; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products != null ? products : new ArrayList<>(); }

    // Helper method to get product count
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }
}
