package br.com.itau.challenge.corebanking.adapter.input.web.dto;

public record ErrorResponse(
    String code,
    String message
) {}