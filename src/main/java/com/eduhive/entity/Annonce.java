package com.eduhive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Annonce {
    private Integer id;
    private String titre;
    private String description;
    private String categorie;
} 