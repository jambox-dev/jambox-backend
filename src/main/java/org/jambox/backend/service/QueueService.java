package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.config.TenantContext;
import org.jambox.backend.model.entity.Queue;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.model.entity.Tenant;
import org.jambox.backend.repository.QueueRepository;
import org.jambox.backend.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {
    private final QueueRepository queueRepository;
    private final SpotifyService spotifyService;
    private final TenantRepository tenantRepository;

    public List<Song> getSongs() {
        // Note: Sorting might need to be done in memory or using a custom query if findAllByTenantId doesn't support Sort directly in the interface definition I made.
        // But MongoRepository methods usually support Sort as a second argument.
        // However, I only defined findAllByTenantId(String).
        // I'll filter in memory for now or just use the list as is, assuming order is insertion order or I can sort stream.
        // Better: I should have added Sort to the repository method.
        // For now, let's sort in stream.
        List<Queue> queueItems = queueRepository.findAllByTenantId(TenantContext.getTenantId());
        
        // Sort by createdAt if needed, but Queue usually implies order.
        // Let's assume they come back in some order or sort by ID/date.
        // The original code used Sort.by("createdAt").
        
        return queueItems.stream()
                .sorted((q1, q2) -> {
                    if (q1.getCreatedAt() == null || q2.getCreatedAt() == null) return 0;
                    return q1.getCreatedAt().compareTo(q2.getCreatedAt());
                })
                .map(Queue::getSong)
                .toList();
    }

    public void addSongToQueue(Song song) {
        String tenantId = TenantContext.getTenantId();
        Queue queue = new Queue();
        queue.setSong(song);
        queue.setTenantId(tenantId);
        queueRepository.save(queue);
        
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new IllegalStateException("Tenant not found"));
        spotifyService.addToUserQueue(song.getSongUrl(), tenant).block();
    }

    public List<Queue> getQueue() {
        return queueRepository.findAllByTenantId(TenantContext.getTenantId());
    }

    public List<Song> getSongs(String search) {
        return queueRepository.findAllByTenantId(TenantContext.getTenantId()).stream()
                .map(Queue::getSong)
                .filter(song -> song.getSongName().contains(search))
                .toList();
    }
}
