package com.autoecole.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.autoecole.school.School;
import com.autoecole.school.SchoolRepository;
import com.autoecole.user.AppUser;
import com.autoecole.user.AppUserRepository;
import com.autoecole.user.Role;

/**
 * Seed de démo (profiles dev / docker uniquement) : une auto-école + un compte gérant,
 * pour la checklist doc §15 et les démos prospects. En prod : profiles > staging sans ce bean.
 */
@Component
@Profile({"dev", "docker"})
public class DevDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private final SchoolRepository schools;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    private final String ownerEmail;
    private final String ownerPassword;

    public DevDataInitializer(SchoolRepository schools, AppUserRepository users,
                              PasswordEncoder passwordEncoder,
                              @org.springframework.beans.factory.annotation.Value("${app.demo.owner-email}") String ownerEmail,
                              @org.springframework.beans.factory.annotation.Value("${app.demo.owner-password}") String ownerPassword) {
        this.schools = schools;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.ownerEmail = ownerEmail;
        this.ownerPassword = ownerPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        School school = schools.findAll().stream().findFirst().orElseGet(() -> {
            School s = new School();
            s.setName("Auto-école Démo");
            s.setEmail("contact@autoecole-demo.dev");
            return schools.save(s);
        });

        if (!users.existsByEmailIgnoreCaseAndActiveTrue(ownerEmail)) {
            AppUser owner = new AppUser();
            owner.setSchoolId(school.getId());
            owner.setEmail(ownerEmail);
            owner.setPasswordHash(passwordEncoder.encode(ownerPassword));
            owner.setRole(Role.OWNER);
            users.save(owner);
            log.info("Seed dev : compte gérant créé → {} / {}", ownerEmail, "(mot de passe via app.demo.owner-password)");
        }
    }
}
