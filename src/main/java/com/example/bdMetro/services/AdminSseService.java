package com.example.bdMetro.services;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AdminSseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> publicEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        if (emitters.size() >= 50) {
            SseEmitter rejected = new SseEmitter();
            rejected.completeWithError(new IllegalStateException("Too many subscribers"));
            return rejected;
        }
        SseEmitter emitter = new SseEmitter(300_000L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public SseEmitter subscribePublic() {
        if (publicEmitters.size() >= 200) {
            SseEmitter rejected = new SseEmitter();
            rejected.completeWithError(new IllegalStateException("Too many subscribers"));
            return rejected;
        }
        SseEmitter emitter = new SseEmitter(300_000L);
        publicEmitters.add(emitter);
        emitter.onCompletion(() -> publicEmitters.remove(emitter));
        emitter.onTimeout(() -> publicEmitters.remove(emitter));
        emitter.onError(e -> publicEmitters.remove(emitter));
        return emitter;
    }

    public void broadcast(String eventName, Object data) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }

    public void broadcastPublic(String eventName, Object data) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : publicEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        publicEmitters.removeAll(dead);
    }

    @Scheduled(fixedDelay = 20000)
    public void sendHeartbeat() {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("ping").data(""));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
        List<SseEmitter> deadPublic = new ArrayList<>();
        for (SseEmitter emitter : publicEmitters) {
            try {
                emitter.send(SseEmitter.event().name("ping").data(""));
            } catch (Exception e) {
                deadPublic.add(emitter);
            }
        }
        publicEmitters.removeAll(deadPublic);
    }
}
