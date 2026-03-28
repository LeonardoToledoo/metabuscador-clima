package com.metaclima.module;

import lombok.Data;

@Data 
public class DadosClima {
    private String cidade;
    private double temperatura;
    private double vento;
    private String fonte;}