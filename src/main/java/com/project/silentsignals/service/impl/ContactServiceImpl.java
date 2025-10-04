package com.project.silentsignals.service.impl;

import com.project.silentsignals.dto.ContactRequest;
import com.project.silentsignals.dto.ContactResponse;
import com.project.silentsignals.entity.Contact;
import com.project.silentsignals.entity.User;
import com.project.silentsignals.exception.ContactAlreadyException;
import com.project.silentsignals.repository.ContactRepository;
import com.project.silentsignals.repository.UserRepository;
import com.project.silentsignals.service.IContactService;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements IContactService {
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ContactResponse addContact(String ownerEmail, ContactRequest contactRequest) {
        User owner = userRepository.findUserByEmail(ownerEmail).orElseThrow(() -> new EntityNotFoundException("User not found" + ownerEmail));
        User contactUser = userRepository.findUserByEmail(contactRequest.contactEmail()).orElseThrow(() -> new RuntimeException("Contact Not found"));
        if (owner.getId().equals(contactUser.getId())) {
            throw new IllegalArgumentException("You cannot add yourself as a contact");
        }
        boolean exists = owner.getContacts().stream().anyMatch(contact -> contact.getContactUser().getId().equals(contactUser.getId()));
        if (exists) {
            throw new RuntimeException("This contact already added");

        }
        Contact contact = Contact.builder().owner(owner).contactUser(contactUser).build();
        contactRepository.save(contact);
        log.info("Contact succesfully added");
        return toContactResponse(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(String ownerEmail) {
        User owner = userRepository.findUserByEmail(ownerEmail).orElseThrow(() -> new EntityNotFoundException("This user not found!"));

        return owner.getContacts().stream().map(this::toContactResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteContact(Long contactId, String ownerEmail) {
        User owner = userRepository.findUserByEmail(ownerEmail).orElseThrow(() -> new EntityNotFoundException("This User not found"));
        Contact contact = contactRepository.findById(contactId).orElseThrow(() -> new EntityNotFoundException("Thsi contact not found!"));

        if (!contact.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("User does not have permission to delete this contact");
        }
        contactRepository.delete(contact);
        log.info("User {} deleted contact with id {}.", owner.getId(), contactId);
    }


    private ContactResponse toContactResponse(Contact contact) {
        User contactUser = contact.getContactUser();
        return new ContactResponse(contact.getId(), contactUser.getUsername(), contactUser.getEmail(), contactUser.getPhoneNumber());
    }
}
