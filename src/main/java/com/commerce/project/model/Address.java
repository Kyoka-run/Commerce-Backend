package com.commerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 3, message = "Postcode must be at least 5 characters")
    private String postcode;

    @NotBlank
    @Size(min = 3, message = "Street must be at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 3, message = "City must be at least 5 characters")
    private String city;

    @NotBlank
    @Size(min = 2, message = "Country must be at least 5 characters")
    private String country;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String postcode, String street, String city, String country) {
        this.postcode = postcode;
        this.street = street;
        this.city = city;
        this.country = country;
    }
}
