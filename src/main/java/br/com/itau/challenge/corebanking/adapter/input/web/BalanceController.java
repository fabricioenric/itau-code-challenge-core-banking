package br.com.itau.challenge.corebanking.adapter.input.web;

import br.com.itau.challenge.corebanking.adapter.input.web.dto.BalanceResponse;
import br.com.itau.challenge.corebanking.domain.model.AccountBalance;
import br.com.itau.challenge.corebanking.port.input.GetBalanceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BalanceController {

    private final GetBalanceUseCase getBalanceUseCase;

    public BalanceController(GetBalanceUseCase getBalanceUseCase) {
        this.getBalanceUseCase = getBalanceUseCase;
    }

    @GetMapping("/balances/{accountId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID accountId) {
        AccountBalance accountBalance = getBalanceUseCase.execute(accountId);
        return ResponseEntity.ok(BalanceResponse.from(accountBalance));
    }
}