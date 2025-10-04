package com.project.silentsignals.service;

import com.project.silentsignals.dto.ContactRequest;
import com.project.silentsignals.dto.ContactResponse;

import java.util.List;

public interface IContactService {
    public ContactResponse addContact(String ownerEmail, ContactRequest contactRequest);

    public List< ContactResponse> getContacts(String ownerEmail);

    public void deleteContact(Long contactId, String ownerEmail);
}
