package com.spring.springsecsection1.controller;

import com.spring.springsecsection1.model.Contact;
import com.spring.springsecsection1.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;

    @PostMapping("/contact")
    @PostFilter("filterObject.contactName != 'Test'")
    public List<Contact> saveContactInquiryDetails(@RequestBody List<Contact> contacts) {
        List<Contact> returnContacts = new ArrayList<>();
        if(!contacts.isEmpty()) {
            Contact contact = contacts.getFirst();
            contact.setContactId(getServiceReqNumber());
            contact.setCreateDt(new Date(System.currentTimeMillis()));
            Contact savedContact =  contactRepository.save(contact);
            returnContacts.add(savedContact);
        }
        return returnContacts;
    }

    public String getServiceReqNumber() {
        Random random = new Random();
        int ranNum = random.nextInt(999999999 - 9999) + 9999;
        return "SR" + ranNum;
    }
}