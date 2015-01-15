package com.malmstein.hnews.feed;

import com.malmstein.hnews.tasks.FetchShowHNTask;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class NewsRetriever<S> implements StoriesRetriever<StoriesUpdateEvent> {

    private final DatabasePersister databasePersister;

    public NewsRetriever(DatabasePersister databasePersister) {
        this.databasePersister = databasePersister;
    }

    @Override
    public Observable<StoriesUpdateEvent> fetch(Long... params) {
        return Observable.create(
                new StoriesUpdateOnSubscribe(databasePersister))
                    .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<StoriesUpdateEvent> fetchTop() {
        return null;
    }

    @Override
    public Observable<StoriesUpdateEvent> fetchNew() {
        return null;
    }

    @Override
    public Observable<StoriesUpdateEvent> fetchBest() {
        return null;
    }

    @Override
    public Observable<StoriesUpdateEvent> fetchShow() {
        return null;
    }

    @Override
    public Observable<StoriesUpdateEvent> fetchAsk() {
        return null;
    }

    private static class StoriesUpdateOnSubscribe implements Observable.OnSubscribe<StoriesUpdateEvent> {

        private final DatabasePersister databasePersister;
        private Subscriber<? super StoriesUpdateEvent> subscriber;

        private StoriesUpdateOnSubscribe(DatabasePersister databasePersister) {
            this.databasePersister = databasePersister;
        }

        @Override
        public void call(Subscriber<? super StoriesUpdateEvent> subscriber) {
            this.subscriber = subscriber;
            startFetchingTopsStories();
            subscriber.onCompleted();
        }

        private void startFetchingTopsStories() {
            subscriber.onNext(new StoriesUpdateEvent(StoriesUpdateEvent.Type.REFRESH_STARTED));
            try {
                createFetchTopStoriesTask().execute();
            } catch (IOException e) {
                subscriber.onError(e);
            }
            subscriber.onNext(new StoriesUpdateEvent(StoriesUpdateEvent.Type.REFRESH_FINISHED));
        }

        private FetchShowHNTask createFetchTopStoriesTask() {
            return new FetchShowHNTask(databasePersister);
        }

    }

}