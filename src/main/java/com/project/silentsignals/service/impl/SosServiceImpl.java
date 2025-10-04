package com.project.silentsignals.service.impl;

import com.project.silentsignals.dto.SosRequest;
import com.project.silentsignals.entity.Contact;
import com.project.silentsignals.entity.User;
import com.project.silentsignals.repository.UserRepository;
import com.project.silentsignals.service.ISosService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SosServiceImpl implements ISosService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void triggerSos(String userEmail, SosRequest sosRequest) {
        User user = userRepository.findUserByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found!" + userEmail));
        Set<Contact> contacts = user.getContacts();
        if (contacts.isEmpty()) {
            log.warn("User {} triggered SOS but has no contacts.", user.getId());
            return;
        }

    }

    @Override
    @Transactional
    public void resolveSos(Long alertId, String respondingUserEmail) {

    }


}
