package br.com.itau.challenge.hello.adapter.input.web.dto;

public record ErrorResponse(
    String code,
    String message
) {}