package com.project.Service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.Model.BlackList;
import com.project.Model.WhiteList;
import com.project.Repository.BlackListRepository;
import com.project.Repository.WhiteListRepository;

@Service
public class ListasService {
    private final BlackListRepository blackListRepository;   // <- seu nome
    private final WhiteListRepository whiteListRepository;   // <- seu nome

    private volatile Set<String> cacheBlack = Set.of();
    private volatile Set<String> cacheWhite = Set.of();
    private volatile long cacheExpiraEpoch  = 0;
    private static final long TTL_SECONDS   = 300;   // 5 min

    public ListasService(BlackListRepository blRepo,
                         WhiteListRepository wlRepo) {
        this.blackListRepository = blRepo;
        this.whiteListRepository = wlRepo;
    }

    /* ---------- mesma lógica --------------- */
    private void refreshIfNeeded() {
        long now = System.currentTimeMillis() / 1000;
        if (now > cacheExpiraEpoch) {
            cacheBlack = blackListRepository.findAll().stream()
                         .map(BlackList::getNumero)
                         .collect(Collectors.toSet());

            cacheWhite = whiteListRepository.findAll().stream()
                         .map(WhiteList::getNumero)
                         .collect(Collectors.toSet());

            cacheExpiraEpoch = now + TTL_SECONDS;
        }
    }

    public boolean emWhitelist(String n) { refreshIfNeeded(); return cacheWhite.contains(n); }
    public boolean emBlacklist(String n) { refreshIfNeeded(); return cacheBlack.contains(n); }
}
