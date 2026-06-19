package com.utp.meditrackapp.application.dto;

/**
 * KPIs principales del Dashboard.
 */
public record DashboardKpiDTO(
    int totalProductos,
    int stockCritico,
    int lotesPorVencer,
    int saludInventario,
    double valorInventario,
    int volumenMovimientos
) {}
