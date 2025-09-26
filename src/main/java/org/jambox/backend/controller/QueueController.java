package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.ApproveRequest;
import org.jambox.backend.model.QueueAddRequest;
import org.jambox.backend.model.entity.ApprovalQueue;
import org.jambox.backend.model.entity.Settings;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.ApprovalQueueRepository;
import org.jambox.backend.service.ApprovalQueueService;
import org.jambox.backend.service.QueueService;
import org.jambox.backend.service.SettingsService;
import org.jambox.backend.service.SongService;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jambox.needsApproval}")
    private boolean needsApproval;

    @PostMapping
    public ResponseEntity<Song> addToQueue(@RequestBody QueueAddRequest queueAddRequest) {
        Song song = songService.getDetailsByUrl(queueAddRequest.getSongUrl());
        if (needsApproval) {
            approvalQueueService.addSongToApprovalQueue(song);
        } else {
            queueService.addSongToQueue(song);
        }
        return ResponseEntity.status(201).build();
    }

    @PostMapping
    public void setNeedsApproval(@RequestParam(name = "needs-approval") boolean needsApproval) {
        Settings settings = settingsService.getSettings();
        settings.setNeedsApproval(needsApproval);
        settingsService.updateSettings(settings);
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
