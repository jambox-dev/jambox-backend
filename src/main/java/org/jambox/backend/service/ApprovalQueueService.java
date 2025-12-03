package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.config.TenantContext;
import org.jambox.backend.model.entity.ApprovalQueue;
import org.jambox.backend.model.entity.Queue;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.ApprovalQueueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApprovalQueueService {

    private final ApprovalQueueRepository approvalQueueRepository;
    private final QueueService queueService;


    public void approveSong(String queueId) {
        ApprovalQueue approvalQueue = approvalQueueRepository.findById(queueId).orElse(null);
        if (approvalQueue == null) {
            throw new IllegalArgumentException("ApprovalQueue not found");
        }
        // Ensure it belongs to current tenant
        if (!Objects.equals(approvalQueue.getTenantId(), TenantContext.getTenantId())) {
             throw new IllegalArgumentException("ApprovalQueue not found for this tenant");
        }
        
        queueService.addSongToQueue(approvalQueue.getSong());
        approvalQueueRepository.deleteById(approvalQueue.getId());
    }

    public void declineSong(String queueId) {
        ApprovalQueue approvalQueue = approvalQueueRepository.findById(queueId).orElse(null);
        if (approvalQueue == null) {
            return;
        }
        if (!Objects.equals(approvalQueue.getTenantId(), TenantContext.getTenantId())) {
            return;
        }
        approvalQueueRepository.deleteById(queueId);
    }

    public List<ApprovalQueue> searchQueueNeedsApproval(String search) {
        return approvalQueueRepository.findAllByTenantId(TenantContext.getTenantId()).stream()
                .filter(queue -> queue.getSong().getSongName().contains(search))
                .toList();
    }

    public List<ApprovalQueue> getQueue() {
        return approvalQueueRepository.findAllByTenantId(TenantContext.getTenantId());
    }

    public void addSongToApprovalQueue(Song song) {
        ApprovalQueue approvalQueue = new ApprovalQueue();
        approvalQueue.setSong(song);
        approvalQueue.setTenantId(TenantContext.getTenantId());
        approvalQueueRepository.save(approvalQueue);
    }
}
