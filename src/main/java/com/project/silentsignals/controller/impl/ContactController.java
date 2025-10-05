package com.project.silentsignals.controller.impl;

import com.project.silentsignals.dto.ContactRequest;
import com.project.silentsignals.dto.ContactResponse;
import com.project.silentsignals.service.IContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final IContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> addContact(@RequestBody ContactRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        ContactResponse newContact = contactService.addContact(userDetails.getUsername(), request);
        return new ResponseEntity<>(newContact, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getUserContacts(@AuthenticationPrincipal UserDetails userDetails) {
        List<ContactResponse> contacts = contactService.getContacts(userDetails.getUsername());
        return ResponseEntity.ok(contacts);
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId, @AuthenticationPrincipal UserDetails userDetails) {
        contactService.deleteContact(contactId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}