package com.eduhive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stage {
    private Integer id;
    private String titre;
    private String entreprise;
    private String description;
    private String duree;
} 