package com.eduhive.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evenement {
    private Integer id;
    private String nom;
    private String description;
    private String lieu;
    private String date;
    private String organisateur;
} 