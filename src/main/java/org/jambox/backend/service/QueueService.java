package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.Queue;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.QueueRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {
    private final QueueRepository queueRepository;
    private final SpotifyService spotifyService;

    public List<Song> getSongs() {
        Sort sortByCreationDate = Sort.by(Sort.Direction.ASC, "createdAt");
        List<Queue> queueItems = queueRepository.findAll(sortByCreationDate);

        return queueItems.stream().map(Queue::getSong).toList();
    }

    public void addSongToQueue(Song song) {
        Queue queue = new Queue();
        queue.setSong(song);
        queueRepository.save(queue);
        spotifyService.addToUserQueue(song.getSongUrl()).block();
    }

    public List<Queue> getQueue() {
        return queueRepository.findAll();
    }

    public List<Song> getSongs(String search) {
        return queueRepository.findAll().stream().map(Queue::getSong).filter(song -> song.getSongName().contains(search)).toList();
    }
}
