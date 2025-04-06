package com.spring.springsecsection1.controller;

import com.spring.springsecsection1.model.Cards;
import com.spring.springsecsection1.model.Customer;
import com.spring.springsecsection1.repository.CardsRepository;
import com.spring.springsecsection1.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CardsController {

    private final CardsRepository cardsRepository;
    private final CustomerRepository customerRepository;

    @GetMapping("/myCards")
    public List<Cards> getCardDetails(@RequestParam String email) {
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(email);
        if(optionalCustomer.isPresent()) {
            List<Cards> cards = cardsRepository.findByCustomerId(optionalCustomer.get().getId());
            if (cards != null ) {
                return cards;
            }else {
                return null;
            }
        } else {
            return null;
        }

    }

}