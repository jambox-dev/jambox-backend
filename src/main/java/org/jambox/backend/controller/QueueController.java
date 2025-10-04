package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.ApproveRequest;
import org.jambox.backend.model.QueueAddRequest;
import org.jambox.backend.model.entity.ApprovalQueue;
import org.jambox.backend.model.entity.Settings;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.ApprovalQueueRepository;
import org.jambox.backend.repository.SettingsRepository;
import org.jambox.backend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {
    private final ApprovalQueueService approvalQueueService;
    private final QueueService queueService;
    private final SongService songService;
    private final ApprovalQueueRepository approvalQueueRepository;
    private final SettingsService settingsService;
    private final SettingsRepository settingsRepository;
    private final BlacklistService blacklistService;

    @PostMapping
    public ResponseEntity<Object> addToQueue(@RequestBody QueueAddRequest queueAddRequest) {
        if (queueAddRequest == null || queueAddRequest.getSongUrl() == null || queueAddRequest.getSongUrl().isBlank()) {
            return ResponseEntity.badRequest().body("Invalid Request");
        }

        if (settingsService.getSettings().isBlacklistEnabled()){
            if (blacklistService.isSongInBlacklist(queueAddRequest.getSongUrl())){
                return ResponseEntity.badRequest().body("Song is in blacklist");
            }
        }

        Song song = songService.getDetailsByUrl(queueAddRequest.getSongUrl());

        if (settingsRepository.getSettings().isNeedsApproval()) {
            approvalQueueService.addSongToApprovalQueue(song);
        } else {
            queueService.addSongToQueue(song);
        }

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/settings")
    public void setNeedsApproval(@RequestParam(name = "needs-approval", required = false) Boolean needsApproval, @RequestParam(name = "blacklist-enabled", required = false) Boolean blacklistEnabled) {
        Settings settings = settingsService.getSettings();
        if (needsApproval != null) {
            settings.setNeedsApproval(needsApproval);
        }
        if (blacklistEnabled != null) {
            settings.setBlacklistEnabled(blacklistEnabled);
        }
        settingsService.updateSettings(settings);

    }

    @GetMapping("/settings")
    public Settings getNeedsApproval() {
        return settingsService.getSettings();
    }

    @GetMapping()
    public Song[] getQueue() {
        return queueService.getSongs().toArray(Song[]::new);
    }

    @GetMapping("/needs-approval")
    public ApprovalQueue[] getQueueNeedsApproval() {
        return approvalQueueService.getQueue().toArray(ApprovalQueue[]::new);
    }

    @GetMapping("/search")
    public Song[] searchQueue(@RequestParam(name = "song_name") String songName) {
        return queueService.getSongs(songName).toArray(Song[]::new);
    }

    @GetMapping("/needs-approval/search")
    public ApprovalQueue[] searchQueueNeedsApproval(@RequestParam(name = "song_name") String songName) {
        return approvalQueueService.searchQueueNeedsApproval(songName).toArray(ApprovalQueue[]::new);
    }

    @PostMapping("/approve")
    public void approveSong(@RequestBody ApproveRequest approveRequest){
        if (approveRequest.isApproved()){
            approvalQueueService.approveSong(approveRequest.getQueueId());
        } else {
            approvalQueueService.declineSong(approveRequest.getQueueId());
        }
    }
}
