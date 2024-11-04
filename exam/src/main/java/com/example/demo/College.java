package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Entity
@Data
public class College {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "College name is mandatory")
    @Size(min = 2, max = 100, message = "College name must be between 2 and 100 characters")
    private String collegeName;

    @NotBlank(message = "College location is mandatory")
    @Size(min = 2, max = 200, message = "College location must be between 2 and 200 characters")
    private String collegeLocation;

    @Min(value = 1000000000L, message = "Contact person's number must be at least 10 digits")
    @Max(value = 9999999999L, message = "Contact person's number cannot exceed 10 digits")
    private Long contactPerseonNumber; // Changed validation to @Min and @Max for number length validation

    @NotBlank(message = "Contact person's name is mandatory")
    @Size(min = 2, max = 100, message = "Contact person's name must be between 2 and 100 characters")
    private String contactPersonName;

    @NotBlank(message = "Contact person's email is mandatory")
    @Email(message = "Contact person's email should be valid")
    private String contactPersonEmail;

    @NotBlank(message = "Username is mandatory")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    private String username;
    //@JsonIgnore
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

   // @NotEmpty(message = "College code is mandatory")
    private int collegeCode;

    @NotBlank(message = "Role is mandatory")
    private String role;

   
    private String status;
}
