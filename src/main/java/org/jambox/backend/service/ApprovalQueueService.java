package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.ApprovalQueue;
import org.jambox.backend.model.entity.Queue;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.ApprovalQueueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalQueueService {

    private final ApprovalQueueRepository approvalQueueRepository;
    private final SpotifyService spotifyService;
    private final SongService songService;
    private final QueueService queueService;


    public void approveSong(String queueId) {
        ApprovalQueue approvalQueue = approvalQueueRepository.findById(queueId).orElse(null);
        if (approvalQueue == null) {
            throw new IllegalArgumentException("ApprovalQueue not found");
        }
        Queue queue = new Queue();
        queue.setSong(approvalQueue.getSong());
        queueService.addSongToQueue(approvalQueue.getSong());
        approvalQueueRepository.deleteById(approvalQueue.getId());
    }

    public void declineSong(String queueId) {
        ApprovalQueue approvalQueue = approvalQueueRepository.findById(queueId).orElse(null);
        if (approvalQueue == null) {
            return;
        }
        approvalQueueRepository.deleteById(queueId);
    }

    public List<ApprovalQueue> searchQueueNeedsApproval(String search) {
        return approvalQueueRepository.findAll().stream().filter(queue -> queue.getSong().getSongName().contains(search)).toList();
    }

    public List<ApprovalQueue> getQueue() {
        return approvalQueueRepository.findAll();
    }

    public void addSongToApprovalQueue(Song song) {
        ApprovalQueue approvalQueue = new ApprovalQueue();
        approvalQueue.setSong(song);
        approvalQueueRepository.save(approvalQueue);
    }
}
